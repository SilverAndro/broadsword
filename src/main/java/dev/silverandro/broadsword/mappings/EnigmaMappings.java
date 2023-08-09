/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.mappings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public final class EnigmaMappings extends MappingsSet {
    public void parseFromDirectory(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                if (path.getFileName().toString().endsWith(".mapping")) {
                    parseMappingsFile(path.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void parseMappingsFile(File file) {
        new FileParser().loadMappingsFile(file);
    }

    private class FileParser {
        String currentBeforeClass = null;
        String currentAfterClass = null;

        public void loadMappingsFile(File file) {
            try (var input = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                int prevTrim = 0;
                String line = input.readLine();
                while (line != null) {
                    var clean = line.trim();
                    var trimAmount = line.length() - clean.length();

                    if (parseMappingsLine(clean, trimAmount - prevTrim)) {
                        prevTrim = trimAmount;
                    }

                    line = input.readLine();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private boolean parseMappingsLine(String line, int trimDiff) {
            if (line.startsWith("COMMENT")) return false;

            if (line.startsWith("CLASS")) {
                var splice = line.substring("CLASS ".length());
                var splitIndex = splice.indexOf(' ');

                String before;
                String after;
                if (splitIndex == -1) {
                    before = splice;
                    after = before;
                } else {
                    before = splice.substring(0, splitIndex);
                    after = splice.substring(splitIndex + 1);
                }

                if (trimDiff > 0) {
                    before = currentBeforeClass + "$" + before;
                    after = currentAfterClass + "$" + after;
                } else if (trimDiff == 0 && currentBeforeClass != null) {
                    var remB = currentBeforeClass.lastIndexOf('$');
                    var remA = currentAfterClass.lastIndexOf('$');
                    before = currentBeforeClass.substring(0, remB) + "$" + before;
                    after = currentAfterClass.substring(0, remA) + "$" + after;
                } else if (trimDiff < 0) {
                    while (trimDiff++ < 0) {
                        var remB = currentBeforeClass.lastIndexOf('$');
                        var remA = currentAfterClass.lastIndexOf('$');
                        before = currentBeforeClass.substring(0, remB);
                        after = currentAfterClass.substring(0, remA);
                    }
                    before = currentBeforeClass + "$" + before;
                    after = currentAfterClass + "$" + after;
                }

                currentBeforeClass = before;
                currentAfterClass = after;
                classMapping.put(before, after);
                return true;
            } else if (line.startsWith("FIELD")) {
                var splice = line.substring("FIELD ".length());

                var splitIndex = splice.indexOf(' ');

                var origName = splice.substring(0, splitIndex);
                var after = splice.substring(splitIndex + 1);

                var splitIndex2 = after.indexOf(' ');
                if (splitIndex2 != -1) {
                    var newName = after.substring(0, splitIndex2);
                    var desc = after.substring(splitIndex2 + 1);

                    fieldMapping.put(currentBeforeClass + "!" + origName + "!" + desc, newName);
                }
                return false;
            } else if (line.startsWith("METHOD")) {
                var splice = line.substring("METHOD ".length());

                var splitIndex = splice.indexOf(' ');

                var origName = splice.substring(0, splitIndex);
                if (origName.startsWith("<")) return false;
                var after = splice.substring(splitIndex + 1);

                var splitIndex2 = after.indexOf(' ');
                if (splitIndex2 == -1) return false;

                var newName = after.substring(0, splitIndex2);
                var desc = after.substring(splitIndex2 + 1);

                methodMapping.put(currentBeforeClass + "!" + origName + "!" + desc, newName);
                return false;
            }  else if (line.startsWith("ARG")) {
                return false;
            } else {
                throw new IllegalStateException("Don't know how to handle mapping line \"" + line + "\"");
            }
        }
    }
}
