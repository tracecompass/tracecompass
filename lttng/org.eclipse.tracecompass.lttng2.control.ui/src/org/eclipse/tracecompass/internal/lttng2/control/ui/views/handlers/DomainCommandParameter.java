/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.handlers;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;

/**
 * Class containing parameter for the command execution.
 *
 *  @author Bernd Hufmann
 */
@NonNullByDefault
public class DomainCommandParameter extends CommandParameter {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private TraceDomainComponent fDomain;


    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param session - a trace session component.
     * @param domain - a trace domain component
     */
    public DomainCommandParameter(TraceSessionComponent session, TraceDomainComponent domain) {
        super(session);
        fDomain = domain;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the trace domain component
     */
    public TraceDomainComponent getDomain() {
        return fDomain;
    }

    // ------------------------------------------------------------------------
    // Cloneable interface
    // ------------------------------------------------------------------------
    @Override
    public DomainCommandParameter clone() {
        DomainCommandParameter clone = (DomainCommandParameter) super.clone();
        clone.fDomain = fDomain;
        return clone;
    }

}