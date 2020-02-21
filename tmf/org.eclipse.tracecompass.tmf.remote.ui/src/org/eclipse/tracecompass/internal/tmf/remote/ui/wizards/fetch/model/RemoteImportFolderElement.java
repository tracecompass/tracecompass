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
 *     Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model;

import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * An RemoteImportFolderElement representing a remote folder.
 *
 * @author Patrick Tasse
 */
public class RemoteImportFolderElement extends TracePackageElement {

    private static final Image IMAGE = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
    private String fFolderName;

    /**
     * Constructs an instance of RemoteImportTraceGroupElement
     *
     * @param parent
     *            the parent of this element, can be set to null
     * @param folderName
     *            the remote folder name
     */
    public RemoteImportFolderElement(TracePackageElement parent, String folderName) {
        super(parent);
        fFolderName = folderName;
    }

    @Override
    public String getText() {
        return fFolderName;
    }

    @Override
    public Image getImage() {
        return IMAGE;
    }

    /**
     * Get the folder name.
     *
     * @return the folder name
     */
    public String getFolderName() {
        return fFolderName;
    }

    /**
     * Set the folder name.
     *
     * @param folderName
     *            the folder name
     */
    public void setFolderName(String folderName) {
        fFolderName = folderName;
    }
}
