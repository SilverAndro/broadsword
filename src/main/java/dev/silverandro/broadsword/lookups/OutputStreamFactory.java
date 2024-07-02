/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.lookups;

import dev.silverandro.broadsword.tools.ClassFileRemapper;
import dev.silverandro.broadsword.tools.UTF8Container;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A function provided to {@link ClassFileRemapper} that allows it to request an output stream for writing remapped
 * class data to. Streams are automatically closed
 */
@FunctionalInterface
public interface OutputStreamFactory {
    /**
     * Generates an output stream to write the remapped class data to
     * @param className The new name of the class this data belongs to.
     */
    OutputStream createOutputStream(UTF8Container className) throws IOException;
}
