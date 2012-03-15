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
package org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.subsystems;

import java.util.List;
import java.util.Vector;

import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.Messages;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.connectorservice.TraceConnectorServiceManager;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;

/**
 * <b><u>TraceSubSystemConfiguration</u></b>
 * <p>
 * Implementation of the subsystem configuration to define the trace subsystem configuration.
 * </p>
 */
public class TraceSubSystemConfiguration extends SubSystemConfiguration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor for TraceSubSystemConfiguration.
     */
    public TraceSubSystemConfiguration() {
        super();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#createSubSystemInternal(org.eclipse.rse.core.model.IHost)
     */
    @Override
    public ISubSystem createSubSystemInternal(IHost conn) {
        return new TraceSubSystem(conn, getConnectorService(conn));
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#getConnectorService(org.eclipse.rse.core.model.IHost)
     */
    @Override
    public IConnectorService getConnectorService(IHost host) {
        return TraceConnectorServiceManager.getInstance().getConnectorService(host, ITCFSubSystem.class);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#createDefaultFilterPool(org.eclipse.rse.core.filters.ISystemFilterPoolManager)
     */
    @Override
    protected ISystemFilterPool createDefaultFilterPool(ISystemFilterPoolManager mgr) {
        ISystemFilterPool defaultPool = null;
        try {
            defaultPool = mgr.createSystemFilterPool(getDefaultFilterPoolName(mgr.getName(), getId()), false); // true=>is deletable by user
            defaultPool.setDeletable(false);
            defaultPool.setNonRenamable(true);
            List<String> strings = new Vector<String>();
            strings.add("*"); //$NON-NLS-1$
            ISystemFilter filter = mgr.createSystemFilter(defaultPool, Messages.AllProviders, strings);
            filter.setNonChangable(false);
            filter.setSingleFilterStringOnly(false);
            filter.setNonDeletable(true);
            filter.setNonRenamable(true);
        } catch (Exception exc) {
        }
        return defaultPool;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#getTranslatedFilterTypeProperty(org.eclipse.rse.core.filters.ISystemFilter)
     *
     * Intercept of parent method so we can supply our own value shown in the property sheet for the "type" property when a filter is selected within our subsystem.
     * 
     * Requires this line in LTTngServicesResources.properties: property.type.providerfilter=Lttng_Resource_Provider filter
     */
    @Override
    public String getTranslatedFilterTypeProperty(ISystemFilter selectedFilter) {
        return Messages.Property_Type_Provider_Filter;
    }

    public boolean supportsUserId() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsServerLaunchProperties(org.eclipse.rse.core.model.IHost)
     */
    @Override
    public boolean supportsServerLaunchProperties(IHost host) {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsFilters()
     */
    @Override
    public boolean supportsFilters() {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsFilterChildren()
     */
    @Override
    public boolean supportsFilterChildren() {
        return true;
    }
}
