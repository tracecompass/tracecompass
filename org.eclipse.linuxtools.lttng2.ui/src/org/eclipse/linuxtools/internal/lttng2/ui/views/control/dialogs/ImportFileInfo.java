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
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;

import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

/**
 * <p>
 * Helper class for storing information about a remote file to import.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class ImportFileInfo {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * Remote file reference
     */
    private IRemoteFile fRemoteFile;
    /**
     * Local Trace Name
     */
    private String fLocalTraceName;
    /**
     * Global overwrite flag
     */
    private boolean fIsOverwrite;

    private boolean fIsKernel;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Standard constructor
     *
     * @param file
     *            A remote file reference
     * @param traceName
     *            A trace name
     * @param isOverwrite
     *            global overwrite flag
     * @param isKernel
     *            <code>true</code> if it is a kernel trace else
     *            <code>false</code>
     */
    public ImportFileInfo(IRemoteFile file, String traceName, boolean isOverwrite, boolean isKernel) {
        fRemoteFile = file;
        fLocalTraceName = traceName;
        fIsOverwrite = isOverwrite;
        fIsKernel = isKernel;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * @return name of traces after importing
     */
    public String getLocalTraceName() {
        return fLocalTraceName;
    }

    /**
     * Sets the local trace name
     *
     * @param importTraceName
     *            - local name of trace to set (name after importing)
     */
    public void setLocalTraceName(String importTraceName) {
        this.fLocalTraceName = importTraceName;
    }
    /**
     * @return true if local trace should be overwritten if a trace with the same name already exists.
     */
    public boolean isOverwrite() {
        return fIsOverwrite;
    }
    /**
     * Sets the overwrite flag.
     * @param isOverwrite If the Overwrite checkbox is checked or not
     */
    public void setOverwrite(boolean isOverwrite) {
        this.fIsOverwrite = isOverwrite;
    }

    /**
     * @return the remote file implementation.
     */
    public IRemoteFile getImportFile() {
        return fRemoteFile;
    }

    /**
     * Sets the remote file implementation
     *
     * @param remoteFile
     *            The remote file implementation.
     */
    public void setRemoteFile(IRemoteFile remoteFile) {
        fRemoteFile = remoteFile;
    }

    /**
     * Returns whether it is a kernel trace or not
     *
     * @return <code>true</code> if it is a kernel trace else <code>false</code>
     */
    public boolean isKernel() {
        return fIsKernel;
    }

    /**
     * Sets whether it is a kernel trace or not
     *
     * @param isKernel
     *            <code>true</code> for kernel trace else <code>false</code>
     */
    public void setKernel(boolean isKernel) {
        fIsKernel = isKernel;
    }
}

