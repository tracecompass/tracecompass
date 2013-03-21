/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.event.lookup;


/**
 * Interface for events to implement to provide information for source lookup.
 *
 * @author Bernd Hufmann
 * @since 2.0
 */
public interface ITmfSourceLookup {
    /**
     * Returns a call site instance.
     *
     * @return a call site instance.
     */
    public ITmfCallsite getCallsite();
}
