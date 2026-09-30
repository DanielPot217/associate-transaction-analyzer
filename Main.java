import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Transaction[] transactions = {
            new Transaction("A100", "credit", 500, "2026-05-10T10:00:00Z"),
            new Transaction("A100", "debit", 200, "2026-05-10T12:00:00Z"),
            new Transaction("B200", "debit", 50, "2026-05-10T11:00:00Z"),
            new Transaction("B200", "credit", 20, "2026-05-10T13:00:00Z"),
            new Transaction("", "credit", 100, "2026-05-10T14:00:00Z" )
        };

        Map<String, Account> accounts = new LinkedHashMap<>();

        for (Transaction transaction : transactions) {
            String accountId = transaction.getAccountId();
            int amount = transaction.getAmount();
            String type = transaction.getType();

            if (accountId == null || accountId.isBlank()) {
                System.out.println("Skipping transaction with invalid account ID, cannot be empty.");
                continue;
            }

            if (amount <= 0) {
                System.out.println("Skipping transaction with invalid amount, Must be above 0.");
                continue;
            }

            if (type == null || (!type.equals("credit") && !type.equals("debit"))) {
                System.out.println("Skipping transaction with invalid type, must be credit or debit.");
                continue;
            }



            Account account = accounts.get(accountId);

            if (account == null) {
                account = new Account(accountId);
                accounts.put(accountId, account);
            }

            account.updateAccount(type, amount);
        }

        for (Account account : accounts.values()) {
            System.out.println(account.getSummary());
        }
    }
}