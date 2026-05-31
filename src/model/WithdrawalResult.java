package model;

public class WithdrawalResult {
    private final boolean success;
    private final String message;
    private final double amount;
    private final double fee;
    private final double remainingBalance;

    private WithdrawalResult(boolean success, String message, double amount, double fee, double remainingBalance) {
        this.success = success;
        this.message = message;
        this.amount = amount;
        this.fee = fee;
        this.remainingBalance = remainingBalance;
    }

    public static WithdrawalResult ok(double amount, double fee, double remainingBalance) {
        return new WithdrawalResult(true, "取款成功", amount, fee, remainingBalance);
    }

    public static WithdrawalResult fail(String message) {
        return new WithdrawalResult(false, message, 0, 0, 0);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public double getAmount() {
        return amount;
    }

    public double getFee() {
        return fee;
    }

    public double getRemainingBalance() {
        return remainingBalance;
    }
}
