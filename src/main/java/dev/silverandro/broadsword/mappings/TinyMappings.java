/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.mappings;

import dev.silverandro.broadsword.tools.UTF8Container;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TinyMappings extends MappingsSet {
    public void parseMappingsFile(File file) {
        new FileParser().loadMappingsFile(file);
    }

    private class FileParser {
        public void loadMappingsFile(File file) {
            try (var input = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                // Skip first metadata line
                input.readLine();
                String line = input.readLine();
                while (line != null) {
                    char firstChar = line.charAt(0);
                    if (firstChar == 'C') {
                        readClassLine(line);
                    } else {
                        readFieldOrMethodLine(line);
                    }
                    line = input.readLine();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void readClassLine(String line) {
            int startIndex = "CLASS\t".length();
            int splitIndex = line.indexOf('\t', startIndex);
            String original = line.substring(startIndex, splitIndex);
            String after = line.substring(splitIndex + 1);
            classMapping.put(new UTF8Container(original), new UTF8Container(after));
        }

        private void readFieldOrMethodLine(String line) {
            String[] split = line.split("\t");
            var ownerClass = split[1];
            var desc = split[2];
            var original = split[3];
            var after = split[4];
            if (split[0].charAt(0) == 'F') {
                fieldMapping.put(OwnedType.of(ownerClass, original, desc), new UTF8Container(after));
            } else {
                methodMapping.put(OwnedType.of(ownerClass, original, desc), new UTF8Container(after));
            }
        }
    }
}
