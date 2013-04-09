/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace;

import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;

/**
 * <b><u>TmfStringLocation</u></b>
 * <p>
 * Implement me. Please.
 * <p>
 */
public class TmfStringLocation extends TmfLocation {

    /**
     * @param location the concrete trace location
     */
    public TmfStringLocation(String location) {
        super(location);
    }

    /**
     * @param other the other location
     */
    public TmfStringLocation(TmfStringLocation other) {
        super(other.getLocationInfo());
    }

    @Override
    public String getLocationInfo() {
        return (String) super.getLocationInfo();
    }

}
