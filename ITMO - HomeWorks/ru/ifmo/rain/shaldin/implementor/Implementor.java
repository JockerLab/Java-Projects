package ru.ifmo.rain.shaldin.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

/**
 * Implement class Implementor for {@link JarImpler} interface
 */
public class Implementor implements JarImpler {
    /**
     * Using for recursive deleting files
     */
    private static final SimpleFileVisitor<Path> DELETE_VISITOR = new SimpleFileVisitor<>() {
        /**
         * Deletes files in file
         * @param path path to file
         * @param attrs attributes of file
         * @return {@link FileVisitResult#CONTINUE}
         * @throws IOException if an I/O error occurs
         */
        @Override
        public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException {
            Files.delete(path);
            return FileVisitResult.CONTINUE;
        }

        /**
         * Deletes directory after deleting al files
         * @param dir current directory
         * @param exc null if the iteration of the directory completes without an error;
         *            otherwise the I/O exception that caused the iteration of the directory to complete prematurely
         * @return {@link FileVisitResult#CONTINUE}
         * @throws IOException if an I/O error occurs
         */
        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    };

    /**
     * Deletes file in current directory
     *
     * @param dir deleting files in current directory
     * @throws IOException if an I/O error is thrown by a visitor method
     */
    private static void clean(final Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walkFileTree(dir, DELETE_VISITOR);
        }
    }

    /**
     * Convert {@link String} to unicode
     *
     * @param s input string data
     * @return {@link String} converted to unicode
     */
    private String getUnicodeName(String s) {
        StringBuilder b = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= 128)
                b.append("\\u").append(String.format("%04X", (int) c));
            else
                b.append(c);
        }
        return b.toString();
    }

    /**
     * Write {@link String} data in output stream {@link Writer}
     *
     * @param writer output stream {@link Writer}
     * @param data   input {@link String} data
     * @throws IOException If an I/O error occurs
     */
    private void writeInUnicode(Writer writer, String data) throws IOException {
        try {
            writer.write(getUnicodeName(data));
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    /**
     * Generate source code by token in directory root
     *
     * @param token type token to create implementation for
     * @param root  root directory
     * @throws ImplerException if cannot open file or cannot write to file
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        check(token, root);
        root = resolveRoot(token, root, ".java");
        createDirectories(root);
        try (BufferedWriter output = Files.newBufferedWriter(root)) {
            try {
                writeInUnicode(output, getPackage(token) + "public class " + getClassName(token) +
                        (token.isInterface() ? " implements " : " extends ") +
                        token.getCanonicalName() + " {" + System.lineSeparator());
                if (!token.isInterface()) {
                    getClassBody(token, output, "constructor");
                }
                getClassBody(token, output, "method");
                writeInUnicode(output, "}" + System.lineSeparator());
            } catch (IOException e) {
                throw new ImplerException("Cannot write to output file", e);
            }
        } catch (IOException e) {
            throw new ImplerException("Cannot open output file", e);
        }
    }

    /**
     * Generate .jar file from {@link Implementor}
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws ImplerException if cannot create temp directory,
     *                         create compiler, compile generated file or write to .jar
     */
    @Override
    public void implementJar(Class<?> token, Path root) throws ImplerException {
        check(token, root);
        createDirectories(root);
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory(root.toAbsolutePath().getParent(), "temp_dir");
        } catch (IOException e) {
            throw new ImplerException("Cannot create temp directory", e);
        }
        try {
            implement(token, tempDir);

            final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            final List<String> args = new ArrayList<>();
            args.add("-cp");
            args.add(tempDir.toString() + File.pathSeparator + System.getProperty("java.class.path"));
            args.add(resolveRoot(token, tempDir, ".java").toString());
            if (compiler == null) {
                throw new ImplerException("Cannot create compiler.");
            }
            if (compiler.run(null, null, null, args.toArray(String[]::new)) != 0) {
                throw new ImplerException("Cannot compile generated file");
            }
            Manifest manifest = new Manifest();
            Attributes attributes = manifest.getMainAttributes();
            attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            attributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Vsevolod Shaldin");
            try (JarOutputStream writer = new JarOutputStream(Files.newOutputStream(root), manifest)) {
                writer.putNextEntry(new ZipEntry(token.getPackageName().replace('.', '/') + '/' + getClassName(token) + ".class"));
                Files.copy(resolveRoot(token, tempDir, ".class"), writer);
            } catch (IOException e) {
                throw new ImplerException("Unable to write to .jar file", e);
            }
        } finally {
            try {
                clean(tempDir);
            } catch (IOException e) {
                System.out.println("Unable to delete temp directory: " + e.getMessage());
            }
        }
    }

    /**
     * Resolve root with token
     *
     * @param token  type token to create implementation for
     * @param root   root directory
     * @param ending {@link String} suffix
     * @return {@link Path} resolved root and token
     */
    private Path resolveRoot(Class<?> token, Path root, String ending) {
        return root.resolve(token.getPackageName().replace('.', File.separatorChar))
                .resolve(getClassName(token) + ending);
    }

    /**
     * Create directories on the path to root
     *
     * @param root directory
     * @throws ImplerException if cannot create directories on the path
     */
    private void createDirectories(Path root) throws ImplerException {
        if (root.getParent() != null) {
            try {
                Files.createDirectories(root.getParent());
            } catch (IOException e) {
                throw new ImplerException("Cannot create directories on the path to root.", e);
            }
        }
    }

    /**
     * Check arguments if null
     *
     * @param token type token to create implementation for
     * @param root  root directory
     * @throws ImplerException if error occurs
     */
    private void check(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Arguments cannot be null.");
        }
        if (Modifier.isPrivate(token.getModifiers()) || token.isPrimitive() || token.isArray() || Modifier.isFinal(token.getModifiers()) || token == Enum.class) {
            throw new ImplerException("Wrong class token.");
        }
    }

    /**
     * Class uses to correctly compare {@link Method}
     */
    private static class HashMethod {
        /**
         * Instance of {@link Method}
         */
        private final Method method;

        /**
         * Constructor for {@link HashMethod}
         *
         * @param method input method
         */
        HashMethod(final Method method) {
            this.method = method;
        }

        /**
         * Getter to get method
         *
         * @return {@link Method} which needed
         */
        Method getMethod() {
            return method;
        }

        /**
         * Compares object with this for equality.
         *
         * @param obj is {@link Object} which need to be compared
         * @return true if obj is equal
         */
        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof HashMethod) {
                final HashMethod tmp = (HashMethod) obj;
                return Arrays.equals(method.getParameterTypes(), tmp.method.getParameterTypes())
                        && method.getName().equals(tmp.method.getName());
            }
            return false;
        }

        /**
         * Calculate hashcode which depends on name, return type and parameters' types
         * @return hashCode of wrapped {@link Method}
         */
        @Override
        public int hashCode() {
            return Objects.hash(method.getName().hashCode(),
                    Arrays.hashCode(method.getParameterTypes()));
        }
    }

    /**
     * Create package from token
     *
     * @param token type token to create implementation for
     * @return {@link String} package
     */
    private String getPackage(Class<?> token) {
        if (token.getPackageName().equals("")) {
            return "";
        }
        return "package " + token.getPackageName() + ";" + System.lineSeparator();
    }

    /**
     * Create class name from token
     *
     * @param token type token to create implementation for
     * @return {@link String} class name
     */
    private String getClassName(Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    /**
     * Create {@link String} with all parameters in method or constructor
     *
     * @param method      {@link Method}
     * @param constructor {@link Constructor}
     * @return {@link String} with all parameters in method or constructor
     */
    private String getParameters(Method method, Constructor constructor) {
        Executable executable = (method == null ? constructor : method);
        return Arrays.stream(executable.getParameters())
                .map(parameter -> (parameter.getType().getCanonicalName() + " " + parameter.getName()))
                .collect(Collectors.joining(", ", "(", ")"));
    }

    /**
     * Create {@link String} with all excpetions thrown by this method or constructor
     *
     * @param method      {@link Method}
     * @param constructor {@link Constructor}
     * @return {@link String} with all excpetions thrown by this method or constructor
     */
    private String getExceptions(Method method, Constructor constructor) {
        Executable executable = (method == null ? constructor : method);
        Class<?>[] exceptions = executable.getExceptionTypes();
        if (exceptions.length == 0) {
            return "";
        }
        return " throws " + Arrays.stream(exceptions)
                .map(Class::getCanonicalName)
                .collect(Collectors.joining(", "));
    }

    /**
     * Return default values by token
     *
     * @param token type token to create implementation for
     * @return default values by token
     */
    private String getDefaultValue(Class<?> token) {
        if (token.equals(boolean.class)) {
            return " false";
        }
        if (token.equals(void.class)) {
            return "";
        }
        if (token.isPrimitive()) {
            return " 0";
        }
        return " null";
    }

    /**
     * Return {@link String} with body of needed constructors and methods
     *
     * @param method      {@link Method}
     * @param constructor {@link Constructor}
     * @return {@link String} with body of needed constructors and methods
     */
    private String getBody(Method method, Constructor constructor) {
        if (method != null) {
            return "return" + getDefaultValue(method.getReturnType());
        } else {
            return "super" + Arrays.stream(constructor.getParameters())
                    .map(Parameter::getName)
                    .collect(Collectors.joining(", ", "(", ")"));
        }
    }

    /**
     * Return return type of method or constructor
     *
     * @param token       type token to create implementation for
     * @param method      {@link Method}
     * @param constructor {@link Constructor}
     * @return return type of method or constructor
     */
    private String getReturnType(Class<?> token, Method method, Constructor constructor) {
        if (method != null) {
            return method.getReturnType().getCanonicalName() + " " + method.getName();
        } else {
            return getClassName(token);
        }
    }

    /**
     * Return {@link String} of full method or constructor
     *
     * @param token       type token to create implementation for
     * @param method      {@link Method}
     * @param constructor {@link Constructor}
     * @return return {@link String} of full method or constructor
     */
    private String getStringMethod(Class<?> token, Method method, Constructor constructor) {
        final int mods;
        if (method != null) {
            mods = method.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.NATIVE & ~Modifier.TRANSIENT;
        } else {
            mods = constructor.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.NATIVE & ~Modifier.TRANSIENT;
        }
        String spaces = "";
        if (mods > 0) {
            spaces = " ";
        }
        return Modifier.toString(mods) +
                spaces +
                getReturnType(token, method, constructor) +
                getParameters(method, constructor) +
                getExceptions(method, constructor) +
                " {" +
                System.lineSeparator() +
                "       " +
                getBody(method, constructor) +
                ";" +
                "   " +
                System.lineSeparator() +
                "}" +
                System.lineSeparator();
    }

    /**
     * Get all methods and constructors of class
     *
     * @param token  type token to create implementation for
     * @param output stream {@link Writer}
     * @param mode   constructor or method
     * @throws IOException     if I/O error occurs
     * @throws ImplerException If no non-private constructors
     */
    private void getClassBody(Class<?> token, Writer output, String mode) throws IOException, ImplerException {
        if (mode.equals("constructor")) {
            ArrayList<Constructor> constructors = Arrays.stream(token.getDeclaredConstructors())
                    .filter(constructor -> !Modifier.isPrivate(constructor.getModifiers()))
                    .collect(Collectors.toCollection(ArrayList::new));
            if (constructors.isEmpty()) {
                throw new ImplerException("There are no non-private constructors in class");
            }
            for (Constructor constructor : constructors) {
                writeInUnicode(output, getStringMethod(token, null, constructor));
            }
        } else {
            Stream<Method> stream = Arrays.stream(token.getMethods());
            while (token != null) {
                stream = Stream.concat(stream, Arrays.stream(token.getDeclaredMethods()));
                token = token.getSuperclass();
            }
            HashSet<HashMethod> methods = stream.filter(method -> Modifier.isAbstract(method.getModifiers()))
                    .map(HashMethod::new).collect(Collectors.toCollection(HashSet::new));
            for (HashMethod method : methods) {
                writeInUnicode(output, getStringMethod(null, method.getMethod(), null));
            }
        }
    }

    /**
     * Constructor for {@link Implementor}
     */
    public Implementor() {}

    /**
     * Check input data on null
     *
     * @param arg0 {@link String} class token
     * @param arg1 {@link String} path
     * @throws ClassNotFoundException if cannot find class
     * @throws InvalidPathException   if path is invalid
     */
    private static void check(String arg0, String arg1) throws ClassNotFoundException, InvalidPathException {
        try {
            Class<?> clazz = Class.forName(arg0);
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Cannot find class.", e);
        }
        try {
            Path path = Paths.get(arg1);
        } catch (InvalidPathException e) {
            throw new InvalidPathException("Invalid path.", e.getMessage());
        }
    }

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
        if (args == null || args.length < 2 || args.length > 3) {
            throw new ImplerException("Use <type_token> <dir_path>");
        }
        JarImpler implementor = new Implementor();
        if (args.length == 2) {
            check(args[0], args[1]);
            implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
        } else {
            throw new ImplerException("Use <type_token> <dir_path>");
        }
    }
}
