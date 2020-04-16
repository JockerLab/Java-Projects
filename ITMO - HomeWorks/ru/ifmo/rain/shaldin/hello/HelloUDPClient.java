package ru.ifmo.rain.shaldin.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Client class, which implements {@link HelloClient}
 */
public class HelloUDPClient implements HelloClient {
    private static final int resendCount = 1_000_000;

    private void printError(String message) {
        System.out.println(message);
    }

    private void sendRequests(SocketAddress address, int requests, int threadNumber, String prefix) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(100);
            for (int i = 0; i < requests; i++) {
                String request = prefix + threadNumber + "_" + i;
                byte[] bufferRequest = request.getBytes();
                final int BUFFER_SIZE = socket.getReceiveBufferSize();
                byte[] bufferReceive = new byte[BUFFER_SIZE];
                for (int j = 0; j < resendCount; j++) {
                    DatagramPacket packetRequest = new DatagramPacket(bufferRequest, bufferRequest.length, address);
                    DatagramPacket packetReceive = new DatagramPacket(bufferReceive, BUFFER_SIZE);
                    try {
                        socket.send(packetRequest);
                        socket.receive(packetReceive);
                        String receiveText = new String(packetReceive.getData(), packetReceive.getOffset(), packetReceive.getLength(), StandardCharsets.UTF_8);
                        if (receiveText.contains(request)) {
                            System.out.println(request);
                            System.out.println(receiveText);
                            break;
                        }
                    } catch (IOException e) {
                        printError("Cannot receive packet. " + e.getMessage());
                    }
                }
            }
        } catch (SocketException e) {
            printError("Cannot create socket for " + threadNumber + "'th thread. " + e.getMessage());
        }
    }

    /**
     * Runs Hello client.
     * @param host server host
     * @param port server port
     * @param prefix request prefix
     * @param threads number of request threads
     * @param requests number of requests per thread.
     */
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        ExecutorService threadPool = Executors.newFixedThreadPool(threads);
        InetAddress address;
        SocketAddress serverAddress;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            printError("Cannot get host address" + e.getMessage());
            return;
        }
        serverAddress = new InetSocketAddress(address, port);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            final int pos = i;
            futures.add(threadPool.submit(() -> sendRequests(serverAddress, requests, pos, prefix)));
        }
        for (int i = 0; i < threads; i++) {
            try {
                futures.get(i).get();
            } catch (InterruptedException | ExecutionException ignore) {
            }
        }
    }

    /**
     * Create new {@link HelloUDPClient} instance.
     * @param args 1 - name or ip-address of server computer
     *             2 - number of port
     *             3 - prefix of requests
     *             4 - count of threads
     *             5 - count of requests in each thread
     */
    public static void main(String[] args) {
        if (args == null) {
            System.out.println("Arguments cannot be null.");
            return;
        }
        try {
            new HelloUDPClient().run(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        } catch (NumberFormatException e) {
            System.out.println("Correct integer arguments expected: " + e.getMessage());
        }
    }
}
