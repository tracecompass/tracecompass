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
 *    Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.event.types.composite;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.event.scope.ILexicalScope;
import org.eclipse.tracecompass.ctf.core.event.types.Declaration;
import org.eclipse.tracecompass.ctf.core.event.types.Definition;
import org.eclipse.tracecompass.ctf.core.event.types.ICompositeDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IEventHeaderDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;

import com.google.common.collect.ImmutableList;

/**
 * An event header definition, as shown in the example of the CTF spec examples
 * section 6.1.1
 *
 * @author Matthew Khouzam
 */
public final class EventHeaderDefinition extends Definition implements ICompositeDefinition {

    private static final @NonNull List<@NonNull String> FIELD_NAMES = ImmutableList.of(
            IEventHeaderDeclaration.ID,
            IEventHeaderDeclaration.TIMESTAMP
            );

    private final int fId;
    private final long fTimestamp;
    private final int fTimestampLength;

    /**
     * Event header defintion
     *
     * @param id
     *            the event id
     * @param timestamp
     *            the timestamp
     * @param eventHeaderDecl
     *            The declaration of this defintion
     * @param timestampLength
     *            the number of bits valid in the timestamp
     */
    public EventHeaderDefinition(@NonNull Declaration eventHeaderDecl, int id, long timestamp, int timestampLength) {
        super(eventHeaderDecl, null, ILexicalScope.EVENT_HEADER.getPath(), ILexicalScope.EVENT_HEADER);
        fId = id;
        fTimestamp = timestamp;
        fTimestampLength = timestampLength;
    }

    /**
     * Gets the timestamp declaration
     *
     * @return the timestamp declaration
     */
    public int getTimestampLength() {
        return fTimestampLength;
    }

    /**
     * Get the event id
     *
     * @return the event id
     */
    public int getId() {
        return fId;
    }

    /**
     * Get the timestamp
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return fTimestamp;
    }

    @Override
    public Definition getDefinition(String fieldName) {
        if (fieldName.equals(IEventHeaderDeclaration.ID)) {
            return new IntegerDefinition(IntegerDeclaration.INT_32B_DECL, null, IEventHeaderDeclaration.ID, getId());
        } else if (fieldName.equals(IEventHeaderDeclaration.TIMESTAMP)) {
            return new IntegerDefinition(IntegerDeclaration.INT_64B_DECL, null, IEventHeaderDeclaration.TIMESTAMP, getTimestamp());
        }
        return null;
    }

    @Override
    public @NonNull List<@NonNull String> getFieldNames() {
        return FIELD_NAMES;
    }
}
