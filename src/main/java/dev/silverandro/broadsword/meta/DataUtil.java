/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.meta;

import dev.silverandro.broadsword.tools.UTF8Container;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Various utility methods for working with ByteBuffers
 */
public final class DataUtil {
    public static int readShort(byte[] data, int index) {
        return (data[index] & 0xff) << 8  | (data[index + 1] & 0xff);
    }

    public static int readInt(byte[] data, int index) {
        return ((data[index] & 0xFF) << 24) |
                ((data[index + 1] & 0xFF) << 16) |
                ((data[index + 2] & 0xFF) << 8 ) |
                ((data[index + 3] & 0xFF));
    }

    public static int indexOf(byte[] data, byte c, int start) {
        int i = start;
        while (i < data.length) {
            if (data[i] == c) return i;
            i++;
        }
        return -1;
    }

    public static void writeContainer(UTF8Container in, DataOutputStream out) throws IOException {
        out.writeShort(in.length());
        out.write(in.getData(), 0, in.length());
    }

    /**
     * Reads a UTF8 string of length {@code length} from the byte buffer.
     */
    public static UTF8Container readBytes(int length, ByteBuffer in) {
        var out = new UTF8Container(Arrays.copyOfRange(in.array(), in.position(), in.position() + length));
        in.position(in.position() + length);
        return out;
    }

    /**
     * Reads a UTF8 string of length {@code length} from the byte array.
     */
    public static UTF8Container readBytes(int length, byte[] in, int position) {
        return new UTF8Container(Arrays.copyOfRange(in, position, position + length));
    }

    /**
     * Copies bytes from a ByteBuffer to a ByteArrayOutputStream.
     */
    public static void copyBytes(int count, ByteBuffer in, OutputStream out) throws IOException {
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
