package net.p2pexchangehub.view.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import net.p2pexchangehub.view.domain.BankCommunication;

public interface BankCommunicationRepository extends MongoRepository<BankCommunication, String> {

}
