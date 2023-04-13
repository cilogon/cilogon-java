/**
 * CILogon specific extensions to the authorization end point, so that the PHP layer can communicate that logins succeeded. Also,
 * since a trust relation exists between CILogon and any MyProxy servers, the Cert servlet has some specific code to handle that.
 * <p>Created by Jeff Gaynor<br>
 * on 4/12/23 at  5:25 PM
 */
package org.cilogon.oauth2.servlet.impl;