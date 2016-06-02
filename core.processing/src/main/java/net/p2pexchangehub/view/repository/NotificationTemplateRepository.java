package net.p2pexchangehub.view.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import net.p2pexchangehub.view.domain.NotificationTemplate;

public interface NotificationTemplateRepository extends MongoRepository<NotificationTemplate, String> {

}
