/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.internal;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Various utility methods for working with ByteBuffers
 */
public final class ByteBufferUtil {
    private static final Charset charset = StandardCharsets.UTF_8;

    /**
     * Reads a string of length {@code length} from the byte buffer.
     *
     * @implNote Works by using the {@code String(byte[], int, int)}
     * constructor to copy bytes directly from the buffer with {@code arraycopy} and then just telling the ByteBuffer to
     * skip {@code length} forward, so very cheap.
     */
    public static String readBytes(int length, ByteBuffer in) {
        var out = new String(in.array(), in.position(), length, charset);
        in.position(in.position() + length);
        return out;
    }

    /**
     * Copies bytes from a ByteBuffer to a ByteArrayOutputStream.
     */
    public static void copyBytes(int count, ByteBuffer in, ByteArrayOutputStream out) {
        out.write(in.array(), in.position(), count);
        in.position(in.position() + count);
    }

    public static void skipBytes(int count, ByteBuffer buffer) {
        buffer.position(buffer.position() + count);
    }
}
