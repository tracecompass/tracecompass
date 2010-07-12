/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.parsers;

import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.swt.widgets.Composite;

public interface IParserProvider {

    public String getCategory();
    
    public ITmfTrace getTraceForParser(String parser, IResource resource);
    
    public ITmfTrace getTraceForContentType(String contentTypeId, IResource resource);
    
    public String getEditorIdForParser(String parser);
    
    public Map<String, String> getParserMap();

    public TmfEventsTable getEventsTable(ITmfTrace trace, Composite parent, int cacheSize);

}
