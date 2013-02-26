/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.core.control.model;

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
    public String getFieldType();

    /**
     * Sets field type string
     *
     * @param fieldType - sting of event field type
     */
    public void setFieldType(String fieldType);
}
