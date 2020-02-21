/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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
 * Interface for retrieval of event field information.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface IFieldInfo extends ITraceInfo {

    /**
     * @return the event field type
     */
    String getFieldType();

    /**
     * Sets field type string
     *
     * @param fieldType - sting of event field type
     */
    void setFieldType(String fieldType);
}
