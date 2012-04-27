/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;

/**
 * The generic trace indexer in TMF.
 * 
 * @since 1.0
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see ITmfTrace
 * @see ITmfEvent
 */
public interface ITmfTraceIndexer<T extends ITmfTrace<ITmfEvent>> {

    /**
     * Start an asynchronous index building job and waits for the job completion
     * if required. Typically, the indexing job sends notifications at regular
     * intervals to indicate its progress.
     * 
     * @param waitForCompletion
     */
    public void buildIndex(boolean waitForCompletion);
    
    /**
     * Adds an entry to the trace index. 
     * 
     * @param context
     * @param timestamp
     */
    public void updateIndex(ITmfContext context, ITmfTimestamp timestamp);
    
    /**
     * Returns the context of the checkpoint immediately preceding the requested
     * timestamp (or at the timestamp if it coincides with a checkpoint).
     * 
     * @param timestamp the requested timestamp
     * @return the checkpoint context
     */
    public ITmfContext seekIndex(ITmfTimestamp timestamp);

    /**
     * Returns the context of the checkpoint immediately preceding the requested
     * rank (or at rank if it coincides with a checkpoint).
     * 
     * @param rank the requested event rank
     * @return the checkpoint context
     */
    public ITmfContext seekIndex(long rank);

}
