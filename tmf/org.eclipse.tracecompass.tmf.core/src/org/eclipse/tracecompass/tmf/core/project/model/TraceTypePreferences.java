/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.tmf.core.project.model;

import java.util.List;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.osgi.service.prefs.BackingStoreException;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * Singleton class to access the trace type preferences of Trace Compass. The
 * trace type preferences is a blacklist preference of disabled trace types.
 *
 * @author Jean-Christian Kouame
 * @since 3.0
 */
public final class TraceTypePreferences {

    private static final String SEPARATOR = "::"; //$NON-NLS-1$
    /**
     * The key for trace type preferences
     */
    public static final String TRACE_TYPE_PREFERENCE_KEY = "org.eclipse.tracecompass.tmf.core.prefs.traceType"; //$NON-NLS-1$

    private static final String INITIAL_TIME_RANGE_PREFERENCE_KEY = "org.eclipse.tracecompass.tmf.core.prefs.traceType.initial.range"; //$NON-NLS-1$

    /**
     * Initialize the trace type preferences
     */
    public static void init() {
        IEclipsePreferences configurationPreferences = getEclipsePreference();
        try {
            configurationPreferences.put(TRACE_TYPE_PREFERENCE_KEY, configurationPreferences.get(TRACE_TYPE_PREFERENCE_KEY, getDefaultValue()));
            configurationPreferences.flush();
        } catch (BackingStoreException e) {
            Activator.logError("Failed to initialize trace type preferences", e); //$NON-NLS-1$
        }

    }

    private static String getDefaultValue() {
        return Joiner.on(SEPARATOR).join(new @NonNull String[] {});
    }

    /**
     * Update the trace type preference value. The value stored is the list of
     * disabled trace types.
     *
     * @param disabled
     *            The list of disabled trace type ids in the preference page
     */
    public static void setPreferenceValue(List<@NonNull String> disabled) {
        try {
            IEclipsePreferences configurationPreferences = getEclipsePreference();

            TmfTraceType.getTraceTypeHelpers().forEach(helper -> {
                if (!helper.isExperimentType()) {
                    helper.setEnabled(!disabled.contains(helper.getTraceTypeId()));
                }
            });

            String value = Joiner.on(SEPARATOR).join(disabled);

            configurationPreferences.put(TraceTypePreferences.TRACE_TYPE_PREFERENCE_KEY, value);
            configurationPreferences.flush();
        } catch (BackingStoreException e) {
            Activator.logError("Failed to set the trace type preferences", e); //$NON-NLS-1$
        }
    }

    private static IEclipsePreferences getEclipsePreference() {
        IEclipsePreferences configurationPreferences = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        return configurationPreferences;
    }

    /**
     * Get the Initial time range
     *
     * @param traceType
     *            the trace type
     * @param defaultValue
     *            the default value
     * @return the time range in ns
     * @since 4.2
     */
    public static long getInitialTimeRange(String traceType, long defaultValue) {
        IEclipsePreferences configurationPreferences = getEclipsePreference();
        return configurationPreferences.getLong(getTracePreferenceKey(traceType), defaultValue);
    }

    /**
     * Set the Initial time range
     *
     * @param traceType
     *            the trace type
     * @param value
     *            the time range in ns
     * @since 4.2
     */
    public static void setInitialTimeRange(String traceType, long value) {
        IEclipsePreferences configurationPreferences = getEclipsePreference();
        configurationPreferences.putLong(getTracePreferenceKey(traceType), value);
    }

    /**
     * Reset to the default value the Initial time range
     *
     * @param traceType
     *            the trace type
     * @since 4.2
     */
    public static void resetInitialTimeRange(String traceType) {
        IEclipsePreferences configurationPreferences = getEclipsePreference();
        configurationPreferences.remove(getTracePreferenceKey(traceType));
    }

    private static String getTracePreferenceKey(String traceType) {
        return TraceTypePreferences.INITIAL_TIME_RANGE_PREFERENCE_KEY + '.' + traceType;
    }

    /**
     * Get the preference value for the trace types
     *
     * @return The list of disabled trace types
     */
    public static List<String> getPreferenceValue() {
        IEclipsePreferences configurationPreferences = getEclipsePreference();
        String joined = configurationPreferences.get(TraceTypePreferences.TRACE_TYPE_PREFERENCE_KEY, getDefaultValue());
        Iterable<String> disabled = Splitter.on(SEPARATOR).split(joined);
        return Lists.newArrayList(disabled);
    }
}
