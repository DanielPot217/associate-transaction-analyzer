import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        List<Transaction> transactions = List.of(
            new Transaction("B200", "debit", 50, "2026-05-10T11:00:00Z"),
            new Transaction("B200", "credit", 20, "2026-05-10T13:00:00Z"),
            new Transaction("A100", "credit", 500, "2026-05-10T10:00:00Z"),
            new Transaction("A100", "debit", 200, "2026-05-10T12:00:00Z"),
            new Transaction("", "credit", 100, "2026-05-10T14:00:00Z")
        );

        Map<String, Object> results = TransactionAnalyzer.analyze(transactions);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(results));
    }
}
