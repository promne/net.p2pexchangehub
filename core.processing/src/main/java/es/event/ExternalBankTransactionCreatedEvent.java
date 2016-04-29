package es.event;

import java.math.BigDecimal;
import java.util.Date;

public class ExternalBankTransactionCreatedEvent {

    private final String id;

    private final String bankAccountId;
    
    private final BigDecimal amount;

    private final Date date;

    private final String fromAccount;

    private final String detailInfo;

    private final String externalId;

    public ExternalBankTransactionCreatedEvent(String id, String bankAccountId, BigDecimal amount, Date date, String fromAccount, String detailInfo, String externalId) {
        super();
        this.id = id;
        this.bankAccountId = bankAccountId;
        this.amount = amount;
        this.date = date;
        this.fromAccount = fromAccount;
        this.detailInfo = detailInfo;
        this.externalId = externalId;
    }

    public String getId() {
        return id;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public String getDetailInfo() {
        return detailInfo;
    }

    public String getExternalId() {
        return externalId;
    }

}
