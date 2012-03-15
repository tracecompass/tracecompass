/*******************************************************************************
 * Copyright (c) 2009,2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.core.control;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.internal.lttng.core.event.LttngSyntheticEvent;
import org.eclipse.linuxtools.internal.lttng.core.model.LTTngTreeNode;

/**
 * @author alvaro
 *
 */
public class LttngCoreProviderFactory {

    // List of provider IDs
    public static final int STATISTICS_LTTNG_SYTH_EVENT_PROVIDER = 0;
    public static final int CONTROL_FLOW_LTTNG_SYTH_EVENT_PROVIDER = 1;
    public static final int RESOURCE_LTTNG_SYTH_EVENT_PROVIDER = 2;
    
    private static final Map<Integer, LttngSyntheticEventProvider> fSyntheticEventProviders = new HashMap<Integer, LttngSyntheticEventProvider>();
    
    /**
     * Pre-creates all known LTTng providers.
     */
    public static void initialize() {
        getEventProvider(STATISTICS_LTTNG_SYTH_EVENT_PROVIDER);
        getEventProvider(CONTROL_FLOW_LTTNG_SYTH_EVENT_PROVIDER);
        getEventProvider(RESOURCE_LTTNG_SYTH_EVENT_PROVIDER);
    }
    
    /**
     * Gets a SyntheticEventProvider for the given ID. It creates a new provider
     * if necessary
     *  
     * @param providerId
     * @return
     */
    public static LttngSyntheticEventProvider getEventProvider(int providerId) {
        if (!fSyntheticEventProviders.containsKey(Integer.valueOf(providerId))) {
            LttngSyntheticEventProvider synEventProvider = new LttngSyntheticEventProvider(LttngSyntheticEvent.class);
            fSyntheticEventProviders.put(Integer.valueOf(providerId), synEventProvider);
        }
        return fSyntheticEventProviders.get(Integer.valueOf(providerId));
    }

    /**
     * Resets all LTTngSytheticEventProviders associated with the given
     * Experiment
     * 
     * @param experimentNode
     */
    public static void reset(LTTngTreeNode experimentNode) {
        for (LttngSyntheticEventProvider provider : fSyntheticEventProviders.values()) {
            provider.reset(experimentNode);
        }
    }
}
