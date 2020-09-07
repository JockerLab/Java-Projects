package ru.ifmo.rain.shaldin.walk;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RecursiveWalk {
    private static final int BUFFER_SIZE = 4096;

    private static int hash(InputStream fileInput) throws IOException {
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

    public static class MyFileVisitor extends SimpleFileVisitor<Path> {
        private final BufferedWriter output;
        MyFileVisitor(BufferedWriter out) {
            output = out;
        }
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            int hash = 0;
            try (InputStream fileInput = Files.newInputStream(file)) {
                hash = hash(fileInput);
            } catch (IOException | InvalidPathException e) {
                hash = 0;
            }
            output.write(String.format("%08x", hash) + " " + file.toString());
            output.newLine();
            return FileVisitResult.CONTINUE;
        }
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
        try (BufferedReader input = Files.newBufferedReader(Paths.get(inputFileName))) {
            try (BufferedWriter output = Files.newBufferedWriter(Paths.get(outputFileName))) {
                String line;
                while ((line = input.readLine()) != null) {
                    try {
                        Files.walkFileTree(Paths.get(line), new MyFileVisitor(output));
                    } catch (Exception e) {
                        output.write(String.format("%08x", 0) + ' ' + line);
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
