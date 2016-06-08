package net.p2pexchangehub.core.handler.external.bank.transaction;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.annotation.EventHandler;

import net.p2pexchangehub.core.api.external.bank.transaction.ExternalBankTransactionCreatedEvent;
import net.p2pexchangehub.core.api.external.bank.transaction.MatchIncomingExternalBankTransactionWithUserAccountCommand;
import net.p2pexchangehub.core.api.external.bank.transaction.MatchOutgoingExternalBankTransactionWithRequestedCommand;
import net.p2pexchangehub.core.handler.user.UserAccountPaymentsCodeGenerator;
import net.p2pexchangehub.view.domain.UserAccount;
import net.p2pexchangehub.view.repository.UserAccountRepository;

public class ExternalBankTransactionMatcher {

    @Inject
    private CommandGateway gateway;
    
    @Inject
    private UserAccountRepository userAccountViewRepository;
    
    @EventHandler
    public void matchWithUserIfPossible(ExternalBankTransactionCreatedEvent event) {
        if (event.getAmount().isNotNegative()) {
            
            final int paymentCodeLength = UserAccountPaymentsCodeGenerator.CODE_LENGTH;
            String referenceInfo = event.getReferenceInfo();
            
            Set<String> userAccountCandidates = new HashSet<>();
            for (int i=0; i<=referenceInfo.length()-paymentCodeLength; i++) {
                String paymentsCodeCandidate = referenceInfo.substring(i, i+paymentCodeLength).replaceAll("\\s", "").trim().toUpperCase();
                if (paymentsCodeCandidate.length()==paymentCodeLength) {
                    Optional<UserAccount> candidate = userAccountViewRepository.findOneByPaymentsCodeIgnoreCase(paymentsCodeCandidate);
                    if (candidate.isPresent()) {
                        userAccountCandidates.add(candidate.get().getId());
                    }
                }
            }
            
            if (userAccountCandidates.size()==1) {
                String userAccountId = userAccountCandidates.iterator().next();
                gateway.send(new MatchIncomingExternalBankTransactionWithUserAccountCommand(event.getId(), userAccountId));                
            }            
        } else {
            gateway.send(new MatchOutgoingExternalBankTransactionWithRequestedCommand(event.getId()));            
        }
    }
    
}
