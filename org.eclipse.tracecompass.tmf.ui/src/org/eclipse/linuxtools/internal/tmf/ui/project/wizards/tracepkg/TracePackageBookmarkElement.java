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

import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.swt.graphics.Image;

/**
 * A trace package element representing the bookmarks of a trace
 *
 * @author Marc-Andre Laperle
 */
public class TracePackageBookmarkElement extends TracePackageElement {
    private static final String BOOKMARK_IMAGE_PATH = "icons/elcl16/bookmark_obj.gif"; //$NON-NLS-1$
    private final List<Map<String, String>> bookmarkAttribs;

    /**
     * Construct a bookmark element containing all the bookmarks
     *
     * @param parent
     *            the parent node
     * @param bookmarkAttribs
     *            the bookmarks for the trace
     */
    public TracePackageBookmarkElement(TracePackageElement parent, List<Map<String, String>> bookmarkAttribs) {
        super(parent);
        this.bookmarkAttribs = bookmarkAttribs;
    }

    @Override
    public long getSize(boolean checkedOnly) {
        return 0;
    }

    @Override
    public String getText() {
        return Messages.TracePackage_Bookmarks;
    }

    @Override
    public Image getImage() {
        return Activator.getDefault().getImageFromImageRegistry(BOOKMARK_IMAGE_PATH);
    }

    /**
     * Get all the bookmarks
     *
     * @return the bookmarks
     */
    public List<Map<String, String>> getBookmarks() {
        return bookmarkAttribs;
    }
}