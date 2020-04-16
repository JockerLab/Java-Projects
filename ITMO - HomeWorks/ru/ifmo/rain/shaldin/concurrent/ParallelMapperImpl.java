package ru.ifmo.rain.shaldin.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {
    private final Queue<Runnable> queue;
    private final List<Thread> threads;

    /**
     * Constructor for {@link ParallelMapperImpl}
     *
     * @param count amount of threads
     */
    public ParallelMapperImpl(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Incorrect amount of threads.");
        }
        threads = new ArrayList<>();
        queue = new ArrayDeque<>();
        for (int i = 0; i < count; i++) {
            threads.add(new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        Runnable task;
                        synchronized (queue) {
                            while (queue.isEmpty()) {
                                queue.wait();
                            }
                            task = queue.poll();
                        }
                        task.run();
                    }
                } catch (InterruptedException ignore) {
                } finally {
                    Thread.currentThread().interrupt();
                }
            }));
            threads.get(i).start();
        }
    }

    /**
     * Maps function {@code f} over specified {@code args}.
     * Mapping for each element performs in parallel.
     *
     * @throws InterruptedException if calling thread was interrupted
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        if (args == null || args.size() == 0) {
            throw new IllegalArgumentException("Empty list of arguments.");
        }
        int n = args.size();
        List<R> result = new ArrayList<>(Collections.nCopies(n, null));
        List<Integer> updated = new ArrayList<>(Collections.nCopies(1, 0));
        for (int i = 0; i < n; i++) {
            final int pos = i;
            synchronized (queue) {
                queue.add(() -> {
                    result.set(pos, f.apply(args.get(pos)));
                    synchronized (updated) {
                        updated.set(0, updated.get(0) + 1);
                        updated.notify();
                    }
                });
                queue.notify();
            }
        }

        while (true) {
            synchronized (updated) {
                if (updated.get(0) == n) {
                    break;
                }
                updated.wait();
            }
        }

        return result;
    }

    /** Stops all threads. All unfinished mappings leave in undefined state. */
    @Override
    public void close() {
        for (int i = 0; i < threads.size(); i++) {
            threads.get(i).interrupt();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignore) {
            }
        }
    }
}
