package ru.ifmo.rain.shaldin.student;

import info.kgeorgiy.java.advanced.student.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentGroupQuery {
    private List<Group> getGroup(Collection<Student> students, Function<Collection<Student>, List<Student>> function) {
        return function.apply(students)
                .stream()
                .collect(Collectors.groupingBy(Student::getGroup, TreeMap::new, Collectors.toList()))
                .entrySet()
                .stream()
                .map(group -> new Group(group.getKey(), group.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroup(students, this::sortStudentsByName);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroup(students, this::sortStudentsById);
    }

    private String getLargestGroup(Collection<Student> students, Comparator<Group> comparator) {
        return getGroupsByName(students)
                .stream()
                .max(comparator
                        .thenComparing(Group::getName, Collections.reverseOrder(String::compareTo)))
                .map(Group::getName)
                .orElse("");
    }

    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getLargestGroup(students, Comparator.comparing(group -> group.getStudents().size()));
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroup(students, Comparator.comparing((Group group) -> getFirstNames(group.getStudents())
                .stream()
                .distinct()
                .count()));
    }

    private <C extends Collection<String>> C getCollection(List<Student> students, Function<Student, String> function, Supplier<C> collection) {
        return students
                .stream()
                .map(function)
                .collect(Collectors.toCollection(collection));
    }

    private List<String> getList(List<Student> students, Function<Student, String> function) {
        return getCollection(students, function, ArrayList::new);
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getList(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return getList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getList(students, (student -> student.getFirstName() + " " + student.getLastName()));
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return getCollection(students, Student::getFirstName, TreeSet::new);
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students
                .stream()
                .min(Student::compareTo)
                .map(Student::getFirstName)
                .orElse("");
    }

    private List<Student> sortStudents(Collection<Student> students, Comparator<Student> comparator) {
        return students
                .stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudents(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudents(students, Comparator
                .comparing(Student::getLastName)
                .thenComparing(Student::getFirstName)
                .thenComparing(Student::getId));
    }

    private Stream<Student> findStudentsCollection(Collection<Student> students, Predicate<Student> predicate) {
        return sortStudentsByName(students)
                .stream()
                .filter(predicate);
    }

    private List<Student> findStudentsList(Collection<Student> students, Predicate<Student> predicate) {
        return findStudentsCollection(students, predicate)
                .collect(Collectors.toList());
    }

    private Predicate<Student> getPredicate(Collection<Student> students, Function<Student, String> function, String name) {
        return student -> (function.apply(student).equals(name));
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentsList(students, getPredicate(students, Student::getFirstName, name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentsList(students, getPredicate(students, Student::getLastName, name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return findStudentsList(students, getPredicate(students, Student::getGroup, group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return findStudentsCollection(students, getPredicate(students, Student::getGroup, group))
                .collect(Collectors.toMap(Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(String::compareTo)));
    }
}

