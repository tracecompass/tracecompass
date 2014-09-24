/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.trace.indexer;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for tmf.core.trace.index
 *
 * @author Marc-Andre Laperle
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.tmf.core.trace.indexer.messages"; //$NON-NLS-1$
    /**
     * Error opening index
     */
    public static String ErrorOpeningIndex;
    /**
     * I/O Error allocating a node
     */
    public static String BTree_IOErrorAllocatingNode;
    /**
     * I/O Error closing the index
     */
    public static String IOErrorClosingIndex;
    /**
     * I/O Error reading header from disk
     */
    public static String IOErrorReadingHeader;
    /**
     * I/O Error writing header from disk
     */
    public static String IOErrorWritingHeader;
    /**
     * I/O Error reading node from disk
     */
    public static String BTreeNode_IOErrorLoading;
    /**
     * I/O Error writing node to disk
     */
    public static String BTreeNode_IOErrorWriting;
    /**
     * I/O Error reading from disk
     */
    public static String FlatArray_IOErrorReading;
    /**
     * I/O Error writing to disk
     */
    public static String FlatArray_IOErrorWriting;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
