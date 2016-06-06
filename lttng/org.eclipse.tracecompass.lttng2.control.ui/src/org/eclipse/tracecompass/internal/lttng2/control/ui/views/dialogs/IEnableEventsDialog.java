/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceProviderGroup;

/**
 * <p>
 * Interface for providing information about Kernel or UST events to be enabled.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface IEnableEventsDialog extends IEnableKernelEvents, IEnableUstEvents {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * @return the domain type ({@link TraceDomainType})
     */
    TraceDomainType getDomain();

    /**
     * Sets the trace provider group.
     * @param providerGroup -  a trace provider group
     */
    void setTraceProviderGroup(TraceProviderGroup providerGroup);

    /**
     * Sets the trace domain component.
     * @param domain - a domain of the events (null if not known)
     */
    void setTraceDomainComponent(TraceDomainComponent domain);

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * @return returns the open return value
     */
    int open();
}
