package org.collectionspace.services.IntegrationTests.test;

import org.collectionspace.services.IntegrationTests.xmlreplay.ServiceResult;
import org.collectionspace.services.IntegrationTests.xmlreplay.XmlReplay;
import org.collectionspace.services.IntegrationTests.xmlreplay.XmlReplayTest;
import org.collectionspace.services.common.Tools;
import org.testng.annotations.Test;

import java.util.List;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class XmlReplayDevTest extends XmlReplayTest {

    @Test
    public void runMaster() throws Exception {
     //   XmlReplay replay = createXmlReplayUsingIntegrationTestsModule("..");
     //   List<List<ServiceResult>> list = replay.runMaster(XmlReplay.DEFAULT_DEV_MASTER_CONTROL);
     //   logTestForGroup(list, "XmlReplayMasterTest");

        /*
        Maven surefire doesn't let you pass stuff on the command line
        unless you define -DforkMode=never inn the command-line args.
        So be sure to use a command-line like:
           mvn -e test -DxmlReplayMaster=dev-master.xml -DforkMode=never -Dtest=XmlReplayDevTest
        */
        String masterFile = System.getProperty("xmlReplayMaster");
        if (Tools.notEmpty(masterFile)){
            System.out.println("Using masterFile specified in System property: "+masterFile);
        } else {
            System.out.println("Using default masterFile: "+masterFile);
            masterFile = XmlReplay.DEFAULT_DEV_MASTER_CONTROL;
        }
        XmlReplay replay = createXmlReplayUsingIntegrationTestsModule("..");
        List<List<ServiceResult>> list = replay.runMaster(masterFile);
        logTestForGroup(list, "XmlReplayMasterTest");
    }
}
