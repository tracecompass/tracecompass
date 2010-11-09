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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.swt.widgets.Composite;

public class ParserProviderManager {

    public static final QualifiedName PARSER_PROPERTY = new QualifiedName(TmfUiPlugin.PLUGIN_ID, "PARSER"); //$NON-NLS-1$

    private static List<IParserProvider> fParserProviders = new ArrayList<IParserProvider>();

    public static void init() {
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IConfigurationElement[] extensions = reg.getConfigurationElementsFor("org.eclipse.linuxtools.tmf.ui.parserProviders"); //$NON-NLS-1$
        for (int i = 0; i < extensions.length; i++) {
            IConfigurationElement element = extensions[i];
            try {
                IParserProvider parserProvider = (IParserProvider) element.createExecutableExtension("class"); //$NON-NLS-1$
                addParserProvider(parserProvider);
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void addParserProvider(IParserProvider parserProvider) {
        fParserProviders.add(parserProvider);
    }

    public static void removeParserProvider(IParserProvider parserProvider) {
        fParserProviders.remove(parserProvider);
    }

    public static ITmfTrace getTrace(IResource resource) {
        if (resource == null) {
            return null;
        }
        try {
            String parser = resource.getPersistentProperty(PARSER_PROPERTY);
            if (parser != null) {
                for (IParserProvider parserProvider : fParserProviders) {
                    if (parserProvider != null) {
                        ITmfTrace trace = parserProvider.getTraceForParser(parser, resource);
                        if (trace != null) {
                            return trace;
                        }
                    }
                }
            }
            IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(resource.getName());
            if (contentType != null) {
                for (IParserProvider parserProvider : fParserProviders) {
                    if (parserProvider != null) {
                        ITmfTrace trace = parserProvider.getTraceForContentType(contentType.getId(), resource);
                        if (trace != null) {
                            resource.setPersistentProperty(PARSER_PROPERTY, trace.getClass().getCanonicalName());
                            return trace;
                        }
                    }
                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getEditorId(IResource resource) {
        if (resource == null) {
            return null;
        }
        try {
            String parser = resource.getPersistentProperty(PARSER_PROPERTY);
            if (parser != null) {
                for (IParserProvider parserProvider : fParserProviders) {
                    if (parserProvider != null) {
                        String editorId = parserProvider.getEditorIdForParser(parser);
                        if (editorId != null) {
                            return editorId;
                        }
                    }
                }
            }
            return TmfEventsEditor.ID;
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static Map<String, Map<String, String>> getParserMap() {
        Map<String, Map<String, String>> parserMap = new LinkedHashMap<String, Map<String, String>>();
        for (IParserProvider parserProvider : fParserProviders) {
            parserMap.put(parserProvider.getCategory(), parserProvider.getParserMap());
        }
        return parserMap;
    }

    public static TmfEventsTable getEventsTable(ITmfTrace trace, Composite parent, int cacheSize) {
        for (IParserProvider parserProvider : fParserProviders) {
            if (parserProvider != null) {
                TmfEventsTable eventsTable = parserProvider.getEventsTable(trace, parent, cacheSize);
                if (eventsTable != null) {
                    return eventsTable;
                }
            }
        }
        return null;
    }

}
