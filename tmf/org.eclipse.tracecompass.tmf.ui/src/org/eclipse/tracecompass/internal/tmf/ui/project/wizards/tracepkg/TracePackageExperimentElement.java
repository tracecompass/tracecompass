/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;

/**
 * A TracePackageElement associated to a TmfExperimentElement. This will be the
 * parent of other elements (supplementary files, bookmarks, etc).
 */
public class TracePackageExperimentElement extends TracePackageTraceElement {

    private final List<String> fExpTraces = new ArrayList<>();

    /**
     * Construct an instance associated to a TraceExperimentElement. For
     * exporting.
     *
     * @param parent
     *            the parent of this element, can be set to null
     * @param experimentElement
     *            the associated TmfExperimentElement
     */
    public TracePackageExperimentElement(TracePackageElement parent, TmfExperimentElement experimentElement) {
        super(parent, experimentElement);
    }

    /**
     * Construct an instance associated to a TmfExperimentElement. For
     * importing.
     *
     * @param parent
     *            the parent of this element, can be set to null
     * @param importName
     *            the name to use to identify this trace
     * @param traceType
     *            the trace type to set for this trace
     */
    public TracePackageExperimentElement(TracePackageElement parent, String importName, String traceType) {
        super(parent, importName, traceType);
    }

    /**
     * Adds an experiment trace to this experiment element.
     *
     * @param expTrace
     *            the experiment trace element path
     */
    public void addExpTrace(String expTrace) {
        fExpTraces.add(expTrace);
    }

    /**
     * Get the experiment traces of this experiment element.
     *
     * @return the list of experiment trace element paths
     */
    public List<String> getExpTraces() {
        return Collections.unmodifiableList(fExpTraces);
    }
}
