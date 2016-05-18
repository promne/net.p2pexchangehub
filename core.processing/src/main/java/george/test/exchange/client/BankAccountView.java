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
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.axonframework.commandhandling.gateway.CommandGateway;

import es.command.MatchExternalBankTransactionWithOfferCommand;
import es.command.SetExternalBankAccountActiveCommand;
import es.command.RequestExternalBankSynchronizationCommand;
import esw.domain.BankAccount;
import esw.domain.BankTransaction;
import esw.view.OfferView;
import george.test.exchange.core.domain.ExternalBankTransactionState;
import george.test.exchange.core.domain.offer.OfferState;

@CDIView(BankAccountView.VIEW_NAME)
public class BankAccountView extends VerticalLayout implements View {
    
    public static final String VIEW_NAME = "BankAccountView";

    private JPAContainer<BankAccount> accountsContainer;

    private JPAContainer<BankTransaction> transactionsContainer;

    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private OfferView offerView;
    
    @Inject
    CommandGateway gateway;
    
    @PostConstruct
    private void init() {
        setSizeFull();

        accountsContainer = JPAContainerFactory.make(BankAccount.class, em);
        accountsContainer.setReadOnly(true);

        transactionsContainer = JPAContainerFactory.make(BankTransaction.class, em);
        transactionsContainer.addNestedContainerProperty(BankTransaction.PROPERTY_BANK_ACCOUNT + "." + BankAccount.PROPERTY_CURRENCY);
        transactionsContainer.setReadOnly(true);
        
        VerticalSplitPanel gridsLayout = new VerticalSplitPanel();
        gridsLayout.setMinSplitPosition(10, Unit.PERCENTAGE);
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
            accountMenu.addItem("Refresh", c -> refreshAccounts());
            if (itemId!=null) {
                BankAccount account = accountsContainer.getItem(itemId).getEntity();
                MenuItem activeItem = accountMenu.addItem("Active", c -> {
                    gateway.send(new SetExternalBankAccountActiveCommand(account.getId(), c.isChecked()));
                    accountsContainer.refreshItem(account.getId());
                });
                activeItem.setCheckable(true);
                activeItem.setChecked(account.isActive());                

                accountMenu.addItem("Synchronize", c -> gateway.send(new RequestExternalBankSynchronizationCommand(account.getId())));
            }
        });
        
        accountsGrid.setSizeFull();
        gridsLayout.addComponent(accountsGrid);
        
        
        
        Grid transactionsGrid = new Grid("Transactions", transactionsContainer);
        transactionsGrid.setColumns(BankTransaction.PROPERTY_DATE, BankTransaction.PROPERTY_AMOUNT,
                BankTransaction.PROPERTY_BANK_ACCOUNT + "." + BankAccount.PROPERTY_CURRENCY,
                BankTransaction.PROPERTY_STATE, BankTransaction.PROPERTY_DETAIL);
        transactionsGrid.sort(BankTransaction.PROPERTY_DATE, SortDirection.DESCENDING);
        transactionsGrid.setSizeFull();
        transactionsGrid.addStyleName(ThemeStyles.GRID_COLORED);
        transactionsGrid.setCellStyleGenerator(cellRef -> {
            switch (transactionsContainer.getItem(cellRef.getItemId()).getEntity().getState()) {
                case PARTIAL_MATCH:
                    return ThemeStyles.GRID_CELL_STYLE_ERROR;
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
                BankTransaction bankTransaction = transactionsContainer.getItem(itemId).getEntity();
                if (bankTransaction.getState() == ExternalBankTransactionState.IMPORTED) {
                    MenuItem matchingOfferMenu = transactionsMenu.addItem("Match with offer", null);
                    
                    if (bankTransaction.isIncoming()) {
                        offerView.listOffersWithState(OfferState.WAITING_FOR_PAYMENT).stream()
                            .filter(offer -> offer.getCurrencyOffered().equalsIgnoreCase(bankTransaction.getBankAccount().getCurrency()))
                            .forEach(offer -> {
                                matchingOfferMenu.addItem(offer.getId(), c -> gateway.send(
                                        new MatchExternalBankTransactionWithOfferCommand(bankTransaction.getId(), offer.getId(), offer.getAmountOffered().subtract(offer.getAmountReceived()).min(bankTransaction.getAmount()))
                                    ));
                            });
                    } else {
                        offerView.listOffersWithState(OfferState.SEND_MONEY_REQUESTED).stream()
                            .filter(offer -> offer.getCurrencyRequested().equalsIgnoreCase(bankTransaction.getBankAccount().getCurrency()))                        
                            .forEach(offer -> {
                                matchingOfferMenu.addItem(offer.getId(), c -> gateway.send(
                                        new MatchExternalBankTransactionWithOfferCommand(bankTransaction.getId(), offer.getId(), offer.getAmountRequested().subtract(offer.getAmountSent()).min(bankTransaction.getAmount().abs()).negate())
                                    ));
                            });
                    }
                }
                
            }
        });
        
        
        gridsLayout.addComponent(transactionsGrid);
        
        addComponent(gridsLayout);
        setExpandRatio(gridsLayout, 1.0f);

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
