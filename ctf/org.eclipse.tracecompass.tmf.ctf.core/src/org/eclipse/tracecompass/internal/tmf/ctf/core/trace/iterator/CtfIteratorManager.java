/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Renamed/extracted from CtfTraceManager
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ctf.core.trace.iterator;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.ctf.core.Activator;
import org.eclipse.tracecompass.tmf.ctf.core.context.CtfLocationInfo;
import org.eclipse.tracecompass.tmf.ctf.core.context.CtfTmfContext;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

/**
 * A CTF trace iterator manager.
 *
 * Each instance of {@link CtfTmfTrace} should possess one of these, which will
 * manage the iterators that are opened to read that trace. This will allow
 * controlling the number of opened file handles per trace.
 *
 * @author Matthew Khouzam
 */
public class CtfIteratorManager {
    /*
     * Cache size. Under 1023 on linux32 systems. Number of file handles
     * created.
     */
    private static final int MAX_SIZE = 100;

    /** The map of the cache */
    private final Map<CtfTmfContext, CtfIterator> fMap;

    /** An array pointing to the same cache. this allows fast "random" accesses */
    private final List<CtfTmfContext> fRandomAccess;

    /** Lock for when we access the two previous data structures */
    private final Lock fAccessLock = new ReentrantLock();

    /** The parent trace */
    private final CtfTmfTrace fTrace;

    /** Random number generator */
    private final Random fRnd;

    /**
     * Constructor
     *
     * @param trace
     *            The trace whose iterators this manager will manage
     */
    public CtfIteratorManager(CtfTmfTrace trace) {
        fMap = new HashMap<>();
        fRandomAccess = new ArrayList<>();
        fRnd = new SecureRandom();
        fTrace = trace;
    }

    /**
     * This needs explaining: the iterator table is effectively a cache. Originally
     * the contexts had a 1 to 1 structure with the file handles of a trace. This
     * failed since there is a limit to how many file handles we can have opened
     * simultaneously. Then a round-robin scheme was implemented, this lead up to a
     * two competing contexts syncing up and using the same file handler, causing
     * horrible slowdowns. Now a random replacement algorithm is selected. This is
     * the same as used by arm processors, and it works quite well when many cores
     * so this looks promising for very multi-threaded systems.
     *
     * @param context
     *            the context to look up
     * @return the iterator referring to the context or null in the case of an error
     */
    public @Nullable CtfIterator getIterator(final CtfTmfContext context) {
        /*
         * if the element is in the map, we don't need to do anything else.
         */
        CtfIterator iter = fMap.get(context);
        if (iter == null) {

            fAccessLock.lock();
            try {
                /*
                 * Assign an iterator to a context.
                 */
                if (fRandomAccess.size() < MAX_SIZE) {
                    /*
                     * if we're not full yet, just add an element.
                     */
                    iter = (CtfIterator) fTrace.createIterator();
                    if (iter == null) {
                        return null;
                    }
                    addElement(context, iter);

                } else {
                    /*
                     * if we're full, randomly replace an element
                     */
                    iter = replaceRandomElement(context);
                }
                if (context.getLocation() != null) {
                    final CtfLocationInfo location = (CtfLocationInfo) context.getLocation().getLocationInfo();
                    iter.seek(location);
                }
            } finally {
                fAccessLock.unlock();
            }
        }
        return iter;
    }

    /**
     * Remove an iterator from this manager
     *
     * @param context
     *            The context of the iterator to remove
     */
    public void removeIterator(CtfTmfContext context) {
        fAccessLock.lock();
        try {
            /* The try below is only to auto-call CtfIterator.close() */
            try (CtfIterator removed = fMap.remove(context)) {
                // try with resource
            }
            fRandomAccess.remove(context);

        } finally {
            fAccessLock.unlock();
        }
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
        fAccessLock.lock();
        try {
            fMap.put(context, elem);
            fRandomAccess.add(context);

        } finally {
            fAccessLock.unlock();
        }
    }

    /**
     * Replace a random element
     *
     * @param context
     *            the context to swap in
     * @return the iterator of the removed elements.
     */
    private CtfIterator replaceRandomElement(final CtfTmfContext context) {
        /*
         * This needs some explanation too: We need to select a random victim
         * and remove it. The order of the elements is not important, so instead
         * of just calling arraylist.remove(element) which has an O(n)
         * complexity, we pick an random number. The element is swapped out of
         * the array and removed and replaced in the hashmap.
         */
        fAccessLock.lock(); // just in case, should only be called when already locked
        try {
            final int size = fRandomAccess.size();
            final int pos = fRnd.nextInt(size);
            final CtfTmfContext victim = fRandomAccess.get(pos);
            fRandomAccess.set(pos, context);
            CtfIterator elem = checkNotNull(fMap.remove(victim));
            if (elem.isClosed()) {
                /*
                 * In case the iterator streams have been closed, we need to
                 * replace it by a fresh new one to access the trace. We also
                 * report that as an error as it should not happen.
                 */
                Activator.getDefault().logError("Found closed iterator in iterator manager for trace " + victim.getTrace()); //$NON-NLS-1$

                elem.dispose();
                elem = (CtfIterator) fTrace.createIterator();
            }
            fMap.put(context, elem);
            victim.dispose();
            return elem;

        } finally {
            fAccessLock.unlock();
        }
    }

    /**
     * Dispose this iterator manager, which will close all the remaining
     * iterators.
     */
    public void dispose() {
        fAccessLock.lock();
        try {
            for (CtfIterator iterator : fMap.values()) {
                iterator.dispose();
            }
            fMap.clear();
            fRandomAccess.clear();

        } finally {
            fAccessLock.unlock();
        }
    }
}