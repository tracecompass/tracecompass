/*******************************************************************************
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
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageFilesElement;

/**
 * An remote trace element representing the trace files of a trace.
 *
 * @author Bernd Hufmann
 */
public class RemoteImportTraceFilesElement extends TracePackageFilesElement {

    private final IFileStore fRemoteFile;

    /**
     * Constructs an instance of ExportTraceFilesElement when exporting
     *
     * @param parent
     *            the parent of this element, can be set to null
     * @param resource
     *            the resource representing the trace file or folder in the
     *            workspace
     */
    public RemoteImportTraceFilesElement(TracePackageElement parent, IResource resource) {
        super(parent, resource);
        fRemoteFile = null;
    }

    /**
     * Constructs an instance of ExportTraceFilesElement when importing
     *
     * @param parent
     *            the parent of this element, can be set to null
     * @param fileName
     *            the name of the file to be imported
     * @param remoteFile
     *            the remote file representing the trace
     */
    public RemoteImportTraceFilesElement(TracePackageElement parent, String fileName, IFileStore remoteFile) {
        super(parent, fileName);
        fRemoteFile = remoteFile;
    }

    /**
     * Returns the remote file representing the trace
     * @return the remote file
     */
    public IFileStore getRemoteFile() {
        return fRemoteFile;
    }
}
