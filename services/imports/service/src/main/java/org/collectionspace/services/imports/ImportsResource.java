/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2011 University of California at Berkeley

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
import org.collectionspace.services.common.XmlSaxFragmenter;
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
import java.io.FileNotFoundException;
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

    public static final String TEMPLATE_DIR = "/src/trunk/services/imports/service/src/main/resources/templates";

    /** you can test this with something like:
     * curl -X POST http://localhost:8180/cspace-services/imports -i  -u "Admin@collectionspace.org:Administrator" -H "Content-Type: application/xml" -T in.xml
     * -T /src/trunk/services/imports/service/src/main/resources/templates/authority-request.xml
     */
    @POST
    @Consumes("application/xml")
    @Produces("application/xml")
    public javax.ws.rs.core.Response create(String xmlPayload) {
        String result;
        javax.ws.rs.core.Response.ResponseBuilder rb;
        try {
        	//PoxPayloadIn input = new PoxPayloadIn(xmlPayload);
        	//ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(input);
            System.out.println("\r\n\r\n\r\n=====================\r\n   RUNNING create with xmlPayload: \r\n"+xmlPayload);

            //First, save the import request to a local file.
            // It may be huge. TODO: may have to deal with streams instead of String.
            // In the process, we must expand the request and wrap it with all kinds of Nuxeo baggage, which expandXmlPayloadToDir knows how to do.

            String outputDir = FileTools.createTmpDir("imports-").getCanonicalPath();
            expandXmlPayloadToDir(xmlPayload, TEMPLATE_DIR, outputDir);

            //Next, call the nuxeo import service, pointing it to our local directory that has the expanded request.
            ImportCommand importCommand = new ImportCommand();
            String destWorkspaces = "/default-domain/workspaces";
            String report = importCommand.run(outputDir, destWorkspaces);
            result = "<?xml ?><import><msg>SUCCESS</msg><report></report>"+report+"</import>";
            rb = javax.ws.rs.core.Response.ok();
	    } catch (Exception e) {
            result = Tools.errorToString(e, true);
            rb = javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
        rb.entity(result);
        return rb.build();
    }

    /** This method may be called statically from outside this class; there is a test call in
     *   org.collectionspace.services.test.ImportsServiceTest
     *
     * @param xmlPayload   A request file has a specific format, you can look at:
     *      trunk/services/imports/service/src/test/resources/requests/authority-request.xml
     * @param templateDir  The local directory where templates are to be found at runtime.
     * @param outputDir    The local directory where expanded files and directories are found, ready to be passed to the Nuxeo importer.
     */
    public static void expandXmlPayloadToDir(String xmlPayload, String templateDir, String outputDir) throws Exception {
        String requestDir = FileTools.createTmpDir("imports-request-").getCanonicalPath();
        File requestFile = FileTools.saveFile(requestDir, "request.xml", xmlPayload, true);
        if (requestFile == null){
            throw new FileNotFoundException("Could not create file in requestDir: "+requestDir);
        }
        String requestFilename = requestFile.getCanonicalPath();
        System.out.println("############## REQUEST_FILENAME: "+requestFilename);
        System.out.println("############## TEMPLATE_DIR: "+templateDir);
        System.out.println("############## OUTPUT_DIR:"+outputDir);
        TemplateExpander.expand(templateDir, outputDir, requestFilename, "/imports/import");
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

                long start = System.currentTimeMillis();
                //TODO: now call import service...
    		}
	    	javax.ws.rs.core.Response.ResponseBuilder rb = javax.ws.rs.core.Response.ok();
	    	rb.entity("File accepted.");
	    	response = rb.build();
    	} catch (Exception e) {
    		throw bigReThrow(e, ServiceMessages.CREATE_FAILED);
    	}
		return response;
    }
    
}
