/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.meta;

import dev.silverandro.broadsword.ClassMappingStruct;
import dev.silverandro.broadsword.lookups.ClassMappingLookup;
import dev.silverandro.broadsword.mappings.TinyMappings;
import dev.silverandro.broadsword.tools.ClassFileRemapper;
import dev.silverandro.broadsword.tools.ClassStructExtractor;
import dev.silverandro.broadsword.tools.UTF8Container;
import net.fabricmc.tinyremapper.NonClassCopyMode;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipOutputStream;

class MeasureRemappers {
    static final File JAR_FILE = new File("run/jars/mc.jar");
    static final File MAPPINGS_FILE = new File("run/mappings/tiny-huge.tiny");
    static TinyMappings broadswordMappings;
    static TinyRemapper tinyRemapper;
    static TinyRemapper tinyRemapperThreads;

    public static void main(String[] arg) throws IOException {
        tinyRemapper = TinyRemapper.newRemapper()
                .withMappings(TinyUtils.createTinyMappingProvider(MAPPINGS_FILE.toPath(), "official", "intermediary"))
                .ignoreFieldDesc(false)
                .withForcedPropagation(Collections.emptySet())
                .propagatePrivate(false)
                .removeFrames(false)
                .ignoreConflicts(true)
                .checkPackageAccess(false)
                .fixPackageAccess(false)
                .resolveMissing(false)
                .rebuildSourceFilenames(false)
                .skipLocalVariableMapping(true)
                .renameInvalidLocals(false)
                .threads(1)
                .build();

        tinyRemapperThreads = TinyRemapper.newRemapper()
                .withMappings(TinyUtils.createTinyMappingProvider(MAPPINGS_FILE.toPath(), "official", "intermediary"))
                .ignoreFieldDesc(false)
                .withForcedPropagation(Collections.emptySet())
                .propagatePrivate(false)
                .removeFrames(false)
                .ignoreConflicts(true)
                .checkPackageAccess(false)
                .fixPackageAccess(false)
                .resolveMissing(false)
                .rebuildSourceFilenames(false)
                .skipLocalVariableMapping(true)
                .renameInvalidLocals(false)
                .threads(8)
                .build();

        broadswordMappings = new TinyMappings();
        broadswordMappings.parseMappingsFile(MAPPINGS_FILE);

        var mcBr = JAR_FILE.toPath().getParent().resolve("mc-br.jar").toFile();
        mcBr.createNewFile();
        var mcBrStream = new ZipOutputStream(new FileOutputStream(mcBr));
        mcBrStream.close();

        long startBr = System.currentTimeMillis();
        broadsword();
        long stopBr = System.currentTimeMillis();
        System.out.println("Broadsword done! Took " + (stopBr - startBr) + "ms");

        long startTR = System.currentTimeMillis();
        tinyRemapper();
        long stopTR = System.currentTimeMillis();
        System.out.println("Tiny remapper done! Took " + (stopTR - startTR) + "ms");

        long startTRT = System.currentTimeMillis();
        tinyRemapperThreads();
        long stopTRT = System.currentTimeMillis();
        System.out.println("Tiny remapper with threads done! Took " + (stopTRT - startTRT) + "ms");
    }

    private static void broadsword() throws IOException {
        URI uri = URI.create("jar:file:/" + JAR_FILE.getAbsolutePath().replace('\\', '/'));
        FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap());

        URI outUri = URI.create("jar:file:/" + JAR_FILE.toPath().getParent().resolve("mc-br.jar").toFile().getAbsolutePath().replace('\\', '/'));
        FileSystem outFs = FileSystems.newFileSystem(outUri, Collections.emptyMap());

        var lookup = getClassMappingLookupSingle(fs);

        for (var root : fs.getRootDirectories()) {
            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    if (path.getFileName().toString().endsWith(".class")) {
                        InputStream inputStream = Files.newInputStream(path);
                        byte[] classFile = inputStream.readAllBytes();
                        inputStream.close();

                        ClassFileRemapper.remapClassBytes(
                                classFile,
                                broadswordMappings,
                                lookup,
                                className -> {
                                    var outFsPath = outFs.getPath(className+".class");
                                    Files.createDirectories(outFsPath.getParent());
                                    return Files.newOutputStream(outFsPath);
                                }
                        );
                    } else {
                        if (Files.isRegularFile(path)) {
                            var outFsPath = outFs.getPath(path.toString());
                            Files.createDirectories(outFsPath.getParent());
                            Files.copy(path, outFsPath);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        fs.close();
        outFs.close();
    }

    private static ClassMappingLookup getClassMappingLookupSingle(FileSystem fs) {
        var structs = new HashMap<UTF8Container, ClassMappingStruct>();
        return className -> structs.computeIfAbsent(className, s -> {
            try {
                var newEntry = fs.getPath(className + ".class");
                if (Files.exists(newEntry)) {
                    InputStream stream = Files.newInputStream(newEntry);
                    var res = ClassStructExtractor.extract(stream.readAllBytes());
                    stream.close();
                    return res;
                } else {
                    return new ClassMappingStruct(Collections.emptyList(), Collections.emptyMap());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static ClassMappingLookup getClassMappingLookupMulti(FileSystem fs) {
        var structs = new ConcurrentHashMap<UTF8Container, ClassMappingStruct>();
        return className -> structs.computeIfAbsent(className, s -> {
            try {
                var newEntry = fs.getPath(className + ".class");
                if (Files.exists(newEntry)) {
                    InputStream stream = Files.newInputStream(newEntry);
                    var res = ClassStructExtractor.extract(stream.readAllBytes());
                    stream.close();
                    return res;
                } else {
                    return new ClassMappingStruct(Collections.emptyList(), Collections.emptyMap());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void tinyRemapper() {
        NonClassCopyMode ncCopyMode = NonClassCopyMode.SKIP_META_INF;
        final Path[] classpath = new Path[]{};


        try (OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(JAR_FILE.toPath().getParent().resolve("mc-tr.jar")).build()) {
            outputConsumer.addNonClassFiles(JAR_FILE.toPath(), ncCopyMode, tinyRemapper);

            tinyRemapper.readInputs(JAR_FILE.toPath());
            tinyRemapper.readClassPath(classpath);

            tinyRemapper.apply(outputConsumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            tinyRemapper.finish();
        }
    }

    private static void tinyRemapperThreads() {
        NonClassCopyMode ncCopyMode = NonClassCopyMode.SKIP_META_INF;
        final Path[] classpath = new Path[]{};


        try (OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(JAR_FILE.toPath().getParent().resolve("mc-tr.jar")).build()) {
            outputConsumer.addNonClassFiles(JAR_FILE.toPath(), ncCopyMode, tinyRemapperThreads);

            tinyRemapperThreads.readInputs(JAR_FILE.toPath());
            tinyRemapperThreads.readClassPath(classpath);

            tinyRemapperThreads.apply(outputConsumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            tinyRemapperThreads.finish();
        }
    }
}
