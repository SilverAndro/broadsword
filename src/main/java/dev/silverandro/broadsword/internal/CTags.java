/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.internal;

/**
 * The tags of constant pool entries so we dont have to use raw values.
 * <a href="https://docs.oracle.com/javase/specs/jvms/se20/html/jvms-4.html#jvms-4.4">See the class file spec</a>
 */
public final class CTags {
    private CTags() {}

    public static final byte UTF8 = 1;
    public static final byte INTEGER = 3;
    public static final byte FLOAT = 4;
    public static final byte LONG = 5;
    public static final byte DOUBLE = 6;
    public static final byte CLASS = 7;
    public static final byte STRING = 8;
    public static final byte FIELD = 9;
    public static final byte METHOD = 10;
    public static final byte INTERFACE_METHOD = 11;
    public static final byte NAME_AND_TYPE = 12;
    public static final byte METHOD_HANDLE = 15;
    public static final byte METHOD_TYPE = 16;
    public static final byte DYNAMIC = 17;
    public static final byte INVOKE_DYNAMIC = 18;
    public static final byte MODULE = 19;
    public static final byte PACKAGE = 20;
}
