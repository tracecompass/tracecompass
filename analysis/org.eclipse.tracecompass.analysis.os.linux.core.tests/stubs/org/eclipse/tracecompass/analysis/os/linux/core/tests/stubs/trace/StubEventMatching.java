/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.trace;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.matching.IEventMatchingKey;
import org.eclipse.tracecompass.tmf.core.event.matching.ITmfMatchEventDefinition;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventMatching.Direction;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Event matching implementation for XML kernel stub traces
 *
 * @author Geneviève Bastien
 */
public class StubEventMatching implements ITmfMatchEventDefinition {

    /**
     * The event key for this simple message ID matching field
     */
    protected static class StubEventKey implements IEventMatchingKey {

        private final int fMsgId;

        /**
         * Constructor
         *
         * @param msgId
         *            A message ID
         */
        public StubEventKey(int msgId) {
            fMsgId = msgId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(fMsgId);
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (o instanceof StubEventKey) {
                StubEventKey key = (StubEventKey) o;
                return key.fMsgId == fMsgId;
            }
            return false;
        }
    }

    @Override
    public @Nullable IEventMatchingKey getEventKey(@Nullable ITmfEvent event) {
        if (event == null) {
            return null;
        }
        Integer fieldValue = event.getContent().getFieldValue(Integer.class, "msgid");
        if (fieldValue == null) {
            return null;
        }
        return new StubEventKey(fieldValue);
    }

    @Override
    public boolean canMatchTrace(@Nullable ITmfTrace trace) {
        return (trace instanceof TmfXmlKernelTraceStub);
    }

    @Override
    public @Nullable Direction getDirection(@Nullable ITmfEvent event) {
        if (event == null) {
            return null;
        }
        String evname = event.getName();
        /* Is the event a tcp socket in or out event */
        if (KernelEventLayoutStub.getInstance().eventsNetworkReceive().contains(evname)) {
            return Direction.EFFECT;
        } else if (KernelEventLayoutStub.getInstance().eventsNetworkSend().contains(evname)) {
            return Direction.CAUSE;
        }
        return null;
    }

}
