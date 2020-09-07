package ru.ifmo.rain.shaldin.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface PersonInterface extends Remote {
    /** Returns name of person. */
    String getName() throws RemoteException;

    /** Returns surname of person. */
    String getSurname() throws RemoteException;

    /** Returns passport number of person. */
    int getPassport() throws RemoteException;

    /** Returns account subId's of person. */
    Set<String> getAccounts() throws RemoteException;

    /** Add account subId of person. */
    void addAccount(String subId) throws RemoteException;

    /** Return added accounts. */
    Set<String> getAdded() throws RemoteException;
}
