/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.

 */
package org.collectionspace.services.authorization;

import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.authentication.AuthN;
import org.collectionspace.authentication.CSpaceTenant;
import org.collectionspace.authentication.CSpaceUser;
import org.collectionspace.authentication.spi.AuthNContext;
import org.collectionspace.services.authorization.perms.ActionType;
import org.collectionspace.services.authorization.spi.CSpaceAuthorizationProvider;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * AuthZ is the authorization service singleton used by the services runtime
 * @author 
 */
public class AuthZ {

    /**
     * volatile is used here to assume about ordering (post JDK 1.5)
     */
    private static volatile AuthZ self = new AuthZ();
    private CSpaceAuthorizationProvider provider;
    final Log logger = LogFactory.getLog(AuthZ.class);

    private AuthZ() {
        setupProvider();
    }

    /**
     *
     * @return
     */
    public final static AuthZ get() {
        return self;
    }
    
    public static String getMethod(ActionType actionType) {
    	String result = null;
    	
    	switch (actionType) {
    	case CREATE:
    		result = "POST";
    		break;
    	case READ:
    		result = "GET";
    		break;
    	case UPDATE:
    		result = "PUT";
    		break;
    	case DELETE:
    		result = "DELETE";
    		break;
    	case RUN:
    		result = "RUN";
    		break;
    	case SEARCH:
    		result = "READ";
    		break;
    	default:
    		throw new RuntimeException(String.format("Encountered unexpected action type '%s'.",
    				actionType.value()));
    	}
    	
    	return result;
    }
    
    private void setupProvider() {
        String beanConfig = "applicationContext-authorization.xml";
        //system property is only set in test environment
        String beanConfigProp = System.getProperty("spring-beans-config");
        if (beanConfigProp != null && !beanConfigProp.isEmpty()) {
            beanConfig = beanConfigProp;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("reading beanConfig=" + beanConfig);
        }
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
                new String[]{beanConfig});
        provider = (CSpaceAuthorizationProvider) appContext.getBean("cspaceAuthorizationProvider");
        if (logger.isDebugEnabled()) {
            logger.debug("initialized the authz provider");
        }
    }

    /**
     * addPermissions add permission for given principals to access given resource
     * -action info is retrieved from the resource
     * @param res
     * @param principals
     *      * @param grant true to grant false to deny
     */
    public void addPermissions(CSpaceResource res, String[] principals, boolean grant) throws PermissionException {
        CSpaceAction action = res.getAction();
        addPermissions(res, action, principals, grant);
    }

    /**
     * addPermissions add permission for given principals to invoke given action on given resource
     * @param res
     * @parm action
     * @param principals
     * @param grant true to grant false to deny
     */
    public void addPermissions(CSpaceResource res, CSpaceAction action, String[] principals, boolean grant)
            throws PermissionException {
        provider.getPermissionManager().addPermissions(res, action, principals, grant);
        provider.clearAclCache();
    }

    /**
     * deletePermissions delete permission(s) for given resource involving given
     * principals
     * - action is retrieved from the resource
     * @param res
     * @param principals
     */
    public void deletePermissions(CSpaceResource res, String[] principals)
            throws PermissionNotFoundException, PermissionException {
        CSpaceAction action = res.getAction();
        deletePermissions(res, action, principals);
    }

    /**
     * deletePermissions delete permission(s) for given action on given resource
     * involving given principals
     * @param res
     * @param action
     * @param principals
     */
    public void deletePermissions(CSpaceResource res, CSpaceAction action, String[] principals)
            throws PermissionNotFoundException, PermissionException {
        provider.getPermissionManager().deletePermissions(res, action, principals);
        provider.clearAclCache();
    }

    /**
     * deletePermissions delete permission(s) for given resource involving any
     * principal
     * - action is retrieved from the resource if available else applicable to
     * all actions associated with the resource
     * @param res
     * @param principals
     */
    public void deletePermissions(CSpaceResource res)
            throws PermissionNotFoundException, PermissionException {
        CSpaceAction action = res.getAction();
        if (action != null) {
            deletePermissions(res, action);
        } else {
            provider.getPermissionManager().deletePermissions(res);
            provider.clearAclCache();
        }
    }

    /**
     * deletePermissions delete permission(s) for given action on given resource
     * involving given principals
     * @param res
     * @param action
     * @param principals
     */
    public void deletePermissions(CSpaceResource res, CSpaceAction action)
            throws PermissionNotFoundException, PermissionException {
        provider.getPermissionManager().deletePermissions(res, action);
        provider.clearAclCache();
    }

    /**
     * isAccessAllowed check if authenticated principal is allowed to access
     * given resource
     *  action is retrieved from the resource if available
     * @param res
     * @return
     */
    public boolean isAccessAllowed(CSpaceResource res) {
        CSpaceAction action = res.getAction();
        return isAccessAllowed(res, action);
    }

    /**
     * isAccessAllowed check if authenticated principal is allowed to invoke
     * given action on given resource
     * @param res
     * @return
     */
    public boolean isAccessAllowed(CSpaceResource res, CSpaceAction action) {
        return provider.getPermissionEvaluator().hasPermission(res, action);
    }
    
    //
    // Login as the admin of no specific tenant
    //
    public void login() {
    	String user = AuthN.SPRING_ADMIN_USER;
    	String password = AuthN.SPRING_ADMIN_PASSWORD;
    	
        HashSet<GrantedAuthority> gauths = new HashSet<GrantedAuthority>();
        gauths.add(new SimpleGrantedAuthority(AuthN.ROLE_SPRING_ADMIN_NAME)); //NOTE: Must match with value in applicationContext-authorization-test.xml (aka SPRING_SECURITY_METADATA));
        
        Authentication authRequest = new UsernamePasswordAuthenticationToken(user, password, gauths);
        SecurityContextHolder.getContext().setAuthentication(authRequest);
        if (logger.isDebugEnabled()) {
            logger.debug("Spring Security login successful for user=" + user);
        }
    }
    
    //
    // Login as the admin for a specific tenant
    //
    public void login(CSpaceTenant tenant) {
    	String user = AuthN.SPRING_ADMIN_USER;
    	String password = AuthN.SPRING_ADMIN_PASSWORD;
    	    	
    	HashSet<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();
    	grantedAuthorities.add(new SimpleGrantedAuthority(AuthN.ROLE_SPRING_ADMIN_NAME));
    	
    	HashSet<CSpaceTenant> tenantSet = new HashSet<CSpaceTenant>();
    	tenantSet.add(tenant);
    	CSpaceUser principal = new CSpaceUser(user, password, tenantSet, grantedAuthorities);
    	
        Authentication authRequest = new UsernamePasswordAuthenticationToken(principal, password, grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authRequest);
        if (logger.isDebugEnabled()) {
            logger.debug("Spring Security login successful for user=" + user);
        }
    }

}
