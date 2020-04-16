package ru.ifmo.rain.shaldin.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RecursiveWalk extends Walk {
    public static class MyFileVisitor extends SimpleFileVisitor<Path> {
        private final BufferedWriter output;

        MyFileVisitor(BufferedWriter out, String line) {
            output = out;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            int hash = 0;
            try (InputStream fileInput = Files.newInputStream(file)) {
                hash = hash(fileInput);
            } catch (IOException e) {
                hash = 0;
            }
            output.write(String.format("%08x", hash) + ' ' + file.toString());
            output.newLine();
            return FileVisitResult.CONTINUE;
        }
    }

    @Override
    protected void visit(BufferedReader input, BufferedWriter output) throws IOException, FileNotFoundException {
        String line;
        while ((line = input.readLine()) != null) {
            try {
                Files.walkFileTree(Paths.get(line), new MyFileVisitor(output, line));
            } catch (Exception e) {
                output.write(String.format("%08x", 0) + ' ' + line);
                output.newLine();
            }
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
        RecursiveWalk walk = new RecursiveWalk();
        walk.run(args[0], args[1]);
    }
}
