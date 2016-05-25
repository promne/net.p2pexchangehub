package net.p2pexchangehub.view.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import net.p2pexchangehub.core.handler.offer.OfferState;
import net.p2pexchangehub.view.domain.Offer;

public interface OfferRepository extends MongoRepository<Offer, String> {

    public List<Offer> findByState(OfferState state);
    
    public List<Offer> findByUserAccountId(String userAccountId);

    public List<Offer> findByUserAccountIdAndState(String userAccountId, OfferState state);
        
}
