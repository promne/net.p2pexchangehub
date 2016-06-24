package net.p2pexchangehub.client.web.offermatch;

import com.vaadin.cdi.CDIView;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Not;
import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.data.validator.BigDecimalRangeValidator;
import com.vaadin.data.validator.NullValidator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.themes.ValoTheme;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.button.PrimaryButton;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.fields.TypedSelect;
import org.vaadin.viritin.label.MLabel;
import org.vaadin.viritin.layouts.MFormLayout;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;
import org.vaadin.viritin.ui.MNotification;

import de.steinwedel.messagebox.MessageBox;
import george.test.exchange.core.domain.UserAccountRole;
import net.p2pexchangehub.client.web.components.ConstantPropertyValueGenerator;
import net.p2pexchangehub.client.web.components.OfferGrid;
import net.p2pexchangehub.client.web.data.StringToBigDecimalFrictionLimitConverter;
import net.p2pexchangehub.client.web.form.OfferForm;
import net.p2pexchangehub.client.web.security.UserIdentity;
import net.p2pexchangehub.core.api._domain.CurrencyAmount;
import net.p2pexchangehub.core.api.offer.CreateOfferCommand;
import net.p2pexchangehub.core.api.offer.MatchExchangeOfferCommand;
import net.p2pexchangehub.core.api.user.contact.RequestContactValidationCodeCommand;
import net.p2pexchangehub.core.handler.offer.OfferState;
import net.p2pexchangehub.core.util.ExchangeRateEvaluator;
import net.p2pexchangehub.view.domain.Offer;
import net.p2pexchangehub.view.domain.UserAccount;
import net.p2pexchangehub.view.domain.UserAccountContact;
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
    private ExchangeRateEvaluator exchangeRateEvaluator;
    
    @Inject
    private OfferGrid offerGrid;
    
    private TypedSelect<String> currencyOffered;

    private TypedSelect<String> currencyRequested;
    
    private Collection<Filter> appliedFilters = new HashSet<>();
    
    public OfferMatchView() {
        super();
    }

    @PostConstruct
    private void init() {
        setSizeFull();
        offerGrid.setSizeFull();
        
        offerGrid.getGeneratedPropertyContainer().addGeneratedProperty(OfferGrid.PROPERTY_ACTION_CUSTOM, new ConstantPropertyValueGenerator<>("Buy"));
        offerGrid.getColumn(OfferGrid.PROPERTY_ACTION_CUSTOM).setRenderer(new ButtonRenderer(e -> matchWithOffer(offerGrid.getEntity(e.getItemId()))));

        offerGrid.setColumns(OfferGrid.PROPERTY_AMOUNT_OFFERED_READABLE, OfferGrid.PROPERTY_EXCHANGE_RATE_READABLE, OfferGrid.PROPERTY_AMOUNT_REQUESTED_READABLE, OfferGrid.PROPERTY_ACTION_CUSTOM);
        offerGrid.setCellStyleGenerator(null);

        offerGrid.getGeneratedPropertyContainer().addContainerFilter(new Not(new Compare.Equal(Offer.PROPERTY_USER_ACCOUNT_ID, userIdentity.getUserAccountId())));
        offerGrid.setFilteredStates(Arrays.asList(OfferState.UNPAIRED));
        
        List<String> availableCurrencies = bankAccountRepositoryHelper.listAvailableCurrencies();
        
        String requiredError = "Field has to contain a value";
        
        currencyOffered = new TypedSelect<>("I have", availableCurrencies).withValidator(new NullValidator(requiredError, false));
        currencyOffered.setNullSelectionAllowed(false);
        currencyOffered.addValueChangeListener(c -> currencySettingsChanged());

        currencyRequested = new TypedSelect<>("I want", availableCurrencies).withValidator(new NullValidator(requiredError, false));
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
        
        MFormLayout currencySelection = new MFormLayout(currencyOffered, currencyRequested).withMargin(true);        
        addComponent(currencySelection);
        addComponent(offerGrid);
        addComponent(new MHorizontalLayout(new MLabel("If you can't find anything suitable, you can"),new MButton("create your own offer", c-> createNewDialog()).withStyleName(ValoTheme.BUTTON_LINK)));
        
        setExpandRatio(offerGrid, 1.0f);
    }

    private void matchWithOffer(Offer offer) {
        if (!checkUserRole()) {
            return;
        }
        
        String rangeValidationError = String.format(getLocale(), "Value has to be between %.2f and %.2f", offer.getAmountOfferedMin(), offer.getAmountOfferedMax());
        
        MLabel limitInfoLabel = new MLabel(String.format(getLocale(), "Requested amount of %s between %.2f and %.2f with exchage rate %.4f", offer.getCurrencyOffered(), offer.getAmountOfferedMin(), offer.getAmountOfferedMax(), exchangeRateEvaluator.evaluate(offer.getRequestedExchangeRateExpression())));
        MLabel amountOfferedLabel = new MLabel();
        
        MTextField amountField = new MTextField()
            .withRequired(true).withRequiredError(rangeValidationError)
            .withConverter(new StringToBigDecimalFrictionLimitConverter(offer.getCurrencyOffered())).withConversionError(rangeValidationError)
            .withValidator(new BigDecimalRangeValidator(rangeValidationError, offer.getAmountOfferedMin(), offer.getAmountOfferedMax()));
        amountField.setEagerValidation(true);
        
        Consumer<String> updateAmountOfferedLabel = s -> {
            if (amountField.isValid()) {
                Object newAmount = amountField.getConverter().convertToModel(s, BigDecimal.class, getLocale());
                BigDecimal calculateExchangePay = exchangeRateEvaluator.calculateExchangePay((BigDecimal) newAmount, offer.getCurrencyOffered(), offer.getCurrencyRequested(), offer.getRequestedExchangeRateExpression());
                amountOfferedLabel.setValue(String.format(getLocale(),  "You have to pay %.2f %s to get %.2f %s", calculateExchangePay, offer.getCurrencyRequested(), newAmount, offer.getCurrencyOffered()));
            } else {
                amountOfferedLabel.setValue("");
            }            
        };
        
        amountField.addTextChangeListener(e -> updateAmountOfferedLabel.accept(e.getText()));
        
        amountField.setConvertedValue(offer.getAmountOfferedMax());
        updateAmountOfferedLabel.accept(amountField.getConverter().convertToPresentation(offer.getAmountOfferedMax(), String.class, getLocale()));
        
        MessageBox.create().asModal(true)
            .withCaption("Buy "+offer.getCurrencyOffered())
            .withMessage(new MVerticalLayout(limitInfoLabel, amountField, amountOfferedLabel))
            .withWidth("30%")
            .withButton(new PrimaryButton("Buy", e -> {
                BigDecimal buyOfferAmount = (BigDecimal) amountField.getConvertedValue();
                BigDecimal calculateExchangePay = exchangeRateEvaluator.calculateExchangePay(buyOfferAmount, offer.getCurrencyOffered(), offer.getCurrencyRequested(), offer.getRequestedExchangeRateExpression());
                commandGateway.sendAndWait(new MatchExchangeOfferCommand(offer.getId(), userIdentity.getUserAccountId(), calculateExchangePay, buyOfferAmount));
                Type notificationType = Type.HUMANIZED_MESSAGE;
                if (showTopupWalletReminder(offer.getCurrencyRequested(), calculateExchangePay)) {
                    notificationType = Type.TRAY_NOTIFICATION;
                }
                MNotification.show("Offer match accepted", notificationType);
                getUI().getNavigator().navigateTo("");                
            }))
            .withCancelButton()
            .open();        
    }

    private void createNewDialog() {
        if (!checkUserRole()) {
            return;
        }
        
        List<String> availableCurrencies = bankAccountRepositoryHelper.listAvailableCurrencies();

        OfferForm editor = new OfferForm(availableCurrencies, exchangeRateEvaluator);
        editor.setModalWindowTitle("Create new offer");
        editor.setSavedHandler(offer -> {
            editor.closePopup();
            commandGateway.send(new CreateOfferCommand(userIdentity.getUserAccountId(), offer.getCurrencyOffered(), offer.getAmountOfferedMin(), offer.getAmountOfferedMax(), offer.getCurrencyRequested(), offer.getExchangeRate().toPlainString()));
            showTopupWalletReminder(offer.getCurrencyOffered(), offer.getAmountOfferedMax());
            getUI().getNavigator().navigateTo("");
        });

        Offer newOffer = new Offer();
        if (currencyOffered.isValid()) {
            newOffer.setCurrencyOffered(currencyOffered.getValue());
        }
        if (currencyRequested.isValid()) {
            newOffer.setCurrencyRequested(currencyRequested.getValue());
        }
        editor.setEntity(newOffer);
        
        editor.openInModalPopup();
    }    
    
    private boolean showTopupWalletReminder(String currency, BigDecimal amount) {
        CurrencyAmount walletAmount = userIdentity.getUserAccount().get().getWalletAmount(currency);
        if (walletAmount.getAmount().compareTo(amount)>=0) {
            return false; //have enough money in the wallet
        }
        
        UserAccount userAccount = userIdentity.getUserAccount().get();
        String incomingBankAccountInstructions = bankAccountRepositoryHelper.getIncomingPaymentInstructions(currency, userIdentity.getUserAccountId());
        
        String topupWalletMessage = 
                "<p>At the moment your wallet balance shows you don't have enough money to finish this transaction."
                + " Your pledge will stay available, but to be able to close the transaction you need to top-up your wallet.</p>"
                + "<p>To do that, you need to send %1$.2f %2$s to the following account:</p>"
                + "<p><center><strong>%3$s</strong></center></p>"
                + "<p>For us to recognize it's a payment from you, please include your payment code <strong>%4$s</strong> as a reference.</p>"
                ;
        
        topupWalletMessage = String.format(topupWalletMessage, amount, currency, incomingBankAccountInstructions, userAccount.getPaymentsCode());
        MessageBox.create().withCaption("Charge up your wallet")
            .withHtmlMessage(topupWalletMessage)
            .withButton(new PrimaryButton("OK")).asModal(true).open();
        return true;
    }
    
    private void currencySettingsChanged() {
        String cOffered = currencyOffered.isValid() ? currencyOffered.getValue() : "INVALID";
        String cRequested = currencyRequested.isValid() ? currencyRequested.getValue() : "INVALID";

        Collection<Filter> newFilters = Arrays.asList(
                new Compare.Equal(Offer.PROPERTY_CURRENCY_OFFERED, cRequested),
                new Compare.Equal(Offer.PROPERTY_CURRENCY_REQUESTED, cOffered)
                );
        
        appliedFilters.forEach(offerGrid.getGeneratedPropertyContainer()::removeContainerFilter);        
        newFilters.forEach(offerGrid.getGeneratedPropertyContainer()::addContainerFilter);
        appliedFilters = newFilters;
    }
    
    /*
     * checks user role and returns true if ok, otherwise false & show error encouraging to validate email. 
     */
    private boolean checkUserRole() {
        boolean hasRole = userIdentity.hasRole(UserAccountRole.TRADER);
        if (!hasRole) {
            List<UserAccountContact> emails = userIdentity.getUserAccount().get().getContacts(net.p2pexchangehub.view.domain.UserAccountContact.Type.EMAIL).stream().filter(ec -> !ec.isValidated()).collect(Collectors.toList());
            if (emails.isEmpty()) {
                MessageBox
                .createWarning()
                .asModal(true)
                .withMessage("You don't have the permission to do this. If you think that's a mistake, please contact our helpdesk.")
                .withButton(new PrimaryButton("OK"))
                .open();                                
            } else {
                MessageBox
                .createInfo()
                .asModal(true)
                .withMessage("You can't do this yet. It looks like your email address hasn't been validated and you have to do that first. Please go back and check your mailbox or we can send you a new validation code.")
                .withButton(new PrimaryButton("Back"))
                .withButton(new MButton("Resend validation", c-> {
                    emails.forEach(mc -> commandGateway.send(new RequestContactValidationCodeCommand(userIdentity.getUserAccountId(), mc.getValue())));
                }))
                .open();                
            }
            
        }        
        return hasRole;
    }
    
    @Override
    public void enter(ViewChangeEvent event) {
        currencySettingsChanged();
    }

}
