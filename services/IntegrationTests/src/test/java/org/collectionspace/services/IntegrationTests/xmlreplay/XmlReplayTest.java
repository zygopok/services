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
    public static XmlReplay createXmlReplay(String xmlReplayControlFile) throws Exception {
        String pwd = (new File(".")).getCanonicalPath();
        XmlReplay replay = new XmlReplay(pwd+"/src/test/resources/test-data/xmlreplay");
        if (Tools.notEmpty(xmlReplayControlFile)){
            replay.setConfigFileName(xmlReplayControlFile);
        }
        return replay;
    }


    @Test
    public void runMaster() throws Exception {
        System.out.println("\r\n\r\n=============== IN runMaster =================\r\n");
        XmlReplay replay = createXmlReplay("");
        replay.runMaster(XmlReplay.DEFAULT_MASTER_CONFIG);
    }

}
