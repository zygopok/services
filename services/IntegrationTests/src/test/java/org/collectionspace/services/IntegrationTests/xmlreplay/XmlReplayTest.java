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
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.collectionspace.services.IntegrationTests.xmlreplay;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class XmlReplayTest {

    private XmlReplay xmlreplay;
    public static XmlReplay createXmlReplay() throws Exception {
        String pwd = (new File(".")).getCanonicalPath();
        XmlReplay replay = new XmlReplay(pwd+"/src/test/resources/test-data/xmlreplay");
        replay.setConfigFileName("security.xml");
        return replay;
    }

    @BeforeClass
    public void initXmlReplay() throws Exception {
        xmlreplay = createXmlReplay();
        System.out.println("IN initXmlReplay "+xmlreplay);
    }

    @AfterClass
    public void finalizeXmlReplay() throws Exception {
        xmlreplay.autoDelete();
        System.out.println("IN finalizeXmlReplay");
    }

    @Test
    public void runTest() throws Exception {
        //xmlreplay.runTest("security", "");
        System.out.println("IN runTest");
    }

    //For some reason, when methods are in groups, @BeforeClass doesn't fire.
    @Test
     public void runGroup() throws Exception {
        System.out.println("IN runGroup");
         if (xmlreplay==null){
             System.out.println("ERROR: XmlReplay not initialized in test, probably because @BeforeClass did not run.");
             return;
         }
    //    XmlReplay xmlreplay2 = createXmlReplay();
        //logger.debug("\r\n===========\r\n XmlReplay.runGroup("+xmlReplayGroup+") :: "+xmlreplay2+"\r\n");
        List<XmlReplay.ServiceResult> results = xmlreplay.runTestGroup("security");
        for (XmlReplay.ServiceResult sr : results){
            if (Tools.notEmpty(sr.error) || (false==sr.gotExpectedResult())){
                Assert.fail("XmlReplay got unexpected response.  ServiceResult: "+sr);
            }
        }
    }

}
