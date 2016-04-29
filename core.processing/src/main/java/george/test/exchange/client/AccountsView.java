package george.test.exchange.client;

import com.vaadin.addon.contextmenu.GridContextMenu;
import com.vaadin.addon.contextmenu.MenuItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.cdi.CDIView;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.axonframework.commandhandling.gateway.CommandGateway;

import es.command.SetExternalBankAccountActiveCommand;
import es.command.SynchronizeExternalBankTransactionsCommand;
import esw.domain.BankAccount;
import esw.domain.BankTransaction;

@CDIView(AccountsView.VIEW_NAME)
public class AccountsView extends VerticalLayout implements View {
    
    public static final String VIEW_NAME = "AccountsView";

    private JPAContainer<BankAccount> accountsContainer;

    private JPAContainer<BankTransaction> transactionsContainer;

    @PersistenceContext
    private EntityManager em;
    
    @Inject
    CommandGateway gateway;
    
    @PostConstruct
    private void init() {
        setSizeFull();

        accountsContainer = JPAContainerFactory.make(BankAccount.class, em);
        accountsContainer.setReadOnly(true);

        transactionsContainer = JPAContainerFactory.make(BankTransaction.class, em);
        transactionsContainer.setReadOnly(true);
        
        VerticalLayout gridsLayout = new VerticalLayout();
        gridsLayout.setSizeFull();
        
        Grid accountsGrid = new Grid("Accounts", accountsContainer);
        accountsGrid.setSelectionMode(SelectionMode.SINGLE);
        accountsGrid.addSelectionListener(e -> {
            transactionsContainer.removeAllContainerFilters();
            e.getSelected().forEach(itemId -> transactionsContainer.addContainerFilter(new Compare.Equal("bankAccount", accountsContainer.getItem(itemId).getEntity())));
        });
        accountsContainer.addListener(e -> transactionsContainer.refresh());
        

        GridContextMenu accountMenu = new GridContextMenu(accountsGrid);
        accountMenu.addGridBodyContextMenuListener(e -> {
            accountMenu.removeItems();
            Object itemId = e.getItemId();
            if (itemId!=null) {
                BankAccount account = accountsContainer.getItem(itemId).getEntity();
                MenuItem activeItem = accountMenu.addItem("Active", c -> {
                    gateway.send(new SetExternalBankAccountActiveCommand(account.getId(), c.isChecked()));
                    accountsContainer.refreshItem(account.getId());
                });
                activeItem.setCheckable(true);
                activeItem.setChecked(account.isActive());                
            }
        });
        
        accountsGrid.setSizeFull();
        gridsLayout.addComponent(accountsGrid);
        
        
        
        Grid transactionsGrid = new Grid("Transactions", transactionsContainer);
        transactionsGrid.setColumns(BankTransaction.PROPERTY_DATE, BankTransaction.PROPERTY_AMOUNT, BankTransaction.PROPERTY_STATE, BankTransaction.PROPERTY_DETAIL);
        transactionsGrid.sort(BankTransaction.PROPERTY_DATE, SortDirection.DESCENDING);
        transactionsGrid.setSizeFull();
        gridsLayout.addComponent(transactionsGrid);
        
        addComponent(gridsLayout);
        setExpandRatio(gridsLayout, 1.0f);

        
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        addComponent(buttonsLayout);
        
        buttonsLayout.addComponent(new Button("Refresh", e -> refreshAccounts()));
        buttonsLayout.addComponent(new Button("Activate/disable account", e -> {
            Object accountId = accountsGrid.getSelectedRow();
            if (accountId!=null) {                
                BankAccount account = accountsContainer.getItem(accountId).getEntity();
                boolean setValue = !account.isActive();
                gateway.send(new SetExternalBankAccountActiveCommand(account.getId(), setValue));
                account.setActive(setValue);
                Notification.show("Account active: " + setValue);
            }
        }));
        buttonsLayout.addComponent(new Button("Synchronize account", e -> {
            Object accountId = accountsGrid.getSelectedRow();
            if (accountId!=null) {                
                BankAccount account = accountsContainer.getItem(accountId).getEntity();
                gateway.send(new SynchronizeExternalBankTransactionsCommand(account.getId()));
            }
        }));
    }

    
    private void refreshAccounts() {
        accountsContainer.refresh();
        transactionsContainer.refresh();
    }
    
    @Override
    public void enter(ViewChangeEvent event) {
        refreshAccounts();
    }

}
