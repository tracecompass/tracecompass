/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.dialogs;

import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceProviderGroup;

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
     * @return the session the events shall be enabled.
     */
    boolean isKernel();

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
