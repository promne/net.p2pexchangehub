package net.p2pexchangehub.view.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import net.p2pexchangehub.view.domain.UserAccount;

public interface UserAccountRepository  extends MongoRepository<UserAccount, String> {

    public Optional<UserAccount> findOneByUsername(String username);

    public Optional<UserAccount> findOneByPaymentsCode(String paymentCode);
    
}
