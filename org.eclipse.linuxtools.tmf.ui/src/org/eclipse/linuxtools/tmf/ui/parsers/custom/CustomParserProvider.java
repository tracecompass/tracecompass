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

package org.eclipse.linuxtools.tmf.ui.parsers.custom;

import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.parsers.IParserProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.swt.widgets.Composite;

public class CustomParserProvider implements IParserProvider {

	@Override
    public String getCategory() {
        return "Custom";
    }

	@Override
    public ITmfTrace getTraceForParser(String parser, IResource resource) {
        try {
            String name = resource.getName();
            String path = resource.getLocation().toOSString();
            for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
                if (parser.equals(CustomTxtTrace.class.getCanonicalName() + "." + def.definitionName)) {
                    return new CustomTxtTrace(name, def, path, 100);
                }
            }
            for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
                if (parser.equals(CustomXmlTrace.class.getCanonicalName() + "." + def.definitionName)) {
                    return new CustomXmlTrace(name, def, path, 100);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

	@Override
    public ITmfTrace getTraceForContentType(String contentTypeId, IResource resource) {
        return null;
    }

	@Override
	public String getEditorIdForParser(String parser) {
        return null;
    }

	@Override
    public Map<String, String> getParserMap() {
        Map<String, String> parserMap = new LinkedHashMap<String, String>();
        for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
            parserMap.put(def.definitionName, CustomTxtTrace.class.getCanonicalName() + "." + def.definitionName);
        }
        for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
            parserMap.put(def.definitionName, CustomXmlTrace.class.getCanonicalName() + "." + def.definitionName);
        }
        return parserMap;
    }

	@Override
    public TmfEventsTable getEventsTable(ITmfTrace trace, Composite parent, int cacheSize) {
        if (trace instanceof CustomTxtTrace) {
            return new CustomEventsTable(((CustomTxtTrace) trace).getDefinition(), parent, cacheSize);
        } else if (trace instanceof CustomXmlTrace) {
            return new CustomEventsTable(((CustomXmlTrace) trace).getDefinition(), parent, cacheSize);
        }
        return null;
    }

}
