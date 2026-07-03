package com.example.akarigame;

import java.util.Arrays;

public class Puzzle {
    private int size;
    private int[][] originalBoard; // board asli
    private int[][] currentBoard;  // board yang bisa dimodifikasi
    private String difficulty;

    public Puzzle(int size, int[][] board, String difficulty) {
        this.size = size;
        this.originalBoard = deepCopy(board);
        this.currentBoard = deepCopy(board);
        this.difficulty = difficulty;
    }

    public String getDifficulty() {
        return difficulty;
    }


    private int[][] deepCopy(int[][] original) {
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return copy;
    }

    // Getter dan setter
    public int getSize() { return size; }
    public int[][] getCurrentBoard() { return currentBoard; }

    public void resetBoard() {
        this.currentBoard = deepCopy(originalBoard);
    }
}