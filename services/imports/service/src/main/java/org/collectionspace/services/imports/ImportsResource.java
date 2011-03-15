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
package org.collectionspace.services.imports;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.FileUtils;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.api.FileTools;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.api.ZipTools;
import org.collectionspace.services.common.context.ServiceContext;

// The modified Nuxeo ImportCommand from nuxeo's shell:
import org.collectionspace.services.imports.nuxeo.ImportCommand;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.ws.Response;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Laramie Crocker
 */
@Path(ImportsResource.SERVICE_PATH)
@Produces({"application/xml"})
@Consumes({"application/xml"})
public class ImportsResource extends ResourceBase {
    
    public static final String SERVICE_PATH = "imports";
    public static final String SERVICE_NAME = "imports";
    
    @Override
    public String getServiceName(){
        return SERVICE_NAME;
    }

    @Override
    protected String getVersionString() {
    	final String lastChangeRevision = "$LastChangedRevision: 2108 $";
    	return lastChangeRevision;
    }

    @Override
    //public Class<ImportsCommon> getCommonPartClass() {
    public Class getCommonPartClass() {
    	try {
            return Class.forName("org.collectionspace.services.imports.ImportsCommon");//.class;
        } catch (ClassNotFoundException e){
            return null;
        }
    }

    @POST
    @Consumes("application/xml")
    @Produces("application/xml")
    public javax.ws.rs.core.Response create(String xmlPayload) {
        String result;
        javax.ws.rs.core.Response.ResponseBuilder rb;
        try {
        	//PoxPayloadIn input = new PoxPayloadIn(xmlPayload);
        	//ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(input);
            String dir = "C:/tmp/imports-resource";
            expandXmlPayloadToDir(xmlPayload, dir);
            ImportCommand importCommand = new ImportCommand();
            String src = "/data/doco/JIRAs/3560/importtmpl/out"; //Local dir with ./Personauthorities/
            String dest = "/default-domain/workspaces";
            System.out.println("\r\n\r\n\r\n=====================\r\n   RUNNING create with xmlPayload: \r\n"+xmlPayload);
            importCommand.run(src, dest);
            result = "<?xml ?><import>SUCCESS</import>";
            rb = javax.ws.rs.core.Response.ok();
	    } catch (Exception e) {
            result = Tools.errorToString(e, true);
            rb = javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
        rb.entity(result);
        return rb.build();
    }

    public static final String REL_DIR_TO_MODULE = "./src/main/resources/templates";

    public static void expandXmlPayloadToDir(String xmlPayload, String dir) throws Exception {
        String filename = REL_DIR_TO_MODULE;
        String templateDir = (new File(filename)).getCanonicalPath();
        TemplateExpander.doOnePersonauthority(templateDir,
                                              templateDir+"/out",
                                              "personauthorities-part.xml",
                                              "authority.xml");


    }

    /** you can test like this:
     * curl -F "file=@out.zip;type=application/zip" --basic -u "Admin@collectionspace.org:Administrator" http://localhost:8280/cspace-services/imports
     */
    @POST
    @Consumes("multipart/form-data")
    @Produces("application/xml")
    public javax.ws.rs.core.Response acceptZip(@Context HttpServletRequest req,
    		                                   MultipartFormDataInput partFormData) {
    	javax.ws.rs.core.Response response = null;
    	try {
    		InputStream fileStream = null;
    		String preamble = partFormData.getPreamble();
    		System.out.println("Preamble type is:" + preamble);
    		Map<String, List<InputPart>> partsMap = partFormData.getFormDataMap();
            StringBuffer linebuf = new StringBuffer();
    		List<InputPart> fileParts = partsMap.get("file");
    		for (InputPart part : fileParts){
                String mediaType = part.getMediaType().toString();
                System.out.println("Media type is:" + mediaType);
    			if ( ! mediaType.equalsIgnoreCase("application/zip")){
                    continue;
                }
    			fileStream = part.getBody(InputStream.class, null);

    			File zipfile = FileUtils.createTmpFile(fileStream, getServiceName() + "_");
                String zipfileName = zipfile.getCanonicalPath();
                System.out.println("Media saved to:" + zipfileName);

                String baseOutputDir = FileTools.createTmpDir("imports-").getCanonicalPath();
                File indir = new File(baseOutputDir+"/in");
                indir.mkdir();
                ZipTools.unzip(zipfileName, indir.getCanonicalPath());
                System.out.println("Zipfile " + zipfileName + "extracted to: " + indir.getCanonicalPath());

                File scriptdir = new File(baseOutputDir+"/scripts");
                scriptdir.mkdir();
                String script =  "connect -u Administrator -p Administrator http://localhost:8080/nuxeo/site/automation\r\n"
                                +"cd /default-domain/workspaces\r\n"
                                +"import \""+baseOutputDir+"/in /default-domain/workspaces\r\n";
                // saveFile(String dir, String relativeName, String content, boolean forceParentDirs)
                File scriptFile = FileTools.saveFile(scriptdir.getCanonicalPath(), "import.txt", script, false);


                Runtime runtime = Runtime.getRuntime();
                String javaargs = " -cp nuxeo-shell.jar org.nuxeo.shell.Main -f \""+scriptFile.getCanonicalPath()+"\"";

                String [] sarray = {"java", javaargs};
                System.out.println("exec args: "+ Arrays.toString(sarray));

                String line;
                long start = System.currentTimeMillis();

                Process process = runtime.exec(sarray);
                System.out.println("process: " + process.toString());  //after a while, you can call process.exitValue()
                try {
                    System.out.println("process exit code: "+process.exitValue());  //after a while, you can call process.exitValue(), until then API is that it throws exception on this call.
                } catch (Throwable ie){
                    System.out.println("process may still be going..." +ie);
                }

    		}
	    	javax.ws.rs.core.Response.ResponseBuilder rb = javax.ws.rs.core.Response.ok();
	    	rb.entity("File accepted. Process output:\r\n"+linebuf.toString());
	    	response = rb.build();
    	} catch (Exception e) {
    		throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
    	}
		return response;
    }
    
}
