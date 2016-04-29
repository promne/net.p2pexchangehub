package es.command;

import java.math.BigDecimal;

public class MatchExternalBankTransactionWithUserAccountCommand {

    private final String transactionId;

    private final String userAccountId;

    private final BigDecimal amount;

    public MatchExternalBankTransactionWithUserAccountCommand(String transactionId, String userAccountId, BigDecimal amount) {
        super();
        this.transactionId = transactionId;
        this.userAccountId = userAccountId;
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

}
