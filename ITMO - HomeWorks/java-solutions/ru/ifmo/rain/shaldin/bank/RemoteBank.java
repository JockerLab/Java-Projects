package ru.ifmo.rain.shaldin.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static ru.ifmo.rain.shaldin.bank.BankUtils.printError;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, AccountInterface> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, PersonInterface> persons = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    private String splitSubId(final String id, int pos) {
        return (id.split(":"))[pos];
    }

    @Override
    public AccountInterface createAccount(final String id) throws RemoteException {
        System.out.println("Creating account " + id);
        final AccountInterface account = new RemoteAccount(id);
        int passport = 0;
        try {
            passport = Integer.parseInt(splitSubId(id, 0));
        } catch (NumberFormatException ignore) {
            printError("Invalid person passport number.");
            return null;
        }
        PersonInterface person = persons.get(passport);
        if (person == null) {
            printError("Invalid person passport number.");
            return null;
        }
        person.addAccount(splitSubId(id, 1));
        if (accounts.putIfAbsent(id, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            return getAccount(id);
        }
    }

    @Override
    public AccountInterface getAccount(final String id) throws RemoteException {
        System.out.println("Retrieving account " + id);
        if (accounts.get(id) == null) {
            PersonInterface person;
            try {
                person = persons.get(Integer.parseInt(splitSubId(id, 0)));
                if (person == null) {
                    return null;
                }
            } catch (NumberFormatException e) {
                printError("Invalid passport number.");
                return null;
            }
            Set<String> added = person.getAdded();
            for (String subId : added) {
                int personPassport = person.getPassport();
                String resultId = personPassport + ":" + subId;
                accounts.put(resultId, createAccount(resultId));
            }
        }
        return accounts.get(id);
    }

    @Override
    public PersonInterface createPerson(final String name, final String surname, final int passport) throws RemoteException {
        System.out.println("Creating person " + name + " " + surname + " " + passport);
        if (passport < 0) {
            return null;
        }
        final PersonInterface person = new RemotePerson(name, surname, passport);
        if (persons.putIfAbsent(passport, person) == null) {
            UnicastRemoteObject.exportObject(person, port);
            return person;
        } else {
            return persons.get(passport);
        }
    }

    @Override
    public PersonInterface getLocalPerson(int passport) throws RemoteException {
        System.out.println("Retrieving person " + passport);
        PersonInterface person = persons.get(passport);
        if (person == null) {
            printError("Invalid Local Person passport number.");
            return null;
        }
        return new LocalPerson(person.getName(), person.getSurname(), person.getPassport(), new TreeSet<>(person.getAccounts()));
    }

    @Override
    public PersonInterface getRemotePerson(int passport) throws RemoteException {
        return persons.get(passport);
    }
}
