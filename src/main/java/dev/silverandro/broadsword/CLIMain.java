/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword;

import dev.silverandro.broadsword.mappings.EnigmaMappings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class CLIMain {
    public static void main(String[] arg) throws IOException {
        var mapping = new EnigmaMappings();
        mapping.parseFromDirectory(Path.of("run/mappings"));
        ClassFileRemapper.registerMappings("intermediary", "named", mapping);

        var open = new File("run/testclass.class");
        var output = new File("run/testoutputs/testclass.class");
        output.getParentFile().mkdirs();
        output.createNewFile();

        var inputStream = new FileInputStream(open);
        var bytes = inputStream.readAllBytes();
        inputStream.close();

        var resultingBytes = ClassFileRemapper.remapClassBytes(
                bytes,
                "intermediary", "named",
                className -> {
                    System.out.println("Received class info request for " + className);
                    return new ClassMappingStruct(List.of(), Map.of());
                }
        );

        var outputStream = new FileOutputStream(output);
        outputStream.write(resultingBytes);
        outputStream.close();
    }
}
