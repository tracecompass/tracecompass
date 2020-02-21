/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.core.event.lookup;


/**
 * Interface for events to implement to provide information for model element lookup.
 *
 * @author Bernd Hufmann
 */
public interface ITmfModelLookup {
    /**
     * Returns a model URI string.
     *
     * @return a model URI string.
     */
    public String getModelUri();
}
