package dev.silverandro.broadsword;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * A representation of a UTF-8 sequence of bytes. This class is used throughout broadsword to both make explicit the
 * encoding of this data and to avoid array copies in several locations. The data stored in this class is mutable and
 * must be explicitly copied when required.
 */
public final class UTF8Container {
    private final byte[] data;
    private int hash;
    private boolean hashIsZero;

    public UTF8Container(byte[] data) {
        this.data = data;
    }
    public UTF8Container(String value) { this(value.getBytes(StandardCharsets.UTF_8)); }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UTF8Container that = (UTF8Container) o;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && !hashIsZero) {
            h = hash(data);
            if (h == 0) {
                hashIsZero = true;
            } else {
                hash = h;
            }
        }
        return h;
    }

    private static int hash(byte[] data) {
        int h = 0;
        for (byte v : data) {
            h = 31 * h + (v & 0xff);
        }
        return h;
    }

    public boolean startsWith(char c) {
        return data[0] == c;
    }

    boolean startsWithJava() {
        return data[0] == 'j' && data[1] == 'a' && data[2] == 'v' && data[3] == 'a';
    }
}
