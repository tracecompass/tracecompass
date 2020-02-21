/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.common.core.tests;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * <b><u>ActivatorTest</u></b>
 * <p>
 * Test suite for the Activator class
 * <p>
 */
public class ActivatorTest {

    /**
     * Test method for {@link Activator#getDefault()}.
     */
    @Test
    public void testGetDefault() {
        Activator activator = Activator.getDefault();
        assertNotNull(activator);
    }
}
