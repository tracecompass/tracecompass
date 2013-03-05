/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.partial;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.linuxtools.internal.tmf.core.statesystem.AttributeTree;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.StateSystem;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.NullBackend;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;

/**
 * State system interface-like extension to use with partial state histories.
 *
 * It mainly exposes the {@link #replaceOngoingState} method, which allows
 * seeking the state system to a different point by updating its "ongoing" state
 * values.
 *
 * @author Alexandre Montplaisir
 */
public class PartialStateSystem extends StateSystem {

    private static final String errMsg = "Partial state system should not modify the attribute tree!"; //$NON-NLS-1$

    private final CountDownLatch ssAssignedLatch = new CountDownLatch(1);
    private final Lock queryLock = new ReentrantLock();

    /**
     * Reference to the real upstream state system. This is used so we can read
     * its attribute tree.
     */
    private StateSystem realStateSystem = null;

    /**
     * Constructor
     */
    public PartialStateSystem() {
        /*
         * We use a Null back end here : we only use this state system for its
         * "ongoing" values, so no need to save the changes that are inserted.
         */
        super(new NullBackend());
    }

    /**
     * Assign the upstream state system to this one.
     *
     * @param ss
     *            The real state system
     */
    public void assignUpstream(StateSystem ss) {
        realStateSystem = ss;
        ssAssignedLatch.countDown();
    }

    ITmfStateSystem getUpstreamSS() {
        return realStateSystem;
    }

    @Override
    public void replaceOngoingState(List<ITmfStateInterval> ongoingIntervals) {
        /* We simply publicize StateSystem's method */
        super.replaceOngoingState(ongoingIntervals);
    }

    // ------------------------------------------------------------------------
    // Methods regarding the query lock
    // ------------------------------------------------------------------------

    /**
     * Take this inner state system's lock before doing a query.
     *
     * When doing queries, you should take the lock, then run
     * {@link #replaceOngoingState}, then send events to its state provider
     * input to cause state changes, and then call {@link #queryOngoingState} to
     * get the states at the new "current time".
     *
     * Only after all that it would be safe to release the lock.
     */
    public void takeQueryLock() {
        try {
            queryLock.lockInterruptibly();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Release the query lock, when you are done with your query.
     */
    public void releaseQueryLock() {
        queryLock.unlock();
    }

    @Override
    public AttributeTree getAttributeTree() {
        waitUntilReady();
        return realStateSystem.getAttributeTree();
    }

    /*
     * Override these methods to make sure we don't try to overwrite the
     * "real" upstream attribute tree.
     */

    @Override
    protected void addEmptyAttribute() {
        throw new RuntimeException(errMsg);
    }

    @Override
    public int getQuarkAbsoluteAndAdd(String... attribute) {
        waitUntilReady();
        try {
            return realStateSystem.getQuarkAbsolute(attribute);
        } catch (AttributeNotFoundException e) {
            throw new RuntimeException(errMsg);
        }
    }

    @Override
    public int getQuarkRelativeAndAdd(int startingNodeQuark, String... subPath) {
        waitUntilReady();
        try {
            return realStateSystem.getQuarkRelative(startingNodeQuark, subPath);
        } catch (AttributeNotFoundException e) {
            throw new RuntimeException(errMsg);
        }
    }

    private void waitUntilReady() {
        try {
            ssAssignedLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
