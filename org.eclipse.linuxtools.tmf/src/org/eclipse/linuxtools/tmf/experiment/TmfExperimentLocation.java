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

package org.eclipse.linuxtools.tmf.experiment;

import org.eclipse.linuxtools.tmf.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.trace.TmfLocation;

/**
 * <b><u>TmfExperimentLocation</u></b>
 * <p>
 * The experiment location is the set of its traces' locations.
 */
public class TmfExperimentLocation extends TmfLocation<ITmfLocation<?>[]> {

    private long[] fRanks;

	public TmfExperimentLocation(ITmfLocation<?>[] locations, long[] ranks) {
		super(locations);
		fRanks = ranks;
	}

	@Override
	public TmfExperimentLocation clone() {
		super.clone();	// To keep FindBugs happy
		ITmfLocation<?>[] locations = (ITmfLocation<?>[]) getLocation();
		ITmfLocation<?>[] clones = new ITmfLocation[locations.length];
		for (int i = 0; i < locations.length; i++) {
			clones[i] = locations[i].clone();
		}
		return new TmfExperimentLocation(clones, fRanks.clone());
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("[TmfExperimentLocation");
		ITmfLocation<?>[] locations = (ITmfLocation<?>[]) getLocation();
		for (int i = 0; i < locations.length; i++) {
			result.append("[" + locations[i].toString() + "," + fRanks[i] + "]");
		}
		result.append("]");
		return result.toString();
	}

	public long[] getRanks() {
        return fRanks;
    }
}
