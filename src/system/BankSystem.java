package system;

import model.WithdrawalResult;

public interface BankSystem {
    boolean cardExists(String cardNumber);

    boolean authenticateCard(String cardNumber, String pin);

    boolean isCardLocked(String cardNumber);

    void lockCard(String cardNumber);

    WithdrawalResult withdraw(String cardNumber, double amount);
}
