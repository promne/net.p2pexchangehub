package net.p2pexchangehub.core.api.user;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;

public class UserAccountChargedFromOfferEvent {

    private final String userAccountId;

    private final String offerId;

    private final CurrencyAmount amount;

    private final CurrencyAmount newBalance;

    public UserAccountChargedFromOfferEvent(String userAccountId, String offerId, CurrencyAmount amount, CurrencyAmount newBalance) {
        super();
        this.userAccountId = userAccountId;
        this.offerId = offerId;
        this.amount = amount;
        this.newBalance = newBalance;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public String getOfferId() {
        return offerId;
    }

    public CurrencyAmount getAmount() {
        return amount;
    }

    public CurrencyAmount getNewBalance() {
        return newBalance;
    }

}
