/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.experiment;

import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;

/**
 * <b><u>TmfExperimentLocation</u></b>
 * <p>
 * The experiment location is the set of its traces' locations.
 */
public class TmfExperimentLocation extends TmfLocation<TmfLocationArray> {

    private long[] fRanks;

	public TmfExperimentLocation(TmfLocationArray locations, long[] ranks) {
		super(locations);
		fRanks = ranks;
	}

	@Override
	public TmfExperimentLocation clone() {
		super.clone();	// To keep FindBugs happy
		TmfLocationArray array = (TmfLocationArray) getLocation();
		TmfLocationArray clones = array.clone();
		return new TmfExperimentLocation(clones, fRanks.clone());
	}

	@Override
    @SuppressWarnings("nls")
	public String toString() {
		StringBuilder result = new StringBuilder("[TmfExperimentLocation");
		TmfLocationArray array = (TmfLocationArray) getLocation();
		for (int i = 0; i < array.locations.length; i++) {
			result.append("[" + array.locations[i].toString() + "," + fRanks[i] + "]");
		}
		result.append("]");
		return result.toString();
	}

	public long[] getRanks() {
        return fRanks;
    }
}
