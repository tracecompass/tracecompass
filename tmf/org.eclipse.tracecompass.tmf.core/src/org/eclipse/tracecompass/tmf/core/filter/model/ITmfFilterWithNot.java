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
 * Interface to leverage commonalities with filters that can be negated.
 *
 * @author Matthew Khouzam
 * @since 5.1
 */
public interface ITmfFilterWithNot {

    /**
     * Gets the not state
     *
     * @return the equals not state
     */
    public boolean isNot();

    /**
     * Sets the current not state
     *
     * @param not
     *            the not state
     */
    public void setNot(boolean not);
}
