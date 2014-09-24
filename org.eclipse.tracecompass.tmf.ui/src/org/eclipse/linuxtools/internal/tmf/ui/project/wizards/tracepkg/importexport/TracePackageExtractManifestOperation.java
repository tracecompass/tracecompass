/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.importexport;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.AbstractTracePackageOperation;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.ITracePackageConstants;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageFilesElement;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageTraceElement;

/**
 * An operation that extracts information from the manifest located in an
 * archive
 *
 * @author Marc-Andre Laperle
 */
public class TracePackageExtractManifestOperation extends AbstractTracePackageOperation {

    /**
     * Constructs a new import operation for reading the manifest
     *
     * @param fileName
     *            the output file name
     */
    public TracePackageExtractManifestOperation(String fileName) {
        super(fileName);
    }

    /**
     * Run extract the manifest operation. The status (result) of the operation
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
            ArchiveFile archiveFile = getSpecifiedArchiveFile();
            progressMonitor.worked(1);
            if (archiveFile == null) {
                setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TracePackageExtractManifestOperation_InvalidFormat));
                return;
            }

            Enumeration<?> entries = archiveFile.entries();

            boolean found = false;
            while (entries.hasMoreElements()) {
                ModalContext.checkCanceled(progressMonitor);

                ArchiveEntry entry = (ArchiveEntry) entries.nextElement();
                IPath p = new Path(entry.getName());
                //Remove project name
                p = p.removeFirstSegments(1);

                if (entry.getName().endsWith(ITracePackageConstants.MANIFEST_FILENAME)) {
                    found = true;
                    InputStream inputStream = archiveFile.getInputStream(entry);
                    ManifestReader.validateManifest(inputStream);

                    inputStream = archiveFile.getInputStream(entry);
                    elements = ManifestReader.loadElementsFromManifest(inputStream);
                    break;
                }

                progressMonitor.worked(1);
            }

            if (found) {
                setStatus(Status.OK_STATUS);
            }
            else {
                elements = generateElementsFromArchive();
                if (elements.length > 0) {
                    setStatus(Status.OK_STATUS);
                } else {
                    setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, MessageFormat.format(Messages.TracePackageExtractManifestOperation_ErrorManifestNotFound, ITracePackageConstants.MANIFEST_FILENAME)));
                }
            }

            setResultElements(elements);

        } catch (InterruptedException e) {
            setStatus(Status.CANCEL_STATUS);
        } catch (Exception e) {
            setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TracePackageExtractManifestOperation_ErrorReadingManifest, e));
        }
    }

    private TracePackageElement[] generateElementsFromArchive() {
        ArchiveFile archiveFile = getSpecifiedArchiveFile();
        Enumeration<?> entries = archiveFile.entries();
        Set<String> traceFileNames = new HashSet<>();
        while (entries.hasMoreElements()) {
            ArchiveEntry entry = (ArchiveEntry) entries.nextElement();
            String entryName = entry.getName();
            IPath fullArchivePath = new Path(entryName);
            if (!fullArchivePath.hasTrailingSeparator() && fullArchivePath.segmentCount() > 0) {
                traceFileNames.add(fullArchivePath.segment(0));
            }
        }

        List<TracePackageElement> packageElements = new ArrayList<>();
        for (String traceFileName : traceFileNames) {
            TracePackageTraceElement traceElement = new TracePackageTraceElement(null, traceFileName, null);
            traceElement.setChildren(new TracePackageElement[] { new TracePackageFilesElement(traceElement, traceFileName) });
            packageElements.add(traceElement);
        }

        return packageElements.toArray(new TracePackageElement[] {});
    }

}
