package dev.silverandro.broadsword.mappings.util;

import dev.silverandro.broadsword.mappings.MappingsSet;

import java.io.*;

public class MappingsSetWriter extends MappingsSet {
    private final MappingsSet toWrite;

    public MappingsSetWriter(MappingsSet toWrite) {
        this.toWrite = toWrite;
    }

    public final void writeTinyV1(File file, String namespaceFrom, String namespaceTo) throws FileNotFoundException {
        try (var output = new PrintWriter(new FileOutputStream(file))) {
            output.println("v1\t" + namespaceFrom + "\t" + namespaceTo);
            toWrite.classMapping.forEach((s, s2) -> output.println("CLASS\t" + s + "\t" + s2));
            toWrite.fieldMapping.forEach((s, s2) -> {
                var decomposed = decomposeEntry(s);
                output.println("FIELD\t" + decomposed[0] + "\t" + decomposed[2] + "\t" + decomposed[1] + "\t" + s2);
            });
            toWrite.methodMapping.forEach((s, s2) -> {
                var decomposed = decomposeEntry(s);
                output.println("METHOD\t" + decomposed[0] + "\t" + decomposed[2] + "\t" + decomposed[1] + "\t" + s2);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] decomposeEntry(String entry) {
        return entry.split("!");
    }
}
