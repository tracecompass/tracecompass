/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.event;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.ICompositeDefinition;

/**
 * Null event definition, used as a poison pill
 *
 * @author Matthew Khouzam
 */
@NonNullByDefault
final class NullEventDefinition implements IEventDefinition {

    public static final NullEventDefinition INSTANCE = new NullEventDefinition();

    private NullEventDefinition() {
    }

    @Override
    public long getTimestamp() {
        return 0;
    }

    @Override
    public @Nullable ICompositeDefinition getPacketContext() {
        return null;
    }

    @Override
    public Map<String, Object> getPacketAttributes() {
        return Collections.emptyMap();
    }

    @Override
    public @Nullable ICompositeDefinition getFields() {
        return null;
    }

    @Override
    public @Nullable ICompositeDefinition getEventHeader() {
        return null;
    }

    @Override
    public @Nullable ICompositeDefinition getEventContext() {
        return null;
    }

    @Override
    public @Nullable IEventDeclaration getDeclaration() {
        return null;
    }

    @Override
    public @Nullable ICompositeDefinition getContext() {
        return null;
    }

    @Override
    public int getCPU() {
        return IEventDefinition.UNKNOWN_CPU;
    }
}