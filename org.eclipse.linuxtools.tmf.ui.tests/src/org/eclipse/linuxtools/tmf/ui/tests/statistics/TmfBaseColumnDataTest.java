/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial design and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseColumnData;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseColumnData.ITmfColumnPercentageProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.Test;

/**
 * TmfBaseColumnData Test Case.
 */
public class TmfBaseColumnDataTest {

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

    /**
     * Pre-test setup
     */
    @Before
    public void init() {
        fHeader = "test Column1";
        fWidth = 300;
        fAlignment = SWT.LEFT;
        fToolTip = "Tooltip " + fHeader;
        fLabelProvider = new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((TmfStatisticsTreeNode) element).getName();
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

                return n1.getName().compareTo(n2.getName());
            }
        };
        fPercentageProvider = new ITmfColumnPercentageProvider() {
            @Override
            public double getPercentage(TmfStatisticsTreeNode node) {
                TmfStatisticsTreeNode parent = node;
                do {
                    parent = parent.getParent();
                } while (parent != null && parent.getValues().getTotal() == 0);

                if (parent == null) {
                    return 0;
                }
                return (double) node.getValues().getTotal() / parent.getValues().getTotal();
            }
        };

        TmfStatisticsTree baseData = new TmfStatisticsTree();
        fTraceName = "trace1";
        fTreeNode = new TmfStatisticsTreeNode(baseData, baseData.getRootNode(), fTraceName);

        fBaseColumnData = new TmfBaseColumnData(fHeader, fWidth, fAlignment, fToolTip, fLabelProvider, fComparator, fPercentageProvider);
    }

    // ------------------------------------------------------------------------
    // Test methods
    // ------------------------------------------------------------------------

    /**
     * Test get header
     */
    @Test
    public void testGetHeader() {
        assertEquals("getHeader", 0, fBaseColumnData.getHeader().compareTo(fHeader));
    }

    /**
     * Test getting of column width.
     */
    @Test
    public void testGetWidth() {
        assertEquals("getWidth", fWidth, fBaseColumnData.getWidth());
    }

    /**
     * Test getting of alignment value
     */
    @Test
    public void testGetAlignment() {
        assertEquals("getAlignment", fAlignment, fBaseColumnData.getAlignment());
    }

    /**
     * Test getting of tooltip.
     */
    @Test
    public void testGetTooltip() {
        assertEquals("getTooltip", fToolTip, fBaseColumnData.getTooltip());
    }

    /**
     * Test getting of label provider
     */
    @Test
    public void testGetLabelProvider() {
        assertEquals("getLabelProvider", 0, fBaseColumnData.getLabelProvider().getText(fTreeNode).compareTo(fLabelProvider.getText(fTreeNode)));
        assertTrue("getLabelProvider", fBaseColumnData.getLabelProvider().getImage(fTreeNode).equals(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT)));
        assertTrue("getLabelProvider", fBaseColumnData.getLabelProvider().equals(fLabelProvider));
    }

    /**
     * Test getting of comparator.
     */
    @Test
    public void testGetComparator() {
        assertTrue("getComparator", fBaseColumnData.getComparator().equals(fComparator));
    }

    /**
     * Test getting of percentage provider.
     */
    @Test
    public void testGetPercentageProvider() {
        assertTrue("getPercentageProvider", fBaseColumnData.getPercentageProvider().equals(fPercentageProvider));
    }
}
