package net.p2pexchangehub.client.web.dialog.sendmoney;

import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;

import net.p2pexchangehub.core.api._domain.CurrencyAmount;
import net.p2pexchangehub.core.api.user.SendMoneyToUserBankAccountCommand;
import net.p2pexchangehub.view.domain.UserAccount;

public class SendMoneyToUserAccountDialog {

    @Inject
    private CommandGateway commandGateway;
    
    public void open(UserAccount userAccount, CurrencyAmount walletAmount) {
        SendMoneyToUserForm form = new SendMoneyToUserForm(userAccount);
        form.setSavedHandler(entity -> {
            commandGateway.send(new SendMoneyToUserBankAccountCommand(userAccount.getId(), entity.getBankAccount().getAccountNumber(), new CurrencyAmount(entity.getCurrency(), entity.getAmount())));
            form.closePopup();
        });
        form.setEntity(new TransferRequest(walletAmount));
        form.openInModalPopup();
    }
    
    public void open(UserAccount userAccount) {
        open(userAccount, null);
    }
    
}
