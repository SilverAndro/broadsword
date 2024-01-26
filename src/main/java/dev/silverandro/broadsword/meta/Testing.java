/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.meta;

import dev.silverandro.broadsword.ClassMappingStruct;
import dev.silverandro.broadsword.mappings.EnigmaMappings;
import dev.silverandro.broadsword.mappings.TinyMappings;
import dev.silverandro.broadsword.mappings.TsrgMappings;
import dev.silverandro.broadsword.tools.ClassFileRemapper;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

class Testing {
    public static void main(String[] arg) throws IOException {
        var yarn = new EnigmaMappings();
        yarn.parseFromDirectory(Path.of("run/mappings/yarn"));

        var tsrg = new TsrgMappings();
        tsrg.parseMappingsFile(new File("run/mappings/joined.tsrg"));

        var tiny = new TinyMappings();
        tiny.parseMappingsFile(new File("run/mappings/mappings.tiny"));

        var open = new File("run/o.class");
        var output = new File("run/testoutputs/o.class");
        output.getParentFile().mkdirs();
        output.createNewFile();

        var open2 = new File("run/VerifyTool.class");
        var inputStream2 = new FileInputStream(open2);
        var bytes2 = inputStream2.readAllBytes();

        int n = 10_000_000;
        while (n-- > 0) {
            ClassFileRemapper.remapClassBytes(
                    bytes2,
                    yarn,
                    className -> new ClassMappingStruct(List.of(), Map.of()),
                    className -> OutputStream.nullOutputStream()
            );
        }


        var inputStream = new FileInputStream(open);
        var bytes = inputStream.readAllBytes();
        inputStream.close();

        ClassFileRemapper.remapClassBytes(
                bytes,
                yarn,
                className -> new ClassMappingStruct(List.of(), Map.of()),
                className -> new FileOutputStream(output)
        );
    }
}
