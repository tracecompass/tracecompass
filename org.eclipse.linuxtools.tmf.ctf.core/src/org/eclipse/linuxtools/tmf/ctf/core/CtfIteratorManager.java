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
package org.eclipse.linuxtools.tmf.ctf.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Ctf Iterator Manager, allows mapping of iterators (a limited resource) to
 * contexts (many many resources).
 *
 * @author Matthew Khouzam
 * @version 1.0
 * @since 1.1
 */
public abstract class CtfIteratorManager {
    /*
     * A side note synchronized works on the whole object, Therefore add and
     * remove will be thread safe.
     */

    /*
     * The map of traces to trace managers.
     */
    private static HashMap<CtfTmfTrace, CtfTraceManager> map = new HashMap<>();

    /**
     * Registers a trace to the iterator manager, the trace can now get
     * iterators.
     *
     * @param trace
     *            the trace to register.
     */
    public static synchronized void addTrace(final CtfTmfTrace trace) {
        map.put(trace, new CtfTraceManager(trace));
    }

    /**
     * Removes a trace to the iterator manager.
     *
     * @param trace
     *            the trace to register.
     */
    public static synchronized void removeTrace(final CtfTmfTrace trace) {
        CtfTraceManager mgr = map.remove(trace);
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
        CtfTraceManager traceManager = map.get(trace);
        if (traceManager != null) {
            traceManager.removeIterator(ctx);
        }
    }
}

/**
 * A trace manager
 *
 * @author Matthew Khouzam
 */
class CtfTraceManager {
    /*
     * Cache size. Under 1023 on linux32 systems. Number of file handles
     * created.
     */
    private final static int MAX_SIZE = 100;
    /*
     * The map of the cache.
     */
    private final HashMap<CtfTmfContext, CtfIterator> fMap;
    /*
     * An array pointing to the same cache. this allows fast "random" accesses.
     */
    private final ArrayList<CtfTmfContext> fRandomAccess;
    /*
     * The parent trace
     */
    private final CtfTmfTrace fTrace;
    /*
     * Random number generator
     */
    private final Random fRnd;

    public CtfTraceManager(CtfTmfTrace trace) {
        fMap = new HashMap<>();
        fRandomAccess = new ArrayList<>();
        fRnd = new Random(System.nanoTime());
        fTrace = trace;
    }

    /**
     * This needs explaining: the iterator table is effectively a cache.
     * Originally the contexts had a 1 to 1 structure with the file handles of a
     * trace. This failed since there is a limit to how many file handles we can
     * have opened simultaneously. Then a round-robin scheme was implemented,
     * this lead up to a two competing contexts syncing up and using the same
     * file handler, causing horrible slowdowns. Now a random replacement
     * algorithm is selected. This is the same as used by arm processors, and it
     * works quite well when many cores so this looks promising for very
     * multi-threaded systems.
     *
     * @param context
     *            the context to look up
     * @return the iterator referring to the context
     */
    public CtfIterator getIterator(final CtfTmfContext context) {
        /*
         * if the element is in the map, we don't need to do anything else.
         */
        CtfIterator retVal = fMap.get(context);
        if (retVal == null) {
            /*
             * Assign an iterator to a context, this means we will need to seek
             * at the end.
             */
            if (fRandomAccess.size() < MAX_SIZE) {
                /*
                 * if we're not full yet, just add an element.
                 */
                retVal = fTrace.createIterator();
                addElement(context, retVal);

            } else {
                /*
                 * if we're full, randomly replace an element
                 */
                retVal = replaceRandomElement(context);
            }
            if (context.getLocation() != null) {
                final CtfLocationInfo location = (CtfLocationInfo) context.getLocation().getLocationInfo();
                retVal.seek(location);
            }
        }
        return retVal;
    }

    public void removeIterator(CtfTmfContext context) {
        try (CtfIterator removed = fMap.remove(context)) {
        }

        fRandomAccess.remove(context);
    }

    /**
     * Add a pair of context and element to the hashmap and the arraylist.
     *
     * @param context
     *            the context
     * @param elem
     *            the iterator
     */
    private void addElement(final CtfTmfContext context,
            final CtfIterator elem) {
        fMap.put(context, elem);
        fRandomAccess.add(context);
    }

    /**
     * Replace a random element
     *
     * @param context
     *            the context to swap in
     * @return the iterator of the removed elements.
     */
    private CtfIterator replaceRandomElement(
            final CtfTmfContext context) {
        /*
         * This needs some explanation too: We need to select a random victim
         * and remove it. The order of the elements is not important, so instead
         * of just calling arraylist.remove(element) which has an O(n)
         * complexity, we pick an random number. The element is swapped out of
         * the array and removed and replaced in the hashmap.
         */
        final int size = fRandomAccess.size();
        final int pos = fRnd.nextInt(size);
        final CtfTmfContext victim = fRandomAccess.get(pos);
        fRandomAccess.set(pos, context);
        final CtfIterator elem = fMap.remove(victim);
        fMap.put(context, elem);
        victim.dispose();
        return elem;
    }

    void clear() {
        for (CtfIterator iterator : fMap.values()) {
            iterator.dispose();
        }
        fMap.clear();
        fRandomAccess.clear();
    }
}
