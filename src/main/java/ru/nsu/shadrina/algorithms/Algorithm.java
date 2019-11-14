package ru.nsu.shadrina.algorithms;

public interface Algorithm {
    int search(String txt);

    int search(char[] txt);

    String getPattern();
}
