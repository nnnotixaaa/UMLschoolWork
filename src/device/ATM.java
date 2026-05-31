package device;

import java.util.Scanner;
import model.WithdrawalResult;
import system.BankSystem;

public class ATM {
    private static final int MAX_PIN_TRIES = 3;

    private final BankSystem bankSystem;
    private final CashDispenser cashDispenser;
    private final ReceiptPrinter receiptPrinter;
    private final Display display;

    public ATM(BankSystem bankSystem, CashDispenser cashDispenser, ReceiptPrinter receiptPrinter, Display display) {
        this.bankSystem = bankSystem;
        this.cashDispenser = cashDispenser;
        this.receiptPrinter = receiptPrinter;
        this.display = display;
    }

    public void startWithdrawalFlow(Scanner scanner) {
        String cardNumber;
        while (true) {
            cardNumber = display.readCardNumber(scanner);

            if (!bankSystem.cardExists(cardNumber)) {
                display.showCardNotFound();
                continue;
            }

            if (bankSystem.isCardLocked(cardNumber)) {
                display.showCardLocked();
                return;
            }

            boolean backToCardPage = false;
            boolean authPassed = false;
            for (int i = 1; i <= MAX_PIN_TRIES; i++) {
                String pin = display.readPin(scanner);

                if ("r".equalsIgnoreCase(pin)) {
                    backToCardPage = true;
                    break;
                }

                if (bankSystem.authenticateCard(cardNumber, pin)) {
                    authPassed = true;
                    break;
                }

                int remaining = MAX_PIN_TRIES - i;
                if (remaining > 0) {
                    display.showPinRetry(remaining);
                }
            }

            if (backToCardPage) {
                display.showBackToCardPage();
                continue;
            }

            if (!authPassed) {
                bankSystem.lockCard(cardNumber);
                display.showCardCaptured();
                return;
            }
            break;
        }

        String menu = display.readMenu(scanner);
        if (!"1".equals(menu)) {
            display.showOnlyWithdrawalSupported();
            return;
        }

        String amountText = display.readAmountText(scanner);
        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException ex) {
            display.showInvalidAmountFormat();
            return;
        }

        WithdrawalResult result = bankSystem.withdraw(cardNumber, amount);
        if (!result.isSuccess()) {
            display.showWithdrawFailure(result.getMessage());
            return;
        }

        cashDispenser.dispense(result.getAmount());
        String printChoice = display.readReceiptChoice(scanner);
        if ("Y".equalsIgnoreCase(printChoice)) {
            receiptPrinter.print(result);
        }
        display.showFarewell();
    }
}
