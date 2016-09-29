/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ctf.core.event.aspect;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.event.types.ICompositeDefinition;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEventField;

/**
 * Packet header aspect for CTF traces.
 *
 * @author Matthew Khouzam
 */
public class CtfPacketHeaderAspect implements ITmfEventAspect<List<ITmfEventField>> {

    private static final @NonNull String ANON = ""; //$NON-NLS-1$
    private static ITmfEventAspect<List<ITmfEventField>> sInstance = null;

    /**
     * Get the instance
     *
     * @return the instance
     */
    public static @NonNull ITmfEventAspect<List<ITmfEventField>> getInstance() {
        ITmfEventAspect<List<ITmfEventField>> instance = sInstance;
        if (instance == null) {
            instance = new CtfPacketHeaderAspect();
            sInstance = instance;
        }
        return instance;
    }

    private CtfPacketHeaderAspect() {
        // do nothing
    }

    @Override
    public @Nullable List<ITmfEventField> resolve(ITmfEvent event) {
        if (!(event instanceof CtfTmfEvent)) {
            return null;
        }
        ICompositeDefinition packetContext = ((CtfTmfEvent) event).getPacketContext();
        if (packetContext == null) {
            return null;
        }
        Object value = CtfTmfEventField.parseField(packetContext, ANON).getValue();
        if(value instanceof ITmfEventField[]) {
            return  Arrays.asList((ITmfEventField[]) value);
        }
        return null;
    }

    @Override
    public @NonNull String getName() {
        return Messages.getMessage(Messages.CtfPacketHeaderAspect_name);
    }

    @Override
    public @NonNull String getHelpText() {
        return Messages.getMessage(Messages.CtfPacketHeaderAspect_description);
    }

    @Override
    public boolean isHiddenByDefault() {
        return true;
    }
}
