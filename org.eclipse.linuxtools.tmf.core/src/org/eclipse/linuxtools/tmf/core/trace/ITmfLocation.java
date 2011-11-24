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
 * <b><u>ITmfLocation</u></b>
 * <p>
 * This is a place-holder for the location objects.
 */
public interface ITmfLocation<L extends Comparable<?>> extends Cloneable {

	public void setLocation(L location);

	public L getLocation();

	public ITmfLocation<L> clone();

}
