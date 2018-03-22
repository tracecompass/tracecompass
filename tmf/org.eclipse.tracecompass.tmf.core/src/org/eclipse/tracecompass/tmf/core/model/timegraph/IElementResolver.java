/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.tmf.core.model.timegraph;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Element Resolver, can be used to convert an element to a set of key value
 * pairs.
 *
 * Possible key strings to use in the map are provider by this interface
 *
 * Candidates to use this are
 * <ul>
 * <li>States</li>
 * <li>Events</li>
 * <li>Colors</li>
 * <li>...</li>
 * </ul>
 *
 * @author Jean-Christian Kouame
 * @since 4.0
 */
public interface IElementResolver {

    /**
     * The key to get the label
     */
    static final String LABEL_KEY= "label"; //$NON-NLS-1$

    /**
     * the key to get the entry name
     */
    static final String ENTRY_NAME_KEY = "entry"; //$NON-NLS-1$

    /**
     * Get available information from an item and return it into a key-value map
     *
     * @return The map of data
     */
    Map<@NonNull String, @NonNull String> computeData();
}