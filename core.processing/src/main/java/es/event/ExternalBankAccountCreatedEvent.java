package es.event;

import george.test.exchange.core.domain.ExternalBankType;

public class ExternalBankAccountCreatedEvent {

    private final String bankAccountId;

    private final String currency;

    private final String accountNumber;

    private final ExternalBankType bankType;

    public ExternalBankAccountCreatedEvent(String bankAccountId, String currency, String accountNumber, ExternalBankType bankType) {
        super();
        this.bankAccountId = bankAccountId;
        this.currency = currency;
        this.accountNumber = accountNumber;
        this.bankType = bankType;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public ExternalBankType getBankType() {
        return bankType;
    }

}
