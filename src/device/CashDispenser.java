package device;

public class CashDispenser {
    public void dispense(double amount) {
        System.out.println("ATM吐出现金: " + String.format("%.2f", amount) + " 元");
    }
}
