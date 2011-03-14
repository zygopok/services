package org.collectionspace.services.imports;

import org.dom4j.Document;

/**
 * @author Laramie Crocker
 */
public interface IFragmentHandler {
    public void onFragmentReady(Document context, String currentPath, String fragment);
}
