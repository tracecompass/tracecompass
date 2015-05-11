/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
