/**	
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c) 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import org.collectionspace.services.conservation.ConservationCommon;

/**
 * ConservationClient.java
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 *
 */
public class ConservationClient extends AbstractCommonListPoxServiceClientImpl<ConservationProxy, ConservationCommon> {

    public static final String SERVICE_NAME = "conservation";
    public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;
    public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
    public static final String SERVICE_PATH_PROXY = SERVICE_PATH + "/";
    public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;

    public ConservationClient() {
		super();
	}

    public ConservationClient(String clientPropertiesFilename) {
		super(clientPropertiesFilename);
	}

	/* (non-Javadoc)
     * @see org.collectionspace.services.client.AbstractServiceClientImpl#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public Class<ConservationProxy> getProxyClass() {
        return ConservationProxy.class;
    }

}
