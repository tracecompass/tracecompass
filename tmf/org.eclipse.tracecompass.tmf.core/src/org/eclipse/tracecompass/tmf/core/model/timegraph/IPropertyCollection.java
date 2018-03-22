/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.tmf.core.model.timegraph;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface to get and set properties. This represents a group of items known
 * as properties. It provides a caching method for filtering elements, storing
 * the results as properties.
 *
 * A developer should use this to store the results of tests to be able to
 * exchange these with another process that can act upon this "report".
 *
 * @author Jean-Christian Kouame
 * @since 4.0
 *
 */
@NonNullByDefault
public interface IPropertyCollection {

    /**
     * Get all the item properties
     *
     * @return The properties
     */
    Set<String> getActiveProperties();

    /**
     * Activate/deactivate a property. The possible properties could be found in
     * {@link IFilterProperty}
     *
     * @param property
     *            the property key
     * @param isActive
     *            the activation status of the property.
     */
    default void setProperty(@NonNull String property, boolean isActive) {
        Set<String> activeProperties = getActiveProperties();
        if (isActive) {
            activeProperties.add(property);
        } else if (activeProperties.contains(property)) {
            activeProperties.remove(property);
        }
    }

    /**
     * Get the active status of a specific property. The possible properties could
     * be found in {@link IFilterProperty}
     *
     * @param property
     *            The property key
     * @return The property activation status false if not set
     */
    default boolean isPropertyActive(@NonNull String property) {
        return getActiveProperties().contains(property);
    }
}
