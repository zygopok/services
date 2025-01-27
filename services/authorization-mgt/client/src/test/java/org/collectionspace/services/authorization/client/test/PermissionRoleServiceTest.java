/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permRoles and
 * limitations under the License.
 */
package org.collectionspace.services.authorization.client.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import javax.ws.rs.core.Response;

import org.collectionspace.services.authorization.perms.EffectType;
import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.perms.PermissionAction;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PermissionClient;
import org.collectionspace.services.client.PermissionFactory;
import org.collectionspace.services.client.PermissionRoleClient;
import org.collectionspace.services.client.PermissionRoleFactory;
import org.collectionspace.services.client.RoleClient;
import org.collectionspace.services.client.RoleFactory;
import org.collectionspace.services.client.test.AbstractServiceTestImpl;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * PermissionServiceTest, carries out tests against a
 * deployed and running Permission, Role and PermissionRole Services.
 * 
 * $LastChangedRevision: 917 $
 * $LastChangedDate: 2009-11-06 12:20:28 -0800 (Fri, 06 Nov 2009) $
 */
public class PermissionRoleServiceTest extends AbstractServiceTestImpl<PermissionRole, PermissionRole,
		PermissionRole, PermissionRole> {

    /** The Constant logger. */
    private final static String CLASS_NAME = PermissionRoleServiceTest.class.getName();
    private final static Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    // Instance variables specific to this test.
    final private static String TEST_MARKER = "_PermissionRoleServiceTest";
    final private static String TEST_SERVICE_NAME = "fakeservice";
    final private static String NO_REL_SUFFIX = "-no-rel";
    /** The perm values. */
    private Hashtable<String, PermissionValue> permValues = new Hashtable<String, PermissionValue>();
    /** The role values. */
    private Hashtable<String, RoleValue> roleValues = new Hashtable<String, RoleValue>();
    /*
     * This method is called only by the parent class, AbstractServiceTestImpl
     */

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    protected String getServicePathComponent() throws Exception {
        return new PermissionRoleClient().getServicePathComponent();
    }
    
	@Override
	protected String getServiceName() {
    	return PermissionClient.SERVICE_NAME; //Since we're a sub-resource of permission service return its name?
	}
	
    /**
     * The entity type expected from the JAX-RS Response object
     */
    public Class<PermissionRole> getEntityResponseType() {
    	return PermissionRole.class;
    }
	
    /**
     * Seed data.
     * @throws Exception 
     */
    @BeforeClass(alwaysRun = true)
    public void seedData() throws Exception {
        String ra = TEST_SERVICE_NAME + TEST_MARKER;
        String accPermId = createPermission(ra, EffectType.PERMIT);
        PermissionValue pva = new PermissionValue();
        pva.setResourceName(ra);
        pva.setPermissionId(accPermId);
        permValues.put(pva.getResourceName(), pva);

        String rc = TEST_SERVICE_NAME + TEST_MARKER + NO_REL_SUFFIX;
        String coPermId = createPermission(rc, EffectType.DENY);
        PermissionValue pvc = new PermissionValue();
        pvc.setResourceName(rc);
        pvc.setPermissionId(coPermId);
        permValues.put(pvc.getResourceName(), pvc);
//
//        String ri = "intakes";
//        String iPermId = createPermission(ri, EffectType.DENY);
//        PermissionValue pvi = new PermissionValue();
//        pvi.setResourceName(ri);
//        pvi.setPermissionId(iPermId);
//        permValues.put(pvi.getResourceName(), pvi);

        String rn1 = "ROLE_CO1" + TEST_MARKER;
        String r1RoleId = createRole(rn1);
        RoleValue rv1 = new RoleValue();
        rv1.setRoleId(r1RoleId);
        rv1.setRoleName(rn1);
        roleValues.put(rv1.getRoleName(), rv1);

        String rn2 = "ROLE_CO2" + TEST_MARKER;
        String r2RoleId = createRole(rn2);
        RoleValue rv2 = new RoleValue();
        rv2.setRoleId(r2RoleId);
        rv2.setRoleName(rn2);
        roleValues.put(rv2.getRoleName(), rv2);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() throws Exception {
        return new PermissionRoleClient();
    }

	@Override
	protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) throws Exception {
        return new PermissionRoleClient(clientPropertiesFilename);
	}

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
     */
    @Override
    protected PermissionRole getCommonList(Response response) {
        //FIXME: http://issues.collectionspace.org/browse/CSPACE-1697
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readPaginatedList(java.lang.String)
     */
    @Override
    public void readPaginatedList(String testName) throws Exception {
        //FIXME: http://issues.collectionspace.org/browse/CSPACE-1697
    }
    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#create(java.lang.String)
     */

    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void create(String testName) throws Exception {
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        // Submit the request to the service and store the response.
        PermissionValue pv = permValues.get(TEST_SERVICE_NAME + TEST_MARKER);
        PermissionRole permRole = createPermissionRoleInstance(pv,
                roleValues.values(), true, true);
        PermissionRoleClient client = new PermissionRoleClient();
        
        Response res = client.create(pv.getPermissionId(), permRole);
        try {
            int statusCode = res.getStatus();

            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
            
            knownResourceId = extractId(res); //This is meaningless in this test, see getKnowResourceId() method for details
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownResourceId=" + knownResourceId);
            }
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    //to not cause uniqueness violation for permRole, createList is removed
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createList(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"create"})
    public void createList(String testName) throws Exception {
        //Should this really be empty?
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithEmptyEntityBody(java.lang.String)
     */
    @Override
    public void createWithEmptyEntityBody(String testName) throws Exception {
        //Should this really be empty?
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithMalformedXml(java.lang.String)
     */
    @Override
    public void createWithMalformedXml(String testName) throws Exception {
        //Should this really be empty?
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithWrongXmlSchema(java.lang.String)
     */
    @Override
    public void createWithWrongXmlSchema(String testName) throws Exception {
        //Should this really be empty?
    }

    // ---------------------------------------------------------------
    // CRUD tests : READ tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#read(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"create"})
    public void read(String testName) throws Exception {
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        PermissionRoleClient client = new PermissionRoleClient();
        Response res = client.read(
                permValues.get(TEST_SERVICE_NAME + TEST_MARKER).getPermissionId());
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);

            PermissionRole output = res.readEntity(PermissionRole.class);
            Assert.assertNotNull(output);
        } finally {
            if (res != null) {
                res.close();
            }
        }

    }

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readNonExistent(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void readNonExistent(String testName) throws Exception {
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        PermissionRoleClient client = new PermissionRoleClient();
        Response res = null;
        try {
            res = client.read(NON_EXISTENT_ID);
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    @Test(dataProvider = "testName",
    		dependsOnMethods = {"CRUDTests"})
    public void readNoRelationship(String testName) throws Exception {
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        PermissionRoleClient client = new PermissionRoleClient();
        Response res = null;
        try {
            res = client.read(
                    permValues.get(TEST_SERVICE_NAME + TEST_MARKER + NO_REL_SUFFIX).getPermissionId());
            // Check the status code of the response: does it match
            // the expected response(s)?
            assertStatusCode(res, testName);
            PermissionRole output = res.readEntity(PermissionRole.class);

            String sOutput = objectAsXmlString(output, PermissionRole.class);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + " received " + sOutput);
            }
        } finally {
            if (res != null) {
                res.close();
            }
        }

    }
    // ---------------------------------------------------------------
    // CRUD tests : READ_LIST tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readList(java.lang.String)
     */

    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"createList", "read"})
    public void readList(String testName) throws Exception {
        //Should this really be empty?
    }

    // Failure outcomes
    // None at present.
    // ---------------------------------------------------------------
    // CRUD tests : UPDATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#update(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"read", "readList", "readNonExistent"})
    public void update(String testName) throws Exception {
        //Should this really be empty?
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithEmptyEntityBody(java.lang.String)
     */
    @Override
    public void updateWithEmptyEntityBody(String testName) throws Exception {
        //Should this really be empty?
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithMalformedXml(java.lang.String)
     */
    @Override
    public void updateWithMalformedXml(String testName) throws Exception {
        //Should this really be empty?
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithWrongXmlSchema(java.lang.String)
     */
    @Override
    public void updateWithWrongXmlSchema(String testName) throws Exception {
        //Should this really be empty?
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateNonExistent(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"readNonExistent", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {
        //Should this really be empty?
    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#delete(java.lang.String)
     */
    @Override
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
//    dependsOnMethods = {"read"})
    public void delete(String testName) throws Exception {
        // Perform setup.
        setupDelete();

        //
        //
        //
        PermissionRoleClient client = new PermissionRoleClient();
        Response readResponse = client.read(
        		permValues.get(TEST_SERVICE_NAME + TEST_MARKER).getPermissionId());
        PermissionRole toDelete = null;
        try {
        	toDelete = readResponse.readEntity(PermissionRole.class);
        } finally {
        	readResponse.close();
        }        

        Response res = client.delete(
        		toDelete.getPermission().get(0).getPermissionId(), toDelete);
        try {
            int statusCode = res.getStatus();
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
            if (res != null) {
                res.close();
            }
        }
        
        //
        // recreate 'TEST_SERVICE_NAME + TEST_MARKER' account and roles
        //
        create(testName);
        setupDelete();
     
        //
        // Lookup a know permission, and delete all of its role relationships
        //
        readResponse = client.read(
        		permValues.get(TEST_SERVICE_NAME + TEST_MARKER).getPermissionId());
        toDelete = null;
        try {
        	toDelete = readResponse.readEntity(PermissionRole.class);
        } finally {
        	readResponse.close();
        }

        Response deleteRes = client.delete(toDelete.getPermission().get(0).getPermissionId());
        try {
            int statusCode = deleteRes.getStatus();
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
        	deleteRes.close();
        }
        
    }

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#deleteNonExistent(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void deleteNonExistent(String testName) throws Exception {
        //ignoring this test as the service side returns 200 now even if it does
        //not find a record in the db
    }
    
    // ---------------------------------------------------------------
    // Search tests
    // ---------------------------------------------------------------
    
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void searchWorkflowDeleted(String testName) throws Exception {
        // Fixme: null test for now, overriding test in base class
    }

    @Override
    protected String getKnowResourceId() {
    	return permValues.get(TEST_SERVICE_NAME + TEST_MARKER).getPermissionId();
    }
    
    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    /**
     * create permRolerole instance
     * @param pv permissionvalue
     * @param rvs rolevalue array
     * @param usePermId 
     * @param useRoleId
     * @return PermissionRole
     */
    public static PermissionRole createPermissionRoleInstance(PermissionValue pv,
            Collection<RoleValue> rvs,
            boolean usePermId,
            boolean useRoleId) {

        List<RoleValue> rvls = new ArrayList<RoleValue>();
        rvls.addAll(rvs);
        PermissionRole permRole = PermissionRoleFactory.createPermissionRoleInstance(
                pv, rvls, usePermId, useRoleId);
        if (logger.isDebugEnabled()) {
            logger.debug("to be created, permRole");
            logger.debug(objectAsXmlString(permRole, PermissionRole.class));
        }
        return permRole;
    }

    /**
     * Clean up.
     * @throws Exception 
     */
    @AfterClass(alwaysRun = true)
    @Override
    public void cleanUp() throws Exception {
        setupDelete();
        String noTest = System.getProperty("noTestCleanup");
        if (Boolean.TRUE.toString().equalsIgnoreCase(noTest)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping Cleanup phase ...");
            }
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Cleaning up temporary resources created for testing ...");
        }

        for (PermissionValue pv : permValues.values()) {
            deletePermission(pv.getPermissionId());
        }
        for (RoleValue rv : roleValues.values()) {
            deleteRole(rv.getRoleId());
        }
    }

    /**
     * Creates the permission.
     *
     * @param resName the res name
     * @param effect the effect
     * @return the string
     * @throws Exception 
     */
    private String createPermission(String resName, EffectType effect) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(getTestBanner("createPermission"));
        }
        setupCreate();
        PermissionClient permClient = new PermissionClient();
        List<PermissionAction> actions = PermissionFactory.createDefaultActions();
        Permission permission = PermissionFactory.createPermissionInstance(resName,
                "default permissions for " + resName,
                actions, effect, true, true, true);
        String id = null;
        Response res = permClient.create(permission);
        try {
            int statusCode = res.getStatus();
            if (logger.isDebugEnabled()) {
                logger.debug("createPermission: resName=" + resName
                        + " status = " + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
            id = extractId(res);
        } finally {
            if (res != null) {
                res.close();
            }
        }
        return id;
    }

    /**
     * Delete permission.
     *
     * @param permId the perm id
     * @throws Exception 
     */
    private void deletePermission(String permId) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(getTestBanner("deletePermission"));
        }
        setupDelete();
        PermissionClient permClient = new PermissionClient();
        Response res = permClient.delete(permId);
        try {
            int statusCode = res.getStatus();
            if (logger.isDebugEnabled()) {
                logger.debug("deletePermission: delete permission id="
                        + permId + " status=" + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
            res.close();
        }

    }

    /**
     * Creates the role.
     *
     * @param roleName the role name
     * @return the string
     * @throws Exception 
     */
    private String createRole(String roleName) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(getTestBanner("createRole"));
        }
        setupCreate();
        RoleClient roleClient = new RoleClient();

        Role role = RoleFactory.createRoleInstance(roleName,
        		roleName, //the display name
                "role for " + roleName, true, RoleFactory.EMPTY_PERMVALUE_LIST);
        Response res = null;
        String id = null;
        try {
            res = roleClient.create(role);
            int statusCode = res.getStatus();
            if (logger.isDebugEnabled()) {
                logger.debug("createRole: name=" + roleName
                        + " status = " + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);

            id = extractId(res);
        } finally {
            res.close();
        }
        return id;
    }

    /**
     * Delete role.
     *
     * @param roleId the role id
     * @throws Exception 
     */
    private void deleteRole(String roleId) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(getTestBanner("deleteRole"));
        }
        setupDelete();
        RoleClient roleClient = new RoleClient();
        Response res = roleClient.delete(roleId);
        try {
            int statusCode = res.getStatus();
            if (logger.isDebugEnabled()) {
                logger.debug("deleteRole: delete role id=" + roleId
                        + " status=" + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
            res.close();
        }
    }

	@Override
	protected PermissionRole createInstance(String commonPartName,
			String identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PermissionRole updateInstance(PermissionRole commonPartObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void compareUpdatedInstances(PermissionRole original,
			PermissionRole updated) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Class<PermissionRole> getCommonListType() {
		return PermissionRole.class;
	}

    /*
     * For convenience and terseness, this test method is the base of the test execution dependency chain.  Other test methods may
     * refer to this method in their @Test annotation declarations.
     */
    @Override
    @Test(dataProvider = "testName",
    		dependsOnMethods = {
        		"org.collectionspace.services.client.test.AbstractServiceTestImpl.baseCRUDTests"})    
    public void CRUDTests(String testName) {
    	// Do nothing.  Simply here to for a TestNG execution order for our tests
    }
    
	@Override
	protected long getSizeOfList(PermissionRole list) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Method getSizeOfList() is not implemented because this service does not support lists.");
	}
}
