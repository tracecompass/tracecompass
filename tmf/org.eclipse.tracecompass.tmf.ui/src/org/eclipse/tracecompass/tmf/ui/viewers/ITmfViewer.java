/*******************************************************************************
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
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers;

import org.eclipse.swt.widgets.Control;
import org.eclipse.tracecompass.tmf.core.component.ITmfComponent;

/**
 * Interface to viewers.
 *
 * Viewers are to be put into views which need to know how to refresh the
 * viewer's contents.
 *
 * @author Mathieu Denis
 */
public interface ITmfViewer extends ITmfComponent {

    /**
     * Returns the primary control associated with this viewer.
     *
     * @return the SWT control which displays this viewer's contents
     */
    Control getControl();

    /**
     * Tells the viewer to refresh its contents.
     */
    void refresh();
}
