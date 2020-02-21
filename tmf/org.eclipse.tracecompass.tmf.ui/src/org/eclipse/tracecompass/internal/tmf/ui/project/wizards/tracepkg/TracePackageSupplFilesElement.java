/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
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

import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;

/**
 * A trace package element used for grouping supplementary file under a single
 * subtree
 *
 * @author Marc-Andre Laperle
 */
public class TracePackageSupplFilesElement extends TracePackageElement {

    private static final String SUPPL_FILE_ICON_PATH = "icons/obj16/thread_obj.gif"; //$NON-NLS-1$

    /**
     * Construct a new TracePackageSupplFilesElement instance
     *
     * @param parent
     *            the parent of this element, can be set to null
     */
    public TracePackageSupplFilesElement(TracePackageElement parent) {
        super(parent);
    }

    @Override
    public String getText() {
        return Messages.TracePackage_SupplementaryFiles;
    }

    @Override
    public Image getImage() {
        return Activator.getDefault().getImageFromImageRegistry(SUPPL_FILE_ICON_PATH);
    }
}