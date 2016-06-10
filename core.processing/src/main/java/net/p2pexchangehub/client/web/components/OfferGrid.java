package net.p2pexchangehub.client.web.components;

import com.vaadin.addon.contextmenu.GridContextMenu;
import com.vaadin.cdi.ViewScoped;
import com.vaadin.data.Item;
import com.vaadin.data.util.PropertyValueGenerator;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;

import george.test.exchange.core.domain.UserAccountRole;
import george.test.exchange.core.processing.service.CurrencyService;
import net.p2pexchangehub.client.web.security.UserIdentity;
import net.p2pexchangehub.core.api.offer.CancelExchangeOfferCommand;
import net.p2pexchangehub.core.api.offer.RequestOfferCreditDeclineCommand;
import net.p2pexchangehub.core.api.offer.UnmatchExchangeOfferCommand;
import net.p2pexchangehub.core.api.user.CreditOfferFromUserAccountCommand;
import net.p2pexchangehub.core.handler.offer.OfferState;
import net.p2pexchangehub.core.util.ExchangeRateEvaluator;
import net.p2pexchangehub.view.domain.Offer;
import net.p2pexchangehub.view.domain.UserAccount;
import net.p2pexchangehub.view.repository.UserAccountRepository;

@ViewScoped
public class OfferGrid extends MongoGrid<Offer> {

    public static final String PROPERTY_AMOUNT_OFFERED_READABLE = "amountOfferedReadable";

    public static final String PROPERTY_AMOUNT_REQUESTED_READABLE = "amountRequestedReadable";

    public static final String PROPERTY_EXCHANGE_RATE_READABLE = "exchangeRateReadable";

    public static final String PROPERTY_ACTION_CUSTOM = "customActionGenerated";

    @Inject
    private CommandGateway gateway;
    
    @Inject
    private UserAccountRepository userAccountRepository;

    @Inject
    private UserIdentity userIdentity;

    @Inject
    private ExchangeRateEvaluator exchangeRateEvaluator;
    
    @Inject
    private CurrencyService currencyService;
    
    public OfferGrid() {
        super(Offer.class);

        setCellStyleGenerator(cellRef -> {
            Offer offer = getEntity(cellRef.getItemId());
            if (Arrays.asList(OfferState.EXCHANGE_COMPLETE, OfferState.DEBIT_REQUESTED, OfferState.WAITING_FOR_PAYMENT, OfferState.PAYED).contains(offer.getState())) {
                return THEME_STYLE_GOOD;
            }
            if (OfferState.UNPAIRED == offer.getState()) {
                return THEME_STYLE_GOOD_HIGHLIGHT;
            }
            if (Arrays.asList(OfferState.CLOSED, OfferState.CANCELED).contains(offer.getState())) {
                return null;
            }
            return THEME_STYLE_ERROR;
        } );        
        
        GridContextMenu offerContextMenu = getContextMenu();
        offerContextMenu.addGridBodyContextMenuListener(e -> {
            offerContextMenu.removeItems();
            offerContextMenu.addItem("Refresh", c -> refresh());

            Offer offer = getEntity(e.getItemId());
            if (offer!=null && userIdentity.hasAnyRole(UserAccountRole.ADMIN, UserAccountRole.TRADER)) {
                boolean isOfferOwner = userIdentity.getUserAccountId().equals(offer.getUserAccountId());
                
                
                if (offer.getState()==OfferState.UNPAIRED && (isOfferOwner || userIdentity.hasAnyRole(UserAccountRole.ADMIN))) {
                    offerContextMenu.addItem("Cancel", c -> gateway.send(new CancelExchangeOfferCommand(offer.getId())));                                        
                }
                if (offer.getState().equals(OfferState.WAITING_FOR_PAYMENT) && (isOfferOwner || userIdentity.hasAnyRole(UserAccountRole.ADMIN))) {
                    UserAccount offerOwnerUserAccount = userAccountRepository.findOne(offer.getUserAccountId());
                    if (offerOwnerUserAccount.getWallet().stream().anyMatch(w -> w.getCurrency().equals(offer.getCurrencyOffered()) && w.getAmount().compareTo(offer.getAmountOffered())>=0)) {
                        offerContextMenu.addItem("Charge money", c -> gateway.send(new CreditOfferFromUserAccountCommand(offer.getId())));
                    }
                }
                if (userIdentity.hasRole(UserAccountRole.ADMIN)) {
                    if (offer.getState().equals(OfferState.PAYED)) {
                        offerContextMenu.addItem("Discharge money", c -> gateway.send(new RequestOfferCreditDeclineCommand(offer.getId())));
                    }                                                    
                    if (offer.getState().equals(OfferState.WAITING_FOR_PAYMENT)) {
                        offerContextMenu.addItem("Unmatch", c -> gateway.send(new UnmatchExchangeOfferCommand(offer.getId())));
                    }                                                    
                }
            }
        });        
    }

    @PostConstruct
    private void init() {
        setSizeFull();
        this.setCaption("Offers");
        
        getGeneratedPropertyContainer().addGeneratedProperty(PROPERTY_AMOUNT_OFFERED_READABLE, new PropertyValueGenerator<String>() {
            @Override
            public String getValue(Item item, Object itemId, Object propertyId) {
                Offer offer = getEntity(itemId);
                String amountString;
                if (offer.getAmountOffered()!=null) {
                    amountString = offer.getAmountOffered().toPlainString();
                } else if (offer.getAmountOfferedMin().compareTo(offer.getAmountOfferedMax())==0) {
                    amountString = offer.getAmountOfferedMin().toPlainString();
                } else {
                    amountString = String.format("%s - %s", offer.getAmountOfferedMin().toPlainString(), offer.getAmountOfferedMax().toPlainString());
                }
                return amountString + " " + offer.getCurrencyOffered();
            }
            
            @Override
            public Class<String> getType() {
                return String.class;
            }
        });        

        getGeneratedPropertyContainer().addGeneratedProperty(PROPERTY_AMOUNT_REQUESTED_READABLE, new PropertyValueGenerator<String>() {
            @Override
            public String getValue(Item item, Object itemId, Object propertyId) {
                Offer offer = getEntity(itemId);
                String amountString;
                if (offer.getAmountRequested()!=null) {
                    amountString = offer.getAmountRequested().toPlainString();
                } else {
                    BigDecimal exchangeRate = exchangeRateEvaluator.evaluate(offer.getRequestedExchangeRateExpression()).setScale(4);
                    String requestedAmountExchangedMin = currencyService.calculateExchangePay(offer.getAmountOfferedMin(), offer.getCurrencyOffered(), offer.getCurrencyRequested(), exchangeRate).toPlainString();
                    if (offer.getAmountOfferedMin().compareTo(offer.getAmountOfferedMax())==0) {
                        amountString = requestedAmountExchangedMin;
                    } else {
                        String requestedAmountExchangedMax = currencyService.calculateExchangePay(offer.getAmountOfferedMax(), offer.getCurrencyOffered(), offer.getCurrencyRequested(), exchangeRate).toPlainString();
                        amountString = String.format("%s - %s", requestedAmountExchangedMin, requestedAmountExchangedMax);
                    }
                }
                return amountString + " " + offer.getCurrencyRequested();
            }
            
            @Override
            public Class<String> getType() {
                return String.class;
            }
        });        

        getGeneratedPropertyContainer().addGeneratedProperty(PROPERTY_EXCHANGE_RATE_READABLE, new PropertyValueGenerator<BigDecimal>() {
            @Override
            public BigDecimal getValue(Item item, Object itemId, Object propertyId) {
                Offer offer = getEntity(itemId);
                if (offer.getExchangeRate()!=null) {
                    return offer.getExchangeRate();
                } else {
                    return exchangeRateEvaluator.evaluate(offer.getRequestedExchangeRateExpression()).setScale(4);
                }
            }
            
            @Override
            public Class<BigDecimal> getType() {
                return BigDecimal.class;
            }
        });        
        getGeneratedPropertyContainer().addGeneratedProperty(PROPERTY_ACTION_CUSTOM, new ConstantPropertyValueGenerator<>("Action"));

        
        Map<String, String> columnTranslationMap = new HashMap<>();
        columnTranslationMap.put(PROPERTY_AMOUNT_OFFERED_READABLE, "Offered");
        columnTranslationMap.put(PROPERTY_AMOUNT_REQUESTED_READABLE, "Requested");
        columnTranslationMap.put(PROPERTY_EXCHANGE_RATE_READABLE, "Exchange rate");
        columnTranslationMap.put(PROPERTY_ACTION_CUSTOM, "Action");

        for (Map.Entry<String, String> columnTranslation : columnTranslationMap.entrySet()) {
            Column column = getColumn(columnTranslation.getKey());
            column.setHeaderCaption(columnTranslation.getValue());
        }
    }

    @Override
    protected Class<Offer> getItemClass() {
        return Offer.class;
    }

    
}
