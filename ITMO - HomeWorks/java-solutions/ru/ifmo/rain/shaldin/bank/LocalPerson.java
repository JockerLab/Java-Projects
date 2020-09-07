package ru.ifmo.rain.shaldin.bank;

import java.io.Serializable;
import java.util.*;

public class LocalPerson extends Person implements Serializable {
    private static final long serialVersionUID = 1L;

    public LocalPerson(String name, String surname, int passport, Set<String> accounts) {
        super(name, surname, passport, accounts);
    }
}
