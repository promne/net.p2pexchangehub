package net.p2pexchangehub.view.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import george.test.exchange.core.domain.entity.CurrencyConfiguration;

public interface CurrencyConfigurationRepository extends MongoRepository<CurrencyConfiguration, String> {

}
