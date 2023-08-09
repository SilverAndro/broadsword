/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.internal;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * Various utility methods for working with ByteBuffers
 */
public final class ByteBufferUtil {
    /**
     * Reads a string of length {@code length} from the byte buffer.
     *
     * @implNote Works by using the {@code String(byte[], int, int)}
     * constructor to copy bytes directly from the buffer with {@code arraycopy} and then just telling the ByteBuffer to
     * skip {@code length} forward, so very cheap.
     */
    public static String readBytes(int length, ByteBuffer in) {
        var out = new String(in.array(), in.position(), length);
        in.position(in.position() + length);
        return out;
    }

    /**
     * Copies bytes from a ByteBuffer to a ByteArrayOutputStream. This method is not particularly fast, take care in
     * its usage.
     */
    public static void copyBytes(int count, ByteBuffer in, ByteArrayOutputStream out) {
        while (count-- > 0) {
            out.write(in.get());
        }
    }

    public static void skipBytes(int count, ByteBuffer buffer) {
        buffer.position(buffer.position() + count);
    }
}
