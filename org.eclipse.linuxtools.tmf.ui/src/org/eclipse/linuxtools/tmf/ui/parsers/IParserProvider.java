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

	/**
	 * Return the readable category name for this parser provider
	 * 
	 * @return the category name
	 */
    public String getCategory();
    
	/**
	 * Return a trace instance for a resource given a parser id
	 * 
	 * @param parser the parser id
	 * @param resource the resource
	 * @return a trace instance or null if the parser id is not handled by this parser provider
	 */
    public ITmfTrace getTraceForParser(String parser, IResource resource);
    
	/**
	 * Return a trace instance for a resource given a content type id
	 * 
	 * @param contentTypeId the content type id
	 * @param resource the resource
	 * @return a trace instance or null if the content type id is not handled by this parser provider
	 */
    public ITmfTrace getTraceForContentType(String contentTypeId, IResource resource);
    
	/**
	 * Return the parser map for this parser provider
	 * Map key: readable parser name
	 * Map value: unique parser id
	 * 
	 * @return the parser map
	 */
    public Map<String, String> getParserMap();

	/**
	 * Return the event type map given a parser id
	 * Map key: readable event type name
	 * Map value: unique event type id
	 * 
	 * @param parser the parser id
	 * @return the event type map or null if the parser id is not handled by this parser provider
	 */
    public Map<String, String> getEventTypeMapForParser(String parser);
    
	/**
	 * Return the field label array given an event type id
	 * Array value: readable field label
	 * 
	 * @param eventType the event type id
	 * @return the field label array or null if the event type id is not handled by this parser provider
	 */
    public String[] getFieldLabelsForEventType(String eventType);
    
	/**
	 * Return the editor id given a parser id
	 * 
	 * @param parser the parser id
	 * @return an editor id or null to use the default trace editor
	 */
    public String getEditorIdForParser(String parser);

	/**
	 * Return an events table instance for a given trace
	 * 
	 * @param parser the parser id
	 * @param parent the parent composite for the table
	 * @param cacheSize the desired cache size for the table
	 * @return an events table instance or null if the trace is not handled by this parser provider
	 */
    public TmfEventsTable getEventsTable(ITmfTrace trace, Composite parent, int cacheSize);

}
