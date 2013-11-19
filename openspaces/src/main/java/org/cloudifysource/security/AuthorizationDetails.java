package org.cloudifysource.security;

import org.springframework.security.core.Authentication;

import java.util.Collection;

/**
 * Holds details for authorization of the active user, when using secured mode.
 *
 * @since 9.7.0
 * @version 1.0
 * @author eliranm
 *
 * User: eliranm
 * Date: 11/19/13
 * Time: 12:26 PM
 */
public interface AuthorizationDetails {

    /**
     * Initialize the authentication to determine the authorization details.
     *
     * @param authentication Obtained from the security context.
     */
    public void init(Authentication authentication);

    /**
     * Gets the permitted security roles for the active user.
     *
     * @return A collection of roles as string values.
     */
    public Collection<String> getRoles();

    /**
     * Gets the registered authorization groups for the active user.
     *
     * @return A collection of authorization groups as string values.
     */
    public Collection<String> getAuthGroups();

}
