/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.swt.graphics.Image;

/**
 * An ExportTraceElement representing the trace files of a trace.
 *
 * @author Marc-Andre Laperle
 */
public class TracePackageFilesElement extends TracePackageElement {

    private static final String TRACE_ICON_PATH = "icons/elcl16/trace.gif"; //$NON-NLS-1$
    private final String fFileName;
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
        return Messages.TracePackage_TraceElement;
    }

    @Override
    public Image getImage() {
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

}
