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

package org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs;

import java.text.MessageFormat;
import java.util.Arrays;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.DiagramToolTip;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.NGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.Messages;
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
    private Button fSetHPagesNumber;
    /**
     * The set vertical pages number.
     */
    private Button fSetVPagesNumber;
    /**
     * Flag whether to use current zoom or not.
     */
    private Button fUseCurrentZoom;
    /**
     * Flag whether to print all pages or not
     */
    private Button fAllPages;
    /**
     * Flag whether to print current page only
     */
    private Button fCurrentPage;
    /**
     * Button to select a page list.
     */
    private Button fPageList;
    /**
     * Button to select a page range.
     */
    private Button fPageRange;
    /**
     * Text field to enter from page.
     */
    private Text fFromPage;
    /**
     * Text field to enter to page.
     */
    private Text fToPage;
    /**
     *  The sequence diagram widget reference.
     */
    private SDWidget fSdView;
    /**
     * Text field for number of horizontal pages
     */
    private Text fHorPagesNum;
    /**
     * Text field for number of vertical pages
     */
    private Text fVertPagesNum;
    /**
     * Text field for toal number of pages
     */
    private Text fTotalPages;
    /**
     * A modify listener implementation to handle modifications.
     */
    private ModifyListener fModifyListener;
    /**
     * A selection listener implementation to handle selections.
     */
    private SelectionListener fSelectionListener;
    /**
     * Local canvas displaying sequence diagram overview.
     */
    private LocalSD fOverviewCanvas;
    /**
     * Number of pages
     */
    private int fNbPages = 0;
    /**
     * Number of selected pages.
     */
    private int fPageNum = -1;
    /**
     * Number of first page.
     */
    private int fFirstPage = -1;
    /**
     * List of pages to print.
     */
    private int fPagesList[];

    /**
     * Value for dividing the sequence diagram into pages
     */
    private float fStepX;

    /**
     * Value for dividing the sequence diagram into pages
     */
    private float fStepY;

    /**
     * Value for dividing the sequence diagram into pages
     */
    private float sTX;

    /**
     * Value for dividing the sequence diagram into pages
     */
    private float sTY;

    /**
     * Page which to print from.
     */
    private int fFrom;
    /**
     * Page which to print to.
     */
    private int fTo;
    /**
     * Flag for enabling multi-selection.
     */
    private boolean fMultiSelection = false;
    /**
     * Flag for enabling area selection.
     */
    private boolean fAreaSelection = false;
    /**
     * Flag for printing all.
     */
    private boolean fPrintAll;
    /**
     * Flag for printing current page only.
     */
    private boolean fPrintCurrent;
    /**
     * Flag for printing a selection of pages.
     */
    private boolean fPrintSelection;
    /**
     * Flag for printing a range of pages.
     */
    private boolean fPrintRange;
    /**
     * Number of selected rows
     */
    private int fNbRows;
    /**
     * Number of selected lines
     */
    private int fNbLines;
    /**
     * The zoom factor.
     */
    private float fZoomFactor;
    /**
     * The printer data reference.
     */
    private PrinterData fPrinterData;
    /**
     * The diagram tooltip to show if necessary.
     */
    private DiagramToolTip fToolTip = null;
    /**
     * Label for current selection.
     */
    private Label fCurrentSelection;
    /**
     * The shell reference.
     */
    private Shell fShell;
    /**
     * Button to open printer dialog from OS.
     */
    private Button fPrinterDialog;
    /**
     * Flag for showing print button.
     */
    private boolean fShowPrintButton;
    /**
     * Test value
     */
    private int fTest = 3;
    /**
     * Parent wizard page if used as wizard
     */
    private WizardPage fParentWizardPage = null;
    /**
     * Reference to parent print dialog.
     */
    private SDPrintDialog fParentDialog = null;

    // ------------------------------------------------------------------------
    // Helper Class
    // ------------------------------------------------------------------------
    /**
     * Local sequence diagram widget used to display overview of sequence diagram to print.
     * @version 1.0
     */
    private class LocalSD extends SDWidget {

        /**
         * Constructor
         * @param c Parent composite
         * @param s Style bits
         */
        public LocalSD(Composite c, int s) {
            super(c, s);
        }

        @Override
        public int getContentsHeight() {
            if (fSdView.getContentsHeight() > fSdView.getContentsHeight()) {
                return (int) (fSdView.getVisibleHeight() / (float) fTest / fSdView.getZoomValue());
            }
            return super.getContentsHeight();
        }

        @Override
        public int getContentsWidth() {
            if (fSdView.getVisibleWidth() > fSdView.getContentsWidth()) {
                return (int) (fSdView.getVisibleWidth() / (float) fTest / fSdView.getZoomValue());
            }
            return super.getContentsWidth();
        }

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

            for (int pageIndex = 0; pageIndex < fPagesList.length; pageIndex++) {

                int pageNum = fPagesList[pageIndex];

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
                    if (fOverviewCanvas.isFocusControl()) {
                        over = new Image(super.getShell().getDisplay(), drawRegionSelected(toDel, new Rectangle(contentsToViewX((int) (row * stepX * fOverviewCanvas.getZoomValue())), contentsToViewY((int) (line * stepY * fOverviewCanvas.getZoomValue())),
                                ((int) (stepX * fOverviewCanvas.getZoomValue())), ((int) (stepY * fOverviewCanvas.getZoomValue()))), new RGB(0, 0, 128)));
                    } else {
                        over = new Image(super.getShell().getDisplay(), drawRegionSelected(toDel, new Rectangle(contentsToViewX((int) (row * stepX * fOverviewCanvas.getZoomValue())), contentsToViewY((int) (line * stepY * fOverviewCanvas.getZoomValue())),
                                ((int) (stepX * fOverviewCanvas.getZoomValue())), ((int) (stepY * fOverviewCanvas.getZoomValue()))), new RGB(221, 208, 200)));
                    }
                    toDel.dispose();
                }
            }

            Arrays.sort(fPagesList);
            int pos = Arrays.binarySearch(fPagesList, fPageNum);
            if ((pos < 0) && (getPagesForSelection() > 0 && fPageNum > 0)) {
                int line = fPageNum / getNbRow();
                int row = fPageNum % getNbRow();
                if (row != 0) {
                    line++;
                } else {
                    row = getNbRow();
                }

                line--;
                row--;

                Image toDel = over;
                over = new Image(super.getShell().getDisplay(), drawRegionSelected(toDel, new Rectangle(contentsToViewX((int) (row * stepX * fOverviewCanvas.getZoomValue())), contentsToViewY((int) (line * stepY * fOverviewCanvas.getZoomValue())),
                        ((int) (stepX * fOverviewCanvas.getZoomValue())), ((int) (stepY * fOverviewCanvas.getZoomValue()))), new RGB(221, 208, 200)));
                toDel.dispose();
            }

            GC imGC2 = new GC(over);
            imGC2.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
            NGC imGC = new NGC(fOverviewCanvas, imGC2);
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

        @Override
        protected void drawContents(GC gc, int clipx, int clipy, int clipw, int cliph) {

            Image dbuffer = getDrawBuffer();
            computeStepXY();
            Image d;

            int lw = (int) (getContentsWidth() / getZoomValue());
            if (getContentsWidth() < getVisibleWidth()) {
                lw = (int) (getVisibleWidth() / getZoomValue());
            }

            int lh = (int) (getContentsHeight() / getZoomValue());
            if (getContentsHeight() < getVisibleHeight()) {
                lh = (int) (getVisibleHeight() / getZoomValue());
            }
            d = createPagesSelectionImages(dbuffer, lw, fStepX, lh, fStepY);

            if (!isEnabled()) {
                Image toDel = d;
                d = new Image(super.getShell().getDisplay(), drawRegionSelected(d, new Rectangle(0, 0, lw, lh), new RGB(221, 208, 200)));
                toDel.dispose();
            }

            Rectangle area = getClientArea();
            int w = d.getBounds().width;
            int h = d.getBounds().height;
            gc.drawImage(d, 0, 0, w, h, 0, 0, area.width, area.height);

            fTotalPages.setText(Integer.valueOf(maxNumOfPages()).toString());
            displayPageNum();
            dbuffer.dispose();
            d.dispose();
            gc.dispose();
        }

        @Override
        protected void keyPressedEvent(KeyEvent e) {
            if (e.keyCode == SWT.CTRL) {
                fMultiSelection = true;
            }
            if (e.keyCode == SWT.SHIFT) {
                fAreaSelection = true;
            }
            if (e.keyCode == SWT.ARROW_DOWN) {
                if (fPageNum + getNbRow() <= maxNumOfPages()) {
                    fPageNum += getNbRow();
                }
                int line = fPageNum / getNbRow();
                int row = fPageNum % getNbRow();
                if (row == 0) {
                    line--;
                }
                if ((line + 1) * fStepY > (fOverviewCanvas.getContentsY() + fOverviewCanvas.getVisibleHeight()) / fOverviewCanvas.getZoomValue()) {
                    fOverviewCanvas.scrollBy(0, (int) (fStepY * fOverviewCanvas.getZoomValue()));
                }
            }
            if (e.keyCode == SWT.ARROW_UP) {
                if (fPageNum - getNbRow() > 0) {
                    fPageNum -= getNbRow();
                }
                int line = fPageNum / getNbRow();
                int row = fPageNum % getNbRow();
                if (row == 0) {
                    line--;
                }
                if ((line) * fStepY <= fOverviewCanvas.getContentsY() / fOverviewCanvas.getZoomValue()) {
                    fOverviewCanvas.scrollBy(0, -(int) (fStepY * fOverviewCanvas.getZoomValue()));
                }
            }
            if (e.keyCode == SWT.ARROW_LEFT) {
                if ((fPageNum - 2) / getNbRow() == (fPageNum - 1) / getNbRow() && fPageNum > 1) {
                    fPageNum--;
                }
                int row = fPageNum % getNbRow();
                if ((row - 1) * fStepX < (fOverviewCanvas.getContentsX()) / fOverviewCanvas.getZoomValue()) {
                    fOverviewCanvas.scrollBy(-(int) (fStepX * fOverviewCanvas.getZoomValue()), 0);
                }
            }
            if (e.keyCode == SWT.ARROW_RIGHT) {
                if ((fPageNum - 1) / getNbRow() == fPageNum / getNbRow()) {
                    fPageNum++;
                }
                int row = fPageNum % getNbRow();
                if (row == 0) {
                    row = getNbRow();
                }
                if ((row) * fStepX > (fOverviewCanvas.getContentsX() + fOverviewCanvas.getVisibleWidth()) / fOverviewCanvas.getZoomValue()) {
                    fOverviewCanvas.scrollBy((int) (fStepX * fOverviewCanvas.getZoomValue()), 0);
                }
            }

            if (e.keyCode == 32 && fPageNum > -1) {
                Arrays.sort(fPagesList);
                int pos = Arrays.binarySearch(fPagesList, fPageNum);
                if (pos < 0) {
                    addToPagesList(fPageNum);
                } else {
                    removeFromPagesList(fPageNum);
                }
            }

            if (!fAreaSelection && !fMultiSelection) {
                fFirstPage = fPageNum;
                fPagesList = new int[1];
                fPagesList[0] = fPageNum;
            } else if ((fPageNum != -1) && (fAreaSelection) && (fFirstPage != -1)) {
                fPagesList = new int[0];
                int line1 = fFirstPage / getNbRow();
                int row1 = fFirstPage % getNbRow();
                if (row1 != 0) {
                    line1++;
                } else {
                    row1 = getNbRow();
                }

                int line2 = fPageNum / getNbRow();
                int row2 = fPageNum % getNbRow();
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
            fOverviewCanvas.redraw();
        }

        @Override
        protected void keyReleasedEvent(KeyEvent e) {
            if (e.keyCode == SWT.CTRL) {
                fMultiSelection = false;
            }
            if (e.keyCode == SWT.SHIFT) {
                fAreaSelection = false;
            }
        }

        @Override
        protected void contentsMouseDownEvent(MouseEvent event) {

            computeStepXY();
            int x1 = (int) ((event.x / fOverviewCanvas.getZoomValue()) / fStepX);
            int x2 = (int) ((event.y / fOverviewCanvas.getZoomValue()) / fStepY);

            int oldPage = fPageNum;

            fPageNum = x1 + x2 * getNbRow() + 1;

            if (fPageNum > maxNumOfPages()) {
                fPageNum = oldPage;
                return;
            }

            if (!fAreaSelection) {
                fFirstPage = fPageNum;
            }

            if ((fPageNum != -1) && (fMultiSelection)) {
                Arrays.sort(fPagesList);
                int pos = Arrays.binarySearch(fPagesList, fPageNum);
                if (pos < 0) {
                    addToPagesList(fPageNum);
                } else {
                    removeFromPagesList(fPageNum);
                }
            } else if ((fPageNum != -1) && (fAreaSelection) && (fFirstPage != -1)) {

                fPagesList = new int[0];

                int line1 = fFirstPage / getNbRow();
                int row1 = fFirstPage % getNbRow();
                if (row1 != 0) {
                    line1++;
                } else {
                    row1 = getNbRow();
                }

                int line2 = fPageNum / getNbRow();
                int row2 = fPageNum % getNbRow();
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
                fPagesList = new int[1];
                fPagesList[0] = fPageNum;
            }
            if ((event.stateMask & SWT.CTRL) != 0) {
                fMultiSelection = true;
            }
            displayPageNum();
            redraw();
        }

        @Override
        protected void contentsMouseMoveEvent(MouseEvent e) {
            fToolTip.hideToolTip();
        }

        @Override
        public void resizeContents(int w, int h) {
            super.resizeContents(w, h);
        }

    }

    /**
     * A traverse listener implementation.
     */
    protected static class LocalTraverseListener implements TraverseListener {
        @Override
        public void keyTraversed(TraverseEvent e) {
            if ((e.detail == SWT.TRAVERSE_TAB_NEXT) || (e.detail == SWT.TRAVERSE_TAB_PREVIOUS)) {
                e.doit = true;
            }
        }
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param shell
     *            The shell reference
     * @param sdWidget
     *            The sequence diagram widget reference
     */
    public SDPrintDialogUI(Shell shell, SDWidget sdWidget) {
        this(shell, sdWidget, false);
    }

    /**
     * Constructor
     *
     * @param shell
     *            The shell reference
     * @param sdWidget
     *            The sequence diagram widget reference
     * @param showPrintBtn
     *            Flag for showing print buttons
     */
    public SDPrintDialogUI(Shell shell, SDWidget sdWidget, boolean showPrintBtn) {
        fShell = shell;
        fSdView = sdWidget;
        fShowPrintButton = showPrintBtn;

        fPrinterData = Printer.getDefaultPrinterData();
        if (fPrinterData != null) {
            fPrinterData.scope = PrinterData.SELECTION;
        }

        fPagesList = new int[0];

        fSelectionListener = new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fUseCurrentZoom.getSelection()) {
                    fHorPagesNum.setEnabled(false);
                    fVertPagesNum.setEnabled(false);
                }
                if (fSetHPagesNumber.getSelection()) {
                    fHorPagesNum.setEnabled(true);
                    fVertPagesNum.setEnabled(false);
                    if (fCurrentPage.getSelection()) {
                        fCurrentPage.setSelection(false);
                        fAllPages.setSelection(true);
                    }
                    if ("".equals(fHorPagesNum.getText())) { //$NON-NLS-1$
                        fHorPagesNum.setText("1"); //$NON-NLS-1$
                    }
                }
                if (fSetVPagesNumber.getSelection()) {
                    fHorPagesNum.setEnabled(false);
                    fVertPagesNum.setEnabled(true);
                    if (fCurrentPage.getSelection()) {
                        fCurrentPage.setSelection(false);
                        fAllPages.setSelection(true);
                    }
                    if ("".equals(fVertPagesNum.getText())) { //$NON-NLS-1$
                        fVertPagesNum.setText("1"); //$NON-NLS-1$
                    }
                }
                if (fCurrentPage.getSelection() || fAllPages.getSelection() || fPageList.getSelection()) {
                    fFromPage.setEnabled(false);
                    fToPage.setEnabled(false);
                } else {
                    fFromPage.setEnabled(true);
                    fToPage.setEnabled(true);
                }

                fCurrentPage.setEnabled(fUseCurrentZoom.getSelection());
                fOverviewCanvas.setEnabled(fPageList.getSelection());
                if (fOverviewCanvas.isEnabled() && (e.widget == fUseCurrentZoom || e.widget == fSetHPagesNumber || e.widget == fSetVPagesNumber)) {
                    fPagesList = new int[1];
                    fPagesList[0] = 1;
                    fPageNum = 1;
                    fFirstPage = 1;
                } else if ((fOverviewCanvas.isEnabled() && (e.widget == fPageList)) &&
                           (fPagesList == null || fPagesList.length <= 0)) {

                    fPagesList = new int[1];
                    fPagesList[0] = 1;
                    fPageNum = 1;
                    fFirstPage = 1;
                }
                computeStepXY();
                fTotalPages.setText(Integer.valueOf(maxNumOfPages()).toString());
                fOverviewCanvas.redraw();
                fOverviewCanvas.update();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                fPagesList = new int[0];
                computeStepXY();
                fOverviewCanvas.redraw();
            }

        };

        fModifyListener = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                fPagesList = new int[0];
                computeStepXY();
                fTotalPages.setText(Integer.valueOf(maxNumOfPages()).toString());
                fOverviewCanvas.redraw();
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
        g1.setText(Messages.SequenceDiagram_ZoomOption);
        g1.setLayoutData(newGridData(3));
        GridLayout g1layout = new GridLayout();
        g1layout.numColumns = 2;
        g1.setLayout(g1layout);

        fUseCurrentZoom = new Button(g1, SWT.RADIO);
        fUseCurrentZoom.setText(Messages.SequenceDiagram_UseCurrentZoom);
        fUseCurrentZoom.setLayoutData(newGridData(2));
        fUseCurrentZoom.addSelectionListener(fSelectionListener);

        fSetHPagesNumber = new Button(g1, SWT.RADIO);
        fSetHPagesNumber.setText(Messages.SequenceDiagram_NumberOfHorizontalPages);
        fSetHPagesNumber.setLayoutData(newGridData(1));
        fSetHPagesNumber.addSelectionListener(fSelectionListener);

        fHorPagesNum = new Text(g1, SWT.SINGLE | SWT.BORDER);
        fHorPagesNum.addModifyListener(fModifyListener);

        fSetVPagesNumber = new Button(g1, SWT.RADIO);
        fSetVPagesNumber.setText(Messages.SequenceDiagram_NumberOfVerticalPages);
        fSetVPagesNumber.setLayoutData(newGridData(1));
        fSetVPagesNumber.addSelectionListener(fSelectionListener);

        fVertPagesNum = new Text(g1, SWT.SINGLE | SWT.BORDER);
        fVertPagesNum.addModifyListener(fModifyListener);

        Label nbTotal = new Label(g1, SWT.SHADOW_NONE | SWT.RIGHT);
        nbTotal.setText(Messages.TotalNumberOfPages);
        // nbTotal.setLayoutData(newGridData(1));

        fTotalPages = new Text(g1, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        // nbHV.addModifyListener(modifListener);

        Group g2 = new Group(parent, SWT.SHADOW_NONE);
        g2.setText(Messages.SequenceDiagram_Preview);
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

        fOverviewCanvas = new LocalSD(g2, SWT.NO_BACKGROUND);
        GridData seqDiagLayoutData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
        fOverviewCanvas.setLayoutData(seqDiagLayoutData);
        // overviewCanvas.resizeContents(100,100);
        if (fSdView.getContentsWidth() < fSdView.getVisibleWidth() && fSdView.getContentsHeight() < fSdView.getVisibleHeight()) {
            fTest = 3;
        } else {
            fTest = 10;
        }
        fOverviewCanvas.setFrame(fSdView.getFrame(), true);
        fOverviewCanvas.setZoomValue((float) 1 / fTest);
        fOverviewCanvas.setCornerControl(null);
        seqDiagLayoutData.widthHint = fOverviewCanvas.getContentsWidth() / fTest;
        seqDiagLayoutData.widthHint = fOverviewCanvas.getFrame().getWidth() / fTest + 15;

        if (fSdView.getVisibleWidth() < fSdView.getContentsWidth()) {
            seqDiagLayoutData.widthHint = fOverviewCanvas.getContentsWidth() / fTest;
            if (seqDiagLayoutData.widthHint > Display.getDefault().getClientArea().width / 4) {
                seqDiagLayoutData.widthHint = Display.getDefault().getClientArea().width / 4;
            }
        } else {
            seqDiagLayoutData.widthHint = fOverviewCanvas.getFrame().getWidth() / fTest + 15;
        }

        if (fSdView.getVisibleHeight() < fSdView.getContentsHeight()) {
            seqDiagLayoutData.heightHint = fOverviewCanvas.getContentsHeight() / fTest;
            if (seqDiagLayoutData.heightHint > Display.getDefault().getClientArea().width / 4) {
                seqDiagLayoutData.heightHint = Display.getDefault().getClientArea().width / 4;
            }
        } else {
            seqDiagLayoutData.heightHint = fOverviewCanvas.getFrame().getHeight() / fTest;
        }

        fOverviewCanvas.setEnabled(false);

        fCurrentSelection = new Label(g2, SWT.SHADOW_NONE | SWT.LEFT);
        fCurrentSelection.setLayoutData(newGridData(1));

        Group g3 = new Group(parent, SWT.SHADOW_NONE);
        g3.setText(Messages.SequenceDiagram_PrintRange);
        g3.setLayoutData(newGridData(3));
        GridLayout g3layout = new GridLayout();
        g3layout.numColumns = 4;
        g3.setLayout(g3layout);

        fAllPages = new Button(g3, SWT.RADIO);
        fAllPages.setText(Messages.SequenceDiagram_AllPages);
        fAllPages.setLayoutData(newGridData(4));
        fAllPages.addSelectionListener(fSelectionListener);

        fCurrentPage = new Button(g3, SWT.RADIO);
        fCurrentPage.setText(Messages.SequenceDiagram_CurrentView);
        fCurrentPage.setLayoutData(newGridData(4));
        fCurrentPage.setEnabled(true);
        fCurrentPage.setSelection(true);
        fCurrentPage.addSelectionListener(fSelectionListener);

        fPageList = new Button(g3, SWT.RADIO);
        fPageList.setText(Messages.SequenceDiagram_SelectedPages);
        fPageList.setLayoutData(newGridData(4));
        fPageList.addSelectionListener(fSelectionListener);

        fPageRange = new Button(g3, SWT.RADIO);
        fPageRange.setText(Messages.SequenceDiagram_FromPage);
        fPageRange.setLayoutData(newGridData(1));
        fPageRange.addSelectionListener(fSelectionListener);

        fFromPage = new Text(g3, SWT.SINGLE | SWT.BORDER);

        Label labelTo = new Label(g3, SWT.CENTER);
        labelTo.setText(Messages.SequenceDiagram_to);

        fToPage = new Text(g3, SWT.SINGLE | SWT.BORDER);

        fToolTip = new DiagramToolTip(fOverviewCanvas);

        fOverviewCanvas.getViewControl().addMouseTrackListener(new MouseTrackListener() {
            @Override
            public void mouseEnter(MouseEvent e) {
                fToolTip.hideToolTip();
            }

            @Override
            public void mouseExit(MouseEvent e) {
                fToolTip.hideToolTip();
            }

            @Override
            public void mouseHover(MouseEvent e) {
                int x1 = (int) (fOverviewCanvas.viewToContentsX(e.x) / fOverviewCanvas.getZoomValue() / fStepX);
                int x2 = (int) (fOverviewCanvas.viewToContentsY(e.y) / fOverviewCanvas.getZoomValue() / fStepY);
                int num = x1 + x2 * getNbRow() + 1;
                if (num > maxNumOfPages()) {
                    return;
                }
                if (num > 0) {
                    fToolTip.showToolTip(String.valueOf(num));
                    displayPageNum();
                } else {
                    fCurrentSelection.setText("");//$NON-NLS-1$
                    fToolTip.hideToolTip();
                }
            }

        });

        fOverviewCanvas.addTraverseListener(new LocalTraverseListener());

        fOverviewCanvas.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                fOverviewCanvas.redraw();
            }

            @Override
            public void focusLost(FocusEvent e) {
                fOverviewCanvas.redraw();
            }
        });

        if (fShowPrintButton) {
            Composite printerDlg = new Composite(parent, SWT.NONE);
            data = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
            data.horizontalSpan = 6;
            parentLayout = new GridLayout();
            parentLayout.numColumns = 2;
            printerDlg.setLayout(parentLayout);
            printerDlg.setLayoutData(data);

            Label label = new Label(printerDlg, SWT.NONE);
            label.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
            fPrinterDialog = new Button(printerDlg, SWT.PUSH);
            fPrinterDialog.setText(Messages.SequenceDiagram_Printer);

            fPrinterDialog.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    printButtonSelected();
                }

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
        return fNbPages;
    }

    /**
     * Handler for when the OK button is pressed
     *
     * @return True if the operation was successful, false if there was an error
     */
    public boolean okPressed() {
        fPrintAll = fAllPages.getSelection();
        fPrintCurrent = fCurrentPage.getSelection();
        fPrintSelection = fPageList.getSelection();
        fPrintRange = fPageRange.getSelection();
        try {
            if (fPrintRange) {
                fFrom = Integer.valueOf(fFromPage.getText()).intValue();
                fTo = Integer.valueOf(fToPage.getText()).intValue();
                if (fFrom > maxNumOfPages() || fTo > maxNumOfPages() || fFrom <= 0 || fTo <= 0) {
                    MessageDialog.openError(getShell(), Messages.SequenceDiagram_Error, Messages.SequenceDiagram_InvalidRange);
                    return false;
                }
            } else if (fSetHPagesNumber.getSelection() && fNbPages <= 0) {
                MessageDialog.openError(getShell(), Messages.SequenceDiagram_Error, Messages.SequenceDiagram_InvalidNbHorizontal);
                return false;
            } else if (fSetVPagesNumber.getSelection() && fNbPages <= 0) {
                MessageDialog.openError(getShell(), Messages.SequenceDiagram_Error, Messages.SequenceDiagram_InvalidNbVertical);
                return false;
            } else if (fPrintSelection && getPageList().length <= 0) {
                MessageDialog.openError(getShell(), Messages.SequenceDiagram_Error, Messages.SequenceDiagram_NoPageSelected);
                return false;
            }

        } catch (Exception e) {
            MessageDialog.openError(getShell(), Messages.SequenceDiagram_Error, Messages.SequenceDiagram_InvalidRange);
            fFrom = 0;
            fTo = 0;
            return false;
        }

        return true;
    }

    /**
     * Draws region that was selected
     * @param img The corresponding image
     * @param r The selected rectangle.
     * @param color The color to use for selection
     * @return image data reference
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
        int _ab = 200;

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
        float cw = fOverviewCanvas.getContentsWidth() / fOverviewCanvas.getZoomValue();
        float ch = fOverviewCanvas.getContentsHeight() / fOverviewCanvas.getZoomValue();
        try {
            if (fPrinterData == null) {
                fStepX = 0;
                fStepY = 0;
                fNbPages = 0;
                fZoomFactor = 0;
            } else {
                Printer printer = new Printer(fPrinterData);
                if (fSetHPagesNumber.getSelection()) {
                    fNbPages = Integer.valueOf(fHorPagesNum.getText()).intValue();
                    float z1 = fSdView.getContentsWidth() / cw;
                    float z2 = printer.getClientArea().width / ((float) fSdView.getContentsWidth() / fNbPages);

                    fStepY = printer.getClientArea().height / z1 / z2;
                    fStepX = cw / fNbPages;
                } else if (fSetVPagesNumber.getSelection()) {
                    fNbPages = Integer.valueOf(fVertPagesNum.getText()).intValue();
                    float z1 = fSdView.getContentsHeight() / ch;
                    float z2 = printer.getClientArea().height / ((float) fSdView.getContentsHeight() / fNbPages);
                    fStepX = printer.getClientArea().width / z1 / z2;
                    fStepY = ch / fNbPages;
                } else {
                    float z1 = fSdView.getContentsWidth() / (cw);
                    fStepX = fSdView.getVisibleWidth() / z1;
                    fNbPages = Math.round(cw / fStepX);
                    if (fNbPages == 0) {
                        fNbPages = 1;
                    }
                    int pw = printer.getClientArea().width;
                    int ph = printer.getClientArea().height;
                    float z2 = pw / ((float) fSdView.getContentsWidth() / fNbPages);
                    fStepY = ph / z1 / z2;
                }
            }
        } catch (NumberFormatException e) {
            fStepX = fStepY = fNbPages = 0;
            fZoomFactor = 0;
        }
        sTX = fStepX * (fSdView.getContentsWidth() / cw);
        sTY = fStepY * (fSdView.getContentsHeight() / ch);
        float rat = 1;
        if ((fSdView.getVisibleWidth() > fSdView.getContentsWidth()) && (fSetVPagesNumber.getSelection() || fSetHPagesNumber.getSelection())) {
            rat = (float) fSdView.getVisibleWidth() / (float) fSdView.getContentsWidth();
        }
        fZoomFactor = (fOverviewCanvas.getContentsWidth() / cw) / fOverviewCanvas.getZoomFactor() * rat;
    }

    /**
     * Returns the pages list.
     *
     * @return the pages list.
     */
    public int[] getPageList() {
        return Arrays.copyOf(fPagesList, fPagesList.length);
    }

    /**
     * Adds a page to pages list.
     *
     * @param num
     *            The number of the the new page
     */
    public void addToPagesList(int num) {
        int temp[] = new int[fPagesList.length + 1];
        System.arraycopy(fPagesList, 0, temp, 0, fPagesList.length);
        temp[temp.length - 1] = num;
        fPagesList = new int[temp.length];
        System.arraycopy(temp, 0, fPagesList, 0, temp.length);
    }

    /**
     * Removes a page from the pages list.
     *
     * @param num
     *            The number of the page to remove
     */
    public void removeFromPagesList(int num) {
        int pos = Arrays.binarySearch(fPagesList, num);
        int temp[] = new int[fPagesList.length - 1];
        System.arraycopy(fPagesList, 0, temp, 0, pos);
        System.arraycopy(fPagesList, pos + 1, temp, pos, fPagesList.length - pos - 1);
        fPagesList = new int[temp.length];
        System.arraycopy(temp, 0, fPagesList, 0, temp.length);
    }

    /**
     * Returns the maximum number of pages.
     *
     * @return maximum number of pages.
     */
    public int maxNumOfPages() {
        return (getNbRow() * getNbLines());
    }

    /**
     * Returns the number of rows.
     *
     * @return number of rows.
     */
    public int getNbRow() {
        if (!fSetHPagesNumber.isDisposed()) {
            int cw = (int) (fOverviewCanvas.getContentsWidth() / fOverviewCanvas.getZoomValue());
            int row = 1;
            if (fStepX != 0) {
                row = (int) (cw / fStepX);
                if (fSetHPagesNumber.getSelection()) {
                    row = Math.round(cw / fStepX);
                } else if ((cw % fStepX != 0)) {
                    row++;
                }
            }
            fNbRows = row;
        }
        return fNbRows;
    }

    /**
     * Returns the number of lines.
     *
     * @return number of lines
     */
    public int getNbLines() {
        if (!fSetVPagesNumber.isDisposed()) {
            int ch = (int) (fOverviewCanvas.getContentsHeight() / fOverviewCanvas.getZoomValue());
            int line = 1;
            if (fStepY != 0) {
                line = (int) (ch / fStepY);
                if (fSetVPagesNumber.getSelection()) {
                    line = Math.round(ch / fStepY);
                } else if (ch % fStepY != 0) {
                    line++;
                }
            }
            fNbLines = line;
        }
        return fNbLines;
    }

    /**
     * Returns whether to print all pages or not.
     *
     * @return <code>true</code> for all pages else <code>false</code>.
     */
    public boolean printAll() {
        return fPrintAll;
    }

    /**
     * Returns whether to print only current page
     *
     * @return <code>true</code> for current page only else <code>false</code>..
     */
    public boolean printCurrent() {
        return fPrintCurrent;
    }

    /**
     * Returns whether to print selected pages.
     *
     * @return <code>true</code> for selected pages only else <code>false</code>.
     */
    public boolean printSelection() {
        return fPrintSelection;
    }

    /**
     * Returns whether to print range of pages.
     *
     * @return <code>true</code> for range of pages only else <code>false</code>.
     */
    public boolean printRange() {
        return fPrintRange;
    }

    /**
     * Returns the step in X direction.
     *
     * @return step in X direction
     */
    public float getStepX() {
        return sTX;
    }

    /**
     * Returns the step in Y direction.
     *
     * @return step in Y direction
     */
    public float getStepY() {
        return sTY;
    }

    /**
     * Returns the zoom factor
     *
     * @return zoom factor
     */
    public float getZoomFactor() {
        return fZoomFactor;
    }

    /**
     * Returns the printer data reference.
     *
     * @return printer data reference
     */
    public PrinterData getPrinterData() {
        return fPrinterData;
    }

    /**
     * Returns the page number to start printing from.
     *
     * @return page number to start printing from
     */
    public int getFrom() {
        return fFrom;
    }

    /**
     * Returns the page number to print to.
     *
     * @return page number to print to
     */
    public int getTo() {
        return fTo;
    }

    /**
     * Displays current number of pages
     */
    protected void displayPageNum() {
        if (fPageNum > 0) {
            String message = MessageFormat.format(Messages.SequenceDiagram_Page, new Object[] { Integer.valueOf(fPageNum) });
            fCurrentSelection.setText(message);
            fCurrentSelection.getParent().layout();
        }
    }

    /**
     * Returns the shell reference.
     *
     * @return the shell reference.
     */
    public Shell getShell() {
        return fShell;
    }

    /**
     * Sets the shell.
     *
     * @param shell The shell reference.
     */
    public void setShell(Shell shell) {
        fShell = shell;
    }

    /**
     * Handle selection of print button.
     */
    public void printButtonSelected() {
        PrintDialog printer = new PrintDialog(getShell());
        if (fAllPages.getSelection()) {
            printer.setScope(PrinterData.ALL_PAGES);
        }
        if (fCurrentPage.getSelection()) {
            printer.setScope(PrinterData.SELECTION);
        }
        if (fPageList.getSelection()) {
            printer.setScope(PrinterData.SELECTION);
        }
        if (fPageRange.getSelection()) {
            printer.setScope(PrinterData.PAGE_RANGE);
            fFrom = Integer.valueOf(fFromPage.getText()).intValue();
            fTo = Integer.valueOf(fToPage.getText()).intValue();
            printer.setStartPage(fFrom);
            printer.setEndPage(fTo);
        }

        PrinterData newPrinterData = printer.open();
        if (newPrinterData != null) {
            fPrinterData = newPrinterData;
        }
        updatePrinterStatus();

        if (printer.getScope() == PrinterData.ALL_PAGES) {
            fAllPages.setSelection(true);
            fCurrentPage.setSelection(false);
            fPageList.setSelection(false);
            fPageRange.setSelection(false);
            fHorPagesNum.setEnabled(false);
            fVertPagesNum.setEnabled(false);
        }
        if (printer.getScope() == PrinterData.PAGE_RANGE) {
            fAllPages.setSelection(false);
            fCurrentPage.setSelection(false);
            fPageList.setSelection(false);
            fPageRange.setSelection(true);
            fFromPage.setEnabled(true);
            fToPage.setEnabled(true);
            fFromPage.setText((Integer.valueOf(printer.getStartPage())).toString());
            fToPage.setText((Integer.valueOf(printer.getEndPage())).toString());
        }
        computeStepXY();
        fOverviewCanvas.redraw();
    }

    /**
     * Sets parent wizard page
     *
     * @param parent The parent wizard page
     */
    public void setParentWizardPage(WizardPage parent) {
        fParentWizardPage = parent;
    }

    /**
     * Sets the parent dialog box.
     *
     * @param parent The parent dialog box.
     */
    public void setParentDialog(SDPrintDialog parent) {
        fParentDialog = parent;
    }

    /**
     * Updates the printer status
     */
    protected void updatePrinterStatus() {
        if (fParentWizardPage != null) {
            // used in the wizard dialog
            if (fPrinterData == null) {
                // show error message and disable Finish button
                fParentWizardPage.setErrorMessage(Messages.SequenceDiagram_NoPrinterSelected);
                fParentWizardPage.setPageComplete(false);
            } else {
                // clear error message and enable Finish button
                fParentWizardPage.setErrorMessage(null);
                fParentWizardPage.setPageComplete(true);
            }
        } else if (fParentDialog != null) {
            // used in the print dialog
            if (fPrinterData == null) {
                // show error message and disable OK button
                fParentDialog.setErrorMessage(Messages.SequenceDiagram_NoPrinterSelected);
                fParentDialog.setPageComplete(false);
            } else {
                // clear error message and enable OK button
                fParentDialog.setErrorMessage(null);
                fParentDialog.setPageComplete(true);
            }
        }
    }

}
