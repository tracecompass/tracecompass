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

package org.eclipse.linuxtools.tmf.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.tmf.event.TmfData;

/**
 * <b><u>TmfProviderManager.java</u></b>
 * <p>
 * Singleton that keeps track of the event providers.
 */
public class TmfProviderManager {

	// ------------------------------------------------------------------------
	// Keeps track of the providers for each event type
	// ------------------------------------------------------------------------
	
	private static Map<Class<? extends TmfData>, List<TmfProvider<? extends TmfData>>> fProviders =
		   new HashMap<Class<? extends TmfData>, List<TmfProvider<? extends TmfData>>>();

	/**
	 * Registers [provider] as a provider of [eventType]
	 * 
	 * @param eventType
	 * @param provider
	 */
	public static <T extends TmfData> void register(Class<T> eventType, TmfProvider<? extends TmfData> provider) {
		if (fProviders.get(eventType) == null)
			fProviders.put(eventType, new ArrayList<TmfProvider<? extends TmfData>>());
		assert(fProviders.get(eventType) != null);
		fProviders.get(eventType).add(provider);
	}

	/**
	 * Re-registers [provider] as a provider of [eventType]
	 * 
	 * @param eventType
	 * @param provider
	 */
	public static <T extends TmfData> void deregister(Class<T> eventType, TmfProvider<? extends TmfData> provider) {
		List<TmfProvider<? extends TmfData>> list = fProviders.get(eventType);
		if (list != null) {
			list.remove(provider);
			if (list.size() == 0)
				fProviders.remove(eventType);
		}
	}

	/**
	 * Returns the list of components that provide [eventType]
	 * 
	 * @param eventType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static TmfProvider<? extends TmfData>[] getProviders(Class<? extends TmfData> eventType) {
		List<TmfProvider<? extends TmfData>> list = fProviders.get(eventType);
		if (list == null)
			list = new ArrayList<TmfProvider<? extends TmfData>>(); 
		TmfProvider<? extends TmfData>[] result = new TmfProvider[list.size()];
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
	public static TmfProvider<? extends TmfData>[] getProviders(Class<? extends TmfData> eventType, Class<? extends TmfProvider<? extends TmfData>> providerType) {
		TmfProvider<? extends TmfData>[] list = getProviders(eventType);
		List<TmfProvider<? extends TmfData>> result = new ArrayList<TmfProvider<? extends TmfData>>();
		if (list != null) {
			for (TmfProvider<? extends TmfData> provider : list) {
				if (provider.getClass() == providerType) {
					result.add(provider);
				}
			}
		}
		TmfProvider<? extends TmfData>[] array = new TmfProvider[result.size()];
		return result.toArray(array);
	}

}
