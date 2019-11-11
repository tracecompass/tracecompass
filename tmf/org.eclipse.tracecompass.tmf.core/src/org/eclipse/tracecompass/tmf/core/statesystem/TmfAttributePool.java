/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.statesystem;

import java.util.ArrayDeque;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;

/**
 * This class allows to recycle state system attributes. Instead of creating a
 * lot of short-lived attributes, it is sometimes useful to re-use an attribute
 * (and its whole sub-tree) that was previously used and is no longer required.
 * This class keeps a list of children attributes of a base quark and grows that
 * list as needed.
 *
 * @author Geneviève Bastien
 * @since 2.0
 */
public class TmfAttributePool {

    private final ITmfStateSystemBuilder fSs;
    private final Integer fBaseQuark;
    private final Queue<@Nullable Integer> fAvailableQuarks;
    private final Set<Integer> fQuarksInUse = new TreeSet<>();
    private int fCount = 0;

    /**
     * The type of queue
     */
    public enum QueueType {
        /**
         * First In First Out, available attributes are stored and returned in
         * the order in which they are recycled
         */
        FIFO,
        /**
         * Available attributes are returned by their number, so attributes with
         * lower numbers will be used more often
         */
        PRIORITY
    }

    /**
     * Constructor
     *
     * @param ss
     *            The state system
     * @param baseQuark
     *            The base quark under which to add the recyclable attributes
     */
    public TmfAttributePool(ITmfStateSystemBuilder ss, Integer baseQuark) {
        this(ss, baseQuark, QueueType.FIFO);
    }

    /**
     * Constructor
     *
     * @param ss
     *            The state system
     * @param baseQuark
     *            The base quark under which to add the recyclable attributes
     * @param type
     *            The type of queue to use for the attribute pool
     */
    public TmfAttributePool(ITmfStateSystemBuilder ss, Integer baseQuark, QueueType type) {
        fSs = ss;
        try {
            /* Make sure the base quark is in range */
            ss.getParentAttributeQuark(baseQuark);
            fBaseQuark = baseQuark;
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("The quark used as base for the attribute pool does not exist"); //$NON-NLS-1$
        }
        switch (type) {
        case FIFO:
            fAvailableQuarks = new ArrayDeque<>();
            break;
        case PRIORITY:
            fAvailableQuarks = new PriorityQueue<>();
            break;
        default:
            throw new IllegalArgumentException("Wrong queue type"); //$NON-NLS-1$
        }
    }

    /**
     * Get an available attribute quark. If there is one available, it will be
     * reused, otherwise a new quark will be created under the base quark. The
     * name of the attributes is a sequential integer. So the first quark to be
     * added will be named '0', the next one '1', etc.
     *
     * @return An available quark
     */
    public synchronized int getAvailable() {
        Integer quark = fAvailableQuarks.poll();
        if (quark == null) {
            quark = fSs.getQuarkRelativeAndAdd(fBaseQuark, String.valueOf(fCount));
            fCount++;
        }
        fQuarksInUse.add(quark);
        return quark;
    }

    /**
     * Recycle a quark so that it can be reused by calling the
     * {@link #getAvailable()} method. The quark has to have been obtained from
     * a previous call to {@link #getAvailable()}. It will set the quark's value
     * in the state system to a null value.
     *
     * It is assumed that it will be reused in the same context each time, so
     * all children are kept and set to null in this method. The quarks are
     * still available for the caller, nothing prevents from re-using them
     * without referring to this class. That means if any attribute's value need
     * to be non-null after recycling the quark, the caller can do it after
     * calling this method.
     *
     * @param quark
     *            The quark to recycle.
     * @param ts
     *            The timestamp at which to close this attribute.
     */
    public synchronized void recycle(int quark, long ts) {
        if (!fQuarksInUse.remove(quark)) {
            throw new IllegalArgumentException("Quark " + quark + " is not in use at time " + ts); //$NON-NLS-1$ //$NON-NLS-2$
        }
        fSs.removeAttribute(ts, quark);
        fAvailableQuarks.add(quark);
    }

}
