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

        // A null list is treated the same as an empty one
        if (transactions == null) {
            transactions = List.of();
        }

        // Reads the list of transactions provided to the Analyzer
        for (Transaction transaction : transactions) {
            if (transaction == null) {
                System.out.println("\nSkipping null transaction.\n");
                continue;
            }

            String accountId = transaction.getAccountId();
            long amount = transaction.getAmount();
            String type = transaction.getType();

            // Input Validation, checks for an invalid fields, skips if found
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


            // Updates Account based on AccountId from Tranasction, will create a new account if no account found 
            Account account = accounts.get(accountId);

            if (account == null) {
                account = new Account(accountId);
                accounts.put(accountId, account);
            }

            account.updateAccount(type, amount);
        }


        // Creates ArrayList of summaries to add to results, based on account information
        // Sorts the summaries list by AccountId to get alphabetically sorted list
        List<Map<String, Object>> summaries = new ArrayList<>();
        for (Account account : accounts.values()) {
            summaries.add(account.getSummary());
        }

        summaries.sort(Comparator.comparing(summary -> (String) summary.get("accountId")));


        // Calculates top spender for all accounts, if a tie between accounts happens, accountId that comes lexicographically(ids with lower ASCII value) first gets picked
        String topSpender = "";
        long highestDebits = 0;

        for (Account account : accounts.values()) {
            long accountDebits = account.getTotalDebits();
            String accountId = account.getAccountId();

            if (accountDebits > highestDebits || (accountDebits == highestDebits && (accountId.compareTo(topSpender) < 0))) {
                highestDebits = accountDebits;
                topSpender = accountId;
            }
        }

        // Final results map gets created, serializes it into a Json object, and returns that object 
        Map<String, Object> results = new LinkedHashMap<>();
        results.put("summaries", summaries);
        results.put("topSpender", topSpender);

        return new Gson().toJsonTree(results).getAsJsonObject();
    }
}
