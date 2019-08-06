/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.core.filter.model;

/**
 * Interface to leverage commonalities with filters with values
 *
 * @author Matthew Khouzam
 * @since 5.1
 */
public interface ITmfFilterWithValue {

    /**
     * value attribute name, for serialization
     */
    String VALUE_ATTRIBUTE = "value"; //$NON-NLS-1$

    /**
     * Gets the value
     *
     * @return the value
     */
    String getValue();

    /**
     * Sets the value
     *
     * @param value
     *            the value
     */
    void setValue(String value);

}
