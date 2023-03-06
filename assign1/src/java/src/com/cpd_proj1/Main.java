package com.cpd_proj1;

import java.util.Scanner;

public class Main {

    public static void initializeMatrices(double[] pha, double[] phb, int mx_size) {
        int i, j;
        for (i = 0; i < mx_size; ++i)
            for (j = 0; j < mx_size; ++j)
                pha[calculateIndex(i, j, mx_size)] = 1.0;

        for (i = 0; i < mx_size; ++i)
            for (j = 0; j < mx_size; ++j)
                phb[calculateIndex(i, j, mx_size)] = (double)(i + 1);
    }

    public static int calculateIndex(int i, int j, int size) {
        return i*size + j;
    }

    public static void onMult(int mx_size) {
        int i, j, k;
        double temp;

        // Setup
        double[] pha = new double[mx_size * mx_size];
        double[] phb = new double[mx_size * mx_size];
        double[] phc = new double[mx_size * mx_size];

        initializeMatrices(pha, phb, mx_size);

        // Start Counting
        long start = System.currentTimeMillis();

        for (i = 0; i < mx_size; ++i) {
            for (j = 0; j < mx_size; ++j) {
                temp = 0;
                for (k = 0; k < mx_size; ++k) {
                    temp += pha[calculateIndex(i, k, mx_size)] * phb[calculateIndex(k, j, mx_size)];
                }
                phc[calculateIndex(i, j, mx_size)] = temp;
            }
        }

        long end = System.currentTimeMillis();
        double timeElapsed = (double)(end - start)/1000;
        String st = String.format("Time: %3.3f seconds%n", timeElapsed);
        System.out.println(st);

        System.out.println("Result matrix: ");
        for (i = 0; i < 1; ++i)
            for (j = 0; j < Math.min(10, mx_size); j++)
                System.out.print(phc[calculateIndex(i, j, mx_size)] + " ");

        System.out.println();
    }

    public static void onMultLine(int mx_size) {
        int i, j, k;
        double temp;

        // Setup
        double[] pha = new double[mx_size * mx_size];
        double[] phb = new double[mx_size * mx_size];
        double[] phc = new double[mx_size * mx_size];

        initializeMatrices(pha, phb, mx_size);

        // Start Counting
        long start = System.currentTimeMillis();

        for (i = 0; i < mx_size; ++i)
            for (k = 0; k < mx_size; ++k)
                for (j = 0; j < mx_size; ++j)
                    phc[calculateIndex(i, j, mx_size)] += pha[calculateIndex(i, k, mx_size)] * phb[calculateIndex(k, j, mx_size)];

        long end = System.currentTimeMillis();
        double timeElapsed = (double)(end - start)/1000;
        String st = String.format("Time: %3.3f seconds%n", timeElapsed);
        System.out.println(st);

        System.out.println("Result matrix: ");
        for (i = 0; i < 1; ++i)
            for (j = 0; j < Math.min(10, mx_size); j++)
                System.out.print(phc[calculateIndex(i, j, mx_size)] + " ");

        System.out.println();
    }

    public static void onMultBlock(int mx_size, int blockSize) {
        System.out.println("onMultBlock function");
    }
    public static void main(String[] args) {
        char c;
        int mx_size, blockSize;
        int op;

        Scanner scanner = new Scanner(System.in);

        do {
            System.out.println("1. Multiplication");
            System.out.println("2. Line Multiplication");
            System.out.println("3. Block Multiplication");
            System.out.print("Selection?: ");

            op = scanner.nextInt();
            if (op == 0)
                break;

            System.out.print("Dimensions: lins=cols ? ");
            mx_size = scanner.nextInt();

            switch (op) {
                case 1 -> onMult(mx_size);
                case 2 -> onMultLine(mx_size);
                case 3 -> {
                    System.out.print("Block Size? ");
                    blockSize = scanner.nextInt();
                    onMultBlock(mx_size, blockSize);
                }
            }

        } while (true);

        scanner.close();
    }
}