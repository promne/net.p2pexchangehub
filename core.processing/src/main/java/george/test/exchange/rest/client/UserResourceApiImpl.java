package george.test.exchange.rest.client;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.BigDecimalConverter;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.gateway.CommandGateway;

import es.command.CancelExchangeOfferCommand;
import es.command.CreateOfferCommand;
import es.command.CreateUserAccountCommand;
import es.command.MatchExchangeOfferCommand;
import es.command.SetOwnerAccountNumberForOfferCommand;
import es.command.SetUserAccountPasswordCommand;
import esw.domain.BankAccount;
import esw.domain.Offer;
import esw.domain.UserAccount;
import esw.view.BankAccountView;
import esw.view.OfferView;
import esw.view.UserAccountView;
import george.test.exchange.core.domain.UserAccountRole;
import george.test.exchange.core.domain.offer.OfferState;
import george.test.exchange.rest.client.security.AllowAll;
import george.test.exchange.rest.client.security.AllowRoles;
import george.test.exchange.rest.client.security.UserIdentity;

@Path("rest")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Stateless
public class UserResourceApiImpl {

    @Inject
    private CommandGateway gateway;
    
    @Inject
    private UserAccountView userAccountView;

    @Inject
    private BankAccountView bankAccountView;

    @Inject
    private OfferView offerView;
    
    @Resource
    private WebServiceContext webServiceContext;
    
    @Context
    private HttpServletRequest httpServletRequest;

    @Inject 
    private UserIdentity userIdentity;    
    
    private final BeanUtilsBean beanUtilsBean;
    
    public UserResourceApiImpl() {
        super();
        beanUtilsBean = new BeanUtilsBean(new ConvertUtilsBean() {
            
            @Override
            public Converter lookup(Class<?> clazz) {
                if (clazz.isEnum()) {
                    return new Converter() {
                        @Override
                        public Object convert(Class type, Object value) {
                            return Enum.valueOf(type, value.toString());
                        }
                    };
                } else {
                    return super.lookup(clazz);                    
                }
            }

        });
        beanUtilsBean.getConvertUtils().deregister(BigDecimal.class);
        beanUtilsBean.getConvertUtils().register(new BigDecimalConverter(null), BigDecimal.class);
    }

    @POST
    @Path("userAccount")
    @AllowAll
    public String createUser(george.test.exchange.rest.client.domain.UserAccount userAccount) {
        String userAccountId = UUID.randomUUID().toString();
        gateway.send(new CreateUserAccountCommand(userAccountId , userAccount.getUsername()));
        gateway.send(new SetUserAccountPasswordCommand(userAccountId , userAccount.getPassword()));        
        return userAccountId;
    }
    
    @GET
    @Path("userAccount")
    public george.test.exchange.rest.client.domain.UserAccount getUser() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        UserAccount userAccount = userIdentity.getUserAccount();
        
        george.test.exchange.rest.client.domain.UserAccount result = new george.test.exchange.rest.client.domain.UserAccount();
        for (String property : Arrays.asList(UserAccount.PROPERTY_ID, UserAccount.PROPERTY_USERNAME)) {
            beanUtilsBean.copyProperty(result, property, BeanUtils.getProperty(userAccount, property));            
        }
        return result;
    }

    @DELETE
    @Path("userAccount/offers/{offerId}")
    @AllowRoles(UserAccountRole.TRADER)
    public void cancelOffer(@PathParam("offerId") String offerId) {
        UserAccount userAccount = userIdentity.getUserAccount();
        Offer offer = offerView.get(offerId);
        if (offer != null && offer.getUserAccountId().equals(userAccount.getId())) {
            gateway.send(new CancelExchangeOfferCommand(offer.getId()));
        }
    }

    @PUT
    @Path("userAccount/offers/{offerId}")
    @AllowRoles(UserAccountRole.TRADER)
    public void updateOffer(@PathParam("offerId") String offerId, Offer offerUpdate) {
        UserAccount userAccount = userIdentity.getUserAccount();
        Offer offer = offerView.get(offerId);
        if (offer != null && offer.getUserAccountId().equals(userAccount.getId())) {
            if (StringUtils.isNotEmpty(offerUpdate.getOwnerAccountNumber())) {
                gateway.send(new SetOwnerAccountNumberForOfferCommand(offerId, offerUpdate.getOwnerAccountNumber()));
            }
        }
    }
    
    @GET
    @Path("userAccount/offers")
    public Collection<Offer> listUserOffers() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        UserAccount userAccount = userIdentity.getUserAccount();
        
        Collection<Offer> result = new ArrayList<>();
        for (Offer offer : offerView.listOffersForUser(userAccount.getId())) {
            Offer resultOffer = new Offer();
            for (String property : Arrays.asList(Offer.PROPERTY_AMOUNT_OFFERED, Offer.PROPERTY_AMOUNT_OFFERED_MAX, Offer.PROPERTY_AMOUNT_OFFERED_MIN, Offer.PROPERTY_AMOUNT_RECEIVED, 
                    Offer.PROPERTY_AMOUNT_REQUESTED, Offer.PROPERTY_AMOUNT_REQUESTED_EXCHANGE_RATE, Offer.PROPERTY_CURRENCY_OFFERED, Offer.PROPERTY_CURRENCY_REQUESTED, 
                    Offer.PROPERTY_ID, Offer.PROPERTY_INCOMING_PAYMENT_BANK_ACCOUNT_ID, Offer.PROPERTY_OWNER_ACCOUNT_NUMBER, Offer.PROPERTY_REFERENCE_ID, Offer.PROPERTY_STATE,
                    Offer.PROPERTY_USER_ACCOUNT_ID)) {
                beanUtilsBean.copyProperty(resultOffer, property, BeanUtils.getProperty(offer, property));            
            }

            //lazy to setup separate rest bean, so override content ...
            if (!StringUtils.isEmpty(offer.getIncomingPaymentBankAccountId())) {
                Optional<BankAccount> bankAccount = bankAccountView.getBankAccount(offer.getIncomingPaymentBankAccountId());
                resultOffer.setIncomingPaymentBankAccountId(bankAccount.get().getAccountNumber());
            }
            
            result.add(resultOffer);
        }
        return result;        
    }

    @POST
    @Path("userAccount/offerMatches")
    @AllowRoles(UserAccountRole.TRADER)
    public String matchOffer(Offer matchOffer) {
        UserAccount userAccount = userIdentity.getUserAccount();
        String newOfferId = UUID.randomUUID().toString();
        gateway.send(new MatchExchangeOfferCommand(newOfferId, matchOffer.getMatchedExchangeOfferId(), userAccount.getId(), matchOffer.getAmountOffered(), matchOffer.getAmountRequested()));
        return newOfferId;
    }
    
    @POST
    @Path("offers")
    @AllowRoles(UserAccountRole.TRADER)
    public String createOffer(Offer offer) {
        UserAccount userAccount = userIdentity.getUserAccount();
        String newOfferId = UUID.randomUUID().toString();
        gateway.send(new CreateOfferCommand(newOfferId, userAccount.getId(), offer.getCurrencyOffered(), offer.getAmountOfferedMin(), offer.getAmountOfferedMax(), offer.getCurrencyRequested(), offer.getAmountRequestedExchangeRate()));
        if (StringUtils.isNotEmpty(offer.getOwnerAccountNumber())) {
            gateway.send(new SetOwnerAccountNumberForOfferCommand(newOfferId, offer.getUserAccountId()));
        }        
        return newOfferId;
    }
    
    @GET
    @Path("offers")
    @AllowRoles(UserAccountRole.TRADER)
    public Collection<Offer> listOffers() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Collection<Offer> result = new ArrayList<>();
        for (Offer offer : offerView.listOffersWithState(OfferState.UNPAIRED)) {
            Offer resultOffer = new Offer();
            for (String property : Arrays.asList(Offer.PROPERTY_AMOUNT_OFFERED_MAX, Offer.PROPERTY_AMOUNT_OFFERED_MIN, Offer.PROPERTY_AMOUNT_REQUESTED_EXCHANGE_RATE, 
                    Offer.PROPERTY_CURRENCY_OFFERED, Offer.PROPERTY_CURRENCY_REQUESTED, Offer.PROPERTY_ID)) {
                beanUtilsBean.copyProperty(resultOffer, property, BeanUtils.getProperty(offer, property));            
            }
            //lazy to setup separate rest bean, so override content ...
            for (String property : Arrays.asList(Offer.PROPERTY_AMOUNT_RECEIVED, Offer.PROPERTY_AMOUNT_SENT)) {
                beanUtilsBean.setProperty(resultOffer, property, null);                            
            }
            
            if (userIdentity.getUserAccount().getId().equals(offer.getUserAccountId())) {
                resultOffer.setUserAccountId(userIdentity.getUserAccount().getId());
            }
            
            result.add(resultOffer);
        }
        return result;        
    }
    
    
}
