/*******************************************************************************
 * Copyright (c) 2013, 2018 Ericsson
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

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;

/**
 * An ExportTraceElement representing the trace files of a trace.
 *
 * @author Marc-Andre Laperle
 */
public class TracePackageFilesElement extends TracePackageElement {

    private static final String TRACE_ICON_PATH = "icons/elcl16/trace.gif"; //$NON-NLS-1$
    private static final String EXPERIMENT_ICON_PATH = "icons/elcl16/experiment.gif"; //$NON-NLS-1$
    private String fFileName;
    private final IResource fResource;
    private long fSize = -1;

    /**
     * Constructs an instance of ExportTraceFilesElement when exporting
     *
     * @param parent
     *            the parent of this element, can be set to null
     * @param resource
     *            the resource representing the trace file or folder in the
     *            workspace
     */
    public TracePackageFilesElement(TracePackageElement parent, IResource resource) {
        super(parent);
        fFileName = null;
        fResource = resource;
    }

    /**
     * Constructs an instance of ExportTraceFilesElement when importing
     *
     * @param parent
     *            the parent of this element, can be set to null
     * @param fileName
     *            the name of the file to be imported
     */
    public TracePackageFilesElement(TracePackageElement parent, String fileName) {
        super(parent);
        fFileName = fileName;
        fResource = null;
    }

    private long getSize(File file) {
        if (file.isDirectory()) {
            long size = 0;
            for (File f : file.listFiles()) {
                size += getSize(f);
            }
            return size;
        }

        return file.length();
    }

    @Override
    public long getSize(boolean checkedOnly) {
        if (checkedOnly && !isChecked()) {
            return 0;
        }

        if (fSize == -1 && fResource.exists()) {
            File file = fResource.getLocation().toFile();
            fSize = getSize(file);
        }

        return fSize;
    }

    @Override
    public String getText() {
        if (getParent() instanceof TracePackageExperimentElement) {
            return Messages.TracePackage_ExperimentElement;
        }
        return Messages.TracePackage_TraceElement;
    }

    @Override
    public Image getImage() {
        if (getParent() instanceof TracePackageExperimentElement) {
            return Activator.getDefault().getImageFromImageRegistry(EXPERIMENT_ICON_PATH);
        }
        return Activator.getDefault().getImageFromImageRegistry(TRACE_ICON_PATH);
    }

    /**
     * Get the file name for this trace file or folder
     *
     * @return the file name for this trace file or folder
     */
    public String getFileName() {
        return fFileName;
    }

    /**
     * Set the file name for this trace file or folder
     *
     * @param fileName the file name for this trace file or folder
     */
    public void setFileName(String fileName) {
        fFileName = fileName;
    }

}
