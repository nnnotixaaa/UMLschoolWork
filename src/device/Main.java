package device;

import java.util.Scanner;
import system.BankSystem;
import system.InMemoryBankSystem;

public class Main {
    public static void main(String[] args) {
        BankSystem bankSystem = new InMemoryBankSystem();
        ATM atm = new ATM(bankSystem, new CashDispenser(), new ReceiptPrinter(), new Display());
        Scanner scanner = new Scanner(System.in);
        atm.startWithdrawalFlow(scanner);
        scanner.close();
    }
}
