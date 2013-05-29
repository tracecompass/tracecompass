/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.statesystem;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.IStateHistoryBackend;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.interval.TmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemBuilder;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue.Type;

/**
 * This is the core class of the Generic State System. It contains all the
 * methods to build and query a state history. It's exposed externally through
 * the IStateSystemQuerier and IStateSystemBuilder interfaces, depending if the
 * user needs read-only access or read-write access.
 *
 * When building, DON'T FORGET to call .closeHistory() when you are done
 * inserting intervals, or the storage backend will have no way of knowing it
 * can close and write itself to disk, and its thread will keep running.
 *
 * @author alexmont
 *
 */
public class StateSystem implements ITmfStateSystemBuilder {

    /* References to the inner structures */
    private final AttributeTree attributeTree;
    private final TransientState transState;
    private final IStateHistoryBackend backend;

    /* Latch tracking if the state history is done building or not */
    private final CountDownLatch finishedLatch = new CountDownLatch(1);

    private boolean buildCancelled = false;
    private boolean isDisposed = false;

    /**
     * New-file constructor. For when you build a state system with a new file,
     * or if the back-end does not require a file on disk.
     *
     * @param backend
     *            Back-end plugin to use
     */
    public StateSystem(IStateHistoryBackend backend) {
        this.backend = backend;
        this.transState = new TransientState(backend);
        this.attributeTree = new AttributeTree(this);
    }

    /**
     * General constructor
     *
     * @param backend
     *            The "state history storage" back-end to use.
     * @param newFile
     *            Put true if this is a new history started from scratch. It is
     *            used to tell the state system where to get its attribute tree.
     * @throws IOException
     *             If there was a problem creating the new history file
     */
    public StateSystem(IStateHistoryBackend backend, boolean newFile)
            throws IOException {
        this.backend = backend;
        this.transState = new TransientState(backend);

        if (newFile) {
            attributeTree = new AttributeTree(this);
        } else {
            /* We're opening an existing file */
            this.attributeTree = new AttributeTree(this, backend.supplyAttributeTreeReader());
            transState.setInactive();
            finishedLatch.countDown(); /* The history is already built */
        }
    }

    @Override
    public boolean waitUntilBuilt() {
        try {
            finishedLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return !buildCancelled;
    }

    @Override
    public synchronized void dispose() {
        isDisposed = true;
        if (transState.isActive()) {
            transState.setInactive();
            buildCancelled = true;
        }
        backend.dispose();
    }

    //--------------------------------------------------------------------------
    //        General methods related to the attribute tree
    //--------------------------------------------------------------------------

    /**
     * Get the attribute tree associated with this state system. This should be
     * the only way of accessing it (and if subclasses want to point to a
     * different attribute tree than their own, they should only need to
     * override this).
     *
     * @return The attribute tree
     */
    public AttributeTree getAttributeTree() {
        return attributeTree;
    }

    /**
     * Method used by the attribute tree when creating new attributes, to keep
     * the attribute count in the transient state in sync.
     */
    protected void addEmptyAttribute() {
        transState.addEmptyEntry();
    }

    @Override
    public int getNbAttributes() {
        return getAttributeTree().getNbAttributes();
    }

    @Override
    public String getAttributeName(int attributeQuark) {
        return getAttributeTree().getAttributeName(attributeQuark);
    }

    @Override
    public String getFullAttributePath(int attributeQuark) {
        return getAttributeTree().getFullAttributeName(attributeQuark);
    }

    //--------------------------------------------------------------------------
    //        Methods related to the storage backend
    //--------------------------------------------------------------------------

    @Override
    public long getStartTime() {
        return backend.getStartTime();
    }

    @Override
    public long getCurrentEndTime() {
        return backend.getEndTime();
    }

    @Override
    public void closeHistory(long endTime) throws TimeRangeException {
        File attributeTreeFile;
        long attributeTreeFilePos;
        long realEndTime = endTime;

        if (realEndTime < backend.getEndTime()) {
            /*
             * This can happen (empty nodes pushing the border further, etc.)
             * but shouldn't be too big of a deal.
             */
            realEndTime = backend.getEndTime();
        }
        transState.closeTransientState(realEndTime);
        backend.finishedBuilding(realEndTime);

        attributeTreeFile = backend.supplyAttributeTreeWriterFile();
        attributeTreeFilePos = backend.supplyAttributeTreeWriterFilePosition();
        if (attributeTreeFile != null) {
            /*
             * If null was returned, we simply won't save the attribute tree,
             * too bad!
             */
            getAttributeTree().writeSelf(attributeTreeFile, attributeTreeFilePos);
        }
        finishedLatch.countDown(); /* Mark the history as finished building */
    }

    //--------------------------------------------------------------------------
    //        Quark-retrieving methods
    //--------------------------------------------------------------------------

    @Override
    public int getQuarkAbsolute(String... attribute)
            throws AttributeNotFoundException {
        return getAttributeTree().getQuarkDontAdd(-1, attribute);
    }

    @Override
    public int getQuarkAbsoluteAndAdd(String... attribute) {
        return getAttributeTree().getQuarkAndAdd(-1, attribute);
    }

    @Override
    public int getQuarkRelative(int startingNodeQuark, String... subPath)
            throws AttributeNotFoundException {
        return getAttributeTree().getQuarkDontAdd(startingNodeQuark, subPath);
    }

    @Override
    public int getQuarkRelativeAndAdd(int startingNodeQuark, String... subPath) {
        return getAttributeTree().getQuarkAndAdd(startingNodeQuark, subPath);
    }

    @Override
    public List<Integer> getSubAttributes(int quark, boolean recursive)
            throws AttributeNotFoundException {
        return getAttributeTree().getSubAttributes(quark, recursive);
    }

    @Override
    public List<Integer> getQuarks(String... pattern) {
        List<Integer> quarks = new LinkedList<Integer>();
        List<String> prefix = new LinkedList<String>();
        List<String> suffix = new LinkedList<String>();
        boolean split = false;
        String[] prefixStr;
        String[] suffixStr;
        List<Integer> directChildren;
        int startingAttribute;

        /* Fill the "prefix" and "suffix" parts of the pattern around the '*' */
        for (String entry : pattern) {
            if (entry.equals("*")) { //$NON-NLS-1$
                if (split) {
                    /*
                     * Split was already true? This means there was more than
                     * one wildcard. This is not supported, return an empty
                     * list.
                     */
                    return quarks;
                }
                split = true;
                continue;
            }

            if (split) {
                suffix.add(entry);
            } else {
                prefix.add(entry);
            }
        }
        prefixStr = prefix.toArray(new String[prefix.size()]);
        suffixStr = suffix.toArray(new String[suffix.size()]);

        /*
         * If there was no wildcard, we'll only return the one matching
         * attribute, if there is one.
         */
        if (!split) {
            int quark;
            try {
                quark = getQuarkAbsolute(prefixStr);
            } catch (AttributeNotFoundException e) {
                /* It's fine, we'll just return the empty List */
                return quarks;
            }
            quarks.add(quark);
            return quarks;
        }

        try {
            if (prefix.size() == 0) {
                /*
                 * If 'prefix' is empty, this means the wildcard was the first
                 * element. Look for the root node's sub-attributes.
                 */
                startingAttribute = -1;
            } else {
                startingAttribute = getQuarkAbsolute(prefixStr);
            }
            directChildren = getSubAttributes(startingAttribute, false);
        } catch (AttributeNotFoundException e) {
            /* That attribute path did not exist, return the empty array */
            return quarks;
        }

        /*
         * Iterate of all the sub-attributes, and only keep those who match the
         * 'suffix' part of the initial pattern.
         */
        for (int childQuark : directChildren) {
            int matchingQuark;
            try {
                matchingQuark = getQuarkRelative(childQuark, suffixStr);
            } catch (AttributeNotFoundException e) {
                continue;
            }
            quarks.add(matchingQuark);
        }

        return quarks;
    }

    //--------------------------------------------------------------------------
    //        Methods related to insertions in the history
    //--------------------------------------------------------------------------

    @Override
    public void modifyAttribute(long t, ITmfStateValue value, int attributeQuark)
            throws TimeRangeException, AttributeNotFoundException,
            StateValueTypeException {
        transState.processStateChange(t, value, attributeQuark);
    }

    @Override
    public void incrementAttribute(long t, int attributeQuark)
            throws StateValueTypeException, TimeRangeException,
            AttributeNotFoundException {
        int prevValue = queryOngoingState(attributeQuark).unboxInt();
        if (prevValue == -1) {
            /* if the attribute was previously null, start counting at 0 */
            prevValue = 0;
        }
        modifyAttribute(t, TmfStateValue.newValueInt(prevValue + 1),
                attributeQuark);
    }

    @Override
    public void pushAttribute(long t, ITmfStateValue value, int attributeQuark)
            throws TimeRangeException, AttributeNotFoundException,
            StateValueTypeException {
        Integer stackDepth;
        int subAttributeQuark;
        ITmfStateValue previousSV = transState.getOngoingStateValue(attributeQuark);

        if (previousSV.isNull()) {
            /*
             * If the StateValue was null, this means this is the first time we
             * use this attribute. Leave stackDepth at 0.
             */
            stackDepth = 0;
        } else if (previousSV.getType() == Type.INTEGER) {
            /* Previous value was an integer, all is good, use it */
            stackDepth = previousSV.unboxInt();
        } else {
            /* Previous state of this attribute was another type? Not good! */
            throw new StateValueTypeException();
        }

        if (stackDepth >= 100000) {
            /*
             * Limit stackDepth to 100000, to avoid having Attribute Trees grow out
             * of control due to buggy insertions
             */
            String message = "Stack limit reached, not pushing"; //$NON-NLS-1$
            throw new AttributeNotFoundException(message);
        }

        stackDepth++;
        subAttributeQuark = getQuarkRelativeAndAdd(attributeQuark, stackDepth.toString());

        modifyAttribute(t, TmfStateValue.newValueInt(stackDepth), attributeQuark);
        modifyAttribute(t, value, subAttributeQuark);
    }

    @Override
    public ITmfStateValue popAttribute(long t, int attributeQuark)
            throws AttributeNotFoundException, TimeRangeException,
            StateValueTypeException {
        /* These are the state values of the stack-attribute itself */
        ITmfStateValue previousSV = queryOngoingState(attributeQuark);

        if (previousSV.isNull()) {
            /*
             * Trying to pop an empty stack. This often happens at the start of
             * traces, for example when we see a syscall_exit, without having
             * the corresponding syscall_entry in the trace. Just ignore
             * silently.
             */
            return null;
        }
        if (previousSV.getType() != Type.INTEGER) {
            /*
             * The existing value was not an integer (which is expected for
             * stack tops), this doesn't look like a valid stack attribute.
             */
            throw new StateValueTypeException();
        }

        Integer stackDepth = previousSV.unboxInt();

        if (stackDepth <= 0) {
            /* This on the other hand should not happen... */
            /* the case where == -1 was handled previously by .isNull() */
            String message = "A top-level stack attribute cannot " + //$NON-NLS-1$
                    "have a value of 0 or less (except -1/null)."; //$NON-NLS-1$
            throw new StateValueTypeException(message);
        }

        /* The attribute should already exist at this point */
        int subAttributeQuark = getQuarkRelative(attributeQuark, stackDepth.toString());
        ITmfStateValue poppedValue = queryOngoingState(subAttributeQuark);

        /* Update the state value of the stack-attribute */
        ITmfStateValue nextSV;
        if (--stackDepth == 0 ) {
            /* Jump over "0" and store -1 (a null state value) */
            nextSV = TmfStateValue.nullValue();
        } else {
            nextSV = TmfStateValue.newValueInt(stackDepth);
        }
        modifyAttribute(t, nextSV, attributeQuark);

        /* Delete the sub-attribute that contained the user's state value */
        removeAttribute(t, subAttributeQuark);

        return poppedValue;
    }

    @Override
    public void removeAttribute(long t, int attributeQuark)
            throws TimeRangeException, AttributeNotFoundException {
        assert (attributeQuark >= 0);
        List<Integer> childAttributes;

        /*
         * "Nullify our children first, recursively. We pass 'false' because we
         * handle the recursion ourselves.
         */
        childAttributes = getSubAttributes(attributeQuark, false);
        for (Integer childNodeQuark : childAttributes) {
            assert (attributeQuark != childNodeQuark);
            removeAttribute(t, childNodeQuark);
        }
        /* Nullify ourselves */
        try {
            transState.processStateChange(t, TmfStateValue.nullValue(),
                    attributeQuark);
        } catch (StateValueTypeException e) {
            /*
             * Will not happen since we're inserting null values only, but poor
             * compiler has no way of knowing this...
             */
            throw new IllegalStateException(e);
        }
    }

    //--------------------------------------------------------------------------
    //        "Current" query/update methods
    //--------------------------------------------------------------------------

    @Override
    public ITmfStateValue queryOngoingState(int attributeQuark)
            throws AttributeNotFoundException {
        return transState.getOngoingStateValue(attributeQuark);
    }

    @Override
    public long getOngoingStartTime(int attribute)
            throws AttributeNotFoundException {
        return transState.getOngoingStartTime(attribute);
    }

    @Override
    public void updateOngoingState(ITmfStateValue newValue, int attributeQuark)
            throws AttributeNotFoundException {
        transState.changeOngoingStateValue(attributeQuark, newValue);
    }

    /**
     * Modify the whole "ongoing state" (state values + start times). This can
     * be used when "seeking" a state system to a different point in the trace
     * (and restoring the known stateInfo at this location). Use with care!
     *
     * @param newStateIntervals
     *            The new List of state values to use as ongoing state info
     */
    protected void replaceOngoingState(List<ITmfStateInterval> newStateIntervals) {
        transState.replaceOngoingState(newStateIntervals);
   }

    //--------------------------------------------------------------------------
    //        Regular query methods (sent to the back-end)
    //--------------------------------------------------------------------------

    @Override
    public synchronized List<ITmfStateInterval> queryFullState(long t)
            throws TimeRangeException, StateSystemDisposedException {
        if (isDisposed) {
            throw new StateSystemDisposedException();
        }

        List<ITmfStateInterval> stateInfo = new ArrayList<ITmfStateInterval>(getNbAttributes());

        /* Bring the size of the array to the current number of attributes */
        for (int i = 0; i < getNbAttributes(); i++) {
            stateInfo.add(null);
        }

        /* Query the storage backend */
        backend.doQuery(stateInfo, t);

        /*
         * If we are currently building the history, also query the "ongoing"
         * states for stuff that might not yet be written to the history.
         */
        if (transState.isActive()) {
            transState.doQuery(stateInfo, t);
        }

        /*
         * We should have previously inserted an interval for every attribute.
         * If we do happen do see a 'null' object here, just replace it with a a
         * dummy internal with a null value, to avoid NPE's further up.
         */
        for (int i = 0; i < stateInfo.size(); i++) {
            if (stateInfo.get(i) == null) {
                stateInfo.set(i, new TmfStateInterval(t, t, i, TmfStateValue.nullValue()));
            }
        }
        return stateInfo;
    }

    @Override
    public ITmfStateInterval querySingleState(long t, int attributeQuark)
            throws AttributeNotFoundException, TimeRangeException,
            StateSystemDisposedException {
        if (isDisposed) {
            throw new StateSystemDisposedException();
        }

        ITmfStateInterval ret;
        if (transState.hasInfoAboutStateOf(t, attributeQuark)) {
            ret = transState.getOngoingInterval(attributeQuark);
        } else {
            ret = backend.doSingularQuery(t, attributeQuark);
        }

        /*
         * Return a fake interval if we could not find anything in the history.
         * We do NOT want to return 'null' here.
         */
        if (ret == null) {
            return new TmfStateInterval(t, this.getCurrentEndTime(),
                    attributeQuark, TmfStateValue.nullValue());
        }
        return ret;
    }

    @Override
    public ITmfStateInterval querySingleStackTop(long t, int stackAttributeQuark)
            throws StateValueTypeException, AttributeNotFoundException,
            TimeRangeException, StateSystemDisposedException {
        Integer curStackDepth = querySingleState(t, stackAttributeQuark).getStateValue().unboxInt();

        if (curStackDepth == -1) {
            /* There is nothing stored in this stack at this moment */
            return null;
        } else if (curStackDepth < -1 || curStackDepth == 0) {
            /*
             * This attribute is an integer attribute, but it doesn't seem like
             * it's used as a stack-attribute...
             */
            throw new StateValueTypeException();
        }

        int subAttribQuark = getQuarkRelative(stackAttributeQuark, curStackDepth.toString());
        return querySingleState(t, subAttribQuark);
    }

    @Override
    public List<ITmfStateInterval> queryHistoryRange(int attributeQuark,
            long t1, long t2) throws TimeRangeException,
            AttributeNotFoundException, StateSystemDisposedException {
        if (isDisposed) {
            throw new StateSystemDisposedException();
        }

        List<ITmfStateInterval> intervals;
        ITmfStateInterval currentInterval;
        long ts, tEnd;

        /* Make sure the time range makes sense */
        if (t2 <= t1) {
            throw new TimeRangeException();
        }

        /* Set the actual, valid end time of the range query */
        if (t2 > this.getCurrentEndTime()) {
            tEnd = this.getCurrentEndTime();
        } else {
            tEnd = t2;
        }

        /* Get the initial state at time T1 */
        intervals = new ArrayList<ITmfStateInterval>();
        currentInterval = querySingleState(t1, attributeQuark);
        intervals.add(currentInterval);

        /* Get the following state changes */
        ts = currentInterval.getEndTime();
        while (ts != -1 && ts < tEnd) {
            ts++; /* To "jump over" to the next state in the history */
            currentInterval = querySingleState(ts, attributeQuark);
            intervals.add(currentInterval);
            ts = currentInterval.getEndTime();
        }
        return intervals;
    }

    @Override
    public List<ITmfStateInterval> queryHistoryRange(int attributeQuark,
            long t1, long t2, long resolution, IProgressMonitor monitor)
            throws TimeRangeException, AttributeNotFoundException,
            StateSystemDisposedException {
        if (isDisposed) {
            throw new StateSystemDisposedException();
        }

        List<ITmfStateInterval> intervals;
        ITmfStateInterval currentInterval;
        long ts, tEnd;

        IProgressMonitor mon = monitor;
        if (mon == null) {
            mon = new NullProgressMonitor();
        }

        /* Make sure the time range makes sense */
        if (t2 < t1 || resolution <= 0) {
            throw new TimeRangeException();
        }

        /* Set the actual, valid end time of the range query */
        if (t2 > this.getCurrentEndTime()) {
            tEnd = this.getCurrentEndTime();
        } else {
            tEnd = t2;
        }

        /* Get the initial state at time T1 */
        intervals = new ArrayList<ITmfStateInterval>();
        currentInterval = querySingleState(t1, attributeQuark);
        intervals.add(currentInterval);

        /*
         * Iterate over the "resolution points". We skip unneeded queries in the
         * case the current interval is longer than the resolution.
         */
        for (ts = t1; (currentInterval.getEndTime() != -1) && (ts < tEnd);
                ts += resolution) {
            if (mon.isCanceled()) {
                return intervals;
            }
            if (ts <= currentInterval.getEndTime()) {
                continue;
            }
            currentInterval = querySingleState(ts, attributeQuark);
            intervals.add(currentInterval);
        }

        /* Add the interval at t2, if it wasn't included already. */
        if (currentInterval.getEndTime() < tEnd) {
            currentInterval = querySingleState(tEnd, attributeQuark);
            intervals.add(currentInterval);
        }
        return intervals;
    }

    //--------------------------------------------------------------------------
    //        Debug methods
    //--------------------------------------------------------------------------

    static void logMissingInterval(int attribute, long timestamp) {
        Activator.logInfo("No data found in history for attribute " + //$NON-NLS-1$
                attribute + " at time " + timestamp + //$NON-NLS-1$
                ", returning dummy interval"); //$NON-NLS-1$
    }

    /**
     * Print out the contents of the inner structures.
     *
     * @param writer
     *            The PrintWriter in which to print the output
     */
    public void debugPrint(PrintWriter writer) {
        getAttributeTree().debugPrint(writer);
        transState.debugPrint(writer);
        backend.debugPrint(writer);
    }

}
