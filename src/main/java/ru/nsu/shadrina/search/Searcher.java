package ru.nsu.shadrina.search;

import ru.nsu.shadrina.algorithms.Algorithm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Searcher {
    public static void searchByName(String folderPath, Algorithm algorithm) throws IOException {
        Files.walk(Paths.get(folderPath))
                .parallel()
                .unordered()
                .filter(path -> {
                    var fileName = path.getFileName().toString();
                    return algorithm.search(fileName) != fileName.length();
                })
                .forEach(System.out::println);
    }

    public static void searchByText(String folderPath, Algorithm algorithm) throws IOException {
        var fileOrDir = new File(folderPath);
        if (fileOrDir.isDirectory()) {
            Files.walk(Paths.get(folderPath))
                    .parallel()
                    .unordered()
                    .forEach(path -> {
                        var file = new File(path.toString());
                        if (file.isFile()) {
                            try {
                                searchByTextInFile(file, algorithm);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } else {
            searchByTextInFileParallel(fileOrDir, algorithm);
        }
    }

    private static void searchByTextInFileParallel(File file, Algorithm algorithm) throws IOException {
        var content = new String(Files.readAllBytes(file.toPath()));
        var contentLength = content.length();
        var patternLength = algorithm.getPattern().length();
        var cores = Runtime.getRuntime().availableProcessors();

        var partLength = contentLength / cores;
        if (partLength < patternLength) {
            searchByTextInFile(file, algorithm);
        } else {
            var parts = new ArrayList<String>((contentLength + partLength - 1) / partLength);
            var addStart = false;
            var intermediatePart = new StringBuilder();
            for (var start = 0; start < contentLength; start += partLength) {
                var end = Math.min(contentLength, start + partLength);
                parts.add(content.substring(start, end));
                if (addStart) {
                    intermediatePart.append(content, start, start + patternLength);
                } else {
                    intermediatePart.append(content, end - patternLength, end);
                }
                addStart = !addStart;
                if (intermediatePart.length() == 2 * patternLength) {
                    parts.add(intermediatePart.toString());
                    intermediatePart.setLength(0);
                }
            }
            parts.parallelStream()
                    .unordered()
                    .filter(part ->
                            algorithm.search(part) != part.length()
                    ).findFirst()
                    .ifPresent(part -> System.out.println(file.getName()));
        }
    }

    private static void searchByTextInFile(File file, Algorithm algorithm) throws IOException {
        var content = new String(Files.readAllBytes(file.toPath()));
        if (algorithm.search(content) != content.length()) {
            System.out.println(file.getName());
        }
    }
}
