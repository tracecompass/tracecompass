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

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;

/**
 * A trace package element representing a single supplementary file
 *
 * @author Marc-Andre Laperle
 */
public class TracePackageSupplFileElement extends TracePackageElement {

    private static final String SUPPL_FILE_ICON_PATH = "icons/obj16/thread_obj.gif"; //$NON-NLS-1$

    private final IResource fResource;
    private final String fSuppFileName;

    /**
     * Constructor used when exporting
     *
     * @param resource
     *            the resource representing this supplementary file in the
     *            workspace
     * @param suppFileName
     *            the supplementary file name
     * @param parent
     *            the parent element
     */
    public TracePackageSupplFileElement(IResource resource, String suppFileName, TracePackageElement parent) {
        super(parent);
        fResource = resource;
        fSuppFileName = suppFileName;
    }

    /**
     * Constructor used when importing
     *
     * @param suppFileName
     *            the name to be used for the supplementary file in the
     *            workspace
     * @param parent
     *            the parent element
     */
    public TracePackageSupplFileElement(String suppFileName, TracePackageElement parent) {
        super(parent);
        fSuppFileName = suppFileName;
        fResource = null;
    }

    /**
     * Get the resource corresponding to this supplementary file
     *
     * @return the resource corresponding to this supplementary file
     */
    public IResource getResource() {
        return fResource;
    }

    @Override
    public String getText() {
        return fSuppFileName;
    }

    @Override
    public long getSize(boolean checkedOnly) {
        if (checkedOnly && !isChecked()) {
            return 0;
        }

        return fResource.getLocation().toFile().length();
    }

    @Override
    public Image getImage() {
        return Activator.getDefault().getImageFromImageRegistry(SUPPL_FILE_ICON_PATH);
    }

}