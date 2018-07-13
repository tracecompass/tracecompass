/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stubs;

import java.util.HashSet;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlLocation;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;

/**
 * This class is a stub for State system Container.
 *
 * @author Jean-Christian Kouame
 *
 */
public class StateSystemContainerStub implements IXmlStateSystemContainer {

    @Override
    public String getAttributeValue(String name) {
        return null;
    }

    @Override
    public ITmfStateSystem getStateSystem() {
        throw new UnsupportedOperationException("No state system here...");
    }

    @Override
    public @NonNull Iterable<@NonNull TmfXmlLocation> getLocations() {
        return new HashSet<>();
    }
}
