package org.collectionspace.services.imports;

import org.dom4j.Document;

/**
 * @author Laramie Crocker
 */
public interface IFragmentHandler {
    /** @param fragmentIndex is the zero-based index of the current fragment; you will first get this event
     *  on fragmentIndex==0, which is a fragmentCount of 1.*/
    public void onFragmentReady(Document context, String currentPath, int fragmentIndex, String fragment);

    /** @param fragmentCount is the count of fragments processed - a value of 1 means 1 fragment was found.*/
    public void onEndDocument(Document document, int fragmentCount);
}
