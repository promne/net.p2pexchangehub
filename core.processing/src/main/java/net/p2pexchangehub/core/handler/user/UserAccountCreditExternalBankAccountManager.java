package net.p2pexchangehub.core.handler.user;

import java.util.Optional;

import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.annotation.EventHandler;

import net.p2pexchangehub.core.api.external.bank.ExternalBankTransactionRequestConfirmedEvent;
import net.p2pexchangehub.core.api.external.bank.ExternalBankTransactionRequestFailedEvent;
import net.p2pexchangehub.core.api.external.bank.RequestExternalBankTransactionCommand;
import net.p2pexchangehub.core.api.user.ConfirmAccountDebitReservationCommand;
import net.p2pexchangehub.core.api.user.DiscardAccountDebitReservationCommand;
import net.p2pexchangehub.core.api.user.UserAccountDebitForExternalBankAccountReservedEvent;
import net.p2pexchangehub.core.handler.AbstractIgnoreReplayEventHandler;
import net.p2pexchangehub.view.domain.BankAccount;
import net.p2pexchangehub.view.repository.BankAccountRepository;

public class UserAccountCreditExternalBankAccountManager extends AbstractIgnoreReplayEventHandler {

    @Inject
    private CommandGateway gateway;
    
    @Inject
    private BankAccountRepository bankAccountRepository;
    
    @EventHandler
    public void reserveAmount(UserAccountDebitForExternalBankAccountReservedEvent event) {
        if (isLive()) {
            //TODO: better external bank account matching
            Optional<BankAccount> externalBankAccount = bankAccountRepository.findByCurrencyAndActiveTrue(event.getAmount().getCurrencyCode()).stream()
                    .filter(a -> a.getBalance().compareTo(event.getAmount().getAmount())>=0).findAny();
            if (externalBankAccount.isPresent()) {
                gateway.send(new RequestExternalBankTransactionCommand(externalBankAccount.get().getId(), event.getTransactionId(), event.getUserAccountId(), event.getBankAccountNumber(), event.getAmount()));
            } else {
                gateway.send(new DiscardAccountDebitReservationCommand(event.getUserAccountId(), event.getTransactionId()));                
            }
        }
    }

    @EventHandler
    public void releaseReservedAmountOnExternalBankFailure(ExternalBankTransactionRequestFailedEvent event) {
        //TODO: better handling. Like retries etc.
        if (isLive()) {
            gateway.send(new DiscardAccountDebitReservationCommand(event.getUserAccountId(), event.getTransactionId()));
        }
    }

    @EventHandler
    public void comfirmReservedAmountOnExternalBankConfirmation(ExternalBankTransactionRequestConfirmedEvent event) {
        if (isLive()) {
            gateway.send(new ConfirmAccountDebitReservationCommand(event.getUserAccountId(), event.getTransactionId()));
        }
    }
}
