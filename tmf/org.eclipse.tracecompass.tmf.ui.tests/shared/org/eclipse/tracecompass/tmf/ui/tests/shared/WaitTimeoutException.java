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
 * This is an exception that is thrown when a timeout occurs waiting for something (e.g. a condition) to complete.
 */
public class WaitTimeoutException extends RuntimeException {

    private static final long serialVersionUID = -2673174817824776871L;

    /**
	 * Constructs the exception with the given message.
	 *
	 * @param message the message.
	 */
	public WaitTimeoutException(String message) {
		super(message);
	}
}
