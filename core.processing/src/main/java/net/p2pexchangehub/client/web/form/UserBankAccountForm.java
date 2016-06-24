package net.p2pexchangehub.client.web.form;

import com.vaadin.data.validator.NullValidator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Component;

import java.util.List;

import org.vaadin.viritin.MBeanFieldGroup;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.fields.TypedSelect;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.layouts.MFormLayout;

import net.p2pexchangehub.core.handler.external.bank.ExternalBankAccountNumberValidator;
import net.p2pexchangehub.view.domain.UserBankAccount;

public class UserBankAccountForm extends AbstractForm<UserBankAccount> {

    private TypedSelect<String> currency;
    
    private MTextField accountNumber;
    
    private MTextField ownerName;

    private ExternalBankAccountNumberValidator numberValidator;
    
    public UserBankAccountForm(ExternalBankAccountNumberValidator numberValidator, List<String> availableCurrencies) {
        super();
        
        setResetHandler(offer -> {
            UserBankAccountForm.this.closePopup();
        });
        
        this.numberValidator = numberValidator;
        
        String requiredError = "Field has to contain a value";
        
        currency = new TypedSelect<>("Currency", availableCurrencies).withValidator(new NullValidator(requiredError, false));
        currency.setNullSelectionAllowed(false);
        currency.addMValueChangeListener(l -> {
            setBankAccountFormat(l.getValue());
        });
                
        accountNumber = new MTextField("Number").withFullWidth()
                .withRequired(true).withRequiredError(requiredError);
        ownerName = new MTextField("Owner name").withFullWidth()
                .withRequired(true).withRequiredError(requiredError);
    }

    @Override
    protected Component createContent() {
        MFormLayout layout = new MFormLayout(currency, accountNumber, ownerName, getToolbar())
                .withMargin(true);
        layout.setSizeUndefined();
        return layout;
    }

    protected void setBankAccountFormat(String currency) {
        String regex = numberValidator.getValidationPattern(currency);
        RegexpValidator validator = new RegexpValidator(regex, "Field has to contain a valid bank account number");
        accountNumber.removeAllValidators();
        accountNumber.withValidator(validator);
        // workaround - force validation by making text value change
        // https://github.com/viritin/viritin/issues/188
        String value = accountNumber.getValue();
        accountNumber.setValue("");
        accountNumber.setValue(value);
    }

    @Override
    public MBeanFieldGroup<UserBankAccount> setEntity(UserBankAccount entity) {
        boolean newOne = entity==null || entity.getCurrency()==null;
        if (!newOne) {
            setBankAccountFormat(entity.getCurrency());            
        }
        currency.withReadOnly(!newOne);
        accountNumber.withReadOnly(!newOne);
            
        return super.setEntity(entity);
    }
    
}
