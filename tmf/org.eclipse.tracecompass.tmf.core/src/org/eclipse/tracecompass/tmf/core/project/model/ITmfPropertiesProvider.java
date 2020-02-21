/*******************************************************************************
 * Copyright (c) 2013, 2016 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.project.model;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for classes to implement when they can provide additional
 * properties.
 *
 * This information will be displayed in the Properties View, among other
 * things.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
@NonNullByDefault
public interface ITmfPropertiesProvider {

    /**
     * Get the properties related to this class.
     *
     * @return The map of properties, <name, value>
     */
    public Map<String, String> getProperties();
}
