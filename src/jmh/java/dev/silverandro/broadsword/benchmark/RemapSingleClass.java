/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.benchmark;

import dev.silverandro.broadsword.ClassFileRemapper;
import dev.silverandro.broadsword.ClassMappingStruct;
import dev.silverandro.broadsword.mappings.EnigmaMappings;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Measurement(timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
public class RemapSingleClass {
    private final String INTERMEDIARY = "intermediary";
    private final String NAMED = "named";
    byte[] classFile;

    @Setup
    public void setup() throws IOException {
        var mapping = new EnigmaMappings();
        mapping.parseFromDirectory(Path.of("run/mappings"));
        ClassFileRemapper.registerMappings(INTERMEDIARY, NAMED, mapping);

        var open = new File("run/testclass.class");
        var stream = new FileInputStream(open);
        classFile = stream.readAllBytes();
        stream.close();
    }

    @Benchmark
    public void broadsword(Blackhole bh) throws IOException {
        bh.consume(ClassFileRemapper.remapClassBytes(classFile, INTERMEDIARY, NAMED, className -> new ClassMappingStruct(List.of(), Map.of())));
    }
}
