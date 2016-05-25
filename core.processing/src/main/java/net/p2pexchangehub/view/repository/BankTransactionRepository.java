package net.p2pexchangehub.view.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import net.p2pexchangehub.view.domain.BankTransaction;

public interface BankTransactionRepository extends MongoRepository<BankTransaction, String> {

    public List<BankTransaction> findByBankAccountId(String bankAccountId);
    
}
