/*
 * Copyright 2024 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.tools;

import dev.silverandro.broadsword.lookups.ClassMappingLookup;
import dev.silverandro.broadsword.lookups.OutputStreamFactory;
import dev.silverandro.broadsword.mappings.MappingsSet;
import dev.silverandro.broadsword.meta.CTags;
import dev.silverandro.broadsword.meta.CommonNames;
import dev.silverandro.broadsword.meta.ConstantPoolTracker;
import dev.silverandro.broadsword.meta.DataUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

public final class TrueClassFileRemapper {
    public static void remapClassBytes(byte[] classFile, MappingsSet mappingsSet, ClassMappingLookup classInfoReq, OutputStreamFactory outputReq) throws IOException {
        var input = ByteBuffer.wrap(classFile);

        // 2 concurrent output streams that we'll stitch together after
        var backingConstantPoolOS = new ByteArrayOutputStream();
        var backingClassBodyOS = new ByteArrayOutputStream();
        var constantPoolOS = new DataOutputStream(backingConstantPoolOS);
        var classBodyOS = new DataOutputStream(backingClassBodyOS);

        //  u4  magic;
        //  u2  minor_version;
        //  u2  major_version;
        var magic = input.getLong();

        //  u2  constant_pool_count;
        var count = input.getShort();
        var index = 1;

        // To be populated later, lets us remap field and method attr
        UTF8Container thisClass;
        UTF8Container superClass;

        // Copy all UFT-8 data into memory to assist in remapping
        var utf8Copy = new UTF8Container[count];
        var tracker = new ConstantPoolTracker(count);

        // Read through all the constant pool entries
        while (index < count) {
            var tag = input.get();
            tracker.putOffset(index, input.position());
            switch (tag) {
                case CTags.UTF8 -> {
                    var length = input.getShort();
                    UTF8Container container = DataUtil.readBytes(length, input);
                    utf8Copy[index] = container;
                }

                case CTags.CLASS -> {
                    var contentIndex = input.getShort();
                    tracker.putClass(index, contentIndex);
                }

                case CTags.FIELD -> {
                    var classIndex = input.getShort();
                    var ntIndex = input.getShort();
                    tracker.putFieldNT(ntIndex);
                    tracker.putNtClass(ntIndex, classIndex);
                }

                case CTags.METHOD, CTags.INTERFACE_METHOD -> {
                    var classIndex = input.getShort();
                    var ntIndex = input.getShort();
                    tracker.putMethodNT(ntIndex);
                    tracker.putNtClass(ntIndex, classIndex);
                }

                case CTags.DYNAMIC, CTags.INVOKE_DYNAMIC -> {
                    input.getShort(); // index into attribute
                    var ntIndex = input.getShort();
                    tracker.putMethodNT(ntIndex);
                }

                case CTags.NAME_AND_TYPE -> {
                    var nameIndex = input.getShort();
                    var descIndex = input.getShort();
                    tracker.putNtName(index, nameIndex);
                    tracker.putNameToDesc(nameIndex, descIndex);
                }

                case CTags.STRING -> input.getShort();

                case CTags.INTEGER, CTags.FLOAT -> input.getInt();

                case CTags.LONG, CTags.DOUBLE -> {
                    input.getLong();
                    index++;
                }

                case CTags.METHOD_HANDLE -> {
                    var kind = input.get();
                    var refIndex = input.getShort();
                    switch (kind) {
                        case 1, 2, 3, 4 -> tracker.putFieldNT(refIndex);
                        case 5, 6, 7, 8, 9 -> tracker.putMethodNT(refIndex);
                    }
                }

                case CTags.METHOD_TYPE -> tracker.putDescriptor(input.getShort());

                case CTags.MODULE -> tracker.putModule(input.getShort());

                case CTags.PACKAGE -> tracker.putPackage(input.getShort());

                default -> throw new IllegalStateException("Unknown class file constant pool tag " + tag);
            }
            index++;
        }


        // Access flags
        classBodyOS.writeShort(input.getShort());

        // this
        thisClass = utf8Copy[tracker.getClassContent(input.getShort())];
        var newThisClass = mappingsSet.remapClass(thisClass);
        var cpb = new ConstantPoolBuilder(newThisClass, constantPoolOS);
        classBodyOS.writeShort(cpb.insertClass(newThisClass));

        superClass = utf8Copy[tracker.getClassContent(input.getShort())];
        var newSuperClass = mappingsSet.remapClass(superClass);
        classBodyOS.writeShort(cpb.insertClass(newSuperClass));

        var interfacesCount = input.getShort();
        classBodyOS.writeShort(interfacesCount);
        while (interfacesCount-- > 0) {
            UTF8Container interfaceName = utf8Copy[tracker.getClassContent(input.getShort())];
            var newInterfaceName = mappingsSet.remapClass(interfaceName);
            classBodyOS.writeShort(cpb.insertClass(newInterfaceName));
        }

        // Treat these basically as NT structures
        var fieldsCount = input.getShort();
        classBodyOS.writeShort(fieldsCount);
        while (fieldsCount-- > 0) {
            // access flags
            classBodyOS.writeShort(input.getShort());

            var nameIndex = input.getShort();
            var descIndex = input.getShort();

            var originalName = utf8Copy[nameIndex];
            var originalDesc = utf8Copy[descIndex];

            var newName = mappingsSet.remapField(thisClass, originalName, originalDesc);
            var newDesc = mappingsSet.remapDescriptor(originalDesc);

            classBodyOS.writeShort(cpb.insertUTF8(newName));
            classBodyOS.writeShort(cpb.insertUTF8(newDesc));

            var attrCount = input.getShort();
            classBodyOS.writeShort(attrCount);
            handleFieldAttribute(attrCount, input, tracker, utf8Copy, mappingsSet, cpb, classBodyOS);
        }

        var methodsCount = input.getShort();
        classBodyOS.writeShort(methodsCount);
        while (methodsCount-- > 0) {
            // access flags
            classBodyOS.writeShort(input.getShort());

            var nameIndex = input.getShort();
            var descIndex = input.getShort();

            var originalName = utf8Copy[nameIndex];
            var originalDesc = utf8Copy[descIndex];

            var newName = mappingsSet.remapMethod(thisClass, originalName, originalDesc);
            var newDesc = mappingsSet.remapDescriptor(originalDesc);

            classBodyOS.writeShort(cpb.insertUTF8(newName));
            classBodyOS.writeShort(cpb.insertUTF8(newDesc));

            var attrCount = input.getShort();
            classBodyOS.writeShort(attrCount);
            handleMethodAttribute(attrCount, input, tracker, utf8Copy, mappingsSet, cpb, classBodyOS);
        }

        var attributesCount = input.getShort();
        classBodyOS.writeShort(attributesCount);
        handleClassAttribute(attributesCount, input, tracker, utf8Copy, mappingsSet, cpb, classBodyOS);

        var outputStream = new DataOutputStream(outputReq.createOutputStream(mappingsSet.remapClass(thisClass)));
        outputStream.writeLong(magic);
        outputStream.writeShort(cpb.nextIndex);
        backingConstantPoolOS.writeTo(outputStream);
        backingClassBodyOS.writeTo(outputStream);
    }

    private static void handleFieldAttribute(
            short attrCount,
            ByteBuffer input,
            ConstantPoolTracker tracker,
            UTF8Container[] utf8Copy,
            MappingsSet mappingsSet,
            ConstantPoolBuilder cpb,
            DataOutputStream classBodyOS
    ) throws IOException {
        while (attrCount-- > 0) {
            var nameIndex = input.getShort();
            var name = utf8Copy[nameIndex];
            cpb.insertUTF8(name);

            var length = input.getInt();
            if (name.equals(CommonNames.SIGNATURE)) {
                classBodyOS.writeInt(2);
                var signature = utf8Copy[input.getShort()];
                var newSignature = mappingsSet.remapFieldSignature(signature);
                var newSignatureIndex = cpb.insertUTF8(newSignature);
                classBodyOS.writeShort(newSignatureIndex);
            } else {
                classBodyOS.writeInt(length);
                DataUtil.copyBytes(length, input, classBodyOS);
            }
        }
    }

    private static void handleMethodAttribute(
            short attrCount,
            ByteBuffer input,
            ConstantPoolTracker tracker,
            UTF8Container[] utf8Copy,
            MappingsSet mappingsSet,
            ConstantPoolBuilder cpb,
            DataOutputStream classBodyOS
    ) throws IOException {
        while (attrCount-- > 0) {
            var nameIndex = input.getShort();
            var name = utf8Copy[nameIndex];
            cpb.insertUTF8(name);

            var length = input.getInt();
            if (name.equals(CommonNames.SIGNATURE)) {
                classBodyOS.writeInt(2);
                var signature = utf8Copy[input.getShort()];
                var newSignature = mappingsSet.remapFieldSignature(signature);
                var newSignatureIndex = cpb.insertUTF8(newSignature);
                classBodyOS.writeShort(newSignatureIndex);
            } else if (name.equals(CommonNames.CODE)) {
                classBodyOS.writeInt(length);
                handleCode(input, tracker, utf8Copy, mappingsSet, cpb, classBodyOS);
            } else {
                classBodyOS.writeInt(length);
                DataUtil.copyBytes(length, input, classBodyOS);
            }
        }
    }

    private static void handleCode(
            ByteBuffer input,
            ConstantPoolTracker tracker,
            UTF8Container[] utf8Copy,
            MappingsSet mappingsSet,
            ConstantPoolBuilder cpb,
            DataOutputStream classBodyOS
    ) throws IOException {
        classBodyOS.writeInt(input.getInt());

        var codeLength = input.getInt();
        classBodyOS.writeInt(codeLength);

        var exceptionTableLength = input.getShort();
        classBodyOS.writeShort(exceptionTableLength);
        while (exceptionTableLength-- > 0) {
            classBodyOS.writeInt(input.getInt());
            classBodyOS.writeShort(input.getShort());
            var catchType = input.getShort();
            if (catchType != 0) {
                classBodyOS.writeShort(cpb.insertClass(mappingsSet.remapClass(utf8Copy[tracker.getClassContent(catchType)])));
            } else {
                classBodyOS.writeShort(0);
            }
        }

        var attributeCount = input.getShort();
        handleCodeAttributes(attributeCount, input, utf8Copy, cpb, classBodyOS);
    }

    private static void handleCodeAttributes(short attributeCount, ByteBuffer input, UTF8Container[] utf8Copy, ConstantPoolBuilder cpb, DataOutputStream classBodyOS) throws IOException {
        while (attributeCount-- > 0) {
            var nameIndex = input.getShort();
            var name = utf8Copy[nameIndex];
            cpb.insertUTF8(name);
            var length = input.getInt();
            if (name.equals(CommonNames.STACK_MAP_TABLE)) {

            } else {
                classBodyOS.writeInt(length);
                DataUtil.copyBytes(length, input, classBodyOS);
            }
        }
    }

    private static void handleClassAttribute(
            short attributesCount,
            ByteBuffer input,
            ConstantPoolTracker tracker,
            UTF8Container[] utf8Copy,
            MappingsSet mappingsSet,
            ConstantPoolBuilder cpb,
            DataOutputStream classBodyOS
    ) throws IOException {
        while (attributesCount-- > 0) {
            // name index
            var nameIndex = input.getShort();
            var name = utf8Copy[nameIndex];
            cpb.insertUTF8(name);

            var length = input.getInt();
            if (name.equals(CommonNames.SIGNATURE)) {
                classBodyOS.writeInt(2);
                var signature = utf8Copy[input.getShort()];
                var newSignature = mappingsSet.remapFieldSignature(signature);
                var newSignatureIndex = cpb.insertUTF8(newSignature);
                classBodyOS.writeShort(newSignatureIndex);
            } else if (name.equals(CommonNames.SOURCE_FILE)) {
                input.getShort(); // original
                var sourceClass = cpb.newThis.toString();
                var start = sourceClass.lastIndexOf('/');
                if (start == -1) start = 0;
                var end = sourceClass.indexOf('$');
                if (end == -1) end = sourceClass.length();
                var newSource = new UTF8Container(sourceClass.substring(start + 1, end) + ".java");
                classBodyOS.writeInt(2);
                classBodyOS.writeShort(cpb.insertUTF8(newSource));
            } else if (name.equals(CommonNames.INNER_CLASSES)) {
                var innerCount = input.getShort();
                classBodyOS.writeInt(length);
                while (innerCount-- > 0) {
                    // inner_class_info_index
                    classBodyOS.writeShort(cpb.insertClass(utf8Copy[tracker.getClassContent(input.getShort())]));
                    var outerInfo = input.getShort();
                    if (outerInfo == 0) {
                        classBodyOS.writeShort(0);
                    } else {
                        classBodyOS.writeShort(
                                cpb.insertClass(utf8Copy[tracker.getClassContent(outerInfo)]));
                    }

                    var innerName = input.getShort();
                    if (innerName == 0) {
                        classBodyOS.writeShort(0);
                    } else {
                        classBodyOS.writeShort(cpb.insertUTF8(utf8Copy[tracker.getClassContent(outerInfo)]));
                    }

                    classBodyOS.writeShort(input.getShort());
                }
            } else if (name.equals(CommonNames.ENCLOSING_METHOD)) {
                classBodyOS.writeInt(4);
                UTF8Container ownerClass = utf8Copy[tracker.getClassContent(input.getShort())];
                classBodyOS.writeShort(cpb.insertClass(mappingsSet.remapClass(ownerClass)));
                var nti = input.getShort();
                if (nti == 0) {
                    classBodyOS.writeShort(0);
                } else {
                    var ntNameIndex = tracker.getNameNT(nti);
                    var ntName = utf8Copy[ntNameIndex];
                    var ntDesc = utf8Copy[tracker.getDescIndex(ntNameIndex)];
                    var newName = mappingsSet.remapMethod(ownerClass, ntName, ntDesc);
                    var newDesc = mappingsSet.remapDescriptor(ntDesc);
                    classBodyOS.writeShort(cpb.insertNT(newName, newDesc));
                }
            } else if (name.equals(CommonNames.PERMITTED_SUBCLASSES) || name.equals(CommonNames.NEST_MEMBERS)) {
                classBodyOS.writeInt(length);
                var entryCount = input.getShort();
                classBodyOS.writeShort(entryCount);
                while (entryCount-- > 0) {
                    classBodyOS.writeShort(cpb.insertClass(mappingsSet.remapClass(utf8Copy[input.getShort()])));
                }
            } else if (name.equals(CommonNames.NEST_HOST)) {
                classBodyOS.writeInt(2);
                classBodyOS.writeShort(cpb.insertClass(mappingsSet.remapClass(utf8Copy[tracker.getClassContent(input.getShort())])));
            } else if (name.equals(CommonNames.BOOTSTRAP_METHODS)) {
                classBodyOS.writeInt(length);
                var methodCount = input.getShort();
                classBodyOS.writeShort(methodCount);
                while (methodCount-- > 0) {
                    var ref = input.getShort();
                    classBodyOS.writeShort(cpb.copyMethodHandle(input, ref, utf8Copy, mappingsSet, tracker));
                }
                var argumentCount = input.getShort();
                classBodyOS.writeShort(argumentCount);
                while (argumentCount-- > 0) {

                }
            } else {
                classBodyOS.writeInt(length);
                DataUtil.copyBytes(length, input, classBodyOS);
            }
        }
    }

    private static class ConstantPoolBuilder {
        private final UTF8Container newThis;
        private final DataOutputStream os;
        private short nextIndex = 1;

        private final HashMap<UTF8Container, Short> utf8Tracker = new HashMap<>();
        private final HashMap<Short, Short> classTracker = new HashMap<>();
        private final HashMap<UTF8Container, Short> ntTracker = new HashMap<>();

        private ConstantPoolBuilder(UTF8Container newThis, DataOutputStream os) {
            this.newThis = newThis;
            this.os = os;
        }

        public short insertUTF8(UTF8Container newThisClass) {
            return utf8Tracker.computeIfAbsent(newThisClass, container -> {
                try {
                    os.writeByte(1);
                    DataUtil.writeContainer(container, os);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return nextIndex++;
            });
        }

        public short insertClass(UTF8Container className) {
            var i = insertUTF8(className);
            return classTracker.computeIfAbsent(i, container -> {
                try {
                    os.writeByte(7);
                    os.writeShort(i);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return nextIndex++;
            });
        }

        public short insertNT(UTF8Container name, UTF8Container type) {
            var a = insertUTF8(name);
            var b = insertUTF8(type);
            return ntTracker.computeIfAbsent(name.append(type), container -> {
                try {
                    os.writeByte(12);
                    os.writeShort(a);
                    os.writeShort(b);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return nextIndex++;
            });
        }

        public short copyMethodHandle(ByteBuffer input, short index, UTF8Container[] utf8Copy, MappingsSet mappingsSet, ConstantPoolTracker tracker) throws IOException {
            var currentOffset = tracker.getOffset(index);
            var type = input.get(currentOffset);
            os.writeByte(15);
            os.writeByte(type);

            var refIndex = input.getShort(currentOffset + 1);
            var refOffset = tracker.getOffset(refIndex);
            var tag = input.get(refOffset - 1);
            var owner = utf8Copy[input.getShort(refOffset)];
            var nt = input.getShort(refOffset + 2);
            var name = utf8Copy[tracker.getNameNT(nt)];
            var desc = utf8Copy[tracker.getDescIndex(nt)];
            short newNt;
            if (tag == 9) {
                newNt = insertNT(mappingsSet.remapField(owner, name, desc), mappingsSet.remapDescriptor(desc));
            } else {
                newNt = insertNT(mappingsSet.remapMethod(owner, name, desc), mappingsSet.remapDescriptor(desc));
            }
            os.writeShort(newNt);
            return nextIndex++;
        }

        public short copyLoadable(ByteBuffer input, short index, UTF8Container[] utf8Copy, MappingsSet mappingsSet, ConstantPoolTracker tracker) {
            var offset = tracker.getOffset(index);
            var tag = input.get(offset - 1);
            switch (tag) {
                case 7 -> {
                    return insertClass(utf8Copy[tracker.getClassContent(input.getShort(offset))]);
                }

                case 9, 10, 11 -> {
                    
                }
            }
            return nextIndex++;
        }
    }
}
