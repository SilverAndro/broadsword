/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.benchmark;

import dev.silverandro.broadsword.ClassMappingStruct;
import dev.silverandro.broadsword.mappings.EnigmaMappings;
import dev.silverandro.broadsword.mappings.MappingsSet;
import dev.silverandro.broadsword.tools.ClassFileRemapper;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Measurement(timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
public class RemapSingleClass {
    private MappingsSet mappingsSet = null;
    byte[] classFile;

    @Setup
    public void setup() throws IOException {
        var mapping = new EnigmaMappings();
        mapping.parseFromDirectory(Path.of("run/mappings"));
        mappingsSet = mapping;

        var open = new File("run/testclass.class");
        var stream = new FileInputStream(open);
        classFile = stream.readAllBytes();
        stream.close();
    }

    @Benchmark
    public void broadsword() throws IOException {
        ClassFileRemapper.remapClassBytes(
                classFile,
                mappingsSet,
                className -> new ClassMappingStruct(List.of(), Map.of()),
                className -> OutputStream.nullOutputStream()
        );
    }
}
