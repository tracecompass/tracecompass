/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui;

/**
 * <b><u>LTTngUILogger</u></b>
 * <p>
 * A logging utility 
 */
public class LTTngUILogger {

	public static void logInfo(String message) {
		LTTngUiPlugin.getDefault().getLogger().logInfo(message);
	}

	public static void logWarning(String message) {
		LTTngUiPlugin.getDefault().getLogger().logWarning(message);
	}

	public static void logError(Throwable exception) {
		logError("Unexpected exception", exception); //$NON-NLS-1$
	}

	public static void logError(String message, Throwable exception) {
		LTTngUiPlugin.getDefault().getLogger().logError(message, exception);
	}

}
