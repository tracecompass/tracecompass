/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.loader;

import junit.extensions.TestSetup;
import junit.framework.Test;

/**
 *  Test setup class for one-time setUp() and tearDown() across test cases. 
 */
public class Uml2SDTestSetup extends TestSetup {

    public Uml2SDTestSetup(Test test) {
        super(test);
    }

    /*
     * (non-Javadoc)
     * @see junit.extensions.TestSetup#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        Uml2SDTestFacility.getInstance().init();
    }

    /*
     * (non-Javadoc)
     * @see junit.extensions.TestSetup#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        Uml2SDTestFacility.getInstance().dispose();
    }

}
