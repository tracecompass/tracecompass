/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Yann N. Dauphin     (dhaemon@gmail.com)  - Implementation for stats
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.statistics.model;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;

/*
 * A tree where nodes can be accessed efficiently using paths.
 * 
 * It works like file systems. Each node is identified by a key. A path is a list of keys separated by the character '/'.
 * For example, the path 'persons/yann' will browse to the child 'persons' and return it's 'yann' child.
 * 
 * If a key might contains the character '/', use the #escapeKey method to get an escaped key. Use the #unescapeKey
 * method to unescaped the key.
 */
public class StatisticsTreeNode {

	private StatisticsTreeNode parent;

	private String key;

	private Statistics value;

	private AbstractMap<String, StatisticsTreeNode> children;

	/*
	 * Construct a node with the given key and value.
	 */
	public StatisticsTreeNode(String key, Statistics value) {
		this(null, key, value);
	}

	/*
	 * Construct a node with the given parent, key and value.
	 */
	public StatisticsTreeNode(StatisticsTreeNode parent, String key,
			Statistics value) {
		super();
		this.parent = parent;
		this.key = key;
		this.value = value;
		this.children = new HashMap<String, StatisticsTreeNode>();
	}

	/*
	 * @return key associated with this node.
	 */
	public StatisticsTreeNode getParent() {
		return this.parent;
	}

	/*
	 * @return key associated with this node.
	 */
	public String getKey() {
		return this.key;
	}

	/*
	 * @return value associated with this node.
	 */
	public Statistics getValue() {
		return this.value;
	}

	/*
	 * Add a direct child with the given value at the given path.
	 * 
	 * @return children node that was created.
	 */
	public StatisticsTreeNode addChild(String key, Statistics value) {
		StatisticsTreeNode created = new StatisticsTreeNode(this, key, value);

		this.children.put(key, created);

		return created;
	}

	/*
	 * @return direct children node with the given key. null if not found.
	 */
	public StatisticsTreeNode getChild(String key) {
		if (!this.children.containsKey(key)) {
			return null;
		}

		return this.children.get(key);
	}

	/*
	 * @return number of direct children of this node.
	 */
	public boolean hasChildren() {
		return getNbChildren() > 0;
	}

	/*
	 * @return direct children of this node.
	 */
	public Collection<StatisticsTreeNode> getChildren() {
		return children.values();
	}

	/*
	 * @return number of direct children of this node.
	 */
	public int getNbChildren() {
		return children.size();
	}

	/*
	 * Get the node at the given path. If it doesn't exist each node in the path
	 * will be created with the given class.
	 * 
	 * @return children node with the given path. null if not found.
	 */
	public StatisticsTreeNode getOrCreateChildFromPath(String[] path) {
		// StatisticsTreeNode previous = this.parent;
		StatisticsTreeNode current = this;
		for (String key : path) {
			if (!current.children.containsKey(key)) {
				current.children.put(key, new StatisticsTreeNode(current, key,
						new Statistics()));
			}

			// previous = current;
			current = current.children.get(key);
		}

		return current;
	}

	/*
	 * Get the node at the given path. If it doesn't exist each node in the path
	 * will be created with the given class.
	 * 
	 * @return children node with the given path. null if not found.
	 */
	public StatisticsTreeNode getOrCreateChildFromPath(String path) {
		StatisticsTreeNode previous = this.parent;
		StatisticsTreeNode current = this;
		for (String key : path.split("/")) {
			if (!current.children.containsKey(key)) {
				current.children.put(key, new StatisticsTreeNode(previous, key,
						new Statistics()));
			}

			previous = current;
			current = current.children.get(key);
		}

		return current;
	}

	/*
	 * @return children node with the given path. null if not found.
	 */
	public StatisticsTreeNode getChildFromPath(String path) {
		StatisticsTreeNode current = this;
		for (String key : path.split("/")) {
			if (!current.children.containsKey(key)) {
				return null;
			}

			current = current.children.get(key);
		}

		return current;
	}

	/*
	 * Use this to escape a key that might contain the '/' character.
	 * 
	 * @return escaped key
	 */
	public static String escapeKey(String key) {
		return key.replace("%", "%25").replace("/", "%2F");
	}

	/*
	 * Use this to unescape a key.
	 * 
	 * @return unescaped key
	 */
	public static String unescapeKey(String key) {
		return key.replace("%2F", "/").replace("%25", "%");
	}
}