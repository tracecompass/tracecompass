/**********************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs;

import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.remote.core.IRemoteConnection;

/**
 * Interface for a dialog box for collecting parameter for loading a session.
 *
 * @author Bernd Hufmann
 */
public interface ILoadDialog {
    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Returns a list of file resources to load
     * @return a list of remote resources or null if local resources to upload
     */
    List<IFileStore> getRemoteResources();

    /**
     * Returns a list of file resources to load
     * @return a list of local resources to upload and load or null in remote case
     */
    List<IFileStore> getLocalResources();


    /**
     * Returns flag to overwrite existing session or not
     * @return flag to overwrite existing session or not
     */
    boolean isForce();

    /**
     * Sets the remote connection reference
     * @param connection
     *                a remote connection
     */
    void initialize(IRemoteConnection connection);

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Open method
     * @return the open return value
     */
    int open();
}
