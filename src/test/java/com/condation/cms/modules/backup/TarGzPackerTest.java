package com.condation.cms.modules.backup;

/*-
 * #%L
 * backup-module
 * %%
 * Copyright (C) 2025 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class TarGzPackerTest {

    @Test
    void testCreateTarGz() throws IOException {
        // Temporäre Struktur anlegen
		Path root = Files.createTempDirectory("testRoot");
        Path project = Files.createDirectories(root.resolve("project"));
        Path folder1 = Files.createDirectories(project.resolve("folder1"));
        Path file1 = Files.writeString(folder1.resolve("file1.txt"), "Hello");
        Path file2 = Files.writeString(project.resolve("file2.txt"), "World");

        File outputArchive = root.resolve("archive.tar.gz").toFile();

        // Packen
        TarGzPacker.createTarGz(
				root,
                outputArchive,
                List.of(folder1, file2)
        );

        // Inhalte aus dem Archiv lesen
        Set<String> entryNames = new HashSet<>();
        try (FileInputStream fis = new FileInputStream(outputArchive);
             BufferedInputStream bis = new BufferedInputStream(fis);
             GzipCompressorInputStream gzis = new GzipCompressorInputStream(bis);
             TarArchiveInputStream tais = new TarArchiveInputStream(gzis)) {

            TarArchiveEntry entry;
            while ((entry = tais.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    entryNames.add(entry.getName());
                }
            }
        }

        // Erwartete Pfade (relativ zum Root)
        Set<String> expected = Set.of(
                "project/folder1/file1.txt",
                "project/file2.txt"
        );

        // Prüfung mit AssertJ
        assertThat(entryNames)
                .containsExactlyInAnyOrderElementsOf(expected);
    }
}
