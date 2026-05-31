package model;

import system.CryptoUtils;

public class BankAccount {
    private final String accountId;
    private final String cardNumber;
    private final String pinHash;
    private final String pinSalt;
    private double balance;
    private boolean locked;

    public BankAccount(String accountId, String cardNumber, String pinHash, String pinSalt, double balance, boolean locked) {
        this.accountId = accountId;
        this.cardNumber = cardNumber;
        this.pinHash = pinHash;
        this.pinSalt = pinSalt;
        this.balance = balance;
        this.locked = locked;
    }

    public static BankAccount createWithPlainPin(String accountId, String cardNumber, String plainPin, double balance) {
        String salt = CryptoUtils.newSaltBase64();
        String hash = CryptoUtils.hashPinBase64(plainPin, salt);
        return new BankAccount(accountId, cardNumber, hash, salt, balance, false);
    }

    public String getAccountId() {
        return accountId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getPinHash() {
        return pinHash;
    }

    public String getPinSalt() {
        return pinSalt;
    }

    public boolean isLocked() {
        return locked;
    }

    public void lock() {
        this.locked = true;
    }

    public boolean verifyPin(String inputPin) {
        return CryptoUtils.verifyPin(inputPin, pinSalt, pinHash);
    }

    public double getBalance() {
        return balance;
    }

    public void debit(double amount) {
        this.balance -= amount;
    }
}
