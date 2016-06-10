package net.p2pexchangehub.view.repository;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import net.p2pexchangehub.view.domain.BankAccount;

public class BankAccountRepositoryHelper {

    @Inject
    private BankAccountRepository bankAccountRepository;
    
    public List<String> listAvailableCurrencies() {
        return bankAccountRepository.findByActiveTrue().stream().map(BankAccount::getCurrency).distinct().collect(Collectors.toList());
    }

    public String getIncomingPaymentInstructions(String currency, String userAccountId) {
        List<BankAccount> availableAccounts = bankAccountRepository.findByCurrencyAndActiveTrue(currency);
        
        BankAccount bankAccount = availableAccounts.get(0);
        return bankAccount.getAccountNumber();
    }
    
}
