package net.p2pexchangehub.client.web.tools;

import com.vaadin.cdi.CDIView;
import com.vaadin.cdi.access.AccessControl;
import com.vaadin.navigator.View;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

public class CDIViewProvider extends com.vaadin.cdi.CDIViewProvider {
    
    @Inject
    private AccessControl accessControl;

    public String getViewNameFromAnnotation(Class<? extends View> view) {
        CDIView annotation = view.getAnnotation(CDIView.class);
        if (annotation != null) {
            return annotation.value();
        }
        return null;
    }
    
    public boolean isUserHavingAccessToView(Class<? extends View> view) {
        if (view.isAnnotationPresent(CDIView.class)) {
            if (!view.isAnnotationPresent(RolesAllowed.class)) {
                // No roles defined, everyone is allowed
                return true;
            } else {
                RolesAllowed rolesAnnotation = view.getAnnotation(RolesAllowed.class);
                boolean hasAccess = accessControl
                        .isUserInSomeRole(rolesAnnotation.value());
                getLogger().log(
                        Level.FINE,
                        "Checking if user {0} is having access to {1}: {2}",
                        new Object[] { accessControl.getPrincipalName(),
                                view, Boolean.toString(hasAccess) });

                return hasAccess;
            }
        }

        // No annotation defined, everyone is allowed
        return true;
    }

    private static Logger getLogger() {
        return Logger.getLogger(CDIViewProvider.class.getCanonicalName());
    }

}
