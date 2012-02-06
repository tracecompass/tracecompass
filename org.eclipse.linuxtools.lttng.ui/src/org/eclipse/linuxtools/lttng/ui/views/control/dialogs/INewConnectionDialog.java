/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.control.dialogs;

/**
 * <b><u>INewConnectionDialog</u></b>
 * <p>
 * Interface for connection information dialog.
 * </p>
 */
public interface INewConnectionDialog {
    
    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * @return returns the node name (alias).
     */
    public String getNodeName();

    /**
     * @return returns the node address (IP address or DNS name)
     */
    public String getNodeAddress();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * @return returns the open return value
     */
    int open();
}
