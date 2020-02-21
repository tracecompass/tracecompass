/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model;

/**
 * Constants used in the remote import model (XML attribute and element names, etc).
 *
 * @author Marc-Andre Laperle
 * @noimplement
 */
public interface RemoteImportProfileConstants {

    /**
     * The current version constant.
     */
    public static final String VERSION = "0.1"; //$NON-NLS-1$

    /**
     * Element representing the root of the profiles.
     */
    public static final String PROFILES_ELEMENT = "profiles"; //$NON-NLS-1$

    /**
     * Element representing the version the profiles file.
     */
    public static final String VERSION_ELEMENT = "version"; //$NON-NLS-1$

    /**
     * Element representing the root of a profile.
     */
    public static final String PROFILE_ELEMENT = "profile"; //$NON-NLS-1$

    /**
     * Attribute representing the name of a profile.
     */
    public static final String PROFILE_NAME_ATTRIB = "name"; //$NON-NLS-1$

    /**
     * Element representing the a group of traces.
     */
    public static final String TRACE_GROUP_ELEMENT = "traceGroup"; //$NON-NLS-1$

    /**
     * Attribute representing the root path of a trace group.
     */
    public static final String TRACE_GROUP_ROOT_ATTRIB = "root"; //$NON-NLS-1$

    /**
     * Attribute representing whether or not the group of traces should be
     * imported recursively.
     */
    public static final String TRACE_GROUP_RECURSIVE_ATTRIB = "recursive"; //$NON-NLS-1$

    /**
     * Element representing the connection node.
     */
    public static final String NODE_ELEMENT = "node"; //$NON-NLS-1$

    /**
     * Attribute representing the name of the connection node.
     */
    public static final String NODE_NAME_ATTRIB = "name"; //$NON-NLS-1$

    /**
     * Element representing the URI of the connection node.
     */
    public static final String NODE_URI_ELEMENT = "uri"; //$NON-NLS-1$
}
