package device;

import java.util.Scanner;

public class Display {
    public String readCardNumber(Scanner scanner) {
        System.out.println("欢迎使用ATM，请插入银行卡(输入卡号):");
        return scanner.nextLine().trim();
    }

    public void showCardNotFound() {
        System.out.println("卡号不存在，请重新输入。");
    }

    public void showCardLocked() {
        System.out.println("该卡已被锁定，请联系银行柜台。");
    }

    public String readPin(Scanner scanner) {
        System.out.println("请输入密码(输入r返回上一步):");
        return scanner.nextLine().trim();
    }

    public void showPinRetry(int remaining) {
        System.out.println("密码错误，请重试。剩余次数: " + remaining);
    }

    public void showBackToCardPage() {
        System.out.println("已返回卡号输入页面。");
    }

    public void showCardCaptured() {
        System.out.println("密码错误已达3次，系统吞卡。交易结束。");
    }

    public String readMenu(Scanner scanner) {
        System.out.println("认证成功。请选择功能: 1.取款");
        return scanner.nextLine().trim();
    }

    public void showOnlyWithdrawalSupported() {
        System.out.println("目前仅实现了取款功能。已退卡。");
    }

    public String readAmountText(Scanner scanner) {
        System.out.println("请输入取款金额:");
        return scanner.nextLine().trim();
    }

    public void showInvalidAmountFormat() {
        System.out.println("金额格式非法，交易结束。");
    }

    public void showWithdrawFailure(String message) {
        System.out.println("取款失败: " + message);
        System.out.println("已退卡。");
    }

    public String readReceiptChoice(Scanner scanner) {
        System.out.println("是否打印凭条? (Y/N)");
        return scanner.nextLine().trim();
    }

    public void showFarewell() {
        System.out.println("请取卡，欢迎下次使用。");
    }
}
