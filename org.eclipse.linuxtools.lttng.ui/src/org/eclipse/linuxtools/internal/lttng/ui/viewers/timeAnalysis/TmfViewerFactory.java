/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng.ui.viewers.timeAnalysis;

import org.eclipse.swt.widgets.Composite;

/**
 * <b><u>TmfWidgetFactory</u></b>
 * <p>
 *
 * TODO: Generalize when extension points are introduced
 * TODO: Today, it is specific for the TimeAnalysis widget
 */
public class TmfViewerFactory {

//    public static ITmfWidget createWidget(String id, Composite parent) {
//        return null;
//    }
    
    public static ITimeAnalysisViewer createViewer(Composite parent,
            TmfTimeAnalysisProvider provider) {
        return new TmfTimeAnalysisViewer(parent, provider);
    }

}
