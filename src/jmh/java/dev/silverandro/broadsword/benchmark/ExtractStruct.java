package dev.silverandro.broadsword.benchmark;

import dev.silverandro.broadsword.ClassStructExtractor;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Measurement(timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
public class ExtractStruct {
    byte[] classFile;

    @Setup
    public void setup() throws IOException {
        var open = new File("run/testclass.class");
        var stream = new FileInputStream(open);
        classFile = stream.readAllBytes();
        stream.close();
    }

    @Benchmark
    public void extract(Blackhole bh) {
        bh.consume(ClassStructExtractor.extract(classFile));
    }
}
