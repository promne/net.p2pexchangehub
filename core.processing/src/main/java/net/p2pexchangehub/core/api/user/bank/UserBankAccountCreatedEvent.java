package net.p2pexchangehub.core.api.user.bank;

import net.p2pexchangehub.core.handler.user.UserBankAccount;

public class UserBankAccountCreatedEvent {

    private final String userAccountId;

    private final UserBankAccount bankAccount;

    public UserBankAccountCreatedEvent(String userAccountId, UserBankAccount bankAccount) {
        super();
        this.userAccountId = userAccountId;
        this.bankAccount = bankAccount;
    }

    public String getUserAccountId() {
        return userAccountId;
    }

    public UserBankAccount getBankAccount() {
        return bankAccount;
    }

}
