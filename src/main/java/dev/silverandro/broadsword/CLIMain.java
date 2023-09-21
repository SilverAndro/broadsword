/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword;

import dev.silverandro.broadsword.mappings.EnigmaMappings;
import dev.silverandro.broadsword.mappings.TinyMappings;
import dev.silverandro.broadsword.mappings.TsrgMappings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class CLIMain {
    public static void main(String[] arg) throws IOException {
        var yarn = new EnigmaMappings();
        yarn.parseFromDirectory(Path.of("run/mappings/yarn"));

        var tsrg = new TsrgMappings();
        tsrg.parseMappingsFile(new File("run/mappings/joined.tsrg"));

        var tiny = new TinyMappings();
        tiny.parseMappingsFile(new File("run/mappings/1.20.1.tiny"));

        ClassFileRemapper.registerMappings("intermediary", "named", yarn);
        ClassFileRemapper.registerMappings("obf", "tsrg", tsrg);

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
