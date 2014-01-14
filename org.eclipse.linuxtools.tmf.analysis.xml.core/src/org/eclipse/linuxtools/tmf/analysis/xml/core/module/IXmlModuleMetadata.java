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

package org.eclipse.linuxtools.tmf.analysis.xml.core.module;

/**
 * Interface that XML analysis modules may implement to interpret the extra
 * metadata they may get for example from the header element in the XML file.
 *
 * @author Geneviève Bastien
 */
public interface IXmlModuleMetadata {

    /**
     * Set the header information node associated with this module
     *
     * @param headInfo
     *            The header information
     */
    void setHeadInfo(XmlHeadInfo headInfo);

}
