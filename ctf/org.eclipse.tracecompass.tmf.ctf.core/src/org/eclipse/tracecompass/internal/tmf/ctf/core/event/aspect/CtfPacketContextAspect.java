/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ctf.core.event.aspect;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;

/**
 * Packet context aspect for CTF traces.
 *
 * @author Matthew Khouzam
 */
public class CtfPacketContextAspect implements ITmfEventAspect<Map<@NonNull String, @NonNull Object>> {

    private static ITmfEventAspect<Map<@NonNull String, @NonNull Object>> sInstance = null;

    /**
     * Get the instance
     *
     * @return the instance
     */
    public static @NonNull ITmfEventAspect<Map<@NonNull String, @NonNull Object>> getInstance() {
        ITmfEventAspect<Map<@NonNull String, @NonNull Object>> instance = sInstance;
        if (instance == null) {
            instance = new CtfPacketContextAspect();
            sInstance = instance;
        }
        return instance;
    }

    private CtfPacketContextAspect() {
        // do nothing
    }

    @Override
    public Map<@NonNull String, @NonNull Object> resolve(ITmfEvent event) {
        if (!(event instanceof CtfTmfEvent)) {
            return null;
        }
        Map<@NonNull String, @NonNull Object> packetContext = ((CtfTmfEvent) event).getPacketAttributes();
        if (packetContext.isEmpty()) {
            return null;
        }
        return packetContext;
    }

    @Override
    public @NonNull String getName() {
        return Messages.getMessage(Messages.CtfPacketContextAspect_name);
    }

    @Override
    public @NonNull String getHelpText() {
        return Messages.getMessage(Messages.CtfPacketContextAspect_description);
    }

    @Override
    public boolean isHiddenByDefault() {
        return true;
    }
}
