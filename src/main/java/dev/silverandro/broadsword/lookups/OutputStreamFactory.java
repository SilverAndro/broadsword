package dev.silverandro.broadsword.lookups;

import dev.silverandro.broadsword.ClassFileRemapper;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A function provided to {@link ClassFileRemapper} that allows it to request a output stream for writing remapped
 * class data to. Streams are automatically closed
 */
@FunctionalInterface
public interface OutputStreamFactory {
    /**
     * Generates an output stream to write the remapped class data to
     * @param className The new name of the class this data belongs to.
     */
    OutputStream createOutputStream(String className) throws IOException;
}
