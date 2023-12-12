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
    public final Map<OwnedType, String> fieldMapping = new HashMap<>(2048, 0.7f);
    public final Map<OwnedType, String> methodMapping = new HashMap<>(2048, 0.7f);
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
        return fieldMapping.getOrDefault(new OwnedType(parentClass, name, fieldType(desc)), name);
    }

    public final String remapFieldOrNull(String parentClass, String name, String desc) {
        return fieldMapping.get(new OwnedType(parentClass, name, fieldType(desc)));
    }

    public final String remapMethod(String parentClass, String name, String desc) {
        return methodMapping.getOrDefault(new OwnedType(parentClass, name, desc), name);
    }

    public final String remapMethodOrNull(String parentClass, String name, String desc) {
        return methodMapping.get(new OwnedType(parentClass, name, desc));
    }

    public final String remapDescriptor(String current) {
        int start = current.indexOf('L');
        if (start != -1) {
            if (current.charAt(0) == '(') {
                var out = new StringBuilder();
                int last = 0;
                do {
                    int end = current.indexOf(';', last);
                    out.append(current, last, start);
                    last = end + 1;
                    out.append('L');
                    out.append(remapClass(current.substring(start + 1, end)));
                    out.append(';');

                    start = current.indexOf('L', last);
                } while (start != -1);

                out.append(current, last, current.length());
                return out.toString();
            } else {
                return 'L' + remapClass(current.substring(1, current.length() - 2)) + ';';
            }
        } else {
            return current;
        }
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

    /**
     * Common type used to represent fields and methods for remapping. Whether this is a field or method is entirely contextual.
     * @param owner The owner class
     * @param name The name of the type
     * @param desc The descriptor of the type
     */
    public record OwnedType(String owner, String name, String desc) {}
}
