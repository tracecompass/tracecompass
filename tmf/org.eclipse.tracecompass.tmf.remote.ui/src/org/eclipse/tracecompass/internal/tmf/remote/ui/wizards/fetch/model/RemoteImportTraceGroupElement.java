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
 *   Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model;

import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.tmf.remote.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;

/**
 * An RemoteImportTraceGroupElement representing the a group of traces under a
 * root path.
 *
 * @author Marc-Andre Laperle
 */
public class RemoteImportTraceGroupElement extends TracePackageElement {

    private static final String IMAGE_PATH = "icons/obj/trace_group.gif"; //$NON-NLS-1$
    private String fRootImportPath;
    private boolean fRecursive;

    /**
     * Constructs an instance of RemoteImportTraceGroupElement
     *
     * @param parent
     *            the parent of this element, can be set to null
     * @param rootImportPath
     *            the root path where the traces should be imported from
     */
    public RemoteImportTraceGroupElement(TracePackageElement parent,
            String rootImportPath) {
        super(parent);
        fRootImportPath = rootImportPath;
        fRecursive = false;
    }

    @Override
    public String getText() {
        return fRootImportPath + (isRecursive() ? " (recursive)" : ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns whether or not the group of traces should be imported recursively
     * from the root path.
     *
     * @return whether or not the trace group is recursive
     */
    public boolean isRecursive() {
        return fRecursive;
    }

    /**
     * Sets whether or not the group of traces should be imported recursively
     * from the root path.
     *
     * @param recursive
     *            if the element should be recursive
     */
    public void setRecursive(boolean recursive) {
        fRecursive = recursive;
    }

    @Override
    public Image getImage() {
        return Activator.getDefault().getImageFromImageRegistry(IMAGE_PATH);
    }

    /**
     * Get the root path where the traces should be imported from.
     *
     * @return the root import path
     */
    public String getRootImportPath() {
        return fRootImportPath;
    }

    /**
     * Set the root path where the traces should be imported from.
     *
     * @param rootImportPath
     *            the root import path
     */
    public void setRootImportPath(String rootImportPath) {
        fRootImportPath = rootImportPath;
    }
}
