package ru.ifmo.rain.shaldin.bank;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;

public class Server {
    private final static int PORT = 8888;
    public static void main(final String... args) {
        final Bank bank = new RemoteBank(PORT);;
        try {
            UnicastRemoteObject.exportObject(bank, PORT);
            Registry registry = LocateRegistry.createRegistry(PORT);
            registry.rebind("bank", bank);
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Server started");
    }
}
