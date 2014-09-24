/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
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

    /**
     * Constructor
     * @param test the test to use.
     */
    public Uml2SDTestSetup(Test test) {
        super(test);
    }

    @Override
    protected void setUp() throws Exception {
        Uml2SDTestFacility.getInstance().init();
    }

    @Override
    protected void tearDown() throws Exception {
        Uml2SDTestFacility.getInstance().dispose();
    }

}
