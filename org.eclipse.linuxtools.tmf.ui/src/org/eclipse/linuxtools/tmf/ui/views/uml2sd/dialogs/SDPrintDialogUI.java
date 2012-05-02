/********************************************************************** 
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * Copyright (c) 2011, 2012 Ericsson.
 *  
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF 
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs;

import java.text.MessageFormat;
import java.util.Arrays;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.GridUtil;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.DiagramToolTip;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.NGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * The class implements the actual print dialog UI for collecting printing data.
 * 
 * @version 1.0
 * @author sveyrier
 */
public class SDPrintDialogUI {
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The set horizontal pages number. 
     */
    protected Button setHPagesNumber;
    /**
     * The set vertical pages number. 
     */
    protected Button setVPagesNumber;
    /**
     * Flag whether to use current zoom or not. 
     */
    protected Button useCurrentZoom;
    /**
     * Flag whether to print all pages or not
     */
    protected Button allPages;
    /**
     * Flag whether to print current page only
     */
    protected Button currentPage;
    /**
     * Button to select a page list.
     */
    protected Button pageList;
    /**
     * Button to select a page range.
     */
    protected Button pageRange;
    /**
     * Text field to enter from page.
     */
    protected Text fromPage;
    /**
     * Text field to enter to page.
     */
    protected Text toPage;
    /**
     *  The sequence diagram widget reference. 
     */
    protected SDWidget view;
    /**
     * Text field for number of horizontal pages
     */
    protected Text hPagesNum;
    /**
     * Text field for number of vertical pages
     */
    protected Text vPagesNum;
    /**
     * Text field for toal number of pages
     */
    protected Text totalPages;
    /**
     * A modify listener implementation to handle modifications. 
     */
    protected ModifyListener modifyListener;
    /**
     * A selection listener implementation to handle selections.
     */
    protected SelectionListener selectionListener;
    /**
     * Local canvas displaying sequence diagram overview.
     */
    protected LocalSD overviewCanvas;
    /**
     * Number of pages
     */
    protected int nbPages = 0;
    /**
     * Number of selected pages.
     */
    protected int pageNum = -1;
    /**
     * Number of first page. 
     */
    protected int firstPage = -1;
    /**
     * List of pages to print.
     */
    protected int pagesList[];
    /**
     * Values for dividing sequence diagram into pages.   
     */
    protected float stepX, stepY, sTX, sTY;
    /**
     * Page which to print from. 
     */
    protected int from;
    /**
     * Page which to print to. 
     */
    protected int to;
    /**
     * Flag for enabling multi-selection.
     */
    protected boolean multiSelection = false;
    /**
     * Flag for enabling area selection.
     */
    protected boolean areaSelection = false;
    /**
     * Flag for printing all.
     */
    protected boolean printAll;
    /**
     * Flag for printing current page only.
     */
    protected boolean printCurrent;
    /**
     * Flag for printing a selection of pages.
     */
    protected boolean printSelection;
    /**
     * Flag for printing a range of pages.
     */
    protected boolean printRange;
    /**
     * Number of selected rows
     */
    protected int nbRows;
    /**
     * Number of selected lines
     */
    protected int nbLines;
    /**
     * The zoom factor.
     */
    protected float zoomFactor;
    /**
     * The printer data reference. 
     */
    protected PrinterData printerData;
    /**
     * The diagram tooltip to show if necessary.
     */
    protected DiagramToolTip toolTip = null;
    /**
     * Label for current selection.
     */
    protected Label currentSelection;
    /**
     * The shell reference.
     */
    protected Shell shell;
    /**
     * Button to open printer dialog from OS.
     */
    protected Button printerDialog;
    /**
     * Flag for showoing print button.
     */
    protected boolean showPrintButton;
    /**
     * Test value  
     */
    protected int test = 3;
    /**
     * Parent wizard page if used as wizard
     */
    protected WizardPage parentWizardPage = null;
    /**
     * Reference to parent print dialog. 
     */
    protected SDPrintDialog parentDialog = null;

    // ------------------------------------------------------------------------
    // Helper Class
    // ------------------------------------------------------------------------
    /**
     * Local sequence diagram widget used to display overview of sequence diagram to print.
     * @version 1.0 
     */
    protected class LocalSD extends SDWidget {

        /**
         * Constructor
         * @param c Parent composite
         * @param s Style bits
         */
        public LocalSD(Composite c, int s) {
            super(c, s);
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.ScrollView#getContentsHeight()
         */
        @Override
        public int getContentsHeight() {
            if (view.getContentsHeight() > view.getContentsHeight()) {
                return (int) (view.getVisibleHeight() / (float) test / view.zoomValue);
            }
            return (int) (super.getContentsHeight());
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.ScrollView#getContentsWidth()
         */
        @Override
        public int getContentsWidth() {
            if (view.getVisibleWidth() > view.getContentsWidth()) {
                return (int) (view.getVisibleWidth() / (float) test / view.zoomValue);
            }
            return (int) (super.getContentsWidth());
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget#contentsMouseHover(org.eclipse.swt.events.MouseEvent)
         */
        @Override
        protected void contentsMouseHover(MouseEvent event) {
        }

        /**
         * Creates page selection images.
         * 
         * @param img - Overview image
         * @param width -The width value
         * @param stepX - Step X
         * @param height - Height value
         * @param stepY - Step Y
         * @return new image
         */
        protected Image createPagesSelectionImages(Image img, int width, float stepX, int height, float stepY) {

            Image over = new Image(super.getShell().getDisplay(), img.getImageData());

            for (int pageIndex = 0; pageIndex < pagesList.length; pageIndex++) {

                int pageNum = pagesList[pageIndex];

                if (getPagesForSelection() > 0 && pageNum > 0) {
                    int line = pageNum / getNbRow();
                    int row = pageNum % getNbRow();
                    if (row != 0) {
                        line++;
                    } else {
                        row = getNbRow();
                    }

                    line--;
                    row--;

                    Image toDel = over;
                    if (overviewCanvas.isFocusControl()) {
                        over = new Image(super.getShell().getDisplay(), drawRegionSelected(toDel, new Rectangle(contentsToViewX((int) (row * stepX * overviewCanvas.zoomValue)), contentsToViewY((int) (line * stepY * overviewCanvas.zoomValue)),
                                ((int) (stepX * overviewCanvas.zoomValue)), ((int) (stepY * overviewCanvas.zoomValue))), new RGB(0, 0, 128)));
                    } else {
                        over = new Image(super.getShell().getDisplay(), drawRegionSelected(toDel, new Rectangle(contentsToViewX((int) (row * stepX * overviewCanvas.zoomValue)), contentsToViewY((int) (line * stepY * overviewCanvas.zoomValue)),
                                ((int) (stepX * overviewCanvas.zoomValue)), ((int) (stepY * overviewCanvas.zoomValue))), new RGB(221, 208, 200)));
                    }
                    toDel.dispose();
                }
            }

            Arrays.sort(pagesList);
            int pos = Arrays.binarySearch(pagesList, pageNum);
            if (pos < 0)
                if (getPagesForSelection() > 0 && pageNum > 0) {
                    int line = pageNum / getNbRow();
                    int row = pageNum % getNbRow();
                    if (row != 0) {
                        line++;
                    } else {
                        row = getNbRow();
                    }

                    line--;
                    row--;

                    Image toDel = over;
                    over = new Image(super.getShell().getDisplay(), drawRegionSelected(toDel, new Rectangle(contentsToViewX((int) (row * stepX * overviewCanvas.zoomValue)), contentsToViewY((int) (line * stepY * overviewCanvas.zoomValue)),
                            ((int) (stepX * overviewCanvas.zoomValue)), ((int) (stepY * overviewCanvas.zoomValue))), new RGB(221, 208, 200)));
                    toDel.dispose();
                }

            GC imGC2 = new GC(over);
            imGC2.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
            NGC imGC = new NGC(overviewCanvas, imGC2);
            for (int i = 0, x = 0; x <= width && stepX > 0; i++, x = (int) (i * stepX)) {
                imGC.drawLine(x, 0, x, height);
            }

            for (int j = 0, y = 0; y <= height && stepY > 0; j++, y = (int) (j * stepY)) {
                imGC.drawLine(0, y, width, y);
            }

            imGC2.dispose();
            imGC.dispose();
            return over;
        }
        
        /*
         * (non-Javadoc)
         * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget#drawContents(org.eclipse.swt.graphics.GC, int, int, int, int)
         */
        @Override
        protected void drawContents(GC gc, int clipx, int clipy, int clipw, int cliph) {

            Image dbuffer = getDrawBuffer();
            computeStepXY();
            Image d;

            int lw = (int) (getContentsWidth() / zoomValue);
            if (getContentsWidth() < getVisibleWidth()) {
                lw = (int) (getVisibleWidth() / zoomValue);
            }

            int lh = (int) (getContentsHeight() / zoomValue);
            if (getContentsHeight() < getVisibleHeight()) {
                lh = (int) (getVisibleHeight() / zoomValue);
            }
            d = createPagesSelectionImages(dbuffer, lw, stepX, lh, stepY);

            if (!isEnabled()) {
                Image toDel = d;
                d = new Image(super.getShell().getDisplay(), drawRegionSelected(d, new Rectangle(0, 0, lw, lh), new RGB(221, 208, 200)));
                // d, new Rectangle(0,0,((int)(stepX*overviewCanvas.zoomValue)),((int)(
                // stepY*overviewCanvas.zoomValue))),new RGB(221,208,200)));
                toDel.dispose();
            }

            Rectangle area = getClientArea();
            int w = d.getBounds().width;
            int h = d.getBounds().height;
            gc.drawImage(d, 0, 0, w, h, 0, 0, area.width, area.height);

            totalPages.setText(Integer.valueOf(maxNumOfPages()).toString());
            displayPageNum();
            dbuffer.dispose();
            d.dispose();
            gc.dispose();

        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget#keyPressedEvent(org.eclipse.swt.events.KeyEvent)
         */
        @Override
        protected void keyPressedEvent(KeyEvent e) {
            if (e.keyCode == SWT.CTRL) {
                multiSelection = true;
            }
            if (e.keyCode == SWT.SHIFT) {
                areaSelection = true;
            }
            if (e.keyCode == SWT.ARROW_DOWN) {
                if (pageNum + getNbRow() <= maxNumOfPages()) {
                    pageNum += getNbRow();
                }
                int line = pageNum / getNbRow();
                int row = pageNum % getNbRow();
                if (row == 0) {
                    line--;
                }
                if ((line + 1) * stepY > (overviewCanvas.getContentsY() + overviewCanvas.getVisibleHeight()) / overviewCanvas.zoomValue) {
                    overviewCanvas.scrollBy(0, (int) (stepY * overviewCanvas.zoomValue));
                }
            }
            if (e.keyCode == SWT.ARROW_UP) {
                if (pageNum - getNbRow() > 0) {
                    pageNum -= getNbRow();
                }
                int line = pageNum / getNbRow();
                int row = pageNum % getNbRow();
                if (row == 0) {
                    line--;
                }
                if ((line) * stepY <= overviewCanvas.getContentsY() / overviewCanvas.zoomValue) {
                    overviewCanvas.scrollBy(0, -(int) (stepY * overviewCanvas.zoomValue));
                }
            }
            if (e.keyCode == SWT.ARROW_LEFT) {
                if ((pageNum - 2) / getNbRow() == (pageNum - 1) / getNbRow() && pageNum > 1) {
                    pageNum--;
                }
                int row = pageNum % getNbRow();
                if ((row - 1) * stepX < (overviewCanvas.getContentsX()) / overviewCanvas.zoomValue) {
                    overviewCanvas.scrollBy(-(int) (stepX * overviewCanvas.zoomValue), 0);
                }
            }
            if (e.keyCode == SWT.ARROW_RIGHT) {
                if ((pageNum - 1) / getNbRow() == pageNum / getNbRow()) {
                    pageNum++;
                }
                int row = pageNum % getNbRow();
                if (row == 0) {
                    row = getNbRow();
                }
                if ((row) * stepX > (overviewCanvas.getContentsX() + overviewCanvas.getVisibleWidth()) / overviewCanvas.zoomValue) {
                    overviewCanvas.scrollBy((int) (stepX * overviewCanvas.zoomValue), 0);
                }
            }

            if (e.keyCode == 32 && pageNum > -1) {
                Arrays.sort(pagesList);
                int pos = Arrays.binarySearch(pagesList, pageNum);
                if (pos < 0) {
                    addToPagesList(pageNum);
                } else {
                    removeFromPagesList(pageNum);
                }
            }

            if (!areaSelection && !multiSelection) {
                firstPage = pageNum;
                pagesList = new int[1];
                pagesList[0] = pageNum;
            } else if ((pageNum != -1) && (areaSelection) && (firstPage != -1)) {
                pagesList = new int[0];
                int line1 = firstPage / getNbRow();
                int row1 = firstPage % getNbRow();
                if (row1 != 0) {
                    line1++;
                } else {
                    row1 = getNbRow();
                }

                int line2 = pageNum / getNbRow();
                int row2 = pageNum % getNbRow();
                if (row2 != 0) {
                    line2++;
                } else {
                    row2 = getNbRow();
                }

                int temp;
                if (line1 > line2) {
                    temp = line2;
                    line2 = line1;
                    line1 = temp;
                }

                if (row1 > row2) {
                    temp = row2;
                    row2 = row1;
                    row1 = temp;
                }

                for (int i = row1 - 1; i < row2; i++) {
                    for (int j = line1 - 1; j < line2; j++) {
                        addToPagesList(i + j * getNbRow() + 1);
                    }
                }
            }
            displayPageNum();
            overviewCanvas.redraw();
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget#keyReleasedEvent(org.eclipse.swt.events.KeyEvent)
         */
        @Override
        protected void keyReleasedEvent(KeyEvent e) {
            if (e.keyCode == SWT.CTRL) {
                multiSelection = false;
            }
            if (e.keyCode == SWT.SHIFT) {
                areaSelection = false;
            }
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget#contentsMouseDownEvent(org.eclipse.swt.events.MouseEvent)
         */
        @Override
        protected void contentsMouseDownEvent(MouseEvent event) {

            computeStepXY();
            int x1 = (int) ((event.x / overviewCanvas.zoomValue) / stepX);
            int x2 = (int) ((event.y / overviewCanvas.zoomValue) / stepY);

            int oldPage = pageNum;

            pageNum = x1 + x2 * getNbRow() + 1;

            if (pageNum > maxNumOfPages()) {
                pageNum = oldPage;
                return;
            }

            if (!areaSelection) {
                firstPage = pageNum;
            }

            if ((pageNum != -1) && (multiSelection)) {
                Arrays.sort(pagesList);
                int pos = Arrays.binarySearch(pagesList, pageNum);
                if (pos < 0) {
                    addToPagesList(pageNum);
                } else {
                    removeFromPagesList(pageNum);
                }
            } else if ((pageNum != -1) && (areaSelection) && (firstPage != -1)) {

                pagesList = new int[0];

                int line1 = firstPage / getNbRow();
                int row1 = firstPage % getNbRow();
                if (row1 != 0) {
                    line1++;
                } else {
                    row1 = getNbRow();
                }

                int line2 = pageNum / getNbRow();
                int row2 = pageNum % getNbRow();
                if (row2 != 0) {
                    line2++;
                } else {
                    row2 = getNbRow();
                }

                int temp;
                if (line1 > line2) {
                    temp = line2;
                    line2 = line1;
                    line1 = temp;
                }

                if (row1 > row2) {
                    temp = row2;
                    row2 = row1;
                    row1 = temp;
                }

                for (int i = row1 - 1; i < row2; i++) {
                    for (int j = line1 - 1; j < line2; j++) {
                        addToPagesList(i + j * getNbRow() + 1);
                    }
                }
            } else {
                pagesList = new int[1];
                pagesList[0] = pageNum;
            }
            if ((event.stateMask & SWT.CTRL) != 0) {
                multiSelection = true;
            }
            displayPageNum();
            redraw();
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget#contentsMouseMoveEvent(org.eclipse.swt.events.MouseEvent)
         */
        @Override
        protected void contentsMouseMoveEvent(MouseEvent e) {
            toolTip.hideToolTip();
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.ScrollView#resizeContents(int, int)
         */
        @Override
        public void resizeContents(int _w, int _h) {
            super.resizeContents(_w, _h);
        }

    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * 
     * @param s The shell reference
     * @param v The sequence diagram widget reference
     */
    public SDPrintDialogUI(Shell s, SDWidget v) {
        this(s, v, false);
    }
    
    /**
     * Constructor
     * 
     * @param s The shell reference
     * @param v The sequence diagram widget reference
     * @param showPrintBtn Flag for showing print buttons
     */
    public SDPrintDialogUI(Shell s, SDWidget v, boolean showPrintBtn) {
        setShell(s);
        view = v;
        showPrintButton = showPrintBtn;

        printerData = Printer.getDefaultPrinterData();
        if (printerData != null) {
            printerData.scope = PrinterData.SELECTION;
        }

        pagesList = new int[0];

        selectionListener = new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (useCurrentZoom.getSelection()) {
                    hPagesNum.setEnabled(false);
                    vPagesNum.setEnabled(false);
                }
                if (setHPagesNumber.getSelection()) {
                    hPagesNum.setEnabled(true);
                    vPagesNum.setEnabled(false);
                    if (currentPage.getSelection()) {
                        currentPage.setSelection(false);
                        allPages.setSelection(true);
                    }
                    if (hPagesNum.getText() == "") { //$NON-NLS-1$ 
                        hPagesNum.setText("1"); //$NON-NLS-1$
                    }
                }
                if (setVPagesNumber.getSelection()) {
                    hPagesNum.setEnabled(false);
                    vPagesNum.setEnabled(true);
                    if (currentPage.getSelection()) {
                        currentPage.setSelection(false);
                        allPages.setSelection(true);
                    }
                    if (vPagesNum.getText() == "") { //$NON-NLS-1$
                        vPagesNum.setText("1"); //$NON-NLS-1$
                    }
                }
                if (currentPage.getSelection() || allPages.getSelection() || pageList.getSelection()) {
                    fromPage.setEnabled(false);
                    toPage.setEnabled(false);
                } else {
                    fromPage.setEnabled(true);
                    toPage.setEnabled(true);
                }

                currentPage.setEnabled(useCurrentZoom.getSelection());
                overviewCanvas.setEnabled(pageList.getSelection());
                if (overviewCanvas.isEnabled() && (e.widget == useCurrentZoom || e.widget == setHPagesNumber || e.widget == setVPagesNumber)) {
                    pagesList = new int[1];
                    pagesList[0] = 1;
                    pageNum = 1;
                    firstPage = 1;
                } else if (overviewCanvas.isEnabled() && (e.widget == pageList)) {
                    if (pagesList == null || pagesList.length <= 0) {
                        pagesList = new int[1];
                        pagesList[0] = 1;
                        pageNum = 1;
                        firstPage = 1;
                    }
                }
                computeStepXY();
                totalPages.setText(Integer.valueOf(maxNumOfPages()).toString());
                overviewCanvas.redraw();
                overviewCanvas.update();
            }

            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                pagesList = new int[0];
                computeStepXY();
                overviewCanvas.redraw();
            }

        };

        modifyListener = new ModifyListener() {
            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
             */
            @Override
            public void modifyText(ModifyEvent e) {
                pagesList = new int[0];
                computeStepXY();
                totalPages.setText(Integer.valueOf(maxNumOfPages()).toString());
                overviewCanvas.redraw();
            }

        };
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * Creates new grid data object.
     * 
     * @param span horizontal span.
     * @return grid data
     */
    protected GridData newGridData(int span) {
        GridData data = new GridData(GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        data.horizontalSpan = span;
        return data;
    }

    /**
     * Creates the dialog area.
     * 
     * @param parent The parent composite
     * @return dialog control
     */
    public Control createDialogArea(Composite parent) {

        GridLayout parentLayout = new GridLayout();
        parentLayout.numColumns = 6;
        parent.setLayout(parentLayout);

        Group g1 = new Group(parent, SWT.SHADOW_NONE);
        g1.setText(SDMessages._113);
        g1.setLayoutData(newGridData(3));
        GridLayout g1layout = new GridLayout();
        g1layout.numColumns = 2;
        g1.setLayout(g1layout);

        useCurrentZoom = new Button(g1, SWT.RADIO);
        useCurrentZoom.setText(SDMessages._112);
        useCurrentZoom.setLayoutData(newGridData(2));
        useCurrentZoom.addSelectionListener(selectionListener);

        setHPagesNumber = new Button(g1, SWT.RADIO);
        setHPagesNumber.setText(SDMessages._110);
        setHPagesNumber.setLayoutData(newGridData(1));
        setHPagesNumber.addSelectionListener(selectionListener);

        hPagesNum = new Text(g1, SWT.SINGLE | SWT.BORDER);
        hPagesNum.addModifyListener(modifyListener);

        setVPagesNumber = new Button(g1, SWT.RADIO);
        setVPagesNumber.setText(SDMessages._111);
        setVPagesNumber.setLayoutData(newGridData(1));
        setVPagesNumber.addSelectionListener(selectionListener);

        vPagesNum = new Text(g1, SWT.SINGLE | SWT.BORDER);
        vPagesNum.addModifyListener(modifyListener);

        Label nbTotal = new Label(g1, SWT.SHADOW_NONE | SWT.RIGHT);
        nbTotal.setText(SDMessages._109);
        // nbTotal.setLayoutData(newGridData(1));

        totalPages = new Text(g1, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        // nbHV.addModifyListener(modifListener);

        Group g2 = new Group(parent, SWT.SHADOW_NONE);
        g2.setText(SDMessages._119);
        GridData data = new GridData(GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        data.horizontalSpan = 3;
        data.verticalSpan = 2;
        g2.setLayoutData(data);
        GridLayout g2layout = new GridLayout();
        // g2layout.
        g2layout.numColumns = 1;
        // SVLayout g2layout = new SVLayout();
        g2.setLayout(g2layout);

        GridData data2 = new GridData(GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        data2.horizontalSpan = 1;
        data2.verticalSpan = 1;

        overviewCanvas = new LocalSD(g2, SWT.NO_BACKGROUND);
        GridData seqDiagLayoutData = new GridData(GridData.HORIZONTAL_ALIGN_FILL// |GridData.GRAB_HORIZONTAL|
                /* GridData.GRAB_VERTICAL| */| GridData.VERTICAL_ALIGN_FILL);
        // seqDiagLayoutData.widthHint=400;
        // seqDiagLayoutData.horizontalAlignment=GridData.HORIZONTAL_ALIGN_FILL;
        overviewCanvas.setLayoutData(seqDiagLayoutData);
        // overviewCanvas.resizeContents(100,100);
        if (view.getContentsWidth() < view.getVisibleWidth() && view.getContentsHeight() < view.getVisibleHeight()) {
            test = 3;
        } else {
            test = 10;
        }
        overviewCanvas.setFrame(view.getFrame(), true);
        overviewCanvas.zoomValue = (float) 1 / test;
        overviewCanvas.setCornerControl(null);
        seqDiagLayoutData.widthHint = overviewCanvas.getContentsWidth() / test;
        seqDiagLayoutData.widthHint = overviewCanvas.getFrame().getWidth() / test + 15;

        if (view.getVisibleWidth() < view.getContentsWidth()) {
            seqDiagLayoutData.widthHint = overviewCanvas.getContentsWidth() / test;
            if (seqDiagLayoutData.widthHint > Display.getDefault().getClientArea().width / 4) {
                seqDiagLayoutData.widthHint = Display.getDefault().getClientArea().width / 4;
            }
        } else {
            seqDiagLayoutData.widthHint = overviewCanvas.getFrame().getWidth() / test + 15;
        }

        if (view.getVisibleHeight() < view.getContentsHeight()) {
            seqDiagLayoutData.heightHint = overviewCanvas.getContentsHeight() / test;
            if (seqDiagLayoutData.heightHint > Display.getDefault().getClientArea().width / 4) {
                seqDiagLayoutData.heightHint = Display.getDefault().getClientArea().width / 4;
            }
        } else {
            seqDiagLayoutData.heightHint = overviewCanvas.getFrame().getHeight() / test;
        }

        overviewCanvas.setEnabled(false);

        currentSelection = new Label(g2, SWT.SHADOW_NONE | SWT.LEFT);
        currentSelection.setLayoutData(newGridData(1));

        Group g3 = new Group(parent, SWT.SHADOW_NONE);
        g3.setText(SDMessages._118);
        g3.setLayoutData(newGridData(3));
        GridLayout g3layout = new GridLayout();
        g3layout.numColumns = 4;
        g3.setLayout(g3layout);

        allPages = new Button(g3, SWT.RADIO);
        allPages.setText(SDMessages._108);
        allPages.setLayoutData(newGridData(4));
        allPages.addSelectionListener(selectionListener);

        currentPage = new Button(g3, SWT.RADIO);
        currentPage.setText(SDMessages._107);
        currentPage.setLayoutData(newGridData(4));
        currentPage.setEnabled(true);
        currentPage.setSelection(true);
        currentPage.addSelectionListener(selectionListener);

        pageList = new Button(g3, SWT.RADIO);
        pageList.setText(SDMessages._106);
        pageList.setLayoutData(newGridData(4));
        pageList.addSelectionListener(selectionListener);

        pageRange = new Button(g3, SWT.RADIO);
        pageRange.setText(SDMessages._103);
        pageRange.setLayoutData(newGridData(1));
        pageRange.addSelectionListener(selectionListener);

        fromPage = new Text(g3, SWT.SINGLE | SWT.BORDER);

        Label labelTo = new Label(g3, SWT.CENTER);
        labelTo.setText(SDMessages._105);

        toPage = new Text(g3, SWT.SINGLE | SWT.BORDER);

        toolTip = new DiagramToolTip(overviewCanvas);

        overviewCanvas.getViewControl().addMouseTrackListener(new MouseTrackListener() {
            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
             */
            @Override
            public void mouseEnter(MouseEvent e) {
                toolTip.hideToolTip();
            }

            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
             */
            @Override
            public void mouseExit(MouseEvent e) {
                toolTip.hideToolTip();
            }

            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
             */
            @Override
            public void mouseHover(MouseEvent e) {
                int x1 = (int) (overviewCanvas.viewToContentsX(e.x) / overviewCanvas.zoomValue / stepX);
                int x2 = (int) (overviewCanvas.viewToContentsY(e.y) / overviewCanvas.zoomValue / stepY);
                int num = x1 + x2 * getNbRow() + 1;
                if (num > maxNumOfPages()) {
                    return;
                }
                if (num > 0) {
                    toolTip.showToolTip(String.valueOf(num));
                    displayPageNum();
                } else {
                    currentSelection.setText("");//$NON-NLS-1$
                    toolTip.hideToolTip();
                }
            }

        });

        overviewCanvas.addTraverseListener(new TraverseListener() {
            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.TraverseListener#keyTraversed(org.eclipse.swt.events.TraverseEvent)
             */
            @Override
            public void keyTraversed(TraverseEvent e) {
                if ((e.detail == SWT.TRAVERSE_TAB_NEXT) || (e.detail == SWT.TRAVERSE_TAB_PREVIOUS)) {
                    e.doit = true;
                }
            }
        });

        overviewCanvas.addFocusListener(new FocusListener() {
            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
             */
            @Override
            public void focusGained(FocusEvent e) {
                overviewCanvas.redraw();
            }

            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
             */
            @Override
            public void focusLost(FocusEvent e) {
                overviewCanvas.redraw();
            }
        });

        if (showPrintButton) {
            Composite printerDlg = new Composite(parent, SWT.NONE);
            data = GridUtil.createHorizontalFill();
            data.horizontalSpan = 6;
            parentLayout = new GridLayout();
            parentLayout.numColumns = 2;
            printerDlg.setLayout(parentLayout);
            printerDlg.setLayoutData(data);

            Label label = new Label(printerDlg, SWT.NONE);
            label.setLayoutData(GridUtil.createHorizontalFill());
            printerDialog = new Button(printerDlg, SWT.PUSH);
            printerDialog.setText(SDMessages._115);

            printerDialog.addSelectionListener(new SelectionListener() {

                /*
                 * (non-Javadoc)
                 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                 */
                @Override
                public void widgetSelected(SelectionEvent e) {

                    printButtonSelected();
                }

                /*
                 * (non-Javadoc)
                 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
                 */
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                }

            });
        }

        updatePrinterStatus();

        return parent;
    }

    /**
     * Get number of pages for selection.
     * @return number of pages for selection.
     */
    public int getPagesForSelection() {
        return nbPages;
    }

    public boolean okPressed() {
        printAll = allPages.getSelection();
        printCurrent = currentPage.getSelection();
        printSelection = pageList.getSelection();
        printRange = pageRange.getSelection();
        try {
            if (printRange) {
                from = Integer.valueOf(fromPage.getText()).intValue();
                to = Integer.valueOf(toPage.getText()).intValue();
                if (from > maxNumOfPages() || to > maxNumOfPages() || from <= 0 || to <= 0) {
                    MessageDialog.openError(getShell(), SDMessages._98, SDMessages._99);
                    return false;
                }
            } else if (setHPagesNumber.getSelection() && nbPages <= 0) {
                MessageDialog.openError(getShell(), SDMessages._98, SDMessages._101);
                return false;
            } else if (setVPagesNumber.getSelection() && nbPages <= 0) {
                MessageDialog.openError(getShell(), SDMessages._98, SDMessages._100);
                return false;
            } else if (printSelection && getPageList().length <= 0) {
                MessageDialog.openError(getShell(), SDMessages._98, SDMessages._102);
                return false;
            }

        } catch (Exception e) {
            MessageDialog.openError(getShell(), SDMessages._98, SDMessages._99);
            from = 0;
            to = 0;
            return false;
        }

        return true;
    }

    /**
     * Draws region that was selected
     * @param img The corresponding image
     * @param r The selected rectangle.
     * @param color The color to use for selection
     * @return
     */
    public ImageData drawRegionSelected(Image img, Rectangle r, RGB color) {
        ImageData id = img.getImageData();
        for (int a = 0; a < r.width && r.x + a < id.width; a++) {
            for (int b = 0; b < r.height && r.y + b < id.height; b++) {
                int index = id.getPixel(r.x + a, r.y + b);
                RGB rgb = id.palette.getRGB(index);
                rgb = combine(color, rgb);
                id.setPixel(r.x + a, r.y + b, id.palette.getPixel(rgb));
            }
        }
        return id;
    }

    /**
     * Combines two RGB colors.
     * @param front The front color
     * @param back The back color
     * @return new RGB color
     */
    public static RGB combine(RGB front, RGB back) {
        int _af = 128;
        if (_af == 1) {
            return front;
        }
        if (_af == 0) {
            return back;
        }
        int _ab = 200;
        if (_ab == 0) {
            return front;
        }

        double af = (_af) / 255.0;
        double rf = front.red;
        double gf = front.green;
        double bf = front.blue;

        double ab = (_ab) / 255.0;
        double rb = back.red;
        double gb = back.green;
        double bb = back.blue;

        double k = (1.0 - af) * ab;
        int r = (int) ((af * rf + k * rb));
        int g = (int) ((af * gf + k * gb));
        int b = (int) ((af * bf + k * bb));

        return new RGB(r, g, b);
    }

    /**
     * Computes value for X coordinates step and Y coordinates step.
     */
    protected void computeStepXY() {
        float cw = overviewCanvas.getContentsWidth() / overviewCanvas.zoomValue;
        float ch = overviewCanvas.getContentsHeight() / overviewCanvas.zoomValue;
        try {
            if (printerData == null) {
                stepX = 0;
                stepY = 0;
                nbPages = 0;
                zoomFactor = 0;
            } else {
                Printer printer = new Printer(printerData);
                if (setHPagesNumber.getSelection()) {
                    nbPages = Integer.valueOf(hPagesNum.getText()).intValue();
                    float z1 = (float) view.getContentsWidth() / (cw);
                    float z2 = printer.getClientArea().width / ((float) view.getContentsWidth() / nbPages);

                    stepY = printer.getClientArea().height / z1 / z2;
                    stepX = cw / nbPages;
                } else if (setVPagesNumber.getSelection()) {
                    nbPages = Integer.valueOf(vPagesNum.getText()).intValue();
                    float z1 = (float) view.getContentsHeight() / (ch);
                    float z2 = printer.getClientArea().height / ((float) view.getContentsHeight() / nbPages);
                    stepX = printer.getClientArea().width / z1 / z2;
                    stepY = ch / nbPages;
                } else {
                    float z1 = view.getContentsWidth() / (cw);
                    stepX = ((float) view.getVisibleWidth() / z1);
                    nbPages = Math.round(cw / stepX);
                    if (nbPages == 0) {
                        nbPages = 1;
                    }
                    int pw = printer.getClientArea().width;
                    int ph = printer.getClientArea().height;
                    float z2 = pw / ((float) view.getContentsWidth() / nbPages);
                    stepY = ((float) ph / z1 / z2);
                }
            }
        } catch (NumberFormatException e) {
            stepX = stepY = nbPages = 0;
            zoomFactor = 0;
        }
        sTX = stepX * (view.getContentsWidth() / cw);
        sTY = stepY * (view.getContentsHeight() / ch);
        float rat = 1;
        if ((view.getVisibleWidth() > view.getContentsWidth()) && (setVPagesNumber.getSelection() || setHPagesNumber.getSelection())) {
            rat = (float) view.getVisibleWidth() / (float) view.getContentsWidth();
        }
        zoomFactor = (overviewCanvas.getContentsWidth() / cw) / overviewCanvas.getZoomFactor() * rat;// /view.getZoomFactor();
    }

    /**
     * @return the pages list.
     */
    public int[] getPageList() {
        return Arrays.copyOf(pagesList, pagesList.length);
    }

    /**
     * Adds a page to pages list.
     * @param num
     */
    public void addToPagesList(int num) {
        int temp[] = new int[pagesList.length + 1];
        System.arraycopy(pagesList, 0, temp, 0, pagesList.length);
        temp[temp.length - 1] = num;
        pagesList = new int[temp.length];
        System.arraycopy(temp, 0, pagesList, 0, temp.length);
    }

    /**
     * Removes a page from the pages list.
     * @param num
     */
    public void removeFromPagesList(int num) {
        int pos = Arrays.binarySearch(pagesList, num);
        int temp[] = new int[pagesList.length - 1];
        System.arraycopy(pagesList, 0, temp, 0, pos);
        System.arraycopy(pagesList, pos + 1, temp, pos, pagesList.length - pos - 1);
        pagesList = new int[temp.length];
        System.arraycopy(temp, 0, pagesList, 0, temp.length);
    }

    /**
     * @return maximum number of pages.
     */
    public int maxNumOfPages() {
        int max = getNbRow() * getNbLines();
        return max;
    }

    /**
     * @return number of rows.
     */
    public int getNbRow() {
        if (!setHPagesNumber.isDisposed()) {
            int cw = (int) (overviewCanvas.getContentsWidth() / overviewCanvas.zoomValue);
            int row = 1;
            if (stepX != 0) {
                row = (int) (cw / stepX);
                if (setHPagesNumber.getSelection()) {
                    row = Math.round((float) cw / stepX);
                } else if ((cw % stepX != 0)) {
                    row++;
                }
            }
            nbRows = row;
        }
        return nbRows;
    }

    /**
     * @return number of lines
     */
    public int getNbLines() {
        if (!setVPagesNumber.isDisposed()) {
            int ch = (int) (overviewCanvas.getContentsHeight() / overviewCanvas.zoomValue);
            int line = 1;
            if (stepY != 0) {
                line = (int) (ch / stepY);
                if (setVPagesNumber.getSelection()) {
                    line = Math.round((float) ch / stepY);
                } else if (ch % stepY != 0) {
                    line++;
                }
            }
            nbLines = line;
        }
        return nbLines;
    }

    /**
     * @return whether to print all pages or not.
     */
    public boolean printAll() {
        return printAll;
    }

    /**
     * @return whether to print only current page.
     */
    public boolean printCurrent() {
        return printCurrent;
    }

    /**
     * @return whether to print selected pages.
     */
    public boolean printSelection() {
        return printSelection;
    }
    
    /**
     * @return whether to print range of pages.
     */
    public boolean printRange() {
        return printRange;
    }

    /**
     * @return step in X direction
     */
    public float getStepX() {
        return sTX;
    }

    /**
     * @return step in Y direction
     */
    public float getStepY() {
        return sTY;
    }

    /**
     * @return zoom factor
     */
    public float getZoomFactor() {
        return zoomFactor;
    }

    /**
     * @return printer data reference
     */
    public PrinterData getPrinterData() {
        return printerData;
    }

    /**
     * @return page number to start printing from
     */
    public int getFrom() {
        return from;
    }

    /**
     * @return page number to print to
     */
    public int getTo() {
        return to;
    }

    /**
     * Displays current number of pages
     */
    protected void displayPageNum() {
        if (pageNum > 0) {
            String message = MessageFormat.format(SDMessages._117, new Object[] { Integer.valueOf(pageNum) });
            currentSelection.setText(message);
            currentSelection.getParent().layout();
        }
    }

    /**
     * @return the shell reference.
     */
    public Shell getShell() {
        return shell;
    }

    /**
     * @param shell The shell reference.
     */
    public void setShell(Shell shell) {
        this.shell = shell;
    }

    /**
     * Handle selection of print button.
     */
    public void printButtonSelected() {
        PrintDialog printer = new PrintDialog(getShell());
        if (allPages.getSelection()) {
            printer.setScope(PrinterData.ALL_PAGES);
        }
        if (currentPage.getSelection()) {
            printer.setScope(PrinterData.SELECTION);
        }
        if (pageList.getSelection()) {
            printer.setScope(PrinterData.SELECTION);
        }
        if (pageRange.getSelection()) {
            printer.setScope(PrinterData.PAGE_RANGE);
            from = Integer.valueOf(fromPage.getText()).intValue();
            to = Integer.valueOf(toPage.getText()).intValue();
            printer.setStartPage(from);
            printer.setEndPage(to);
        }

        PrinterData newPrinterData = printer.open();
        if (newPrinterData != null) {
            printerData = newPrinterData;
        }
        updatePrinterStatus();

        if (printer.getScope() == PrinterData.ALL_PAGES) {
            allPages.setSelection(true);
            currentPage.setSelection(false);
            pageList.setSelection(false);
            pageRange.setSelection(false);
            hPagesNum.setEnabled(false);
            vPagesNum.setEnabled(false);
        }
        if (printer.getScope() == PrinterData.PAGE_RANGE) {
            allPages.setSelection(false);
            currentPage.setSelection(false);
            pageList.setSelection(false);
            pageRange.setSelection(true);
            fromPage.setEnabled(true);
            toPage.setEnabled(true);
            fromPage.setText((Integer.valueOf(printer.getStartPage())).toString());
            toPage.setText((Integer.valueOf(printer.getEndPage())).toString());
        }
        computeStepXY();
        overviewCanvas.redraw();
    }

    /**
     * Sets parent wizard page
     * 
     * @param parent The parent wizard page
     */
    public void setParentWizardPage(WizardPage parent) {
        parentWizardPage = parent;
    }

    /**
     * Sets the parent dialog box.
     * 
     * @param parent The parent dialog box. 
     */
    public void setParentDialog(SDPrintDialog parent) {
        parentDialog = parent;
    }

    /**
     * Updates the printer status
     */
    protected void updatePrinterStatus() {
        if (parentWizardPage != null) {
            // used in the wizard dialog
            if (printerData == null) {
                // show error message and disable Finish button
                parentWizardPage.setErrorMessage(SDMessages._135);
                parentWizardPage.setPageComplete(false);
            } else {
                // clear error message and enable Finish button
                parentWizardPage.setErrorMessage(null);
                parentWizardPage.setPageComplete(true);
            }
        } else if (parentDialog != null) {
            // used in the print dialog
            if (printerData == null) {
                // show error message and disable OK button
                parentDialog.setErrorMessage(SDMessages._135);
                parentDialog.setPageComplete(false);
            } else {
                // clear error message and enable OK button
                parentDialog.setErrorMessage(null);
                parentDialog.setPageComplete(true);
            }
        }
    }

}
