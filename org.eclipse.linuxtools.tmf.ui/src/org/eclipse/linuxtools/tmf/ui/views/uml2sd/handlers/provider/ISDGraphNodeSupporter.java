/**********************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider;

/**
 * Interface for providing a graph node supporter.
 *
 * Sequence Diagram loaders which implement this class provide the actions for finding or filtering the sequence
 * diagram. This interface also allow the implementor to set which action/feature are supported
 *
 * Action provider are associated to a Sequence Diagram SDWidget calling <code>SDViewer.setSDFindProvider()</code> or
 * <code>SDViewer.setSDFilterProvider()</code>.
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public interface ISDGraphNodeSupporter {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Lifeline support ID
     */
    static int LIFELINE = 0;
    /**
     * Synchronous message support ID
     */
    static int SYNCMESSAGE = 1;
    /**
     * Synchronous message return support ID
     */
    static int SYNCMESSAGERETURN = 2;
    /**
     * Asynchronous message support ID
     */
    static int ASYNCMESSAGE = 3;
    /**
     * Asynchronous message return support ID
     */
    static int ASYNCMESSAGERETURN = 4;
    /**
     * Stop support ID
     */
    static int STOP = 5;

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * Return true to enable this options, false otherwise
     *
     * @param nodeType
     *            The integer value matching the type of the node
     * @return true to enable this options, false otherwise
     */
    boolean isNodeSupported(int nodeType);

    /**
     * Return the name to use in dialogs Not called if isNodeSupported return
     * false
     *
     * @param nodeType
     *            The integer value matching the type of the node
     * @param loaderClassName
     *            The name of the loader class
     * @return the name to use in dialogs
     */
    String getNodeName(int nodeType, String loaderClassName);
}
