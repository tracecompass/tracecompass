/**********************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.internal.tmf.ui.TmfUiTracer;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.ISDPreferences;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.SDViewPref;

/**
 * The base class used for all UML2 graph nodes displayed in the Sequence Diagram SDWidget.
 *
 * @author sveyrier
 * @version 1.0
 */
public abstract class GraphNode {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The start event occurrence.
     */
    protected int fStartEventOccurrence = 0;
    /**
     * The event event occurrence.
     */
    protected int fEndEventOccurrence = 0;
    /**
     * Preference ColorId to use to draw font
     */
    public String fPrefId = ISDPreferences.PREF_SYNC_MESS;
    /**
     * The selection state of the graph node.
     */
    protected boolean fSelected = false;
    /**
     * The focus state of the graph node.
     */
    protected boolean fFocused = false;
    /**
     * Flag to indicate whether node has children or not.
     */
    protected boolean fHasChilden = false;
    /**
     * The graph node name used to label the graph node in the View.
     */
    protected String fName = ""; //$NON-NLS-1$
    /**
     * A map from node name to graph node.
     */
    protected Map<String, List<GraphNode>> fNodes;
    /**
     * A map from node name to graph node for forward sorting
     */
    protected Map<String, List<GraphNode>> fForwardNodes;
    /**
     * A map from node name to graph node for backwards sorting.
     */
    protected Map<String, List<GraphNode>> fBackwardNodes;
    /**
     * A map from node name to index.
     */
    protected Map<String, Integer> fIndexes;
    /**
     * A map from node name to index for forwards sorting.
     */
    protected Map<String, Boolean> fForwardSort;
    /**
     * A map from node name to index for forwards sorting.
     */
    protected Map<String, Boolean> fBackwardSort;

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * Reset the internal index of the first visible GraphNode for each ordered GraphNode lists
     */
    public void resetIndex() {
        if (!fHasChilden) {
            return;
        }

        Iterator<String> it = fIndexes.keySet().iterator();
        while (it.hasNext()) {
            String nodeType = it.next();
            fIndexes.put(nodeType, Integer.valueOf(0));
        }
    }

    /**
     * Add a GraphNode into the receiver
     *
     * @param nodeToAdd the node to add
     */
    public void addNode(GraphNode nodeToAdd) {
        if (!fHasChilden) {
            fNodes = new HashMap<String, List<GraphNode>>(2);
            fForwardNodes = new HashMap<String, List<GraphNode>>(2);
            fBackwardNodes = new HashMap<String, List<GraphNode>>(2);
            fIndexes = new HashMap<String, Integer>(2);
            fBackwardSort = new HashMap<String, Boolean>(2);
            fForwardSort = new HashMap<String, Boolean>(2);
            fHasChilden = true;
        }

        // Nothing to add
        if (nodeToAdd == null) {
            return;
        }

        if (fNodes.get(nodeToAdd.getArrayId()) == null) {
            fNodes.put(nodeToAdd.getArrayId(), new ArrayList<GraphNode>(1));
            fIndexes.put(nodeToAdd.getArrayId(), Integer.valueOf(0));
            fForwardNodes.put(nodeToAdd.getArrayId(), new ArrayList<GraphNode>(1));
            fForwardSort.put(nodeToAdd.getArrayId(), Boolean.FALSE);
            if (nodeToAdd.getBackComparator() != null) {
                fBackwardNodes.put(nodeToAdd.getArrayId(), new ArrayList<GraphNode>(1));
                fBackwardSort.put(nodeToAdd.getArrayId(), Boolean.FALSE);
            }
        }

        List<GraphNode> fNodeList = fForwardNodes.get(nodeToAdd.getArrayId());
        List<GraphNode> bNodeList = null;
        if (fBackwardNodes != null) {
            bNodeList = fBackwardNodes.get(nodeToAdd.getArrayId());
        }
        if (fNodeList != null && fNodeList.size() > 0) {
            // check if the nodes are added y ordered
            // if not, tag the list to sort it later (during draw)
            GraphNode node = fNodeList.get(fNodeList.size() - 1);
            Comparator<GraphNode> fcomp = nodeToAdd.getComparator();
            Comparator<GraphNode> bcomp = nodeToAdd.getBackComparator();
            if ((fcomp != null) && (fcomp.compare(node, nodeToAdd) > 0)) {
                fForwardSort.put(nodeToAdd.getArrayId(), Boolean.TRUE);
            }
            if ((bcomp != null) && (bcomp.compare(node, nodeToAdd) > 0)) {
                fBackwardSort.put(nodeToAdd.getArrayId(), Boolean.TRUE);
            }
        }

        if (fNodeList == null) {
            fNodeList = new ArrayList<GraphNode>();
        }

        fNodeList.add(nodeToAdd);
        fNodes.put(nodeToAdd.getArrayId(), fNodeList);
        fForwardNodes.put(nodeToAdd.getArrayId(), fNodeList);
        if ((bNodeList != null) && (nodeToAdd.getBackComparator() != null)) {
            bNodeList.add(nodeToAdd);
            fBackwardNodes.put(nodeToAdd.getArrayId(), bNodeList);
        }
    }

    /**
     * Set the graph node name.<br>
     * It is the name display in the view to label the graph node.
     *
     * @param nodeName the name to set
     */
    public void setName(String nodeName) {
        fName = nodeName;
    }

    /**
     * Returns the graph node name.<br>
     * It is the name display in the view to label the graph node.
     *
     * @return the graph node name
     */
    public String getName() {
        return fName;
    }

    /**
     * Tags the the graph node has selected.<br>
     * WARNING: This method is only used to draw the graph node using the system selection colors. <br>
     * To use the complete SDViewer selection mechanism (selection management, notification, etc..) see SDWidget class
     *
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget#addSelection(GraphNode)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget#removeSelection(GraphNode)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget#clearSelection()
     * @param selection - true to set selected, false to set unselected
     */
    public void setSelected(boolean selection) {
        fSelected = selection;
    }

    /**
     * Tags the the graph node as focused.<br>
     * WARNING: This method is only used to draw the graph node using the system focus style. <br>
     * To use the complete SDViewer focus mechanism see SDWidget class
     *
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget#addSelection(GraphNode)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget#removeSelection(GraphNode)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget#clearSelection()
     * @param focus - true to set focued, false otherwise
     */
    public void setFocused(boolean focus) {
        fFocused = focus;
    }

    /**
     * Returns true if the graph node is selected, false otherwise.<br>
     * The returned value is used to highlight the graph node in the View.
     *
     * @return true if selected, false otherwise
     */
    public boolean isSelected() {
        return fSelected;
    }

    /**
     * Returns true if the graph node is focused, false otherwise.<br>
     * The returned value is used to highlight the graph node in the View.
     *
     * @return true if focused, false otherwise
     */
    public boolean hasFocus() {
        return fFocused;
    }

    /**
     * Returns true if the graph node contains the point given in parameter,
     * return false otherwise.
     *
     * @param x
     *            the x coordinate of the point to test containment
     * @param y
     *            the y coordinate of the point to test containment
     * @return true if contained, false otherwise
     */
    abstract public boolean contains(int x, int y);

    /**
     * Returns the x coordinate of the graph node
     *
     * @return the x coordinate
     */
    abstract public int getX();

    /**
     * Returns the y coordinate of the graph node
     *
     * @return the y coordinate
     */
    abstract public int getY();

    /**
     * Returns the graph node height
     *
     * @return the graph node height
     */
    abstract public int getHeight();

    /**
     * Returns the graph node width
     *
     * @return the graph node width
     */
    abstract public int getWidth();

    /**
     * Draws the graph node in the given context
     *
     * @param context the graphical context to draw in
     */
    abstract protected void draw(IGC context);

    /**
     * Returns the GraphNode visibility for the given visible area. Wrong
     * visibility calculation, may strongly impact drawing performance
     *
     * @param x
     *            The X coordinate
     * @param y
     *            The Y coordinate
     * @param width
     *            The width of the area
     * @param height
     *            The height of the area
     * @return true if visible, false otherwise
     */
    public boolean isVisible(int x, int y, int width, int height) {
        return true;
    }

    /**
     * Return a comparator to sort the GraphNode of the same type This
     * comparator is used to order the GraphNode array of the given node type.
     * (see getArrayId).
     *
     * @return the comparator
     */
    public Comparator<GraphNode> getComparator() {
        return null;
    }

    /**
     * If needed, return a different comparator to backward scan the GraphNode
     * array
     *
     * @return the backward comparator or null if not needed
     */
    public Comparator<GraphNode> getBackComparator() {
        return null;
    }

    /**
     * Compare two graphNodes
     *
     * @param node
     *            the node to compare to
     * @return true if equal false otherwise
     */
    public boolean isSameAs(GraphNode node) {
        return false;
    }

    /**
     * Return the node type for all class instances. This id is used to store the same nodes kind in the same ordered
     * array.
     *
     * @return the node type identifier
     */
    abstract public String getArrayId();

    /**
     * Return true if the distance from the GraphNode to the given point is positive
     *
     * @param x the point x coordinate
     * @param y the point y coordinate
     * @return true if positive false otherwise
     */
    public boolean positiveDistanceToPoint(int x, int y) {
        return false;
    }

    /**
     * Returns the graph node which contains the point given in parameter WARNING: Only graph nodes in the current
     * visible area can be returned
     *
     * @param x the x coordinate of the point to test
     * @param y the y coordinate of the point to test
     * @return the graph node containing the point given in parameter, null otherwise
     */
    public GraphNode getNodeAt(int x, int y) {
        GraphNode toReturn = null;

        if (!fHasChilden) {
            return null;
        }

        Iterator<String> it = fNodes.keySet().iterator();
        GraphNode node = null;
        while (it.hasNext()) {
            Object nodeType = it.next();
            List<GraphNode> list = fNodes.get(nodeType);
            int index = fIndexes.get(nodeType).intValue();
            node = getNodeFromListAt(x, y, list, index);
            if (toReturn == null) {
                toReturn = node;
            }
            if (node != null) {
                GraphNode internalNode = node.getNodeAt(x, y);
                if (internalNode != null) {
                    return internalNode;
                } else if (Math.abs(node.getWidth()) < Math.abs(toReturn.getWidth()) || Math.abs(node.getHeight()) < Math.abs(toReturn.getHeight())) {
                    toReturn = node;
                }
            }
        }
        return toReturn;
    }

    /**
     * Gets node list from node A to node B

     * @param from A from node
     * @param to A to node
     * @return the list of nodes
     */
    public List<GraphNode> getNodeList(GraphNode from, GraphNode to) {
        List<GraphNode> result = new ArrayList<GraphNode>();

        if (from != null) {
            result.add(from);
        } else if (to != null) {
            result.add(to);
        }

        if ((from == null) || (to == null)) {
            return result;
        }

        if (from == to) {
            return result;
        }

        int startX = Math.min(from.getX(), Math.min(to.getX(), Math.min(from.getX() + from.getWidth(), to.getX() + to.getWidth())));
        int endX = Math.max(from.getX(), Math.max(to.getX(), Math.max(from.getX() + from.getWidth(), to.getX() + to.getWidth())));
        int startY = Math.min(from.getY(), Math.min(to.getY(), Math.min(from.getY() + from.getHeight(), to.getY() + to.getHeight())));
        int endY = Math.max(from.getY(), Math.max(to.getY(), Math.max(from.getY() + from.getHeight(), to.getY() + to.getHeight())));

        if (!fHasChilden) {
            return result;
        }

        Iterator<String> it = fNodes.keySet().iterator();
        while (it.hasNext()) {
            Object nodeType = it.next();
            List<GraphNode> nodesList = fNodes.get(nodeType);
            if (nodesList == null || nodesList.isEmpty()) {
                return null;
            }
            for (int i = 0; i < nodesList.size(); i++) {
                GraphNode node = nodesList.get(i);
                int nw = node.getWidth();
                int nh = node.getHeight();
                int nx = node.getX();
                int ny = node.getY();
                if (contains(startX, startY, endX - startX, endY - startY, nx + 1, ny + 1) && contains(startX, startY, endX - startX, endY - startY, nx + nw - 2, ny + nh - 2)) {
                    result.add(node);
                }
                result.addAll(node.getNodeList(from, to));
            }
        }

        if (!result.contains(to)) {
            result.add(to);
        }
        return result;
    }

    /**
     * Returns the graph node which contains the point given in parameter for the given graph node list and starting the
     * iteration at the given index<br>
     * WARNING: Only graph nodes with smaller coordinates than the current visible area can be returned.<br>
     *
     * @param x the x coordinate of the point to test
     * @param y the y coordinate of the point to test
     * @param list the list to search in
     * @param fromIndex list browsing starting point
     * @return the graph node containing the point given in parameter, null otherwise
     */
    protected GraphNode getNodeFromListAt(int x, int y, List<GraphNode> list, int fromIndex) {
        if (list == null) {
            return null;
        }
        for (int i = fromIndex; i < list.size(); i++) {
            GraphNode node = list.get(i);
            if (node.contains(x, y)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Returns the start event occurrence attached to this graphNode.
     *
     * @return the start event occurrence attached to the graphNode
     */
    public int getStartOccurrence() {
        return fStartEventOccurrence;
    }

    /**
     * Returns the end event occurrence attached to this graphNode
     *
     * @return the start event occurrence attached to the graphNode
     */
    public int getEndOccurrence() {
        return fEndEventOccurrence;
    }

    /**
     * Computes the index of the first visible GraphNode for each ordered graph node lists depending on the visible area
     * given in parameter
     *
     * @param x visible area top left corner x coordinate
     * @param y visible area top left corner y coordinate
     * @param width visible area width
     * @param height visible area height
     */
    public void updateIndex(int x, int y, int width, int height) {
        if (!fHasChilden) {
            return;
        }
        if(TmfUiTracer.isIndexTraced()) {
            TmfUiTracer.traceIndex("*****************************\n"); //$NON-NLS-1$
            TmfUiTracer.traceIndex("Visible area position in virtual screen (x,y)= " + x + " " + y + "\n\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        Iterator<String> it = fNodes.keySet().iterator();
        while (it.hasNext()) {
            String nodeType = it.next();
            int direction = 1;
            int drawIndex = fIndexes.get(nodeType).intValue();
            /*
             * if (x==0) { drawIndex = 0; indexes.put(nodeType,new Integer(drawIndex)); }
             */
            if ((fNodes.get(nodeType) != null) && (fNodes.get(nodeType).size() > 1)) {
                if (fNodes.get(nodeType).get(drawIndex).positiveDistanceToPoint(x, y)) {
                    direction = -1;
                }

                if (drawIndex == 0) {
                    direction = 1;
                }

                if ((direction == -1) && (fBackwardNodes.get(nodeType) != null)) {
                    GraphNode currentNode = fNodes.get(nodeType).get(drawIndex);
                    drawIndex = Arrays.binarySearch(fBackwardNodes.get(nodeType).toArray(new GraphNode[fBackwardNodes.get(nodeType).size()]),
                            fNodes.get(nodeType).get(drawIndex), currentNode.getBackComparator());
                    fNodes.put(nodeType, fBackwardNodes.get(nodeType));
                    if (drawIndex < 0) {
                        drawIndex = 0;
                        direction = 1;
                    } else {
                        fNodes.put(nodeType, fBackwardNodes.get(nodeType));
                    }
                }
                GraphNode prev = null;

                for (int i = drawIndex; i < fNodes.get(nodeType).size() && i >= 0; i = i + direction) {
                    drawIndex = i;
                    fIndexes.put(nodeType, Integer.valueOf(i));

                    GraphNode currentNode = fNodes.get(nodeType).get(i);

                    if (prev == null) {
                        prev = currentNode;
                    }

                    Comparator<GraphNode> comp = currentNode.getComparator();
                    Map<String, Boolean> sort = fForwardSort;

                    if ((direction == -1) && (currentNode.getBackComparator() != null)) {
                        comp = currentNode.getBackComparator();
                        sort = fBackwardSort;
                    }

                    if (i < fNodes.get(nodeType).size() - 1) {
                        GraphNode next = fNodes.get(nodeType).get(i + 1);

                        if ((comp != null) && (comp.compare(currentNode, next) > 0)) {
                            sort.put(nodeType, Boolean.TRUE);
                        }
                    }
                    if (direction == 1) {
                        if (fNodes.get(nodeType).get(i).positiveDistanceToPoint(x, y)) {
                            break;
                        }
                    } else {
                        if (currentNode.getBackComparator() == null) {
                            if // (currentNode.isVisible(x,y,width,height)
                            (!currentNode.positiveDistanceToPoint(x, y)) {
                                break;
                            }
                        } else {
                            if (currentNode.isVisible(x, y, width, height) && !currentNode.positiveDistanceToPoint(x, y)) {
                                if ((comp != null) && (comp.compare(currentNode, prev) <= 0)) {
                                    break;
                                }
                            } else if ((comp != null) && (comp.compare(currentNode, prev) <= 0)) {
                                prev = currentNode;
                            }
                        }
                    }
                }

                fNodes.put(nodeType, fForwardNodes.get(nodeType));
                if ((fBackwardNodes.get(nodeType) != null) && (direction == -1)) {
                    // nodes.put(nodeType,fnodes.get(nodeType));
                    int index = fIndexes.get(nodeType).intValue();
                    List<GraphNode> list = fNodes.get(nodeType);
                    List<GraphNode> backList = fBackwardNodes.get(nodeType);
                    GraphNode currentNode = (backList.get(index));
                    if (index > 0) {
                        index = Arrays.binarySearch(list.toArray(new GraphNode[list.size()]), backList.get(index), currentNode.getComparator());
                        if (index < 0) {
                            index = 0;
                        }
                        fIndexes.put(nodeType, Integer.valueOf(index));
                    }
                }

                for (int i = drawIndex; i < fNodes.get(nodeType).size() && i >= 0; i++) {
                    GraphNode toDraw = fNodes.get(nodeType).get(i);
                    toDraw.updateIndex(x, y, width, height);
                    if (!toDraw.isVisible(x, y, width, height)) {
                        break;
                    }
                }
            }
            if (TmfUiTracer.isIndexTraced()) {
                TmfUiTracer.traceIndex("First drawn " + nodeType + " index = " + drawIndex + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                TmfUiTracer.traceIndex(nodeType + " found in " + 0 + " iterations\n"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        if (TmfUiTracer.isIndexTraced()) {
            TmfUiTracer.traceIndex("*****************************\n"); //$NON-NLS-1$
        }
    }

    /**
     * Draws the children nodes on the given context.<br>
     * This method start width GraphNodes ordering if needed.<br>
     * After, depending on the visible area, only visible GraphNodes are drawn.<br>
     *
     * @param context the context to draw to
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#draw(IGC)
     */
    protected void drawChildenNodes(IGC context) {

        if (!fHasChilden) {
            return;
        }
        // If the nodes have not been added ordered, the array is ordered
        Iterator<String> it = fForwardSort.keySet().iterator();
        while (it.hasNext()) {
            String nodeType = it.next();
            boolean sort = fForwardSort.get(nodeType).booleanValue();
            if (sort) {
                GraphNode[] temp = fForwardNodes.get(nodeType).toArray(new GraphNode[fForwardNodes.get(nodeType).size()]);
                GraphNode node = fNodes.get(nodeType).get(0);
                Arrays.sort(temp, node.getComparator());
                fForwardSort.put(nodeType, Boolean.FALSE);
                fNodes.put(nodeType, Arrays.asList(temp));
                fForwardNodes.put(nodeType, Arrays.asList(temp));
                if (TmfUiTracer.isSortingTraced()) {
                    TmfUiTracer.traceSorting(nodeType + " array sorted\n"); //$NON-NLS-1$
                }
            }
        }

        Iterator<String> it2 = fBackwardSort.keySet().iterator();
        while (it2.hasNext()) {
            String nodeType = it2.next();
            boolean sort = fBackwardSort.get(nodeType).booleanValue();
            if (sort) {
                GraphNode[] temp = fBackwardNodes.get(nodeType).toArray(new GraphNode[fBackwardNodes.get(nodeType).size()]);
                GraphNode node = fNodes.get(nodeType).get(0);
                Arrays.sort(temp, node.getBackComparator());
                fBackwardSort.put(nodeType, Boolean.FALSE);
                fBackwardNodes.put(nodeType, Arrays.asList(temp));
                if (TmfUiTracer.isSortingTraced()) {
                    TmfUiTracer.traceSorting(nodeType + " back array sorted\n"); //$NON-NLS-1$
                }
            }
        }

        if (TmfUiTracer.isDisplayTraced()) {
            TmfUiTracer.traceDisplay("*****************************\n"); //$NON-NLS-1$
        }

        int arrayStep = 1;
        if ((Metrics.getMessageFontHeigth() + Metrics.MESSAGES_NAME_SPACING * 2) * context.getZoom() < Metrics.MESSAGE_SIGNIFICANT_VSPACING) {
            arrayStep = Math.round(Metrics.MESSAGE_SIGNIFICANT_VSPACING / ((Metrics.getMessageFontHeigth() + Metrics.MESSAGES_NAME_SPACING * 2) * context.getZoom()));
        }

        int count = 0;
        Iterator<String> it3 = fForwardSort.keySet().iterator();
        while (it3.hasNext()) {
            count = 0;
            Object nodeType = it3.next();
            GraphNode node = fNodes.get(nodeType).get(0);
            context.setFont(SDViewPref.getInstance().getFont(node.fPrefId));
            int index = fIndexes.get(nodeType).intValue();
            count = drawNodes(context, fNodes.get(nodeType), index, arrayStep);
            if (TmfUiTracer.isDisplayTraced()) {
                TmfUiTracer.traceDisplay(count + " " + nodeType + " drawn, starting from index " + index + "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
        if (TmfUiTracer.isDisplayTraced()) {
            TmfUiTracer.traceDisplay("*****************************\n"); //$NON-NLS-1$
        }

    }

    /**
     * Draw the GraphNode stored in the given list, starting at index startIndex with the given step
     *
     * @param context the context to draw to
     * @param list the GraphNodes list
     * @param startIndex the start index
     * @param step the step to browse the list
     * @return the number of GraphNodes drawn
     */
    protected int drawNodes(IGC context, List<GraphNode> list, int startIndex, int step) {
        if (!fHasChilden) {
            return 0;
        }

        GraphNode last = null;
        int nodesCount = 0;
        if (list.size() < 0) {
            return 0;
        }

        GraphNode node = list.get(0);
        context.setFont(SDViewPref.getInstance().getFont(node.fPrefId));
        Comparator<GraphNode> comparator = node.getComparator();
        for (int i = startIndex; i < list.size(); i = i + step) {
            GraphNode toDraw = list.get(i);
            if (i < list.size() - 1) {
                GraphNode next = list.get(i + 1);
                if ((comparator != null) && (comparator.compare(toDraw, next) > 0)) {
                    fForwardSort.put(next.getArrayId(), Boolean.TRUE);
                }
            }
            int cx = context.getContentsX();
            int cy = context.getContentsY();
            int cw = context.getVisibleWidth();
            int ch = context.getVisibleHeight();
            // The arrays should be ordered, no needs to continue for this one
            if (!toDraw.isVisible(cx, cy, cw, ch) && toDraw.positiveDistanceToPoint(cx + cw, cy + ch)) {
                break;
            }
            // ***Common*** nodes visibility
            if ((!toDraw.isSameAs(last) || toDraw.isSelected()) && (toDraw.isVisible(context.getContentsX(), context.getContentsY(), context.getVisibleWidth(), context.getVisibleHeight()))) {
                nodesCount++;

                toDraw.draw(context);
                if (hasFocus()) {
                    toDraw.drawFocus(context);
                }
            }
            last = toDraw;
        }
        return nodesCount;
    }

    /**
     * Draws the focus within the graphical context.
     *
     * @param context
     *            The context
     */
    public void drawFocus(IGC context) {
        context.drawFocus(getX(), getY(), getWidth(), getHeight());
    }

    /**
     * Determine if the given point (px,py) is contained in the rectangle (x,y,width,height)
     *
     * @param x the rectangle x coordinate
     * @param y the rectangle y coordinate
     * @param width the rectangle width
     * @param height the rectangle height
     * @param px the x coordinate of the point to test
     * @param py the y coordinate of the point to test
     * @return true if contained false otherwise
     */
    public static boolean contains(int x, int y, int width, int height, int px, int py) {
        int locX = x;
        int locY = y;
        int locWidth = width;
        int locHeight = height;

        if (width < 0) {
            locX = locX + width;
            locWidth = -locWidth;
        }

        if (height < 0) {
            locY = locY + height;
            locHeight = -locHeight;
        }
        return (px >= locX) && (py >= locY) && ((px - locX) <= locWidth) && ((py - locY) <= locHeight);
    }
}
