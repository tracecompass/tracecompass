/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.event.types.composite;

import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.Declaration;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IEventHeaderDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDeclaration;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

/**
 * An event header declaration is a declaration of a structure defined in the
 * CTF spec examples section 6.1.1 . It is used in LTTng traces. This will
 * accelerate reading of the trace.
 *
 * Reminder
 *
 * <pre>
 * struct event_header_large {
 *     enum : uint16_t { compact = 0 ... 65534, extended = 65535 } id;
 *     variant <id> {
 *         struct {
 *             uint32_clock_monotonic_t timestamp;
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
public class EventHeaderLargeDeclaration extends Declaration implements IEventHeaderDeclaration {

    /**
     * The id is 16 bits
     */
    private static final int COMPACT_ID = 16;
    private static final int EXTENDED_VALUE = 65535;
    /**
     * Full sized id is 32 bits
     */
    private static final int ID_SIZE = 32;
    /**
     * Full sized timestamp is 64 bits
     */
    private static final int FULL_TS = 64;
    /**
     * Compact timestamp is 32 bits,
     */
    private static final int COMPACT_TS = 32;
    /**
     * Maximum size = largest this header can be
     */
    private static final int MAX_SIZE = 112;
    /**
     * Byte aligned
     */
    private static final int ALIGN = 8;
    /**
     * Name of the variant according to the spec
     */
    private static final String V = "v"; //$NON-NLS-1$
    private static final int VARIANT_SIZE = 2;
    private static final int COMPACT_SIZE = 1;
    private static final int EXTENDED_FIELD_SIZE = 2;

    private final ByteOrder fByteOrder;

    /**
     * Event Header Declaration
     *
     * @param byteOrder
     *            the byteorder
     */
    public EventHeaderLargeDeclaration(ByteOrder byteOrder) {
        fByteOrder = byteOrder;
    }

    @Override
    public EventHeaderDefinition createDefinition(IDefinitionScope definitionScope, String fieldName, BitBuffer input) throws CTFReaderException {
        alignRead(input);
        ByteOrder bo = input.getByteOrder();
        input.setByteOrder(fByteOrder);
        int first = (int) input.get(COMPACT_ID, false);
        long second = input.get(COMPACT_TS, false);
        if (first != EXTENDED_VALUE) {
            input.setByteOrder(bo);
            return new EventHeaderDefinition(this, first, second, COMPACT_TS);
        }
        long timestampLong = input.get(FULL_TS, false);
        input.setByteOrder(bo);
        if (second > Integer.MAX_VALUE) {
            throw new CTFReaderException("ID " + second + " larger than " + Integer.MAX_VALUE + " is currently unsupported by the parser"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        }
        return new EventHeaderDefinition(this, (int) second, timestampLong, FULL_TS);
    }

    @Override
    public long getAlignment() {
        return ALIGN;
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
     * @return true if the event is a large event header
     */
    public static boolean isLargeEventHeader(StructDeclaration declaration) {

        IDeclaration iDeclaration = declaration.getFields().get(ID);
        if (!(iDeclaration instanceof EnumDeclaration)) {
            return false;
        }
        EnumDeclaration eId = (EnumDeclaration) iDeclaration;
        if (eId.getContainerType().getLength() != COMPACT_ID) {
            return false;
        }
        iDeclaration = declaration.getFields().get(V);

        if (!(iDeclaration instanceof VariantDeclaration)) {
            return false;
        }
        VariantDeclaration vDec = (VariantDeclaration) iDeclaration;
        if (!vDec.hasField(COMPACT) || !vDec.hasField(EXTENDED)) {
            return false;
        }
        if (vDec.getFields().size() != VARIANT_SIZE) {
            return false;
        }
        iDeclaration = vDec.getFields().get(COMPACT);
        if (!(iDeclaration instanceof StructDeclaration)) {
            return false;
        }
        StructDeclaration compactDec = (StructDeclaration) iDeclaration;
        if (compactDec.getFields().size() != COMPACT_SIZE) {
            return false;
        }
        if (!compactDec.hasField(TIMESTAMP)) {
            return false;
        }
        iDeclaration = compactDec.getFields().get(TIMESTAMP);
        if (!(iDeclaration instanceof IntegerDeclaration)) {
            return false;
        }
        IntegerDeclaration tsDec = (IntegerDeclaration) iDeclaration;
        if (tsDec.getLength() != COMPACT_TS || tsDec.isSigned()) {
            return false;
        }
        iDeclaration = vDec.getFields().get(EXTENDED);
        if (!(iDeclaration instanceof StructDeclaration)) {
            return false;
        }
        StructDeclaration extendedDec = (StructDeclaration) iDeclaration;
        if (!extendedDec.hasField(TIMESTAMP)) {
            return false;
        }
        if (extendedDec.getFields().size() != EXTENDED_FIELD_SIZE) {
            return false;
        }
        iDeclaration = extendedDec.getFields().get(TIMESTAMP);
        if (!(iDeclaration instanceof IntegerDeclaration)) {
            return false;
        }
        tsDec = (IntegerDeclaration) iDeclaration;
        if (tsDec.getLength() != FULL_TS || tsDec.isSigned()) {
            return false;
        }
        iDeclaration = extendedDec.getFields().get(ID);
        if (!(iDeclaration instanceof IntegerDeclaration)) {
            return false;
        }
        IntegerDeclaration iId = (IntegerDeclaration) iDeclaration;
        if (iId.getLength() != ID_SIZE || iId.isSigned()) {
            return false;
        }
        return true;
    }

}
