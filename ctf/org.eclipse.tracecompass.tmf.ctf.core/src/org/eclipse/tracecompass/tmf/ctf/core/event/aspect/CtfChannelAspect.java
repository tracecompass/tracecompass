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
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.event.aspect;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;

/**
 * Channel aspect for CTF traces
 *
 * @author Alexandre Montplaisir
 */
public class CtfChannelAspect implements ITmfEventAspect<String> {

    @Override
    public String getName() {
        return Messages.getMessage(Messages.AspectName_Channel);
    }

    @Override
    public String getHelpText() {
        return Messages.getMessage(Messages.AspectHelpText_Channel);
    }

    @Override
    public String resolve(ITmfEvent event) {
        if (!(event instanceof CtfTmfEvent)) {
            return EMPTY_STRING;
        }
        return ((CtfTmfEvent) event).getChannel();
    }
}
