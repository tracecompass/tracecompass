/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.lttng.TraceDebug;
import org.eclipse.linuxtools.tmf.event.TmfData;

public abstract class LTTngTreeNodeGeneric<E extends LTTngTreeNodeGeneric<E>>
		extends TmfData implements ILTTngTreeNode<E> {

	// ========================================================================
	// Data
	// ========================================================================
	private final Long fid;
	private final Object ftype;
	private final Object fvalue;
	protected final Map<Long, E> fchildren = new HashMap<Long, E>();
	protected final Map<String, E> fchildrenByName = new HashMap<String, E>();
	protected final Map<String, Object> fattributesByName = new HashMap<String, Object>();
	private E fparent = null;
	private final String fname;
	private Long idCount = 0L;

	// ========================================================================
	// Constructors
	// ========================================================================
	/**
	 * @param id
	 * @param parent
	 * @param name
	 * @param value
	 */
	public LTTngTreeNodeGeneric(Long id, E parent, String name,
			Object value) {
		fid = id;
		fparent = parent;
		fname = name;

		if (value != null) {
			fvalue = value;
			ftype = fvalue.getClass();
		} else {
			fvalue = this;
			ftype = this.getClass();
		}
	}

	/**
	 * @param id
	 * @param parent
	 * @param name
	 */
	public LTTngTreeNodeGeneric(Long id, E parent, String name) {
		this(id, parent, name, null);
	}

	/**
	 * When parent is not know just yet
	 * 
	 * @param id
	 * @param type
	 * @param name
	 */
	public LTTngTreeNodeGeneric(Long id, String name, Object value) {
		this(id, null, name, value);
	}

	// ========================================================================
	// Methods
	// ========================================================================

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.control.ILTTngAnalysisResource#getId()
	 */
	public Long getId() {
		return fid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.control.ILTTngAnalysisResource#getType()
	 */
	public Object getType() {
		return ftype;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.control.ILTTngAnalysisResource#getChildByName
	 * ()
	 */
	public E getChildByName(String name) {
		E child = null;
		if (name != null) {
			child = fchildrenByName.get(name);
		}
		return child;
	}

	/**
	 * @param child
	 */
	public void addChild(E child) {
		if (child != null) {
			Long id = child.getId();
			if (id != null) {
				if (fchildren.containsKey(id) && fchildren.get(id) != child) {
					TraceDebug.debug("Replaced child " + id + " for: " + child);
				}
				fchildren.put(id, child);
				fchildrenByName.put(child.getName(), child);
			}
		}

		return;
	}

	/**
	 * @param child
	 */
	public void removeChild(E child) {
		if (child != null) {
			Long id = child.getId();
			if (id != null) {
				E childToRemove = fchildren.remove(id);
				if (childToRemove != null) {
					fchildrenByName.remove(childToRemove.getName());
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.model.ILTTngTreeNode#getChildren()
	 */
	public abstract E[] getChildren();
	// {
	// return (T[]) childrenToArray(fchildren.values(), this.getClass());
	// }

	/**
	 * Convert from generic collection to generic array. An empty array is
	 * provided when no children nodes are defined
	 * 
	 * @param collection
	 * @param k
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected E[] childrenToArray(Collection<E> collection, Class<? extends E> k) {
		// check entries
		if (collection == null || k == null) {
			return null;
		}

		int size = collection.size();

		// unchecked cast
		E[] childrenArray = (E[]) java.lang.reflect.Array.newInstance(k, size);
		int i = 0;
		for (E item : collection) {
			childrenArray[i++] = item;
		}

		return childrenArray;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.control.ILTTngAnalysisResource#getChildById
	 * (java.lang.Long)
	 */
	public E getChildById(Long id) {
		if (id == null)
			return null;
		return fchildren.get(id);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.control.ILTTngAnalysisResource#getParent()
	 */
	public E getParent() {
		return fparent;
	}

	/**
	 * @param parent
	 */
	public void setParent(E parent) {
		fparent = parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.control.ILTTngAnalysisResource#hasChildren()
	 */
	public boolean hasChildren() {
		if (fchildren.size() > 0) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.control.ILTTngAnalysisResource#getName()
	 */
	public String getName() {
		return fname;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.control.ILTTngAnalysisResource#getPath()
	 */
	public String getPath() {
		return getPath(this, "");
	}

	/**
	 * Obtaining the path recursively up to the related parents until no parent
	 * is found
	 * 
	 * @param child
	 * @param ipath
	 * @return
	 */
	private String getPath(LTTngTreeNodeGeneric<E> child,
			String ipath) {
		String path = ipath;
		if (ipath != null) {
			if (child == null) {
				return ipath;
			} else {
				E parent = child.getParent();
				path = getPath(parent, "/" + child.getName() + ipath);
			}
		}

		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class clazz) {
		if (clazz == ftype) {
			return fvalue;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.model.ILTTngTreeNode#getValue()
	 */
	public Object getValue() {
		return fvalue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.model.ILTTngTreeNode#getNextUniqueId()
	 */
	public synchronized Long getNextUniqueId() {
		return idCount++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.model.ILTTngTreeNode#getAttribute(java.lang
	 * .String)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String key, Class<T> type) {
		if (key != null) {
			Object value = fattributesByName.get(key);
			if (value.getClass().isAssignableFrom(type)) {
				return (T) value;
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.model.ILTTngTreeNode#addAttribute(java.lang
	 * .String, java.lang.Object)
	 */
	public boolean addAttribute(String key, Object value) {
		// validate
		if (key == null || value == null) {
			return false;
		}

		fattributesByName.put(key, value);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.tmf.event.TmfData#isNullRef()
	 */
	public boolean isNullRef() {
		return false;
	}
}
