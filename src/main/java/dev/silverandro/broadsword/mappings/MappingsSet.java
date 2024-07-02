/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.mappings;

import dev.silverandro.broadsword.meta.DataUtil;
import dev.silverandro.broadsword.tools.UTF8Container;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The base class of a mappings set. Provides the ability to store and remap classes, fields, methods, modules, and packages.
 */
public class MappingsSet {
    public final Map<UTF8Container, UTF8Container> classMapping = new HashMap<>(1024, 0.7f);
    public final Map<OwnedType, UTF8Container> fieldMapping = new HashMap<>(2048, 0.7f);
    public final Map<OwnedType, UTF8Container> methodMapping = new HashMap<>(2048, 0.7f);
    public final Map<UTF8Container, UTF8Container> moduleMapping = new HashMap<>();
    public final Map<UTF8Container, UTF8Container> packageMapping = new HashMap<>();

    protected boolean supportsFieldTypes() {
        return true;
    }

    private UTF8Container fieldType(UTF8Container type) {
        if (supportsFieldTypes()) return type;
        return null;
    }

    public final UTF8Container remapClass(UTF8Container current) {
        return classMapping.getOrDefault(current, current);
    }

    public final UTF8Container remapClassOrNull(UTF8Container current) {
        return classMapping.get(current);
    }

    public final UTF8Container remapField(UTF8Container parentClass, UTF8Container name, UTF8Container desc) {
        return fieldMapping.getOrDefault(new OwnedType(parentClass, name, fieldType(desc)), name);
    }

    public final UTF8Container remapFieldOrNull(UTF8Container parentClass, UTF8Container name, UTF8Container desc) {
        return fieldMapping.get(new OwnedType(parentClass, name, fieldType(desc)));
    }

    public final UTF8Container remapMethod(UTF8Container parentClass, UTF8Container name, UTF8Container desc) {
        return methodMapping.getOrDefault(new OwnedType(parentClass, name, desc), name);
    }

    public final UTF8Container remapMethodOrNull(UTF8Container parentClass, UTF8Container name, UTF8Container desc) {
        return methodMapping.get(new OwnedType(parentClass, name, desc));
    }

    public final UTF8Container remapDescriptor(UTF8Container current) throws IOException {
        var output = new ByteArrayOutputStream();
        var currentData = current.getData();

        int i = -1;
        while (i++ < currentData.length - 1) {
            var c = currentData[i];
            output.write(c);
            if (c == 'L') {
                var end = DataUtil.indexOf(currentData, (byte)';', i + 2);
                var remapped = remapClass(new UTF8Container(Arrays.copyOfRange(currentData, i + 1, end))).getData();
                output.write(remapped);
                output.write(';');
                i = end;
            }
        }

        return new UTF8Container(output.toByteArray());
    }

    public UTF8Container remapModule(UTF8Container current) {
        return moduleMapping.getOrDefault(current, current);
    }

    public UTF8Container remapModuleOrNull(UTF8Container current) {
        return moduleMapping.get(current);
    }

    public UTF8Container remapPackage(UTF8Container current) {
        return packageMapping.getOrDefault(current, current);
    }

    public UTF8Container remapPackageOrNull(UTF8Container current) {
        return packageMapping.get(current);
    }

    public UTF8Container remapFieldSignature(UTF8Container current) throws IOException {
        var output = new ByteArrayOutputStream();
        var currentData = current.getData();

        int i = -1;
        while (i++ < currentData.length - 1) {
            var c = currentData[i];
            output.write(c);
            if (c == 'L') {
                var end = DataUtil.indexOf(currentData, (byte)';', i + 2);
                var remapped = remapClass(new UTF8Container(Arrays.copyOfRange(currentData, i + 1, end))).getData();
                output.write(remapped);
                output.write(';');
                i = end;
            }
        }

        return new UTF8Container(output.toByteArray());
    }

    /**
     * Common type used to represent fields and methods for remapping. Whether this is a field or method is entirely contextual.
     * @param owner The owner class
     * @param name The name of the type
     * @param desc The descriptor of the type
     */
    public record OwnedType(UTF8Container owner, UTF8Container name, UTF8Container desc) {
        public static OwnedType of(String owner, String origName, String desc) {
            if (desc == null) {
                return new OwnedType(new UTF8Container(owner), new UTF8Container(origName), null);
            } else {
                return new OwnedType(new UTF8Container(owner), new UTF8Container(origName), new UTF8Container(desc));
            }
        }
    }
}
