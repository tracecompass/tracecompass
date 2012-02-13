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

import org.eclipse.linuxtools.tmf.core.event.TmfDataItem;

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
	
	private static Map<Class<? extends TmfDataItem>, List<TmfDataProvider<? extends TmfDataItem>>> fProviders =
		   new HashMap<Class<? extends TmfDataItem>, List<TmfDataProvider<? extends TmfDataItem>>>();

	/**
	 * Registers [provider] as a provider of [eventType]
	 * 
	 * @param eventType
	 * @param provider
	 */
	public static <T extends TmfDataItem> void register(Class<T> eventType, TmfDataProvider<? extends TmfDataItem> provider) {
		if (fProviders.get(eventType) == null)
			fProviders.put(eventType, new ArrayList<TmfDataProvider<? extends TmfDataItem>>());
		fProviders.get(eventType).add(provider);
	}

	/**
	 * Re-registers [provider] as a provider of [eventType]
	 * 
	 * @param dataClass
	 * @param provider
	 */
	public static <T extends TmfDataItem> void deregister(Class<T> dataClass, TmfDataProvider<? extends TmfDataItem> provider) {
		List<TmfDataProvider<? extends TmfDataItem>> list = fProviders.get(dataClass);
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
	public static TmfDataProvider<? extends TmfDataItem>[] getProviders(Class<? extends TmfDataItem> dataClass) {
		List<TmfDataProvider<? extends TmfDataItem>> list = fProviders.get(dataClass);
		if (list == null)
			list = new ArrayList<TmfDataProvider<? extends TmfDataItem>>(); 
		TmfDataProvider<? extends TmfDataItem>[] result = new TmfDataProvider[list.size()];
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
	public static TmfDataProvider<? extends TmfDataItem>[] getProviders(Class<? extends TmfDataItem> dataClass, Class<? extends TmfDataProvider<? extends TmfDataItem>> providerClass) {
		if (providerClass == null) {
			return getProviders(dataClass);
		}
		TmfDataProvider<? extends TmfDataItem>[] list = getProviders(dataClass);
		List<TmfDataProvider<? extends TmfDataItem>> result = new ArrayList<TmfDataProvider<? extends TmfDataItem>>();
		if (list != null) {
			for (TmfDataProvider<? extends TmfDataItem> provider : list) {
				if (provider.getClass() == providerClass) {
					result.add(provider);
				}
			}
		}
		TmfDataProvider<? extends TmfDataItem>[] array = new TmfDataProvider[result.size()];
		return result.toArray(array);
	}

}
