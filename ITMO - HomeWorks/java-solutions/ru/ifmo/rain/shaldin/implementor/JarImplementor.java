package ru.ifmo.rain.shaldin.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Generate code and pack it to .jar
 */
public class JarImplementor extends Implementor {
    /**
     * Constructor for {@link JarImplementor}
     */
    public JarImplementor() {}
    /**
     * Do implementation of given file
     *
     * @param args main arguments:
     *             token path if need to do implementation
     *             -jar token path if need to create jar after implementation
     * @throws ImplerException        if error occurs
     * @throws ClassNotFoundException if class doesn't exist
     */
    public static void main(String[] args) throws ImplerException, ClassNotFoundException {
        if (args == null) {
            System.err.println("Use -jar <type_token> <dir_path>");
            return;
        }
        JarImpler implementor = new Implementor();
        if (args.length == 3 && "-jar".equals(args[0])) {
            try {
                Class<?> clazz = Class.forName(args[1]);
            } catch (ClassNotFoundException e) {
                System.err.println("Cannot find class.");
                return;
            }
            try {
                Path path = Paths.get(args[2]);
            } catch (InvalidPathException e) {
                System.err.println("Invalid path.");
                return;
            }
            implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
        } else {
            System.err.println("Use -jar <type_token> <dir_path>");
        }
    }
}
