/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.tmf.core.model.timegraph;

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
public interface IPropertyCollection {

    /**
     * Get the item active properties value. Each bit of the value corresponds to a
     * item property. Available properties can be found at {@link IFilterProperty}.
     *
     * @return The properties value
     */
    default int getActiveProperties() {
        return 0;
    }

    /**
     * Set the active properties value. Each bit of the value corresponds to a item
     * property. Available properties can be found at {@link IFilterProperty}.
     *
     * @param activeProperties
     *            The active properties value
     */
    default void setActiveProperties(int activeProperties) {
        // Do nothing
    }

    /**
     * Activate/deactivate a property. The possible properties could be found in
     * {@link IFilterProperty}
     *
     * @param propertyMask
     *            The property key found in {@link IFilterProperty}
     * @param isActive
     *            The activation status of the property.
     */
    default void setProperty(int propertyMask, boolean isActive) {
        int activeProperties = getActiveProperties();
        if (isActive) {
            activeProperties |= propertyMask;
        } else {
            activeProperties &= ~propertyMask;
        }
        setActiveProperties(activeProperties);
    }

    /**
     * Get the active status of a specific property. The possible properties could
     * be found in {@link IFilterProperty}
     *
     * @param propertyMask
     *            The property key found in {@link IFilterProperty}
     * @return The property activation status false if not set
     */
    default boolean isPropertyActive(int propertyMask) {
        return (getActiveProperties() & propertyMask) != 0;
    }
}
