/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;

/**
 * <b><u>TmfProviderManager.java</u></b>
 * <p>
 * Singleton that keeps track of the event providers.
 */
public class TmfProviderManager {

	// ------------------------------------------------------------------------
	// No constructor
	// ------------------------------------------------------------------------

	private TmfProviderManager() {}
	
	// ------------------------------------------------------------------------
	// Keeps track of the providers for each event type
	// ------------------------------------------------------------------------
	
	private static Map<Class<? extends ITmfEvent>, List<TmfDataProvider<? extends ITmfEvent>>> fProviders =
		   new HashMap<Class<? extends ITmfEvent>, List<TmfDataProvider<? extends ITmfEvent>>>();

	/**
	 * Registers [provider] as a provider of [eventType]
	 * 
	 * @param eventType
	 * @param provider
	 */
	public static <T extends ITmfEvent> void register(Class<T> eventType, TmfDataProvider<? extends ITmfEvent> provider) {
		if (fProviders.get(eventType) == null)
			fProviders.put(eventType, new ArrayList<TmfDataProvider<? extends ITmfEvent>>());
		fProviders.get(eventType).add(provider);
	}

	/**
	 * Re-registers [provider] as a provider of [eventType]
	 * 
	 * @param dataClass
	 * @param provider
	 */
	public static <T extends ITmfEvent> void deregister(Class<T> dataClass, TmfDataProvider<? extends ITmfEvent> provider) {
		List<TmfDataProvider<? extends ITmfEvent>> list = fProviders.get(dataClass);
		if (list != null) {
			list.remove(provider);
			if (list.size() == 0)
				fProviders.remove(dataClass);
		}
	}

	/**
	 * Returns the list of components that provide [eventType]
	 * 
	 * @param dataClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static TmfDataProvider<? extends ITmfEvent>[] getProviders(Class<? extends ITmfEvent> dataClass) {
		List<TmfDataProvider<? extends ITmfEvent>> list = fProviders.get(dataClass);
		if (list == null)
			list = new ArrayList<TmfDataProvider<? extends ITmfEvent>>(); 
		TmfDataProvider<? extends ITmfEvent>[] result = new TmfDataProvider[list.size()];
		return list.toArray(result);
	}

	/**
	 * Returns the list of components of type [providerType] that provide [eventType]
	 * 
	 * @param type
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static TmfDataProvider<? extends ITmfEvent>[] getProviders(Class<? extends ITmfEvent> dataClass, Class<? extends TmfDataProvider<? extends ITmfEvent>> providerClass) {
		if (providerClass == null) {
			return getProviders(dataClass);
		}
		TmfDataProvider<? extends ITmfEvent>[] list = getProviders(dataClass);
		List<TmfDataProvider<? extends ITmfEvent>> result = new ArrayList<TmfDataProvider<? extends ITmfEvent>>();
		if (list != null) {
			for (TmfDataProvider<? extends ITmfEvent> provider : list) {
				if (provider.getClass() == providerClass) {
					result.add(provider);
				}
			}
		}
		TmfDataProvider<? extends ITmfEvent>[] array = new TmfDataProvider[result.size()];
		return result.toArray(array);
	}

}
