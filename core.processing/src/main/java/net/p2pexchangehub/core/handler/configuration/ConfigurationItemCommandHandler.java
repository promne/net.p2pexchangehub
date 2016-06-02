package net.p2pexchangehub.core.handler.configuration;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.domain.MetaData;
import org.axonframework.repository.Repository;

import net.p2pexchangehub.core.api.configuration.ChangeConfigurationItemValueCommand;
import net.p2pexchangehub.core.api.configuration.CreateConfigurationItemCommand;

@Singleton
public class ConfigurationItemCommandHandler {

    @Inject
    private Repository<ConfigurationItem> repository;
    
    @CommandHandler
    public void handleCreateOffer(CreateConfigurationItemCommand command, MetaData metadata) {
        ConfigurationItem configurationItem = new ConfigurationItem(command.getId(), command.getValue(), metadata);
        repository.add(configurationItem);
    }
    
    @CommandHandler
    public void handleValueChange(ChangeConfigurationItemValueCommand command, MetaData metadata) {
        repository.load(command.getId()).setValue(command.getValue(), metadata);
    }
    
}
