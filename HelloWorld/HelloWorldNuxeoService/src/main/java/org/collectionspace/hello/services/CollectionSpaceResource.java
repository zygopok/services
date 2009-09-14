package org.collectionspace.hello.services;

import org.collectionspace.hello.services.nuxeo.NuxeoRESTClient;

public abstract class CollectionSpaceResource {
    //replace WORKSPACE_UID for resource workspace
	static String CS_COLLECTIONOBJECT_WORKSPACE_UID = "3fc8df62-c8f6-4599-b2bc-95aebd96e4c4";
    //sanjay 6c7881fe-54c5-486e-b144-a025dee3a484
    //demo eae0d7b6-580a-45a3-a0f3-e25e980e03bb
	static String CS_PERSON_WORKSPACE_UID = "eae0d7b6-580a-45a3-a0f3-e25e980e03bb";
	
    //replace host if not running on localhost
    static String CS_NUXEO_HOST = "173.45.230.59";
//	static String CS_NUXEO_HOST = "localhost";
    static String CS_NUXEO_URI = "http://" + CS_NUXEO_HOST + ":8080/nuxeo";
    
    NuxeoRESTClient getClient() {
        NuxeoRESTClient nxClient = new NuxeoRESTClient(CS_NUXEO_URI);
        nxClient.setAuthType(NuxeoRESTClient.AUTH_TYPE_BASIC);
        nxClient.setBasicAuthentication("Administrator", "Administrator");
        return nxClient;
    }
    
}
