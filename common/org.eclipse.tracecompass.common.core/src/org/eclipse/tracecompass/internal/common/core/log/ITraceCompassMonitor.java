/*******************************************************************************
 * Copyright (c) 2022 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.common.core.log;

import javax.management.MXBean;

/**
 * Trace Compass Monitor MXBean interface. Needed to publish MXBeans.
 *
 * An MXBean is a managed java object that uses Java Management (JMX) to publish
 * information. MXBeans have a pre-defined datatype, and in this case will be
 * SimpleTypes. This allows the value to be easily plotted.
 *
 * @author Matthew Khouzam
 */
@MXBean
public interface ITraceCompassMonitor {

    /**
     * Get the observed element name
     *
     * @return the observed element names (in the order of being added)
     */
    String getObservedElementName();

    /**
     * Get the mean (average) time
     *
     * @return the average times (same order as the element names)
     */
    double getMeanTime();

    /**
     * Get the total (sum) time
     *
     * @return the sum times (same order as the element names)
     */
    long getTotalTime();

    /**
     * Get the number of times (count) the element is added
     *
     * @return the count of elements added (same order as the element names)
     */
    long getCount();

    /**
     * Get the minimum time
     *
     * @return the minimum times
     */
    long getMinTime();

    /**
     * Get the maximum time
     *
     * @return the maximum times
     */
    long getMaxTime();
}
