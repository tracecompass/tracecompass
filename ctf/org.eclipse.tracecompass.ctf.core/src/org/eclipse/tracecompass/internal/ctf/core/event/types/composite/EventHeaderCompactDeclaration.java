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

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.types.Declaration;
import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IEventHeaderDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.VariantDeclaration;

/**
 * An event header declaration is a declaration of a structure defined in the
 * CTF spec examples section 6.1.1 . It is used in LTTng traces. This will
 * accelerate reading of the trace.
 *
 * Reminder
 *
 * <pre>
 * struct event_header_compact {
 *     enum : uint5_t { compact = 0 ... 30, extended = 31 } id;
 *     variant <id> {
 *         struct {
 *             uint27_clock_monotonic_t timestamp;
 *         } compact;
 *         struct {
 *             uint32_t id;
 *             uint64_clock_monotonic_t timestamp;
 *         } extended;
 *     } v;
 * } align(8);
 * </pre>
 *
 * @author Matthew Khouzam
 */
@NonNullByDefault
public final class EventHeaderCompactDeclaration extends Declaration implements IEventHeaderDeclaration {

    private static final int BASE_10 = 10;
    /**
     * The id is 5 bits
     */
    private static final int COMPACT_ID = 5;
    private static final int EXTENDED_VALUE = (1 << COMPACT_ID) - 1;
    /**
     * Full sized id is 32 bits
     */
    private static final int ID_SIZE = 32;
    /**
     * Full sized timestamp is 64 bits
     */
    private static final int FULL_TS = 64;
    /**
     * Compact timestamp is 27 bits,
     */
    private static final int COMPACT_TS = 27;
    /**
     * Clock identifier
     */
    private static final String CLOCK = ""; //$NON-NLS-1$
    /**
     * Maximum size = largest this header can be
     */
    private static final int MAX_SIZE = 104;
    /**
     * Byte aligned
     */
    private static final int ALIGN_ON_1 = 1;
    private static final int ALIGN_ON_8 = 8;

    private final ByteOrder fByteOrder;
    private final List<StructDeclaration> fReferenceStructs = new ArrayList<>();

    /**
     * Big-Endian Large Event Header
     */
    private static final EventHeaderCompactDeclaration EVENT_HEADER_BIG_ENDIAN = new EventHeaderCompactDeclaration(nullCheck(ByteOrder.BIG_ENDIAN));

    /**
     * Little-Endian Large Event Header
     */
    private static final EventHeaderCompactDeclaration EVENT_HEADER_LITTLE_ENDIAN = new EventHeaderCompactDeclaration(nullCheck(ByteOrder.LITTLE_ENDIAN));

    /**
     * Event Header Declaration
     *
     * @param byteOrder
     *            the byteorder
     */
    private EventHeaderCompactDeclaration(ByteOrder byteOrder) {
        fByteOrder = byteOrder;
        populateReferences();
    }

    private void populateReferences() {
        if (!fReferenceStructs.isEmpty()) {
            return;
        }
        StructDeclaration ref = new StructDeclaration(ALIGN_ON_8);
        EnumDeclaration id = new EnumDeclaration(IntegerDeclaration.createDeclaration(COMPACT_ID, false, BASE_10, fByteOrder, Encoding.NONE, CLOCK, ALIGN_ON_1));
        id.add(0, EXTENDED_VALUE - 1, COMPACT);
        id.add(EXTENDED_VALUE, EXTENDED_VALUE, EXTENDED);
        ref.addField(ID, id);
        VariantDeclaration v = new VariantDeclaration();
        StructDeclaration compact = new StructDeclaration(ALIGN_ON_1);
        compact.addField(TIMESTAMP, IntegerDeclaration.createDeclaration(COMPACT_TS, false, BASE_10, fByteOrder, Encoding.NONE, CLOCK, ALIGN_ON_1));
        StructDeclaration extended = new StructDeclaration(ALIGN_ON_8);
        extended.addField(ID, IntegerDeclaration.createDeclaration(ID_SIZE, false, BASE_10, fByteOrder, Encoding.NONE, CLOCK, ALIGN_ON_8));
        extended.addField(TIMESTAMP, IntegerDeclaration.createDeclaration(FULL_TS, false, BASE_10, fByteOrder, Encoding.NONE, CLOCK, ALIGN_ON_8));
        v.addField(COMPACT, compact);
        v.addField(EXTENDED, extended);
        ref.addField(VARIANT_NAME, v);
        fReferenceStructs.add(ref);
    }

    /**
     * Gets an {@link EventHeaderCompactDeclaration} of a given ByteOrder
     *
     * @param byteOrder
     *            the byte order
     * @return the header declaration
     */
    public static EventHeaderCompactDeclaration getEventHeader(@Nullable ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return EVENT_HEADER_BIG_ENDIAN;
        }
        return EVENT_HEADER_LITTLE_ENDIAN;
    }

    @Override
    public EventHeaderDefinition createDefinition(@Nullable IDefinitionScope definitionScope, String fieldName, BitBuffer input) throws CTFException {
        alignRead(input);
        ByteOrder bo = input.getByteOrder();
        input.setByteOrder(fByteOrder);
        int enumId = (int) input.get(COMPACT_ID, false);
        if (enumId != EXTENDED_VALUE) {
            long timestamp2 = input.get(COMPACT_TS, false);
            input.setByteOrder(bo);
            return new EventHeaderDefinition(this, enumId, timestamp2, COMPACT_TS);
        }
        // needed since we read 5 bits
        input.position(input.position() + 3);
        long id = input.get(ID_SIZE, false);
        if (id > Integer.MAX_VALUE) {
            throw new CTFException("ID " + id + " larger than " + Integer.MAX_VALUE + " is currently unsupported by the parser"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        }
        long timestampLong = input.get(FULL_TS, false);
        input.setByteOrder(bo);
        return new EventHeaderDefinition(this, (int) id, timestampLong, FULL_TS);

    }

    @Override
    public long getAlignment() {
        return ALIGN_ON_8;
    }

    @Override
    public int getMaximumSize() {
        return MAX_SIZE;
    }

    /**
     * Check if a given struct declaration is an event header
     *
     * @param declaration
     *            the declaration
     * @return true if the struct is a compact event header
     */
    public boolean isCompactEventHeader(@Nullable StructDeclaration declaration) {
        if (declaration == null) {
            return false;
        }
        for (IDeclaration ref : fReferenceStructs) {
            if (ref.isBinaryEquivalent(declaration)) {
                return true;
            }
        }
        return false;
    }

    private static ByteOrder nullCheck(@Nullable ByteOrder bo) {
        if (bo == null) {
            throw new IllegalStateException("Could not create byteorder"); //$NON-NLS-1$
        }
        return bo;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (fByteOrder.equals(ByteOrder.BIG_ENDIAN) ? 4321 : 1234);
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EventHeaderCompactDeclaration other = (EventHeaderCompactDeclaration) obj;
        return (fByteOrder.equals(other.fByteOrder));
    }

    @Override
    public boolean isBinaryEquivalent(@Nullable IDeclaration other) {
        for (StructDeclaration referenceStruct : fReferenceStructs) {
            if (referenceStruct.isBinaryEquivalent(other)) {
                return true;
            }
        }
        return false;
    }

}
