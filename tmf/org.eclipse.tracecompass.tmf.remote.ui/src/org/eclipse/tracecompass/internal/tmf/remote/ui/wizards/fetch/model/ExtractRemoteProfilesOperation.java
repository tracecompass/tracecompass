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
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tracecompass.internal.tmf.remote.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.AbstractTracePackageOperation;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;

/**
 * An operation that extracts profiles information from a file
 *
 * @author Marc-Andre Laperle
 */
public class ExtractRemoteProfilesOperation extends AbstractTracePackageOperation {

    /**
     * Constructs a new import operation for reading the profiles
     *
     * @param fileName
     *            the output file name
     */
    public ExtractRemoteProfilesOperation(String fileName) {
        super(fileName);
    }

    /**
     * Run the extract profiles operation. The status (result) of the operation
     * can be obtained with {@link #getStatus}
     *
     * @param progressMonitor
     *            the progress monitor to use to display progress and receive
     *            requests for cancellation
     */
    @Override
    public void run(IProgressMonitor progressMonitor) {
        TracePackageElement[] elements = null;
        try {
            progressMonitor.worked(1);
            File file = new File(getFileName());
            progressMonitor.worked(1);
            if (!file.exists()) {
                setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Invalid format")); //$NON-NLS-1$
                return;
            }

            // TODO backwards compatibility for location
            try (FileInputStream inputStream = new FileInputStream(file)) {
                RemoteImportProfilesReader.validate(inputStream);
            }

            try (FileInputStream inputStream = new FileInputStream(file)) {
                elements = RemoteImportProfilesReader.loadElementsFromProfiles(inputStream);
            }

            progressMonitor.worked(1);

            setResultElements(elements);
            setStatus(Status.OK_STATUS);
        } catch (Exception e) {
            setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error reading profiles", e)); //$NON-NLS-1$
        }
    }
}
