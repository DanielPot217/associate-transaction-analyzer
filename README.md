# Transaction Analyzer

A small Java program that processes a list of financial transactions and produces a summary for each account, plus the account with the highest total debits (`topSpender`).
This solution is made by Daniel Potapov.

## Design Process

After understanding the requirements from the requirements document, I began by implementing the Account & Transaction Classes with their proper attributes. Next I created the 
TransactionAnalyzer file and core logic of reading the list of transactions, creating/updating accounts, and processing the transactions per account. Finally I created the sample
Main.java file to run the program with sample input, and give the final JSON with the output expected. I then asked Claude Code to review my project to see if I missed any edge cases or if there are cleaner ways of writing specific portions of the code. After implementing the minor changes, I asked Claude to implement JUnit tests for the project to catch all the edge cases. Claude wrote around 30 tests which I narrowed down to 14 by combining certain assertions together (since they were practically doing the same thing). Periodic commits to git were made to have a history on GitHub of the development process. 


## How to Run

Requires Java 17 or newer. Both libraries are included in `lib/`, so there is nothing else to install.

Run commands:
```bash
javac -cp "lib/*" -d out *.java
java -cp "out;lib/*" Main
```

On macOS/Linux run with: 
```bash
javac -cp "lib/*" -d out *.java
java -cp "out:lib/*" Main
```

## Assumptions

- **Account IDs are case-insensitive** `"a100"` & `"A100"` are treated as the same account and reported as `A100`, keeping an ID standard for the application.
- **Transaction types are case-insensitive** `"CREDIT"` & `"credit"` are accepted as valid types.
- **Timestamps are not used in the calculation.** The timestamp is not used anywhere in the calculation or the output, hence is ignored, no sorting is done based on it.
- **Amounts are whole numbers** (`long`), matching the example input, long was chosen to accommodate larger amounts however.
- **Overdrawn** is `true` only when the ending balance is below 0. A balance of exactly 0 is not overdrawn.


## Design Choices and Tradeoffs

- **No build tools for external libraries.** Decided to go with single self-contained jars for the two libraries used. They are checked into `lib/` and the project compiles with a plain `javac`. Reason being since the project is smaller scale and dependencies won't grow, for simplicity I decided to just include them in the repo. A build tool like Maven or Gradle would be the better choice if the project was larger and had more dependencies.
- **Logic separated from `Main.java`.** `TransactionAnalyzer.analyze` takes a list of transactions and returns the result instead of printing it, so it can be tested directly. `Main` only provides the input and prints the output. `Main` could also load a JSON file instead of hardcoding the transaction list, but I decided to keep it hardcoded for simplicity.
- **Returning a `JsonObject`.** The analyzer builds the result as an ordered `LinkedHashMap`, to preserve the order of summaries after sorting, also having the correct order as in the specification, then converts it to a Gson `JsonObject`. Returning JSON lets tests read values with accessors and match the format exactly as specified in the document, using a pretty-print function in `Main` to simplify the output print.
- **Running totals in `Account`.** Each account updates its totals as transactions arrive instead of storing every transaction, since the scope of this project did not require storage of transactions for later use, easier to just loop over the list of transactions and update the totals.
- **Encapsulation of Attributes** For `Account` & `Transaction` classes, most of the attributes like amounts/type are private. Since we do not need to change any of the variables outside of updating the account, which the updateAccount function does, privatizing the attributes becomes a security layer, and only allows to retrieve some specific attributes for calculations but not directly change any of them.
- **Summary Sorting** Summary sorting is done using the .sort method of a list with a comparator. The comparator grabs the summary accountId for each summary in the list, and compares it to another to see which one comes first by ASCII characters (alphabetically), and moves them in the list accordingly.
- **Top Spender Comparison** The Top Spender is calculated first by seeing if the current account debits being checked is greater than the ones checked before it. If it's a tie it will lexicographically select the account that comes first. Using accountIds it uses the compareTo function by getting the ASCII values, and choosing one that has a lower value.

## Project Structure

| File | Purpose |
|---|---|
| `Main.java` | Entry point. Builds the sample transactions, calls the analyzer, and pretty-prints the JSON result. |
| `TransactionAnalyzer.java` | Core logic. `analyze(List<Transaction>)` validates transactions, builds account summaries, picks the top spender, and returns the result as a Gson `JsonObject`. |
| `Account.java` | Tracks running totals for a single account and produces its summary.  |
| `Transaction.java` | A single transaction: `accountId`, `type`, `amount`, `timestamp`. Normalizes `accountId` and `type` on creation. |
| `test/TransactionAnalyzerTest.java` | Unit tests for `TransactionAnalyzer`. |
| `lib/gson-2.14.0.jar` | [Gson](https://github.com/google/gson), used to build and print the JSON result. |
| `lib/junit-platform-console-standalone-6.1.3.jar` | [JUnit](https://junit.org), used to write and run the tests. |



## How It Works

1. A list of transactions is sent to the TransactionAnalyzer class from the Main Class. 
2. List of Transactions is read & each transaction is validated. Invalid ones are skipped.
3. Accounts are created the first time their ID appears in the list of transactions starting at a balance of 0, if account existed it will be updated with new data.
4. Valid transactions are applied to the account, updating balance, credit/debit amounts, etc... 
5. Summaries list (map) gets created after transaction list ends & sorted by `accountId`.
6. `topSpender` is then decided, which is the account with the highest `totalDebits`. On a tie, the lexicographically smaller `accountId` wins.
7. The summaries and `topSpender` are returned as a `JsonObject` to match output requirement.

Skipped transactions and account updates are also logged on the console before the JSON.


## Libraries

The project uses two third-party libraries. Both were downloaded from Maven Central and are checked into `lib/` and loaded with commands, so no build tool or package manager is needed.

| Library | Version | Purpose |
|---|---|---|
| Gson | 2.14.0 | Serializing final result object to JSON and used for testing |
| JUnit | 6.1.3 | Creating & Running Unit Tests |

**Gson**

Main purpose of the library was to help with formatting the final results into a JSON format to exactly match the output required and easily access with JSON specific accessors.

| File | Usage |
|---|---|
| `TransactionAnalyzer.java` | Used to convert the ordered results map into a `JsonObject`, for easier testing and format matching. |
| `Main.java` | prints the `JsonObject` in the indented format of the specification. |
| `TransactionAnalyzerTest.java` | turns the expected output into a `JsonObject` to compare against. `JsonObject` and `JsonArray` are used to read results.|

**JUnit**

Main purpose of the library was to create and run unit tests for the application.

| File | Usage |
|---|---|
| `TransactionAnalyzerTest.java` | `@Test` marks each test case. Used to unit test the entire application. |


## Tests

Tests are in `test/TransactionAnalyzerTest.java` and use JUnit.

```bash
javac -cp "lib/*" -d out *.java test/*.java
java -jar lib/junit-platform-console-standalone-6.1.3.jar execute -cp "out;lib/gson-2.14.0.jar" --scan-class-path
```

On macOS/Linux:
```bash
javac -cp "lib/*" -d out *.java test/*.java
java -jar lib/junit-platform-console-standalone-6.1.3.jar execute -cp "out:lib/gson-2.14.0.jar" --scan-class-path
```

There are 14 tests. Each one builds a list of `Transaction` objects, passes it to `TransactionAnalyzer.analyze`, and checks the returned `JsonObject`. Tests that check the same rule with several inputs (for example, the three kinds of invalid account ID) pass all the inputs in a single list instead of one test per input.

| # | Category | Test | What it checks |
|---|---|---|---|
| 1 | Example from the specification | `exampleInputProducesExpectedOutput` | The example input from the specification produces exactly the expected JSON, including the summaries and topSpender. |
| 2 | Invalid transactions | `ignoresInvalidAccountIds` | Transactions with a null, empty, or whitespace-only account ID are skipped. |
| 3 | Invalid transactions | `ignoresInvalidTypes` | Transactions with a null type or a type other than credit/debit (e.g. "refund") are skipped. |
| 4 | Invalid transactions | `ignoresInvalidAmounts` | Transactions with an amount of 0 or a negative amount are skipped. |
| 5 | Invalid transactions | `invalidTransactionsDoNotAffectValidOnesForSameAccount` | Invalid transactions mixed in with a valid one on the same account do not change its count, balance, or largest transaction. |
| 6 | Null input | `nullTransactionListIsTreatedAsEmpty` | Passing null instead of a list returns empty summaries and a topSpender of `""`. |
| 7 | Null input | `nullTransactionsInListAreSkipped` | null entries inside the list are skipped, and the valid transaction between them is still counted. |
| 8 | Large amounts | `hugeAmountsDoNotOverflow` | Two credits of 2,000,000,000 total 4,000,000,000 without overflowing, and the ending balance and `overdrawn` flag are correct. |
| 9 | Ordering | `summariesAreSortedByAccountId` | Accounts given out of order (`C300`, `A100`, `B200`) are returned sorted by `accountId`. |
| 10 | Top spender | `topSpenderIsAccountWithHighestDebits` | The account with the highest total debits is chosen. |
| 11 | Top spender | `topSpenderTieGoesToLexicographicallySmallerAccountId` | When two accounts have the same total debits, the smaller `accountId` wins. |
| 12 | Top spender | `topSpenderIsEmptyWhenNoAccountHasDebits` | When no account has any debits, `topSpender` is `""`. |
| 13 | Input normalization | `accountIdsAreCaseInsensitiveAndTrimmed` | `"a100"` and `" A100 "` are combined into a single `A100` account. |
| 14 | Input normalization | `typesAreCaseInsensitiveAndTrimmed` | `"CREDIT"` and `" Debit "` are accepted and applied as a credit and a debit. |

<br>
<br>


<br>

# Thank you for reviewing this project and reading this README

<br>

# Have a Great Day!