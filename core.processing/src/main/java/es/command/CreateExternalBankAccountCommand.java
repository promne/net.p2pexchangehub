package es.command;

import george.test.exchange.core.domain.ExternalBankType;

public class CreateExternalBankAccountCommand {

    private final String bankAccountId;

    private final String currency;

    private final String country;

    private final String accountNumber;

    private final ExternalBankType bankType;

    public CreateExternalBankAccountCommand(String bankAccountId, String currency, String country, String accountNumber, ExternalBankType bankType) {
        super();
        this.bankAccountId = bankAccountId;
        this.currency = currency;
        this.country = country;
        this.accountNumber = accountNumber;
        this.bankType = bankType;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCountry() {
        return country;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public ExternalBankType getBankType() {
        return bankType;
    }

}
