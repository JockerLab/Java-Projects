package ru.ifmo.rain.shaldin.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Walk {
    private static final int BUFFER_SIZE = 1024;

    protected static int hash(InputStream fileInput) throws IOException {
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

    protected static void check(String inputFileName, String outputFileName) {
        try {
            Paths.get(outputFileName);
        } catch (InvalidPathException e) {
            System.err.println("Invalid output path");
            return;
        }
        try {
            Paths.get(inputFileName);
        } catch (InvalidPathException e) {
            System.err.println("Invalid input path");
            return;
        }
        try {
            if (Paths.get(outputFileName).getParent() != null) {
                Files.createDirectories(Paths.get(outputFileName).getParent());
            }
        } catch (IOException e) {
            System.err.println("Cannot create directories for output file.");
            return;
        }
    }

    protected void visit(BufferedReader input, BufferedWriter output) throws IOException, FileNotFoundException {
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
    }

    protected void run(String inputFileName, String outputFileName) throws IOException {
        check(inputFileName, outputFileName);
        try (BufferedReader input = new BufferedReader(new FileReader(inputFileName, StandardCharsets.UTF_8))) {
            try (BufferedWriter output = new BufferedWriter(new FileWriter(outputFileName, StandardCharsets.UTF_8))) {
                visit(input, output);
            } catch (FileNotFoundException e) {
                System.err.println("No such file");
            } catch (IOException e) {
                System.err.println("Cannot read or write");
            }
        } catch (FileNotFoundException e) {
            System.err.println("No such file");
        } catch (IOException e) {
            System.err.println("Cannot read or write");
        }
    }

    public static void main(String[] args) throws IOException {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Wrong number of arguments. You should use Walk input_file_name output_file_name");
        } else {
            Walk walk = new Walk();
            walk.run(args[0], args[1]);
        }
    }
}
