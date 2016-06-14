package net.p2pexchangehub.client.web.components;

import com.vaadin.addon.contextmenu.GridContextMenu;
import com.vaadin.cdi.ViewScoped;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.ui.Grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.data.mongodb.core.MongoOperations;
import org.tylproject.vaadin.addon.MongoContainer;
import org.tylproject.vaadin.addon.MongoContainer.Builder;

import net.p2pexchangehub.client.web.ThemeStyles;
import net.p2pexchangehub.client.web.data.util.filter.MongoFilterConverter;

@ViewScoped
public abstract class MongoGrid<T> extends Grid {

    public static final String THEME_STYLE_WARNING = ThemeStyles.GRID_CELL_STYLE_WARNING;

    public static final String THEME_STYLE_GOOD = ThemeStyles.GRID_CELL_STYLE_GOOD;

    public static final String THEME_STYLE_GOOD_HIGHLIGHT = ThemeStyles.GRID_CELL_STYLE_GOOD_HIGHLIGHT;
    
    public static final String THEME_STYLE_ERROR = ThemeStyles.GRID_CELL_STYLE_ERROR;
    
    private final Class<T> clazz;
    
    @Inject
    private MongoOperations mongoOperations;

    private MongoContainer<T> mongoDataContainer;

    private GeneratedPropertyContainer generatedPropertyContainer;
    
    private GridContextMenu contextMenu;

    public MongoGrid(Class<T> clazz) {
        super();
        this.clazz = clazz;
        contextMenu = new GridContextMenu(this);
        addStyleName(ThemeStyles.GRID_COLORED);
        setColumnReorderingAllowed(true);
    }
    
    protected abstract Class<T> getItemClass();
    
    @PostConstruct
    private void init() {
        mongoDataContainer = adjustMongoBuilder(MongoContainer.Builder.forEntity(clazz, mongoOperations)).buildBuffered();
        generatedPropertyContainer = new GeneratedPropertyContainer(mongoDataContainer);
        this.setContainerDataSource(generatedPropertyContainer);        
    }

    protected Builder<T> adjustMongoBuilder(Builder<T> builder) {
        return builder.withFilterConverter(new MongoFilterConverter());
    }
    
    public GridContextMenu getContextMenu() {
        return contextMenu;
    }

    public void refresh() {
        //mongocontainer is lazy, we need to trick grid to refresh cached data
        super.setSortOrder(new ArrayList<>(super.getSortOrder()));
    }
    
    public MongoContainer<T> getMongoContainerDataSource() {
        return mongoDataContainer;
    }    
    
    public GeneratedPropertyContainer getGeneratedPropertyContainer() {
        return generatedPropertyContainer;
    }
    
    public T getSelectedEntity() {
        Object itemId = getSelectedRow();
        return itemId==null ?  null : getMongoContainerDataSource().getItem(itemId).getBean();
    }

    public T getEntity(Object itemId) {
        return itemId==null ?  null : getMongoContainerDataSource().getItem(itemId).getBean();
    }

    @Override
    public void setColumns(Object... propertyIds) {
        super.setColumns(propertyIds);
        getColumns().forEach(c -> c.setHidable(true));
        setVisibleColumns(propertyIds);
    }

    public void setVisibleColumns(Object... propertyIds) {
        Collection<Object> propIds = Arrays.asList(propertyIds);
        setColumnOrder(propertyIds);
        getColumns().forEach(c -> c.setHidden(!propIds.contains(c.getPropertyId())));
    }
    
}
