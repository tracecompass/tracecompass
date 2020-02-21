/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.pcap.core.event;

import org.eclipse.tracecompass.internal.pcap.core.packet.Packet;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;

/**
 * Class that represents a TMF Pcap Event Field. It is identical to a
 * TmfEventField, except that it overrides the toString() method.
 *
 * @author Vincent Perot
 */
public class PcapEventField extends TmfEventField {

    private final String fSummaryString;

    /**
     * Full constructor
     *
     * @param name
     *            The event field id.
     * @param value
     *            The event field value.
     * @param fields
     *            The list of subfields.
     * @param packet
     *            The packet from which to take the fields from.
     * @throws IllegalArgumentException
     *             If 'name' is null, or if 'fields' has duplicate field names.
     */
    public PcapEventField(String name, Object value, ITmfEventField[] fields, Packet packet) {
        super(name, value, fields);
        fSummaryString = packet.getLocalSummaryString();
    }

    /**
     * Copy constructor
     *
     * @param field
     *            the other event field
     */
    public PcapEventField(final PcapEventField field) {
        super(field);
        fSummaryString = field.fSummaryString;
    }

    @Override
    public String toString() {
        return fSummaryString;
    }
}
