package george.test.exchange.core.processing.service;

import java.util.Optional;

import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;

import net.p2pexchangehub.core.api.user.AuthenticateUserAccountCommand;
import net.p2pexchangehub.view.domain.UserAccount;
import net.p2pexchangehub.view.repository.UserAccountRepository;

public class AuthenticationService {

    @Inject
    private UserAccountRepository userAccountRepository;
    
    @Inject
    private CommandGateway commandGateway;
    
    public Optional<UserAccount> authenticate(String username, String password) {
        String authenticatedUserAccountId = commandGateway.sendAndWait(new AuthenticateUserAccountCommand(username, password));
        if (authenticatedUserAccountId != null) {
            UserAccount userAccount = userAccountRepository.findOne(authenticatedUserAccountId);
            return Optional.of(userAccount);
        }
        return Optional.empty();
    }

}
