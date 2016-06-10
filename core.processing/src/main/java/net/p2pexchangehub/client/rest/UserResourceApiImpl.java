package net.p2pexchangehub.client.rest;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import org.axonframework.commandhandling.gateway.CommandGateway;

import george.test.exchange.core.domain.UserAccountRole;
import net.p2pexchangehub.client.rest.security.UserIdentityRest;
import net.p2pexchangehub.client.security.AllowAll;
import net.p2pexchangehub.client.security.AllowRoles;
import net.p2pexchangehub.core.api.offer.CancelExchangeOfferCommand;
import net.p2pexchangehub.core.api.offer.CreateOfferCommand;
import net.p2pexchangehub.core.api.offer.MatchExchangeOfferCommand;
import net.p2pexchangehub.core.api.user.CreateUserAccountCommand;
import net.p2pexchangehub.core.api.user.SetUserAccountPasswordCommand;
import net.p2pexchangehub.core.handler.offer.OfferState;
import net.p2pexchangehub.view.domain.Offer;
import net.p2pexchangehub.view.domain.UserAccount;
import net.p2pexchangehub.view.repository.BankAccountRepository;
import net.p2pexchangehub.view.repository.OfferRepository;
import net.p2pexchangehub.view.repository.UserAccountRepository;

@Path("rest")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Stateless
public class UserResourceApiImpl {

    @Inject
    private CommandGateway gateway;
    
    @Inject
    private UserAccountRepository userAccountRepository;

    @Inject
    private BankAccountRepository bankAccountView;

    @Inject
    private OfferRepository offerView;
    
    @Resource
    private WebServiceContext webServiceContext;
    
    @Context
    private HttpServletRequest httpServletRequest;

    @Inject 
    private UserIdentityRest userIdentity;    
    
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
    public String createUser(net.p2pexchangehub.client.rest.domain.UserAccount userAccount) {
        String userAccountId = UUID.randomUUID().toString();
        gateway.send(new CreateUserAccountCommand(userAccountId , userAccount.getUsername()));
        gateway.send(new SetUserAccountPasswordCommand(userAccountId , userAccount.getPassword()));        
        return userAccountId;
    }
    
    @GET
    @Path("userAccount")
    public net.p2pexchangehub.client.rest.domain.UserAccount getUser() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        UserAccount userAccount = userIdentity.getUserAccount();
        
        net.p2pexchangehub.client.rest.domain.UserAccount result = new net.p2pexchangehub.client.rest.domain.UserAccount();
        for (String property : Arrays.asList(UserAccount.PROPERTY_ID, UserAccount.PROPERTY_USERNAME, UserAccount.PROPERTY_PAYMENTS_CODE, UserAccount.PROPERTY_WALLET)) {
            beanUtilsBean.copyProperty(result, property, BeanUtils.getProperty(userAccount, property));            
        }
        
        return result;
    }

    @DELETE
    @Path("userAccount/offers/{offerId}")
    @AllowRoles(UserAccountRole.TRADER)
    public void cancelOffer(@PathParam("offerId") String offerId) {
        UserAccount userAccount = userIdentity.getUserAccount();
        Offer offer = offerView.findOne(offerId);
        if (offer != null && offer.getUserAccountId().equals(userAccount.getId())) {
            gateway.send(new CancelExchangeOfferCommand(offer.getId()));
        }
    }

    
    @GET
    @Path("userAccount/offers")
    public Collection<Offer> listUserOffers() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        UserAccount userAccount = userIdentity.getUserAccount();
        
        Collection<Offer> result = new ArrayList<>();
        for (Offer offer : offerView.findByUserAccountId(userAccount.getId())) {
            Offer resultOffer = new Offer();
            for (String property : Arrays.asList(Offer.PROPERTY_AMOUNT_OFFERED, Offer.PROPERTY_AMOUNT_OFFERED_MAX, Offer.PROPERTY_AMOUNT_OFFERED_MIN, 
                    Offer.PROPERTY_AMOUNT_REQUESTED, Offer.PROPERTY_REQUESTED_EXCHANGE_RATE_EXPRESSION, Offer.PROPERTY_CURRENCY_OFFERED, Offer.PROPERTY_CURRENCY_REQUESTED, 
                    Offer.PROPERTY_ID, Offer.PROPERTY_REFERENCE_ID, Offer.PROPERTY_STATE,
                    Offer.PROPERTY_USER_ACCOUNT_ID)) {
                beanUtilsBean.copyProperty(resultOffer, property, BeanUtils.getProperty(offer, property));            
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
        gateway.send(new CreateOfferCommand(newOfferId, userAccount.getId(), offer.getCurrencyOffered(), offer.getAmountOfferedMin(), offer.getAmountOfferedMax(), offer.getCurrencyRequested(), offer.getRequestedExchangeRateExpression()));
        return newOfferId;
    }
    
    @GET
    @Path("offers")
    @AllowRoles(UserAccountRole.TRADER)
    public Collection<Offer> listOffers() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Collection<Offer> result = new ArrayList<>();
        for (Offer offer : offerView.findByState(OfferState.UNPAIRED)) {
            Offer resultOffer = new Offer();
            for (String property : Arrays.asList(Offer.PROPERTY_AMOUNT_OFFERED_MAX, Offer.PROPERTY_AMOUNT_OFFERED_MIN, Offer.PROPERTY_REQUESTED_EXCHANGE_RATE_EXPRESSION, 
                    Offer.PROPERTY_CURRENCY_OFFERED, Offer.PROPERTY_CURRENCY_REQUESTED, Offer.PROPERTY_ID)) {
                beanUtilsBean.copyProperty(resultOffer, property, BeanUtils.getProperty(offer, property));            
            }
            
            if (userIdentity.getUserAccount().getId().equals(offer.getUserAccountId())) {
                resultOffer.setUserAccountId(userIdentity.getUserAccount().getId());
            }
            
            result.add(resultOffer);
        }
        return result;        
    }
    
    
}
