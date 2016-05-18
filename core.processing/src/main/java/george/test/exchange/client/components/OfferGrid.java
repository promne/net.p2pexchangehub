package george.test.exchange.client.components;

import com.vaadin.addon.contextmenu.GridContextMenu;
import com.vaadin.addon.contextmenu.MenuItem;
import com.vaadin.cdi.ViewScoped;

import java.util.Arrays;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.gateway.CommandGateway;

import es.command.CancelExchangeOfferCommand;
import es.command.RequestOfferPaymentCommand;
import es.command.SetOwnerAccountNumberForOfferCommand;
import esw.domain.Offer;
import esw.domain.UserAccount;
import esw.domain.UserBankAccount;
import esw.view.UserAccountView;
import george.test.exchange.core.domain.offer.OfferState;

@ViewScoped
public class OfferGrid extends JPAGrid<Offer> {

    @Inject
    private CommandGateway gateway;
    
    @Inject
    private UserAccountView userAccountView;

    public OfferGrid() {
        super();

        setCellStyleGenerator(cellRef -> {
            Offer offer = getEntity(cellRef.getItemId());
            if (Arrays.asList(OfferState.PAYMENT_RECEIVED, OfferState.SEND_MONEY_REQUESTED, OfferState.WAITING_FOR_PAYMENT).contains(offer.getState())) {
                if (cellRef.getPropertyId().equals(Offer.PROPERTY_OWNER_ACCOUNT_NUMBER) && StringUtils.isEmpty(offer.getOwnerAccountNumber())) {
                    return THEME_STYLE_WARNING; 
                }
                return THEME_STYLE_GOOD;
            }
            if (OfferState.UNPAIRED == offer.getState()) {
                return THEME_STYLE_GOOD_HIGHLIGHT;
            }
            return null;
        } );        
        
        GridContextMenu offerContextMenu = getContextMenu();
        offerContextMenu.addGridBodyContextMenuListener(e -> {
            offerContextMenu.removeItems();
            offerContextMenu.addItem("Refresh", c -> refresh());

            Offer offer = getEntity(e.getItemId());
            if (offer!=null) {
                
                UserAccount offerUserAccount = userAccountView.get(offer.getUserAccountId());
                Set<UserBankAccount> bankAccounts = offerUserAccount.getBankAccounts();
                if (!bankAccounts.isEmpty()) {
                    MenuItem recipientBankAccountMenu = offerContextMenu.addItem("Set user bank account", null);
                    bankAccounts.stream()
                    .filter(userBankAccount -> userBankAccount.getCurrency().equalsIgnoreCase(offer.getCurrencyRequested()))
                    .forEach(userBankAccount -> {
                        MenuItem userBankAccountMenu = recipientBankAccountMenu.addItem(userBankAccount.getAccountNumber(), ubc -> {
                            gateway.send(new SetOwnerAccountNumberForOfferCommand(offer.getId(), userBankAccount.getAccountNumber()));
                        });
                        userBankAccountMenu.setCheckable(true);
                        userBankAccountMenu.setChecked(userBankAccount.getAccountNumber().equals(offer.getOwnerAccountNumber()));
                    });                    
                }
                
                if (offer.getState()==OfferState.PAYMENT_RECEIVED) {
                    offerContextMenu.addItem("Send money to user", c -> gateway.send(new RequestOfferPaymentCommand(offer.getId())));                    
                }
                
                if (offer.getState()==OfferState.UNPAIRED) {
                    offerContextMenu.addItem("Cancel", c -> gateway.send(new CancelExchangeOfferCommand(offer.getId())));                                        
                }
            }
        });        
    }

    @PostConstruct
    private void init() {
        setSizeFull();
        this.setCaption("Offers");
    }

    @Override
    protected Class<Offer> getItemClass() {
        return Offer.class;
    }

    
}
