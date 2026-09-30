public class Transaction {
    private final String accountId;
    private final String type;
    private final int amount;
    private final String timestamp;

    public Transaction(String accountId, String type, int amount, String timestamp) {
        this.accountId = accountId.toUpperCase().strip();
        this.type = type.toLowerCase().strip();
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public String getTimestamp() {
        return timestamp;
    }
}