/*******************************************************************************
 * Copyright (c) 2016 EfficiOS inc, Michael Jeanson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.tests.format;

import java.text.Format;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.format.DecimalUnitFormat;
import org.junit.Test;

/**
 * Test the {@link DecimalUnitFormat} class
 *
 * @author Michael Jeanson
 */
public class DecimalUnitFormatErrorTest {

    private static final @NonNull Format FORMATTER = new DecimalUnitFormat();

    /**
     * Test an illegal argument
     */
    @Test(expected = IllegalArgumentException.class)
    public void testException() {
        FORMATTER.format(new String("Toto"));
    }
}
