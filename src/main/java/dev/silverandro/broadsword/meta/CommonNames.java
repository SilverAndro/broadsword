/*
 * Copyright 2024 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.meta;

import dev.silverandro.broadsword.tools.UTF8Container;

public final class CommonNames {
    private CommonNames() {}

    public static final UTF8Container SIGNATURE = new UTF8Container("Signature");
    public static final UTF8Container SOURCE_FILE = new UTF8Container("SourceFile");
    public static final UTF8Container INNER_CLASSES = new UTF8Container("InnerClasses");
    public static final UTF8Container ENCLOSING_METHOD = new UTF8Container("EnclosingMethod");
    public static final UTF8Container PERMITTED_SUBCLASSES = new UTF8Container("PermittedSubclasses");
    public static final UTF8Container NEST_HOST = new UTF8Container("NestHost");
    public static final UTF8Container NEST_MEMBERS = new UTF8Container("NestMembers");
}
