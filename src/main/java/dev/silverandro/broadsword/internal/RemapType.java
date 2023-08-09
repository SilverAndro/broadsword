/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.internal;

import dev.silverandro.broadsword.ClassFileRemapper;

/**
 * Internal IDs used in {@link ConstantPoolTracker} and {@link ClassFileRemapper} to make sure they're referring to the
 * same thing.
 */
public class RemapType {
    private RemapType() {}

    public static final int CLASS = 1;
    public static final int FIELD_NAME = 2;
    public static final int METHOD_NAME = 3;
    public static final int SELF_FIELD_NAME = 4;
    public static final int SELF_METHOD_NAME = 5;
    public static final int DESCRIPTOR = 6;
    public static final int MODULE = 7;
    public static final int PACKAGE = 8;
}