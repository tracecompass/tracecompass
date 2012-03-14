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
package org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.model;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.ProviderResource;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TargetResource;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TraceResource;
import org.eclipse.rse.ui.view.AbstractSystemRemoteAdapterFactory;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * <b><u>TargetResourceAdapter</u></b>
 * <p>
 * This factory maps requests for an adapter object from a given remote object.
 * </p>
 */
public class TraceAdapterFactory extends AbstractSystemRemoteAdapterFactory implements IAdapterFactory {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private ProviderResourceAdapter providerAdapter = new ProviderResourceAdapter();
    private TargetResourceAdapter targetAdapter = new TargetResourceAdapter();
    private TraceResourceAdapter traceAdapter = new TraceResourceAdapter();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor for TraceAdapterFactory.
     */
    public TraceAdapterFactory() {
        super();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.AbstractSystemRemoteAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        ISystemViewElementAdapter adapter = null;
        if (adaptableObject instanceof ProviderResource) {
            adapter = providerAdapter;
        }
        else if (adaptableObject instanceof TargetResource) {
            adapter = targetAdapter;
        }
        else if (adaptableObject instanceof TraceResource) {
            adapter = traceAdapter;
        }
        // these lines are very important!
        if ((adapter != null) && (adapterType == IPropertySource.class)) {
            adapter.setPropertySourceInput(adaptableObject);
        }
        return adapter;
    }
}
