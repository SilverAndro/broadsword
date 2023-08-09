/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.benchmark;

import dev.silverandro.broadsword.mappings.EnigmaMappings;
import net.fabricmc.mappingio.FlatMappingVisitor;
import net.fabricmc.mappingio.format.EnigmaReader;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Measurement(timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.SingleShotTime)
public class ReadEnigmaMappings {
    private final Path path = Path.of("run/mappings");

    @Benchmark
    public void broadsword(Blackhole bh) throws IOException {
        var mapping = new EnigmaMappings();
        mapping.parseFromDirectory(path);
        bh.consume(mapping);
    }

    @Benchmark
    public void mapping_io(Blackhole bh) throws IOException {
        var vistor = new FlatMappingVisitor() {
            @Override
            public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {

            }

            @Override
            public boolean visitClass(String srcName, String[] dstNames) throws IOException {
                return false;
            }

            @Override
            public void visitClassComment(String srcName, String[] dstNames, String comment) throws IOException {

            }

            @Override
            public boolean visitField(String srcClsName, String srcName, String srcDesc, String[] dstClsNames, String[] dstNames, String[] dstDescs) throws IOException {
                return false;
            }

            @Override
            public void visitFieldComment(String srcClsName, String srcName, String srcDesc, String[] dstClsNames, String[] dstNames, String[] dstDescs, String comment) throws IOException {

            }

            @Override
            public boolean visitMethod(String srcClsName, String srcName, String srcDesc, String[] dstClsNames, String[] dstNames, String[] dstDescs) throws IOException {
                return false;
            }

            @Override
            public void visitMethodComment(String srcClsName, String srcName, String srcDesc, String[] dstClsNames, String[] dstNames, String[] dstDescs, String comment) throws IOException {

            }

            @Override
            public boolean visitMethodArg(String srcClsName, String srcMethodName, String srcMethodDesc, int argPosition, int lvIndex, String srcArgName, String[] dstClsNames, String[] dstMethodNames, String[] dstMethodDescs, String[] dstArgNames) throws IOException {
                return false;
            }

            @Override
            public void visitMethodArgComment(String srcClsName, String srcMethodName, String srcMethodDesc, int argPosition, int lvIndex, String srcArgName, String[] dstClsNames, String[] dstMethodNames, String[] dstMethodDescs, String[] dstArgNames, String comment) throws IOException {

            }

            @Override
            public boolean visitMethodVar(String srcClsName, String srcMethodName, String srcMethodDesc, int lvtRowIndex, int lvIndex, int startOpIdx, String srcVarName, String[] dstClsNames, String[] dstMethodNames, String[] dstMethodDescs, String[] dstVarNames) throws IOException {
                return false;
            }

            @Override
            public void visitMethodVarComment(String srcClsName, String srcMethodName, String srcMethodDesc, int lvtRowIndex, int lvIndex, int startOpIdx, String srcVarName, String[] dstClsNames, String[] dstMethodNames, String[] dstMethodDescs, String[] dstVarNames, String comment) throws IOException {

            }
        };

        EnigmaReader.read(path, vistor.asMethodVisitor());
        bh.consume(vistor);
    }
}
