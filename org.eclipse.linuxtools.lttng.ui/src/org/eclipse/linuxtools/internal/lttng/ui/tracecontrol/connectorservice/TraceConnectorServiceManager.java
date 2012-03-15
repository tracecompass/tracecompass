/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Polytechnique Montréal - Initial API and implementation
 *   Bernd Hufmann - Productification, enhancements and fixes
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.connectorservice;

import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.subsystems.ITCFSubSystem;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.subsystems.ITraceSubSystem;
import org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * <b><u>TraceConnectorServiceManager</u></b>
 * <p>
 * Implementation of the Trace Connector Service Manager class for the creation 
 * of the Trace Connector service.
 * </p>
 */
public class TraceConnectorServiceManager extends AbstractConnectorServiceManager {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private static TraceConnectorServiceManager INSTANCE = new TraceConnectorServiceManager();

    public static final int TCF_PORT = 1534;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Return singleton instance
     */
    public static TraceConnectorServiceManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TraceConnectorServiceManager();
        }
        return INSTANCE;
    }


    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager#createConnectorService(org.eclipse.rse.core.model.IHost)
     */
    @Override
    public IConnectorService createConnectorService(IHost host) {
        return new TraceConnectorService(host, TCF_PORT);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager#sharesSystem(org.eclipse.rse.core.subsystems.ISubSystem)
     */
    @Override
    public boolean sharesSystem(ISubSystem otherSubSystem) {
        return (otherSubSystem instanceof ITCFSubSystem);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager#getSubSystemCommonInterface(org.eclipse.rse.core.subsystems.ISubSystem)
     */
    @Override
    public Class<ITraceSubSystem> getSubSystemCommonInterface(ISubSystem subsystem) {
        return ITraceSubSystem.class;
    }

}
