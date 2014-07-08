/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.types;

import java.util.List;

/**
 * Interface for data definitions containing heterogenous definitions
 * (subfields)
 *
 * @author Matthew Khouzam
 * @since 3.1
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
    List<String> getFieldNames();

}