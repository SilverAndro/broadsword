/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword;

import java.util.List;
import java.util.Map;

/**
 * Represents a class, used for class data lookup during remapping.
 */
public class ClassMappingStruct {
    private final List<String> superAndInterfaceClasses;
    private final Map<String, String> methodsAndDesc;

    public ClassMappingStruct(List<String> superAndInterfaceClasses, Map<String, String> methodsAndDesc) {
        this.superAndInterfaceClasses = superAndInterfaceClasses;
        this.methodsAndDesc = methodsAndDesc;
    }

    public List<String> getSuperAndInterfaceClasses() {
        return superAndInterfaceClasses;
    }

    public Map<String, String> getMethodsAndDesc() {
        return methodsAndDesc;
    }
}
