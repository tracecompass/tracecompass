/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Simon Delisle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

import java.util.Set;

/**
 * Interface for events to implement to provide information about custom
 * attributes.
 *
 * @author Simon Delisle
 * @since 2.0
 */
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
    String getCustomAttribute(String name);
}