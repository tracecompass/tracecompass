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
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;

import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionGroup;

/**
 * <p>
 * Interface for create session dialog.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface ICreateSessionDialog {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * @return the session name.
     */
    public String getSessionName();

    /**
     * @return the session path (null for default path)
     */
    public String getSessionPath();

    /**
     * @return true for default location else false
     */
    public boolean isDefaultSessionPath();

    /**
     * Initializes the dialog box.
     * @param group - the session group
     */
    public void initialize(TraceSessionGroup group);

    /**
     * @return true if traces is to be streamed else false.
     */
    public boolean isStreamedTrace();

    /**
     * Get the network URL in case control and data is configured together otherwise null
     * If it returns a non-null value, getControlUrl() and getDataUrl() have to return null.
     * @return The network URL or null.
     */
    public String getNetworkUrl();

    /**
     * Get the control URL in case control and data is configured separately.
     * If it returns a non-null value, getDataUrl() has to return a valid value too
     * and getNetworkUrl() has to return null.
     *
     * @return The control URL or null.
     */
    public String getControlUrl();

    /**
     * Get the data URL in case control and data is configured separately.
     * If it returns a non-null value, getControlUrl() has to return a valid value too
     * and getNetworkUrl() has to return null.
     *
     * @return The data URL or null.
     */
    public String getDataUrl();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * @return the open return value
     */
    int open();
}
