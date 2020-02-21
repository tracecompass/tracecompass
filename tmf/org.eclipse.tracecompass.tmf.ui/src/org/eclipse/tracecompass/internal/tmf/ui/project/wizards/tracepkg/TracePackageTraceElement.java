/*******************************************************************************
 * Copyright (c) 2013, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfCommonProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfNavigatorLabelProvider;

/**
 * An TracePackageElement associated to a TmfTraceElement. This will be the
 * parent of other elements (files, supplementary files, bookmarks, etc).
 *
 * @author Marc-Andre Laperle
 */
public class TracePackageTraceElement extends TracePackageElement {

    private final TmfCommonProjectElement fTraceElement;
    private String fImportName;
    private String fTraceType;

    /**
     * Construct an instance associated to a TmfTraceElement. For exporting.
     *
     * @param parent
     *            the parent of this element, can be set to null
     * @param traceElement
     *            the associated TmfTraceElement
     */
    public TracePackageTraceElement(TracePackageElement parent, TmfCommonProjectElement traceElement) {
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
        return fTraceElement != null ? fTraceElement.getElementPath() : getDestinationElementPath();
    }

    /**
     * Return the target TmfCommonProjectElement element path for a given trace
     * package element. {@link TmfCommonProjectElement#getElementPath()}
     *
     * @return the element path
     */
    public String getDestinationElementPath() {
        String traceName = getImportName();
        for (TracePackageElement element : getChildren()) {
            if (element instanceof TracePackageFilesElement) {
                TracePackageFilesElement tracePackageFilesElement = (TracePackageFilesElement) element;
                String fileName = tracePackageFilesElement.getFileName();
                String parentDir = removeLastSegment(fileName);
                return append(parentDir, traceName);
            }
        }

        return traceName;
    }

    /**
     * We do this outside of the Path class because we don't want it to convert
     * \ to / on Windows in the presence of regular expressions
     */
    private static String removeLastSegment(String str) {
        String ret = removeAllTrailing(str, IPath.SEPARATOR);
        int lastIndexOf = ret.lastIndexOf(IPath.SEPARATOR);
        if (lastIndexOf != -1) {
            ret = ret.substring(0, lastIndexOf);
            ret = removeAllTrailing(ret, IPath.SEPARATOR);
        } else {
            ret = ""; //$NON-NLS-1$
        }

        return ret;
    }

    private static String removeAllTrailing(String str, char toRemove) {
        String ret = str;
        while (ret.endsWith(Character.toString(toRemove))) {
            ret = ret.substring(0, ret.length() - 1);
        }
        return ret;
    }

    private static String append(String path, String str) {
        if (!path.isEmpty()) {
            return path + IPath.SEPARATOR + str;
        }

        return str;
    }

    /**
     * @return the associated TmfTraceElement
     */
    public TmfCommonProjectElement getTraceElement() {
        return fTraceElement;
    }

    /**
     * Set the import name.
     *
     * @param importName the import name.
     */
    public void setImportName(String importName) {
        fImportName = importName;
    }

    /**
     * @return the import name
     */
    public String getImportName() {
        return fImportName;
    }

    /**
     * @return the trace type of this trace
     */
    public String getTraceType() {
        return fTraceType;
    }

    /**
     * Set the trace type of this trace.
     *
     * @param traceType the trace type of this trace
     */
    public void setTraceType(String traceType) {
        fTraceType = traceType;
    }

    @Override
    public Image getImage() {
        TmfNavigatorLabelProvider tmfNavigatorLabelProvider = new TmfNavigatorLabelProvider();
        return tmfNavigatorLabelProvider.getImage(fTraceElement);
    }
}
