/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.btf.core.tests;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Stub to do nothing
 *
 * @author Matthew Khouzam
 */
public class BtfTestPlugin implements BundleActivator {

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.btf.core.tests";

    private static BundleContext fContext;

    /**
     * Gets the bundle of this plug-in.
     *
     * @return the oel.btf.core.tests bundle
     */
    public static Bundle getBundle() {
        if (fContext == null) {
            return null;
        }
        return fContext.getBundle();
    }

    @Override
    public void start(BundleContext context) throws Exception {
        fContext = context;
    }

    @Override
    public void stop(BundleContext context) throws Exception {

    }

}
