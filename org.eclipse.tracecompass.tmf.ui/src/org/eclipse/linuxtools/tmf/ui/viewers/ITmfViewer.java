/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers;

import org.eclipse.linuxtools.tmf.core.component.ITmfComponent;
import org.eclipse.swt.widgets.Control;

/**
 * Interface to viewers.
 *
 * Viewers are to be put into views which need to know how to refresh the
 * viewer's contents.
 *
 * @author Mathieu Denis
 * @version 2.0
 * @since 2.0
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
