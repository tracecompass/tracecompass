/**********************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.contexts.IContextIds;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.BaseMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.BasicExecutionOccurrence;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Frame;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.ITimeRange;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Lifeline;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Metrics;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.SDPrintDialog;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs.SDPrintDialogUI;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IColor;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDCollapseProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.load.LoadersManager;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.ISDPreferences;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.SDViewPref;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.ViewPart;

/**
 * <p>
 * This class implements sequence diagram widget used in the sequence diagram view.
 * </p>
 *
 * @version 1.0
 * @author sveyrier
 */
public class SDWidget extends ScrollView implements SelectionListener,
        IPropertyChangeListener, DisposeListener, ITimeCompressionListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The frame to display in the sequence diagram widget.
     */
    private Frame fFrame;
    /**
     * The overview image to display.
     */
    private Image fOverView = null;
    /**
     * The zoom in menu item.
     */
    private MenuItem fZoomIn = null;
    /**
     * The zoom out menu item.
     */
    private MenuItem fZoomOut = null;
    /**
     * The sequence diagram selection provider.
     */
    private SDWidgetSelectionProvider fSelProvider = null;
    /**
     * The current zoom value.
     */
    private float fZoomValue = 1;
    /**
     * The current zoomInMode (true for zoom in).
     */
    private boolean fZoomInMode = false;
    /**
     * The current zoomOutMode (true for zoom out).
     */
    private boolean fZoomOutMode = false;
    /**
     * The current list of selected graph nodes.
     */
    private List<GraphNode> fSelectedNodeList = null;
    /**
     * Flag whether ctrl button is selected or not.
     */
    private boolean fCtrlSelection = false;
    /**
     * A reference to the view site.
     */
    private ViewPart fSite = null;
    /**
     * The current graph node (the last selected one).
     */
    private GraphNode fCurrentGraphNode = null;
    /**
     * The first graph node in list (multiple selection).
     */
    private GraphNode fListStart = null;
    /**
     * The previous graph node (multiple selection).
     */
    private List<GraphNode> fPrevList = null;
    /**
     * The time compression bar.
     */
    private TimeCompressionBar fTimeBar = null;
    /**
     * The current diagram tool tip.
     */
    private DiagramToolTip fToolTip = null;
    /**
     * The accessible object reference of view control.
     */
    private Accessible fAccessible = null;
    /**
     * The current node for the tooltip to display.
     */
    private GraphNode fToolTipNode;
    /**
     * The life line to drag and drop.
     */
    private Lifeline fDragAndDrop = null;
    /**
     * The number of focused widgets.
     */
    private int fFocusedWidget = -1;
    /**
     * The printer zoom.
     */
    private float fPrinterZoom = 0;
    /**
     * Y coordinate for printer.
     */
    private int fPrinterY = 0;
    /**
     * X coordinate for printer.
     */
    private int fPrinterX = 0;
    /**
     * Flag whether drag and drop is enabled or not.
     */
    private boolean fIsDragAndDrop = false;
    /**
     * The x coordinate for drag.
     */
    private int fDragX = 0;
    /**
     * The y coordinate for drag.
     */
    private int fDragY = 0;
    /**
     * The reorder mode.
     */
    private boolean fReorderMode = false;
    /**
     * The collapse caret image.
     */
    private Image fCollapaseCaretImg = null;
    /**
     * The arrow up caret image.
     */
    private Image fArrowUpCaretImg = null;
    /**
     * The current caret image.
     */
    private Image fCurrentCaretImage = null;
    /**
     * A sequence diagramm collapse provider (for collapsing graph nodes)
     */
    private ISDCollapseProvider fCollapseProvider = null;
    /**
     * The insertion caret.
     */
    private Caret fInsertionCartet = null;
    /**
     * The reorder list when in reorder mode.
     */
    private List<Lifeline[]> fReorderList = null;
    /**
     * Flag to specify whether in printing mode or not.
     */
    private boolean fIsPrinting = false;
    /**
     * A printer reference.
     */
    private Printer fPrinter = null;
    /**
     * Flag whether shift was selected or not.
     */
    private boolean fShiftSelection = false;
    /**
     * The scroll tooltip.
     */
    private DiagramToolTip fScrollToolTip = null;
    /**
     * Timer for auto_scroll feature
     */
    private AutoScroll fLocalAutoScroll = null;
    /**
     * TimerTask for auto_scroll feature !=null when auto scroll is running
     */
    private Timer fLocalAutoScrollTimer = null;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Constructor for SDWidget.
     * @param c The parent composite
     * @param s The style
     */
    public SDWidget(Composite c, int s) {
        super(c, s | SWT.NO_BACKGROUND, true);
        setOverviewEnabled(true);
        fSelectedNodeList = new ArrayList<GraphNode>();
        fSelProvider = new SDWidgetSelectionProvider();
        SDViewPref.getInstance().addPropertyChangeListener(this);
        fToolTip = new DiagramToolTip(getViewControl());
        super.addDisposeListener(this);

        fScrollToolTip = new DiagramToolTip(c);
        getVerticalBar().addListener(SWT.MouseUp, new Listener() {

            @Override
            public void handleEvent(Event event) {
                fScrollToolTip.hideToolTip();
            }

        });
        fAccessible = getViewControl().getAccessible();

        fAccessible.addAccessibleListener(new AccessibleAdapter() {
            @Override
            public void getName(AccessibleEvent e) {
                // Case toolTip
                if (e.childID == 0) {
                    if (fToolTipNode != null) {
                        if (fToolTipNode instanceof Lifeline) {
                            Lifeline lifeline = (Lifeline) fToolTipNode;
                            e.result = lifeline.getToolTipText();
                        } else {
                            e.result = fToolTipNode.getName() + getPostfixForTooltip(true);
                        }
                    }
                } else {
                    if (getFocusNode() != null) {
                        if (getFocusNode() instanceof Lifeline) {
                            e.result = MessageFormat.format(Messages.SequenceDiagram_LifelineNode, new Object[] { String.valueOf(getFocusNode().getName()) });
                        }
                        if (getFocusNode() instanceof BaseMessage) {
                            BaseMessage mes = (BaseMessage) getFocusNode();
                            if ((mes.getStartLifeline() != null) && (mes.getEndLifeline() != null)) {
                                e.result = MessageFormat.format(
                                        Messages.SequenceDiagram_MessageNode,
                                        new Object[] { String.valueOf(mes.getName()), String.valueOf(mes.getStartLifeline().getName()), Integer.valueOf(mes.getStartOccurrence()), String.valueOf(mes.getEndLifeline().getName()),
                                                Integer.valueOf(mes.getEndOccurrence()) });
                            } else if ((mes.getStartLifeline() == null) && (mes.getEndLifeline() != null)) {
                                e.result = MessageFormat.format(Messages.SequenceDiagram_FoundMessageNode, new Object[] { String.valueOf(mes.getName()), String.valueOf(mes.getEndLifeline().getName()), Integer.valueOf(mes.getEndOccurrence()) });
                            } else if ((mes.getStartLifeline() != null) && (mes.getEndLifeline() == null)) {
                                e.result = MessageFormat.format(Messages.SequenceDiagram_LostMessageNode, new Object[] { String.valueOf(mes.getName()), String.valueOf(mes.getStartLifeline().getName()), Integer.valueOf(mes.getStartOccurrence()) });
                            }
                        } else if (getFocusNode() instanceof BasicExecutionOccurrence) {
                            BasicExecutionOccurrence exec = (BasicExecutionOccurrence) getFocusNode();
                            e.result = MessageFormat.format(Messages.SequenceDiagram_ExecutionOccurrenceWithParams,
                                    new Object[] { String.valueOf(exec.getName()), String.valueOf(exec.getLifeline().getName()), Integer.valueOf(exec.getStartOccurrence()), Integer.valueOf(exec.getEndOccurrence()) });
                        }

                    }
                }
            }
        });

        fAccessible.addAccessibleControlListener(new AccessibleControlAdapter() {
            @Override
            public void getFocus(AccessibleControlEvent e) {
                if (fFocusedWidget == -1) {
                    e.childID = ACC.CHILDID_SELF;
                } else {
                    e.childID = fFocusedWidget;
                }
            }

            @Override
            public void getRole(AccessibleControlEvent e) {
                switch (e.childID) {
                case ACC.CHILDID_SELF:
                    e.detail = ACC.ROLE_CLIENT_AREA;
                    break;
                case 0:
                    e.detail = ACC.ROLE_TOOLTIP;
                    break;
                case 1:
                    e.detail = ACC.ROLE_LABEL;
                    break;
                default:
                    break;
                }
            }

            @Override
            public void getState(AccessibleControlEvent e) {
                e.detail = ACC.STATE_FOCUSABLE;
                if (e.childID == ACC.CHILDID_SELF) {
                    e.detail |= ACC.STATE_FOCUSED;
                } else {
                    e.detail |= ACC.STATE_SELECTABLE;
                    if (e.childID == fFocusedWidget) {
                        e.detail |= ACC.STATE_FOCUSED | ACC.STATE_SELECTED | ACC.STATE_CHECKED;
                    }
                }
            }
        });

        fInsertionCartet = new Caret((Canvas) getViewControl(), SWT.NONE);
        fInsertionCartet.setVisible(false);

        fCollapaseCaretImg = Activator.getDefault().getImageFromPath(ITmfImageConstants.IMG_UI_ARROW_COLLAPSE_OBJ);
        fArrowUpCaretImg = Activator.getDefault().getImageFromPath(ITmfImageConstants.IMG_UI_ARROW_UP_OBJ);

        fReorderList = new ArrayList<Lifeline[]>();
        getViewControl().addTraverseListener(new LocalTraverseListener());

        addTraverseListener(new LocalTraverseListener());

        getViewControl().addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                SDViewPref.getInstance().setNoFocusSelection(false);
                fCtrlSelection = false;
                fShiftSelection = false;
                redraw();
            }

            @Override
            public void focusLost(FocusEvent e) {
                SDViewPref.getInstance().setNoFocusSelection(true);
                redraw();
            }
        });
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Sets the time compression bar.
     *
     * @param bar The time compression bar to set
     */
    public void setTimeBar(TimeCompressionBar bar) {
        if (bar != null) {
            fTimeBar = bar;
            fTimeBar.addTimeCompressionListener(this);
        }
    }

    /**
     * Resize the contents to insure the frame fit into the view
     *
     * @param frame the frame which will be drawn in the view
     */
    public void resizeContents(Frame frame) {
        int width = Math.round((frame.getWidth() + 2 * Metrics.FRAME_H_MARGIN) * fZoomValue);
        int height = Math.round((frame.getHeight() + 2 * Metrics.FRAME_V_MARGIN) * fZoomValue);
        resizeContents(width, height);
    }

    /**
     * The frame to render (the sequence diagram)
     *
     * @param theFrame the frame to display
     * @param resetPosition boolean
     */
    public void setFrame(Frame theFrame, boolean resetPosition) {
        fReorderList.clear();
        fSelectedNodeList.clear();
        fSelProvider.setSelection(new StructuredSelection());
        fFrame = theFrame;
        if (resetPosition) {
            setContentsPos(0, 0);
            resizeContents(fFrame);
            redraw();
        }
        // prepare the old overview to be reused
        if (fOverView != null) {
            fOverView.dispose();
        }
        fOverView = null;
        resizeContents(fFrame);
    }

    /**
     * Returns the current Frame (the sequence diagram container)
     *
     * @return the frame
     */
    public Frame getFrame() {
        return fFrame;
    }

    /**
     * Returns the selection provider for the current sequence diagram
     *
     * @return the selection provider
     */
    public ISelectionProvider getSelectionProvider() {
        return fSelProvider;
    }

    /**
     * Returns a list of selected graph nodes.
     *
     * @return a list of selected graph nodes.
     */
    public List<GraphNode> getSelection() {
        return fSelectedNodeList;
    }

    /**
     * Adds a graph node to the selected nodes list.
     *
     * @param node A graph node
     */
    public void addSelection(GraphNode node) {
        if (node == null) {
            return;
        }
        fSelectedNodeList.add(node);
        node.setSelected(true);
        fCurrentGraphNode = node;
        StructuredSelection selection = new StructuredSelection(fSelectedNodeList);
        fSelProvider.setSelection(selection);
    }

    /**
     * Adds a list of node to the selected nodes list.
     *
     * @param list of graph nodes
     */
    public void addSelection(List<GraphNode> list) {
        for (int i = 0; i < list.size(); i++) {
            if (!fSelectedNodeList.contains(list.get(i))) {
                fSelectedNodeList.add(list.get(i));
                list.get(i).setSelected(true);
            }
        }
        StructuredSelection selection = new StructuredSelection(fSelectedNodeList);
        fSelProvider.setSelection(selection);
    }

    /**
     * Removes a node from the selected nodes list.
     *
     * @param node to remove
     */
    public void removeSelection(GraphNode node) {
        fSelectedNodeList.remove(node);
        node.setSelected(false);
        node.setFocused(false);
        StructuredSelection selection = new StructuredSelection(fSelectedNodeList);
        fSelProvider.setSelection(selection);
    }

    /**
     * Removes a list of graph nodes from the selected nodes list.
     *
     * @param list of nodes to remove.
     */
    public void removeSelection(List<GraphNode> list) {
        fSelectedNodeList.removeAll(list);
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setSelected(false);
            list.get(i).setFocused(false);
        }
        StructuredSelection selection = new StructuredSelection(fSelectedNodeList);
        fSelProvider.setSelection(selection);
    }

    /**
     * Clear the list of GraphNodes which must be drawn selected.
     */
    public void clearSelection() {
        for (int i = 0; i < fSelectedNodeList.size(); i++) {
            fSelectedNodeList.get(i).setSelected(false);
            fSelectedNodeList.get(i).setFocused(false);
        }
        fCurrentGraphNode = null;
        fSelectedNodeList.clear();
        fSelProvider.setSelection(new StructuredSelection());
    }

    /**
     * Sets view part.
     *
     * @param viewSite The view part to set
     */
    public void setSite(ViewPart viewSite) {
        fSite = viewSite;
        fSite.getSite().setSelectionProvider(fSelProvider);
        IContextService service = (IContextService) fSite.getSite().getWorkbenchWindow().getService(IContextService.class);
        service.activateContext("org.eclipse.linuxtools.tmf.ui.view.uml2sd.context"); //$NON-NLS-1$
        service.activateContext(IContextIds.CONTEXT_ID_WINDOW);
    }

    /**
     * Returns the GraphNode overView the mouse if any
     *
     * @return the current graph node
     * */
    public GraphNode getMouseOverNode() {
        return fCurrentGraphNode;
    }

    /**
     * Sets the zoom in mode.
     *
     * @param value
     *            The mode value to set.
     */
    public void setZoomInMode(boolean value) {
        if (value) {
            setZoomOutMode(false);
        }
        fZoomInMode = value;
    }

    /**
     * Sets the zoom out mode.
     *
     * @param value
     *          The mode value to set.
     */
    public void setZoomOutMode(boolean value) {
        if (value) {
            setZoomInMode(false);
        }
        fZoomOutMode = value;
    }

    /**
     * Sets the current zoom value.
     *
     * @param zoomValue
     *          The current zoom value
     * @since 2.0
     */
    public void setZoomValue(float zoomValue) {
        fZoomValue = zoomValue;
    }

    /**
     * Moves the Sequence diagram to ensure the given node is visible and draw it selected
     *
     * @param node the GraphNode to move to
     */
    public void moveTo(GraphNode node) {
        if (node == null) {
            return;
        }
        clearSelection();
        addSelection(node);
        ensureVisible(node);
    }

    /**
     * Moves the Sequence diagram to ensure the given node is visible
     *
     * @param node the GraphNode to move to
     */
    public void ensureVisible(GraphNode node) {
        if (node == null) {
            return;
        }
        int x = Math.round(node.getX() * fZoomValue);
        int y = Math.round(node.getY() * fZoomValue);
        int width = Math.round(node.getWidth() * fZoomValue);
        int height = Math.round(node.getHeight() * fZoomValue);
        if ((node instanceof BaseMessage) && (height == 0)) {
            int header = Metrics.LIFELINE_HEARDER_TEXT_V_MARGIN * 2 + Metrics.getLifelineHeaderFontHeigth();
            height = -Math.round((Metrics.getMessagesSpacing() + header) * fZoomValue);
            y = y + Math.round(Metrics.SYNC_INTERNAL_MESSAGE_HEIGHT * fZoomValue);
        }
        if (node instanceof BasicExecutionOccurrence) {
            width = 1;
            height = 1;
        }
        if (node instanceof Lifeline) {
            y = getContentsY();
            height = getVisibleHeight();
        }
        ensureVisible(x, y, width, height, SWT.CENTER, true);
        redraw();
    }

    /**
     * Returns the current zoom factor.
     * @return the current zoom factor.
     */
    public float getZoomFactor() {
        return fZoomValue;
    }

    /**
     * Returns teh printer reference.
     *
     * @return the printer reference
     */
    public Printer getPrinter() {
        return fPrinter;
    }

    /**
     * Returns whether the widget is used for printing or not.
     *
     * @return whether the widget is used for printing or not
     */
    public boolean isPrinting() {
        return fIsPrinting;
    }

    /**
     * Returns the current graph node.
     *
     * @return the current graph node
     * @since 2.0
     */
    public GraphNode getCurrentGraphNode() {
        return fCurrentGraphNode;
    }

    /**
     * Returns the current zoom value.
     *
     * @return the current zoom value
     * @since 2.0
     */
    public float getZoomValue() {
        return fZoomValue;
    }

    /**
     * Gets the zoom in mode.
     *
     * @return the mode value to set.
     * @since 2.0
     */
    public boolean getZoomInMode() {
        return fZoomInMode;
    }


    /**
     * Gets the zoom out mode.
     *
     * @return the mode value to set.
     * @since 2.0
     */
    public boolean getZoomOutMode() {
        return fZoomOutMode;
    }

    /**
     * Returns if ctrl selection
     * @return true if ctrl selection else false
     * @since 2.0
     */
    public boolean isCtrlSelection() {
        return fCtrlSelection;
    }

    /**
     * Returns if shift selection
     * @return true if shift Selection else false
     * @since 2.0
     */
    public boolean isShiftSelection() {
        return fCtrlSelection;
    }

    /**
     * Gets the overview image.
     *
     * @param rect Rectangle to include overview.
     * @return the overview image
     */
    public Image getOverview(Rectangle rect) {
        float oldzoom = fZoomValue;
        if ((fOverView != null) && ((rect.width != fOverView.getBounds().width) || (rect.height != fOverView.getBounds().height))) {
            fOverView.dispose();
            fOverView = null;
        }
        if (fOverView == null) {
            int backX = getContentsX();
            int backY = getContentsY();
            setContentsPos(0, 0);
            fOverView = new Image(getDisplay(), rect.width, rect.height);
            GC gcim = new GC(fOverView);
            NGC context = new NGC(this, gcim);
            context.setBackground(SDViewPref.getInstance().getBackGroundColor(ISDPreferences.PREF_FRAME));
            fFrame.draw(context);
            setContentsPos(backX, backY);
            gcim.dispose();
            context.dispose();
        }
        fZoomValue = oldzoom;
        return fOverView;
    }

    /**
     * Resets the zoom factor.
     */
    public void resetZoomFactor() {
        int currentX = Math.round(getContentsX() / fZoomValue);
        int currentY = Math.round(getContentsY() / fZoomValue);
        fZoomValue = 1;
        if (fTimeBar != null && !fTimeBar.isDisposed()) {
            fTimeBar.setZoom(fZoomValue);
        }
        redraw();
        update();
        setContentsPos(currentX, currentY);
    }

    /**
     * Enable or disable the lifeline reodering using Drag and Drop
     *
     * @param mode - true to enable false otherwise
     */
    public void setReorderMode(boolean mode) {
        fReorderMode = mode;
    }

    /**
     * Return the lifelines reorder sequence (using Drag and Drop) if the the reorder mode is turn on. Each ArryList
     * element is of type Lifeline[2] with Lifeline[0] inserted before Lifeline[1] in the diagram
     *
     * @return - the re-odered sequence
     */
    public List<Lifeline[]> getLifelineReoderList() {
        return fReorderList;
    }

    /**
     * Sets the focus on given graph node (current node).
     *
     * @param node
     *            The graph node to focus on.
     */
    public void setFocus(GraphNode node) {
        if (node == null) {
            return;
        }
        if (fCurrentGraphNode != null) {
            fCurrentGraphNode.setFocused(false);
        }
        fCurrentGraphNode = node;
        node.setFocused(true);
        ensureVisible(node);
        setFocus(0);
    }

    /**
     * Returns the graph node focused on.
     *
     * @return the current graph node
     */
    public GraphNode getFocusNode() {
        return fCurrentGraphNode;
    }

    /**
     * Method to traverse right.
     */
    public void traverseRight() {
        Object selectedNode = getFocusNode();
        if (selectedNode == null) {
            traverseLeft();
        }
        GraphNode node = null;
        if ((selectedNode instanceof BaseMessage) && (((BaseMessage) selectedNode).getEndLifeline() != null)) {
            node = fFrame.getCalledMessage((BaseMessage) selectedNode);
        }
        if (selectedNode instanceof BasicExecutionOccurrence) {
            selectedNode = ((BasicExecutionOccurrence) selectedNode).getLifeline();
        }
        if ((node == null) && (selectedNode instanceof Lifeline)) {
            for (int i = 0; i < fFrame.lifeLinesCount(); i++) {
                if ((selectedNode == fFrame.getLifeline(i)) && (i < fFrame.lifeLinesCount() - 1)) {
                    node = fFrame.getLifeline(i + 1);
                    break;
                }
            }
        }
        if (node != null) {
            setFocus(node);
            redraw();
        }
    }

    /**
     * Method to traverse left.
     */
    public void traverseLeft() {
        Object selectedNode = getFocusNode();
        GraphNode node = null;
        if ((selectedNode instanceof BaseMessage) && (((BaseMessage) selectedNode).getStartLifeline() != null)) {
            node = fFrame.getCallerMessage((BaseMessage) selectedNode);
        }
        if (selectedNode instanceof BasicExecutionOccurrence) {
            selectedNode = ((BasicExecutionOccurrence) selectedNode).getLifeline();
        }
        if (node == null) {
            if ((selectedNode instanceof BaseMessage) && (((BaseMessage) selectedNode).getEndLifeline() != null)) {
                selectedNode = ((BaseMessage) selectedNode).getEndLifeline();
            }
            for (int i = 0; i < fFrame.lifeLinesCount(); i++) {
                if ((selectedNode == fFrame.getLifeline(i)) && (i > 0)) {
                    node = fFrame.getLifeline(i - 1);
                    break;
                }
            }
            if ((fFrame.lifeLinesCount() > 0) && (node == null)) {
                node = fFrame.getLifeline(0);
            }
        }
        if (node != null) {
            setFocus(node);
            redraw();
        }
    }

    /**
     * Method to traverse up.
     */
    public void traverseUp() {
        Object selectedNode = getFocusNode();
        if (selectedNode == null) {
            traverseLeft();
        }
        GraphNode node = null;
        if (selectedNode instanceof BaseMessage) {
            node = fFrame.getPrevLifelineMessage(((BaseMessage) selectedNode).getStartLifeline(), (BaseMessage) selectedNode);
        } else if (selectedNode instanceof Lifeline) {
            node = fFrame.getPrevLifelineMessage((Lifeline) selectedNode, null);
            if (!(node instanceof Lifeline)) {
                node = null;
            }
        } else if (selectedNode instanceof BasicExecutionOccurrence) {
            node = fFrame.getPrevExecOccurrence((BasicExecutionOccurrence) selectedNode);
            if (node == null) {
                node = ((BasicExecutionOccurrence) selectedNode).getLifeline();
            }
        }
        if ((node == null) && (selectedNode instanceof BaseMessage) && (((BaseMessage) selectedNode).getStartLifeline() != null)) {
            node = ((BaseMessage) selectedNode).getStartLifeline();
        }

        if (node != null) {
            setFocus(node);
            redraw();
        }
    }

    /**
     * Method to traverse down.
     */
    public void traverseDown() {
        Object selectedNode = getFocusNode();
        if (selectedNode == null) {
            traverseLeft();
        }
        GraphNode node;
        if (selectedNode instanceof BaseMessage) {
            node = fFrame.getNextLifelineMessage(((BaseMessage) selectedNode).getStartLifeline(), (BaseMessage) selectedNode);
        } else if (selectedNode instanceof Lifeline) {
            node = fFrame.getFirstExecution((Lifeline) selectedNode);
        } else if (selectedNode instanceof BasicExecutionOccurrence) {
            node = fFrame.getNextExecOccurrence((BasicExecutionOccurrence) selectedNode);
        } else {
            return;
        }

        if (node != null) {
            setFocus(node);
            redraw();
        }
    }

    /**
     * Method to traverse home.
     */
    public void traverseHome() {
        Object selectedNode = getFocusNode();
        if (selectedNode == null) {
            traverseLeft();
        }
        GraphNode node = null;

        if (selectedNode instanceof BaseMessage) {
            if (((BaseMessage) selectedNode).getStartLifeline() != null) {
                node = fFrame.getNextLifelineMessage(((BaseMessage) selectedNode).getStartLifeline(), null);
            } else {
                node = fFrame.getNextLifelineMessage(((BaseMessage) selectedNode).getEndLifeline(), null);
            }
        } else if (selectedNode instanceof Lifeline) {
            node = fFrame.getNextLifelineMessage((Lifeline) selectedNode, null);
        } else if (selectedNode instanceof BasicExecutionOccurrence) {
            node = fFrame.getFirstExecution(((BasicExecutionOccurrence) selectedNode).getLifeline());
        } else {
            if (fFrame.lifeLinesCount() > 0) {
                Lifeline lifeline = fFrame.getLifeline(0);
                node = fFrame.getNextLifelineMessage(lifeline, null);
            }
        }

        if (node != null) {
            setFocus(node);
            redraw();
        }
    }

    /**
     * Method to traverse to the end.
     */
    public void traverseEnd() {
        Object selectedNode = getFocusNode();
        if (selectedNode == null) {
            traverseLeft();
        }
        GraphNode node;
        if (selectedNode instanceof BaseMessage) {
            node = fFrame.getPrevLifelineMessage(((BaseMessage) selectedNode).getStartLifeline(), null);
        } else if (selectedNode instanceof Lifeline) {
            node = fFrame.getPrevLifelineMessage((Lifeline) selectedNode, null);
        } else if (selectedNode instanceof BasicExecutionOccurrence) {
            node = fFrame.getLastExecOccurrence(((BasicExecutionOccurrence) selectedNode).getLifeline());
        } else {
            if (fFrame.lifeLinesCount() > 0) {
                Lifeline lifeline = fFrame.getLifeline(0);
                node = fFrame.getPrevLifelineMessage(lifeline, null);
            } else {
                return;
            }
        }

        if (node != null) {
            setFocus(node);
            redraw();
        }
    }

    /**
     * Method to print UI.
     *
     * @param sdPrintDialog the sequence diagram printer dialog.
     */
    public void printUI(SDPrintDialogUI sdPrintDialog) {
        PrinterData data = sdPrintDialog.getPrinterData();

        if ((data == null) || (fFrame == null)) {
            return;
        }

        fPrinter = new Printer(data);

        String jobName = MessageFormat.format(Messages.SequenceDiagram_plus, new Object[] { String.valueOf(fSite.getContentDescription()), String.valueOf(fFrame.getName()) });
        fPrinter.startJob(jobName);

        GC gc = new GC(fPrinter);

        float lastZoom = fZoomValue;

        Rectangle area = getClientArea();
        GC gcim = null;

        gcim = gc;
        NGC context = new NGC(this, gcim);

        // Set the metrics to use for lifeline text and message text
        // using the Graphical Context
        Metrics.setLifelineFontHeight(context.getFontHeight(SDViewPref.getInstance().getFont(ISDPreferences.PREF_LIFELINE)));
        Metrics.setLifelineFontWidth(context.getFontWidth(SDViewPref.getInstance().getFont(ISDPreferences.PREF_LIFELINE)));
        Metrics.setLifelineWidth(SDViewPref.getInstance().getLifelineWidth());
        Metrics.setFrameFontHeight(context.getFontHeight(SDViewPref.getInstance().getFont(ISDPreferences.PREF_FRAME_NAME)));
        Metrics.setLifelineHeaderFontHeight(context.getFontHeight(SDViewPref.getInstance().getFont(ISDPreferences.PREF_LIFELINE_HEADER)));

        int syncMessFontH = context.getFontHeight(SDViewPref.getInstance().getFont(ISDPreferences.PREF_SYNC_MESS));
        int syncMessRetFontH = context.getFontHeight(SDViewPref.getInstance().getFont(ISDPreferences.PREF_SYNC_MESS_RET));
        int asyncMessFontH = context.getFontHeight(SDViewPref.getInstance().getFont(ISDPreferences.PREF_ASYNC_MESS));
        int asyncMessRetFontH = context.getFontHeight(SDViewPref.getInstance().getFont(ISDPreferences.PREF_ASYNC_MESS_RET));

        int messageFontHeight = 0;
        if (syncMessFontH > syncMessRetFontH) {
            messageFontHeight = syncMessFontH;
        } else {
            messageFontHeight = syncMessRetFontH;
        }
        if (messageFontHeight < asyncMessFontH) {
            messageFontHeight = asyncMessFontH;
        }
        if (messageFontHeight < asyncMessRetFontH) {
            messageFontHeight = asyncMessRetFontH;
        }
        Metrics.setMessageFontHeight(messageFontHeight);
        context.setFont(SDViewPref.getInstance().getFont(ISDPreferences.PREF_LIFELINE));

        int width = Math.round((fFrame.getWidth() + 2 * Metrics.FRAME_H_MARGIN) * fZoomValue);
        int height = Math.round((fFrame.getHeight() + 2 * Metrics.FRAME_V_MARGIN) * fZoomValue);
        if (width < area.width) {
            width = area.width;
        }
        if (height < area.height) {
            height = area.height;
        }
        resizeContents(width, height);

        context.setBackground(SDViewPref.getInstance().getBackGroundColor(ISDPreferences.PREF_FRAME));
        context.fillRectangle(0, 0, getContentsWidth(), Metrics.FRAME_V_MARGIN);
        context.fillRectangle(0, 0, fFrame.getX(), getContentsHeight());
        context.fillRectangle(fFrame.getX() + fFrame.getWidth() + 1, 0, getContentsWidth() - (fFrame.getX() + fFrame.getWidth() + 1), getContentsHeight());
        context.fillRectangle(0, fFrame.getY() + fFrame.getHeight() + 1, getContentsWidth(), getContentsHeight() - (fFrame.getY() + fFrame.getHeight() + 1));
        gcim.setLineWidth(1);

        fPrinter.startPage();
        fZoomValue = lastZoom;

        int restoreX = getContentsX();
        int restoreY = getContentsY();

        float zh = sdPrintDialog.getStepY() * sdPrintDialog.getZoomFactor();
        float zw = sdPrintDialog.getStepX() * sdPrintDialog.getZoomFactor();

        float zoomValueH = fPrinter.getClientArea().height / zh;
        float zoomValueW = fPrinter.getClientArea().width / zw;
        if (zoomValueH > zoomValueW) {
            fPrinterZoom = zoomValueH;
        } else {
            fPrinterZoom = zoomValueW;
        }

        if (sdPrintDialog.printSelection()) {
            int[] pagesList = sdPrintDialog.getPageList();

            for (int pageIndex = 0; pageIndex < pagesList.length; pageIndex++) {
                printPage(pagesList[pageIndex], sdPrintDialog, context);
            }
        } else if (sdPrintDialog.printAll()) {
            for (int pageIndex = 1; pageIndex <= sdPrintDialog.maxNumOfPages(); pageIndex++) {
                printPage(pageIndex, sdPrintDialog, context);
            }
        } else if (sdPrintDialog.printCurrent()) {
            printPage(getContentsX(), getContentsY(), sdPrintDialog, context, 1);
        } else if (sdPrintDialog.printRange()) {
            for (int pageIndex = sdPrintDialog.getFrom(); pageIndex <= sdPrintDialog.maxNumOfPages() && pageIndex <= sdPrintDialog.getTo(); pageIndex++) {
                printPage(pageIndex, sdPrintDialog, context);
            }
        }

        fPrinter.endJob();
        fIsPrinting = false;

        gc.dispose();
        context.dispose();

        fZoomValue = lastZoom;
        fPrinter.dispose();
        setContentsPos(restoreX, restoreY);
    }

    /**
     * Method to print.
     */
    public void print() {
        SDPrintDialog sdPrinter = new SDPrintDialog(this.getShell(), this);
        try {
            if (sdPrinter.open() != 0) {
                return;
            }
        } catch (Exception e) {
            Activator.getDefault().logError("Error creating image", e); //$NON-NLS-1$
            return;
        }
        printUI(sdPrinter.getDialogUI());
    }

    /**
     * Method to print a page.
     *
     * @param pageNum The page number
     * @param pd The sequence diagram print dialog
     * @param context The graphical context
     */
    public void printPage(int pageNum, SDPrintDialogUI pd, NGC context) {
        int j = pageNum / pd.getNbRow();
        int i = pageNum % pd.getNbRow();
        if (i != 0) {
            j++;
        } else {
            i = pd.getNbRow();
        }

        i--;
        j--;

        i = (int) (i * pd.getStepX());
        j = (int) (j * pd.getStepY());

        printPage(i, j, pd, context, pageNum);

        fPrinter.endPage();
    }

    /**
     * Method to print page ranges.
     *
     * @param i
     *            The start page
     * @param j
     *            The end page
     * @param pd
     *            The sequence diagram print dialog
     * @param context
     *            The graphical context
     * @param pageNum
     *            The current page
     */
    public void printPage(int i, int j, SDPrintDialogUI pd, NGC context, int pageNum) {
        fIsPrinting = false;
        int pageNumFontZoom = fPrinter.getClientArea().height / getVisibleHeight();
        fPrinterX = i;
        fPrinterY = j;
        setContentsPos(i, j);
        update();
        fIsPrinting = true;
        float lastZoom = fZoomValue;
        fZoomValue = fPrinterZoom * lastZoom;

        fFrame.draw(context);

        fZoomValue = pageNumFontZoom;
        context.setFont(SDViewPref.getInstance().getFont(ISDPreferences.PREF_LIFELINE));
        String currentPageNum = String.valueOf(pageNum);
        int ii = context.textExtent(currentPageNum);
        int jj = context.getCurrentFontHeight();
        fZoomValue = fPrinterZoom * lastZoom;
        context.drawText(currentPageNum, Math.round(fPrinterX + getVisibleWidth() / fPrinterZoom - ii / fPrinterZoom), Math.round(fPrinterY + getVisibleHeight() / fPrinterZoom - jj / fPrinterZoom), false);
        fIsPrinting = false;
        fZoomValue = lastZoom;
    }

    /**
     * Sets the collapse provider.
     *
     * @param provider The collapse provider to set
     */
    protected void setCollapseProvider(ISDCollapseProvider provider) {
        fCollapseProvider = provider;
    }


    /**
     * Checks for focus of children.
     *
     * @param children Control to check
     * @return true if child is on focus else false
     */
    protected boolean checkFocusOnChilds(Control children) {
        if (children instanceof Composite) {
            Control[] child = ((Composite) children).getChildren();
            for (int i = 0; i < child.length; i++) {
                if (child[i].isFocusControl()) {
                    return true;
                }
                checkFocusOnChilds(child[i]);
            }
        }
        return false;
    }

    /**
     * A post action for a tooltip (before displaying).
     *
     * @param accessible true if accessible else false
     * @return the tooltip text.
     */
    protected String getPostfixForTooltip(boolean accessible) {
        StringBuffer postfix = new StringBuffer();
        // Determine if the tooltip must show the time difference between the current mouse position and
        // the last selected graphNode
        if ((fCurrentGraphNode != null) &&
                (fCurrentGraphNode instanceof ITimeRange) &&
                (fToolTipNode instanceof ITimeRange) &&
                (fCurrentGraphNode != fToolTipNode) &&
                ((ITimeRange) fToolTipNode).hasTimeInfo() &&
                ((ITimeRange) fCurrentGraphNode).hasTimeInfo()) {
            postfix.append(" -> "); //$NON-NLS-1$
            postfix.append(fCurrentGraphNode.getName());
            postfix.append("\n"); //$NON-NLS-1$
            postfix.append(Messages.SequenceDiagram_Delta);
            postfix.append(" "); //$NON-NLS-1$

            //double delta = ((ITimeRange)toolTipNode).getLastTime()-((ITimeRange)currentGraphNode).getLastTime();
            ITmfTimestamp firstTime = ((ITimeRange) fCurrentGraphNode).getEndTime();
            ITmfTimestamp lastTime = ((ITimeRange) fToolTipNode).getEndTime();
            ITmfTimestamp delta =  lastTime.getDelta(firstTime);
            postfix.append(delta.toString());

        } else {
            if ((fToolTipNode instanceof ITimeRange) && ((ITimeRange) fToolTipNode).hasTimeInfo()) {
                postfix.append("\n"); //$NON-NLS-1$
                ITmfTimestamp firstTime = ((ITimeRange) fToolTipNode).getStartTime();
                ITmfTimestamp lastTime = ((ITimeRange) fToolTipNode).getEndTime();

                if (firstTime != null) {
                    if (lastTime != null && firstTime.compareTo(lastTime, true) != 0) {
                        postfix.append("start: "); //$NON-NLS-1$
                        postfix.append(firstTime.toString());
                        postfix.append("\n"); //$NON-NLS-1$
                        postfix.append("end: "); //$NON-NLS-1$
                        postfix.append(lastTime.toString());
                        postfix.append("\n"); //$NON-NLS-1$
                        } else {
                            postfix.append(firstTime.toString());
                        }
                    }
                else if (lastTime != null) {
                    postfix.append(lastTime.toString());
                }
            }
        }
        return postfix.toString();
    }

    /**
     * Sets a new focused widget.
     *
     * @param newFocusShape A new focus shape.
     */
    protected void setFocus(int newFocusShape) {
        fFocusedWidget = newFocusShape;
        if (fFocusedWidget == -1) {
            getViewControl().getAccessible().setFocus(ACC.CHILDID_SELF);
        } else {
            getViewControl().getAccessible().setFocus(fFocusedWidget);
        }
    }

    /**
     * Highlight the given GraphNode<br>
     * The GraphNode is then displayed using the system default selection color
     *
     * @param node the GraphNode to highlight
     */
    protected void performSelection(GraphNode node) {
        if ((fCtrlSelection) || (fShiftSelection)) {
            if (node != null) {
                if (fSelectedNodeList.contains(node)) {
                    removeSelection(node);
                } else {
                    addSelection(node);
                }
            } else {
                return;
            }
        } else {
            clearSelection();
            if (node != null) {
                addSelection(node);
            }
        }
    }

    /**
     * Returns a draw buffer image.
     *
     * @return a Image containing the draw buffer.
     */
    protected Image getDrawBuffer() {

        update();
        Rectangle area = getClientArea();
        Image dbuffer = new Image(getDisplay(), area.width, area.height);
        GC gcim = new GC(dbuffer);
        NGC context = new NGC(this, gcim);

        // Set the metrics to use for lifeline text and message text
        // using the Graphical Context
        Metrics.setLifelineFontHeight(context.getFontHeight(SDViewPref.getInstance().getFont(ISDPreferences.PREF_LIFELINE)));
        Metrics.setLifelineFontWidth(context.getFontWidth(SDViewPref.getInstance().getFont(ISDPreferences.PREF_LIFELINE)));
        Metrics.setLifelineWidth(SDViewPref.getInstance().getLifelineWidth());
        Metrics.setFrameFontHeight(context.getFontHeight(SDViewPref.getInstance().getFont(ISDPreferences.PREF_FRAME_NAME)));
        Metrics.setLifelineHeaderFontHeight(context.getFontHeight(SDViewPref.getInstance().getFont(ISDPreferences.PREF_LIFELINE_HEADER)));

        int syncMessFontH = context.getFontHeight(SDViewPref.getInstance().getFont(ISDPreferences.PREF_SYNC_MESS));
        int syncMessRetFontH = context.getFontHeight(SDViewPref.getInstance().getFont(ISDPreferences.PREF_SYNC_MESS_RET));
        int asyncMessFontH = context.getFontHeight(SDViewPref.getInstance().getFont(ISDPreferences.PREF_ASYNC_MESS));
        int asyncMessRetFontH = context.getFontHeight(SDViewPref.getInstance().getFont(ISDPreferences.PREF_ASYNC_MESS_RET));

        int messageFontHeight = 0;
        if (syncMessFontH > syncMessRetFontH) {
            messageFontHeight = syncMessFontH;
        } else {
            messageFontHeight = syncMessRetFontH;
        }
        if (messageFontHeight < asyncMessFontH) {
            messageFontHeight = asyncMessFontH;
        }
        if (messageFontHeight < asyncMessRetFontH) {
            messageFontHeight = asyncMessRetFontH;
        }
        Metrics.setMessageFontHeight(messageFontHeight);
        context.setFont(SDViewPref.getInstance().getFont(ISDPreferences.PREF_LIFELINE));

        int width = (int) ((fFrame.getWidth() + 2 * Metrics.FRAME_H_MARGIN) * fZoomValue);
        int height = (int) ((fFrame.getHeight() + 2 * Metrics.FRAME_V_MARGIN) * fZoomValue);

        resizeContents(width, height);

        context.setBackground(SDViewPref.getInstance().getBackGroundColor(ISDPreferences.PREF_FRAME));
        context.fillRectangle(0, 0, getContentsWidth(), Metrics.FRAME_V_MARGIN);
        context.fillRectangle(0, 0, fFrame.getX(), getContentsHeight());
        context.fillRectangle(fFrame.getX() + fFrame.getWidth() + 1, 0, getContentsWidth() - (fFrame.getX() + fFrame.getWidth() + 1), getContentsHeight());
        context.fillRectangle(0, fFrame.getY() + fFrame.getHeight() + 1, getContentsWidth(), getContentsHeight() - (fFrame.getY() + fFrame.getHeight() + 1));
        gcim.setLineWidth(1);

        fFrame.draw(context);
        if (fDragAndDrop != null) {
            Lifeline node = fDragAndDrop;
            boolean isSelected = fDragAndDrop.isSelected();
            boolean hasFocus = fDragAndDrop.hasFocus();
            node.setSelected(false);
            node.setFocused(false);
            node.draw(context, fDragX, fDragY);
            node.setSelected(isSelected);
            node.setFocused(hasFocus);
        }
        gcim.dispose();
        context.dispose();
        return dbuffer;
    }

    @Override
    protected void keyPressedEvent(KeyEvent event) {
        if (!(isFocusControl() || getViewControl().isFocusControl())) {
            Control[] child = getParent().getChildren();
            for (int i = 0; i < child.length; i++) {
                if ((child[i].isFocusControl())&& (!(child[i] instanceof ScrollView))) {
                    getViewControl().setFocus();
                    break;
                }
            }
        }
        setFocus(-1);

        if (event.keyCode == SWT.CTRL) {
            fCtrlSelection = true;
        }
        if (event.keyCode == SWT.SHIFT) {
            fShiftSelection = true;
            fPrevList = new ArrayList<GraphNode>();
            fPrevList.addAll(getSelection());
        }

        GraphNode prevNode = getFocusNode();

        if (event.keyCode == SWT.ARROW_RIGHT) {
            traverseRight();
        }

        if (event.keyCode == SWT.ARROW_LEFT) {
            traverseLeft();
        }

        if (event.keyCode == SWT.ARROW_DOWN) {
            traverseDown();
        }

        if (event.keyCode == SWT.ARROW_UP) {
            traverseUp();
        }

        if (event.keyCode == SWT.HOME) {
            traverseHome();
        }

        if (event.keyCode == SWT.END) {
            traverseEnd();
        }

        if ((!fShiftSelection) && (!fCtrlSelection)) {
            fListStart = fCurrentGraphNode;
        }

        if (event.character == ' ') {
            performSelection(fCurrentGraphNode);
            if (!fShiftSelection) {
                fListStart = fCurrentGraphNode;
            }
        }

        if ((fShiftSelection) && (prevNode != getFocusNode())) {
            clearSelection();
            addSelection(fPrevList);
            addSelection(fFrame.getNodeList(fListStart, getFocusNode()));
            if (getFocusNode() instanceof Lifeline) {
                ensureVisible(getFocusNode().getX(), getFocusNode().getY(), getFocusNode().getWidth(), getFocusNode().getHeight(), SWT.CENTER | SWT.VERTICAL, true);
            } else {
                ensureVisible(getFocusNode());
            }
        } else if ((!fCtrlSelection) && (!fShiftSelection)) {

            clearSelection();
            if (getFocusNode() != null) {
                addSelection(getFocusNode());

                if (getFocusNode() instanceof Lifeline) {
                    ensureVisible(getFocusNode().getX(), getFocusNode().getY(), getFocusNode().getWidth(), getFocusNode().getHeight(), SWT.CENTER | SWT.VERTICAL, true);
                } else {
                    ensureVisible(getFocusNode());
                }
            }
        }

        if (fCurrentGraphNode != null) {
            fCurrentGraphNode.setFocused(true);
        }
        redraw();

        if ((event.character == ' ') && ((fZoomInMode) || (fZoomOutMode))) {
            int cx = Math.round((getContentsX() + getVisibleWidth() / 2) / fZoomValue);
            int cy = Math.round((getContentsY() + getVisibleHeight() / 2) / fZoomValue);
            if (fZoomInMode) {
                if (fZoomValue < 64) {
                    fZoomValue = fZoomValue * (float) 1.25;
                }
            } else {
                fZoomValue = fZoomValue / (float) 1.25;
            }
            int x = Math.round(cx * fZoomValue - getVisibleWidth() / (float) 2);
            int y = Math.round(cy * fZoomValue - getVisibleHeight() / (float) 2);
            setContentsPos(x, y);
            if (fTimeBar != null) {
                fTimeBar.setZoom(fZoomValue);
            }
            // redraw also resize the scrollView content
            redraw();
        }
    }

    @Override
    protected void keyReleasedEvent(KeyEvent event) {
        setFocus(-1);
        if (event.keyCode == SWT.CTRL) {
            fCtrlSelection = false;
        }
        if (event.keyCode == SWT.SHIFT) {
            fShiftSelection = false;
        }
        super.keyReleasedEvent(event);
        setFocus(1);
    }

    @Override
    public boolean isFocusControl() {
        Control[] child = getChildren();
        for (int i = 0; i < child.length; i++) {
            if (child[i].isFocusControl()) {
                return true;
            }
            checkFocusOnChilds(child[i]);
        }
        return false;
    }

    @Override
    public boolean setContentsPos(int x, int y) {
        int localX = x;
        int localY = y;

        if (localX < 0) {
            localX = 0;
        }
        if (localY < 0) {
            localY = 0;
        }
        if (fFrame == null) {
            return false;
        }
        if (localX + getVisibleWidth() > getContentsWidth()) {
            localX = getContentsWidth() - getVisibleWidth();
        }
        if (localY + getVisibleHeight() > getContentsHeight()) {
            localY = getContentsHeight() - getVisibleHeight();
        }
        int x1 = Math.round(localX / fZoomValue);
        int y2 = Math.round(localY / fZoomValue);
        int width = Math.round(getVisibleWidth() / fZoomValue);
        int height = Math.round(getVisibleHeight() / fZoomValue);
        fFrame.updateIndex(x1, y2, width, height);

        if (fInsertionCartet != null && fInsertionCartet.isVisible()) {
            fInsertionCartet.setVisible(false);
        }

        return super.setContentsPos(localX, localY);
    }

    @Override
    protected void contentsMouseHover(MouseEvent event) {
        GraphNode graphNode = null;
        if (fFrame != null) {
            int x = Math.round(event.x / fZoomValue);
            int y = Math.round(event.y / fZoomValue);
            graphNode = fFrame.getNodeAt(x, y);
            if ((graphNode != null) && (SDViewPref.getInstance().tooltipEnabled())) {
                fToolTipNode = graphNode;
                String postfix = getPostfixForTooltip(true);
                if (graphNode instanceof Lifeline) {
                    Lifeline lifeline = (Lifeline) graphNode;
                    fToolTip.showToolTip(lifeline.getToolTipText() + postfix);
                    setFocus(0);
                } else {
                    fToolTip.showToolTip(graphNode.getName() + postfix);
                    setFocus(0);
                }
            } else {
                fToolTip.hideToolTip();
            }
        }
    }

    @Override
    protected void contentsMouseMoveEvent(MouseEvent e) {
        fScrollToolTip.hideToolTip();
        fToolTip.hideToolTip();
        if (!(isFocusControl() || getViewControl().isFocusControl())) {
            Control[] child = getParent().getChildren();
            for (int i = 0; i < child.length; i++) {
                if ((child[i].isFocusControl()) && (!(child[i] instanceof ScrollView))) {
                    getViewControl().setFocus();
                    break;
                }
            }
        }
        setFocus(-1);

        if (((e.stateMask & SWT.BUTTON_MASK) != 0) && ((fDragAndDrop != null) || fIsDragAndDrop) && (fReorderMode || fCollapseProvider != null)) {
            fIsDragAndDrop = false;
            if (fCurrentGraphNode instanceof Lifeline) {
                fDragAndDrop = (Lifeline) fCurrentGraphNode;
            }
            if (fDragAndDrop != null) {
                int dx = 0;
                int dy = 0;
                if (e.x > getContentsX() + getVisibleWidth()) {
                    dx = e.x - (getContentsX() + getVisibleWidth());
                } else if (e.x < getContentsX()) {
                    dx = -getContentsX() + e.x;
                }
                if (e.y > getContentsY() + getVisibleHeight()) {
                    dy = e.y - (getContentsY() + getVisibleHeight());
                } else if (e.y < getContentsY()) {
                    dy = -getContentsY() + e.y;
                }
                fDragX = e.x;
                fDragY = e.y;
                if (dx != 0 || dy != 0) {
                    if (fLocalAutoScroll == null) {
                        if (fLocalAutoScrollTimer == null) {
                            fLocalAutoScrollTimer = new Timer(true);
                        }
                        fLocalAutoScroll = new AutoScroll(this, dx, dy);
                        fLocalAutoScrollTimer.schedule(fLocalAutoScroll, 0, 75);
                    } else {
                        fLocalAutoScroll.fDeltaX = dx;
                        fLocalAutoScroll.fDeltaY = dy;
                    }
                } else if (fLocalAutoScroll != null) {
                    fLocalAutoScroll.cancel();
                    fLocalAutoScroll = null;
                }
                fDragX = Math.round(e.x / fZoomValue);
                fDragY = Math.round(e.y / fZoomValue);
                redraw();
                Lifeline node = fFrame.getCloserLifeline(fDragX);
                if ((node != null) && (node != fDragAndDrop)) {
                    int y = 0;
                    int y1 = 0;
                    int height = Metrics.getLifelineHeaderFontHeigth() + 2 * Metrics.LIFELINE_HEARDER_TEXT_V_MARGIN;
                    int hMargin = Metrics.LIFELINE_VT_MAGIN / 4;
                    int x = node.getX();
                    int width = node.getWidth();
                    if (fFrame.getVisibleAreaY() < node.getY() + node.getHeight() - height - hMargin) {
                        y = contentsToViewY(Math.round((node.getY() + node.getHeight()) * fZoomValue));
                    } else {
                        y = Math.round(height * fZoomValue);
                    }

                    if (fFrame.getVisibleAreaY() < contentsToViewY(node.getY() - hMargin)) {
                        y1 = contentsToViewY(Math.round((node.getY() - hMargin) * fZoomValue));
                    } else {
                        y1 = Math.round(height * fZoomValue);
                    }

                    int rx = Math.round(x * fZoomValue);

                    fInsertionCartet.setVisible(true);
                    if ((fInsertionCartet.getImage() != null) && (!fInsertionCartet.getImage().isDisposed())) {
                        fInsertionCartet.getImage().dispose();
                    }
                    if (rx <= e.x && Math.round(rx + (width * fZoomValue)) >= e.x) {
                        if (fCollapseProvider != null) {
                            ImageData data = fCollapaseCaretImg.getImageData();
                            data = data.scaledTo(Math.round(fCollapaseCaretImg.getBounds().width * fZoomValue), Math.round(fCollapaseCaretImg.getBounds().height * fZoomValue));
                            fCurrentCaretImage = new Image(Display.getCurrent(), data);
                            fInsertionCartet.setImage(fCurrentCaretImage);
                            fInsertionCartet.setLocation(contentsToViewX(rx + Math.round((width / (float) 2) * fZoomValue)) - fCurrentCaretImage.getBounds().width / 2, y);
                        }
                    } else if (fReorderMode) {
                        if (rx > e.x) {
                            if (node.getIndex() > 1 && fFrame.getLifeline(node.getIndex() - 2) == fDragAndDrop) {
                                return;
                            }
                            ImageData data = fArrowUpCaretImg.getImageData();
                            data = data.scaledTo(Math.round(fArrowUpCaretImg.getBounds().width * fZoomValue), Math.round(fArrowUpCaretImg.getBounds().height * fZoomValue));
                            fCurrentCaretImage = new Image(Display.getCurrent(), data);
                            fInsertionCartet.setImage(fCurrentCaretImage);
                            fInsertionCartet.setLocation(contentsToViewX(Math.round((x - Metrics.LIFELINE_SPACING / 2) * fZoomValue)) - fCurrentCaretImage.getBounds().width / 2, y1);
                        } else {
                            if (node.getIndex() < fFrame.lifeLinesCount() && fFrame.getLifeline(node.getIndex()) == fDragAndDrop) {
                                return;
                            }
                            ImageData data = fArrowUpCaretImg.getImageData();
                            data = data.scaledTo(Math.round(fArrowUpCaretImg.getBounds().width * fZoomValue), Math.round(fArrowUpCaretImg.getBounds().height * fZoomValue));
                            fCurrentCaretImage = new Image(Display.getCurrent(), data);
                            fInsertionCartet.setImage(fCurrentCaretImage);
                            fInsertionCartet.setLocation(contentsToViewX(Math.round((x + width + Metrics.LIFELINE_SPACING / 2) * fZoomValue)) - fCurrentCaretImage.getBounds().width / 2 + 1, y1);
                        }
                    }
                } else {
                    fInsertionCartet.setVisible(false);
                }
            }
        } else {
            super.contentsMouseMoveEvent(e);
        }
    }

    @Override
    protected void contentsMouseUpEvent(MouseEvent event) {
        // Just in case the diagram highlight a time compression region
        // this region need to be released when clicking everywhere
        fInsertionCartet.setVisible(false);
        if (fDragAndDrop != null) {
            if ((fOverView != null) && (!fOverView.isDisposed())) {
                fOverView.dispose();
            }
            fOverView = null;
            Lifeline node = fFrame.getCloserLifeline(fDragX);
            if (node != null) {
                int rx = Math.round(node.getX() * fZoomValue);
                if (rx <= event.x && Math.round(rx + (node.getWidth() * fZoomValue)) >= event.x) {
                    if ((fCollapseProvider != null) && (fDragAndDrop != node)) {
                        fCollapseProvider.collapseTwoLifelines(fDragAndDrop, node);
                    }
                } else if (rx < event.x) {
                    fFrame.insertLifelineAfter(fDragAndDrop, node);
                    if (node.getIndex() < fFrame.lifeLinesCount()) {
                        Lifeline temp[] = { fDragAndDrop, fFrame.getLifeline(node.getIndex()) };
                        fReorderList.add(temp);
                    } else {
                        Lifeline temp[] = { fDragAndDrop, null };
                        fReorderList.add(temp);
                    }
                } else {
                    fFrame.insertLifelineBefore(fDragAndDrop, node);
                    Lifeline temp[] = { fDragAndDrop, node };
                    fReorderList.add(temp);
                }
            }
        }
        fDragAndDrop = null;
        redraw();
        if (fFrame == null) {
            return;
        }
        fFrame.resetTimeCompression();

        // reset auto scroll if it's engaged
        if (fLocalAutoScroll != null) {
            fLocalAutoScroll.cancel();
            fLocalAutoScroll = null;
        }
        super.contentsMouseUpEvent(event);
    }

    @Override
    protected void contentsMouseDownEvent(MouseEvent event) {
        if (fCurrentGraphNode != null) {
            fCurrentGraphNode.setFocused(false);
        }

        // Just in case the diagram highlight a time compression region
        // this region need to be released when clicking everywhere
        if (fFrame == null) {
            return;
        }

        fFrame.resetTimeCompression();

        if ((event.stateMask & SWT.CTRL) != 0) {
            fCtrlSelection = true;
        } else {
            fCtrlSelection = false;
        }

        if (((fZoomInMode) || (fZoomOutMode)) && (event.button == 1)) {
            int cx = Math.round(event.x / fZoomValue);
            int cy = Math.round(event.y / fZoomValue);
            if (fZoomInMode) {
                if (fZoomValue < 64) {
                    fZoomValue = fZoomValue * (float) 1.25;
                }
            } else {
                fZoomValue = fZoomValue / (float) 1.25;
            }
            int x = Math.round(cx * fZoomValue - getVisibleWidth() / (float) 2);
            int y = Math.round(cy * fZoomValue - getVisibleHeight() / (float) 2);
            setContentsPos(x, y);
            if (fTimeBar != null) {
                fTimeBar.setZoom(fZoomValue);
            }
            // redraw also resize the scrollView content
            redraw();
        } else {
            GraphNode node = null;
            int x = Math.round(event.x / fZoomValue);
            int y = Math.round(event.y / fZoomValue);
            node = fFrame.getNodeAt(x, y);

            if ((event.button == 1) || ((node != null) && !node.isSelected())) {
                if (!fShiftSelection) {
                    fListStart = node;
                }
                if (fShiftSelection) {
                    clearSelection();
                    addSelection(fFrame.getNodeList(fListStart, node));
                } else {
                    performSelection(node);
                }
                fCurrentGraphNode = node;
                if (node != null) {
                    node.setFocused(true);
                }
            }
            redraw();
        }
        if (fDragAndDrop == null) {
            super.contentsMouseDownEvent(event);
        }
        fIsDragAndDrop = (event.button == 1);

    }

    /**
     * TimerTask for auto scroll feature.
     */
    protected static class AutoScroll extends TimerTask {
        /**
         * Field delta x.
         */
        public int fDeltaX;
        /**
         * Field delta y.
         */
        public int fDeltaY;
        /**
         * Field sequence diagram reference.
         */
        public SDWidget fSdWidget;

        /**
         * Constructor for AutoScroll.
         * @param sv sequence diagram widget reference
         * @param dx delta x
         * @param dy delta y
         */
        public AutoScroll(SDWidget sv, int dx, int dy) {
            fSdWidget = sv;
            fDeltaX = dx;
            fDeltaY = dy;
        }

        @Override
        public void run() {
            Display display = Display.getDefault();
            if ((display == null) || (display.isDisposed())) {
                return;
            }
            display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (fSdWidget.isDisposed()) {
                        return;
                    }
                    fSdWidget.fDragX += fDeltaX;
                    fSdWidget.fDragY += fDeltaY;
                    fSdWidget.scrollBy(fDeltaX, fDeltaY);
                }
            });
        }
    }

    @Override
    protected void drawContents(GC gc, int clipx, int clipy, int clipw, int cliph) {
        if (fFrame == null) {
            gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
            gc.fillRectangle(0, 0, getVisibleWidth(), getVisibleHeight());
            gc.dispose();
            return;
        }
        SDViewPref.getInstance();

        Rectangle area = getClientArea();
        Image dbuffer = getDrawBuffer();
        int height = Math.round((fFrame.getHeight() + 2 * Metrics.FRAME_V_MARGIN) * fZoomValue);

        try {
            gc.drawImage(dbuffer, 0, 0, area.width, area.height, 0, 0, area.width, area.height);
        } catch (Exception e) {
            Activator.getDefault().logError("Error drawin content", e); //$NON-NLS-1$
        }
        dbuffer.dispose();
        setHScrollBarIncrement(Math.round(SDViewPref.getInstance().getLifelineWidth() / (float) 2 * fZoomValue));
        setVScrollBarIncrement(Math.round(Metrics.getMessagesSpacing() * fZoomValue));
        if ((fTimeBar != null) && (fFrame.hasTimeInfo())) {
            fTimeBar.resizeContents(9, height + getHorizontalBarHeight());
            fTimeBar.setContentsPos(getContentsX(), getContentsY());
            fTimeBar.redraw();
            fTimeBar.update();
        }
        float xRatio = getContentsWidth() / (float) getVisibleWidth();
        float yRatio = getContentsHeight() / (float) getVisibleHeight();
        if (yRatio > xRatio) {
            setOverviewSize((int) (getVisibleHeight() * 0.75));
        } else {
            setOverviewSize((int) (getVisibleWidth() * 0.75));
        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent event) {
    }

    @Override
    public void widgetSelected(SelectionEvent event) {
        if (event.widget == fZoomIn) {
            fZoomValue = fZoomValue * 2;
        } else if (event.widget == fZoomOut) {
            fZoomValue = fZoomValue / 2;
        }
        redraw();
    }

    /**
     * Called when property changed occurs in the preference page. "PREFOK" is
     * fired when the user press the ok or apply button
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (fFrame != null && !isDisposed()) {
            fFrame.resetTimeCompression();
        }
        if (e.getProperty().equals("PREFOK")) //$NON-NLS-1$
        {
            // Prepare the overview to be reused for the new
            // settings (especially the colors)
            if (fOverView != null) {
                fOverView.dispose();
            }
            fOverView = null;
            redraw();
        }
    }

    @Override
    public void widgetDisposed(DisposeEvent e) {
        if (fOverView != null) {
            fOverView.dispose();
        }
        super.removeDisposeListener(this);
        if ((fCurrentCaretImage != null) && (!fCurrentCaretImage.isDisposed())) {
            fCurrentCaretImage.dispose();
        }
        if ((fArrowUpCaretImg != null) && (!fArrowUpCaretImg.isDisposed())) {
            fArrowUpCaretImg.dispose();
        }
        if ((fCollapaseCaretImg != null) && (!fCollapaseCaretImg.isDisposed())) {
            fCollapaseCaretImg.dispose();
        }
        SDViewPref.getInstance().removePropertyChangeListener(this);
        LoadersManager lm = LoadersManager.getInstance();
        if (fSite instanceof SDView) {
            ((SDView) fSite).resetProviders();
            if (lm != null) {
                lm.resetLoader(((SDView) fSite).getViewSite().getId());
            }
        }
    }

    @Override
    protected void drawOverview(GC gc, Rectangle r) {
        float oldzoom = fZoomValue;
        if (getContentsWidth() > getContentsHeight()) {
            fZoomValue = (float) r.width / (float) getContentsWidth() * oldzoom;
        } else {
            fZoomValue = (float) r.height / (float) getContentsHeight() * oldzoom;
        }
        if ((fOverView != null) && ((r.width != fOverView.getBounds().width) || (r.height != fOverView.getBounds().height))) {
            fOverView.dispose();
            fOverView = null;
        }
        if (fOverView == null) {
            int backX = getContentsX();
            int backY = getContentsY();
            setContentsPos(0, 0);
            fOverView = new Image(getDisplay(), r.width, r.height);
            GC gcim = new GC(fOverView);
            NGC context = new NGC(this, gcim);
            context.setBackground(SDViewPref.getInstance().getBackGroundColor(ISDPreferences.PREF_FRAME));
            fFrame.draw(context);
            setContentsPos(backX, backY);
            gcim.dispose();
            context.dispose();
        }
        if ((fOverView != null) && (r.width == fOverView.getBounds().width) && (r.height == fOverView.getBounds().height)) {
            gc.drawImage(fOverView, 0, 0, r.width, r.height, 0, 0, r.width, r.height);
        }

        fZoomValue = oldzoom;

        super.drawOverview(gc, r);
    }

    @Override
    public void deltaSelected(Lifeline lifeline, int startEvent, int nbEvent, IColor color) {
        fFrame.highlightTimeCompression(lifeline, startEvent, nbEvent, color);
        ensureVisible(lifeline);
        int y1 = lifeline.getY() + lifeline.getHeight() + (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * startEvent;
        int y2 = lifeline.getY() + lifeline.getHeight() + (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * (startEvent + nbEvent);
        ensureVisible(lifeline.getX(), y1 - (Metrics.getLifelineHeaderFontHeigth() + +2 * Metrics.LIFELINE_HEARDER_TEXT_V_MARGIN), lifeline.getWidth(), y2 - y1 + 3, SWT.CENTER | SWT.VERTICAL, true);
        redraw();
        update();
    }

    @Override
    public int getVisibleWidth() {
        if (fIsPrinting) {
            return fPrinter.getClientArea().width;
        }
        return super.getVisibleWidth();
    }

    @Override
    public int getVisibleHeight() {
        if (fIsPrinting) {
            return fPrinter.getClientArea().height;
        }
        return super.getVisibleHeight();
    }

    @Override
    public int contentsToViewX(int x) {
        if (fIsPrinting) {
            int v = Math.round(fPrinterX * fPrinterZoom);
            return x - v;
        }
        return x - getContentsX();
    }

    @Override
    public int contentsToViewY(int y) {
        if (fIsPrinting) {
            int v = Math.round(fPrinterY * fPrinterZoom);
            return y - v;
        }
        return y - getContentsY();
    }

    @Override
    public int getContentsX() {
        if (fIsPrinting) {
            return Math.round(fPrinterX * fPrinterZoom);
        }
        return super.getContentsX();
    }

    @Override
    public int getContentsY() {
        if (fIsPrinting) {
            return Math.round(fPrinterY * fPrinterZoom);
        }
        return super.getContentsY();
    }

    /**
     * Traverse Listener implementation.
     */
    protected static class LocalTraverseListener implements TraverseListener {
        @Override
        public void keyTraversed(TraverseEvent e) {
            if ((e.detail == SWT.TRAVERSE_TAB_NEXT) || (e.detail == SWT.TRAVERSE_TAB_PREVIOUS)) {
                e.doit = true;
            }
        }
    }

}
