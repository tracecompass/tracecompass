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

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;

/**
 * <b><u>TmfTimestampLocation</u></b>
 * <p>
 * Implement me. Please.
 * <p>
 * @since 2.0
 */
public class TmfTimestampLocation extends TmfLocation<ITmfTimestamp> {

    public TmfTimestampLocation(ITmfTimestamp location) {
        super(location);
    }

    public TmfTimestampLocation(TmfTimestampLocation location) {
        super( location.getLocationData());
    }

    @Override
    public TmfTimestampLocation clone() {
        return new TmfTimestampLocation(getLocationData());
    }
}
