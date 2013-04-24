/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;

import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;
import org.eclipse.rse.core.model.IHost;

/**
 * <p>
 * Interface for connection information dialog.
 * </p>
 *
 *  @author Bernd Hufmann
 */
public interface INewConnectionDialog {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * @return the connection name (alias).
     */
    public String getConnectionName();

    /**
     * @return the host name (IP address or DNS name)
     */
    public String getHostName();

    /**
     * @return port of IP connection to be used
     */
    public int getPort();

    /**
     * Sets the trace control root
     * @param parent - the trace control parent
     */
    public void setTraceControlParent(ITraceControlComponent parent);

    /**
     * Sets the available hosts to select.
     * @param hosts - the available hosts
     */
    public void setHosts(IHost[] hosts);

    /**
     * Set the port of the IP connection to be used.
     * @param port - the IP port to set
     */
    public void setPort(int port);

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * @return returns the open return value
     */
    int open();
}
