package net.p2pexchangehub.client.web.components;

import com.vaadin.addon.contextmenu.GridContextMenu;
import com.vaadin.addon.contextmenu.MenuItem;
import com.vaadin.cdi.ViewScoped;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.shared.data.sort.SortDirection;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;

import net.p2pexchangehub.client.web.data.StringToBigDecimalFrictionLimitConverter;
import net.p2pexchangehub.client.web.data.util.filter.InFilter;
import net.p2pexchangehub.client.web.security.UserIdentity;
import net.p2pexchangehub.core.api.offer.CancelExchangeOfferCommand;
import net.p2pexchangehub.core.api.offer.RequestOfferCreditDeclineCommand;
import net.p2pexchangehub.core.api.offer.UnmatchExchangeOfferCommand;
import net.p2pexchangehub.core.api.user.CreditOfferFromUserAccountCommand;
import net.p2pexchangehub.core.domain.UserAccountRole;
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
    
    private Set<OfferState> filteredStates = new HashSet<>();
    private Filter statesFilter = new InFilter(Offer.PROPERTY_STATE, new ArrayList<>());

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
        offerContextMenu.addGridHeaderContextMenuListener(e -> {
            offerContextMenu.removeItems();
            MenuItem menuItemFilterClosed = offerContextMenu.addItem("Show closed", c -> {
                if (c.isChecked()) {
                    addFilteredState(OfferState.CLOSED);
                } else {
                    removeFilteredState(OfferState.CLOSED);
                }
            });
            menuItemFilterClosed.setCheckable(true);
            menuItemFilterClosed.setChecked(filteredStates.contains(OfferState.CLOSED));
            
            MenuItem menuItemFilterCanceled = offerContextMenu.addItem("Show canceled", c -> {
                if (c.isChecked()) {
                    addFilteredState(OfferState.CANCELED);
                } else {
                    removeFilteredState(OfferState.CANCELED);
                }
            });
            menuItemFilterCanceled.setCheckable(true);
            menuItemFilterCanceled.setChecked(filteredStates.contains(OfferState.CANCELED));
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
                    amountString = String.format(getLocale(), "%.2f", offer.getAmountOfferedMin());
                } else {
                    amountString = String.format(getLocale(), "%.2f - %.2f", offer.getAmountOfferedMin(), offer.getAmountOfferedMax());
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
                    BigDecimal requestedAmountExchangedMin = exchangeRateEvaluator.calculateExchangePay(offer.getAmountOfferedMin(), offer.getCurrencyOffered(), offer.getCurrencyRequested(), offer.getRequestedExchangeRateExpression());
                    if (offer.getAmountOfferedMin().compareTo(offer.getAmountOfferedMax())==0) {
                        amountString = String.format(getLocale(), "%.2f", requestedAmountExchangedMin);
                    } else {
                        BigDecimal requestedAmountExchangedMax = exchangeRateEvaluator.calculateExchangePay(offer.getAmountOfferedMax(), offer.getCurrencyOffered(), offer.getCurrencyRequested(), offer.getRequestedExchangeRateExpression());
                        amountString = String.format(getLocale(), "%.2f - %.2f", requestedAmountExchangedMin, requestedAmountExchangedMax);
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
        getColumn(PROPERTY_EXCHANGE_RATE_READABLE).setConverter(new StringToBigDecimalFrictionLimitConverter());
        
        getGeneratedPropertyContainer().addGeneratedProperty(PROPERTY_ACTION_CUSTOM, new ConstantPropertyValueGenerator<>("Action"));

        setColumnsHideable();
        setVisibleColumns(Offer.PROPERTY_STATE, Offer.PROPERTY_DATE_CREATED,
                OfferGrid.PROPERTY_AMOUNT_OFFERED_READABLE, Offer.PROPERTY_CURRENCY_OFFERED, OfferGrid.PROPERTY_AMOUNT_REQUESTED_READABLE, Offer.PROPERTY_CURRENCY_REQUESTED,  
                OfferGrid.PROPERTY_EXCHANGE_RATE_READABLE, Offer.PROPERTY_REQUESTED_EXCHANGE_RATE_EXPRESSION,
                Offer.PROPERTY_REFERENCE_ID
                );        
        
        Map<String, String> columnTranslationMap = new HashMap<>();
        columnTranslationMap.put(PROPERTY_AMOUNT_OFFERED_READABLE, "Offered");
        columnTranslationMap.put(PROPERTY_AMOUNT_REQUESTED_READABLE, "Requested");
        columnTranslationMap.put(PROPERTY_EXCHANGE_RATE_READABLE, "Exchange rate");
        columnTranslationMap.put(PROPERTY_ACTION_CUSTOM, "Action");

        for (Map.Entry<String, String> columnTranslation : columnTranslationMap.entrySet()) {
            Column column = getColumn(columnTranslation.getKey());
            column.setHeaderCaption(columnTranslation.getValue());
        }
        
        setFilteredStates(Arrays.asList(OfferState.values()));
        setSortOrder(Arrays.asList(new SortOrder(Offer.PROPERTY_DATE_CREATED, SortDirection.DESCENDING)));
    }

    public Set<OfferState> getFilteredStates() {
        return Collections.unmodifiableSet(filteredStates);
    }
    
    public void addFilteredState(OfferState state) {
        if (!filteredStates.contains(state)) {
            Set<OfferState> newStates = new HashSet<>(filteredStates);
            newStates.add(state);
            setFilteredStates(newStates);
        }
    }

    public void setFilteredStates(Collection<OfferState> states) {
        getGeneratedPropertyContainer().removeContainerFilter(statesFilter);
        statesFilter = new InFilter(Offer.PROPERTY_STATE, states.stream().map(Object::toString).collect(Collectors.toSet()));
        getGeneratedPropertyContainer().addContainerFilter(statesFilter);
        this.filteredStates = new HashSet<>(states);
    }
    
    
    public void removeFilteredState(OfferState state) {
        if (filteredStates.contains(state)) {
            Set<OfferState> newStates = new HashSet<>(filteredStates);
            newStates.remove(state);
            setFilteredStates(newStates);            
        }
    }
    
    @Override
    protected Class<Offer> getItemClass() {
        return Offer.class;
    }

    
}
