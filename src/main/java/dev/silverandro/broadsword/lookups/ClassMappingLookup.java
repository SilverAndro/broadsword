/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.lookups;

import dev.silverandro.broadsword.data.ClassMappingStruct;
import dev.silverandro.broadsword.mappings.MappingsSet;
import dev.silverandro.broadsword.tools.ClassFileRemapper;
import dev.silverandro.broadsword.tools.UTF8Container;

/**
 * A function provided to {@link ClassFileRemapper} that allows it to request a {@link ClassMappingStruct} when required.
 * See {@link ClassFileRemapper#remapClassBytes(byte[], MappingsSet, ClassMappingLookup, OutputStreamFactory)} for details on the performance
 * implications of these requests.
 */
@FunctionalInterface
public interface ClassMappingLookup {
    /**
     * Handles the request for class info from the remapper.
     * @param className The name of the class that information is being requested on. This is in the origin namespace.
     */
    ClassMappingStruct lookupClassInfo(UTF8Container className);
}
