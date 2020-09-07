package ru.ifmo.rain.shaldin.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it is not already exists.
     *
     * @param id account id
     * @return created or existing account.
     */
    AccountInterface createAccount(String id) throws RemoteException;

    /**
     * Returns account by identifier.
     *
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exists.
     */
    AccountInterface getAccount(String id) throws RemoteException;

    /**
     * Return new {@link PersonInterface} by name, surname and passport
     *
     * @param name     name of {@link PersonInterface}
     * @param surname  surname of {@link PersonInterface}
     * @param passport passport of {@link PersonInterface}
     * @return new {@link PersonInterface} by name, surname and passport
     * @throws RemoteException if error occurred
     */
    PersonInterface createPerson(String name, String surname, int passport) throws RemoteException;

    /**
     * Return {@link LocalPerson} by passport
     *
     * @param passport passport of {@link PersonInterface}
     * @return {@link LocalPerson} by passport
     * @throws RemoteException if error occurred
     */
    PersonInterface getLocalPerson(int passport) throws RemoteException;

    /**
     * Return {@link RemotePerson} by passport
     *
     * @param passport passport of {@link PersonInterface}
     * @return {@link RemotePerson} by passport
     * @throws RemoteException if error occurred
     */
    PersonInterface getRemotePerson(int passport) throws RemoteException;
}
