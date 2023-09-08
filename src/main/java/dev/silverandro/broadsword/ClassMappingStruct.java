/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword;

import java.util.List;
import java.util.Map;

/**
 * Represents a class, used for class data lookup during remapping.
 */
public record ClassMappingStruct(List<String> superAndInterfaceClasses, Map<String, String> methodsAndDesc) {

}
