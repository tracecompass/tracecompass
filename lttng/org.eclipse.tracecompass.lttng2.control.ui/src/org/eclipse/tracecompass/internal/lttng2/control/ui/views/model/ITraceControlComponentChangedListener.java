/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.model;

/**
 * <p>
 * Listener interface a class can implement to be notified about changes
 * of components
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface ITraceControlComponentChangedListener {
    /**
     * Interface for notifications about the addition of a component.
     * @param parent - the parent where the child was added.
     * @param component - the child that was added.
     */
    void componentAdded(ITraceControlComponent parent, ITraceControlComponent component);

    /**
     * Interface for notifications about the removal of a child.
     * @param parent - the parent where the child was removed.
     * @param component - the child that was removed.
     */
    void componentRemoved(ITraceControlComponent parent, ITraceControlComponent component);
    /**
     * NInterface for notifications about the change of a component.
     * @param component - the component that was changed.
     */
    void componentChanged(ITraceControlComponent component);
}

