/*******************************************************************************
 * Copyright (c) 2011-2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Ansgar Radermacher - support for model URI
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.ctfadaptor;

/**
 * Set of constants defining common custom attribute names (key within hash table)
 *
 * @since 2.0
 */
public abstract class CtfCustomAttributes {
    /**
     * Model URI for traces related to EMF models
     */
    public final static String MODEL_URI_KEY = "model.emf.uri"; //$NON-NLS-1$
}
