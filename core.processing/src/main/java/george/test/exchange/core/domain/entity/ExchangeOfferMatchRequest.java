package george.test.exchange.core.domain.entity;

import java.math.BigDecimal;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
//@Table(uniqueConstraints=@UniqueConstraint(columnNames = {ExchangeOfferMatchRequest.SOURCE, ExchangeOfferMatchRequest.DESTINATION}))
public class ExchangeOfferMatchRequest {

    @Id
    private String id = UUID.randomUUID().toString();
    
    @ManyToOne(optional=false)
    private ExchangeOffer offer;

    @ManyToOne(optional=false)
    private UserAccount userAccountRequesting;
    
    private String ownerAccountNumber;
    
    private BigDecimal requestedAmount;

    public ExchangeOfferMatchRequest() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ExchangeOffer getOffer() {
        return offer;
    }

    public void setOffer(ExchangeOffer offer) {
        this.offer = offer;
    }

    public UserAccount getUserAccountRequesting() {
        return userAccountRequesting;
    }

    public void setUserAccountRequesting(UserAccount userAccountRequesting) {
        this.userAccountRequesting = userAccountRequesting;
    }

    public String getOwnerAccountNumber() {
        return ownerAccountNumber;
    }

    public void setOwnerAccountNumber(String ownerAccountNumber) {
        this.ownerAccountNumber = ownerAccountNumber;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

}
