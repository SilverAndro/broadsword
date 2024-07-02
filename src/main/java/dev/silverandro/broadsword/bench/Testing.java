/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.bench;

import dev.silverandro.broadsword.data.ClassMappingStruct;
import dev.silverandro.broadsword.mappings.EnigmaMappings;
import dev.silverandro.broadsword.mappings.TinyMappings;
import dev.silverandro.broadsword.mappings.TsrgMappings;
import dev.silverandro.broadsword.tools.TrueClassFileRemapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

        var open = new File("run/Dummy.class");
        var output = new File("run/testoutputs/Dummy.class");
        output.getParentFile().mkdirs();
        output.createNewFile();

        var inputStream = new FileInputStream(open);
        var bytes = inputStream.readAllBytes();
        inputStream.close();

        int n = 100_000;
        while (n-- > 0) {
            TrueClassFileRemapper.remapClassBytes(
                    bytes,
                    yarn,
                    className -> new ClassMappingStruct(List.of(), Map.of()),
                    className -> new FileOutputStream(output)
            );
        }
    }
}
