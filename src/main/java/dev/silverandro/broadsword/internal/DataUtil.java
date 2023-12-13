/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.internal;

import dev.silverandro.broadsword.UTF8Container;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Various utility methods for working with ByteBuffers
 */
public final class DataUtil {
    public static int indexOf(byte[] data, byte c, int start) {
        int i = start;
        while (i < data.length) {
            if (data[i] == c) return i;
            i++;
        }
        return -1;
    }

    /**
     * Reads a string of length {@code length} from the byte buffer.
     *
     * @implNote Works by using the {@code String(byte[], int, int)}
     * constructor to copy bytes directly from the buffer with {@code arraycopy} and then just telling the ByteBuffer to
     * skip {@code length} forward, so reasonably cheap.
     */
    public static UTF8Container readBytes(int length, ByteBuffer in) {
        var out = new UTF8Container(Arrays.copyOfRange(in.array(), in.position(), in.position() + length));
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

    /**
     * Skips {@code count} bytes forward without reading.
     */
    public static void skipBytes(int count, ByteBuffer buffer) {
        buffer.position(buffer.position() + count);
    }
}
