/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.synchronization.graph;

import java.math.BigDecimal;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.linuxtools.internal.tmf.core.synchronization.ITmfTimestampTransformInvertible;
import org.eclipse.linuxtools.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.linuxtools.tmf.core.synchronization.TimestampTransformFactory;

/**
 * Implements a tree to calculate the synchronization between hosts
 *
 * TODO: This minimal implementation does not take into account the accuracy of
 * the synchronization or the number of hops between 2 traces. A great
 * improvement would be to implement Masoume Jabbarifar's minimal spanning tree
 * algorithm to select reference trace(s) and optimal path to each node of the
 * tree.
 *
 * @author Geneviève Bastien
 */
public class SyncSpanningTree {

    private final SyncGraph<String, ITmfTimestampTransform> fSyncGraph;

    /*
     * Using a TreeSet here to make sure the order of the hosts, and thus the
     * reference node, is predictable, mostly for unit tests.
     */
    private SortedSet<String> fHosts = new TreeSet<>();

    /**
     * Default constructor
     */
    public SyncSpanningTree() {
        fSyncGraph = new SyncGraph<>();
    }

    /**
     * Add a synchronization formula between hostFrom and hostTo with a given
     * accuracy
     *
     * @param hostFrom
     *            Host from which the transform applies
     * @param hostTo
     *            Host to which the transform applies
     * @param transform
     *            The timestamp transform
     * @param accuracy
     *            The accuracy of the synchronization between hostFrom and
     *            hostTo
     */
    public void addSynchronization(String hostFrom, String hostTo, ITmfTimestampTransform transform, BigDecimal accuracy) {
        fHosts.add(hostFrom);
        fHosts.add(hostTo);
        fSyncGraph.addEdge(hostFrom, hostTo, transform);
        if (transform instanceof ITmfTimestampTransformInvertible) {
            fSyncGraph.addEdge(hostTo, hostFrom, ((ITmfTimestampTransformInvertible) transform).inverse());
        }
    }

    /**
     * Get the timestamp transform to a host
     *
     * FIXME: This might not work in situations where we have disjoint graphs
     * since we only calculate 1 root node and each tree has its own root. When
     * implementing the algorithm with minimal spanning tree, we will solve this
     * problem.
     *
     * @param host
     *            The host to reach
     * @return The timestamp transform to host
     */
    public ITmfTimestampTransform getTimestampTransform(String host) {
        ITmfTimestampTransform result = TimestampTransformFactory.getDefaultTransform();
        String rootNode = getRootNode();
        /*
         * Compute the path from reference node to the given host id
         */
        if (rootNode != null) {
            List<Edge<String, ITmfTimestampTransform>> path = fSyncGraph.path(rootNode, host);
            /*
             * Compute the resulting transform by chaining each transforms on
             * the path.
             */
            for (Edge<String, ITmfTimestampTransform> edge : path) {
                result = result.composeWith(edge.getLabel());
            }
        }
        return result;
    }

    private String getRootNode() {
        /**
         * Get the root node from which all other paths will be calculated. For
         * now, we take the first node alphabetically.
         */
        if (fHosts.size() == 0) {
            return null;
        }
        return fHosts.first();
    }

    /**
     * Check if this multi-host synchronization tree is connected, ie all nodes
     * have a synchronization path to a reference node.
     *
     * @return true if the tree is connected, false otherwise
     */
    public boolean isConnected() {
        return fSyncGraph.isConnected();
    }

}
