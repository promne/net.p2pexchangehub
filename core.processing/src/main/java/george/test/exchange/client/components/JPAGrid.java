package george.test.exchange.client.components;

import com.vaadin.addon.contextmenu.GridContextMenu;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.cdi.ViewScoped;
import com.vaadin.ui.Grid;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import george.test.exchange.client.ThemeStyles;

@ViewScoped
public abstract class JPAGrid<T> extends Grid {

    public static final String THEME_STYLE_WARNING = ThemeStyles.GRID_CELL_STYLE_WARNING;

    public static final String THEME_STYLE_GOOD = ThemeStyles.GRID_CELL_STYLE_GOOD;

    public static final String THEME_STYLE_GOOD_HIGHLIGHT = ThemeStyles.GRID_CELL_STYLE_GOOD_HIGHLIGHT;
    
    public static final String THEME_STYLE_ERROR = ThemeStyles.GRID_CELL_STYLE_ERROR;
    
    @PersistenceContext
    private EntityManager em;

    private JPAContainer<T> offersContainer;

    private GridContextMenu contextMenu;

    public JPAGrid() {
        super();
        contextMenu = new GridContextMenu(this);
        addStyleName(ThemeStyles.GRID_COLORED);
    }
    
    protected abstract Class<T> getItemClass();
    
    @PostConstruct
    private void init() {
        offersContainer = JPAContainerFactory.make(getItemClass(), em);
        offersContainer.setReadOnly(true);
        this.setContainerDataSource(offersContainer);        
    }
    
    public GridContextMenu getContextMenu() {
        return contextMenu;
    }

    public void refresh() {
        getJPAContainerDataSource().refresh();
    }
    
    public JPAContainer<T> getJPAContainerDataSource() {
        return (JPAContainer<T>) super.getContainerDataSource();
    }    
    
    public T getSelectedEntity() {
        Object itemId = getSelectedRow();
        return itemId==null ?  null : getJPAContainerDataSource().getItem(itemId).getEntity();
    }

    public T getEntity(Object itemId) {
        return itemId==null ?  null : getJPAContainerDataSource().getItem(itemId).getEntity();
    }
    
}
