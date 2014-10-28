/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Simon Delisle - Added a method to remove the iterator
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core;

import java.util.HashMap;

/**
 * Ctf Iterator Manager, allows mapping of iterators (a limited resource) to
 * contexts (many many resources).
 *
 * @author Matthew Khouzam
 * @version 1.0
 * @since 1.1
 */
public abstract class CtfIteratorCEO {
    /*
     * A side note synchronized works on the whole object, Therefore add and
     * remove will be thread safe.
     */

    /*
     * The map of traces to trace managers.
     */
    private static HashMap<CtfTmfTrace, CtfIteratorManager> map = new HashMap<>();

    /**
     * Registers a trace to the iterator manager, the trace can now get
     * iterators.
     *
     * @param trace
     *            the trace to register.
     */
    public static synchronized void addTrace(final CtfTmfTrace trace) {
        map.put(trace, new CtfIteratorManager(trace));
    }

    /**
     * Removes a trace to the iterator manager.
     *
     * @param trace
     *            the trace to register.
     */
    public static synchronized void removeTrace(final CtfTmfTrace trace) {
        CtfIteratorManager mgr = map.remove(trace);
        if (mgr != null) {
            mgr.clear();
        }
    }

    /**
     * Get an iterator for a given trace and context.
     *
     * @param trace
     *            the trace
     * @param ctx
     *            the context
     * @return the iterator
     * @since 2.0
     */
    public static synchronized CtfIterator getIterator(final CtfTmfTrace trace,
            final CtfTmfContext ctx) {
        return map.get(trace).getIterator(ctx);
    }

    /**
     * Remove an iterator for a given trace and context
     *
     * @param trace
     *            the trace
     * @param ctx
     *            the context
     * @since 2.1
     */
    public static synchronized void removeIterator(final CtfTmfTrace trace, final CtfTmfContext ctx) {
        CtfIteratorManager traceManager = map.get(trace);
        if (traceManager != null) {
            traceManager.removeIterator(ctx);
        }
    }
}
