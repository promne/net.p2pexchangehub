package net.p2pexchangehub.client.web.dialog.sendmoney;

import com.vaadin.data.validator.BigDecimalRangeValidator;
import com.vaadin.data.validator.NullValidator;
import com.vaadin.ui.Component;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.fields.TypedSelect;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.label.MLabel;
import org.vaadin.viritin.layouts.MFormLayout;

import net.p2pexchangehub.client.web.data.StringToBigDecimalFrictionLimitConverter;
import net.p2pexchangehub.view.domain.UserAccount;
import net.p2pexchangehub.view.domain.UserAccountWallet;
import net.p2pexchangehub.view.domain.UserBankAccount;

public class SendMoneyToUserForm extends AbstractForm<TransferRequest> {

    private TypedSelect<String> currency;
    
    private MLabel currencyWalletAmountLabel;
    
    private MTextField amount;
    private BigDecimalRangeValidator amountRangeValidator;

    private TypedSelect<UserBankAccount> bankAccount;
    
    public SendMoneyToUserForm(UserAccount userAccount) {
        super();
        String requiredError = "Field value has to be present";
        
        currency = new TypedSelect<>("Currency", userAccount.getWallet().stream().filter(w -> w.getAmount().signum()>0).map(UserAccountWallet::getCurrency).collect(Collectors.toSet())).withValidator(new NullValidator(requiredError, false));
        currency.setNullSelectionAllowed(false);
        currency.addMValueChangeListener(event -> {
            String newCurrencyCode = event.getValue();
            Set<UserBankAccount> userBankAccountsWithCurrency = userAccount.getBankAccounts().stream().filter(uba -> uba.getCurrency().equals(newCurrencyCode)).collect(Collectors.toSet());
            bankAccount.setBeans(userBankAccountsWithCurrency);
            bankAccount.setValue(null);
            
            BigDecimal maximalAmountNewCurrency = userAccount.getWalletAmount(newCurrencyCode).getAmount();
            
            currencyWalletAmountLabel.setValue(String.format("%.2f %s", maximalAmountNewCurrency, newCurrencyCode));
            
            amount.removeValidator(amountRangeValidator);
            amountRangeValidator = new BigDecimalRangeValidator(String.format(getLocale(), "Please enter value smaller than %.2f", maximalAmountNewCurrency), BigDecimal.ZERO, userAccount.getWalletAmount(newCurrencyCode).getAmount());
            amountRangeValidator.setMinValueIncluded(false);
            amount.withValidator(amountRangeValidator).withConverter(new StringToBigDecimalFrictionLimitConverter(newCurrencyCode));
        });
        
        currencyWalletAmountLabel = new MLabel("In your wallet is", null);
        
        amount = new MTextField("Amount you want to send")
                .withRequired(true).withRequiredError(requiredError);
        
        
        bankAccount = new TypedSelect<>(UserBankAccount.class).withCaption("Recipient bank account").withValidator(new NullValidator(requiredError, false));
        bankAccount.setNullSelectionAllowed(false);
        bankAccount.setCaptionGenerator(i -> String.format("%s (%s)", i.getAccountNumber(), i.getOwnerName()));

        
        
        setModalWindowTitle("Send money to bank account");
        setResetHandler(offer -> {
            SendMoneyToUserForm.this.closePopup();            
        });
        
        setSaveCaption("Send");
    }
    
    @Override
    protected Component createContent() {
        MFormLayout mFormLayout = new MFormLayout(currency, currencyWalletAmountLabel, amount, bankAccount, getToolbar())
                .withMargin(true);
        mFormLayout.setSizeUndefined();
        return mFormLayout;
    }

}