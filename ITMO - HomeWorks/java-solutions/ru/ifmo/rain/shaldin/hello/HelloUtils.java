package ru.ifmo.rain.shaldin.hello;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class HelloUtils {
    protected static void printError(String message) {
        System.err.println(message);
    }

    protected static DatagramPacket makeRequestPacket(String request, SocketAddress address) {
        byte[] bufferRequest = request.getBytes(StandardCharsets.UTF_8);
        return new DatagramPacket(bufferRequest, bufferRequest.length, address);
    }

    protected static byte[] makeBuffer(DatagramSocket socket) throws SocketException {
        final int BUFFER_SIZE = socket.getReceiveBufferSize();
        return new byte[BUFFER_SIZE];
    }

    protected static DatagramPacket makeRecievePacket(DatagramSocket socket) throws SocketException {
        byte[] bufferReceive = makeBuffer(socket);
        return new DatagramPacket(bufferReceive, bufferReceive.length);
    }

    protected static String recieveText(DatagramPacket packetReceive) {
        return new String(packetReceive.getData(), packetReceive.getOffset(), packetReceive.getLength(), StandardCharsets.UTF_8);
    }

    protected static void threadClose(ExecutorService threadPool) {
        threadPool.shutdownNow();
        try {
            threadPool.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            printError("Interrupted while waiting. " + e.getMessage());
        }
    }
}
