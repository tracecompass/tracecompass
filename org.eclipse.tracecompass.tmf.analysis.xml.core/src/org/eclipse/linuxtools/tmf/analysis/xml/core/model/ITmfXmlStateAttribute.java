/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.model;

import org.eclipse.linuxtools.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;

/**
 * Interface that describe a state attribute defined in an XML element
 *
 * @author Geneviève Bastien
 */
public interface ITmfXmlStateAttribute {

    /**
     * This method gets the quark for this state attribute in the State System.
     *
     * Unless this attribute is a location, in which case the quark must exist,
     * the quark will be added to the state system if the state system is in
     * builder mode.
     *
     * @param startQuark
     *            root quark, use {@link IXmlStateSystemContainer#ROOT_QUARK} to search
     *            the full attribute tree
     * @return the quark described by attribute or
     *         {@link IXmlStateSystemContainer#ERROR_QUARK} if quark cannot be found
     */
    int getAttributeQuark(int startQuark);

    /**
     * This method gets the quark for this state attribute in the State System.
     *
     * Unless this attribute is a location, in which case the quark must exist,
     * the quark will be added to the state system if the state system is in
     * builder mode.
     *
     * @param event
     *            The current event being handled
     * @param startQuark
     *            root quark, use {@link IXmlStateSystemContainer#ROOT_QUARK} to search
     *            the full attribute tree
     * @return the quark described by attribute or
     *         {@link IXmlStateSystemContainer#ERROR_QUARK} if quark cannot be found
     */
    int getAttributeQuark(ITmfEvent event, int startQuark);
}
