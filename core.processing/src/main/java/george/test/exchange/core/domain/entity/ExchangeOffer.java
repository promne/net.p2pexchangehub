package george.test.exchange.core.domain.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import george.test.exchange.core.domain.entity.bank.ExternalBankAccount;
import george.test.exchange.core.domain.offer.OfferState;

@Entity
public class ExchangeOffer {

    @Id
    private String id = UUID.randomUUID().toString();
    
    @Version
    private long version;
    
    @ManyToOne(optional=false)
    private UserAccount owner;
    
    private Date created;
    
    private String currencyOffered;
    
    @Column(precision=10, scale=4)
    private BigDecimal amountOfferedMin;

    @Column(precision=10, scale=4)
    private BigDecimal amountOfferedMax;
    
    // how much was actually offered in the deal
    @Column(precision=10, scale=4)
    private BigDecimal amountOffered;
    
    //received amount towards amountOffered
    @Column(precision=10, scale=4)
    private BigDecimal amountReceived = BigDecimal.ZERO;
    
    private String currencyRequested;

    @Column(precision=10, scale=4)
    private BigDecimal amountRequestedExchangeRate;
    
    // how much will the user actually get
    @Column(precision=10, scale=4)
    private BigDecimal amountRequested;
    
    private OfferState state;
    public static final String STATE = "state";
    
    private String ownerAccountNumber;
    
    @OneToOne
    private ExchangeOffer matchedExchangeOffer;
    public static final String MATCHED_EXCHANGE_OFFER = "matchedExchangeOffer";
    
    private String referenceId;
    
    @ManyToOne
    private ExternalBankAccount incomimgPaymentBankAccount;
    public static final String INCOMIMG_PAYMENT_BANK_ACCOUNT = "incomimgPaymentBankAccount";

    public ExchangeOffer() {
        super();
    }

    public String getOwnerAccountNumber() {
        return ownerAccountNumber;
    }

    public void setOwnerAccountNumber(String ownerAccountNumber) {
        this.ownerAccountNumber = ownerAccountNumber;
    }

    public ExternalBankAccount getIncomimgPaymentBankAccount() {
        return incomimgPaymentBankAccount;
    }

    public void setIncomimgPaymentBankAccount(ExternalBankAccount incomimgPaymentBankAccount) {
        this.incomimgPaymentBankAccount = incomimgPaymentBankAccount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserAccount getOwner() {
        return owner;
    }

    public void setOwner(UserAccount owner) {
        this.owner = owner;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getCurrencyOffered() {
        return currencyOffered;
    }

    public void setCurrencyOffered(String currencyOffered) {
        this.currencyOffered = currencyOffered;
    }

    public BigDecimal getAmountOffered() {
        return amountOffered;
    }

    public void setAmountOffered(BigDecimal amountOffered) {
        this.amountOffered = amountOffered;
    }

    public String getCurrencyRequested() {
        return currencyRequested;
    }

    public void setCurrencyRequested(String currencyRequested) {
        this.currencyRequested = currencyRequested;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public BigDecimal getAmountOfferedMin() {
        return amountOfferedMin;
    }

    public void setAmountOfferedMin(BigDecimal amountOfferedMin) {
        this.amountOfferedMin = amountOfferedMin;
    }

    public BigDecimal getAmountOfferedMax() {
        return amountOfferedMax;
    }

    public void setAmountOfferedMax(BigDecimal amountOfferedMax) {
        this.amountOfferedMax = amountOfferedMax;
    }

    public BigDecimal getAmountRequestedExchangeRate() {
        return amountRequestedExchangeRate;
    }

    public void setAmountRequestedExchangeRate(BigDecimal amountRequestedExchangeRate) {
        this.amountRequestedExchangeRate = amountRequestedExchangeRate;
    }

    public OfferState getState() {
        return state;
    }

    public void setState(OfferState state) {
        this.state = state;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public ExchangeOffer getMatchedExchangeOffer() {
        return matchedExchangeOffer;
    }

    public void setMatchedExchangeOffer(ExchangeOffer matchedExchangeOffer) {
        this.matchedExchangeOffer = matchedExchangeOffer;
    }

    public BigDecimal getAmountReceived() {
        return amountReceived;
    }

    public void setAmountReceived(BigDecimal amountReceived) {
        this.amountReceived = amountReceived;
    }

    public BigDecimal getAmountRequested() {
        return amountRequested;
    }

    public void setAmountRequested(BigDecimal amountRequested) {
        this.amountRequested = amountRequested;
    }
    
}
