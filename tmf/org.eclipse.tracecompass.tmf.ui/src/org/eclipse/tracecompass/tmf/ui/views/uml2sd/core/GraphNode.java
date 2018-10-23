/**********************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.uml2sd.core;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tracecompass.internal.tmf.ui.TmfUiTracer;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.drawings.IGC;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.preferences.ISDPreferences;
import org.eclipse.tracecompass.tmf.ui.views.uml2sd.preferences.SDViewPref;

/**
 * The base class used for all UML2 graph nodes displayed in the Sequence Diagram SDWidget.
 *
 * @author sveyrier
 * @version 1.0
 */
public abstract class GraphNode {

    private static final String UI_DELIMITER = "*****************************\n"; //$NON-NLS-1$
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The start event occurrence.
     */
    private int fStartEventOccurrence = 0;
    /**
     * The event event occurrence.
     */
    private int fEndEventOccurrence = 0;
    /**
     * Preference ColorId to use to draw font
     */
    private String fPrefId = ISDPreferences.PREF_SYNC_MESS;
    /**
     * The selection state of the graph node.
     */
    private boolean fSelected = false;
    /**
     * The focus state of the graph node.
     */
    private boolean fFocused = false;
    /**
     * Flag to indicate whether node has children or not.
     */
    private boolean fHasChilden = false;
    /**
     * The graph node name used to label the graph node in the View.
     */
    private String fName = ""; //$NON-NLS-1$
    /**
     * A map from node name to graph node.
     */
    private Map<String, List<GraphNode>> fNodes;
    /**
     * A map from node name to graph node for forward sorting
     */
    private Map<String, List<GraphNode>> fForwardNodes;
    /**
     * A map from node name to graph node for backwards sorting.
     */
    private Map<String, List<GraphNode>> fBackwardNodes;
    /**
     * A map from node name to index.
     */
    private Map<String, Integer> fIndexes;
    /**
     * A map from node name to flag for forwards sorting.
     */
    private Map<String, Boolean> fForwardSort;
    /**
     * A map from node name to flag for backwards sorting.
     */
    private Map<String, Boolean> fBackwardSort;

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

        for (Map.Entry<String, Integer> entry : fIndexes.entrySet()) {
            entry.setValue(Integer.valueOf(0));
        }
    }

    /**
     * Add a GraphNode into the receiver
     *
     * @param nodeToAdd the node to add
     */
    public void addNode(GraphNode nodeToAdd) {
        if (!fHasChilden) {
            fNodes = new HashMap<>(2);
            fForwardNodes = new HashMap<>(2);
            fBackwardNodes = new HashMap<>(2);
            fIndexes = new HashMap<>(2);
            fBackwardSort = new HashMap<>(2);
            fForwardSort = new HashMap<>(2);
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
        if (fNodeList != null && !fNodeList.isEmpty()) {
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
            fNodeList = new ArrayList<>();
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
     * @see org.eclipse.tracecompass.tmf.ui.views.uml2sd.SDWidget#addSelection(GraphNode)
     * @see org.eclipse.tracecompass.tmf.ui.views.uml2sd.SDWidget#removeSelection(GraphNode)
     * @see org.eclipse.tracecompass.tmf.ui.views.uml2sd.SDWidget#clearSelection()
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
     * @see org.eclipse.tracecompass.tmf.ui.views.uml2sd.SDWidget#addSelection(GraphNode)
     * @see org.eclipse.tracecompass.tmf.ui.views.uml2sd.SDWidget#removeSelection(GraphNode)
     * @see org.eclipse.tracecompass.tmf.ui.views.uml2sd.SDWidget#clearSelection()
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
    public abstract boolean contains(int x, int y);

    /**
     * Returns the x coordinate of the graph node
     *
     * @return the x coordinate
     */
    public abstract int getX();

    /**
     * Returns the y coordinate of the graph node
     *
     * @return the y coordinate
     */
    public abstract int getY();

    /**
     * Returns the graph node height
     *
     * @return the graph node height
     */
    public abstract int getHeight();

    /**
     * Returns the graph node width
     *
     * @return the graph node width
     */
    public abstract int getWidth();

    /**
     * Draws the graph node in the given context
     *
     * @param context the graphical context to draw in
     */
    protected abstract void draw(IGC context);

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
    public abstract String getArrayId();

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

        GraphNode node = null;
        for (Map.Entry<String, List<GraphNode>> entry : fNodes.entrySet()) {
            List<GraphNode> list = entry.getValue();
            int index = checkNotNull(fIndexes.get(entry.getKey())).intValue();
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
        List<GraphNode> result = new ArrayList<>();

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

        for (Map.Entry<String, List<GraphNode>> entry : fNodes.entrySet()) {
            List<GraphNode> nodesList = entry.getValue();
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
            TmfUiTracer.traceIndex(UI_DELIMITER);
            TmfUiTracer.traceIndex("Visible area position in virtual screen (x,y)= " + x + " " + y + "\n\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        for (Map.Entry<String, List<GraphNode>> entry : fNodes.entrySet()) {
            String nodeType = entry.getKey();
            int direction = 1;
            int drawIndex = checkNotNull(fIndexes.get(nodeType)).intValue();
            if ((entry.getValue() != null) && (entry.getValue().size() > 1)) {
                if (entry.getValue().get(drawIndex).positiveDistanceToPoint(x, y)) {
                    direction = -1;
                }

                if (drawIndex == 0) {
                    direction = 1;
                }

                List<GraphNode> nodes = fBackwardNodes.get(nodeType);
                if ((direction == -1) && (nodes != null)) {
                    GraphNode currentNode = entry.getValue().get(drawIndex);
                    drawIndex = Arrays.binarySearch(nodes.toArray(new GraphNode[nodes.size()]),
                            entry.getValue().get(drawIndex), currentNode.getBackComparator());
                    entry.setValue(nodes);
                    if (drawIndex < 0) {
                        drawIndex = 0;
                        direction = 1;
                    } else {
                        entry.setValue(fBackwardNodes.get(nodeType));
                    }
                }
                GraphNode prev = null;

                for (int i = drawIndex; i < entry.getValue().size() && i >= 0; i = i + direction) {
                    drawIndex = i;
                    fIndexes.put(nodeType, Integer.valueOf(i));

                    GraphNode currentNode = entry.getValue().get(i);

                    if (prev == null) {
                        prev = currentNode;
                    }

                    Comparator<GraphNode> comp = currentNode.getComparator();
                    Map<String, Boolean> sort = fForwardSort;

                    if ((direction == -1) && (currentNode.getBackComparator() != null)) {
                        comp = currentNode.getBackComparator();
                        sort = fBackwardSort;
                    }

                    if (i < entry.getValue().size() - 1) {
                        GraphNode next = entry.getValue().get(i + 1);

                        if ((comp != null) && (comp.compare(currentNode, next) > 0)) {
                            sort.put(nodeType, Boolean.TRUE);
                        }
                    }
                    if (direction == 1) {
                        if (entry.getValue().get(i).positiveDistanceToPoint(x, y)) {
                            break;
                        }
                    } else {
                        if (currentNode.getBackComparator() == null) {
                            if (!currentNode.positiveDistanceToPoint(x, y)) {
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

                entry.setValue(fForwardNodes.get(nodeType));
                if ((fBackwardNodes.get(nodeType) != null) && (direction == -1)) {
                    int index = checkNotNull(fIndexes.get(nodeType)).intValue();
                    List<GraphNode> list = entry.getValue();
                    List<GraphNode> backList = checkNotNull(fBackwardNodes.get(nodeType));
                    GraphNode currentNode = (backList.get(index));
                    if (index > 0) {
                        index = Arrays.binarySearch(list.toArray(new GraphNode[list.size()]), backList.get(index), currentNode.getComparator());
                        if (index < 0) {
                            index = 0;
                        }
                        fIndexes.put(nodeType, Integer.valueOf(index));
                    }
                }

                for (int i = drawIndex; i < entry.getValue().size() && i >= 0; i++) {
                    GraphNode toDraw = entry.getValue().get(i);
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
            TmfUiTracer.traceIndex(UI_DELIMITER);
        }
    }

    /**
     * Draws the children nodes on the given context.<br>
     * This method start width GraphNodes ordering if needed.<br>
     * After, depending on the visible area, only visible GraphNodes are drawn.<br>
     *
     * @param context the context to draw to
     * @see org.eclipse.tracecompass.tmf.ui.views.uml2sd.core.GraphNode#draw(IGC)
     */
    protected void drawChildenNodes(IGC context) {

        if (!fHasChilden) {
            return;
        }
        // If the nodes have not been added ordered, the array is ordered
        for (Map.Entry<String, Boolean> entry : fForwardSort.entrySet()) {
            boolean sort = entry.getValue().booleanValue();
            if (sort) {
                sortNodes(fForwardNodes, entry, true);
            }
        }

        for (Map.Entry<String, Boolean> entry : fBackwardSort.entrySet()) {
            boolean sort = entry.getValue().booleanValue();
            if (sort) {
                sortNodes(fBackwardNodes, entry, false);
            }
        }

        if (TmfUiTracer.isDisplayTraced()) {
            TmfUiTracer.traceDisplay(UI_DELIMITER);
        }

        int arrayStep = 1;
        if ((Metrics.getMessageFontHeigth() + Metrics.MESSAGES_NAME_SPACING * 2) * context.getZoom() < Metrics.MESSAGE_SIGNIFICANT_VSPACING) {
            arrayStep = Math.round(Metrics.MESSAGE_SIGNIFICANT_VSPACING / ((Metrics.getMessageFontHeigth() + Metrics.MESSAGES_NAME_SPACING * 2) * context.getZoom()));
        }

        int count = 0;
        for (Map.Entry<String, Boolean> entry : fForwardSort.entrySet()) {
            count = 0;
            String nodeType = entry.getKey();
            GraphNode node = checkNotNull(fNodes.get(nodeType)).get(0);
            context.setFont(SDViewPref.getInstance().getFont(node.fPrefId));
            int index = checkNotNull(fIndexes.get(nodeType)).intValue();
            count = drawNodes(context, fNodes.get(nodeType), index, arrayStep);
            if (TmfUiTracer.isDisplayTraced()) {
                TmfUiTracer.traceDisplay(count + " " + nodeType + " drawn, starting from index " + index + "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
        if (TmfUiTracer.isDisplayTraced()) {
            TmfUiTracer.traceDisplay(UI_DELIMITER);
        }

    }

    private void sortNodes(Map<String, List<GraphNode>> nodesToSort, Map.Entry<String, Boolean> sortMapEntry, boolean forward) {
        String nodeType = sortMapEntry.getKey();
        List<GraphNode> temp = checkNotNull(nodesToSort.get(nodeType));
        GraphNode node = checkNotNull(fNodes.get(nodeType)).get(0);
        if (forward) {
            temp.sort(node.getComparator());
            fNodes.put(nodeType, temp);
        } else {
            temp.sort(node.getBackComparator());
        }
        nodesToSort.put(nodeType, temp);
        sortMapEntry.setValue(Boolean.FALSE);
        if (TmfUiTracer.isSortingTraced()) {
            TmfUiTracer.traceSorting(nodeType + " array sorted\n"); //$NON-NLS-1$
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
        if (list.isEmpty()) {
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

    /**
     * Sets the start event occurrence attached to this graphNode.
     *
     * @param occurence
     *          the start event occurrence attached to the graphNode
     */
    protected void setStartOccurrence(int occurence) {
        fStartEventOccurrence = occurence;
    }

    /**
     * Sets the end event occurrence attached to this graphNode
     *
     * @param occurence
     *          the start event occurrence attached to the graphNode
     */
    protected void setEndOccurrence(int occurence) {
        fEndEventOccurrence = occurence;
    }

    /**
     * Sets the color preference id
     * @param id
     *          The color preference id
     */
    protected void setColorPrefId(String id) {
        fPrefId = id;
    }

    /**
     * Gets the color preference id
     * @return the color preference id
     */
    protected String getColorPrefId() {
        return fPrefId;
    }

    /**
     * @return if node has children or not
     */
    protected boolean hasChildren() {
        return fHasChilden;
    }

    /**
     * Sets the flag indicating where the node has children or not.
     * @param hasChildren
     *          if node has children or not
     */
    protected void hasChildren(boolean hasChildren) {
        fHasChilden = hasChildren;
    }
    /**
     * Returns a map from node name to graph node.
     *
     * @return map with children graph bodes
     */
    protected Map<String, List<GraphNode>> getNodeMap() {
        return fNodes;
    }
    /**
     * Returns a map from node name to graph node for forward sorting
     *
     * @return forward sorting map
     */
    protected Map<String, List<GraphNode>> getForwardNodes() {
        return fForwardNodes;
    }
    /**
     * Returns a map from node name to graph node for backwards sorting.
     *
     * @return backwards sorting map
     */
    protected Map<String, List<GraphNode>> getBackwardNodes() {
        return fBackwardNodes;
    }
    /**
     * Returns a map from node name to index.
     *
     * @return map with node name to index
     */
    protected Map<String, Integer> getIndexes() {
        return fIndexes;
    }

    /**
     * Returns a map from node name to sort flag for forwards sorting.
     * @return a map from node name to sort flag
     */
    protected Map<String, Boolean> getForwardSortMap() {
        return fForwardSort;
    }
    /**
     * Returns a map from node name to flag for backwards sorting.
     * @return map from node name to flag for backwards sorting.
     */
    protected Map<String, Boolean> getBackwardSortMap() {
        return fBackwardSort;
    }
}
