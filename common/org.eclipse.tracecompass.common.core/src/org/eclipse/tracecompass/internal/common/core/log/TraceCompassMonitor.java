/*******************************************************************************
 * Copyright (c) 2022 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.common.core.log;

import java.lang.management.ManagementFactory;
import java.util.LongSummaryStatistics;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;

/**
 * Trace compass monitor internal bean.
 *
 * A bean is a java standard object to publish information.
 *
 * Used to publish performance metrics and KPIs, can be seen with tools such as
 * visualvm and jconsole.
 *
 * This class is internal, it should not be extended or made into API.
 *
 * @author Matthew Khouzam
 */
@NonNullByDefault
public final class TraceCompassMonitor extends NotificationBroadcasterSupport implements ITraceCompassMonitor {

    private final LongSummaryStatistics fStats = new LongSummaryStatistics();
    private final String fLabel;

    /**
     * Constructor
     *
     * @param label
     *            the name of the bean, colons (':') will be replaced with
     *            hyphens ('-')
     */
    public TraceCompassMonitor(String label) {
        fLabel = label;
        /**
         * Override potentially finer logging for these, as this breaks the
         * resulting JSON trace file. This happens upon @{link
         * ManagementFactory} use below. Use the default @{link Level.FINE},
         * which doesn't output any such breaking strings. Finer logging for
         * this package isn't necessary anyway here.
         */
        Logger.getLogger("javax.management").setLevel(Level.FINE); //$NON-NLS-1$
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        String beanName = "org.eclipse.tracecompass.common.core.log:type=TraceCompassMonitoring,name=" + label.replace(':', '-'); //$NON-NLS-1$
        try {
            ObjectName name = new ObjectName(beanName);
            mbs.registerMBean(this, name);
        } catch (JMException e) {
            TraceCompassLog.getLogger(getClass()).log(Level.WARNING, "Cannot create bean", e); //$NON-NLS-1$
        }
    }

    @Override
    public @NonNull String getObservedElementName() {
        return fLabel;
    }

    @Override
    public double getMeanTime() {
        return fStats.getAverage();
    }

    @Override
    public long getMinTime() {
        return fStats.getMin();
    }

    @Override
    public long getMaxTime() {
        return fStats.getMax();
    }

    @Override
    public long getTotalTime() {
        return fStats.getSum();
    }

    @Override
    public long getCount() {
        return fStats.getCount();
    }

    /**
     * Accept a long to aggregate
     *
     * @param value
     *            the value to aggregate
     */
    public void accept(long value) {
        fStats.accept(value);
    }
}
