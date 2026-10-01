import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransactionAnalyzer {

    public static JsonObject analyze(List<Transaction> transactions) {
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


        List<Map<String, Object>> summaries = new ArrayList<>();

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

        return new Gson().toJsonTree(results).getAsJsonObject();
    }
}
