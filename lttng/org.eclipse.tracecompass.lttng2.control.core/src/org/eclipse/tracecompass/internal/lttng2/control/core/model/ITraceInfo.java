/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.core.model;

/**
 * <p>
 * Interface for retrieve trace comon information.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface ITraceInfo {
    /**
     * @return the name of the information element.
     */
    String getName();

    /**
     * Sets the name of the information element.
     *
     * @param name
     *            The name to assign
     */
    void setName(String name);

}
