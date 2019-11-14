package ru.nsu.shadrina;

import ru.nsu.shadrina.algorithms.Algorithm;
import ru.nsu.shadrina.algorithms.BoyerMoore;
import ru.nsu.shadrina.algorithms.KMP;
import ru.nsu.shadrina.algorithms.RabinKarp;
import ru.nsu.shadrina.search.Searcher;

import java.io.IOException;


public class Main {
    public static void main(String[] args) {
        check(args.length > 0, "You need to provide some arguments");
        var key = args[0];
        try {
            if (key.equals("--name")) {
                check(args.length == 3, "Wrong arguments count for searching by name");
                var algorithm = initializeAlgorithm("kmp", args[1]);
                Searcher.searchByName(args[2], algorithm);
            } else if (key.equals("--data")) {
                check(args.length == 3, "Wrong arguments count for searching by text");
                var algorithm = initializeAlgorithm("kmp", formatString(args[1]));
                Searcher.searchByText(args[2], algorithm);
            } else {
                printHelp();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Algorithm initializeAlgorithm(String value, String pattern) {
        switch (value) {
            case "bm": return new BoyerMoore(pattern);
            case "rk": return new RabinKarp(pattern);
            default: return new KMP(pattern);
        }
    }

    private static String formatString(String s) {
        check(s.length() > 3, "String has to be wrapped in brackets and be non empty");
        return s.substring(1, s.length() - 1);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            System.err.println(message);
            printHelp();
            System.exit(1);
        }
    }

    private static void printHelp() {
        var help = "The following arguments are available:\n" +
                "-h:                              Print help\n" +
                "--name <name> <folder-path>:     Find files by name in the folder\n" +
                "--data \"<text>\" <folder-path>: Find files by text in the folder";
        System.out.println(help);
    }
}
