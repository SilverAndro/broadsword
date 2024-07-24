/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.tools;

import dev.silverandro.broadsword.data.ClassMappingStruct;
import dev.silverandro.broadsword.meta.CTags;
import dev.silverandro.broadsword.meta.DataUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class used to extract {@link ClassMappingStruct}s from classes for remapping.
 */
public class ClassStructExtractor {
    private ClassStructExtractor() {}

    /**
     * Extracts a mappings struct from a class file's bytes. Assumes the class file is valid.
     * @param classFile The bytes of a single class file, not modified.
     * @return A new {@link ClassMappingStruct} containing the data from the class file
     */
    public static ClassMappingStruct extract(byte[] classFile) {
        List<UTF8Container> superAndInterfaceClasses = new ArrayList<>();
        Map<UTF8Container, UTF8Container> methodsAndDesc = new HashMap<>();

        int position = 8;

        var count = DataUtil.readShort(classFile, position);
        position += 2;
        var index = 1;

        // Copy all UFT-8 data into memory to assist in remapping
        var utf8Copy = new UTF8Container[count];
        // track where each class points
        var classData = new int[count];

        while (index < count) {
            var tag = classFile[position++];
            switch (tag) {
                case CTags.UTF8 -> {
                    var length = DataUtil.readShort(classFile, position);
                    position += 2;
                    var content = DataUtil.readBytes(length, classFile, position);
                    position += length;
                    utf8Copy[index] = content;
                }

                case CTags.CLASS -> {
                    var contentIndex = DataUtil.readShort(classFile, position);
                    position += 2;
                    classData[index] = contentIndex;
                }

                case CTags.FIELD, CTags.INTEGER, CTags.FLOAT, CTags.METHOD, CTags.INTERFACE_METHOD, CTags.NAME_AND_TYPE, CTags.DYNAMIC, CTags.INVOKE_DYNAMIC -> position += 4;

                case CTags.STRING, CTags.METHOD_TYPE, CTags.MODULE, CTags.PACKAGE -> position += 2;

                case CTags.LONG, CTags.DOUBLE -> {
                    position += 8;
                    index++;
                }

                case CTags.METHOD_HANDLE -> {
                    position += 3;
                }

                default -> throw new IllegalStateException("Unknown class file constant pool tag " + tag);
            }
            index++;
        }

        var superIndex = DataUtil.readShort(classFile, position + 4);
        position += 6;
        if (superIndex == 0) {
            superAndInterfaceClasses.add(new UTF8Container("java/lang/Object"));
        } else {
            superAndInterfaceClasses.add(utf8Copy[classData[superIndex]]);
        }
        var interfacesCount = DataUtil.readShort(classFile, position);
        position += 2;
        while (interfacesCount-- > 0) {
            superAndInterfaceClasses.add(utf8Copy[classData[DataUtil.readShort(classFile, position)]]);
            position += 2;
        }

        // Treat these basically as NT structures
        var fieldsCount = DataUtil.readShort(classFile, position);
        position += 2;
        while (fieldsCount-- > 0) {
            position += 8;
            var attrCount = DataUtil.readShort(classFile, position - 2);
            position = eatAttributes(attrCount, classFile, position);
        }

        var methodsCount = DataUtil.readShort(classFile, position);
        position += 2;
        while (methodsCount-- > 0) {
            position += 2;
            var nameIndex = DataUtil.readShort(classFile, position);
            var descIndex = DataUtil.readShort(classFile, position += 2);
            position += 2;
            methodsAndDesc.put(utf8Copy[nameIndex], utf8Copy[descIndex]);

            var attrCount = DataUtil.readShort(classFile, position);
            position += 2;
            position = eatAttributes(attrCount, classFile, position);
        }

        return new ClassMappingStruct(superAndInterfaceClasses, methodsAndDesc);
    }

    private static int eatAttributes(int count, byte[] input, int position) {
        while (count-- > 0) {
            position += 2;
            position += 4 + DataUtil.readInt(input, position);
        }
        return position;
    }
}
