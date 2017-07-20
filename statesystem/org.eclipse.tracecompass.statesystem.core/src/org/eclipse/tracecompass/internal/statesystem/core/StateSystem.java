/*******************************************************************************
 * Copyright (c) 2012, 2017 Ericsson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Add message to exceptions
 *******************************************************************************/

package org.eclipse.tracecompass.internal.statesystem.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.ScopeLog;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.IntegerRangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue.Type;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

import com.google.common.collect.ImmutableCollection.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

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
 * @author Alexandre Montplaisir
 *
 */
public class StateSystem implements ITmfStateSystemBuilder {

    private static final int MAX_STACK_DEPTH = 100000;
    private static final String PARENT = ".."; //$NON-NLS-1$
    private static final String WILDCARD = "*"; //$NON-NLS-1$

    private static final @NonNull Logger LOGGER = TraceCompassLog.getLogger(StateSystem.class);

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
    public StateSystem(@NonNull IStateHistoryBackend backend) {
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
    public StateSystem(@NonNull IStateHistoryBackend backend, boolean newFile)
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
    public String getSSID() {
        return backend.getSSID();
    }

    @Override
    public boolean isCancelled() {
        return buildCancelled;
    }

    @Override
    public void waitUntilBuilt() {
        try {
            finishedLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean waitUntilBuilt(long timeout) {
        boolean ret = false;
        try {
            ret = finishedLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ret;
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

    // --------------------------------------------------------------------------
    // General methods related to the attribute tree
    // --------------------------------------------------------------------------

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
    public void addEmptyAttribute() {
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

    @Override
    public String[] getFullAttributePathArray(int attributeQuark) {
        return getAttributeTree().getFullAttributePathArray(attributeQuark);
    }

    // --------------------------------------------------------------------------
    // Methods related to the storage backend
    // --------------------------------------------------------------------------

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

    // --------------------------------------------------------------------------
    // Quark-retrieving methods
    // --------------------------------------------------------------------------

    @Override
    public int getQuarkAbsolute(String... attribute)
            throws AttributeNotFoundException {
        int quark = getAttributeTree().getQuarkDontAdd(ROOT_ATTRIBUTE, attribute);
        if (quark == INVALID_ATTRIBUTE) {
            throw new AttributeNotFoundException(getSSID() + " Path:" + Arrays.toString(attribute)); //$NON-NLS-1$
        }
        return quark;
    }

    @Override
    public int optQuarkAbsolute(String... attribute) {
        return getAttributeTree().getQuarkDontAdd(ROOT_ATTRIBUTE, attribute);
    }

    @Override
    public int getQuarkAbsoluteAndAdd(String... attribute) {
        return getAttributeTree().getQuarkAndAdd(ROOT_ATTRIBUTE, attribute);
    }

    @Override
    public int getQuarkRelative(int startingNodeQuark, String... subPath)
            throws AttributeNotFoundException {
        int quark = getAttributeTree().getQuarkDontAdd(startingNodeQuark, subPath);
        if (quark == INVALID_ATTRIBUTE) {
            throw new AttributeNotFoundException(getSSID() + " Quark:" + startingNodeQuark + ", SubPath:" + Arrays.toString(subPath)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return quark;
    }

    @Override
    public int optQuarkRelative(int startingNodeQuark, String... subPath) {
        return getAttributeTree().getQuarkDontAdd(startingNodeQuark, subPath);
    }

    @Override
    public int getQuarkRelativeAndAdd(int startingNodeQuark, String... subPath) {
        return getAttributeTree().getQuarkAndAdd(startingNodeQuark, subPath);
    }

    @Override
    public List<@NonNull Integer> getSubAttributes(int quark, boolean recursive) {
        return getAttributeTree().getSubAttributes(quark, recursive);
    }

    @Override
    public List<@NonNull Integer> getSubAttributes(int quark, boolean recursive, String pattern) {
        List<Integer> all = getSubAttributes(quark, recursive);
        List<@NonNull Integer> ret = new LinkedList<>();
        for (Integer attQuark : all) {
            String name = getAttributeName(attQuark.intValue());
            if (name.matches(pattern)) {
                ret.add(attQuark);
            }
        }
        return ret;
    }

    @Override
    public int getParentAttributeQuark(int quark) {
        return getAttributeTree().getParentAttributeQuark(quark);
    }

    @Override
    public List<@NonNull Integer> getQuarks(String... pattern) {
        return getQuarks(ROOT_ATTRIBUTE, pattern);
    }

    @Override
    public List<@NonNull Integer> getQuarks(int startingNodeQuark, String... pattern) {
        Builder<@NonNull Integer> builder = ImmutableSet.builder();
        if (pattern.length > 0) {
            getQuarks(builder, startingNodeQuark, Arrays.asList(pattern));
        } else {
            builder.add(startingNodeQuark);
        }
        return builder.build().asList();
    }

    private void getQuarks(Builder<@NonNull Integer> builder, int quark, List<String> pattern) {
        String element = pattern.get(0);
        if (element == null) {
            return;
        }
        List<String> remainder = pattern.subList(1, pattern.size());
        if (remainder.isEmpty()) {
            if (element.equals(WILDCARD)) {
                builder.addAll(getSubAttributes(quark, false));
            } else if (element.equals(PARENT)) {
                builder.add(getParentAttributeQuark(quark));
            } else {
                int subQuark = optQuarkRelative(quark, element);
                if (subQuark != INVALID_ATTRIBUTE) {
                    builder.add(subQuark);
                }
            }
        } else {
            if (element.equals(WILDCARD)) {
                getSubAttributes(quark, false).forEach(subquark -> getQuarks(builder, subquark, remainder));
            } else if (element.equals(PARENT)) {
                getQuarks(builder, getParentAttributeQuark(quark), remainder);
            } else {
                int subQuark = optQuarkRelative(quark, element);
                if (subQuark != INVALID_ATTRIBUTE) {
                    getQuarks(builder, subQuark, remainder);
                }
            }
        }
    }

    // --------------------------------------------------------------------------
    // Methods related to insertions in the history
    // --------------------------------------------------------------------------

    @Deprecated
    @Override
    public void modifyAttribute(long t, @NonNull ITmfStateValue value, int attributeQuark)
            throws TimeRangeException, StateValueTypeException {
        transState.processStateChange(t, value.unboxValue(), attributeQuark);
    }

    @Override
    public void modifyAttribute(long t, Object value, int attributeQuark)
            throws TimeRangeException, StateValueTypeException {
        transState.processStateChange(t, value, attributeQuark);
    }

    @Deprecated
    @Override
    public void pushAttribute(long t, @NonNull ITmfStateValue value, int attributeQuark)
            throws TimeRangeException, StateValueTypeException {
        pushAttribute(t, value.unboxValue(), attributeQuark);
    }

    @Override
    public void pushAttribute(long t, Object value, int attributeQuark)
            throws TimeRangeException, StateValueTypeException {
        int stackDepth;
        int subAttributeQuark;
        Object previousSV = transState.getOngoingStateValue(attributeQuark);

        if (previousSV == null) {
            /*
             * If the StateValue was null, this means this is the first time we
             * use this attribute. Leave stackDepth at 0.
             */
            stackDepth = 0;
        } else if (previousSV instanceof Integer) {
            /* Previous value was an integer, all is good, use it */
            stackDepth = (int) previousSV;
        } else {
            /* Previous state of this attribute was another type? Not good! */
            throw new StateValueTypeException(getSSID() + " Quark:" + attributeQuark + ", Type:" + previousSV.getClass() + ", Expected:" + Type.INTEGER); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        if (stackDepth >= MAX_STACK_DEPTH) {
            /*
             * Limit stackDepth to 100000, to avoid having Attribute Trees grow
             * out of control due to buggy insertions
             */
            String message = " Stack limit reached, not pushing"; //$NON-NLS-1$
            throw new IllegalStateException(getSSID() + " Quark:" + attributeQuark + message); //$NON-NLS-1$
        }

        stackDepth++;
        subAttributeQuark = getQuarkRelativeAndAdd(attributeQuark, String.valueOf(stackDepth));

        modifyAttribute(t, stackDepth, attributeQuark);
        modifyAttribute(t, value, subAttributeQuark);
    }

    @Override
    public ITmfStateValue popAttribute(long t, int attributeQuark)
            throws TimeRangeException, StateValueTypeException {
        Object pop = popAttributeObject(t, attributeQuark);
        return pop != null ? TmfStateValue.newValue(pop) : null;
    }

    @Override
    public Object popAttributeObject(long t, int attributeQuark)
            throws TimeRangeException, StateValueTypeException {
        /* These are the state values of the stack-attribute itself */
        @Nullable Object previousSV = transState.getOngoingStateValue(attributeQuark);

        if (previousSV == null) {
            /*
             * Trying to pop an empty stack. This often happens at the start of
             * traces, for example when we see a syscall_exit, without having
             * the corresponding syscall_entry in the trace. Just ignore
             * silently.
             */
            return null;
        }


        int stackDepth = 0;

        if (previousSV instanceof Integer) {
            stackDepth = (int) previousSV;
        } else {
            /* This on the other hand should not happen... */
            throw new StateValueTypeException(getSSID() + " Quark:" + attributeQuark + ", Stack depth:" + stackDepth); //$NON-NLS-1$//$NON-NLS-2$
        }

        /* The attribute should already exist at this point */
        int subAttributeQuark;
        try {
            subAttributeQuark = getQuarkRelative(attributeQuark, String.valueOf(stackDepth));
        } catch (AttributeNotFoundException e) {
            String message = " Stack attribute missing sub-attribute for depth:" + stackDepth; //$NON-NLS-1$
            throw new IllegalStateException(getSSID() + " Quark:" + attributeQuark + message); //$NON-NLS-1$
        }
        Object poppedValue = queryOngoing(subAttributeQuark);

        /* Update the state value of the stack-attribute */
        Integer nextSV;
        if (--stackDepth == 0) {
            /* Store a null state value */
            nextSV = null;
        } else {
            nextSV = stackDepth;
        }
        modifyAttribute(t, nextSV, attributeQuark);

        /* Delete the sub-attribute that contained the user's state value */
        removeAttribute(t, subAttributeQuark);

        return poppedValue;
    }

    @Override
    public void removeAttribute(long t, int attributeQuark)
            throws TimeRangeException {
        /*
         * Nullify our children first, recursively. We pass 'false' because we
         * handle the recursion ourselves.
         */
        List<Integer> childAttributes = getSubAttributes(attributeQuark, false);
        for (int childNodeQuark : childAttributes) {
            if (attributeQuark == childNodeQuark) {
                /* Something went very wrong when building out attribute tree */
                throw new IllegalStateException();
            }
            removeAttribute(t, childNodeQuark);
        }
        /* Nullify ourselves */
        try {
            transState.processStateChange(t, null, attributeQuark);
        } catch (StateValueTypeException e) {
            /*
             * Will not happen since we're inserting null values only, but poor
             * compiler has no way of knowing this...
             */
            throw new IllegalStateException(e);
        }
    }

    // --------------------------------------------------------------------------
    // "Current" query/update methods
    // --------------------------------------------------------------------------

    @Override
    public ITmfStateValue queryOngoingState(int attributeQuark) {
        return TmfStateValue.newValue(queryOngoing(attributeQuark));
    }

    @Override
    public Object queryOngoing(int attributeQuark) {
        return transState.getOngoingStateValue(attributeQuark);
    }

    @Override
    public long getOngoingStartTime(int attribute) {
        return transState.getOngoingStartTime(attribute);
    }

    @Override
    public void updateOngoingState(ITmfStateValue newValue, int attributeQuark) {
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
    protected void replaceOngoingState(@NonNull List<@NonNull ITmfStateInterval> newStateIntervals) {
        transState.replaceOngoingState(newStateIntervals);
    }

    // --------------------------------------------------------------------------
    // Regular query methods (sent to the back-end)
    // --------------------------------------------------------------------------

    @Override
    public List<ITmfStateInterval> queryFullState(long t)
            throws TimeRangeException, StateSystemDisposedException {
        if (isDisposed) {
            throw new StateSystemDisposedException();
        }

        try (ScopeLog log = new ScopeLog(LOGGER, Level.FINER, "StateSystem:FullQuery", //$NON-NLS-1$
                "ssid", getSSID(), "ts", t);) { //$NON-NLS-1$ //$NON-NLS-2$

            final int nbAttr = getNbAttributes();
            List<@Nullable ITmfStateInterval> stateInfo = new ArrayList<>(nbAttr);

            /*
             * Bring the size of the array to the current number of attributes
             */
            for (int i = 0; i < nbAttr; i++) {
                stateInfo.add(null);
            }

            /*
             * If we are currently building the history, also query the
             * "ongoing" states for stuff that might not yet be written to the
             * history.
             */
            if (transState.isActive()) {
                transState.doQuery(stateInfo, t);
            }

            /* Query the storage backend */
            backend.doQuery(stateInfo, t);

            /*
             * We should have previously inserted an interval for every
             * attribute.
             */
            for (ITmfStateInterval interval : stateInfo) {
                if (interval == null) {
                    throw new IllegalStateException("Incoherent interval storage"); //$NON-NLS-1$
                }
            }
            return stateInfo;
        }
    }

    @Override
    public ITmfStateInterval querySingleState(long t, int attributeQuark)
            throws TimeRangeException, StateSystemDisposedException {
        if (isDisposed) {
            throw new StateSystemDisposedException();
        }
        try (TraceCompassLogUtils.ScopeLog log = new TraceCompassLogUtils.ScopeLog(LOGGER, Level.FINER, "StateSystem:SingleQuery", //$NON-NLS-1$
                "ssid", this.getSSID(), //$NON-NLS-1$
                "ts", t, //$NON-NLS-1$
                "attribute", attributeQuark)) { //$NON-NLS-1$
            ITmfStateInterval ret = transState.getIntervalAt(t, attributeQuark);
            if (ret == null) {
                /*
                 * The transient state did not have the information, let's look
                 * into the backend next.
                 */
                ret = backend.doSingularQuery(t, attributeQuark);
            }

            if (ret == null) {
                /*
                 * If we did our job correctly, there should be intervals for
                 * every possible attribute, over all the valid time range.
                 */
                throw new IllegalStateException("Incoherent interval storage"); //$NON-NLS-1$
            }
            return ret;
        }
    }

    @Override
    public Iterable<@NonNull ITmfStateInterval> query2D(Collection<@NonNull Integer> quarks, Collection<@NonNull Long> times)
            throws StateSystemDisposedException, TimeRangeException, IndexOutOfBoundsException {
        if (isDisposed) {
            throw new StateSystemDisposedException();
        }
        if (times.isEmpty()) {
            return Collections.emptyList();
        }

        TimeRangeCondition timeCondition = TimeRangeCondition.forDiscreteRange(times);
        return query2D(quarks, timeCondition);
    }

    @Override
    public Iterable<@NonNull ITmfStateInterval> query2D(Collection<@NonNull Integer> quarks, long start, long end)
            throws StateSystemDisposedException, TimeRangeException, IndexOutOfBoundsException {
        if (isDisposed) {
            throw new StateSystemDisposedException();
        }

        TimeRangeCondition timeCondition = TimeRangeCondition.forContinuousRange(start, end);
        return query2D(quarks, timeCondition);
    }

    private Iterable<@NonNull ITmfStateInterval> query2D(@NonNull Collection<@NonNull Integer> quarks, TimeRangeCondition timeCondition)
            throws TimeRangeException, IndexOutOfBoundsException {
        if (timeCondition.min() < getStartTime()) {
            throw new TimeRangeException();
        }

        if (quarks.isEmpty()) {
            return Collections.emptyList();
        }

        IntegerRangeCondition quarkCondition = IntegerRangeCondition.forDiscreteRange(quarks);
        if (quarkCondition.min() < 0 || quarkCondition.max() >= getNbAttributes()) {
            throw new IndexOutOfBoundsException();
        }

        Iterable<@NonNull ITmfStateInterval> transStateIterable = transState.query2D(quarks, timeCondition);
        Iterable<@NonNull ITmfStateInterval> backendIterable = backend.query2D(quarkCondition, timeCondition);

        return Iterables.concat(transStateIterable, backendIterable);
    }

    @Override
    public void removeFiles() {
        backend.removeFiles();
    }

    // --------------------------------------------------------------------------
    // Debug methods
    // --------------------------------------------------------------------------

    static void logMissingInterval(int attribute, long timestamp) {
        Activator.getDefault().logInfo("No data found in history for attribute " + //$NON-NLS-1$
                attribute + " at time " + timestamp + //$NON-NLS-1$
                ", returning dummy interval"); //$NON-NLS-1$
    }
}
