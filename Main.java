import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Transaction[] transactions = {

            new Transaction("B200", "debit", 50, "2026-05-10T11:00:00Z"),
            new Transaction("B200", "credit", 20, "2026-05-10T13:00:00Z"),
            new Transaction("A100", "credit", 500, "2026-05-10T10:00:00Z"),
            new Transaction("A100", "debit", 200, "2026-05-10T12:00:00Z"),
            new Transaction("", "credit", 100, "2026-05-10T14:00:00Z" )
        };

        Map<String, Account> accounts = new LinkedHashMap<>();

        for (Transaction transaction : transactions) {
            String accountId = transaction.getAccountId();
            int amount = transaction.getAmount();
            String type = transaction.getType();

            if (accountId == null || accountId.isBlank()) {
                System.out.println("\nSkipping transaction with invalid account ID, cannot be empty.\n");
                continue;
            }

            if (amount <= 0) {
                System.out.println("\nSkipping transaction with invalid amount, Must be above 0.\n");
                continue;
            }

            if (type == null || (!type.equals("credit") && !type.equals("debit"))) {
                System.out.println("\nSkipping transaction with invalid type, must be credit or debit.\n");
                continue;
            }

            Account account = accounts.get(accountId);

            if (account == null) {
                account = new Account(accountId);
                accounts.put(accountId, account);
            }

            account.updateAccount(type, amount);
        }


        List<Map> summaries = new ArrayList<>();

        for (Account account : accounts.values()) {
            summaries.add(account.getSummary());
        }

        summaries.sort(Comparator.comparing(summary -> (String) summary.get("accountId")));

        String topSpender = "";
        int highestDebits = 0;

        for (Account account : accounts.values()) {
            int accountDebits = account.getTotalDebits();
            String accountId = account.getAccountId();

            if (accountDebits > highestDebits || (accountDebits == highestDebits && (accountId.compareTo(topSpender) < 0))) {
                highestDebits = accountDebits;
                topSpender = accountId;
            }
        }


        Map<String, Object> results = new LinkedHashMap<>();
        results.put("summaries", summaries);
        results.put("topSpender", topSpender);

        System.out.println(results);
    }
}