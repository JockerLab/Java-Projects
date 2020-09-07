package ru.ifmo.rain.shaldin.bank;

import java.io.Serializable;

public class LocalAccount extends Account implements Serializable {
    private static final long serialVersionUID = 1L;

    public LocalAccount(String id, int amount) {
        super(id, amount);
    }

    public LocalAccount(String id) {
        super(id);
    }
}
