/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.parsers.custom;

/**
 * Event type for custom text traces.
 *
 * @author Patrick Tassé
 */
public class CustomTxtEventType extends CustomEventType {

    /**
     * Constructor
     *
     * @param definition
     *            Custom text trace definition
     */
    public CustomTxtEventType(CustomTxtTraceDefinition definition) {
        super(definition);
    }

}
