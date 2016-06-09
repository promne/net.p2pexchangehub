package net.p2pexchangehub.view.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import net.p2pexchangehub.view.domain.BankAccount;

public interface BankAccountRepository extends MongoRepository<BankAccount, String> {

    public List<BankAccount> findByCurrencyAndActiveTrue(String currency);

    public List<BankAccount> findByActiveTrue();
    
}
