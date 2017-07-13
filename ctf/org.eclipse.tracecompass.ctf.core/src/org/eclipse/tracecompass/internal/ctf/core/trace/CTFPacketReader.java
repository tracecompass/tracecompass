/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.trace;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.CTFStrings;
import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.scope.ILexicalScope;
import org.eclipse.tracecompass.ctf.core.event.types.ICompositeDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IEventHeaderDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.SimpleDatatypeDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.VariantDefinition;
import org.eclipse.tracecompass.ctf.core.trace.CTFIOException;
import org.eclipse.tracecompass.ctf.core.trace.ICTFPacketDescriptor;
import org.eclipse.tracecompass.ctf.core.trace.IPacketReader;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDefinition;
import org.eclipse.tracecompass.internal.ctf.core.event.LostEventDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.types.composite.EventHeaderDefinition;

/**
 * Packet reader with a fixed bit buffer, should be the fast and easily
 * parallelizable one.
 */
@NonNullByDefault
public final class CTFPacketReader implements IPacketReader, IDefinitionScope {

    private static final IDefinitionScope EVENT_HEADER_SCOPE = new IDefinitionScope() {

        @Override
        public @Nullable IDefinition lookupDefinition(@Nullable String lookupPath) {
            return null;
        }

        @Override
        public @Nullable ILexicalScope getScopePath() {
            return null;
        }
    };

    private final BitBuffer fInput;
    private final ICTFPacketDescriptor fPacketContext;
    private final List<@Nullable IEventDeclaration> fDeclarations;
    private boolean fHasLost;
    private long fLastTimestamp;
    private @Nullable final IDeclaration fStreamEventHeaderDecl;

    private @Nullable final StructDeclaration fStreamContext;

    private @Nullable final ICompositeDefinition fTracePacketHeader;

    private @Nullable final IDefinitionScope fPacketScope;

    private @Nullable ICompositeDefinition fEventHeader;

    /**
     * Constructor
     *
     * @param input
     *            input {@link BitBuffer}
     * @param packetContext
     *            packet_context where we get info like lost events and cpu_id
     * @param declarations
     *            event declarations for this packet reader
     * @param eventHeaderDeclaration
     *            event header declaration, what to read before any given event, to
     *            find it's id
     * @param streamContext
     *            the context declaration
     * @param packetHeader
     *            the header with the magic numbers and such
     * @param packetScope
     *            the scope of the packetHeader
     */
    public CTFPacketReader(BitBuffer input, ICTFPacketDescriptor packetContext, List<@Nullable IEventDeclaration> declarations, @Nullable IDeclaration eventHeaderDeclaration, @Nullable StructDeclaration streamContext,
            @Nullable ICompositeDefinition packetHeader,
            IDefinitionScope packetScope) {
        fInput = input;
        fPacketContext = packetContext;
        fDeclarations = declarations;
        fPacketScope = packetScope;
        fHasLost = fPacketContext.getLostEvents() != 0;
        fLastTimestamp = fPacketContext.getTimestampBegin();
        fStreamEventHeaderDecl = eventHeaderDeclaration;
        fStreamContext = streamContext;
        fTracePacketHeader = packetHeader;
    }

    @Override
    public int getCPU() {
        return (int) fPacketContext.getTargetId();
    }

    @Override
    public boolean hasMoreEvents() {
        return fHasLost || (fInput.position() < fPacketContext.getContentSizeBits());
    }

    @Override
    public EventDefinition readNextEvent() throws CTFException {
        int eventID = (int) IEventDeclaration.UNSET_EVENT_ID;
        final long posStart = fInput.position();
        /*
         * Return the Lost Event after all other events in this packet. We need to check
         * if the bytebuffer is at the beginning too.
         */
        if (fHasLost && (posStart >= fPacketContext.getContentSizeBits())) {
            fHasLost = false;
            return createLostEvent(fPacketContext);
        }

        fEventHeader = null;
        /* Read the stream event header. */
        final IDeclaration streamEventHeaderDecl = fStreamEventHeaderDecl;
        if (streamEventHeaderDecl instanceof IEventHeaderDeclaration) {
            IEventHeaderDeclaration eventHeaderDeclaration = (IEventHeaderDeclaration) streamEventHeaderDecl;
            EventHeaderDefinition ehd = (EventHeaderDefinition) eventHeaderDeclaration.createDefinition(EVENT_HEADER_SCOPE, "", fInput); //$NON-NLS-1$
            fEventHeader = ehd;
            eventID = ehd.getId();
        } else if (streamEventHeaderDecl instanceof StructDeclaration) {
            StructDefinition structEventHeaderDef = ((StructDeclaration) streamEventHeaderDecl).createDefinition(EVENT_HEADER_SCOPE, ILexicalScope.EVENT_HEADER, fInput);
            fEventHeader = structEventHeaderDef;
            /* Check for the event id. */
            IDefinition idDef = structEventHeaderDef.lookupDefinition("id"); //$NON-NLS-1$
            SimpleDatatypeDefinition simpleIdDef = null;
            if (idDef instanceof SimpleDatatypeDefinition) {
                simpleIdDef = ((SimpleDatatypeDefinition) idDef);
            } else if (idDef != null) {
                throw new CTFIOException("Id defintion not an integer, enum or float definiton in event header."); //$NON-NLS-1$
            }
            /* Check for the variant v. */
            IDefinition variantDef = structEventHeaderDef.lookupDefinition("v"); //$NON-NLS-1$
            if (variantDef instanceof VariantDefinition) {

                /* Get the variant current field */
                StructDefinition variantCurrentField = (StructDefinition) ((VariantDefinition) variantDef).getCurrentField();

                /*
                 * Try to get the id field in the current field of the variant. If it is
                 * present, it overrides the previously read event id.
                 */
                IDefinition vIdDef = variantCurrentField.lookupDefinition("id"); //$NON-NLS-1$
                if (vIdDef instanceof IntegerDefinition) {
                    simpleIdDef = (SimpleDatatypeDefinition) vIdDef;
                }

            }
            if (simpleIdDef != null) {
                eventID = simpleIdDef.getIntegerValue().intValue();
            }
        }
        /* Single event type in a trace */
        if (eventID == IEventDeclaration.UNSET_EVENT_ID && fDeclarations.size() == 1) {
            eventID = 0;
        }
        if (eventID < 0 || eventID >= fDeclarations.size()) {
            throw new CTFIOException("Invalid event id : " + eventID + " File position : " + fInput.position() / 8 + '/' + fPacketContext.getContentSizeBits() / 8); //$NON-NLS-1$ //$NON-NLS-2$
        }
        /* Get the right event definition using the event id. */
        IEventDeclaration eventDeclaration = fDeclarations.get(eventID);
        if (!(eventDeclaration instanceof EventDeclaration)) {
            throw new CTFIOException("Invalid event id : " + eventID); //$NON-NLS-1$
        }
        EventDeclaration declaration = (EventDeclaration) eventDeclaration;
        EventDefinition eventDef = declaration.createDefinition(fStreamContext, fPacketContext, fTracePacketHeader, fEventHeader, fInput, fLastTimestamp);
        fLastTimestamp = eventDef.getTimestamp();
        /*
         * Set the event timestamp using the timestamp calculated by updateTimestamp.
         */

        if (posStart == fInput.position()) {
            throw new CTFIOException("Empty event not allowed, event: " + eventDef.getDeclaration().getName()); //$NON-NLS-1$
        }

        return eventDef;
    }

    private EventDefinition createLostEvent(final ICTFPacketDescriptor currentPacket) {
        IEventDeclaration lostEventDeclaration = LostEventDeclaration.INSTANCE;
        StructDeclaration lostFields = lostEventDeclaration.getFields();
        // this is a hard coded map, we know it's not null
        IntegerDeclaration lostFieldsDecl = (IntegerDeclaration) lostFields.getField(CTFStrings.LOST_EVENTS_FIELD);
        if (lostFieldsDecl == null) {
            throw new IllegalStateException("Lost events count not declared!"); //$NON-NLS-1$
        }
        IntegerDeclaration lostEventsDurationDecl = (IntegerDeclaration) lostFields.getField(CTFStrings.LOST_EVENTS_DURATION);
        if (lostEventsDurationDecl == null) {
            throw new IllegalStateException("Lost events duration not declared!"); //$NON-NLS-1$
        }
        long lostEventsTimestamp = fLastTimestamp;
        long lostEventsDuration = currentPacket.getTimestampEnd() - lostEventsTimestamp;
        IntegerDefinition lostDurationDef = new IntegerDefinition(lostFieldsDecl, null, CTFStrings.LOST_EVENTS_DURATION, lostEventsDuration);
        IntegerDefinition lostCountDef = new IntegerDefinition(lostEventsDurationDecl, null, CTFStrings.LOST_EVENTS_FIELD, fPacketContext.getLostEvents());
        IntegerDefinition[] fields = new IntegerDefinition[] { lostCountDef, lostDurationDef };
        int cpu = (int) fPacketContext.getTargetId();
        return new EventDefinition(
                lostEventDeclaration,
                cpu,
                lostEventsTimestamp,
                null,
                null,
                null,
                null,
                new StructDefinition(
                        lostFields,
                        this, "fields", //$NON-NLS-1$
                        fields),
                fPacketContext);
    }

    @Override
    public ILexicalScope getScopePath() {
        return ILexicalScope.PACKET;
    }

    @Override
    public @Nullable IDefinition lookupDefinition(@Nullable String lookupPath) {
        if (ILexicalScope.TRACE_PACKET_HEADER.getPath().equals(lookupPath)) {
            return fTracePacketHeader;
        } else if (ILexicalScope.STREAM_PACKET_CONTEXT.getPath().equals(lookupPath) && fPacketScope != null) {
            return fPacketScope.lookupDefinition(lookupPath);
        }
        return null;
    }

    @Override
    public ICTFPacketDescriptor getCurrentPacket() {
        return fPacketContext;
    }

    /**
     * TODO: remove when API is reworked a bit.
     */
    @Override
    public @Nullable ICompositeDefinition getCurrentPacketEventHeader() {
        return fEventHeader;
    }

}
