package ru.nsu.shadrina.search;

import java.io.File;
import java.util.Arrays;

public class Searcher {
    public static void searchByName(String name, String folderPath) {
        File dir = new File(folderPath);
        File[] files = dir.listFiles((dir1, fileName) -> fileName.equals(name));
        System.out.println(Arrays.toString(files));
    }

    public static void searchByText(String text, String folderPath) {

    }
}
