package ru.nsu.shadrina;

import ru.nsu.shadrina.search.Searcher;


public class Main {
    public static void main(String[] args) {
        check(args.length > 0, "You need to provide some arguments");
        var key = args[0];
        if (key.equals("--name")) {
            check(args.length == 3, "Wrong arguments count for searching by name");
            Searcher.searchByName(args[1], args[2]);
        } else if (key.equals("--data")) {
            check(args.length == 3, "Wrong arguments count for searching by text");
            Searcher.searchByText(formatString(args[1]), args[2]);
        } else {
            printHelp();
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
                "-h:                            Print help\n" +
                "--name <name> <folder-path>:   Find files by name in the folder\n" +
                "--data '<text>' <folder-path>: Find files by text in the folder";
        System.out.println(help);
    }
}
