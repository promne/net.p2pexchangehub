package net.p2pexchangehub.client.web.offermatch;

import com.vaadin.cdi.CDIView;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Not;
import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.data.validator.NullValidator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.themes.ValoTheme;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.TypedSelect;
import org.vaadin.viritin.label.MLabel;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;
import org.vaadin.viritin.layouts.MWindow;

import de.steinwedel.messagebox.MessageBox;
import net.p2pexchangehub.client.web.components.ConstantPropertyValueGenerator;
import net.p2pexchangehub.client.web.components.OfferGrid;
import net.p2pexchangehub.client.web.form.OfferForm;
import net.p2pexchangehub.client.web.security.UserIdentity;
import net.p2pexchangehub.core.api._domain.CurrencyAmount;
import net.p2pexchangehub.core.api.offer.CreateOfferCommand;
import net.p2pexchangehub.core.handler.offer.OfferState;
import net.p2pexchangehub.view.domain.Offer;
import net.p2pexchangehub.view.domain.UserAccount;
import net.p2pexchangehub.view.repository.BankAccountRepositoryHelper;

@CDIView(OfferMatchView.VIEW_NAME)
// @RolesAllowed - everyone
public class OfferMatchView extends MVerticalLayout implements View {

    public static final String VIEW_NAME = "OfferMatchView";
    
    @Inject
    private BankAccountRepositoryHelper bankAccountRepositoryHelper;
    
    @Inject
    private CommandGateway commandGateway;
    
    @Inject
    private UserIdentity userIdentity;
    
    @Inject
    private OfferGrid offerGrid;
    
    private TypedSelect<String> currencyOffered;

    private TypedSelect<String> currencyRequested;
    
    
    public OfferMatchView() {
        super();
    }

    @PostConstruct
    private void init() {
        setSizeFull();
        offerGrid.setSizeFull();
        
        offerGrid.getGeneratedPropertyContainer().addGeneratedProperty(OfferGrid.PROPERTY_ACTION_CUSTOM, new ConstantPropertyValueGenerator<>("Match"));
        offerGrid.getColumn(OfferGrid.PROPERTY_ACTION_CUSTOM).setRenderer(new ButtonRenderer(e -> matchWithOffer(offerGrid.getEntity(e.getItemId()))));

        offerGrid.setColumns(OfferGrid.PROPERTY_AMOUNT_OFFERED_READABLE, OfferGrid.PROPERTY_EXCHANGE_RATE_READABLE, OfferGrid.PROPERTY_AMOUNT_REQUESTED_READABLE, OfferGrid.PROPERTY_ACTION_CUSTOM);
        offerGrid.setCellStyleGenerator(null);
        
        List<String> availableCurrencies = bankAccountRepositoryHelper.listAvailableCurrencies();
        
        String requiredError = "Field has to contain a value";
        
        currencyOffered = new TypedSelect<>("I have currency", availableCurrencies).withValidator(new NullValidator(requiredError, false));
        currencyOffered.setNullSelectionAllowed(false);
        currencyOffered.addValueChangeListener(c -> currencySettingsChanged());

        currencyRequested = new TypedSelect<>("I want currency", availableCurrencies).withValidator(new NullValidator(requiredError, false));
        currencyRequested.setNullSelectionAllowed(false);
        currencyRequested.addValueChangeListener(c -> currencySettingsChanged());
        currencyRequested.withValidator(new AbstractValidator<String>("Currency has to differ from offered") {
            
            @Override
            protected boolean isValidValue(String value) {
                return currencyOffered.getValue()!=value;
            }
            
            @Override
            public Class<String> getType() {
                return String.class;
            }
        });
        
        MHorizontalLayout currencySelection = new MHorizontalLayout(currencyOffered, currencyRequested).withMargin(true);        
        addComponent(currencySelection);
        addComponent(offerGrid);
        addComponent(new MHorizontalLayout(new MLabel("If you can't find anything suitable, you can"),new MButton("create your own offer", c-> createNewDialog()).withStyleName(ValoTheme.BUTTON_LINK)));
        
        setExpandRatio(offerGrid, 1.0f);
    }

    private void matchWithOffer(Offer entity) {
        // TODO Auto-generated method stub
    }

    private void createNewDialog() {
        List<String> availableCurrencies = bankAccountRepositoryHelper.listAvailableCurrencies();

        OfferForm editor = new OfferForm(availableCurrencies);
        Window window = new MWindow("Create new offer", editor).withModal(true).withResizable(false)
                .withWidth("30%"); //TODO otherwise goes screen wide
        
        editor.setSavedHandler(offer -> {
            window.close();
            commandGateway.send(new CreateOfferCommand(userIdentity.getUserAccountId(), offer.getCurrencyOffered(), offer.getAmountOfferedMin(), offer.getAmountOfferedMax(), offer.getCurrencyRequested(), offer.getRequestedExchangeRateExpression()));
            showTopupWalletReminder(offer.getCurrencyOffered(), offer.getAmountOfferedMax());
            getUI().getNavigator().navigateTo("");
        });
        
        editor.setResetHandler(offer -> {
            window.close();            
        });
        
        Offer newOffer = new Offer();
        if (currencyOffered.isValid()) {
            newOffer.setCurrencyOffered(currencyOffered.getValue());
        }
        if (currencyRequested.isValid()) {
            newOffer.setCurrencyRequested(currencyRequested.getValue());
        }
        
        editor.setEntity(newOffer);
        
        getUI().addWindow(window);
    }    
    
    private void showTopupWalletReminder(String currency, BigDecimal amount) {
        CurrencyAmount walletAmount = userIdentity.getUserAccount().get().getWalletAmount(currency);
        if (walletAmount.getAmount().compareTo(amount)>=0) {
            return; //have enough money in the wallet
        }
        
        UserAccount userAccount = userIdentity.getUserAccount().get();
        String incomingBankAccountInstructions = bankAccountRepositoryHelper.getIncomingPaymentInstructions(currency, userIdentity.getUserAccountId());
        
        String topupWalletMessage = 
                "<p>At the moment your wallet balance shows you don't have enough money to finish this deal."
                + " Your offer will stay available, but to be able to close the deal you need to top-up your wallet.</p>"
                + "<p>To do that, you need to send %s %s to the following account:</p>"
                + "<p><center><strong>%s</strong></center></p>"
                + "<p>For us to recognize it's a payment from you, please include your payment code <strong>%s</strong> as a reference.</p>"
                ;
        
        topupWalletMessage = String.format(topupWalletMessage, amount, currency, incomingBankAccountInstructions, userAccount.getPaymentsCode());
        MessageBox.create().withCaption("Charge up your wallet")
            .withHtmlMessage(topupWalletMessage)
            .withOkButton().asModal(true).open();
    }
    
    private void currencySettingsChanged() {
        String cOffered = currencyOffered.isValid() ? currencyOffered.getValue() : "INVALID";
        String cRequested = currencyRequested.isValid() ? currencyRequested.getValue() : "INVALID";

        //TODO: if adding filter fails, user will be able to see all
        offerGrid.getGeneratedPropertyContainer().removeAllContainerFilters();
        offerGrid.getGeneratedPropertyContainer().addContainerFilter(new Not(new Compare.Equal(Offer.PROPERTY_USER_ACCOUNT_ID, userIdentity.getUserAccountId())));
        offerGrid.getGeneratedPropertyContainer().addContainerFilter(new Compare.Equal(Offer.PROPERTY_STATE, OfferState.UNPAIRED.toString()));
        offerGrid.getGeneratedPropertyContainer().addContainerFilter(new Compare.Equal(Offer.PROPERTY_CURRENCY_OFFERED, cRequested));
        offerGrid.getGeneratedPropertyContainer().addContainerFilter(new Compare.Equal(Offer.PROPERTY_CURRENCY_REQUESTED, cOffered));        
    }
    
    
    @Override
    public void enter(ViewChangeEvent event) {
        currencySettingsChanged();
    }

}
