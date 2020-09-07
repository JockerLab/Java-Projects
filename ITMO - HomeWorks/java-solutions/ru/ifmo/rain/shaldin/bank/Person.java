package ru.ifmo.rain.shaldin.bank;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Person implements PersonInterface {
    private final int passport;
    private final String name, surname;
    private final Set<String> accounts, added;

    public Person(String name, String surname, int passport, Set<String> accounts) {
        this.name = name;
        this.surname = surname;
        this.passport = passport;
        this.accounts = accounts;
        this.added = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public Person(String name, String surname, int passport) {
        this(name, surname, passport,  Collections.newSetFromMap(new ConcurrentHashMap<>()));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public int getPassport() {
        return passport;
    }

    @Override
    public Set<String> getAccounts() {
        return accounts;
    }

    @Override
    public void addAccount(String subId) {
        accounts.add(subId);
        added.add(subId);
    }

    @Override
    public Set<String> getAdded() {
        Set<String> newSet = new TreeSet<String>(added);
        added.clear();
        return newSet;
    }
}
