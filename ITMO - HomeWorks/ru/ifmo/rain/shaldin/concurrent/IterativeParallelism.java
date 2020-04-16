package ru.ifmo.rain.shaldin.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Iterative parallelism support.
 *
 * @author Vsevolod Shaldin
 */
public class IterativeParallelism implements ScalarIP {
    private final ParallelMapper mapper;

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    public IterativeParallelism() {
        mapper = null;
    }

    private <T, R> R common(int threads,
                            List<? extends T> values,
                            Function<Stream<? extends T>, R> func,
                            Function<Stream<? extends R>, R> returnFunc) throws InterruptedException {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("List is empty");
        }
        if (threads <= 0) {
            throw new IllegalArgumentException("Negative amount of threads or zero");
        }
        threads = Math.min(threads, values.size());
        int threadsCount = values.size() / threads;
        int rest = values.size() - threadsCount * threads;
        int l = 0;
        List<Stream<? extends T>> list = new ArrayList<>(Collections.nCopies(threads, null));
        final List<R> results;
        for (int i = 0; i < threads; i++) {
            int r = l + threadsCount + (rest > 0 ? 1 : 0);
            list.set(i, values.subList(l, r).stream());
            rest = Math.max(rest - 1, 0);
            l = r;
        }
        if (mapper != null) {
            results = mapper.map(func, list);
        } else {
            final ArrayList<Thread> array = new ArrayList<>(Collections.nCopies(threads, null));
            results = new ArrayList<>(Collections.nCopies(threads, null));
            for (int i = 0; i < threads; i++) {
                final int pos = i;
                Thread t = new Thread(() -> results.set(pos, func.apply(list.get(pos))));
                array.set(i, t);
                array.get(i).start();
            }
            boolean anyException = false;
            for (int i = 0; i < threads; i++) {
                try {
                    array.get(i).join();
                } catch (InterruptedException e) {
                    anyException = true;
                }
            }
            if (anyException) {
                for (int i = 0; i < threads; i++) {
                    if (!array.get(i).isInterrupted()) {
                        array.get(i).interrupt();
                    }
                }
                throw new InterruptedException("Not all threads finished correctly");
            }
        }
        return returnFunc.apply(results.stream());
    }

    /**
     * Returns maximum value.
     *
     * @param threads number or concurrent threads.
     * @param values values to get maximum of.
     * @param comparator value comparator.
     * @param <T> value type.
     *
     * @return maximum of given values
     *
     * @throws InterruptedException if executing thread was interrupted.
     * @throws java.util.NoSuchElementException if not values are given.
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        Function<Stream<? extends T>, T> func = stream -> stream.max(comparator).orElse(null);
        if (threads == 10 && values.size() == 1) {
            int kek = 0;
        }
        return common(threads, values, func, func);
    }

    /**
     * Returns minimum value.
     *
     * @param threads number or concurrent threads.
     * @param values values to get minimum of.
     * @param comparator value comparator.
     * @param <T> value type.
     *
     * @return minimum of given values
     *
     * @throws InterruptedException if executing thread was interrupted.
     * @throws java.util.NoSuchElementException if not values are given.
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    /**
     * Returns whether all values satisfies predicate.
     *
     * @param threads number or concurrent threads.
     * @param values values to test.
     * @param predicate test predicate.
     * @param <T> value type.
     *
     * @return whether all values satisfies predicate or {@code true}, if no values are given.
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return common(threads, values, stream -> stream.allMatch(predicate), stream -> stream.allMatch(Boolean::booleanValue));
    }

    /**
     * Returns whether any of values satisfies predicate.
     *
     * @param threads number or concurrent threads.
     * @param values values to test.
     * @param predicate test predicate.
     * @param <T> value type.
     *
     * @return whether any value satisfies predicate or {@code false}, if no values are given.
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return common(threads, values, stream -> stream.anyMatch(predicate), stream -> stream.anyMatch(Boolean::booleanValue));
    }
}
