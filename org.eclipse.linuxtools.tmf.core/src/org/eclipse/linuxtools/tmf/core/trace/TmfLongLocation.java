/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;


/**
 * <b><u>TmfLongLocation</u></b>
 * <p>
 * Implement me. Please.
 * <p>
 * @since 2.0
 */
public class TmfLongLocation extends TmfLocation<Long> {

    public TmfLongLocation(Long location) {
        super(location);
    }

    public TmfLongLocation(TmfLongLocation location) {
        super(location.getLocationData());
    }

    @Override
    public TmfLongLocation clone() {
        return new TmfLongLocation(getLocationData());
    }
}
