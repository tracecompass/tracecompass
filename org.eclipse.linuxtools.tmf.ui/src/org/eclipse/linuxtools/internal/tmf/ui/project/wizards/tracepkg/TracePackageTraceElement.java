/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg;

import org.eclipse.linuxtools.tmf.ui.project.model.TmfNavigatorLabelProvider;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.swt.graphics.Image;

/**
 * An ExportTraceElement associated to a TmfTraceElement. This will be the
 * parent of other elements (events, supplementary files, bookmarks, etc).
 *
 * @author Marc-Andre Laperle
 */
public class TracePackageTraceElement extends TracePackageElement {

    private final TmfTraceElement fTraceElement;
    private final String fImportName;
    private final String fTraceType;

    /**
     * Construct an instance associated to a TmfTraceElement. For exporting.
     *
     * @param parent
     *            the parent of this element, can be set to null
     * @param traceElement
     *            the associated TmfTraceElement
     */
    public TracePackageTraceElement(TracePackageElement parent, TmfTraceElement traceElement) {
        super(parent);
        fTraceElement = traceElement;
        fImportName = null;
        fTraceType = null;
    }

    /**
     * Construct an instance associated to a TmfTraceElement. For importing.
     *
     * @param parent
     *            the parent of this element, can be set to null
     * @param importName
     *            the name to use to identify this trace
     * @param traceType
     *            the trace type to set for this trace
     */
    public TracePackageTraceElement(TracePackageElement parent, String importName, String traceType) {
        super(parent);
        fImportName = importName;
        fTraceElement = null;
        fTraceType = traceType;
    }

    @Override
    public String getText() {
        return fTraceElement != null ? fTraceElement.getName() : fImportName;
    }

    /**
     * @return the associated TmfTraceElement
     */
    public TmfTraceElement getTraceElement() {
        return fTraceElement;
    }

    /**
     * @return the trace type of this trace
     */
    public String getTraceType() {
        return fTraceType;
    }

    @Override
    public Image getImage() {
        TmfNavigatorLabelProvider tmfNavigatorLabelProvider = new TmfNavigatorLabelProvider();
        return tmfNavigatorLabelProvider.getImage(fTraceElement);
    }
}
