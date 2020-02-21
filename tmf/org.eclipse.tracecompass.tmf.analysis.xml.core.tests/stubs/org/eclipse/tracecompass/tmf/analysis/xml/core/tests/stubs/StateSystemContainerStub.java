/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stubs;

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

}
