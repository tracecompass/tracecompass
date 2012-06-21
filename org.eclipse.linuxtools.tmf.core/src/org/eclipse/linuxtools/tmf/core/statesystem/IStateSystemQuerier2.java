/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - IStateSystemQuerier API augmented for bug 382910
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statesystem;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;

/**
 * This interface augments the base interface IStateSystemQuerier.
 *
 * @version 1.1
 * @author Patrick Tasse
 * @since 1.1
 */
public interface IStateSystemQuerier2 extends IStateSystemQuerier {

    /**
     * Return the state history of a given attribute, but with at most one
     * update per "resolution". This can be useful for populating views (where
     * it's useless to have more than one query per pixel, for example).
     * A progress monitor can be used to cancel the query before completion.
     *
     * @param attributeQuark
     *            Which attribute this query is interested in
     * @param t1
     *            Start time of the range query
     * @param t2
     *            Target end time of the query. If t2 is greater than the end of
     *            the trace, we will return what we have up to the end of the
     *            history.
     * @param resolution
     *            The "step" of this query
     * @param monitor
     *            A progress monitor. If the monitor is canceled during a query,
     *            we will return what has been found up to that point.
     * @return The List of states that happened between t1 and t2
     * @throws TimeRangeException
     *             If t1 is invalid, if t2 <= t1, or if the resolution isn't
     *             greater than zero.
     * @throws AttributeNotFoundException
     *             If the attribute doesn't exist
     * @since 1.1
     */
    public List<ITmfStateInterval> queryHistoryRange(int attributeQuark,
            long t1, long t2, long resolution, IProgressMonitor monitor) throws TimeRangeException,
            AttributeNotFoundException;
}
