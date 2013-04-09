/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.tabsview;

import java.util.Collection;
import java.util.HashMap;

import org.eclipse.linuxtools.tmf.ui.viewers.ITmfViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Allows the user to create multiple tabs which makes it look like folders. It
 * simplifies the management of the viewer contained in each tab.
 *
 * The indexing of the viewers is based on their name.
 *
 * @author Mathieu Denis
 * @version 2.0
 * @since 2.0
 */
public class TmfViewerFolder extends Composite {

    /**
     * The list of viewers in the folder
     */
    private final HashMap<String, ITmfViewer> fViewers;

    /**
     * The parent folder that contains all viewers
     */
    private CTabFolder fFolder;

    /**
     * Constructor with empty style
     *
     * @param parent
     *            The parent composite
     */
    public TmfViewerFolder(Composite parent) {
        this(parent, SWT.NONE);
    }

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite
     * @param style
     *            The style of the view that will be created
     */
    public TmfViewerFolder(Composite parent, int style) {
        super(parent, style);
        setLayout(new FillLayout());

        fViewers = new HashMap<String, ITmfViewer>();
        initFolder();
    }

    @Override
    public void dispose() {
        super.dispose();
        for (ITmfViewer viewer : fViewers.values()) {
            viewer.dispose();
        }
        if (fFolder != null) {
            fFolder.dispose();
        }
    }

    /**
     * Disposes of all the viewers contained in the folder and restart to a
     * clean state.
     */
    public void clear() {
        for (ITmfViewer viewer : fViewers.values()) {
            viewer.dispose();
        }
        fViewers.clear();
        fFolder.dispose();
        initFolder();
    }

    /**
     * Create a new tab that will hold the viewer content. The viewer name will
     * be used as the name for the tab. The viewer ID must be unique and can be
     * used to retrieve the viewer from the folder.
     *
     * The parent of the viewer control must be the folder returned by
     * {@link #getParentFolder()}
     *
     * @param viewer
     *            The viewer to put in the new tab
     * @param viewerID
     *            The ID that will be assigned to this viewer for easy
     *            retrieving
     * @param style
     *            The style of the widget to build
     * @return true on success, false otherwise
     */
    public boolean addTab(ITmfViewer viewer, String viewerID, int style) {
        if (fFolder == null
                || viewer.getControl().getParent() != fFolder
                || fViewers.containsKey(viewerID)) {
            return false;
        }
        CTabItem item = new CTabItem(fFolder, style);
        item.setText(viewer.getName());
        item.setControl(viewer.getControl());
        // Register the viewer in the map to dispose it at closing time
        fViewers.put(viewerID, viewer);
        return true;
    }

    /**
     * Gets the folder that will be use as the parent of tabs that will hold the
     * viewer.
     *
     * In order to be able to add new tabs in this view, the parent of the
     * viewer control has to be this composite.
     *
     * @return the folder composite to use as the parent for the viewer control
     *         to create.
     */
    public Composite getParentFolder() {
        return fFolder;
    }

    /**
     * Gets a viewer based on his name.
     *
     * @param viewerName
     *            The name of the viewer to find in the folder
     * @return The viewer which name is viewerName, or null if there is no such
     *         viewer
     */
    public ITmfViewer getViewer(String viewerName) {
        return fViewers.get(viewerName);
    }

    /**
     * Gets the viewers list contained in the folder view. The list can return
     * the viewers in any order. It is not to be assumed that the viewers are
     * returned in the same order as they were inserted.
     *
     * @return a collection of viewers contained in this view.
     */
    public Collection<ITmfViewer> getViewers() {
        return fViewers.values();
    }

    /**
     * Selects the tab at the specified index from the insertion order
     *
     * @param index
     *            The index of the tab to be selected
     * @throws SWTException
     *             <ul>
     *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
     *             </li>
     *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     *             thread that created the receiver</li>
     *             </ul>
     */
    public void setSelection(int index) throws SWTException {
        fFolder.setSelection(index);
    }

    /**
     * Initializes the folder or put it a back to a clean state.
     */
    private void initFolder() {
        if (fFolder != null) {
            fFolder.dispose();
        }
        fFolder = new CTabFolder(this, SWT.LEFT | SWT.BORDER);
        fFolder.setSimple(false);
    }
}
