/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event;

import java.util.HashMap;
import java.util.Map;

/**
 * Clock description used in CTF traces.
 *
 * From the TSDL perspective, they describe the clock topology of the system, as well as to detail
 * each clock parameter. In absence of clock description, it is assumed that all fields named
 * timestamp use the same clock source, which increments once per nanosecond.
 * <p>
 * Describing a clock and how it is used by streams is threefold:
 * <ol>
 * <li>the clock and clock topology should be described in a clock description block</li>
 * <li>a reference to this clock should be added within an integer type. (timestamp)</li>
 * <li>stream declarations can reference the clock they use as a timestamp source</li></ol>
 * In for trace compass's perspective, clock attributes are added when the trace is parsed. The ones
 * used at this moment are:
 * <ul><li>offsets</li><li>names</li><li>frequencies</li></ul>
 *
 * Most traces only have one clock source. As all events have timestamps offsetted by the same clock.
 * It is however possible especially with mixed traces (hardware and software) to have different
 * clock sources for a given event.
 * <p>
 * An individual event should only have one timestamp and therefore only one clock source though.
 */
public class CTFClock {

    private static final long ONE_BILLION_L = 1000000000L;
    private static final double ONE_BILLION_D = 1000000000.0;

    private static final String NAME = "name"; //$NON-NLS-1$
    private static final String FREQ = "freq"; //$NON-NLS-1$
    private static final String OFFSET = "offset"; //$NON-NLS-1$

    private long fClockOffset = 0;
    private double fClockScale = 1.0;
    private double fClockAntiScale = 1.0;

    /**
     * Field properties.
     */
    private final Map<String, Object> fProperties = new HashMap<>();
    /**
     * Field name.
     */
    private String fName;
    private boolean fIsScaled = false;

    /**
     * Default constructor
     */
    public CTFClock() {
        // The attributes are added later using addAttribute
    }

    /**
     * Method addAttribute.
     *
     * @param key
     *            String
     * @param value
     *            Object
     */
    public void addAttribute(String key, Object value) {
        fProperties.put(key, value);
        if (key.equals(NAME)) {
            fName = (String) value;
        }
        if (key.equals(FREQ)) {
            /*
             * Long is converted to a double. the double is then dividing
             * another double that double is saved. this is precise as long as
             * the long is under 53 bits long. this is ok as long as we don't
             * have a system with a frequency of > 1 600 000 000 GHz with
             * 200 ppm precision
             */
            fIsScaled = !((Long) getProperty(FREQ)).equals(ONE_BILLION_L);
            fClockScale = ONE_BILLION_D / ((Long) getProperty(FREQ)).doubleValue();
            fClockAntiScale = 1.0 / fClockScale;

        }
        if (key.equals(OFFSET)) {
            fClockOffset = (Long) getProperty(OFFSET);
        }
    }

    /**
     * Method getName.
     *
     * @return String
     */
    public String getName() {
        return fName;
    }

    /**
     * Method getProperty.
     *
     * @param key
     *            String
     * @return Object
     */
    public Object getProperty(String key) {
        return fProperties.get(key);
    }

    /**
     * @return the clockOffset
     */
    public long getClockOffset() {
        return fClockOffset;
    }

    /**
     * @return the clockScale
     */
    public double getClockScale() {
        return fClockScale;
    }

    /**
     * @return the clockAntiScale
     */
    public double getClockAntiScale() {
        return fClockAntiScale;
    }

    /**
     * @return is the clock in ns or cycles?
     */
    public boolean isClockScaled() {
        return fIsScaled;
    }

}
