/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.data;

import dev.silverandro.broadsword.tools.ClassStructExtractor;
import dev.silverandro.broadsword.tools.UTF8Container;

import java.util.List;
import java.util.Map;

/**
 * Represents a class, used for class data lookup during remapping. Consider use of {@link ClassStructExtractor} for highly
 * efficient creation of these structs and their data.
 */
public record ClassMappingStruct(List<UTF8Container> superAndInterfaceClasses, Map<UTF8Container, UTF8Container> methodsAndDesc) {

}
