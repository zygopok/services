package org.collectionspace.services.test;

import org.collectionspace.services.imports.IFragmentHandler;
import org.collectionspace.services.imports.ImportsContentHandler;
import org.dom4j.Document;
import org.testng.annotations.Test;


public class ImportsServiceTest {
    @Test
    public void testSax(){
        //String xmlString = FileTools.readFile("C:\\tmp", "collectionobject.xml");
        //ImportsContentHandler.parse("C:\\tmp\\collectionobject.xml");

        //ImportsContentHandler.parse("C:\\tmp\\imports.xml");

        //ImportsContentHandler handler = new ImportsContentHandler();
        //handler.setChopPath("/document/schema");
        IFragmentHandler callback = new FragmentHandlerImpl();
        ImportsContentHandler.parse("C:\\tmp\\imports.xml", "/document/schema", callback);
    }

    public static class FragmentHandlerImpl implements IFragmentHandler {
        public void onFragmentReady(Document context, String currentPath, String fragment){
            System.out.println("====Path=========\r\n"+currentPath);
            System.out.println("====XML==========\r\n"+context.asXML());
            System.out.println("====Fragment=====\r\n"+fragment+"\r\n=============\r\n");
        }
    }
}
