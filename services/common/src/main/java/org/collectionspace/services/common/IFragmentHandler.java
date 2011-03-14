package org.collectionspace.services.common;

import org.dom4j.Document;
import org.dom4j.Element;

/** Define this interface to listen for events from the driving class:
 *   org.collectionspace.services.common.XmlSaxFragmenter , so that
 *   the XmlSaxFragmenter class may be passed a large file or InputSource (stream)
 *   and it will be parsed with SAX, but you will get fragments from it that you can
 *   parse with DOM.
 *
 *  You will be passed a Document context, which is a Dom4j document that represents the
 *  skeleton of the document you started with, but without any fragments, so the Document
 *  will just be context information of how the XmlSaxFragmenter found this fragment.
 *
 *  You will receive onFragmentReady() events whenever a fragment is parsed completely.
 *  the fragment parameter will be just the inner XML String of fragmentParent, and will
 *  not be represented in the DOM of the Document context.
 *
 *  @author Laramie Crocker
 */
public interface IFragmentHandler {
    /** @param fragmentIndex is the zero-based index of the current fragment; you will first get this event
     *  on fragmentIndex==0, which is a fragmentCount of 1. */
    public void onFragmentReady(Document context,
                                Element fragmentParent,
                                String currentPath,
                                int fragmentIndex,
                                String fragment);

    /** @param fragmentCount is the count of fragments processed - a value of 1 means 1 fragment was found. */
    public void onEndDocument(Document context, int fragmentCount);
}
