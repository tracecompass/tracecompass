/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module;

/**
 * Interface implemented by all classes representing XML top-level elements, for
 * example state providers and views
 *
 * @author Geneviève Bastien
 */
public interface ITmfXmlTopLevelElement {

    /**
     * Get the requested value for an attribute. If the value is a pre-defined
     * value, we return the string corresponding in the defined values map in
     * the top-level XML element.
     *
     * @param name
     *            the string to get
     * @return the actual string value
     */
    String getAttributeValue(String name);

}
