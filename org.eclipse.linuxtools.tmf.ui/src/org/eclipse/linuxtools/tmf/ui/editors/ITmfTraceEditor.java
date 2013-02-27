/*******************************************************************************
 * Copyright (c) 2010, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * The trace editor interface
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public interface ITmfTraceEditor {

    /**
     * Get the trace to which this editor is assigned
     *
     * @return The trace
     */
    public ITmfTrace getTrace();

    /**
     * Get the bookmark file associated to this trace
     *
     * @return The IFile object pointing to the bookmark file
     */
    public IFile getBookmarksFile();
}
