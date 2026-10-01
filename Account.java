import java.util.LinkedHashMap;
import java.util.Map;

public class Account{

    private final String accountId;
    private long totalCredits;
    private long totalDebits;
    private long endingBalance;
    private long largestTransaction;
    private int transactionCount;
    private boolean overdrawn;

    Account(String accountId){
        this.accountId = accountId.toUpperCase().strip();
        this.totalCredits = 0;
        this.totalDebits = 0;
        this.endingBalance = 0;
        this.largestTransaction = 0;
        this.transactionCount = 0;
        this.overdrawn = false;
    }

    public String getAccountId(){
        return this.accountId;
    }

    public long getTotalDebits(){
        return this.totalDebits;
    }


    public void updateAccount(String type, long amount){

        if(type.equals("credit")){
            this.totalCredits += amount;
        }
        else if(type.equals("debit")){
            this.totalDebits += amount;
        }

        this.endingBalance = Math.subtractExact(this.totalCredits, this.totalDebits);

        if(amount > this.largestTransaction){
            this.largestTransaction = amount;
        }

        this.transactionCount += 1;

        this.overdrawn = this.endingBalance < 0;

        System.out.printf("Account %s Updated with new transaction!\n", this.accountId);
    }

    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("accountId", accountId);
        summary.put("totalCredits", totalCredits);
        summary.put("totalDebits", totalDebits);
        summary.put("endingBalance", endingBalance);
        summary.put("largestTransaction", largestTransaction);
        summary.put("transactionCount", transactionCount);
        summary.put("overdrawn", overdrawn);
        return summary;
    }
}
