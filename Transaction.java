public class Transaction {
    private final String accountId;
    private final String type;
    private final long amount;
    private final String timestamp;

    public Transaction(String accountId, String type, long amount, String timestamp) {
        this.accountId = accountId == null ? null : accountId.toUpperCase().strip();
        this.type = type == null ? null : type.toLowerCase().strip();
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getType() {
        return type;
    }

    public long getAmount() {
        return amount;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
