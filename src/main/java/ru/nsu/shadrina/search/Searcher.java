package ru.nsu.shadrina.search;

import ru.nsu.shadrina.algorithms.Algorithm;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class Searcher {
    private static int BUFFER_SIZE = 20000;

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
        var fileOrDirPath = Paths.get(folderPath);
        if (fileOrDir.isDirectory()) {
            Files.walk(fileOrDirPath)
                    .parallel()
                    .unordered()
                    .forEach(path -> {
                        var file = new File(path.toString());
                        if (file.isFile()) {
                            try {
                                searchByTextInFile(path, algorithm);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } else {
            searchByTextInFileParallel(Paths.get(folderPath), algorithm);
        }
    }

    private static void searchByTextInFileParallel(Path path, Algorithm algorithm) throws IOException {
        var cores = Runtime.getRuntime().availableProcessors();
        var patternLength = algorithm.getPattern().length();

        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            var bb = ByteBuffer.allocateDirect(BUFFER_SIZE);
            int n = channel.read(bb);
            var content = "";
            var globalPreviousPostfix = "";
            while (n > 0) {
                bb.flip();
                content = globalPreviousPostfix + StandardCharsets.UTF_8.decode(bb).toString();
                var contentLength = content.length();
                var partLength = contentLength / cores;

                var parts = new ArrayList<String>((contentLength + partLength - 1) / partLength);
                var previousPostfix = "";
                for (var start = 0; start < contentLength; start += partLength) {
                    var end = Math.min(contentLength, start + partLength);
                    parts.add(previousPostfix + content.substring(start, end));
                    previousPostfix = content.substring(end - patternLength, end);
                }
                parts.parallelStream()
                        .unordered()
                        .filter(part -> algorithm.search(part) != part.length())
                        .findFirst()
                        .ifPresent(part -> System.out.println(path.getFileName().toString()));
                globalPreviousPostfix = content.substring(contentLength - patternLength, contentLength);
                n = channel.read(bb);
            }
        }
    }

    private static void searchByTextInFile(Path path, Algorithm algorithm) throws IOException {
        var patternLength = algorithm.getPattern().length();
        try (SeekableByteChannel channel = Files.newByteChannel(path)) {
            var bb = ByteBuffer.allocateDirect(BUFFER_SIZE);
            int n = channel.read(bb);
            var content = "";
            var previousPostfix = "";
            while (n > 0) {
                content = previousPostfix + bb.toString();
                var contentLength = content.length();
                if (algorithm.search(content) != contentLength) {
                    System.out.println(path.getFileName().toString());
                }

                previousPostfix = content.substring(contentLength - patternLength, contentLength);
                n = channel.read(bb);
            }
        }
    }
}
