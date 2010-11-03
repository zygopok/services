package org.collectionspace.services.IntegrationTests.xmlreplay;
/*
When this is a jar file, run like so:

cd C:\src\trunk\services\IntegrationTests\xmlreplay\

java -DxmlReplayBaseDir=.\src\test\resources\test-data\xmlReplay
     -DtestGroup=2parts -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8008
     -Xnoagent -Djava.compiler=NONE
     -jar target\collectionspace-services-common-test-jar-with-dependencies.jar

When you are in cspace trunk, inside maven, do these steps:

     There is a bug, in that I define a log4j listener, but don't provide a log4jfile, so cheat:

         touch C:\src\trunk\services\client\target\classes\log4j.properties

     Set the opts for maven, but remember to unset them when you are done, or maven will drop into debug for the rest of this shell session.

         set MAVEN_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8012
         C:\src\trunk\services\client>set MAVEN_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8012 -verbose:class
     Now launch maven exec:java

          mvn  -Dmaven.surefire.debug exec:java  -DtestGroupID=organization -DxmlReplayBaseDir=C:\bin\xmlreplay\test-data\xmlReplay

     Now you will fire up your debugger and remote to port 8012 or whatever you set above.


 */
import org.apache.commons.cli.*;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.*;

/**  This class is used to replay a request to the Services layer, by sending the XML payload
 *   in an appropriate Multipart request.  See example usage in services/objectexit/client.
 *   @author Laramie Crocker
 */
public class XmlReplay {

    public XmlReplay(String basedir){
        this.basedir = basedir;
        this.serviceResultsMap = createResultsMap();
        this.variablesMap = createVariablesMap();
    }

    public static class ServiceResult {
        public String testID = "";
        public String testGroupID = "";
        public String fullURL = "";
        public String deleteURL = "";
        public String location = "";
        public String CSID = "";
        public String subresourceCSID = "";
        public String result = "";
        public int responseCode = 0;
        public String responseMessage = "";
        public String method = "";
        public String error = "";
        public String fromTestID = "";
        public String auth = "";
        public List<Integer> expectedCodes = new ArrayList<Integer>();
        public boolean gotExpectedResult(){
            for (Integer oneExpected : expectedCodes){
                if (responseCode == oneExpected){
                    return true;
                }
            }
            if (0<responseCode && responseCode<200){
                return false;
            } else if (400<=responseCode) {
                return false;
            }
            return true;
        }
        public String toString(){
            return "{ServiceResult: "
                    + ( Tools.notEmpty(testID) ? " testID:"+testID : "" )
                    + ( Tools.notEmpty(testGroupID) ? "; testGroupID:"+testGroupID : "" )
                    + ( Tools.notEmpty(fromTestID) ? "; fromTestID:"+fromTestID : "" )
                    +"; "+method
                    +"; "+responseCode
                    + ( Tools.notEmpty(responseMessage) ? "; msg:"+responseMessage : "" )
                    +"; URL:"+fullURL
                    +";auth: "+auth
                    + ( Tools.notEmpty(deleteURL) ? "; deleteURL:"+deleteURL : "" )
                    + ( Tools.notEmpty(location) ? "; location.CSID:"+location : "" )
                    + ( Tools.notEmpty(error) ? "; ERROR:"+error : "" )
                    + ( Tools.notEmpty(result) ? "; result:"+result : "" )
                    + ( (expectedCodes.size()>0) ? ";expectedCodes:"+expectedCodes : "" )
                    + ";gotExpected:"+gotExpectedResult()
                    +"}";
        }
    }

    public String toString(){
        return "XmlReplay{"+this.basedir+", "+this.configFileName+'}';
    }

    private static final String DEFAULT_CONFIG = "xml-replay-config.xml";

    private String configFileName = DEFAULT_CONFIG;
    public String getConfigFileName() {
        return configFileName;
    }

    /** @param configFileName The default name is stored in XmlReplay.DEFAULT_CONFIG
     *  but you may override that value with this setter, using
     *  just the filename and extension, but do NOT include any path info, after you
     *  call the constructor, but BEFORE you call any test methods.
     */
    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    private String basedir = ".";

    private Map<String, ServiceResult> serviceResultsMap;
    private Map<String, String> variablesMap;

    // TODO: make this use the org.collectionspace.services.client.test.ServiceRequestType without messing up the jar dependencies for the stand-alone version.
    private boolean responseCodeOK(ServiceResult pr){
        int code = pr.responseCode;
        if (0<code && code<200){
            return false;
        } else if (400<=code) {
            return false;
        } else {
            return true;
        }
    }

    public void runTest(String testGroupID, String testID) throws Exception {
        List<ServiceResult> result =
            runXmlReplayFile(this.basedir, this.configFileName, testGroupID, testID, this.serviceResultsMap, this.variablesMap, false, false);
        for(ServiceResult pr: result){
            if ( ! responseCodeOK(pr)){
                throw new Exception("Response code not expected: "+pr.responseCode+" in "+pr);
            }
        }
    }

    public List<ServiceResult> runTestGroup(String testGroupID) throws Exception {
        return runXmlReplayFile(this.basedir, this.configFileName, testGroupID, "", createResultsMap(), createVariablesMap(), true, false);
    }

    public static Map<String, ServiceResult> createResultsMap(){
        return new HashMap<String, ServiceResult>();
    }

    public static Map<String, String> createVariablesMap(){
        return new HashMap<String, String>();
    }

    public void autoDelete(){
        autoDelete(this.serviceResultsMap);
    }

    public static List<String> autoDelete(Map<String, ServiceResult> serviceResultsMap){
        List<String> results = new ArrayList<String>();
        for (ServiceResult pr : serviceResultsMap.values()){
            try {
                XmlReplayTransport.doDELETE(pr.deleteURL, pr.auth, pr.testID, "[autodelete]");
            } catch (Throwable t){
                String s = (pr!=null) ? "ERROR while cleaning up ServiceResult map: "+pr+" for "+pr.deleteURL+" :: "+t
                                      : "ERROR while cleaning up ServiceResult map (null ServiceResult): "+t;
                System.err.println(s);
                results.add(s);
            }
        }
        return results;
    }


    public static List<ServiceResult> runXmlReplayFile(String xmlReplayBaseDir,
                                          String configFileName,
                                          String testGroupID,
                                          String oneTestID,
                                          Map<String, ServiceResult> serviceResultsMap,
                                          Map<String, String> variablesMap,
                                          boolean param_autoDeletePOSTS,
                                          boolean dumpResults)
                                          throws Exception {
        //Internally, we maintain two collections of ServiceResult:
        //  the first is the return value of this method.  PostResults should only contain primitives, so this should not be a leak.
        //  the second is the serviceResultsMap, which is used for keeping track of CSIDs created by POSTs.

        List<ServiceResult> results = new ArrayList<ServiceResult>();
        String configFile = Tools.glue(xmlReplayBaseDir, "/", configFileName);
        Document document = getDocument(configFile); //will check full path first, then checks relative to PWD.
        if (document == null){
            throw new FileNotFoundException("XmlReplay config file ("+configFileName+") not found in basedir: "+xmlReplayBaseDir+" Exiting test.");
        }
        String php = document.selectSingleNode("/xmlReplay/protoHostPort").getText();
        String protoHostPort = php.trim();

        Map<String, String> authsMap = new HashMap<String, String>();
        List<Node> auths = document.selectNodes("//auths/auth");
        for (Node auth : auths) {
            authsMap.put(auth.valueOf("@ID"), auth.getStringValue());
        }

        String autoDeletePOSTS = "";
        List<Node> testgroupNodes;
        if (Tools.notEmpty(testGroupID)){
            testgroupNodes = document.selectNodes("//testGroup[@ID='"+testGroupID+"']");
        } else {
            testgroupNodes = document.selectNodes("//testGroup");
        }
        JexlEngine jexl = new JexlEngine();   // Used for expression language expansion from uri field.
        XmlReplayEval evalStruct = new XmlReplayEval();
        evalStruct.serviceResultsMap = serviceResultsMap;
        evalStruct.jexl = jexl;

        for (Node testgroup : testgroupNodes) {
            //Get a new JexlContext for each test group:
            JexlContext jc = new MapContext();
            evalStruct.jc = jc;

            autoDeletePOSTS = testgroup.valueOf("@autoDeletePOSTS");
            List<Node> tests;
            if (Tools.notEmpty(oneTestID)){
                tests = testgroup.selectNodes("test[@ID='"+oneTestID+"']");
            } else {
                tests = testgroup.selectNodes("test");
            }
            String authForTest = "";
            int testElementIndex = -1;
            //List<Integer> standardExpectedCodes = new ArrayList<Integer>();
            for (Node testNode : tests) {
                testElementIndex++;
                String testID = testNode.valueOf("@ID");
                String testIDLabel = Tools.notEmpty(testID) ? (testGroupID+'.'+testID) : (testGroupID+'.'+testElementIndex);
                String method = testNode.valueOf("method");
                String uri = testNode.valueOf("uri");
                String fullURL = Tools.glue(protoHostPort, "/", uri);
                String initURI = uri;

                String authIDForTest = testNode.valueOf("@auth");
                String currentAuthForTest = authsMap.get(authIDForTest);
                if (Tools.notEmpty(currentAuthForTest)){
                    authForTest = currentAuthForTest; //else just run with current from last loop;
                }

                if (uri.indexOf("$")>-1){
                    uri = evalStruct.eval(uri, serviceResultsMap, jexl, jc);
                    if ( ! uri.startsWith(protoHostPort)){
                        fullURL = Tools.glue(protoHostPort, "/", uri);
                    } else {
                        fullURL = uri;
                    }
                }

                List<Integer> expectedCodes = new ArrayList<Integer>();
                String expectedCodesStr = testNode.valueOf("expectedCodes");
                if (Tools.notEmpty(expectedCodesStr)){
                     String[] codesArray = expectedCodesStr.split(",");
                     for (String code : codesArray){
                         expectedCodes.add(new Integer(code));
                     }
                }

                boolean bDoingSinglePartPayload = false;

                if (   method.equalsIgnoreCase("POST")
                    || method.equalsIgnoreCase("PUT")) {
                    
                    List<String> partsList = new ArrayList<String>();
                    List<String> filesList = new ArrayList<String>();

                    String singlePartPayloadFilename = testNode.valueOf("filename");
                    if (Tools.notEmpty(singlePartPayloadFilename)){
                        bDoingSinglePartPayload = true;
                        singlePartPayloadFilename = xmlReplayBaseDir + '/' + singlePartPayloadFilename;
                    } else {
                        bDoingSinglePartPayload = false;
                        List<Node> parts = testNode.selectNodes("parts/part");
                        if (parts == null || parts.size()==0){  //path is just /testGroup/test/part/
                            String commonPartName = testNode.valueOf("part/label");
                            String testfile = testNode.valueOf("part/filename");
                            String fullTestFilename = xmlReplayBaseDir + '/' + testfile;
                            if ( Tools.isEmpty(testID) ){
                                testID = testfile; //It is legal to have a missing ID attribute, and rely on a unique filename.
                            }
                            partsList.add(commonPartName);
                            filesList.add(fullTestFilename);
                        } else { // path is /testGroup/test/parts/part/
                            for (Node part : parts){
                                String commonPartName = part.valueOf("label");
                                String filename = part.valueOf("filename");
                                String fullTestFilename = xmlReplayBaseDir + '/' + filename;
                                if ( Tools.isEmpty(testID) ){  //if testID is empty, we'll use the *first*  filename as ID.
                                    testID = filename; //It is legal to have a missing ID attribute, and rely on a unique filename.
                                }
                                partsList.add(commonPartName);
                                filesList.add(fullTestFilename);
                            }
                        }
                    }

                    ServiceResult postResult;
                    if (method.equalsIgnoreCase("POST")){
                        String csid = CSIDfromTestID(testNode, serviceResultsMap);
                        if (Tools.notEmpty(csid)){
                            uri = Tools.glue(uri, "/", csid+"/items/");
                        }
                        if (bDoingSinglePartPayload){
                            postResult = XmlReplayTransport.doPOST_PUTFromXML(singlePartPayloadFilename, protoHostPort, uri, "POST", XmlReplayTransport.APPLICATION_XML, evalStruct, authForTest);
                        } else {
                            postResult = XmlReplayTransport.doPOST_PUTFromXML_Multipart(filesList, partsList, protoHostPort, uri, "POST", evalStruct, authForTest);

                        }
                        postResult.testID = testID;
                        postResult.fullURL = fullURL;
                        postResult.auth = authForTest;
                        postResult.method = method;
                        //  TODO: sort out if this is desired: for now, put it in the map even if expected codes not correct.
                        serviceResultsMap.put(testID, postResult);
                        variablesMap.put(testID+".CSID", postResult.CSID);
                        results.add(postResult);
                    } else {  // is a PUT
                        uri = fromTestID(uri, testNode, serviceResultsMap);
                        if (bDoingSinglePartPayload) {
                            postResult = XmlReplayTransport.doPOST_PUTFromXML(singlePartPayloadFilename, protoHostPort, uri, "PUT", XmlReplayTransport.APPLICATION_XML, evalStruct, authForTest);
                        } else {
                            postResult = XmlReplayTransport.doPOST_PUTFromXML_Multipart(filesList, partsList, protoHostPort, uri, "PUT", evalStruct, authForTest);
                        }
                        postResult.testID = testID;
                        postResult.fullURL = Tools.glue(protoHostPort, "/", uri);
                        postResult.auth = authForTest;
                        postResult.method = method;
                        results.add(postResult);
                        //PUTs do not return a Location, so don't add to PostResultMap.
                    }
                } else if (method.equalsIgnoreCase("DELETE")){
                    ServiceResult deleteResult;
                    String fromTestID = testNode.valueOf("fromTestID");
                    ServiceResult pr = serviceResultsMap.get(fromTestID);
                    if (pr!=null){
                        deleteResult = XmlReplayTransport.doDELETE(pr.deleteURL, authForTest, testIDLabel, fromTestID);
                        deleteResult.testID = testID;
                        deleteResult.fromTestID = fromTestID;
                        deleteResult.auth = authForTest;
                        deleteResult.method = method;
                        results.add(deleteResult);
                        if (deleteResult.gotExpectedResult()){
                            serviceResultsMap.remove(fromTestID);
                        }
                    } else {
                        if (Tools.notEmpty(fromTestID)){
                            System.err.println("****\r\nServiceResult: ID not found in element fromTestID: "+fromTestID+". Using full URL: "+fullURL);
                        }
                        deleteResult = XmlReplayTransport.doDELETE(fullURL, authForTest, testID, fromTestID);
                        deleteResult.testID = testID;
                        deleteResult.fullURL = fullURL;
                        deleteResult.fromTestID = fromTestID;
                        deleteResult.auth = authForTest;
                        deleteResult.method = method;
                        results.add(deleteResult);
                    }
                } else if (method.equalsIgnoreCase("GET")){
                    fullURL = fromTestID(fullURL, testNode, serviceResultsMap);
                    //if (initURI.indexOf("$")>-1){
                    //    fullURL = Tools.glue(protoHostPort, "/", uri);  //TODO: this should be done for DELETE, etc.
                    //}
                    ServiceResult getResult = XmlReplayTransport.doGET(fullURL, authForTest);
                    getResult.testID = testID;
                    getResult.fullURL = fullURL;
                    getResult.auth = authForTest;
                    getResult.method = method;
                    results.add(getResult);
                } else if (method.equalsIgnoreCase("LIST")){
                    //fullURL = fromTestID(fullURL, testNode, serviceResultsMap);
                    if (initURI.indexOf("$")>-1){
                        fullURL = Tools.glue(protoHostPort, "/", uri);  //TODO: this should be done for DELETE, etc.
                    }
                    String listQueryParams = ""; //TODO: empty for now, later may pick up from XML config file.
                    ServiceResult listResult = XmlReplayTransport.doLIST(fullURL, listQueryParams, authForTest);
                    listResult.testID = testID;
                    listResult.fullURL = fullURL;
                    listResult.auth = authForTest;
                    listResult.method = method;
                    results.add(listResult);
                }
                ServiceResult oneResult = results.get(results.size()-1);
                if (expectedCodes.size()>0){
                    oneResult.expectedCodes = expectedCodes;
                }
                if (Tools.isEmpty(oneResult.testID)) oneResult.testID = testIDLabel;
                if (Tools.isEmpty(oneResult.testGroupID)) oneResult.testGroupID = testGroupID;

                System.out.println("XmlReplay:"+testIDLabel+": "+oneResult+"\r\n");
                if (dumpResults) System.out.println(oneResult.result);
            }
            if (Tools.isTrue(autoDeletePOSTS)&&param_autoDeletePOSTS){
                autoDelete(serviceResultsMap);
            }
        }
        return results;
    }

    private static String fromTestID(String fullURL, Node testNode, Map<String, ServiceResult> serviceResultsMap){
        String fromTestID = testNode.valueOf("fromTestID");
        if (Tools.notEmpty(fromTestID)){
            ServiceResult getPR = serviceResultsMap.get(fromTestID);
            if (getPR != null){
                fullURL = Tools.glue(fullURL, "/", getPR.location);
            }
        }
        return fullURL;
    }

    private static String CSIDfromTestID(Node testNode, Map<String, ServiceResult> serviceResultsMap){
        String result = "";
        String fromTestID = testNode.valueOf("fromTestID");
        if (Tools.notEmpty(fromTestID)){
            ServiceResult getPR = serviceResultsMap.get(fromTestID);
            if (getPR != null){
                result = getPR.location;
            }
        }
        //if (Tools.isEmpty(result)){
        //    System.out.println("No location csid found in serviceResultsMap: "+serviceResultsMap);
        //}
        return result;
    }


    public static org.dom4j.Document getDocument(String xmlFileName) {
        Document document = null;
        SAXReader reader = new SAXReader();
        try {
            document = reader.read(xmlFileName);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return document;
    }




    //======================== MAIN ===================================================================

    private static Options createOptions() {
        Options options = new Options();
        options.addOption("xmlReplayBaseDir", true, "default/basedir");
        return options;
    }

    public static String usage(){
        String result = "org.collectionspace.services.IntegrationTests.xmlreplay.XmlReplay {args}\r\n"
                        +"  -xmlReplayBaseDir <dir> \r\n"
                        +" You may also override these with system args, e.g.: \r\n"
                        +"   -DxmlReplayBaseDir=/path/to/dir \r\n"
                        +" These may also be passed in via the POM.\r\n"
                        +" You can also set these system args, e.g.: \r\n"
                        +"  -DtestGroupID=<oneID> \r\n"
                        +"  -DtestID=<one TestGroup ID>"
                        +"  -DautoDeletePOSTS=<true|false> \r\n"
                        +"    (note: -DautoDeletePOSTS won't force deletion if set to false in config file.";
        return result;
    }

    private static String opt(CommandLine line, String option){
        String result;
        String fromProps = System.getProperty(option);
        if (Tools.notEmpty(fromProps)){
            return fromProps;
        }
        result = line.getOptionValue(option);
        if (result == null){
            result = "";
        }
        return result;
    }

    public static void main(String[]args) throws Exception {
        Options options = createOptions();
        //System.out.println("System CLASSPATH: "+prop.getProperty("java.class.path", null));
        CommandLineParser parser = new GnuParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            String xmlReplayBaseDir = opt(line, "xmlReplayBaseDir");
            String testGroupID      = opt(line, "testGroupID");
            String testID           = opt(line, "testID");
            String autoDeletePOSTS  = opt(line, "autoDeletePOSTS");
            String dumpResults      = opt(line, "dumpResults");
            String configFilename   = opt(line, "configFilename");

            boolean bAutoDeletePOSTS = true;
            if (Tools.notEmpty(autoDeletePOSTS)) {
                bAutoDeletePOSTS = Tools.isTrue(autoDeletePOSTS);
            }
            boolean bDumpResults = false;
            if (Tools.notEmpty(dumpResults)) {
                bDumpResults = Tools.isTrue(autoDeletePOSTS);
            }
            if (Tools.isEmpty(configFilename)) {
                configFilename = DEFAULT_CONFIG;
            }
            if (Tools.isEmpty(xmlReplayBaseDir)){
                System.err.println("ERROR: xmlReplayBaseDir is empty.");
                return;
            }
            File f = new File(Tools.glue(xmlReplayBaseDir, "/", configFilename));
            if (!f.exists()){
                System.err.println("Config file not found: "+f.getCanonicalPath());
                return;
            }

            System.out.println("XmlReplay ::"
                            + "\r\n    xmlReplayBaseDir: "+xmlReplayBaseDir
                            + "\r\n    configFilename: "+configFilename
                            + "\r\n    testGroupID: "+testGroupID
                            + "\r\n    testID: "+testID
                            + "\r\n    autoDeletePOSTS: "+bAutoDeletePOSTS
                            + "\r\n    will use config file: "+f.getCanonicalPath());
            
            runXmlReplayFile(xmlReplayBaseDir, configFilename, testGroupID, testID, createResultsMap(), createVariablesMap(), bAutoDeletePOSTS, bDumpResults);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Cmd-line parsing failed.  Reason: " + exp.getMessage());
            System.err.println(usage());
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
            e.printStackTrace();
        }
    }

}
