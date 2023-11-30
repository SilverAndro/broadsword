/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword;

import dev.silverandro.broadsword.internal.ByteBufferUtil;
import dev.silverandro.broadsword.internal.CTags;
import dev.silverandro.broadsword.internal.ConstantPoolTracker;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassStructExtractor {
    /**
     * Extracts a mappings struct from a class file.
     * @param classFile The bytes of a single class file, not modified.
     * @return A new {@link ClassMappingStruct} containing the data from the class file
     */
    public static ClassMappingStruct extract(byte[] classFile) {
        List<String> superAndInterfaceClasses = new ArrayList<>();
        Map<String, String> methodsAndDesc = new HashMap<>();

        var input = ByteBuffer.wrap(classFile);
        input.getLong();

        var count = input.getShort();
        var index = 1;

        // Copy all UFT-8 data into memory to assist in remapping
        var utf8Copy = new String[count];
        // Keep track of info
        var tracker = new ConstantPoolTracker(count);

        while (index < count) {
            var tag = input.get();
            switch (tag) {
                case CTags.UTF8 -> {
                    var length = input.getShort();
                    var content = ByteBufferUtil.readBytes(length, input);
                    utf8Copy[index] = content;
                }

                case CTags.CLASS -> {
                    var contentIndex = input.getShort();
                    tracker.putClass(index, contentIndex);
                }

                case CTags.FIELD, CTags.INTEGER, CTags.FLOAT, CTags.METHOD, CTags.INTERFACE_METHOD, CTags.NAME_AND_TYPE, CTags.DYNAMIC, CTags.INVOKE_DYNAMIC -> input.getInt();

                case CTags.STRING, CTags.METHOD_TYPE, CTags.MODULE, CTags.PACKAGE -> input.getShort();

                case CTags.LONG, CTags.DOUBLE -> {
                    input.getLong();
                    index++;
                }

                case CTags.METHOD_HANDLE -> {
                    input.get();
                    input.getShort();
                }

                default -> throw new IllegalStateException("Unknown class file constant pool tag " + tag);
            }
            index++;
        }

        input.getInt();
        var superIndex = input.getShort();
        if (superIndex == 0) {
            superAndInterfaceClasses.add("java/lang/Object");
        } else {
            superAndInterfaceClasses.add(utf8Copy[tracker.getClassContent(superIndex)]);
        }
        var interfacesCount = input.getShort();
        while (interfacesCount-- > 0) {
            superAndInterfaceClasses.add(utf8Copy[tracker.getClassContent(input.getShort())]);
        }

        // Treat these basically as NT structures
        var fieldsCount = input.getShort();
        while (fieldsCount-- > 0) {
            ByteBufferUtil.skipBytes(6, input);
            var attrCount = input.getShort();
            eatAttributes(attrCount, input);
        }

        var methodsCount = input.getShort();
        while (methodsCount-- > 0) {
            input.getShort();
            var nameIndex = input.getShort();
            var descIndex = input.getShort();
            methodsAndDesc.put(utf8Copy[nameIndex], utf8Copy[descIndex]);

            var attrCount = input.getShort();
            eatAttributes(attrCount, input);
        }

        return new ClassMappingStruct(superAndInterfaceClasses, methodsAndDesc);
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
