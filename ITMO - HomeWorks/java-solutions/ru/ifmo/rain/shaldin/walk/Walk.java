package ru.ifmo.rain.shaldin.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class Walk {
    private static final int BUFFER_SIZE = 4096;

    private static int hash(FileInputStream fileInput) throws IOException {
        if (fileInput == null) {
            return 0;
        }
        int c = 0, hash = 0x811c9dc5;
        byte[] BUFFER = new byte[BUFFER_SIZE];
        while ((c = fileInput.read(BUFFER)) >= 0) {
            for (int i = 0; i < c; i++) {
                hash = (hash * 0x01000193) ^ (BUFFER[i] & 0xff);
            }
        }
        return hash;
    }

    private static void run(String inputFileName, String outputFileName) throws IOException {
        try {
            Paths.get(outputFileName);
        } catch (InvalidPathException e) {
            throw new InvalidPathException("Invalid output path", e.getMessage());
        }
        try {
            Paths.get(inputFileName);
        } catch (InvalidPathException e) {
            throw new InvalidPathException("Invalid input path", e.getMessage());
        }
        try {
            if (Paths.get(outputFileName).getParent() != null) {
                Files.createDirectories(Paths.get(outputFileName).getParent());
            }
        } catch (IOException e) {
            throw new IOException("Cannot create directories for output file. " + e);
        }
        try (BufferedReader input = new BufferedReader(new FileReader(inputFileName, StandardCharsets.UTF_8))) {
            try (BufferedWriter output = new BufferedWriter(new FileWriter(outputFileName, StandardCharsets.UTF_8))) {
                String line;
                while ((line = input.readLine()) != null) {
                    int hash = 0;
                    try (FileInputStream fileInput = new FileInputStream(line)) {
                        hash = hash(fileInput);
                    } catch (Exception ignore) {
                    } finally {
                        output.write(String.format("%08x", hash) + ' ' + line);
                        output.newLine();
                    }
                }
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException("No such output file. " + e.getMessage());
            } catch (IOException e) {
                throw new IOException("Cannot read or write in output file. " + e.getMessage());
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("No such input file. " + e.getMessage());
        } catch (IOException e) {
            throw new IOException("Cannot read or write in input file. " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        if (args == null) {
            System.err.println("Arguments is null. You should use RecursiveWalk input_file_name output_file_name");
            return;
        }
        if (args.length != 2) {
            System.err.println("Wrong number of arguments");
            return;
        }
        if (args[0] == null || args[1] == null) {
            System.err.println("Arguments cannot be null");
            return;
        }
        try {
            run(args[0], args[1]);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
