/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.types;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Interface for data definitions containing heterogenous definitions
 * (subfields)
 *
 * @author Matthew Khouzam
 */
public interface ICompositeDefinition extends IDefinition {

    /**
     * Gets the definition of the field
     *
     * @param fieldName
     *            the fieldname
     * @return The definitions of all the fields
     */
    Definition getDefinition(String fieldName);

    /**
     * Gets an array of the field names
     *
     * @return the field names array
     */
    List<@NonNull String> getFieldNames();

}
