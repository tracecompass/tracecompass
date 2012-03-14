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
package org.eclipse.linuxtools.internal.lttng.core.model;

import org.eclipse.core.runtime.IAdaptable;

/**
 * @author alvaro
 *
 */
public interface ILTTngTreeNode<E extends ILTTngTreeNode<E>> extends IAdaptable {
	// ========================================================================
	// Methods
	// ========================================================================
	/**
	 * Return the unique id of this resource
	 * 
	 * @return
	 */
	public Long getId();

	/**
	 * Types are defined by the user application
	 * 
	 * @return
	 */
	public Object getNodeType();

	/**
	 * Return this resource name
	 * 
	 * @return
	 */
	public E getChildByName(String name);

	/**
	 * @param k
	 *            k needed for the creation of the generic array
	 * @return
	 */
	public E[] getChildren();

	/**
	 * Get the child by its unique id
	 * 
	 * @param id
	 * @return
	 */
	public E getChildById(Long id);

	/**
	 * Get the parent of this resource
	 * 
	 * @return
	 */
	public E getParent();

	/**
	 * @return
	 */
	public boolean hasChildren();

	/**
	 * Return the name of this resource
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * <p>
	 * Get the path from parent resources to this resource
	 * </p>
	 * <p>
	 * e.g. /root/name1/name2
	 * </p>
	 * 
	 * @return
	 */
	public String getPath();

	/**
	 * Return the reference value associated to this tree node
	 * 
	 * @return
	 */
	public Object getValue();

	/**
	 * returns the next value to be used as unique id in reference to this
	 * instance e.g. can be used to construct children unique ids.
	 */
	public Long getNextUniqueId();

	/**
	 * Returns an attribute by name and casts the attribute value to the
	 * specified type, returns null if the attribute itself is null, not and
	 * instance of the specified class or the attribute has not been added
	 * 
	 * @return
	 */
	public <T> T getAttribute(String name, Class<T> type);

	/**
	 * Adds an attribute by name and which is not a tree node element
	 * 
	 * @return true if the element was not added e.g. invalid input
	 */
	public boolean addAttribute(String name, Object attribute);
}
