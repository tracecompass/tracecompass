/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.chart.core.tests.stubs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DataChartNumericalDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DataChartStringDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.AbstractDoubleResolver;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.AbstractLongResolver;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.INumericalResolver;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.IStringResolver;
import org.eclipse.tracecompass.tmf.chart.core.model.IDataChartProvider;

/**
 * A chart provider that will return a few long values
 *
 * @author Geneviève Bastien
 */
public class StubChartProvider implements IDataChartProvider<StubObject> {

    /**
     * Name of this chart provider
     */
    public static final @NonNull String NAME = "Long Chart Provider";
    /**
     * Name of the String descriptor
     */
    public static final @NonNull String STRING_DESCRIPTOR = "String";
    /**
     * Name of the Integer descriptor
     */
    public static final @NonNull String INTEGER_DESCRIPTOR = "Integer";
    /**
     * Name of the Long descriptor
     */
    public static final @NonNull String LONG_DESCRIPTOR = "Long";
    /**
     * Name of the Double descriptor
     */
    public static final @NonNull String DOUBLE_DESCRIPTOR = "Double";

    private final @NonNull List<@NonNull StubObject> fSource = new ArrayList<>();

    @Override
    public @NonNull String getName() {
        return NAME;
    }

    @Override
    public @NonNull Stream<@NonNull StubObject> getSource() {
        return NonNullUtils.checkNotNull(fSource.stream());
    }

    /**
     * Adds an object to this provider's source. It will be returned with the
     * stream
     *
     * @param obj
     *            an object to add to the data stream
     */
    public void addData(@NonNull StubObject obj) {
        fSource.add(obj);
    }

    @Override
    public @NonNull Collection<@NonNull IDataChartDescriptor<StubObject, ?>> getDataDescriptors() {
        List<@NonNull IDataChartDescriptor<StubObject, ?>> list = new ArrayList<>();
        list.add(new DataChartStringDescriptor<>(STRING_DESCRIPTOR, new IStringResolver<StubObject>() {

            @Override
            public @NonNull Function<StubObject, @Nullable String> getMapper() {
                return o -> o.getString();
            }
        }));
        list.add(new DataChartNumericalDescriptor<>(INTEGER_DESCRIPTOR, new INumericalResolver<StubObject, @NonNull Integer>() {

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
        list.add(new DataChartNumericalDescriptor<>(LONG_DESCRIPTOR, new AbstractLongResolver<StubObject>() {

            @Override
            public @NonNull Function<StubObject, @Nullable Long> getMapper() {
                return o -> o.getLong();
            }
        }));
        list.add(new DataChartNumericalDescriptor<>(DOUBLE_DESCRIPTOR, new AbstractDoubleResolver<StubObject>() {

            @Override
            public @NonNull Function<StubObject, @Nullable Double> getMapper() {
                return o -> o.getDbl();
            }
        }));
        return list;
    }

    /**
     * Get a descriptor with the specified name
     *
     * @param name
     *            The name of the desired descriptor
     * @return The descriptor
     */
    public IDataChartDescriptor<StubObject, ?> getDataDescriptor(String name) {
        return getDataDescriptors().stream().filter(d -> d.getName().equals(name))
                .findFirst().get();
    }

}
