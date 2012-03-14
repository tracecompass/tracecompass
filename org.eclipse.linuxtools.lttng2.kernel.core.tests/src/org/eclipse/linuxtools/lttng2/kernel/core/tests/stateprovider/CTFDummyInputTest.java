/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.tests.stateprovider;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.CTFDummyInput;
import org.junit.BeforeClass;

/**
 * Test the dummy CTF input plugin
 * 
 * @author alexmont
 *
 */
public class CTFDummyInputTest extends CTFKernelStateInputTest {

    /* Hiding superclass method */
    @BeforeClass
    public static void initialize() {
        input = new CTFDummyInput(CTFTestFiles.getTestTrace());
    }

}
