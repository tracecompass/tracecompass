/*******************************************************************************
 * Copyright (c) 2014 Wind River Systems, Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Markus Schorn - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.ui.IStartup;

/**
 * Collection of workarounds needed for dealing with the RSE adapter.
 */
public class Workaround_Bug449362 {

    private static final String RSE_ADAPTER_ID = "org.eclipse.ptp.remote.RSERemoteServices"; //$NON-NLS-1$

    private static boolean fTriggeredRSEStartup = false;

    private Workaround_Bug449362() {
        // utility class
    }

    /**
     * Trigger the startup of RSE, if necessary.
     *
     * @param adapterID
     *            the id of the adapter that will be initialized
     * @return <code>false</code> if the startup cannot be triggered, although
     *         it should be.
     */
    public static boolean triggerRSEStartup(String adapterID) {
        if (fTriggeredRSEStartup || !RSE_ADAPTER_ID.equals(adapterID)) {
            return true;
        }

        IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.ui.startup"); //$NON-NLS-1$
        if (ep == null) {
            return false;
        }
        for (IConfigurationElement elem : ep.getConfigurationElements()) {
            String clazz = elem.getAttribute("class"); //$NON-NLS-1$
            if (clazz != null && clazz.endsWith("RSEUIStartup")) { //$NON-NLS-1$
                try {
                    Object ext = elem.createExecutableExtension("class"); //$NON-NLS-1$
                    if (ext instanceof IStartup) {
                        ((IStartup) ext).earlyStartup();
                        fTriggeredRSEStartup = true;
                        return true;
                    }
                } catch (CoreException e) {
                    Activator.getDefault().logError(e.getMessage(), e);
                }
            }
        }
        return false;
    }

}
