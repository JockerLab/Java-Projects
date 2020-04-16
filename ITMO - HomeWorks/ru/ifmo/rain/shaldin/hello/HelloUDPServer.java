package ru.ifmo.rain.shaldin.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;
import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server class, which implements {@link HelloServer}
 */
public class HelloUDPServer implements HelloServer {
    private DatagramSocket socket;
    private ExecutorService threadPool;

    private void printError(String message) {
        System.out.println(message);
    }

    private void sendResponse() {
        while (!Thread.interrupted()) {
            final int BUFFER_SIZE;
            try {
                BUFFER_SIZE = socket.getReceiveBufferSize();
            } catch (SocketException e) {
                System.out.println("Cannot receive buffer size. " + e.getMessage());
                break;
            }
            byte[] bufferResponse = new byte[BUFFER_SIZE];
            try {
                DatagramPacket packetReceive = new DatagramPacket(bufferResponse, BUFFER_SIZE);
                socket.receive(packetReceive);
                String receiveText = "Hello, " + new String(packetReceive.getData(), packetReceive.getOffset(), packetReceive.getLength(), StandardCharsets.UTF_8);
                packetReceive.setData(receiveText.getBytes(), packetReceive.getOffset(), receiveText.getBytes().length);
                socket.send(packetReceive);
            } catch (IOException e) {
                printError("Cannot receive packet. " + e.getMessage());
            }
        }
    }

    /**
     * Starts a new Hello server.
     *
     * @param port server port.
     * @param threads number of working threads.
     */
    @Override
    public void start(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            printError("Cannot create socket for server. " + e.getMessage());
            return;
        }
        threadPool = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            threadPool.submit(this::sendResponse);
        }
    }

    /**
     * Stops server and deallocates all resources.
     */
    @Override
    public void close() {
        threadPool.shutdown();
        socket.close();
    }

    /**
     * Create new {@link HelloUDPServer} instance.
     * @param args 1 - number of port
     *             2 - count of threads
     */
    public static void main(String[] args) {
        if (args == null) {
            System.out.println("Arguments cannot be null");
            return;
        }
        if (args.length != 2) {
            System.out.println("Invalid number of arguments");
            return;
        }
        try {
            new HelloUDPServer().start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        } catch (NumberFormatException e) {
            System.out.println("Integer arguments expected");
        }
    }
}
