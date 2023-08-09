/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword;

import dev.silverandro.broadsword.internal.ByteBufferUtil;
import dev.silverandro.broadsword.internal.CTags;
import dev.silverandro.broadsword.internal.ConstantPoolTracker;
import dev.silverandro.broadsword.internal.RemapType;
import dev.silverandro.broadsword.mappings.MappingsSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;

public class ClassFileRemapper {
    private static final HashMap<String, MappingsSet> mappings = new HashMap<>();

    public static void registerMappings(String from, String to, MappingsSet mappings) {
        ClassFileRemapper.mappings.put(from + "->" + to, mappings);
    }

    /**
     * Remaps the provided class file from the origin namespace to the target namespace. When required, {@code classInfoReq}
     * is invoked to request information about classes that are needed for remapping.
     * <p>
     * <strong>
     *     Please note that classInfoReq does not cache or de-duplicate requests, the invoker is in charge of the
     *     performance of invocations.
     * </strong>
     *
     * @param classFile The {@code byte[]} that makes up the class file to remap
     * @param originNamespace The origin namespace of the provided class file
     * @param targetNamespace The namespace to remap the class into
     * @param classInfoReq The lookup used to request information on classes necessary for remapping
     */
    public static byte[] remapClassBytes(byte[] classFile, String originNamespace, String targetNamespace, ClassMappingLookup classInfoReq) throws IOException {
        var mappingsKey = originNamespace + "->" + targetNamespace;
        var mappingsSet = mappings.get(mappingsKey);

        if (mappingsSet == null) {
            throw new IllegalStateException("Cannot convert from mappings " + originNamespace + " to " + targetNamespace);
        }

        var input = ByteBuffer.wrap(classFile);

        //  u4  magic;
        //  u2  minor_version;
        //  u2  major_version;
        var magic = input.getLong();

        //  u2  constant_pool_count;
        var count = input.getShort();
        var index = 1;

        // Save our current input position
        input.mark();

        // To be populated later, lets us remap field and method attr
        String thisClass;
        // Copy all UFT-8 data into memory to assist in remapping
        var utf8Copy = new Int2ObjectAVLTreeMap<String>();
        // Keep track of info
        var tracker = new ConstantPoolTracker();

        // Read through all the constant pool entries
        while (index < count) {
            var tag = input.get();
            switch (tag) {
                case CTags.UTF8 -> {
                    var length = input.getShort();
                    var content = ByteBufferUtil.readBytes(length, input);
                    utf8Copy.put(index, content);
                }

                case CTags.CLASS -> {
                    var contentIndex = input.getShort();
                    tracker.putClass(index, contentIndex);
                }

                case CTags.FIELD, CTags.METHOD, CTags.INTERFACE_METHOD, CTags.DYNAMIC, CTags.INVOKE_DYNAMIC -> {
                    var classIndex = input.getShort();
                    var ntIndex = input.getShort();
                    tracker.putNtClass(ntIndex, classIndex);
                }

                case CTags.NAME_AND_TYPE -> {
                    var nameIndex = input.getShort();
                    var descIndex = input.getShort();
                    tracker.putNtName(index, nameIndex);
                    tracker.putDescriptor(descIndex);
                }

                case CTags.STRING -> input.getShort();

                case CTags.INTEGER, CTags.FLOAT -> input.getInt();

                case CTags.LONG, CTags.DOUBLE -> {
                    input.getLong();
                    index++;
                }

                case CTags.METHOD_HANDLE -> {
                    input.get();
                    input.getShort();
                }

                case CTags.METHOD_TYPE -> tracker.putDescriptor(input.getShort());

                case CTags.MODULE -> tracker.putModule(input.getShort());

                case CTags.PACKAGE -> tracker.putPackage(input.getShort());

                default -> throw new IllegalStateException("Unknown class file constant pool tag " + tag);
            }
            index++;
        }

        // Access flags, this, super, and interfaces
        input.getShort();
        thisClass = utf8Copy.get(tracker.getClassContent(input.getShort()));
        var superClass = utf8Copy.get(tracker.getClassContent(input.getShort()));
        var interfacesCount = input.getShort();
        var interfaces = new String[interfacesCount];
        while (interfacesCount-- > 0) {
            interfaces[interfacesCount] = utf8Copy.get(tracker.getClassContent(input.getShort()));
        }

        // Generate fake NT structures
        var fieldsCount = input.getShort();
        while (fieldsCount-- > 0) {
            input.getShort();
            var nameIndex = input.getShort();
            var descIndex = input.getShort();
            tracker.putSelfFieldName(nameIndex, descIndex);
            tracker.putDescriptor(descIndex);

            var attrCount = input.getShort();
            eatAttributes(attrCount, input);
        }

        var methodsCount = input.getShort();
        while (methodsCount-- > 0) {
            input.getShort();
            var nameIndex = input.getShort();
            var descIndex = input.getShort();
            tracker.putSelfMethodName(nameIndex, descIndex);
            tracker.putDescriptor(descIndex);

            var attrCount = input.getShort();
            eatAttributes(attrCount, input);
        }

        // And we're done parsing! fly back to the start and rewrite it the way out
        input.reset();

        // Write to a stream
        var constantPool = new ByteArrayOutputStream();

        // Run through the constant pool entries again, rewriting as we go
        index = 1;
        while (index < count) {
            var tag = input.get();
            constantPool.write(tag);
            switch (tag) {
                case CTags.UTF8 -> {
                    var length = input.getShort();
                    var original = ByteBufferUtil.readBytes(length, input);
                    String newOutput = original;

                    switch (tracker.getRemapType(index)) {
                        case RemapType.CLASS -> newOutput = mappingsSet.remapClass(original);

                        case RemapType.DESCRIPTOR -> newOutput = mappingsSet.remapDescriptor(original);

                        case RemapType.MODULE -> newOutput = mappingsSet.remapModule(original);

                        case RemapType.PACKAGE -> newOutput = mappingsSet.remapPackage(original);

                        case RemapType.FIELD_NAME -> {
                            var desc = utf8Copy.get(tracker.getDescIndex(index));
                            var owner = utf8Copy.get(tracker.getNameOwner(index));
                            newOutput = mappingsSet.remapField(owner, original, desc);
                        }

                        case RemapType.METHOD_NAME -> {
                            var desc = utf8Copy.get(tracker.getDescIndex(index));
                            var owner = utf8Copy.get(tracker.getNameOwner(index));
                            newOutput = mappingsSet.remapMethod(owner, original, desc);
                        }

                        case RemapType.SELF_FIELD_NAME ->
                            newOutput = mappingsSet.remapField(thisClass, original, utf8Copy.get(tracker.getDescIndex(index)));

                        case RemapType.SELF_METHOD_NAME -> {
                            var desc = utf8Copy.get(tracker.getDescIndex(index));
                            newOutput = mappingsSet.remapMethodOrNull(thisClass, original, desc);
                            if (newOutput == null) {
                                var superStruct = classInfoReq.lookupClassInfo(mappingsSet.remapClass(superClass));
                                var methodDesc = superStruct.getMethodsAndDesc().get(original);
                                if (desc.equals(methodDesc)) {
                                    newOutput = mappingsSet.remapMethod(superClass, original, desc);
                                } else {
                                    // Advanced search!
                                    // This could be arbitrarily deep in the inheritance tree, so build a queue of structs to try
                                    // If a struct fails, enqueue its parents for later, this makes search breadth first
                                    newOutput = original;
                                    var searchQueue = new ArrayDeque<>(superStruct.getSuperAndInterfaceClasses());
                                    searchQueue.addAll(List.of(interfaces));
                                    while (!searchQueue.isEmpty()) {
                                        var name = searchQueue.removeFirst();
                                        var classStruct = classInfoReq.lookupClassInfo(mappingsSet.remapClass(name));
                                        methodDesc = classStruct.getMethodsAndDesc().get(original);
                                        if (desc.equals(methodDesc)) {
                                            newOutput = mappingsSet.remapMethod(name, original, desc);
                                            break;
                                        } else {
                                            searchQueue.addAll(classStruct.getSuperAndInterfaceClasses());
                                        }
                                    }
                                }
                            }
                        }

                        default -> {}
                    }

                    constantPool.write(newOutput.length() >> 8);
                    constantPool.write(newOutput.length() & 0xFF);
                    constantPool.write(newOutput.getBytes(StandardCharsets.UTF_8));
                }

                case CTags.CLASS, CTags.PACKAGE, CTags.MODULE, CTags.METHOD_TYPE, CTags.STRING ->
                        ByteBufferUtil.copyBytes(2, input, constantPool);

                case CTags.FIELD, CTags.METHOD, CTags.INTERFACE_METHOD,
                        CTags.NAME_AND_TYPE, CTags.DYNAMIC, CTags.INVOKE_DYNAMIC,
                        CTags.INTEGER, CTags.FLOAT -> ByteBufferUtil.copyBytes(4, input, constantPool);

                case CTags.LONG, CTags.DOUBLE -> {
                    ByteBufferUtil.copyBytes(8, input, constantPool);
                    index++;
                }

                case CTags.METHOD_HANDLE -> ByteBufferUtil.copyBytes(3, input, constantPool);

                default -> throw new IllegalStateException("Got out of sync? Unexpected constant pool tag " + tag + " in rewrite phase.");
            }
            index++;
        }

        var bytes = ByteBuffer.allocate(10 + constantPool.size() + input.remaining())
                .putLong(magic)
                .putShort(count)
                .put(constantPool.toByteArray())
                .put(input);
        return bytes.array();
    }

    private static void eatAttributes(int count, ByteBuffer input) {
        while (count-- > 0) {
            // name index
            input.getShort();
            var length = input.getInt();
            ByteBufferUtil.skipBytes(length, input);
        }
    }
}
