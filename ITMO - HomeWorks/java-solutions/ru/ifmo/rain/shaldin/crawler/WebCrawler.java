package ru.ifmo.rain.shaldin.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Crawls web sites.
 */
public class WebCrawler implements Crawler {
    private ExecutorService downloaders;
    private ExecutorService extractors;
    private Downloader downloader;
    private ConcurrentHashMap<String, IOException> errorPages;
    private Set<String> used;

    /**
     * Constructor to {@link WebCrawler}
     *
     * @param downloader  {@link Downloader}
     * @param downloaders maximum amount of downloader threads
     * @param extractors  maximum amount of extractors threads
     * @param perHost     maximum amount of threads per host
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        errorPages = new ConcurrentHashMap<>();
        used = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    /**
     * Constructor to {@link WebCrawler}
     *
     * @param downloaders maximum amount of downloader threads
     * @param extractors  maximum amount of extractors threads
     * @param perHost     maximum amount of threads per host
     */
    public WebCrawler(int downloaders, int extractors, int perHost) throws IOException {
        this(new CachingDownloader(), downloaders, extractors, perHost);
    }

    /**
     * Constructor to {@link WebCrawler}
     *
     * @param downloaders maximum amount of downloader threads
     * @param extractors  maximum amount of extractors threads
     */
    public WebCrawler(int downloaders, int extractors) throws IOException {
        this(downloaders, extractors, 1);
    }

    /**
     * Constructor to {@link WebCrawler}
     *
     * @param downloaders maximum amount of downloader threads
     */
    public WebCrawler(int downloaders) throws IOException {
        this(downloaders, 1);
    }

    /**
     * Constructor to {@link WebCrawler}
     */
    public WebCrawler() throws IOException {
        this(1);
    }

    private Future<Document> futurePage(String url, Set<String> set) {
        return downloaders.submit(() -> {
            Document page = null;
            try {
                page = downloader.download(url);
                set.add(url);
            } catch (IOException e) {
                errorPages.putIfAbsent(url, e);
            }
            return page;
        });
    }

    private List<String> futureLists(String url, Future<Document> futurePage) {
        List<String> links = new ArrayList<>();
        try {
            Document page = futurePage.get();
            try {
                links = page.extractLinks();
            } catch (IOException ignore) {
            }
        } catch (InterruptedException | ExecutionException ignore) {
        }
        return links;
    }

    public class Pair {
        private final String url;
        private final Future<Document> document;

        public Pair(String url, Future<Document> document) {
            this.url = url;
            this.document = document;
        }

        public String getKey() {
            return url;
        }

        public Future<Document> getValue() {
            return document;
        }
    }

    /**
     * Downloads web site up to specified depth.
     *
     * @param startUrl start <a href="http://tools.ietf.org/html/rfc3986">URL</a>.
     * @param depth    download depth.
     * @return download result.
     */
    @Override
    public Result download(String startUrl, int depth) {
        Set<String> set = Collections.newSetFromMap(new ConcurrentHashMap<>());
        Queue<String> q = new ArrayDeque<>();
        q.add(startUrl);
        used.add(startUrl);

        while (!q.isEmpty()) {
            final int d = depth;
            List<Pair> futures = new ArrayList<>();
            for (String url : q) {
                futures.add(new Pair(url, futurePage(url, set)));
            }
            q.clear();
            if (d == 1) {
                for (Pair pair : futures) {
                    try {
                        pair.getValue().get();
                    } catch (InterruptedException | ExecutionException ignore) {
                    }
                }
                continue;
            }
            try {
                List<Callable<List<String>>> callableList = new ArrayList<>();
                for (Pair pair : futures) {
                    Callable<List<String>> callable = () -> futureLists(pair.getKey(), pair.getValue());
                    callableList.add(callable);
                }
                List<Future<List<String>>> extracted = extractors.invokeAll(callableList);
                for (Future<List<String>> elem : extracted) {
                    List<String> links = new ArrayList<>();
                    try {
                        links = elem.get();
                    } catch (InterruptedException | ExecutionException ignore) {
                    }
                    for (String link : links) {
                        if (!used.contains(link)) {
                            q.add(link);
                            used.add(link);
                        }
                    }
                }
            } catch (InterruptedException ignore) {
            }
            depth--;
        }
        return new Result(new ArrayList<>(set), errorPages);
    }

    /**
     * Closes this web-crawler, relinquishing any allocated resources.
     */
    @Override
    public void close() {
        downloaders.shutdown();
        extractors.shutdown();
    }

    /**
     * Create new {@link WebCrawler} instance and download pages. First argument is necessary, other is optional.
     * @param args 1 - url
     *             2 - depth of search
     *             3 - maximum count of simultaneous downloads
     *             4 - maximum count of pages, which are simultaneously extracted
     *             5 - maximum count of simultaneously downloading per host
     */
    public static void main(String[] args) {
        if (args == null) {
            System.out.println("Arguments cannot be null");
            return;
        }
        WebCrawler crawler;
        try {
            switch (args.length) {
                case 3:
                    crawler = new WebCrawler(Integer.parseInt(args[2]));
                    break;
                case 4:
                    crawler = new WebCrawler(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                    break;
                case 5:
                    crawler = new WebCrawler(Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
                    break;
                default:
                    crawler = new WebCrawler();
                    if (args.length > 5) {
                        System.out.println("Invalid number of arguments");
                    }
                    break;
            }
            if (args.length == 1) {
                crawler.download(args[0], 1);
            } else {
                crawler.download(args[0], Integer.parseInt(args[1]));
            }
        } catch (IOException e) {
            System.out.println("Cannot create instance of WebCrawler" + e.getMessage());
        }
    }
}
