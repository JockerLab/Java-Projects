package ru.ifmo.rain.shaldin.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static ru.ifmo.rain.shaldin.bank.BankUtils.printError;

public class Client {
    public static void main(final String... args) throws RemoteException {
        try {
            if (args == null || args.length != 5) {
                printError("Wrong number of arguments. Use <name> <surname> <passport> <subId> <change>.");
                return;
            }

            for (var arg : args) {
                if (arg == null) {
                    printError("Arguments cannot be null.");
                    return;
                }
            }

            String name, surname, subId;
            int passport, change;

            name = args[0];
            surname = args[1];
            subId = args[3];

            try {
                passport = Integer.parseInt(args[2]);
                change = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                printError("Invalid number format.");
                return;
            }

            if (passport < 0) {
                printError("Passport number cannot be less zero");
                return;
            }

            final Bank bank;
            try {
                Registry registry = LocateRegistry.getRegistry(8888);
                bank = (Bank) registry.lookup("bank");
            } catch (final NotBoundException e) {
                System.out.println("Bank is not bound");
                return;
            }

            PersonInterface person = bank.getRemotePerson(passport);
            if (person == null) {
                System.out.println("Creating new person " + passport);
                person = bank.createPerson(name, surname, passport);
            } else {
                if (!name.equals(person.getName()) || !surname.equals(person.getSurname())) {
                    printError("Wrong name or surname.");
                    return;
                }
            }

            final String accountId = passport + ":" + subId;

            AccountInterface account = bank.getAccount(accountId);
            if (account == null) {
                System.out.println("Creating account");
                account = bank.createAccount(accountId);
            } else {
                System.out.println("Account already exists");
            }
            System.out.println("Account id: " + account.getId());
            System.out.println("Money: " + account.getAmount());
            System.out.println("Adding money");
            if (account.getAmount() < -change) {
                printError("Final result cannot be less zero.");
                return;
            }
            account.setAmount(account.getAmount() + change);
            System.out.println("Money: " + account.getAmount());
        } catch (RemoteException e) {
            printError("Remote error occurred");
        }
    }
}
