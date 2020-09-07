package ru.ifmo.rain.shaldin.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ru.ifmo.rain.shaldin.hello.HelloUtils.*;

/**
 * Server class, which implements {@link HelloServer}
 */
public class HelloUDPServer implements HelloServer {
    private DatagramSocket socket;
    private ExecutorService threadPool;

    private void sendResponse() {
        while (!Thread.interrupted()) {
            try {
                DatagramPacket packetReceive = makeRecievePacket(socket);
                socket.receive(packetReceive);
                String receiveText = "Hello, " + recieveText(packetReceive);
                packetReceive.setData(receiveText.getBytes(StandardCharsets.UTF_8), packetReceive.getOffset(), receiveText.getBytes(StandardCharsets.UTF_8).length);
                socket.send(packetReceive);
            } catch (SocketException e) {
                System.out.println("Cannot receive buffer size. " + e.getMessage());
                break;
            } catch (IOException e) {
                printError("Cannot receive packet. " + e.getMessage());
            }
        }
    }

    /**
     * Starts a new Hello server.
     *
     * @param port    server port.
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
        threadClose(threadPool);
        socket.close();
    }

    /**
     * Create new {@link HelloUDPServer} instance.
     *
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
