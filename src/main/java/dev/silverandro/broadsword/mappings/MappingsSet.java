/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.mappings;

import java.util.HashMap;
import java.util.Map;

/**
 * The base class of a mappings set. Provides the ability to store and remap classes, fields, methods, modules, and packages.
 */
public class MappingsSet {
    public final Map<String, String> classMapping = new HashMap<>(1024, 0.7f);
    public final Map<String, String> fieldMapping = new HashMap<>(2048, 0.7f);
    public final Map<String, String> methodMapping = new HashMap<>(2048, 0.7f);
    public final Map<String, String> moduleMapping = new HashMap<>();
    public final Map<String, String> packageMapping = new HashMap<>();

    protected boolean supportsFieldTypes() {
        return true;
    }

    private String fieldType(String type) {
        if (supportsFieldTypes()) return type;
        return null;
    }

    public final String remapClass(String current) {
        return classMapping.getOrDefault(current, current);
    }

    public final String remapClassOrNull(String current) {
        return classMapping.get(current);
    }

    public final String remapField(String parentClass, String name, String desc) {
        return fieldMapping.getOrDefault(parentClass + "!" + name + "!" + fieldType(desc), name);
    }

    public final String remapFieldOrNull(String parentClass, String name, String desc) {
        return fieldMapping.get(parentClass + "!" + name + "!" + fieldType(desc));
    }

    public final String remapMethod(String parentClass, String name, String desc) {
        return methodMapping.getOrDefault(parentClass + "!" + name + "!" + desc, name);
    }

    public final String remapMethodOrNull(String parentClass, String name, String desc) {
        return methodMapping.get(parentClass + "!" + name + "!" + desc);
    }

    public final String remapDescriptor(String current) {
        var out = new StringBuilder(current.length());

        int i = -1;
        while (i++ < current.length() - 1) {
            var c = current.charAt(i);
            if (c != 'L') {
                out.append(c);
            } else {
                var endIndex = current.indexOf(';', i+2);
                var className = current.substring(i+1, endIndex);
                out.append('L').append(remapClass(className)).append(';');
                i += className.length() + 1;
            }
        }

        return out.toString();
    }

    public String remapModule(String current) {
        return moduleMapping.getOrDefault(current, current);
    }

    public String remapModuleOrNull(String current) {
        return moduleMapping.get(current);
    }

    public String remapPackage(String current) {
        return packageMapping.getOrDefault(current, current);
    }

    public String remapPackageOrNull(String current) {
        return packageMapping.get(current);
    }
}
