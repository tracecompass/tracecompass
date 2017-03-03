/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiEmptyAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTableEntryAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiTimeRange;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DataChartNumericalDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DataChartStringDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DataChartTimestampDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.AbstractDoubleResolver;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.AbstractLongResolver;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.IStringResolver;
import org.eclipse.tracecompass.tmf.chart.core.model.IDataChartProvider;

import com.google.common.collect.ImmutableList;

/**
 * Class holding the results contained in one table outputted by a LAMI
 * analysis.
 *
 * @author Alexandre Montplaisir
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class LamiResultTable implements IDataChartProvider<LamiTableEntry> {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final LamiTimeRange fTimeRange;
    private final LamiTableClass fTableClass;
    private final List<LamiTableEntry> fEntries;
    private final List<IDataChartDescriptor<LamiTableEntry, ?>> fDescriptors;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Construct a new table from its components.
     *
     * @param timeRange
     *            The time range represented by this table. Some analyses
     *            separate tables by time range "buckets".
     * @param tableClass
     *            The class of this table
     * @param entries
     *            The list of entries, or rows, contained in this table
     */
    public LamiResultTable(LamiTimeRange timeRange, LamiTableClass tableClass,
            Iterable<LamiTableEntry> entries) {
        fTimeRange = timeRange;
        fTableClass = tableClass;
        fEntries = ImmutableList.copyOf(entries);
        fDescriptors = new ArrayList<>();

        for (LamiTableEntryAspect aspect : getTableClass().getAspects()) {
            if (aspect instanceof LamiEmptyAspect) {
                continue;
            }

            /**
             * Differenciation of lami aspect types for use by the custom charts
             * is made by continuity or timestamp. There is no special treatment
             * for time durations, or at least there was none in the original
             * lami charts.
             */
            if (aspect.isContinuous()) {
                if (aspect.isTimeStamp()) {
                    /* Create descriptors for timestamps */
                    fDescriptors.add(new DataChartTimestampDescriptor<>(aspect.getName(), new LamiDataLongResolver(aspect)));
                } else if (aspect.isTimeDuration()) {
                    /*
                     * Create descriptors for numbers with units. We do not use
                     * duration descriptor since in the case of lami charts, we
                     * want the durations and numerical data to be compatible
                     */
                    fDescriptors.add(new DataChartNumericalDescriptor<>(aspect.getName(), new LamiDataDoubleResolver(aspect), aspect.getUnits()));
                } else {
                    /* Create descriptors for general numbers */
                    fDescriptors.add(new DataChartNumericalDescriptor<>(aspect.getName(), new LamiDataDoubleResolver(aspect)));
                }
            } else {
                /* Create descriptors for general strings */
                fDescriptors.add(new DataChartStringDescriptor<>(aspect.getName(), new LamiDataStringResolver(aspect)));
            }
        }
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public String getName() {
        return getTableClass().getTableTitle();
    }

    @Override
    public Stream<@NonNull LamiTableEntry> getSource() {
        return checkNotNull(fEntries.stream());
    }

    @Override
    public List<IDataChartDescriptor<LamiTableEntry, ?>> getDataDescriptors() {
        return ImmutableList.copyOf(fDescriptors);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Get the time range of this table.
     *
     * @return The time range
     */
    public LamiTimeRange getTimeRange() {
        return fTimeRange;
    }

    /**
     * Get the class of this table.
     *
     * @return The table class
     */
    public LamiTableClass getTableClass() {
        return fTableClass;
    }

    /**
     * Get the list of entries contained in this table.
     *
     * @return The table entries
     */
    public List<LamiTableEntry> getEntries() {
        return fEntries;
    }

    // ------------------------------------------------------------------------
    // Data resolvers
    // ------------------------------------------------------------------------

    private final class LamiDataLongResolver extends AbstractLongResolver<LamiTableEntry> {
        LamiTableEntryAspect fAspect;

        public LamiDataLongResolver(LamiTableEntryAspect aspect) {
            fAspect = aspect;
        }

        @Override
        public Function<LamiTableEntry, @Nullable Long> getMapper() {
            return o -> {
                Number res = fAspect.resolveNumber(o);
                if (res == null) {
                    return null;
                }
                return res.longValue();
            };
        }
    }

    private final class LamiDataDoubleResolver extends AbstractDoubleResolver<LamiTableEntry> {
        LamiTableEntryAspect fAspect;

        public LamiDataDoubleResolver(LamiTableEntryAspect aspect) {
            fAspect = aspect;
        }

        @Override
        public Function<LamiTableEntry, @Nullable Double> getMapper() {
            return o -> {
                Number res = fAspect.resolveNumber(o);
                if (res == null) {
                    return null;
                }
                return res.doubleValue();
            };
        }
    }

    private final class LamiDataStringResolver implements IStringResolver<LamiTableEntry> {
        LamiTableEntryAspect fAspect;

        public LamiDataStringResolver(LamiTableEntryAspect aspect) {
            fAspect = aspect;
        }

        @Override
        public Function<@NonNull LamiTableEntry, @Nullable String> getMapper() {
            return o -> fAspect.resolveString(o);
        }
    }

}
