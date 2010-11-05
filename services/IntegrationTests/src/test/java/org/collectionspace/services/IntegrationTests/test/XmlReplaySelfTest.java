package org.collectionspace.services.IntegrationTests.test;

import org.collectionspace.services.IntegrationTests.xmlreplay.ServiceResult;
import org.collectionspace.services.IntegrationTests.xmlreplay.XmlReplay;
import org.collectionspace.services.IntegrationTests.xmlreplay.XmlReplayTest;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class XmlReplaySelfTest extends XmlReplayTest {


    @Test
    public void runMaster() throws Exception {
        XmlReplay replay = createXmlReplayForModule();
        List<List<ServiceResult>> list = replay.runMaster("xml-replay-master-self-test.xml");
        logTestForGroup(list, "runMaster");
    }

    @Test
    public void runOneTest() throws Exception {
        XmlReplay replay = createXmlReplayForModule();
        replay.readOptionsFromMasterConfigFile("xml-replay-master-self-test.xml");
        replay.setControlFileName("xml-replay-self-test.xml");

        List<ServiceResult> list = replay.runTest("selftestGroup", "OrgAuth1");
        logTest(list, "runOneTest");
    }

    @Test
    public void runTestGroup_AllOptions() throws Exception {
        //Create an XmlReplay relative to this module, which should then have a directory of payloads and config files
        // as specified in XmlReplayTest.XMLREPLAY_REL_DIR_TO_MODULE
        XmlReplay replay = createXmlReplayForModule();

        //You may read Dump, Auths, and protoHostPort from the master file:
        replay.readOptionsFromMasterConfigFile("xml-replay-master-self-test.xml"); //or use: XmlReplay.DEFAULT_MASTER_CONTROL as master filename;
        //or you may set those options individually as shown next.
        // Note that controlFileName is NOT set from calling readOptionsFromMasterConfigFile.
        // If you run a master, it sets controlFileName, possibly in a loop.
        // All of the Auths will be read from the master file, and may be referenced from your control file,
        // or you may specify Auths in your control file.  There are also public methods to set the AuthsMap yourself.

        //XmlReplay wants to know about two files: a master and a control file
        //  The master references one to many control files.
        //  If you don't call runMaster(), you must specify the control file:
        replay.setControlFileName("xml-replay-self-test.xml");

        //These option default sensibly, some of them from the master, but here's how to set them all:

        //Dump determines how much goes to log, and how verbose.
        XmlReplay.Dump dump = XmlReplay.getDumpConfig(); //static factory call.
        dump.payloads = false;
        dump.dumpServiceResult = XmlReplay.Dump.dumpServiceResultOptions[0];
        replay.setDump(dump);

        //use this if you must look it up from some other place.
        // Default is to have it in xml-replay-master.xml
        replay.setProtoHostPort("http://localhost:8180");

        //Default is true, but you can override if you want to leave objects on server, or control the order of deletion.
        replay.setAutoDeletePOSTS(false);

        //You don't need this, but you can inspect what XmlReplay holds onto: a data structure of CSIDs
        Map<String, ServiceResult> serviceResultsMap = replay.getServiceResultsMap();

        // ****** RUN A GROUP ***********************************************
        List<ServiceResult> list = replay.runTestGroup("selftestGroup");

        // This runs a group called "organization" inside a control file named above, which happens to be called "organization.xml".
        // You could also run just one test using these options by calling replay.runTest as shown above in XmlReplayTest.runOneTest()

        //Now, since we set setAutoDeletePOSTS(false) above, you can clean up manually:
        replay.autoDelete(); //deletes everything in serviceResultsMap, which it hangs onto.

        logTest(list, "runTestGroup_AllOptions");
    }

}
