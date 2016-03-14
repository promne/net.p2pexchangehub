package george.test.exchange.client;

import com.vaadin.cdi.CDIView;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;

import javax.inject.Inject;

import george.test.exchange.core.domain.entity.bank.ExternalBankAccount;
import george.test.exchange.core.processing.service.bank.BankService;

@CDIView(AccountsView.VIEW_NAME)
public class AccountsView extends VerticalLayout implements View {
    
    public static final String VIEW_NAME = "AccountsView";

    @Inject
    private BankService bankService;

    private BeanItemContainer<ExternalBankAccount> accountsContainer = new BeanItemContainer<>(ExternalBankAccount.class);
    
    public AccountsView() {
        super();
        setSizeFull();
        
        Grid externalAccountsGrid = new Grid("Accounts", accountsContainer);
        externalAccountsGrid.setSizeFull();
        addComponent(externalAccountsGrid);
        setExpandRatio(externalAccountsGrid, 1.0f);

        addComponent(new Button("Refresh", e -> refreshAccounts()));
    }

    private void refreshAccounts() {
        accountsContainer.removeAllItems();
        accountsContainer.addAll(bankService.listExternalBankAccounts());
    }
    
    @Override
    public void enter(ViewChangeEvent event) {
        refreshAccounts();
    }

}
