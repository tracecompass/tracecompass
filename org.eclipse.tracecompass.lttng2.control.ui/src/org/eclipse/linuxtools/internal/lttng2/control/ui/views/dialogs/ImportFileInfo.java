/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.dialogs;

import org.eclipse.core.resources.IFolder;
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
    /**
     * Destination folder to import the trace to (full workspace path)
     */
    private IFolder fDestinationFolder;

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
     * @param destinationFolder
     *            The destination folder (full workspace path)
     * @param isOverwrite
     *            global overwrite flag
     */
    public ImportFileInfo(IRemoteFile file, String traceName, IFolder destinationFolder, boolean isOverwrite) {
        fRemoteFile = file;
        fLocalTraceName = traceName;
        fDestinationFolder = destinationFolder;
        fIsOverwrite = isOverwrite;
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
     * Returns the destination folder to import the trace to (full workspace path).
     *
     * @return destination folder
     */
    public IFolder getDestinationFolder() {
        return fDestinationFolder;
    }
}

