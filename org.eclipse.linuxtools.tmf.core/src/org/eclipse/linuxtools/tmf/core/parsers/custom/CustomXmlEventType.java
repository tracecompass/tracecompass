/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.parsers.custom;

/**
 * Event type class for custom XML traces.
 *
 * @author Patrick Tassé
 * @since 3.0
 */
public class CustomXmlEventType extends CustomEventType {

    /**
     * Constructor
     *
     * @param definition
     *            Trace definition
     */
	public CustomXmlEventType(CustomXmlTraceDefinition definition) {
		super(definition);
	}

}
