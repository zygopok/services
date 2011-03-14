package org.collectionspace.services.test;

import org.collectionspace.services.common.IFragmentHandler;
import org.collectionspace.services.common.XmlSaxFragmenter;
import org.dom4j.Document;
import org.dom4j.Element;
import org.testng.annotations.Test;


public class ImportsServiceTest {
    @Test
    public void testSax(){

        IFragmentHandler callback = new FragmentHandlerImpl();
        XmlSaxFragmenter.parse("C:\\tmp\\imports.xml", "/document/schema", callback);
    }

    public static class FragmentHandlerImpl implements IFragmentHandler {
        public void onFragmentReady(Document context, Element fragmentParent, String currentPath, int fragmentIndex, String fragment){
            System.out.println("====Path=========\r\n"+currentPath+'['+fragmentIndex+']');
            System.out.println("====Context======\r\n"+ XmlSaxFragmenter.prettyPrint(context));
            System.out.println("====Fragment=====\r\n"+fragment+"\r\n=============\r\n");
        }

        public void onEndDocument(Document document, int fragmentCount){
            System.out.println("====DONE=========\r\n"+ XmlSaxFragmenter.prettyPrint(document)+"\r\n=============\r\n");
        }
    }
}
