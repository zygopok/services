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
package org.collectionspace.services.test;

import org.collectionspace.services.common.IFragmentHandler;
import org.collectionspace.services.common.XmlSaxFragmenter;
import org.collectionspace.services.common.api.FileTools;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.imports.ImportsResource;
import org.collectionspace.services.imports.TemplateExpander;
import org.dom4j.Document;
import org.dom4j.Element;
import org.restlet.util.Template;
import org.testng.annotations.Test;

import java.io.File;

public class ImportsServiceTest {
    public static final String REL_DIR_TO_MODULE = "./src/main/resources/templates";

    @Test
    public void testImports() throws Exception {
        String TEMPLATE_DIR = (new File(REL_DIR_TO_MODULE)).getCanonicalPath();
        String SERVICE_NAME = "Personauthorities";
        String SERVICE_TYPE = "Personauthority";
        String filename = TEMPLATE_DIR+"/authority-request.xml";

        FragmentHandlerImpl callback = new FragmentHandlerImpl(); // IS_A IFragmentHandler
        callback.SERVICE_NAME = SERVICE_NAME;
        callback.TEMPLATE_DIR = TEMPLATE_DIR;
        callback.SERVICE_TYPE = SERVICE_TYPE;


        createWorkspace(SERVICE_NAME, TEMPLATE_DIR);
        XmlSaxFragmenter.parse(filename, "/imports/import", callback, false);
    }

    //This gets called by the fragmenter callback.
    public static void createDocsInWorkspace(String partTmpl, String SERVICE_NAME, String SERVICE_TYPE, String TEMPLATE_DIR) throws Exception {
        String wrapperTmpl = FileTools.readFile(TEMPLATE_DIR,"authority.xml");
        TemplateExpander.doOneService(TEMPLATE_DIR, TEMPLATE_DIR+"/out/"+SERVICE_NAME, partTmpl, wrapperTmpl, SERVICE_TYPE, SERVICE_NAME);
    }

    public static void createWorkspace(String SERVICE_NAME, String TEMPLATE_DIR) throws Exception {
        String personauthoritiesWorkspaceDoc = TEMPLATE_DIR+'/'+"personauthorities-workspace-document.xml";
        String personauthoritiesDir = TEMPLATE_DIR+"/out/"+SERVICE_NAME;
        FileTools.copyFile(personauthoritiesWorkspaceDoc, personauthoritiesDir+"/document.xml", true);
        String personauthoritiesDoc = FileTools.readFile(personauthoritiesDir, "/document.xml");
        personauthoritiesDoc = TemplateExpander.searchAndReplaceVar(personauthoritiesDoc, "ServiceName", SERVICE_NAME);
        FileTools.saveFile(personauthoritiesDir, "document.xml", personauthoritiesDoc, true);
    }




    public static class FragmentHandlerImpl implements IFragmentHandler {
        String SERVICE_NAME = "";
        String SERVICE_TYPE = "";
        String TEMPLATE_DIR = "";
        public void onFragmentReady(Document context, Element fragmentParent, String currentPath, int fragmentIndex, String fragment){
            System.out.println("====Path============\r\n"+currentPath+'['+fragmentIndex+']');
            System.out.println("====Context=========\r\n"+ XmlSaxFragmenter.prettyPrint(context));
            System.out.println("====Fragment========\r\n"+fragment+"\r\n===================\r\n");
            try {
                createDocsInWorkspace(fragment, SERVICE_NAME, SERVICE_TYPE,  TEMPLATE_DIR);
            } catch (Exception e){
                System.err.println("ERROR calling expandXmlPayloadToDir"+e);
                e.printStackTrace();
            }
        }

        public void onEndDocument(Document document, int fragmentCount){
            System.out.println("====DONE============"+ XmlSaxFragmenter.prettyPrint(document)+"================");
        }

    }
}
