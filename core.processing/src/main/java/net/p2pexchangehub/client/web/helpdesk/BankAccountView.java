package net.p2pexchangehub.client.web.helpdesk;

import com.vaadin.addon.contextmenu.GridContextMenu;
import com.vaadin.addon.contextmenu.MenuItem;
import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.data.mongodb.core.MongoOperations;
import org.tylproject.vaadin.addon.MongoContainer;
import org.tylproject.vaadin.addon.MongoContainer.Builder;

import net.p2pexchangehub.client.web.ThemeStyles;
import net.p2pexchangehub.core.api.external.bank.RequestExternalBankSynchronizationCommand;
import net.p2pexchangehub.core.api.external.bank.SetExternalBankAccountActiveCommand;
import net.p2pexchangehub.core.api.external.bank.SetExternalBankAccountSynchronizationEnabledCommand;
import net.p2pexchangehub.core.api.external.bank.transaction.MatchIncomingExternalBankTransactionWithUserAccountCommand;
import net.p2pexchangehub.core.api.external.bank.transaction.MatchOutgoingExternalBankTransactionWithRequestedCommand;
import net.p2pexchangehub.core.domain.ExternalBankTransactionState;
import net.p2pexchangehub.view.domain.BankAccount;
import net.p2pexchangehub.view.domain.BankTransaction;
import net.p2pexchangehub.view.repository.UserAccountRepository;

@CDIView(BankAccountView.VIEW_NAME)
@RolesAllowed("admin")
public class BankAccountView extends VerticalLayout implements View {
    
    public static final String VIEW_NAME = "BankAccountView";

    private MongoContainer<BankAccount> accountsContainer;

    private MongoContainer<BankTransaction> transactionsContainer;

    @Inject
    private MongoOperations mongoOperations;
    
    @Inject
    private UserAccountRepository userAccountView;
    
    @Inject
    CommandGateway gateway;

    private Grid accountsGrid;

    private Grid transactionsGrid;
    
    @PostConstruct
    private void init() {
        setSizeFull();

        accountsContainer = MongoContainer.Builder.forEntity(BankAccount.class, mongoOperations).buildBuffered();

        transactionsContainer = getTransactionContainerBuilder().buildBuffered();
        
        VerticalSplitPanel gridsLayout = new VerticalSplitPanel();
        gridsLayout.setMinSplitPosition(10, Unit.PERCENTAGE);
        gridsLayout.setSizeFull();
        
        accountsGrid = new Grid("Accounts", accountsContainer);
        accountsGrid.setSelectionMode(SelectionMode.SINGLE);
//        accountsGrid.addSelectionListener(e -> {
//            transactionsContainer.removeAllContainerFilters();
//            Builder<BankTransaction> transactionContainerBuilder = getTransactionContainerBuilder();
//            for (Object selectedItemId : e.getSelected()) {
//                //works only for single selection
//                transactionContainerBuilder.forCriteria(Criteria.where(BankTransaction.PROPERTY_BANK_ACCOUNT + "." + BankAccount.PROPERTY_ID).is(accountsContainer.getItem(selectedItemId).getBean().getId()));
//            }
//            transactionsContainer = transactionContainerBuilder.buildBuffered();
//            transactionsGrid.setContainerDataSource(transactionsContainer);
//        });
        

        GridContextMenu accountMenu = new GridContextMenu(accountsGrid);
        accountMenu.addGridBodyContextMenuListener(e -> {
            accountMenu.removeItems();
            Object itemId = e.getItemId();
            accountMenu.addItem("Refresh", c -> refreshAccounts());
            if (itemId!=null) {
                BankAccount account = accountsContainer.getItem(itemId).getBean();
                MenuItem activeItem = accountMenu.addItem("Active", c -> {
                    gateway.send(new SetExternalBankAccountActiveCommand(account.getId(), c.isChecked()));
                });
                activeItem.setCheckable(true);
                activeItem.setChecked(account.isActive());                

                MenuItem synchronizationItem = accountMenu.addItem("Synchronization enabled", c -> {
                    gateway.send(new SetExternalBankAccountSynchronizationEnabledCommand(account.getId(), c.isChecked()));
                });
                synchronizationItem.setCheckable(true);
                synchronizationItem.setChecked(account.isSynchronizationEnabled());                

                accountMenu.addItem("Synchronize", c -> gateway.send(new RequestExternalBankSynchronizationCommand(account.getId())));
            }
        });
        
        accountsGrid.setSizeFull();
        gridsLayout.addComponent(accountsGrid);
        
        
        
        transactionsGrid = new Grid("Transactions", transactionsContainer);
        transactionsGrid.setColumns(BankTransaction.PROPERTY_DATE, BankTransaction.PROPERTY_AMOUNT,
                BankTransaction.PROPERTY_BANK_ACCOUNT + "." + BankAccount.PROPERTY_CURRENCY,
                BankTransaction.PROPERTY_STATE, BankTransaction.PROPERTY_REFERENCE_INFO, BankTransaction.PROPERTY_DETAIL);
        transactionsGrid.sort(BankTransaction.PROPERTY_DATE, SortDirection.DESCENDING);
        transactionsGrid.setSizeFull();
        transactionsGrid.addStyleName(ThemeStyles.GRID_COLORED);
        transactionsGrid.setCellStyleGenerator(cellRef -> {
            switch (transactionsContainer.getItem(cellRef.getItemId()).getBean().getState()) {
                case IMPORTED:
                    return ThemeStyles.GRID_CELL_STYLE_WARNING;
                default:
                    return null;
            }
        } );        

        
        GridContextMenu transactionsMenu = new GridContextMenu(transactionsGrid);
        transactionsMenu.addGridBodyContextMenuListener(e -> {
            transactionsMenu.removeItems();
            Object itemId = e.getItemId();
            transactionsMenu.addItem("Refresh", c -> refreshAccounts());
            if (itemId!=null) {
                BankTransaction bankTransaction = transactionsContainer.getItem(itemId).getBean();
                if (bankTransaction.getState() == ExternalBankTransactionState.IMPORTED) {
                    transactionsMenu.addSeparator();
                    if (bankTransaction.isIncoming()) {
                        MenuItem matchingOfferMenu = transactionsMenu.addItem("Transfer to the user", null);
                        
                        userAccountView.findAll().forEach(userAccount -> {
                            matchingOfferMenu.addItem(String.format("%s (%s)",userAccount.getUsername(), userAccount.getPaymentsCode()), c -> gateway
                                    .send(new MatchIncomingExternalBankTransactionWithUserAccountCommand(bankTransaction.getId(), userAccount.getId())));
                        });
                    } else {
                        transactionsMenu.addItem("Try to match with a user", c -> {
                            gateway.send(new MatchOutgoingExternalBankTransactionWithRequestedCommand(bankTransaction.getId()));
                        });
                    }
                }
            }
        });
        
        
        gridsLayout.addComponent(transactionsGrid);
        
        addComponent(gridsLayout);
        setExpandRatio(gridsLayout, 1.0f);

    }

    private Builder<BankTransaction> getTransactionContainerBuilder() {
        return MongoContainer.Builder.forEntity(BankTransaction.class, mongoOperations)
                .withNestedProperty(BankTransaction.PROPERTY_BANK_ACCOUNT + "." + BankAccount.PROPERTY_CURRENCY, String.class);
    }
    
    private void refreshAccounts() {
        accountsGrid.setSortOrder(new ArrayList<>(accountsGrid.getSortOrder()));
        transactionsGrid.setSortOrder(new ArrayList<>(transactionsGrid.getSortOrder()));
    }
    
    @Override
    public void enter(ViewChangeEvent event) {
        refreshAccounts();
    }

}
