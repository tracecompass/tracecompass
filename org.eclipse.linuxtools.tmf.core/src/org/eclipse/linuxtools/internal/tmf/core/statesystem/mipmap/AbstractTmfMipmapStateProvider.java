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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue.Type;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * This is an abstract state provider that allows attributes to be mipmapped
 * for one or more of the supported mipmap features (min, max, average).
 *
 * Extend this class for a specific implementation
 */
public abstract class AbstractTmfMipmapStateProvider extends AbstractTmfStateProvider {

    /**
     * Feature bit for the maximum mipmap feature (value is 1&lt;&lt;1).
     */
    public static final int MAX = 1 << 1;

    /**
     * Feature bit for the minimum mipmap feature (value is 1&lt;&lt;2).
     */
    public static final int MIN = 1 << 2;

    /**
     * Feature bit for the average mipmap feature (value is 1&lt;&lt;3).
     */
    public static final int AVG = 1 << 3;

    /**
     * The string for maximum mipmap feature sub-attribute.
     * This attribute value is the mipmap number of levels.
     * It has sub-attributes for every level ("1", "2", etc.)
     */
    public static final String MAX_STRING = "max"; //$NON-NLS-1$

    /**
     * The string for minimum mipmap feature sub-attribute.
     * This attribute value is the mipmap number of levels.
     * It has sub-attributes for every level ("1", "2", etc.)
     */
    public static final String MIN_STRING = "min"; //$NON-NLS-1$

    /**
     * The string for average mipmap feature sub-attribute.
     * This attribute value is the mipmap number of levels.
     * It has sub-attributes for every level ("1", "2", etc.)
     */
    public static final String AVG_STRING = "avg"; //$NON-NLS-1$

    /**
     * Map of mipmap features per attribute. The map's key is the base attribute quark.
     */
    private Map<Integer, Set<ITmfMipmapFeature>> featureMap = new HashMap<>();

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param trace
     *            The trace directory
     * @param eventType
     *            The specific class for the event type
     * @param id
     *            The name given to this state change input. Only used
     *            internally.
     */
    public AbstractTmfMipmapStateProvider(ITmfTrace trace, Class<? extends ITmfEvent> eventType, String id) {
        super(trace, eventType, id);
    }

    // ------------------------------------------------------------------------
    // Public methods
    // ------------------------------------------------------------------------

    @Override
    public void dispose() {
        waitForEmptyQueue();
        for (Set<ITmfMipmapFeature> features : featureMap.values()) {
            for (ITmfMipmapFeature feature : features) {
                feature.updateAndCloseMipmap();
            }
        }
        super.dispose();
    }

    /**
     * Modify a mipmap attribute. The base attribute is modified and the mipmap
     * attributes for the feature(s) specified in the mipmap feature bitmap are
     * created and/or updated.<br>
     * Note: The mipmapFeatureBits and resolution are only used on the first
     * call of this method for a particular attribute, and the mipmap features
     * for this attribute are then activated until the end of the trace.<br>
     * Note: The base attribute should only be modified by calling this method.
     *
     * @param ts
     *            The timestamp of the event
     * @param value
     *            The value of the base attribute
     * @param baseQuark
     *            The quark of the base attribute
     * @param mipmapFeatureBits
     *            The mipmap feature bit(s)
     * @param resolution
     *            The mipmap resolution (must be greater than 1)
     * @throws TimeRangeException
     *             If the requested time is outside of the trace's range
     * @throws AttributeNotFoundException
     *             If the requested attribute quark is invalid
     * @throws StateValueTypeException
     *             If the inserted state value's type does not match what is
     *             already assigned to this attribute.
     * @see #MAX
     * @see #MIN
     * @see #AVG
     */
    public void modifyMipmapAttribute(long ts, ITmfStateValue value, int baseQuark, int mipmapFeatureBits, int resolution)
            throws TimeRangeException, AttributeNotFoundException, StateValueTypeException {
        ss.modifyAttribute(ts, value, baseQuark);
        if (value.getType() == Type.LONG || value.getType() == Type.INTEGER || value.getType() == Type.DOUBLE || value.isNull()) {
            Set<ITmfMipmapFeature> features = getFeatureSet(baseQuark, ts, value, mipmapFeatureBits, resolution);
            for (ITmfMipmapFeature mf : features) {
                mf.updateMipmap(value, ts);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Private methods
    // ------------------------------------------------------------------------

    private Set<ITmfMipmapFeature> getFeatureSet(int baseQuark, long ts, ITmfStateValue value, int mipmapFeatureBits, int resolution) {
        Set<ITmfMipmapFeature> features = featureMap.get(baseQuark);
        if (features != null) {
            return features;
        }
        features = new LinkedHashSet<>();
        if (value.isNull()) {
            return features;
        }
        featureMap.put(baseQuark, features);
        if (resolution > 1) {
            try {
                if ((mipmapFeatureBits & MAX) != 0) {
                    int featureQuark = ss.getQuarkRelativeAndAdd(baseQuark, MAX_STRING);
                    ss.modifyAttribute(ts, TmfStateValue.newValueInt(0), featureQuark);
                    MaxMipmapFeature mf = new MaxMipmapFeature(baseQuark, featureQuark, resolution, ss);
                    features.add(mf);
                }
                if ((mipmapFeatureBits & MIN) != 0) {
                    int featureQuark = ss.getQuarkRelativeAndAdd(baseQuark, MIN_STRING);
                    ss.modifyAttribute(ts, TmfStateValue.newValueInt(0), featureQuark);
                    MinMipmapFeature mf = new MinMipmapFeature(baseQuark, featureQuark, resolution, ss);
                    features.add(mf);
                }
                if ((mipmapFeatureBits & AVG) != 0) {
                    int featureQuark = ss.getQuarkRelativeAndAdd(baseQuark, AVG_STRING);
                    ss.modifyAttribute(ts, TmfStateValue.newValueInt(0), featureQuark);
                    AvgMipmapFeature mf = new AvgMipmapFeature(baseQuark, featureQuark, resolution, ss);
                    features.add(mf);
                }
            } catch (TimeRangeException e) {
                e.printStackTrace();
            } catch (AttributeNotFoundException e) {
                e.printStackTrace();
            } catch (StateValueTypeException e) {
                e.printStackTrace();
            }
        }
        return features;
    }
}
