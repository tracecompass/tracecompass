/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.parsers.custom;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;

/**
 * A type of field for the custom parser's extra field.
 *
 * @author Geneviève Bastien
 */
public class CustomExtraField extends TmfEventField {

    /**
     * Full constructor
     *
     * @param name
     *            the event field id
     * @param value
     *            the event field value
     * @param fields
     *            the list of subfields
     * @throws IllegalArgumentException
     *             If 'name' is null, or if 'fields' has duplicate field names.
     */
    public CustomExtraField(@NonNull String name, @Nullable Object value, ITmfEventField @Nullable [] fields) {
        super(name, value, fields);
    }

    /**
     * Copy constructor
     *
     * @param field
     *            the other event field
     */
    public CustomExtraField(final TmfEventField field) {
        super(field);
    }

}
