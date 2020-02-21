/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.ui.consumer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.IStringResolver;
import org.eclipse.tracecompass.internal.tmf.chart.core.consumer.IDataConsumer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;

/**
 * This class processes string values in order to create valid data for a
 * scatter chart. It takes a {@link IStringResolver} for mapping values.
 * <p>
 * The current implementation of the scatter chart maps each unique string to an
 * int. It is different than the bar chart because the other one cannot allow
 * multiple Y values on an X value. With this consumer, all object sharing a
 * same string value will also share the same value on the axis.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ScatterStringConsumer implements IDataConsumer {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final IStringResolver<Object> fResolver;
    private final BiMap<String, Integer> fMap;
    /** The list of value for each object consumed */
    private final List<String> fList = new ArrayList<>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param resolver
     *            The resolver that consumes values
     */
    public ScatterStringConsumer(IStringResolver<Object> resolver) {
        fResolver = resolver;
        fMap = HashBiMap.create();
    }

    /**
     * Surcharged constructor with a bimap provided.
     *
     * @param resolver
     *            The resolver that consumes values
     * @param map
     *            The bimap to store values
     */
    public ScatterStringConsumer(IStringResolver<Object> resolver, BiMap<String, Integer> map) {
        fResolver = resolver;
        fMap = map;
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public boolean test(Object obj) {
        return true;
    }

    @Override
    public void accept(@NonNull Object obj) {
        String str = fResolver.getMapper().apply(obj);

        /* Convert null string to unknown */
        if (str == null) {
            str = "?"; //$NON-NLS-1$
        }

        fList.add(str);
        fMap.putIfAbsent(str, fMap.size());
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns the list of string value for each object.
     *
     * @return The list of string
     */
    public List<String> getList() {
        return ImmutableList.copyOf(fList);
    }

    /**
     * Accessor that returns the map between strings and numbers used in a
     * scatter chart.
     *
     * @return The map of string
     */
    public BiMap<String, Integer> getMap() {
        return ImmutableBiMap.copyOf(fMap);
    }

}
