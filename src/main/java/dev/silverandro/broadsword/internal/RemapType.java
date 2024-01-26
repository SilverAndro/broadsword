/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.internal;

import dev.silverandro.broadsword.tools.ClassFileRemapper;

/**
 * Internal IDs used in {@link ConstantPoolTracker} and {@link ClassFileRemapper} to make sure they're referring to the
 * same thing.
 */
public class RemapType {
    private RemapType() {}

    public static final int CLASS               = 1;
    public static final int NAME_NT             = 2;
    public static final int FIELD_NT            = 3;
    public static final int METHOD_NT           = 4;
    public static final int SELF_FIELD_NAME     = 5;
    public static final int SELF_METHOD_NAME    = 6;
    public static final int DESCRIPTOR          = 7;
    public static final int MODULE              = 8;
    public static final int PACKAGE             = 9;
}