package ru.ifmo.rain.shaldin.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {
    private final List<E> array;
    private final Comparator<? super E> comparator;

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Collection<? extends E> collection) {
        this(collection, null);
    }

    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        TreeSet<E> set = new TreeSet<>(comparator);
        set.addAll(collection);
        this.array = new ArrayList<>(set);
        this.comparator = comparator;
    }

    private ArraySet(List<E> collection, Comparator<? super E> comparator) {
        this.array = collection;
        this.comparator = comparator;
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(array).iterator();
    }

    @Override
    public int size() {
        return array.size();
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        int to = lowerBound(toElement);
        return new ArraySet<E>(array.subList(0, to), comparator);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        int from = lowerBound(fromElement);
        return new ArraySet<E>(array.subList(from, size()), comparator);
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        if (!(fromElement instanceof Comparable) || !(toElement instanceof Comparable)) {
            throw new IllegalArgumentException("Invalid comparator.");
        }
        if (comparator != null) {
            if (comparator.compare(fromElement, toElement) > 0) {
                throw new IllegalArgumentException("Left argument must be less than right.");
            }
        } else {
            if (((Comparable) fromElement).compareTo(toElement) > 0) {
                throw new IllegalArgumentException("Left argument must be less than right.");
            }
        }

        return tailSet(fromElement).headSet(toElement);
    }

    private int lowerBound(E element) {
        int from = Collections.binarySearch(array, element, comparator);
        if (from < 0) {
            from = ~from;
        }
        return from;
    }

    private void checkSize() throws NoSuchElementException {
        if (size() == 0) {
            throw new NoSuchElementException("Collection is empty.");
        }
    }

    @Override
    public E first() throws NoSuchElementException {
        checkSize();
        return array.get(0);
    }

    @Override
    public E last() throws NoSuchElementException {
        checkSize();
        return array.get(size() - 1);
    }

    @Override
    public boolean contains(Object o) {
        int to = Collections.binarySearch(array, (E)o, comparator);
        return to >= 0;
    }
}
