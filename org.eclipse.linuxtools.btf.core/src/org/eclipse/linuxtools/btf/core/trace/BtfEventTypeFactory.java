/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.btf.core.trace;

import java.util.Map;

import org.eclipse.linuxtools.btf.core.Messages;
import org.eclipse.linuxtools.btf.core.event.BtfEventType;

import com.google.common.collect.ImmutableMap;

/**
 * Entity Types for BTF
 *
 * @author Matthew Khouzam
 */
public final class BtfEventTypeFactory {

    private BtfEventTypeFactory() {}

    private static final Map<String, BtfEventType> TYPES;

    static {
        ImmutableMap.Builder<String, BtfEventType> builder = new ImmutableMap.Builder<>();
        // Environment
        builder.put("STI", new BtfEventType(Messages.BtfTypeId_STIName, Messages.BtfTypeId_STIDescr)); //$NON-NLS-1$
        // Software
        builder.put("T", new BtfEventType(Messages.BtfTypeId_TName, Messages.BtfTypeId_TDescr)); //$NON-NLS-1$
        builder.put("ISR", new BtfEventType(Messages.BtfTypeId_ISRName, Messages.BtfTypeId_ISRDescr)); //$NON-NLS-1$
        builder.put("R", new BtfEventType(Messages.BtfTypeId_RName, Messages.BtfTypeId_RDescr)); //$NON-NLS-1$
        builder.put("IB", new BtfEventType(Messages.BtfTypeId_IBName, Messages.BtfTypeId_IBDescr)); //$NON-NLS-1$
        // Hardware
        builder.put("ECU", new BtfEventType(Messages.BtfTypeId_ECUName, Messages.BtfTypeId_ECUDescr)); //$NON-NLS-1$
        builder.put("P", new BtfEventType(Messages.BtfTypeId_PName, Messages.BtfTypeId_PDescr)); //$NON-NLS-1$
        builder.put("C", new BtfEventType(Messages.BtfTypeId_CName, Messages.BtfTypeId_CDescr)); //$NON-NLS-1$
        // Operating system
        builder.put("SCHED", new BtfEventType(Messages.BtfTypeId_SCHEDName, Messages.BtfTypeId_SCHEDDescr)); //$NON-NLS-1$
        builder.put("SIG", new BtfEventType(Messages.BtfTypeId_SIGName, Messages.BtfTypeId_SIGDescr)); //$NON-NLS-1$
        builder.put("SEM", new BtfEventType(Messages.BtfTypeId_SEMName, Messages.BtfTypeId_SEMDescr)); //$NON-NLS-1$
        // Information
        builder.put("SIM", new BtfEventType(Messages.BtfTypeId_SIMName, Messages.BtfTypeId_SIMDescr)); //$NON-NLS-1$
        TYPES = builder.build();
    }

    /**
     * Parse the string and get a type id
     *
     * @param typeName
     *            the string to parse
     * @return a BTF trace type, can be null if the string is invalid.
     */
    public static BtfEventType parse(String typeName) {
        return TYPES.get(typeName);
    }

}
