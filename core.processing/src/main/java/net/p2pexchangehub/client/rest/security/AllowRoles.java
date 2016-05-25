package net.p2pexchangehub.client.rest.security;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import george.test.exchange.core.domain.UserAccountRole;

@Retention (RUNTIME)
@Target({TYPE, METHOD})
public @interface AllowRoles {
    UserAccountRole[] value();
}
