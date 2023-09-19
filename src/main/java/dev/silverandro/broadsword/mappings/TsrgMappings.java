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

public class TsrgMappings extends MappingsSet {
    @Override
    protected boolean supportsFieldTypes() {
        return false;
    }

    public void parseFromDirectory(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                if (path.getFileName().toString().endsWith(".tsrg")) {
                    parseMappingsFile(path.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void parseMappingsFile(File file) {
        new FileParser().loadMappingsFile(file);
    }

    private class FileParser {
        String currentClass = null;

        public void loadMappingsFile(File file) {
            try (var input = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                // Skip first metadata line
                input.readLine();
                String line = input.readLine();
                while (line != null) {
                    var clean = line.trim();
                    var trimAmount = line.length() - clean.length();

                    if (trimAmount == 0) {
                        readClassLine(clean);
                    } else if (trimAmount == 1) {
                        readFieldOrMethodLine(clean);
                    }

                    line = input.readLine();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void readFieldOrMethodLine(String clean) {
            var firstIndex = clean.indexOf(' ');
            var secondIndex = clean.indexOf(' ', firstIndex + 1);

            var firstEntry = clean.substring(0, firstIndex);
            var secondEntry = clean.substring(firstIndex + 1, secondIndex);
            if (secondEntry.charAt(0) == '(') {
                // Method
                var thirdIndex = clean.indexOf(' ', secondIndex + 1);
                var thirdEntry = clean.substring(secondIndex + 1, thirdIndex);
                methodMapping.put(currentClass + "!" + firstEntry + "!" + secondEntry, thirdEntry);
            } else {
                // Field
                fieldMapping.put(currentClass + "!" + firstEntry + "!null", secondEntry);
            }
        }

        private void readClassLine(String clean) {
            var firstIndex = clean.indexOf(' ');
            var secondIndex = clean.indexOf(' ', firstIndex + 1);
            currentClass = clean.substring(0, firstIndex);
            classMapping.put(currentClass, clean.substring(firstIndex + 1, secondIndex));
        }
    }
}
