/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.timechart;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;

public class TimeChartDecorationProvider {

	private IFile fBookmarksFile;
    private Set<Long> fBookmarksSet = new HashSet<Long>();
    private ITmfFilter fFilterFilter;
    private ITmfFilter fSearchFilter;

	public TimeChartDecorationProvider(IFile bookmarksFile) {
	    fBookmarksFile = bookmarksFile;
	    refreshBookmarks();
    }

	public IFile getBookmarksFile() {
		return fBookmarksFile;
	}
	
	public boolean isBookmark(long rank) {
	    return fBookmarksSet.contains(rank);
    }
	
	public void refreshBookmarks() {
		try {
			fBookmarksSet.clear();
	        for (IMarker bookmark : fBookmarksFile.findMarkers(IMarker.BOOKMARK, false, IResource.DEPTH_ZERO)) {
	        	int location = bookmark.getAttribute(IMarker.LOCATION, -1);
	        	if (location != -1) {
	        		Long rank = (long) location;
	        		fBookmarksSet.add(rank);
	        	}
	        }
        } catch (CoreException e) {
            TmfUiPlugin.getDefault().logError("Error refreshing bookmarks", e); //$NON-NLS-1$
        }
    }

	public void filterApplied(ITmfFilter filter) {
		fFilterFilter = filter;
    }

	public boolean isVisible(ITmfEvent event) {
		if (fFilterFilter != null) {
			return fFilterFilter.matches(event);
		}
		return true;
	}
	
	public void searchApplied(ITmfFilter filter) {
		fSearchFilter = filter;
    }
	
	public boolean isSearchMatch(ITmfEvent event) {
		if (fSearchFilter != null) {
			return fSearchFilter.matches(event);
		}
		return false;
	}
	
}
