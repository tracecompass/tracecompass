/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jean-Christian Kouamé - Initial API and implementation
 *     Patrick Tasse - Updates to mipmap feature
 *******************************************************************************/
package org.eclipse.linuxtools.internal.tmf.core.statesystem.mipmap;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.linuxtools.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.statesystem.core.interval.TmfStateInterval;
import org.eclipse.linuxtools.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.statesystem.core.statevalue.TmfStateValue;

/**
 * The mipmap feature base implementation.
 *
 * @author Jean-Christian Kouamé
 * @author Patrick Tasse
 *
 */
public abstract class TmfMipmapFeature implements ITmfMipmapFeature {

    /** The current state value */
    protected ITmfStateValue currentValue = TmfStateValue.nullValue();
    /** The current start time for the state value */
    protected long currentStartTime;
    /** The list of ongoing state intervals per mipmap level */
    protected List<List<ITmfStateInterval>> intervals = new ArrayList<>();
    /** The state system used to store the mipmap attributes */
    protected ITmfStateSystemBuilder ss;

    private int mipmapResolution;
    private int mipmapQuark;
    private List<Integer> levelQuarks = new ArrayList<>();

    /**
     * Constructor
     *
     * @param baseQuark
     *            The quark of the attribute we want to mipmap
     * @param mipmapQuark
     *            The quark of the mipmap feature attribute
     * @param mipmapResolution
     *            The resolution that will be used for the mipmap
     * @param ss
     *            The state system in which to insert the state changes
     */
    public TmfMipmapFeature(int baseQuark, int mipmapQuark, int mipmapResolution, ITmfStateSystemBuilder ss) {
        this.mipmapQuark = mipmapQuark;
        this.mipmapResolution = mipmapResolution;
        this.ss = ss;

        /* store the base attribute quark at level 0 */
        this.levelQuarks.add(baseQuark);

        /* create the level 0 list */
        intervals.add(new ArrayList<ITmfStateInterval>(mipmapResolution));
    }

    @Override
    public void updateMipmap(ITmfStateValue value, long ts) {
        /* if the value did not change, ignore it */
        if (currentValue.equals(value)) {
            return;
        }

        /* if the ongoing state value is not null, create and store a state interval */
        if (!currentValue.isNull()) {
            ITmfStateInterval interval = new TmfStateInterval(currentStartTime, ts, getLevelQuark(0), currentValue);
            intervals.get(0).add(interval);
        }

        /* if the new value is not null, update the mipmap levels that are full */
        if (!value.isNull()) {
            int level = 0;
            while (intervals.get(level).size() == getMipmapResolution()) {
                updateMipmapLevel(++level, ts);
            }
        }

        /* store the new value as the ongoing state value */
        currentValue = value;
        currentStartTime = ts;
    }

    @Override
    public void updateAndCloseMipmap() {
        if (!currentValue.isNull()) {
            ITmfStateInterval interval = new TmfStateInterval(currentStartTime, currentStartTime, getLevelQuark(0), currentValue);
            intervals.get(0).add(interval);
        }
        for (int level = 1; level <= getNbLevels(); level++) {
            updateMipmapLevel(level, currentStartTime);
        }
    }

    /**
     * Compute and update the mipmap level attribute from the lower-level
     * state interval list
     *
     * @param level
     *            The mipmap level to update
     * @param endTime
     *            The end timestamp to use for the mipmap interval
     */
    protected void updateMipmapLevel(int level, long endTime) {
        try {
            /* get the lower-level interval list */
            List<ITmfStateInterval> lowerIntervals = intervals.get(level - 1);
            if (lowerIntervals.size() == 0) {
                return;
            }

            /* get the start time from the first interval in the lower-level list */
            long startTime = lowerIntervals.get(0).getStartTime();

            /* compute the mipmap value */
            ITmfStateValue value = computeMipmapValue(lowerIntervals, startTime, endTime);

            /* clear the lower-level list */
            lowerIntervals.clear();

            /* get or create the current-level quark */
            int levelQuark = ss.getQuarkRelativeAndAdd(mipmapQuark, String.valueOf(level));
            if (!checkLevelExists(level)) {
                addLevelQuark(levelQuark);
                ss.updateOngoingState(TmfStateValue.newValueInt(level), mipmapQuark);
                intervals.add(new ArrayList<ITmfStateInterval>(getMipmapResolution()));
            }

            /* add new interval to current-level list */
            ITmfStateInterval interval = new TmfStateInterval(startTime, endTime, levelQuark, value);
            intervals.get(level).add(interval);

            /* update the current-level attribute */
            ss.modifyAttribute(startTime, value, levelQuark);
        } catch (StateValueTypeException e) {
            e.printStackTrace();
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (TimeRangeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compute the mipmap value from a list of lower-level state intervals
     *
     * @param lowerIntervals
     *            The list of lower-level state intervals
     * @param startTime
     *            The start time of the mipmap interval
     * @param endTime
     *            The end time of the mipmap interval
     * @return A state value to be stored in the mipmap level attribute
     */
    protected abstract ITmfStateValue computeMipmapValue(List<ITmfStateInterval> lowerIntervals, long startTime, long endTime);

    /**
     * Get the mipmap resolution
     *
     * @return The mipmap resolution for this feature
     */
    protected int getMipmapResolution() {
        return mipmapResolution;
    }

    /**
     * Get the mipmap feature quark. The state value
     * of this attribute is the mipmap number of levels.
     * This is the parent attribute of the mipmap level quarks.
     *
     * @return The attribute quark for this mipmap feature
     */
    protected int getMipmapQuark() {
        return mipmapQuark;
    }

    /**
     * Get the mipmap quark for the specified level.
     * For level 0 the base attribute quark is returned.
     *
     * @param level
     *         The mipmap level (0 for the base attribute)
     * @return The attribute quark for this mipmap level
     */
    protected int getLevelQuark(int level) {
        return levelQuarks.get(level);
    }

    /**
     * Add a new mipmap level quark.
     *
     * @param quark
     *         The attribute quark for the new mipmap level
     */
    protected void addLevelQuark(int quark) {
        levelQuarks.add(quark);
    }

    /**
     * Get the mipmap number of levels.
     *
     * @return The current number of mipmap levels for this feature
     *         (excluding the base attribute)
     */
    protected int getNbLevels() {
        return levelQuarks.size() - 1;
    }

    /**
     * Checks if a mipmap level exists.
     *
     * @param level
     *            The mipmap level to check
     * @return true if this level exists, false otherwise
     */
    protected boolean checkLevelExists(int level) {
        if (level >= levelQuarks.size() || level < 0) {
            return false;
        }
        return true;
    }

}
