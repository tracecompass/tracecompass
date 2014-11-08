/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *   Alexandre Montplaisir - Update to new TmfEventTableColumn API
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.pcap.ui.editor;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.pcap.core.event.PcapEvent;
import org.eclipse.tracecompass.internal.tmf.pcap.core.protocol.TmfPcapProtocol;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.ui.viewers.events.columns.ITmfEventTableColumns;
import org.eclipse.tracecompass.tmf.ui.viewers.events.columns.TmfEventTableColumn;

import com.google.common.collect.ImmutableList;

/**
 * Default event table for pcap traces.
 *
 * @author Vincent Perot
 */
public class PcapEventTableColumns implements ITmfEventTableColumns {

    // ------------------------------------------------------------------------
    // Table data
    // ------------------------------------------------------------------------

    @SuppressWarnings("null")
    private static final @NonNull Collection<TmfEventTableColumn> PCAP_COLUMNS = ImmutableList.of(
            new TmfEventTableColumn(ITmfEventAspect.BaseAspects.TIMESTAMP),
            new TmfEventTableColumn(new PcapSourceAspect()),
            new TmfEventTableColumn(new PcapDestinationAspect()),
            new TmfEventTableColumn(new PcapReferenceAspect()),
            new TmfEventTableColumn(new PcapProtocolAspect()),
            new TmfEventTableColumn(ITmfEventAspect.BaseAspects.CONTENTS)
            );

    /**
     * The "packet source" column for pcap events
     */
    private static class PcapSourceAspect implements ITmfEventAspect {

        @Override
        public String getName() {
            return getString(Messages.PcapEventsTable_Source);
        }

        @Override
        public String getHelpText() {
            return EMPTY_STRING;
        }

        @Override
        public String resolve(ITmfEvent event) {
            if (!(event instanceof PcapEvent)) {
                return EMPTY_STRING;
            }
            PcapEvent pcapEvent = (PcapEvent) event;
            TmfPcapProtocol protocol = pcapEvent.getMostEncapsulatedProtocol();

            return getString(pcapEvent.getSourceEndpoint(protocol));
        }

        @Override
        public @NonNull String getFilterId() {
            return PcapEvent.EVENT_FIELD_PACKET_SOURCE;
        }
    }

    /**
     * The "packet destination" column for pcap events
     */
    private static class PcapDestinationAspect implements ITmfEventAspect {

        @Override
        public String getName() {
            return getString(Messages.PcapEventsTable_Destination);
        }

        @Override
        public String getHelpText() {
            return EMPTY_STRING;
        }

        @Override
        public String resolve(ITmfEvent event) {
            if (!(event instanceof PcapEvent)) {
                return EMPTY_STRING;
            }
            PcapEvent pcapEvent = (PcapEvent) event;
            TmfPcapProtocol protocol = pcapEvent.getMostEncapsulatedProtocol();
            return getString(pcapEvent.getDestinationEndpoint(protocol));
        }

        @Override
        public @NonNull String getFilterId() {
            return PcapEvent.EVENT_FIELD_PACKET_DESTINATION;
        }
    }

    /**
     * The "packet reference" column for pcap events
     */
    private static class PcapReferenceAspect implements ITmfEventAspect {

        @Override
        public String getName() {
            return getString(Messages.PcapEventsTable_Reference);
        }

        @Override
        public String getHelpText() {
            return EMPTY_STRING;
        }

        @Override
        public String resolve(ITmfEvent event) {
            if (!(event instanceof PcapEvent)) {
                return EMPTY_STRING;
            }
            return ((PcapEvent) event).getReference();
        }

        @Override
        public @Nullable String getFilterId() {
            return ITmfEvent.EVENT_FIELD_REFERENCE;
        }
    }

    /**
     * The "packet protocol" column for pcap events
     */
    private static class PcapProtocolAspect implements ITmfEventAspect {

        @Override
        public String getName() {
            return getString(Messages.PcapEventsTable_Protocol);
        }

        @Override
        public String getHelpText() {
            return EMPTY_STRING;
        }

        @Override
        public String resolve(ITmfEvent event) {
            if (!(event instanceof PcapEvent)) {
                return EMPTY_STRING;
            }
            PcapEvent pcapEvent = (PcapEvent) event;
            TmfPcapProtocol protocol = pcapEvent.getMostEncapsulatedProtocol();

            @SuppressWarnings("null")
            @NonNull String proto = protocol.getShortName().toUpperCase();
            return proto;
        }

        @Override
        public @Nullable String getFilterId() {
            return PcapEvent.EVENT_FIELD_PACKET_PROTOCOL;
        }
    }

    /**
     * Little convenience method to work around the incompatibilities between
     * null annotations and NLS files...
     */
    private static String getString(@Nullable String str) {
        return (str == null ? "" : str); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // ITmfEventTableColumns
    // ------------------------------------------------------------------------

    @Override
    public Collection<? extends TmfEventTableColumn> getEventTableColumns() {
        return PCAP_COLUMNS;
    }
}
