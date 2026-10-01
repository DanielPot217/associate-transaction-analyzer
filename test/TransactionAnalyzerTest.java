import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class TransactionAnalyzerTest {

    // Example from the specification, validate final output

    @Test
    void exampleInputProducesExpectedOutput() {
        JsonObject results = TransactionAnalyzer.analyze(List.of(
            new Transaction("A100", "credit", 500, "2026-05-10T10:00:00Z"),
            new Transaction("A100", "debit", 200, "2026-05-10T12:00:00Z"),
            new Transaction("B200", "debit", 50, "2026-05-10T11:00:00Z"),
            new Transaction("B200", "credit", 20, "2026-05-10T13:00:00Z"),
            new Transaction("", "credit", 100, "2026-05-10T14:00:00Z")
        ));

        JsonObject expected = JsonParser.parseString("""
            {
              "summaries": [
                {
                  "accountId": "A100",
                  "totalCredits": 500,
                  "totalDebits": 200,
                  "endingBalance": 300,
                  "largestTransaction": 500,
                  "transactionCount": 2,
                  "overdrawn": false
                },
                {
                  "accountId": "B200",
                  "totalCredits": 20,
                  "totalDebits": 50,
                  "endingBalance": -30,
                  "largestTransaction": 50,
                  "transactionCount": 2,
                  "overdrawn": true
                }
              ],
              "topSpender": "A100"
            }
            """).getAsJsonObject();

        assertEquals(expected, results);
    }

    // Invalid transactions are ignored

    @Test
    void ignoresInvalidAccountIds() {
        JsonObject results = TransactionAnalyzer.analyze(List.of(
            new Transaction(null, "credit", 100, "2026-05-10T10:00:00Z"),
            new Transaction("", "credit", 100, "2026-05-10T11:00:00Z"),
            new Transaction("   ", "credit", 100, "2026-05-10T12:00:00Z")
        ));

        assertTrue(results.getAsJsonArray("summaries").isEmpty());
    }

    @Test
    void ignoresInvalidTypes() {
        JsonObject results = TransactionAnalyzer.analyze(List.of(
            new Transaction("A100", null, 100, "2026-05-10T10:00:00Z"),
            new Transaction("A100", "refund", 100, "2026-05-10T11:00:00Z")
        ));

        assertTrue(results.getAsJsonArray("summaries").isEmpty());
    }

    @Test
    void ignoresInvalidAmounts() {
        JsonObject results = TransactionAnalyzer.analyze(List.of(
            new Transaction("A100", "credit", 0, "2026-05-10T10:00:00Z"),
            new Transaction("A100", "credit", -50, "2026-05-10T11:00:00Z")
        ));

        assertTrue(results.getAsJsonArray("summaries").isEmpty());
    }

    // Null input

    @Test
    void nullTransactionListIsTreatedAsEmpty() {
        JsonObject results = TransactionAnalyzer.analyze(null);

        assertTrue(results.getAsJsonArray("summaries").isEmpty());
        assertEquals("", results.get("topSpender").getAsString());
    }

    @Test
    void nullTransactionsInListAreSkipped() {
        JsonObject results = TransactionAnalyzer.analyze(Arrays.asList(
            null,
            new Transaction("A100", "credit", 100, "2026-05-10T10:00:00Z"),
            null
        ));

        JsonArray summaries = results.getAsJsonArray("summaries");
        assertEquals(1, summaries.size());
        assertEquals(1, summaries.get(0).getAsJsonObject().get("transactionCount").getAsInt());
    }

    @Test
    void invalidTransactionsDoNotAffectValidOnesForSameAccount() {
        JsonObject results = TransactionAnalyzer.analyze(List.of(
            new Transaction("A100", "credit", 100, "2026-05-10T10:00:00Z"),
            new Transaction("A100", "refund", 999, "2026-05-10T11:00:00Z"),
            new Transaction("A100", "debit", -999, "2026-05-10T12:00:00Z")
        ));

        JsonObject summary = results.getAsJsonArray("summaries").get(0).getAsJsonObject();
        assertEquals(1, summary.get("transactionCount").getAsInt());
        assertEquals(100, summary.get("endingBalance").getAsInt());
        assertEquals(100, summary.get("largestTransaction").getAsInt());
    }

    // Large amounts

    @Test
    void hugeAmountsDoNotOverflow() {
        JsonObject results = TransactionAnalyzer.analyze(List.of(
            new Transaction("A100", "credit", 2000000000, "2026-05-10T10:00:00Z"),
            new Transaction("A100", "credit", 2000000000, "2026-05-10T11:00:00Z"),
            new Transaction("A100", "debit", 1, "2026-05-10T12:00:00Z")
        ));

        JsonObject summary = results.getAsJsonArray("summaries").get(0).getAsJsonObject();

        assertEquals(4000000000L, summary.get("totalCredits").getAsLong());
        assertEquals(3999999999L, summary.get("endingBalance").getAsLong());
        assertFalse(summary.get("overdrawn").getAsBoolean());
    }


    // Ordering
    @Test
    void summariesAreSortedByAccountId() {
        JsonObject results = TransactionAnalyzer.analyze(List.of(
            new Transaction("C300", "credit", 10, "2026-05-10T10:00:00Z"),
            new Transaction("A100", "credit", 10, "2026-05-10T11:00:00Z"),
            new Transaction("B200", "credit", 10, "2026-05-10T12:00:00Z")
        ));

        JsonArray summaries = results.getAsJsonArray("summaries");

        assertEquals("A100", summaries.get(0).getAsJsonObject().get("accountId").getAsString());
        assertEquals("B200", summaries.get(1).getAsJsonObject().get("accountId").getAsString());
        assertEquals("C300", summaries.get(2).getAsJsonObject().get("accountId").getAsString());
    }

    // Top spender

    @Test
    void topSpenderIsAccountWithHighestDebits() {
        JsonObject results = TransactionAnalyzer.analyze(List.of(
            new Transaction("A100", "debit", 100, "2026-05-10T10:00:00Z"),
            new Transaction("B200", "debit", 300, "2026-05-10T11:00:00Z"),
            new Transaction("C300", "debit", 200, "2026-05-10T12:00:00Z")
        ));

        assertEquals("B200", results.get("topSpender").getAsString());
    }

    @Test
    void topSpenderTieGoesToLexicographicallySmallerAccountId() {
        JsonObject results = TransactionAnalyzer.analyze(List.of(
            new Transaction("B200", "debit", 100, "2026-05-10T10:00:00Z"),
            new Transaction("A100", "debit", 100, "2026-05-10T11:00:00Z")
        ));

        assertEquals("A100", results.get("topSpender").getAsString());
    }

    @Test
    void topSpenderIsEmptyWhenNoAccountHasDebits() {
        JsonObject results = TransactionAnalyzer.analyze(List.of(
            new Transaction("A100", "credit", 100, "2026-05-10T10:00:00Z"),
            new Transaction("B200", "credit", 200, "2026-05-10T11:00:00Z")
        ));

        assertEquals("", results.get("topSpender").getAsString());
    }


    // Input normalization

    @Test
    void accountIdsAreCaseInsensitiveAndTrimmed() {
        JsonObject results = TransactionAnalyzer.analyze(List.of(
            new Transaction("a100", "credit", 100, "2026-05-10T10:00:00Z"),
            new Transaction(" A100 ", "credit", 50, "2026-05-10T11:00:00Z")
        ));

        assertEquals(1, results.getAsJsonArray("summaries").size());

        JsonObject summary = results.getAsJsonArray("summaries").get(0).getAsJsonObject();
        assertEquals("A100", summary.get("accountId").getAsString());
        assertEquals(2, summary.get("transactionCount").getAsInt());
    }

    @Test
    void typesAreCaseInsensitiveAndTrimmed() {
        JsonObject results = TransactionAnalyzer.analyze(List.of(
            new Transaction("A100", "CREDIT", 100, "2026-05-10T10:00:00Z"),
            new Transaction("A100", " Debit ", 30, "2026-05-10T11:00:00Z")
        ));

        JsonObject summary = results.getAsJsonArray("summaries").get(0).getAsJsonObject();
        assertEquals(100, summary.get("totalCredits").getAsInt());
        assertEquals(30, summary.get("totalDebits").getAsInt());
    }
}
