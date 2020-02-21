/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.chart.core.tests.stubs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DataChartDurationDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DataChartStringDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DataChartTimestampDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.AbstractLongResolver;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.INumericalResolver;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.IStringResolver;

/**
 * A chart provider that returns a few descriptors of each types. It allows to
 * test the interactions of many types of descriptors, similar and different.
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public class StubChartProviderFull extends StubChartProvider {

    /**
     * Name of a second String descriptor. It will return the String of the stub
     * object, prefixed with "alt_"
     */
    public static final String SECOND_STRING_DESCRIPTOR = "String2";
    /**
     * Name of a duration descriptor. It will return the Integer field of the
     * stub object
     */
    public static final String DURATION_DESCRIPTOR = "Duration";
    /**
     * Name of a second duration descriptor. It will return the Integer field of
     * the stub object, plus 10
     */
    public static final String SECOND_DURATION_DESCRIPTOR = "Duration2";
    /**
     * Name of a timestamp descriptor. It will return the Long field of the stub
     * object
     */
    public static final String TIMESTAMP_DESCRIPTOR = "Timestamp";
    /**
     * Name of a second timestamp descriptor. It will return the Long field of
     * the stub object, plus 1000
     */
    public static final String SECOND_TIMESTAMP_DESCRIPTOR = "Timestamp2";
    private @Nullable List<IDataChartDescriptor<StubObject, ?>> fDescriptors = null;

    @Override
    public Collection<IDataChartDescriptor<StubObject, ?>> getDataDescriptors() {
        List<IDataChartDescriptor<StubObject, ?>> descriptors = fDescriptors;
        if (descriptors == null) {
            descriptors = new ArrayList<>(super.getDataDescriptors());
            descriptors.add(new DataChartStringDescriptor<>(SECOND_STRING_DESCRIPTOR, new IStringResolver<StubObject>() {

                @Override
                public @NonNull Function<StubObject, @Nullable String> getMapper() {
                    return o -> "alt_" + o.getString();
                }
            }));
            descriptors.add(new DataChartDurationDescriptor<>(DURATION_DESCRIPTOR, new INumericalResolver<StubObject, @NonNull Integer>() {

                @Override
                public @NonNull Function<StubObject, @Nullable Integer> getMapper() {
                    return o -> o.getInt();
                }

                @Override
                public @NonNull Comparator<@NonNull Integer> getComparator() {
                    return NonNullUtils.checkNotNull(Comparator.naturalOrder());
                }

                @Override
                public Integer getMinValue() {
                    return Integer.MIN_VALUE;
                }

                @Override
                public Integer getMaxValue() {
                    return Integer.MAX_VALUE;
                }

                @Override
                public Integer getZeroValue() {
                    return 0;
                }
            }));
            descriptors.add(new DataChartDurationDescriptor<>(SECOND_DURATION_DESCRIPTOR, new INumericalResolver<StubObject, @NonNull Integer>() {

                @Override
                public @NonNull Function<StubObject, @Nullable Integer> getMapper() {
                    return o -> o.getInt() + 10;
                }

                @Override
                public @NonNull Comparator<@NonNull Integer> getComparator() {
                    return NonNullUtils.checkNotNull(Comparator.naturalOrder());
                }

                @Override
                public Integer getMinValue() {
                    return Integer.MIN_VALUE;
                }

                @Override
                public Integer getMaxValue() {
                    return Integer.MAX_VALUE;
                }

                @Override
                public Integer getZeroValue() {
                    return 0;
                }
            }));
            descriptors.add(new DataChartTimestampDescriptor<>(TIMESTAMP_DESCRIPTOR, new AbstractLongResolver<StubObject>() {

                @Override
                public @NonNull Function<StubObject, @Nullable Long> getMapper() {
                    return o -> o.getLong();
                }
            }));
            descriptors.add(new DataChartTimestampDescriptor<>(SECOND_TIMESTAMP_DESCRIPTOR, new AbstractLongResolver<StubObject>() {

                @Override
                public @NonNull Function<StubObject, @Nullable Long> getMapper() {
                    return o -> o.getLong() + 1000;
                }
            }));
            fDescriptors = descriptors;
        }
        return descriptors;
    }

}
