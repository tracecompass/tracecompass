/*******************************************************************************
 * Copyright (c) 2008, 2016 Ketan Padegaonkar and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ketan Padegaonkar - initial API and implementation
 *     Marc-Andre Laperle - Adapted to Trace Compass from SWTBot's TimeoutException
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.tests.shared;

/**
 * A condition meant to be tested periodically. If it fails after a certain
 * timeout, a failure message is provided.
 */
public interface IWaitCondition {
    /**
     * Tests if the condition has been met.
     *
     * @return <code>true</code> if the condition is satisfied, <code>false</code> otherwise.
     * @throws Exception if the test encounters an error while processing the check.
     */
    boolean test() throws Exception;

    /**
     * Gets the failure message when a test fails (returns <code>false</code>).
     *
     * @return the failure message to show in case the test fails.
     */
    String getFailureMessage();
}
