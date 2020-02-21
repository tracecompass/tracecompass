/*******************************************************************************
 * Copyright (c) 2014, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.aspect;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Aspect representing a query in a given state system, at the timestamp of the
 * event.
 *
 * This is a good example of how aspects can be "indirect" with regards to their
 * events.
 *
 * @author Alexandre Montplaisir
 */
public class TmfStateSystemAspect implements ITmfEventAspect<String> {

    private final @Nullable String fName;
    private final ITmfStateSystem fSS;
    private final int fAttribute;

    /**
     * Constructor
     *
     * @param name
     *            The name of this aspect. You can use 'null' to use the
     *            default name, which is the (base) name of the attribute.
     * @param ss
     *            The state system in which we want to query
     * @param attributeQuark
     *            The quark of the attribute in the state system to look for
     */
    public TmfStateSystemAspect(@Nullable String name, ITmfStateSystem ss, int attributeQuark) {
        fName = name;
        fSS = ss;
        fAttribute = attributeQuark;
    }

    @Override
    public String getName() {
        String name = fName;
        if (name != null) {
            return name;
        }

        name = fSS.getFullAttributePath(fAttribute);
        return name;
    }

    @Override
    public @NonNull String getHelpText() {
        return Messages.getMessage(NLS.bind(Messages.AspectHelpText_Statesystem,
                fSS.getSSID(), fSS.getFullAttributePath(fAttribute)));
    }

    @Override
    public @Nullable String resolve(ITmfEvent event) {
        try {
            ITmfStateValue value = fSS.querySingleState(event.getTimestamp().getValue(), fAttribute).getStateValue();
            return checkNotNull(value.toString());
        } catch (StateSystemDisposedException e) {
            return null;
        }
    }
}
