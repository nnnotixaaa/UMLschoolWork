package system;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import model.BankAccount;

public class EncryptedAccountDatabase {
    private static final String MAGIC = "ATMDB1";
    private static final String DB_PATH = "data/accounts.encdb";
    private static final String DB_MASTER_KEY_ENV = "ATM_DB_KEY";
    private static final String FALLBACK_MASTER_KEY = "change-this-demo-key";

    private final Path dbPath;
    private final byte[] dbKey;

    public EncryptedAccountDatabase() {
        this.dbPath = Paths.get(DB_PATH);
        String fromEnv = System.getenv(DB_MASTER_KEY_ENV);
        String masterKey = (fromEnv == null || fromEnv.isBlank()) ? FALLBACK_MASTER_KEY : fromEnv;
        this.dbKey = CryptoUtils.deriveAesKeyFromSecret(masterKey);
        initIfNotExists();
    }

    public synchronized BankAccount findByCard(String cardNumber) {
        List<BankAccount> accounts = loadAll();
        for (BankAccount account : accounts) {
            if (account.getCardNumber().equals(cardNumber)) {
                return account;
            }
        }
        return null;
    }

    public synchronized void upsert(BankAccount target) {
        List<BankAccount> accounts = loadAll();
        boolean replaced = false;
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getCardNumber().equals(target.getCardNumber())) {
                accounts.set(i, target);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            accounts.add(target);
        }
        saveAll(accounts);
    }

    private void initIfNotExists() {
        try {
            if (Files.exists(dbPath)) {
                return;
            }
            Files.createDirectories(dbPath.getParent());
            List<BankAccount> seed = new ArrayList<>();
            seed.add(BankAccount.createWithPlainPin("A1001", "622202001", "123456", 12000));
            saveAll(seed);
        } catch (IOException ex) {
            throw new IllegalStateException("初始化账户数据库失败", ex);
        }
    }

    private List<BankAccount> loadAll() {
        try {
            byte[] allBytes = Files.readAllBytes(dbPath);
            if (allBytes.length < MAGIC.length() + 12) {
                throw new IllegalStateException("数据库文件损坏");
            }
            byte[] magicBytes = Arrays.copyOfRange(allBytes, 0, MAGIC.length());
            String magic = new String(magicBytes, StandardCharsets.UTF_8);
            if (!MAGIC.equals(magic)) {
                throw new IllegalStateException("数据库头不匹配");
            }

            byte[] iv = Arrays.copyOfRange(allBytes, MAGIC.length(), MAGIC.length() + 12);
            byte[] cipherBytes = Arrays.copyOfRange(allBytes, MAGIC.length() + 12, allBytes.length);
            byte[] plainBytes = CryptoUtils.decryptAesGcm(cipherBytes, dbKey, iv);
            String plain = new String(plainBytes, StandardCharsets.UTF_8);
            return parseRows(plain);
        } catch (IOException ex) {
            throw new IllegalStateException("读取账户数据库失败", ex);
        }
    }

    private void saveAll(List<BankAccount> accounts) {
        try {
            String plain = toRows(accounts);
            byte[] plainBytes = plain.getBytes(StandardCharsets.UTF_8);
            byte[] iv = CryptoUtils.randomIv();
            byte[] cipherBytes = CryptoUtils.encryptAesGcm(plainBytes, dbKey, iv);

            byte[] magicBytes = MAGIC.getBytes(StandardCharsets.UTF_8);
            byte[] allBytes = new byte[magicBytes.length + iv.length + cipherBytes.length];
            System.arraycopy(magicBytes, 0, allBytes, 0, magicBytes.length);
            System.arraycopy(iv, 0, allBytes, magicBytes.length, iv.length);
            System.arraycopy(cipherBytes, 0, allBytes, magicBytes.length + iv.length, cipherBytes.length);

            Files.write(dbPath, allBytes);
        } catch (IOException ex) {
            throw new IllegalStateException("写入账户数据库失败", ex);
        }
    }

    private static List<BankAccount> parseRows(String plain) {
        List<BankAccount> accounts = new ArrayList<>();
        if (plain.isBlank()) {
            return accounts;
        }

        String[] rows = plain.split("\\R");
        for (String row : rows) {
            if (row.isBlank()) {
                continue;
            }
            String[] cols = row.split("\\|", -1);
            if (cols.length != 6) {
                throw new IllegalStateException("数据库记录格式错误: " + row);
            }
            String accountId = cols[0];
            String cardNumber = cols[1];
            String pinHash = cols[2];
            String pinSalt = cols[3];
            double balance = Double.parseDouble(cols[4]);
            boolean locked = Boolean.parseBoolean(cols[5]);
            accounts.add(new BankAccount(accountId, cardNumber, pinHash, pinSalt, balance, locked));
        }
        return accounts;
    }

    private static String toRows(List<BankAccount> accounts) {
        StringBuilder sb = new StringBuilder();
        for (BankAccount a : accounts) {
            sb.append(a.getAccountId()).append('|')
                    .append(a.getCardNumber()).append('|')
                    .append(a.getPinHash()).append('|')
                    .append(a.getPinSalt()).append('|')
                    .append(a.getBalance()).append('|')
                    .append(a.isLocked())
                    .append('\n');
        }
        return sb.toString();
    }
}
