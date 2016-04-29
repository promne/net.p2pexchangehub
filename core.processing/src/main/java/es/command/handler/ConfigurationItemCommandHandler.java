package es.command.handler;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.repository.Repository;

import es.aggregate.ConfigurationItem;
import es.command.ChangeConfigurationItemValueCommand;
import es.command.CreateConfigurationItemCommand;

@Singleton
public class ConfigurationItemCommandHandler {

    @Inject
    private Repository<ConfigurationItem> repository;
    
    @CommandHandler
    public void handleCreateOffer(CreateConfigurationItemCommand command) {
        ConfigurationItem configurationItem = new ConfigurationItem(command.getId(), command.getValue());
        repository.add(configurationItem);
    }
    
    @CommandHandler
    public void handleValueChange(ChangeConfigurationItemValueCommand command) {
        repository.load(command.getId()).setValue(command.getValue());
    }
    
}
