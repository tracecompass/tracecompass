/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.rawviewer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.IThemeManager;

/**
 * TmfRawEventViewer allows for the display of the raw data for an arbitrarily
 * large number of TMF events.
 *
 * It is essentially a Composite of a StyledText area and a Slider, where the number
 * of visible lines in the StyledText control is set to fill the viewer display area.
 * An underlying data model is used to store a cache of event raw text line data.
 * The slider is ratio-based.
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfRawEventViewer extends Composite implements ControlListener, SelectionListener, MouseListener,
        KeyListener, CaretListener, MouseMoveListener, MouseTrackListener, MouseWheelListener, IPropertyChangeListener {

    private static final Color COLOR_BACKGROUND_ODD = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    private static final Color COLOR_BACKGROUND_EVEN = new Color(Display.getDefault(), 242, 242, 242);
    private static final String FONT_DEFINITION_ID = "org.eclipse.tracecompass.tmf.ui.font.eventraw"; //$NON-NLS-1$
    private static final String HIGHLIGHT_COLOR_DEFINITION_ID = "org.eclipse.tracecompass.tmf.ui.color.eventraw.highlight"; //$NON-NLS-1$
    private static final String SELECTION_COLOR_DEFINITION_ID = "org.eclipse.tracecompass.tmf.ui.color.eventraw.selection"; //$NON-NLS-1$
    private static final int MAX_LINE_DATA_SIZE = 1000;
    private static final int SLIDER_MAX = 1000000;
    private static final String EMPTY_STRING = ""; //$NON-NLS-1$
    private static final String LF = "\n"; //$NON-NLS-1$
    private static final @NonNull String CR_LF = "\r?\n"; //$NON-NLS-1$

    private ITmfTrace fTrace;
    private ITmfContext fBottomContext;

    private ScrolledComposite fScrolledComposite;
    private Composite fTextArea;
    private StyledText fStyledText;
    private Font fFixedFont;
    private Color fHighlightColor;
    private Color fSelectionColor;
    private Slider fSlider;
    private SliderThrottler fSliderThrottler;

    private final List<LineData> fLines = new ArrayList<>();
    private boolean fActualRanks = false;
    private int fTopLineIndex;
    private int fLastTopLineIndex;
    private final CaretPosition[] fStoredCaretPosition = new CaretPosition[]
                { new CaretPosition(0, 0), new CaretPosition(0,0)};
    private int fNumVisibleLines;
    private ITmfLocation fSelectedLocation = null;
    private long fHighlightedRank = Long.MIN_VALUE;
    private int fCursorYCoordinate = -1;
    private int fHoldSelection = 0;

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    private static class LineData {
        long rank;
        ITmfLocation location;
        String string;
        public LineData(long rank, ITmfLocation location, String string) {
            this.rank = rank;
            this.location = location;
            if (string.length() == 0) {
                /* workaround for setLineBackground has no effect on empty line */
                this.string = " "; //$NON-NLS-1$
            } else {
                this.string = string;
            }
        }
        @Override
        public String toString() {
            return rank + " [" + location + "]: " + string; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private static class CaretPosition {
        int time;
        int caretOffset;
        public CaretPosition(int time, int caretOffset) {
            this.time = time;
            this.caretOffset = caretOffset;
        }
    }

    private class SliderThrottler extends Thread {
        private static final long DELAY = 400L;
        private static final long POLLING_INTERVAL = 10L;

        @Override
        public void run() {
            final long startTime = System.currentTimeMillis();
            while ((System.currentTimeMillis() - startTime) < DELAY) {
                try {
                    Thread.sleep(POLLING_INTERVAL);
                } catch (InterruptedException e) {
                }
            }
            Display.getDefault().asyncExec(() -> {
                if (fSliderThrottler != SliderThrottler.this) {
                    return;
                }
                fSliderThrottler = null;
                if (SliderThrottler.this.isInterrupted() || fSlider.isDisposed()) {
                    return;
                }
                Event event = new Event();
                event.widget = TmfRawEventViewer.this;
                event.detail = SWT.NONE;
                widgetSelected(new SelectionEvent(event));
            });
        }
    }

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param parent The parent composite
     * @param style The style bits
     */
    public TmfRawEventViewer(Composite parent, int style) {
        super(parent, style & (~SWT.H_SCROLL) & (~SWT.V_SCROLL));

        // Set the layout
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.horizontalSpacing = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        setLayout(gridLayout);

        // Create the controls
        createTextArea(style & SWT.H_SCROLL);
        createSlider(style & SWT.V_SCROLL);

        // Prevent the slider from being traversed
        setTabList(new Control[] { fScrolledComposite });

        addDisposeListener((e) -> {
            if (fBottomContext != null) {
                fBottomContext.dispose();
            }
            PlatformUI.getWorkbench().getThemeManager().removePropertyChangeListener(TmfRawEventViewer.this);
        });
    }

    // ------------------------------------------------------------------------
    // Font and color handling
    // ------------------------------------------------------------------------

    /**
     * Initialize the fonts.
     * @since 1.0
     */
    protected void initializeFonts() {
        FontRegistry fontRegistry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry();
        fFixedFont = fontRegistry.get(FONT_DEFINITION_ID);
        fStyledText.setFont(fFixedFont);
    }

    /**
     * Initialize the colors.
     * @since 1.1
     */
    protected void initializeColors() {
        ColorRegistry colorRegistry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
        fHighlightColor = colorRegistry.get(HIGHLIGHT_COLOR_DEFINITION_ID);
        fSelectionColor = colorRegistry.get(SELECTION_COLOR_DEFINITION_ID);
    }

    /**
     * @since 1.0
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ((IThemeManager.CHANGE_CURRENT_THEME.equals(event.getProperty())) ||
                (FONT_DEFINITION_ID.equals(event.getProperty()))) {
            initializeFonts();
            refreshTextArea();
        }
        if ((IThemeManager.CHANGE_CURRENT_THEME.equals(event.getProperty())) ||
                (HIGHLIGHT_COLOR_DEFINITION_ID.equals(event.getProperty())) ||
                (SELECTION_COLOR_DEFINITION_ID.equals(event.getProperty()))) {
            initializeColors();
            refreshTextArea();
        }
    }

    // ------------------------------------------------------------------------
    // Text area handling
    // ------------------------------------------------------------------------

    /**
     * Create the text area and add listeners
     */
    private void createTextArea(int style) {
        fScrolledComposite = new ScrolledComposite(this, style);
        fScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        fTextArea = new Composite(fScrolledComposite, SWT.NONE);
        fTextArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        fScrolledComposite.setContent(fTextArea);
        fScrolledComposite.setExpandHorizontal(true);
        fScrolledComposite.setExpandVertical(true);
        fScrolledComposite.setAlwaysShowScrollBars(true);
        fScrolledComposite.setMinSize(fTextArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        fScrolledComposite.addControlListener(this);

        GridLayout textAreaGridLayout = new GridLayout();
        textAreaGridLayout.marginHeight = 0;
        textAreaGridLayout.marginWidth = 0;
        fTextArea.setLayout(textAreaGridLayout);

        fStyledText = new StyledText(fTextArea, SWT.READ_ONLY);
        fStyledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        initializeFonts();
        initializeColors();
        PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(this);

        fStyledText.addCaretListener(this);
        fStyledText.addMouseMoveListener(this);
        fStyledText.addMouseTrackListener(this);
        fStyledText.addMouseWheelListener(this);
        /* disable mouse scroll of horizontal scroll bar */
        fStyledText.addListener(SWT.MouseWheel, event -> event.doit = false);
        fStyledText.addKeyListener(this);

        fTextArea.setBackground(fStyledText.getBackground());
        fTextArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                fTextArea.setFocus();
            }
        });
    }

    // ------------------------------------------------------------------------
    // Slider handling
    // ------------------------------------------------------------------------

    private void createSlider(int style) {
        fSlider = new Slider(this, SWT.VERTICAL);
        fSlider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
        fSlider.setValues(0, 0, SLIDER_MAX, SLIDER_MAX, 1, 1);
        fSlider.addSelectionListener(this);
        fSlider.addMouseListener(this);
        if ((style & SWT.V_SCROLL) == 0) {
            fSlider.setVisible(false);
        }
    }

    // ------------------------------------------------------------------------
    // Controls interactions
    // ------------------------------------------------------------------------

    @Override
    public boolean setFocus() {
        boolean isVisible = isVisible();
        if (isVisible) {
            fTextArea.setFocus();
        }
        return isVisible;
    }

    @Override
    public void setMenu(Menu menu) {
        fStyledText.setMenu(menu);
    }

    /**
     * Sets the trace and updates the content
     * @param trace The trace to set
     */
    public void setTrace(ITmfTrace trace) {
        fTrace = trace;
        fTopLineIndex = 0;
        fLines.clear();
        refreshEventCount();
    }

    /**
     * Refreshes the event count, updates the slider thumb and loads display
     */
    public void refreshEventCount() {
        if (fTrace != null) {
            if (fTrace.getNbEvents() > 0) {
                fSlider.setThumb((int) Math.max(SLIDER_MAX / fTrace.getNbEvents(), 1));
            } else {
                fSlider.setThumb(SLIDER_MAX);
            }

            if (!isVisible()) {
                return;
            }

            if (fLines.isEmpty()) {
                setTopRank(0);
            } else if (fLines.size() < fNumVisibleLines) {
                if (fBottomContext != null) {
                    fBottomContext.dispose();
                    fBottomContext = null;
                }
                loadLineData();
                fillTextArea();
                fSlider.setSelection((int) (SLIDER_MAX * fTrace.getLocationRatio(fLines.get(fTopLineIndex).location)));
            }
        } else {
            if (fBottomContext != null) {
                fBottomContext.dispose();
                fBottomContext = null;
            }
            fillTextArea();
            fSlider.setThumb(SLIDER_MAX);
            fSlider.setSelection(0);
        }
    }

    /**
     * Selects the event of given rank and makes it visible.
     * @param rank The rank of event
     */
    public void selectAndReveal(long rank) {
        if (fTrace == null || !isVisible()) {
            return;
        }
        if (fActualRanks && fTopLineIndex < fLines.size() && rank >= fLines.get(fTopLineIndex).rank) {
            int lastVisibleIndex = Math.min(fTopLineIndex + fNumVisibleLines, fLines.size()) - 1;
            if (rank <= fLines.get(lastVisibleIndex).rank) {
                for (int i = fTopLineIndex; i < fLines.size(); i++) {
                    if (fLines.get(i).rank == rank) {
                        fSelectedLocation = fLines.get(i).location;
                        break;
                    }
                }
                refreshLineBackgrounds();
                return;
            }
        }
        setTopRank(rank);
        if (!fLines.isEmpty() && fHoldSelection == 0) {
            fSelectedLocation = fLines.get(0).location;
            refreshLineBackgrounds();
        }
    }

    /**
     * Add a selection listener
     * @param listener A listener to add
     */
    public void addSelectionListener(Listener listener) {
        checkWidget();
        if (listener == null) {
            SWT.error (SWT.ERROR_NULL_ARGUMENT);
        }
        addListener (SWT.Selection, listener);
    }

    /**
     * Remove selection listener
     * @param listener A listener to remove
     */
    public void removeSelectionListener(Listener listener) {
        checkWidget();
        if (listener == null) {
            SWT.error (SWT.ERROR_NULL_ARGUMENT);
        }
        removeListener(SWT.Selection, listener);
    }

    private void sendSelectionEvent(LineData lineData) {
        Event event = new Event();
        if (fActualRanks) {
            event.data = Long.valueOf(lineData.rank);
        } else {
            event.data = lineData.location;
        }
        notifyListeners(SWT.Selection, event);
    }

    private void setTopRank(long rank) {
        if (fBottomContext != null) {
            fBottomContext.dispose();
        }
        fBottomContext = fTrace.seekEvent(rank);
        if (fBottomContext == null) {
            return;
        }
        fLines.clear();
        fActualRanks = true;
        fTopLineIndex = 0;
        loadLineData();
        refreshTextArea();
        if (fLines.isEmpty()) {
            fSlider.setSelection(0);
        } else {
            fSlider.setSelection((int) (SLIDER_MAX * fTrace.getLocationRatio(fLines.get(fTopLineIndex).location)));
        }
    }

    private void setTopPosition(double ratio) {
        if (fBottomContext != null) {
            fBottomContext.dispose();
        }
        fBottomContext = fTrace.seekEvent(ratio);
        if (fBottomContext == null) {
            return;
        }
        fBottomContext.setRank(0);
        fLines.clear();
        fActualRanks = false;
        fTopLineIndex = 0;
        loadLineData();
        refreshTextArea();
    }

    private void loadLineData() {
        if (fTopLineIndex < 0) {
            if (!fLines.isEmpty() && fTrace.getLocationRatio(fLines.get(0).location) > 0) {
                double lastRatio = fTrace.getLocationRatio(fLines.get(fLines.size() - 1).location);
                double firstRatio = fTrace.getLocationRatio(fLines.get(0).location);
                double delta;
                boolean singleEvent = false;
                if (firstRatio != lastRatio) {
                    // approximate ratio of at least 20 items
                    delta = Math.max(20, fNumVisibleLines) * (lastRatio - firstRatio) / (fLines.size() - 1);
                } else {
                    delta = Math.pow(10, -15);
                    singleEvent = true;
                }
                while (fTopLineIndex < 0) {
                    ITmfLocation endLocation = fLines.get(0).location;
                    firstRatio = Math.max(0, firstRatio - delta);
                    ITmfContext context = fTrace.seekEvent(firstRatio);
                    ITmfLocation location;
                    int index = 0;
                    long rank = 0;
                    while (!context.getLocation().equals(endLocation)) {
                        location = context.getLocation();
                        ITmfEvent event = fTrace.getNext(context);
                        if (event == null) {
                            break;
                        }
                        if (event.getContent() != null && event.getContent().getValue() != null) {
                            String[] lines = event.getContent().getValue().toString().split(CR_LF);
                            for (int i = 0; i < lines.length; i++) {
                                String line = lines[i];
                                LineData lineData = new LineData(rank, location, line);
                                fLines.add(index++, lineData);
                                fTopLineIndex++;
                                fLastTopLineIndex++;
                            }
                        } else {
                            LineData lineData = new LineData(rank, location, EMPTY_STRING);
                            fLines.add(index++, lineData);
                            fTopLineIndex++;
                            fLastTopLineIndex++;
                        }
                        rank++;
                    }
                    context.dispose();
                    long rankOffset = fLines.get(index).rank - rank;
                    for (int i = 0; i < index; i++) {
                        fLines.get(i).rank += rankOffset;
                    }
                    if (firstRatio == 0) {
                        break;
                    }
                    if (singleEvent) {
                        delta = Math.min(delta * 10, 0.1);
                    }
                }
            }
            if (fTopLineIndex < 0) {
                fTopLineIndex = 0;
            }
        }

        while (fLines.size() - fTopLineIndex < fNumVisibleLines) {
            if (fBottomContext == null) {
                if (fLines.isEmpty()) {
                    fBottomContext = fTrace.seekEvent(0);
                } else {
                    fBottomContext = fTrace.seekEvent(fLines.get(fLines.size() - 1).location);
                    fTrace.getNext(fBottomContext);
                }
                if (fBottomContext == null) {
                    break;
                }
            }
            long rank = fBottomContext.getRank();
            ITmfLocation location = fBottomContext.getLocation() != null ? fBottomContext.getLocation() : null;
            ITmfEvent event = fTrace.getNext(fBottomContext);
            if (event == null) {
                break;
            }
            if (event.getContent() != null && event.getContent().getValue() != null) {
                for (String line : event.getContent().getValue().toString().split(CR_LF)) {
                    int crPos;
                    if ((crPos = line.indexOf('\r')) != -1) {
                        line = line.substring(0, crPos);
                    }
                    LineData lineData = new LineData(rank, location, line);
                    fLines.add(lineData);
                }
            } else {
                LineData lineData = new LineData(rank, location, EMPTY_STRING);
                fLines.add(lineData);
            }
        }
        fTopLineIndex = Math.max(0, Math.min(fTopLineIndex, fLines.size() - 1));

        if (fLines.size() > MAX_LINE_DATA_SIZE) {
            if (fTopLineIndex < MAX_LINE_DATA_SIZE / 2) {
                long rank = fLines.get(MAX_LINE_DATA_SIZE - 1).rank;
                for (int i = MAX_LINE_DATA_SIZE; i < fLines.size(); i++) {
                    if (fLines.get(i).rank > rank) {
                        fLines.subList(i, fLines.size()).clear();
                        if (fBottomContext != null) {
                            fBottomContext.dispose();
                            fBottomContext = null;
                        }
                        break;
                    }
                }
            } else {
                long rank = fLines.get(fLines.size() - MAX_LINE_DATA_SIZE).rank;
                for (int i = fLines.size() - MAX_LINE_DATA_SIZE - 1; i >= 0; i--) {
                    if (fLines.get(i).rank < rank) {
                        fLines.subList(0, i + 1).clear();
                        fTopLineIndex -= (i + 1);
                        fLastTopLineIndex -= (i + 1);
                        break;
                    }
                }
            }
        }
    }

    private void refreshTextArea() {
        fStyledText.setText(EMPTY_STRING);
        for (int i = 0; i < fLines.size() - fTopLineIndex && i < fNumVisibleLines; i++) {
            if (i > 0)
             {
                fStyledText.append(LF);
            }
            LineData lineData = fLines.get(fTopLineIndex + i);
            fStyledText.append(lineData.string);
            setLineBackground(i, lineData);
        }
        fTextArea.layout();
        fScrolledComposite.setMinSize(fTextArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        fLastTopLineIndex = fTopLineIndex;
    }

    private void fillTextArea() {
        int nextLine = fStyledText.getCharCount() == 0 ? 0 : fStyledText.getLineCount();
        for (int i = nextLine; i < fLines.size() - fTopLineIndex && i < fNumVisibleLines; i++) {
            if (i > 0)
             {
                fStyledText.append(LF);
            }
            LineData lineData = fLines.get(fTopLineIndex + i);
            fStyledText.append(lineData.string);
            setLineBackground(i, lineData);
        }
        int endLine = Math.min(fNumVisibleLines, fLines.size());
        if (endLine < fStyledText.getLineCount()) {
            int endOffset = fStyledText.getOffsetAtLine(endLine) - 1;
            if (endOffset > fStyledText.getCharCount()) {
                fHoldSelection++;
                fStyledText.replaceTextRange(endOffset, fStyledText.getCharCount() - endOffset, EMPTY_STRING);
                fHoldSelection--;
            }
        }
        fTextArea.layout();
        fScrolledComposite.setMinSize(fTextArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    private void updateTextArea() {
        if (fTopLineIndex < fLastTopLineIndex) {
            StringBuilder insertedText = new StringBuilder();
            for (int i = fTopLineIndex; i < fLastTopLineIndex; i++) {
                insertedText.append(fLines.get(i).string).append(LF);
            }
            fStyledText.replaceTextRange(0, 0, insertedText.toString());
            for (int i = 0; i < fLastTopLineIndex - fTopLineIndex; i++) {
                LineData lineData = fLines.get(fTopLineIndex + i);
                setLineBackground(i, lineData);
            }
            fLastTopLineIndex = fTopLineIndex;
        } else if (fTopLineIndex > fLastTopLineIndex) {
            int length = 0;
            for (int i = 0; i < fTopLineIndex - fLastTopLineIndex && i < fNumVisibleLines; i++) {
                length += fLines.get(i + fLastTopLineIndex).string.length();
                if (i < fStyledText.getLineCount()) {
                    length += 1;
                }
            }
            fStyledText.replaceTextRange(0, length, EMPTY_STRING);
            fLastTopLineIndex = fTopLineIndex;
            fillTextArea();
        }
        int endLine = Math.min(fNumVisibleLines, fLines.size());
        if (endLine < fStyledText.getLineCount()) {
            int endOffset = fStyledText.getOffsetAtLine(endLine) - 1;
            if (endOffset > fStyledText.getCharCount()) {
                fStyledText.replaceTextRange(endOffset, fStyledText.getCharCount() - endOffset, EMPTY_STRING);
            }
        }
        fTextArea.layout();
        fScrolledComposite.setMinSize(fTextArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    private void refreshLineBackgrounds() {
        for (int i = 0; (i < fStyledText.getLineCount()) && (i < fNumVisibleLines) && (i < fLines.size() - fTopLineIndex); i++) {
            LineData lineData = fLines.get(fTopLineIndex + i);
            setLineBackground(i, lineData);
        }
    }

    private void setLineBackground(int index, LineData lineData) {
        if (lineData.location.equals(fSelectedLocation)) {
            fStyledText.setLineBackground(index, 1, fSelectionColor);
        } else if (lineData.rank == fHighlightedRank) {
            fStyledText.setLineBackground(index, 1, fHighlightColor);
        } else if (lineData.rank % 2 == 0) {
            fStyledText.setLineBackground(index, 1, COLOR_BACKGROUND_EVEN);
        } else {
            fStyledText.setLineBackground(index, 1, COLOR_BACKGROUND_ODD);
        }
    }

    private void storeCaretPosition(int time, int caretOffset) {
        if (fStoredCaretPosition[0].time == time) {
            fStoredCaretPosition[0].caretOffset = caretOffset;
        } else {
            fStoredCaretPosition[1] = fStoredCaretPosition[0];
            fStoredCaretPosition[0] = new CaretPosition(time, caretOffset);
        }
    }

    private int getPreviousCaretOffset(int time) {
        if (fStoredCaretPosition[0].time == time) {
            return fStoredCaretPosition[1].caretOffset;
        }
        return fStoredCaretPosition[0].caretOffset;
    }

    private void updateHighlightedRank() {
        if (fCursorYCoordinate < 0 || fCursorYCoordinate > fStyledText.getSize().y) {
            if (fHighlightedRank != Long.MIN_VALUE) {
                fHighlightedRank = Long.MIN_VALUE;
                refreshLineBackgrounds();
            }
            return;
        }
        int offset = fStyledText.getOffsetAtLocation(new Point(0, fCursorYCoordinate));
        int line = fStyledText.getLineAtOffset(offset);
        if (line < fLines.size() - fTopLineIndex) {
            LineData lineData = fLines.get(fTopLineIndex + line);
            if (fHighlightedRank != lineData.rank) {
                fHighlightedRank = lineData.rank;
                refreshLineBackgrounds();
            }
        } else {
            if (fHighlightedRank != Long.MIN_VALUE) {
                fHighlightedRank = Long.MIN_VALUE;
                refreshLineBackgrounds();
            }
        }
    }

    // ------------------------------------------------------------------------
    // ControlListener (ScrolledComposite)
    // ------------------------------------------------------------------------

    @Override
    public void controlResized(ControlEvent event) {
        int areaHeight = fScrolledComposite.getSize().y;
        if (fScrolledComposite.getHorizontalBar() != null) {
            areaHeight -= fScrolledComposite.getHorizontalBar().getSize().y;
        }
        int lineHeight = fStyledText.getLineHeight();
        fNumVisibleLines = Math.max((areaHeight + lineHeight - 1) / lineHeight, 1);

        if (fBottomContext != null) {
            loadLineData();
            fillTextArea();
        }
    }

    @Override
    public void controlMoved(ControlEvent e) {
    }

    // ------------------------------------------------------------------------
    // SelectionListener (Slider)
    // ------------------------------------------------------------------------

    @Override
    public void widgetSelected(SelectionEvent e) {
        fTextArea.setFocus();
        if (fLines.isEmpty()) {
            return;
        }
        fHoldSelection++;
        switch (e.detail) {
            case SWT.DRAG:
            case SWT.NONE: {
                if (e.widget == fSlider) {
                    /*
                     * While the slider thumb is being dragged, only perform the
                     * refresh periodically. The event detail during the drag is
                     * SWT.DRAG on Windows and SWT.NONE on Linux.
                     */
                    if (fSliderThrottler == null) {
                        fSliderThrottler = new SliderThrottler();
                        fSliderThrottler.start();
                    }
                    fHoldSelection = 0;
                    return;
                }
                /*
                 * The selection event was sent by the viewer, refresh now.
                 */
                if (fSlider.getSelection() == 0 || fSlider.getThumb() == SLIDER_MAX) {
                    fLines.clear();
                    setTopPosition(0.0);
                    break;
                }
                double ratio = (double) fSlider.getSelection() / (SLIDER_MAX - fSlider.getThumb());
                double delta = Math.pow(10, -15);
                fLines.clear();
                while (fLines.isEmpty()) {
                    setTopPosition(ratio);
                    if (ratio == 0.0) {
                        break;
                    }
                    delta = Math.min(delta * 10, 0.1);
                    ratio = Math.max(ratio - delta, 0.0);
                }
                break;
            }
            case SWT.ARROW_DOWN: {
                if (fTopLineIndex >= fLines.size()) {
                    break;
                }
                fTopLineIndex++;
                loadLineData();
                updateTextArea();
                break;
            }
            case SWT.PAGE_DOWN: {
                fTopLineIndex += Math.max(fNumVisibleLines - 1, 1);
                loadLineData();
                updateTextArea();
                break;
            }
            case SWT.ARROW_UP: {
                if (fLines.isEmpty()) {
                    break;
                }
                fTopLineIndex--;
                loadLineData();
                updateTextArea();
                break;
            }
            case SWT.PAGE_UP: {
                fTopLineIndex -= Math.max(fNumVisibleLines - 1, 1);
                loadLineData();
                updateTextArea();
                break;
            }
            case SWT.HOME: {
                setTopPosition(0.0);
                break;
            }
            case SWT.END: {
                double ratio = 1.0;
                double delta = Math.pow(10, -15);
                fLines.clear();
                while (fLines.isEmpty()) {
                    setTopPosition(ratio);
                    if (ratio == 0.0) {
                        break;
                    }
                    delta = Math.min(delta * 10, 0.1);
                    ratio = Math.max(ratio - delta, 0.0);
                }
                break;
            }
            default:
                break;
        }
        if (e.detail != SWT.NONE) {
            fSlider.setSelection((int) (SLIDER_MAX * fTrace.getLocationRatio(fLines.get(fTopLineIndex).location)));
        }

        fHoldSelection = 0;
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
    }

    // ------------------------------------------------------------------------
    // MouseListener (Slider)
    // ------------------------------------------------------------------------

    /**
     * @since 1.1
     */
    @Override
    public void mouseDown(MouseEvent e) {
    }

    /**
     * @since 1.1
     */
    @Override
    public void mouseUp(MouseEvent e) {
        if (e.button != 1) {
            return;
        }
        /*
         * When the mouse button is released, perform the refresh immediately
         * and interrupt and discard the slider throttler.
         */
        if (fSliderThrottler != null) {
            fSliderThrottler.interrupt();
            fSliderThrottler = null;
        }
        Event event = new Event();
        event.widget = this;
        event.detail = SWT.NONE;
        widgetSelected(new SelectionEvent(event));
    }

    /**
     * @since 1.1
     */
    @Override
    public void mouseDoubleClick(MouseEvent e) {
    }

    // ------------------------------------------------------------------------
    // KeyListener (StyledText)
    // ------------------------------------------------------------------------

    @Override
    public void keyPressed(KeyEvent e) {
        if (fLines.isEmpty()) {
            return;
        }
        int caretOffset = fStyledText.getCaretOffset();
        int previousCaretOffset = getPreviousCaretOffset(e.time);
        int previousLineAtCaretPosition = fStyledText.getLineAtOffset(previousCaretOffset);
        int previousColumnAtCaretPosition = getPreviousCaretOffset(e.time) - fStyledText.getOffsetAtLine(previousLineAtCaretPosition);
        switch (e.keyCode) {
            case SWT.ARROW_DOWN: {
                if (previousLineAtCaretPosition < (fNumVisibleLines - 2)) {
                    break;
                }
                fHoldSelection++;
                fTopLineIndex++;
                loadLineData();
                updateTextArea();
                fHoldSelection--;
                LineData lineData = fLines.get(fTopLineIndex + fStyledText.getLineAtOffset(fStyledText.getCaretOffset()));
                if (!lineData.location.equals(fSelectedLocation)) {
                    fSelectedLocation = lineData.location;
                    refreshLineBackgrounds();
                    sendSelectionEvent(lineData);
                }
                break;
            }
            case SWT.PAGE_DOWN: {
                if (previousLineAtCaretPosition >= (fNumVisibleLines - 1)) {
                    fHoldSelection++;
                    if (fLines.get(fTopLineIndex + previousLineAtCaretPosition).rank % 2 == 0) {
                        fStyledText.setLineBackground(previousLineAtCaretPosition, 1, COLOR_BACKGROUND_EVEN);
                    } else {
                        fStyledText.setLineBackground(previousLineAtCaretPosition, 1, COLOR_BACKGROUND_ODD);
                    }
                    fSelectedLocation = null;
                    fTopLineIndex += Math.max(fNumVisibleLines - 1, 1);
                    loadLineData();
                    updateTextArea();
                    fHoldSelection--;
                }
                int line = Math.min(fNumVisibleLines - 1, fStyledText.getLineCount() - 1);
                int offset = fStyledText.getOffsetAtLine(line);
                fStyledText.setSelection(offset + Math.min(previousColumnAtCaretPosition, fLines.get(fTopLineIndex + line).string.length()));
                break;
            }
            case SWT.ARROW_RIGHT: {
                if (previousCaretOffset < fStyledText.getCharCount() || previousLineAtCaretPosition < (fNumVisibleLines - 2)) {
                    break;
                }
                fHoldSelection++;
                fTopLineIndex++;
                loadLineData();
                updateTextArea();
                fHoldSelection--;
                fStyledText.setSelection(fStyledText.getCaretOffset() + 1);
                break;
            }
            case SWT.ARROW_UP: {
                if (previousLineAtCaretPosition > 0) {
                    break;
                }
                if (fLines.isEmpty()) {
                    break;
                }
                fHoldSelection++;
                fTopLineIndex--;
                loadLineData();
                updateTextArea();
                fHoldSelection--;
                LineData lineData = fLines.get(fTopLineIndex);
                if (!lineData.location.equals(fSelectedLocation)) {
                    fSelectedLocation = lineData.location;
                    refreshLineBackgrounds();
                    sendSelectionEvent(lineData);
                }
                fStyledText.setSelection(caretOffset);
                break;
            }
            case SWT.PAGE_UP: {
                if (previousLineAtCaretPosition > 0) {
                    break;
                }
                fHoldSelection++;
                fTopLineIndex -= Math.max(fNumVisibleLines - 1, 1);
                loadLineData();
                updateTextArea();
                fHoldSelection--;
                LineData lineData = fLines.get(fTopLineIndex);
                if (!lineData.location.equals(fSelectedLocation)) {
                    fSelectedLocation = lineData.location;
                    refreshLineBackgrounds();
                    sendSelectionEvent(lineData);
                }
                fStyledText.setSelection(caretOffset);
                break;
            }
            case SWT.ARROW_LEFT: {
                if (previousCaretOffset > 0) {
                    break;
                }
                if (fLines.isEmpty()) {
                    break;
                }
                long topRank = fLines.get(fTopLineIndex).rank;
                fHoldSelection++;
                fTopLineIndex--;
                loadLineData();
                updateTextArea();
                fHoldSelection--;
                LineData lineData = fLines.get(fTopLineIndex);
                if (!lineData.location.equals(fSelectedLocation)) {
                    fSelectedLocation = lineData.location;
                    refreshLineBackgrounds();
                    sendSelectionEvent(lineData);
                }
                if (topRank != fLines.get(fTopLineIndex).rank) {
                    fStyledText.setSelection(fLines.get(fTopLineIndex).string.length());
                }
                break;
            }
            case SWT.HOME: {
                if ((e.stateMask & SWT.CTRL) == 0) {
                    break;
                }
                setTopPosition(0.0);
                LineData lineData = fLines.get(fTopLineIndex);
                if (!lineData.location.equals(fSelectedLocation)) {
                    fSelectedLocation = lineData.location;
                    refreshLineBackgrounds();
                    sendSelectionEvent(lineData);
                }
                break;
            }
            case SWT.END: {
                if ((e.stateMask & SWT.CTRL) == 0) {
                    break;
                }
                double ratio = 1.0;
                double delta = Math.pow(10, -15);
                fLines.clear();
                while (fLines.isEmpty()) {
                    setTopPosition(ratio);
                    if (ratio == 0.0) {
                        break;
                    }
                    delta = Math.min(delta * 10, 0.1);
                    ratio = Math.max(ratio - delta, 0.0);
                }
                LineData lineData = fLines.get(fTopLineIndex);
                if (!lineData.location.equals(fSelectedLocation)) {
                    fSelectedLocation = lineData.location;
                    refreshLineBackgrounds();
                    sendSelectionEvent(lineData);
                }
                break;
            }
            default:
                break;
        }
        updateHighlightedRank();
        fSlider.setSelection((int) (SLIDER_MAX * fTrace.getLocationRatio(fLines.get(fTopLineIndex).location)));
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    // ------------------------------------------------------------------------
    // CaretListener (StyledText)
    // ------------------------------------------------------------------------

    @Override
    public void caretMoved(CaretEvent event) {
        if (fHoldSelection == 0) {
            int line = fStyledText.getLineAtOffset(event.caretOffset);
            if (fTopLineIndex + line < fLines.size()) {
                LineData lineData = fLines.get(fTopLineIndex + line);
                if (!lineData.location.equals(fSelectedLocation)) {
                    fSelectedLocation = lineData.location;
                    refreshLineBackgrounds();
                    sendSelectionEvent(lineData);
                }
            }
        }
        storeCaretPosition(event.time, event.caretOffset);
        if (fHoldSelection == 0) {
            Point caret = fStyledText.getLocationAtOffset(fStyledText.getCaretOffset());
            Point origin = fScrolledComposite.getOrigin();
            if (origin.x > caret.x) {
                origin.x = caret.x;
            } else if (caret.x - origin.x > fScrolledComposite.getSize().x) {
                origin.x = caret.x - fScrolledComposite.getSize().x + 1;
            }
            fScrolledComposite.setOrigin(origin);
        }
    }

    // ------------------------------------------------------------------------
    // MouseMoveListener (StyledText)
    // ------------------------------------------------------------------------

    @Override
    public void mouseMove(MouseEvent e) {
        fCursorYCoordinate = e.y;
        if (e.y < 0 || e.y > fStyledText.getSize().y) {
            if (fHighlightedRank != Long.MIN_VALUE) {
                fHighlightedRank = Long.MIN_VALUE;
                refreshLineBackgrounds();
            }
            return;
        }
        int offset = fStyledText.getOffsetAtLocation(new Point(0, e.y));
        int line = fStyledText.getLineAtOffset(offset);
        if (line < fLines.size() - fTopLineIndex) {
            LineData lineData = fLines.get(fTopLineIndex + line);
            if (fHighlightedRank != lineData.rank) {
                fHighlightedRank = lineData.rank;
                refreshLineBackgrounds();
            }
        } else {
            if (fHighlightedRank != Long.MIN_VALUE) {
                fHighlightedRank = Long.MIN_VALUE;
                refreshLineBackgrounds();
            }
        }
    }

    // ------------------------------------------------------------------------
    // MouseTrackListener (StyledText)
    // ------------------------------------------------------------------------

    @Override
    public void mouseExit(MouseEvent e) {
        fCursorYCoordinate = -1;
        if (fHighlightedRank != Long.MIN_VALUE) {
            fHighlightedRank = Long.MIN_VALUE;
            refreshLineBackgrounds();
        }
    }

    @Override
    public void mouseEnter(MouseEvent e) {
        fCursorYCoordinate = e.y;
    }

    @Override
    public void mouseHover(MouseEvent e) {
    }

    // ------------------------------------------------------------------------
    // MouseWheelListener (StyledText)
    // ------------------------------------------------------------------------

    @Override
    public void mouseScrolled(MouseEvent e) {
        if (fLines.isEmpty() || e.count == 0) {
            return;
        }
        fHoldSelection++;
        fTopLineIndex -= e.count;
        loadLineData();
        updateTextArea();
        fHoldSelection = 0;
        updateHighlightedRank();
        fSlider.setSelection((int) (SLIDER_MAX * fTrace.getLocationRatio(fLines.get(fTopLineIndex).location)));
    }

}
