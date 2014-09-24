/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.shared;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

/**
 * Test suite that adds a {@link DebugListener} to unit tests, to help debug
 * misbehaving tests.
 *
 * Use with @RunWith(DebugSuite) and DebugSuite.SuiteClasses({ })
 *
 * @author Alexandre Montplaisir
 */
public class DebugSuite extends Suite {

    /**
     * Constructor (required by JUnit)
     *
     * @param klass
     *            Root of the suite
     * @throws InitializationError
     *             If an error happened when getting the test classes
     */
    public DebugSuite(Class<?> klass) throws InitializationError {
        super(klass, getAnnotatedClasses(klass));
    }

    @Override
    public void run(RunNotifier runNotifier) {
        runNotifier.addListener(new DebugListener());
        super.run(runNotifier);
    }

    private static Class<?>[] getAnnotatedClasses(Class<?> klass) throws InitializationError {
        SuiteClasses annotation = klass.getAnnotation(SuiteClasses.class);
        if (annotation == null) {
            throw new InitializationError(String.format("class '%s' must have a SuiteClasses annotation", klass.getName()));
        }
        return annotation.value();
    }
}
