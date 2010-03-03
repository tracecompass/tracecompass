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

/**
 * <b><u>TmfExperimentLocation</u></b>
 * <p>
 * The experiment location is the set of its traces' locations.
 */
public class TmfExperimentLocation implements ITmfLocation {

	private ITmfLocation[] fLocations;
	
	public TmfExperimentLocation(ITmfLocation[] locations) {
		fLocations = locations;
	}

	public ITmfLocation[] getLocations() {
		return fLocations;
	}

	public TmfExperimentLocation clone() {
		ITmfLocation[] locations = new ITmfLocation[fLocations.length];
		for (int i = 0; i < fLocations.length; i++) {
			locations[i] = fLocations[i].clone();
		}
		return new TmfExperimentLocation(locations);
	}

}
