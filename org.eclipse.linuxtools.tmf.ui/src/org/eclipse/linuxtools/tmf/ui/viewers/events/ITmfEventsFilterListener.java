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

package org.eclipse.linuxtools.tmf.ui.viewers.events;

import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * A filter/search event listener
 * 
 * @version 1.0
 * @author Patrick Tasse
 */
public interface ITmfEventsFilterListener {

	    /**
     * Notify this listener that a filter has been applied.
     *
     * @param filter
     *            The filter that was applied
     * @param trace
     *            The trace on which this filter is applied
     */
	public void filterApplied(ITmfFilter filter, ITmfTrace<?> trace);

    /**
     * Notify this listener that a new search has been run.
     *
     * @param filter
     *            The filter that was applied
     * @param trace
     *            The trace on which this filter is applied
     */
	public void searchApplied(ITmfFilter filter, ITmfTrace<?> trace);

}
