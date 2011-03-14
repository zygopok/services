package org.collectionspace.services.test;

import org.collectionspace.services.imports.IFragmentHandler;
import org.collectionspace.services.imports.ImportsContentHandler;
import org.dom4j.Document;
import org.testng.annotations.Test;


public class ImportsServiceTest {
    @Test
    public void testSax(){

        IFragmentHandler callback = new FragmentHandlerImpl();
        ImportsContentHandler.parse("C:\\tmp\\imports.xml", "/document/schema", callback);
    }

    public static class FragmentHandlerImpl implements IFragmentHandler {
        public void onFragmentReady(Document context, String currentPath, int fragmentIndex, String fragment){
            System.out.println("====Path=========\r\n"+currentPath+'['+fragmentIndex+']');
            System.out.println("====XML==========\r\n"+ImportsContentHandler.prettyPrint(context));
            System.out.println("====Fragment=====\r\n"+fragment+"\r\n=============\r\n");
        }

        public void onEndDocument(Document document, int fragmentCount){
            System.out.println("====DONE=====\r\n"+ImportsContentHandler.prettyPrint(document)+"\r\n=============\r\n");
        }
    }
}
