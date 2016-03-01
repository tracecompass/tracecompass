/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlLocation;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.w3c.dom.Element;

/**
 * State provider for the pattern analysis
 *
 * @author Jean-Christian Kouame
 */
public class XmlPatternStateProvider extends AbstractTmfStateProvider implements IXmlStateSystemContainer {

    private final IPath fFilePath;

    private final @NonNull String fStateId;

    /** List of all Locations */
    private final @NonNull Set<@NonNull TmfXmlLocation> fLocations;

    private final ISegmentListener fListener;

    /**
     * @param trace
     *            The active trace
     * @param stateid
     *            The state id, which corresponds to the id of the analysis
     *            defined in the XML file
     * @param file
     *            The XML file
     * @param listener
     *            Listener for segment creation
     */
    public XmlPatternStateProvider(@NonNull ITmfTrace trace, @NonNull String stateid, @Nullable IPath file, ISegmentListener listener) {
        super(trace, stateid);
        fStateId = stateid;
        fFilePath = file;
        fListener = listener;
        final String pathString = fFilePath.makeAbsolute().toOSString();
        Element doc = XmlUtils.getElementInFile(pathString, TmfXmlStrings.PATTERN, fStateId);
        fLocations = new HashSet<>();
        if (doc == null) {
            Activator.logError("Failed to find a pattern in " + pathString); //$NON-NLS-1$
            return;
        }
    }

    @Override
    public String getAttributeValue(String name) {
        return null;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public @NonNull ITmfStateProvider getNewInstance() {
        return new XmlPatternStateProvider(getTrace(), getStateId(), fFilePath, fListener);
    }

    /**
     * Get the state ID of the provider. It corresponds to the analysis ID.
     *
     * @return the state Id
     */
    public @NonNull String getStateId() {
        return fStateId;
    }

    @Override
    public ITmfStateSystem getStateSystem() {
        return getStateSystemBuilder();
    }

    @Override
    public @NonNull Iterable<@NonNull TmfXmlLocation> getLocations() {
        return fLocations;
    }

    @Override
    protected void eventHandle(@NonNull ITmfEvent event) {
    }

    /**
     * Get the listerner for segments creation
     *
     * @return The segment listener
     */
    public ISegmentListener getListener() {
        return fListener;
    }

    @Override
    public void dispose() {
        fListener.onNewSegment(XmlPatternSegmentStoreModule.END_SEGMENT);
        super.dispose();
    }
}
