/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.chart.core.model;

import java.util.Collection;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;

/**
 * This is the base class used by the chart plugin to make a chart. In order to
 * make a chart with the default chart maker dialog, this interface must be
 * implemented. It should also be noted that we can make a chart without using
 * this interface by building the series manually.
 * <p>
 * This data provider needs to provide the following:
 * <ul>
 * <li>a name</li>
 * <li>a stream of raw objects</li>
 * <li>a list of data descriptors</li>
 * </ul>
 * The name is the name of the data provider. It is the name used in the chart.
 * For the stream and the descriptors, consider a table containing multiples
 * rows and columns. Here, data descriptors would describe each column. They
 * would tell information about the type of data inside a column.
 * <p>
 * With such table, it would be possible to make a chart with two columns (e.g.
 * scatter plot). Rather than providing each column, this class provides a
 * stream of all the rows. This facilitate data processing. In order to generate
 * data from rows, data descriptors are used for mapping values inside.
 * <p>
 * This analogy of a table is not the limit of this plugin. We can have a type
 * more complex than simple row as long as a data descriptor can be used to
 * obtain values from it.
 *
 * @param <T>
 *            The type of objects it provides
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IDataChartProvider<T> {

    /**
     * This method returns the name of the data provider.
     *
     * @return The name of the data provider
     */
    String getName();

    /**
     * This method returns the stream of raw data that will be used for making
     * charts.
     *
     * @return The source of data
     */
    Stream<@NonNull T> getSource();

    /**
     * This method returns a list of data descriptors used for describing the
     * data returned by {@link #getSource()}. Each descriptor describes possible
     * data that can be extracted from each object of the input source.
     *
     * @return The list of descriptors
     */
    Collection<IDataChartDescriptor<T, ?>> getDataDescriptors();

}
