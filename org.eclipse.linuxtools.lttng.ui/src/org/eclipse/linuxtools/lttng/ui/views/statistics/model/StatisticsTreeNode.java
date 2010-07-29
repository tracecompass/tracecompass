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
 *   Francois Godin (copelnug@gmail.com)  - Re-design for new stats structure
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.statistics.model;

import java.util.Collection;

/**
 * <h4>A tree where nodes can be accessed efficiently using paths.</h4>
 * 
 * <p>It works like file systems. Each node is identified by a key. A path is an array ({@link FixedArray}) of String. The elements of the array represent the path from the root to this node.</p>
 */
public class StatisticsTreeNode {
	/**
	 * <h4>Value of the node.</h4>
	 */
	private Statistics fValue;
	/**
	 * <h4>Path of the node.</h4>
	 */
	private FixedArray<String> fPath;
	/**
	 * <h4>Corresponding StatisticsData.</h4>
	 */
	private StatisticsData fNodes;
	/**
	 * <h4>Constructor.</h4>
	 * @param path Path to the node.
	 * @param nodes Corresponding StatisticsData.
	 */
	public StatisticsTreeNode(final FixedArray<String> path, StatisticsData nodes) {
		fPath = path;
		fNodes = nodes;
		fValue = new Statistics();
	}
	/**
	 * <h4>Test if a node contain the specified child.</h4>
	 * @param key Name of the child.
	 * @return true: if child with given key is present, false: if no child exists with given key name
	 */
	public boolean containsChild(String key) {
		if(StatisticsData.ROOT.equals(fPath))
			return fNodes.get(new FixedArray<String>(key)) != null;
		return (fNodes.get(fPath.append(key)) != null);
	}
	/**
	 * <h4>Get the children of this node.</h4>
	 * @return Direct children of this node.
	 */
	public Collection<StatisticsTreeNode> getChildren() {
		return fNodes.getChildren(fPath);
	}
	/**
	 * <h4>Get the key for this node.</h4>
	 * @return Key associated with this node.
	 */
	public String getKey() {
		return fPath.get(fPath.size() - 1);
	}
	/**
	 * <h4>Get the number of children this node have.</h4>
	 * @return Number of direct children of this node.
	 */
	public int getNbChildren() {
		return fNodes.getChildren(fPath).size();
	}
	/**
	 * <h4>Return the parent node.</h4>
	 * @return Parent node.
	 */
	public StatisticsTreeNode getParent() {
		return fNodes.getParent(fPath);
	}
	/**
	 * <h4>Get the path of the node.</h4>
	 * @return The path of the node.
	 */
	public FixedArray<String> getPath() {
		return fPath;
	}
	/**
	 * <h4>Get the value of this node.</h4>
	 * @return Value associated with this node.
	 */
	public Statistics getValue() {
		return fValue;
	}
	/**
	 * <h4>Indicate if the node have children.</h4>
	 * @return True if the node has children.
	 */
	public boolean hasChildren() {
		return !fNodes.getChildren(fPath).isEmpty();
	}
	/**
	 * <h4>Start from creation time i.e. keep key and parent but new statistics and no children.</h4>
	 */
	public void reset() {
		fValue = new Statistics();
		fNodes.reset(fPath);
	}
}