package org.collectionspace.services.imports;

import org.collectionspace.services.common.api.Tools;
//import org.xml.sax.*;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.ext.Locator2;

/**
 * @author Laramie Crocker
 */
public class ImportsContentHandler implements ContentHandler, ErrorHandler {

    //=============== ContentHandler ====================================================

    public void setDocumentLocator(Locator locator) {
        if (xmlDeclarationDone){
            return;
        }
        if (locator instanceof Locator2){
            Locator2 l2 = ((Locator2) locator);
            String enc = l2.getEncoding();
            String ver = l2.getXMLVersion();
            append("<?xml version=\""+ver+"\" encoding=\""+enc+"\"?>\r\n");
            xmlDeclarationDone = true;
        } else {
            //System.err.println("Locator2 not found.");
            append("<?xml version=\"1.0\"?>\r\n");
            xmlDeclarationDone = true;
        }
        //System.err.println("Locator getPublicId:"+locator.getPublicId()+" Locator getSystemId: "+locator.getSystemId());
    }

    public void startDocument() throws SAXException {
        document = DocumentHelper.createDocument();
    }

    public void endDocument() throws SAXException {
        System.out.println("\r\nEND");
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        append("<" + name(qName, localName) + attsToStr(atts) + ">");
        if (inFragment){
            inFragmentDepth++;
            return;
        }
        if (currentElement == null){
            currentElement =  document.addElement(qName);
        } else {
            Element element = DocumentHelper.createElement(qName);
            currentElement.add(element);
            previousElement = currentElement;
            currentElement = element;
        }
        addAttributes(currentElement, atts);
        String currentPath = currentElement.getPath();
        if (currentPath.equals(chopPath)){
            buffer = new StringBuffer();
            inFragment = true;
        }
        //System.out.println("      ~~~ START currentPath: "+currentPath);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        append("</" + name(qName, localName) + '>');
        if (inFragment){
            inFragmentDepth--;
        }
        String currentPath = currentElement.getPath();
        //System.out.println("      ~~~ END currentPath: "+currentPath);
        if (inFragment && (inFragmentDepth==0) && currentPath.equals(chopPath)){
            if (fragmentHandler!=null) {
                fragmentHandler.onFragmentReady(document, currentPath, buffer.toString());
            }
            inFragment = false;
        } else {
            currentElement = previousElement;
        }
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        String chars = new String(ch, start, length);
        append(chars);
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }
    public void endPrefixMapping(String prefix) throws SAXException {
    }
    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
    }
    public void processingInstruction(String target, String data) throws SAXException {
    }
    public void skippedEntity(String name) throws SAXException {
    }

    //=============== ErrorHandler ====================================================

    public void error(SAXParseException exception){
        System.err.println("ERROR:"+exception);
    }
    public void fatalError(SAXParseException exception){
        System.err.println("FATAL_ERROR:"+exception);
    }
    public void warning(SAXParseException exception){
        System.err.println("WARNING:"+exception);
    }



    public void foo(String[] args) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement( "imports" );
        Element author1 = root.addElement("author")
          .addAttribute("name", "James")
          .addAttribute("location", "UK")
          .addText("James Strachan");

    }

    //================ Helper Methods ===================================================

    private Document document;
    private Element currentElement;
    private Element previousElement;
    private StringBuffer buffer = new StringBuffer();

    private boolean xmlDeclarationDone = false;
    private boolean inFragment = false;
    private int inFragmentDepth = 0;

    private String chopPath = "";
    public String getChopPath() {
        return chopPath;
    }
    public void setChopPath(String chopPath) {
        this.chopPath = chopPath;
    }

    private IFragmentHandler fragmentHandler;
    public IFragmentHandler getFragmentHandler() {
        return fragmentHandler;
    }
    public void setFragmentHandler(IFragmentHandler fragmentHandler) {
        this.fragmentHandler = fragmentHandler;
    }

    protected void append(String str){
        //System.out.print(str);
        buffer.append(str);
    }



    protected String name(String qn, String ln){
        if (Tools.isEmpty(qn)){
            return ln;
        }
        if (qn.equals(ln)){
            return ln;
        }
        return qn;//"["+qn+';'+ln+"]";
    }

    //NOTE: we don't deal with this here because we don't need to
    // actually understand the namespace uri:
    // a.getURI(i)
    protected String attsToStr(Attributes a){
        StringBuffer b = new StringBuffer();
        String qn, ln;
        int attsLen = a.getLength();
        for (int i=0; i<attsLen; i++){
            b.append(' ');
            qn = a.getQName(i);
            ln = a.getLocalName(i);
            b.append(name(qn, ln)).append("=\"")
             .append(a.getValue(i)).append('\"');
        }
        return b.toString();
    }

    protected void addAttributes(Element cur, Attributes a){
        int attsLen = a.getLength();
        for (int i=0; i<attsLen; i++){
            cur.addAttribute(a.getQName(i), a.getValue(i));
        }
    }

    public static void parse(String theFileName, String chopPath, IFragmentHandler handler){
        try{
            XMLReader parser = XMLReaderFactory.createXMLReader();
            ImportsContentHandler importsHandler = new ImportsContentHandler();
            importsHandler.setChopPath(chopPath);
            importsHandler.setFragmentHandler(handler);
            parser.setContentHandler(importsHandler);
            parser.setErrorHandler(importsHandler);
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            parser.parse(theFileName);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
