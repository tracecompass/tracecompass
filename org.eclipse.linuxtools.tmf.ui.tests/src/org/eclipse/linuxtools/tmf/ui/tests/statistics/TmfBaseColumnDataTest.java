/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Mathieu Denis (mathieu.denis@polymtl.ca)  - Initial design and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.statistics;

import junit.framework.TestCase;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.internal.tmf.core.util.TmfFixedArray;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfBaseColumnData;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfBaseColumnData.ITmfColumnPercentageProvider;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfBaseStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("nls")
public class TmfBaseColumnDataTest extends TestCase {

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    private int fAlignment;
    private int fWidth;
    private String fHeader;
    private String fToolTip;
    private ColumnLabelProvider fLabelProvider;
    private ViewerComparator fComparator;
    private ITmfColumnPercentageProvider fPercentageProvider;
    private TmfStatisticsTreeNode fTreeNode;
    private String fTraceName;
    private TmfBaseColumnData fBaseColumnData;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    private void init() {
        fHeader = "test Column1";
        fWidth = 300;
        fAlignment = SWT.LEFT;
        fToolTip = "Tooltip " + fHeader;
        fLabelProvider = new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((TmfStatisticsTreeNode) element).getKey();
            }

            @Override
            public Image getImage(Object element) {
                return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
            }
        };
        fComparator = new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                TmfStatisticsTreeNode n1 = (TmfStatisticsTreeNode) e1;
                TmfStatisticsTreeNode n2 = (TmfStatisticsTreeNode) e2;

                return n1.getKey().compareTo(n2.getKey());
            }
        };
        fPercentageProvider = new ITmfColumnPercentageProvider() {
            @Override
            public double getPercentage(TmfStatisticsTreeNode node) {
                TmfStatisticsTreeNode parent = node;
                do {
                    parent = parent.getParent();
                } while (parent != null && parent.getValue().nbEvents == 0);

                if (parent == null) {
                    return 0;
                } else {
                    return (double) node.getValue().nbEvents / parent.getValue().nbEvents;
                }
            }
        };

        TmfBaseStatisticsTree baseData = new TmfBaseStatisticsTree();
        fTraceName = "trace1";
        fTreeNode = new TmfStatisticsTreeNode(new TmfFixedArray<String>(fTraceName), baseData);

        fBaseColumnData = new TmfBaseColumnData(fHeader, fWidth, fAlignment, fToolTip, fLabelProvider, fComparator, fPercentageProvider);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        init();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    // ------------------------------------------------------------------------
    // getHeader
    // ------------------------------------------------------------------------

    public void testGetHeader() {
        assertEquals("getHeader", 0, fBaseColumnData.getHeader().compareTo(fHeader));
    }

    // ------------------------------------------------------------------------
    // getWidth
    // ------------------------------------------------------------------------

    public void testGetWidth() {
        assertEquals("getWidth", fWidth, fBaseColumnData.getWidth());
    }

    // ------------------------------------------------------------------------
    // getAlignment
    // ------------------------------------------------------------------------

    public void testGetAlignment() {
        assertEquals("getAlignment", fAlignment, fBaseColumnData.getAlignment());
    }

    // ------------------------------------------------------------------------
    // getToolTip
    // ------------------------------------------------------------------------

    public void testGetTooltip() {
        assertEquals("getTooltip", fToolTip, fBaseColumnData.getTooltip());
    }

    // ------------------------------------------------------------------------
    // getLabelProvider
    // ------------------------------------------------------------------------

    public void testGetLabelProvider() {
        assertEquals("getLabelProvider", 0, fBaseColumnData.getLabelProvider().getText(fTreeNode).compareTo(fLabelProvider.getText(fTreeNode)));
        assertTrue("getLabelProvider", fBaseColumnData.getLabelProvider().getImage(fTreeNode).equals(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT)));
        assertTrue("getLabelProvider", fBaseColumnData.getLabelProvider().equals(fLabelProvider));
    }

    // ------------------------------------------------------------------------
    // getComparator
    // ------------------------------------------------------------------------

    public void testGetComparator() {
        assertTrue("getComparator", fBaseColumnData.getComparator().equals(fComparator));
    }

    // ------------------------------------------------------------------------
    // getPercentageProvider
    // ------------------------------------------------------------------------

    public void testGetPercentageProvider() {
        assertTrue("getPercentageProvider", fBaseColumnData.getPercentageProvider().equals(fPercentageProvider));
    }
}
