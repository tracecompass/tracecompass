/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.timechart;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.resources.ITmfMarker;

/**
 * Provider for decorations in the time chart view
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TimeChartDecorationProvider {

    private final IFile fBookmarksFile;
    private final Set<Long> fBookmarksSet = new HashSet<>();
    private ITmfFilter fFilterFilter;
    private ITmfFilter fSearchFilter;

    /**
     * Constructor
     *
     * @param bookmarksFile
     *            Bookmark file associated with the trace
     */
    public TimeChartDecorationProvider(IFile bookmarksFile) {
        fBookmarksFile = bookmarksFile;
        refreshBookmarks();
    }

    /**
     * Retrieve the bookmark file that was assigned to this provider
     *
     * @return The bookmark file
     */
    public IFile getBookmarksFile() {
        return fBookmarksFile;
    }

    /**
     * Verify if the selected rank has a bookmark assigned to it.
     *
     * @param rank
     *            The rank to check for
     * @return If there is a bookmark there
     */
    public boolean isBookmark(long rank) {
        return fBookmarksSet.contains(rank);
    }

    /**
     * Refresh the bookmark display.
     */
    public void refreshBookmarks() {
        try {
            fBookmarksSet.clear();
            if (fBookmarksFile == null) {
                return;
            }
            for (IMarker bookmark : fBookmarksFile.findMarkers(
                    IMarker.BOOKMARK, false, IResource.DEPTH_ZERO)) {
                /* try location as an integer for backward compatibility */
                long rank = bookmark.getAttribute(IMarker.LOCATION, -1);
                if (rank == -1) {
                    String rankString = bookmark.getAttribute(ITmfMarker.MARKER_RANK, (String) null);
                    try {
                        rank = Long.parseLong(rankString);
                    } catch (NumberFormatException e) {
                        /* ignored */
                    }
                }
                if (rank != -1) {
                    fBookmarksSet.add(rank);
                }
            }
        } catch (CoreException e) {
            Activator.getDefault().logError("Error refreshing bookmarks", e); //$NON-NLS-1$
        }
    }

    /**
     * Notify that a filter is now applied on the view.
     *
     * @param filter
     *            The filter that was applied
     */
    public void filterApplied(ITmfFilter filter) {
        fFilterFilter = filter;
    }

    /**
     * Check if an event is currently visible in the view or not.
     *
     * @param event
     *            The event to check for
     * @return If the event is visible or not
     */
    public boolean isVisible(ITmfEvent event) {
        if (fFilterFilter != null) {
            return fFilterFilter.matches(event);
        }
        return true;
    }

    /**
     * Notify that a search is applied on the view.
     *
     * @param filter
     *            The search filter that was applied
     */
    public void searchApplied(ITmfFilter filter) {
        fSearchFilter = filter;
    }

    /**
     * Verify if the currently active search filter applies to the given event
     * or not.
     *
     * @param event
     *            The event to check for
     * @return If the event matches
     */
    public boolean isSearchMatch(ITmfEvent event) {
        if (fSearchFilter != null) {
            return fSearchFilter.matches(event);
        }
        return false;
    }

}
