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

import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.swt.graphics.Image;

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