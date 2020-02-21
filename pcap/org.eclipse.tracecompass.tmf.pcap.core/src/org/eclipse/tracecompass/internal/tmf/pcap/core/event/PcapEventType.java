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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;

/**
 * Class that represents the type of a PcapEvent.
 *
 * @author Vincent Perot
 */
public class PcapEventType extends TmfEventType {

    /**
     * The default Pcap Type ID for a PcapEvent
     */
    public static final String DEFAULT_PCAP_TYPE_ID = NonNullUtils.nullToEmptyString(Messages.PcapEventType_DefaultTypeID);

    /**
     * Default constructor
     */
    public PcapEventType() {
        this(DEFAULT_PCAP_TYPE_ID, null);
    }

    /**
     * Full constructor
     *
     * @param typeId
     *            the type name
     * @param root
     *            the root field
     */
    public PcapEventType(final String typeId, final @Nullable ITmfEventField root) {
        super(typeId, root);
    }

    /**
     * Copy constructor
     *
     * @param type
     *            the other type
     */
    public PcapEventType(final PcapEventType type) {
        super(type);
    }

    @Override
    public @Nullable String toString() {
        return getName();
    }

}
