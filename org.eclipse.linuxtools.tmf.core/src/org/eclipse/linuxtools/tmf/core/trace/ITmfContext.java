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

package org.eclipse.linuxtools.tmf.core.trace;

/**
 * <b><u>ITmfContext</u></b>
 * <p>
 * This is a place-holder for the context objects.
 */
public interface ITmfContext extends Cloneable {

    public long UNKNOWN_RANK = -2L;
    public long INITIAL_RANK = -1L;

    public void dispose();

    public void setLocation(ITmfLocation<?> location);

    @SuppressWarnings("rawtypes")
    public ITmfLocation<? extends Comparable> getLocation();

    public void setRank(long rank);

    public long getRank();

    public void updateRank(int rank);

    public boolean isValidRank();

    public ITmfContext clone();

}
