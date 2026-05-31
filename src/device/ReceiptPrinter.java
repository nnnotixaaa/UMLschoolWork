package device;

import model.WithdrawalResult;

public class ReceiptPrinter {
    public void print(WithdrawalResult result) {
        System.out.println("===== 交易凭条 =====");
        System.out.println("交易类型: 取款");
        System.out.println("取款金额: " + String.format("%.2f", result.getAmount()) + " 元");
        System.out.println("手续费: " + String.format("%.2f", result.getFee()) + " 元");
        System.out.println("账户余额: " + String.format("%.2f", result.getRemainingBalance()) + " 元");
        System.out.println("结果: " + result.getMessage());
        System.out.println("====================");
    }
}
