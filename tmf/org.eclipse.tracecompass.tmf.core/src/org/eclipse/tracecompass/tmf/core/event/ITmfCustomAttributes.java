/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Simon Delisle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface for events to implement to provide information about custom
 * attributes.
 *
 * @author Simon Delisle
 */
@NonNullByDefault
public interface ITmfCustomAttributes {

    /**
     * List the custom attributes of this event.
     *
     * @return The list of custom attribute names. Should not be null, but could
     *         be empty.
     */
    Set<String> listCustomAttributes();

    /**
     * Get the value of a custom attribute.
     *
     * @param name
     *            Name of the the custom attribute
     * @return Value of this attribute, or null if there is no attribute with
     *         that name
     */
    @Nullable String getCustomAttribute(String name);
}