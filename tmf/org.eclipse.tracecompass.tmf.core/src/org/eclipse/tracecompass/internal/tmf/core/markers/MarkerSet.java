/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.markers;

import java.util.ArrayList;
import java.util.List;

/**
 * Model element for configurable marker set.
 */
public class MarkerSet {
    private final String fName;
    private final String fId;
    private final List<Marker> fMarkers;

    /**
     * Constructor
     *
     * @param name the name
     * @param id the id
     */
    public MarkerSet(String name, String id) {
        super();
        fName = name;
        fId = id;
        fMarkers = new ArrayList<>();
    }

    /**
     * @return the name
     */
    public String getName() {
        return fName;
    }

    /**
     * @return the id
     */
    public String getId() {
        return fId;
    }

    /**
     * @return the markers
     */
    public List<Marker> getMarkers() {
        return fMarkers;
    }

    /**
     * Add a marker.
     *
     * @param marker the marker
     */
    public void addMarker(Marker marker) {
        fMarkers.add(marker);
    }
}
