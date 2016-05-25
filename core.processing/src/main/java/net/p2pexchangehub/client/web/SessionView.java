package net.p2pexchangehub.client.web;

import com.vaadin.cdi.CDIView;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.converter.DateToLongConverter;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.DateRenderer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import net.p2pexchangehub.client.web.tools.SessionCollector;

@CDIView(SessionView.VIEW_NAME)
public class SessionView extends VerticalLayout implements View {

    public static final String VIEW_NAME = "SesisonView";
    
    @Inject
    private SessionCollector sessionCollector;
    
    private BeanContainer<String, HttpSession> sessionContainer = new BeanContainer<>(HttpSession.class);
    private Grid sessionGrid;
    
    @PostConstruct
    private void init() {
        setSizeFull();
        
        sessionContainer.setBeanIdResolver(session -> session.getId());
        
        GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(sessionContainer);
        
        sessionGrid = new Grid("Active sessions", gpc);
        sessionGrid.setSizeFull();
        
        sessionGrid.getColumn("lastAccessedTime").setRenderer(new DateRenderer(), new DateToLongConverter());
        sessionGrid.getColumn("creationTime").setRenderer(new DateRenderer(), new DateToLongConverter());
        
        sessionGrid.setColumns("lastAccessedTime", "creationTime", "id");
        sessionGrid.sort("lastAccessedTime", SortDirection.DESCENDING);
        
        addComponent(sessionGrid);
    }

    private void refreshSessions() {
        sessionContainer.removeAllItems();
        sessionContainer.addAll(sessionCollector.getSessions().values());
    }
    
    @Override
    public void enter(ViewChangeEvent event) {
        refreshSessions();
    }
    
}
