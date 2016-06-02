package george.test.exchange.core.processing.service;

import java.util.Optional;

import javax.inject.Inject;

import org.mindrot.jbcrypt.BCrypt;

import net.p2pexchangehub.view.domain.UserAccount;
import net.p2pexchangehub.view.repository.UserAccountRepository;

public class AuthenticationService {

    @Inject
    private UserAccountRepository userAccountRepository;
    
    public Optional<UserAccount> authenticate(String username, String password, Object metadata) {
        //FIXME: turn into call to aggregate!! With additional info like ip etc.
        Optional<UserAccount> userAccount = userAccountRepository.findOneByUsername(username);
        if (userAccount.isPresent() && userAccount.get().isEnabled()) {
            if (BCrypt.checkpw(password, userAccount.get().getPasswordHash())) {
                return userAccount;
            }            
        }
        return Optional.empty();
    }

}
