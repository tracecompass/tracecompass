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
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.btf.core.trace;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.util.Map;

import org.eclipse.tracecompass.internal.btf.core.Messages;
import org.eclipse.tracecompass.btf.core.event.BtfEventType;

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
        builder.put("STI", new BtfEventType(nullToEmptyString(Messages.BtfTypeId_STIName), Messages.BtfTypeId_STIDescr)); //$NON-NLS-1$
        // Software
        builder.put("T", new BtfEventType(nullToEmptyString(Messages.BtfTypeId_TName), Messages.BtfTypeId_TDescr)); //$NON-NLS-1$
        builder.put("ISR", new BtfEventType(nullToEmptyString(Messages.BtfTypeId_ISRName), Messages.BtfTypeId_ISRDescr)); //$NON-NLS-1$
        builder.put("R", new BtfEventType(nullToEmptyString(Messages.BtfTypeId_RName), Messages.BtfTypeId_RDescr)); //$NON-NLS-1$
        builder.put("IB", new BtfEventType(nullToEmptyString(Messages.BtfTypeId_IBName), Messages.BtfTypeId_IBDescr)); //$NON-NLS-1$
        builder.put("I", new BtfEventType(nullToEmptyString(Messages.BtfTypeId_IName), Messages.BtfTypeId_IDescr)); //$NON-NLS-1$
        // Hardware
        builder.put("ECU", new BtfEventType(nullToEmptyString(Messages.BtfTypeId_ECUName), Messages.BtfTypeId_ECUDescr)); //$NON-NLS-1$
        builder.put("P", new BtfEventType(nullToEmptyString(Messages.BtfTypeId_PName), Messages.BtfTypeId_PDescr)); //$NON-NLS-1$
        builder.put("C", new BtfEventType(nullToEmptyString(Messages.BtfTypeId_CName), Messages.BtfTypeId_CDescr)); //$NON-NLS-1$
        // Operating system
        builder.put("SCHED", new BtfEventType(nullToEmptyString(Messages.BtfTypeId_SCHEDName), Messages.BtfTypeId_SCHEDDescr)); //$NON-NLS-1$
        builder.put("SIG", new BtfEventType(nullToEmptyString(Messages.BtfTypeId_SIGName), Messages.BtfTypeId_SIGDescr)); //$NON-NLS-1$
        builder.put("SEM", new BtfEventType(nullToEmptyString(Messages.BtfTypeId_SEMName), Messages.BtfTypeId_SEMDescr)); //$NON-NLS-1$
        // Information
        builder.put("SIM", new BtfEventType(nullToEmptyString(Messages.BtfTypeId_SIMName), Messages.BtfTypeId_SIMDescr)); //$NON-NLS-1$
        builder.put("SYS", new BtfEventType(nullToEmptyString(Messages.BtfTypeId_SYSName), Messages.BtfTypeId_SYSDescr)); //$NON-NLS-1$
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
