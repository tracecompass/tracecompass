/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.shared;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Debugging RunListener, which will print to stdout the name of the tests being
 * run.
 *
 * @author Alexandre Montplaisir
 */
public class DebugListener extends RunListener {

    @Override
    public void testStarted(Description description) {
        System.out.println("Running " + getTestName(description));
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        System.out.println("SKIPPING TEST: " + getTestName(failure.getDescription()));
        System.out.println(failure.getMessage());
    }

    /**
     * Get the className#methodName from a Description.
     */
    private static String getTestName(Description description) {
        return description.getClassName() + '#' + description.getMethodName();
    }
}
