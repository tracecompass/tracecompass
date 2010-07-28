/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Francois Chouinard - Refactoring, slider support, bug fixing 
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * <b><u>TmfVirtualTable</u></b>
 * <p>
 * TmfVirtualTable allows for the tabular display of arbitrarily large data sets
 * (well, up to Integer.MAX_VALUE or ~2G rows).
 * 
 * It is essentially a Composite of Table and Slider, where the number of rows
 * in the table is set to fill the table display area. The slider is rank-based.
 * 
 * It differs from Table with the VIRTUAL style flag where an empty entry is
 * created for each virtual row. This does not scale well for very large data sets.
 */
public class TmfVirtualTable extends Composite {

	// The table
	private Table fTable;
	private int   fFirstRowOffset = 0;
	private int   fTableRow       = 0; 
	private int   fEffectiveRow   = 0; 

	private TableItem fSelectedItems[] = null;
	private int       fTableItemCount  = 0;
	private int       fRowsDisplayed;
	private TableItem fTableItems[];

	// The slider
	private Slider  fSlider;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	/**
	 * @param parent
	 * @param style
	 */
	public TmfVirtualTable(Composite parent, int style) {
		super(parent, style | SWT.BORDER & (~SWT.H_SCROLL) & (~SWT.V_SCROLL));

		// Create the controls
		createTable();
		createSlider();

		// Set the layout
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);
		
		GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		fTable.setLayoutData(tableGridData);

		GridData sliderGridData = new GridData(SWT.FILL, SWT.FILL, false, true);
		fSlider.setLayoutData(sliderGridData);

		// Add the listeners
		addMouseWheelListener(new MouseWheelListener() {
			public void mouseScrolled(MouseEvent event) {
				fFirstRowOffset -= event.count;
				int lastFirstRowOffset = fTableItemCount - fRowsDisplayed - 1;
				if (fFirstRowOffset > lastFirstRowOffset) {
					fFirstRowOffset = lastFirstRowOffset;
				} else if (fFirstRowOffset < 0) {
					fFirstRowOffset = 0;
				}
				fSlider.setSelection(fFirstRowOffset);
				setSelection();
			}
		});

		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent event) {
				resize();
			}
		});		

		// And display
		refresh();
	}

	// ------------------------------------------------------------------------
	// Table handling
	// ------------------------------------------------------------------------

	/**
	 * Create the table and add listeners
	 */
	private void createTable() {

		int tableStyle = SWT.NO_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION; 
		fTable = new Table(this, tableStyle);

		fTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleTableSelection();
			}
		});

		fTable.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent event) {
				handleTableKeyEvent(event);
			}
			public void keyReleased(KeyEvent event) {
			}
		});
	}	

	/**
	 * Update the rows and selected item
	 */
	private void handleTableSelection() {
		fTableRow = fTable.getSelectionIndices()[0];
		fEffectiveRow = fFirstRowOffset + fTableRow;
		fSelectedItems = new TableItem[1];
		fSelectedItems[0] = fTable.getSelection()[0];
	}

	/**
	 * Handle key-based navigation in table.
	 * 
	 * The key variables are:
	 * - fFirstRowOffset: the absolute index (in the data set) of the first row displayed
	 * - fTableRow: the index of the selected event in the table window
	 * - fEffectiveRow: the absolute index of the selected event (in the data set)
	 * 
	 * At all times, the following relation should hold true:
	 * 		fEffectiveRow = fFirstRowOffset + fTableRow
	 * 
	 * @param event
	 */
	private void handleTableKeyEvent(KeyEvent event) {

		boolean needsUpdate    = false;
		final int lastTableRow = fTableItemCount - 1;
		int lastRowDisplayed = ((fTableItemCount < fRowsDisplayed) ?  fTableItemCount : fRowsDisplayed) - 1;

		// We are handling things
		event.doit = false;

		switch (event.keyCode) {

			case SWT.ARROW_DOWN: {
				if (fEffectiveRow < lastTableRow) {
					fEffectiveRow++;
					if (fTableRow < lastRowDisplayed) {
						fTableRow++;
					} else if (fTableRow < fEffectiveRow) {
						fFirstRowOffset++;
						needsUpdate = true;
					}
				}
				break;
			}

			case SWT.PAGE_DOWN: {
				if (fEffectiveRow < lastTableRow) {
					if ((lastTableRow - fEffectiveRow) >= fRowsDisplayed) {
						fEffectiveRow   += fRowsDisplayed;
						fFirstRowOffset += fRowsDisplayed;
					} else {
						fEffectiveRow = lastTableRow;
						fTableRow = lastRowDisplayed;
					}
					needsUpdate = true;
				}
				break;
			}

			case SWT.END: {
				fEffectiveRow = lastTableRow;
				fTableRow = lastRowDisplayed;
				if (lastTableRow > lastRowDisplayed) {
					fFirstRowOffset = fTableItemCount - fRowsDisplayed;
				}
				needsUpdate = true;
				break;
			}

			case SWT.ARROW_UP: {
				if (fEffectiveRow > 0) {
					fEffectiveRow--;
					if (fTableRow > 0) {
						fTableRow--;
					} else {
						fFirstRowOffset--;
						needsUpdate = true;
					}
				}
				break;
			}

			case SWT.PAGE_UP: {
				if (fEffectiveRow > 0) {
					if (fEffectiveRow > fRowsDisplayed - 1) {
						fEffectiveRow   -= fRowsDisplayed;
						fFirstRowOffset -= fRowsDisplayed;
					} else {
						fEffectiveRow = 0;
						fTableRow = 0;
					}
					needsUpdate = true;
				}
				break;
			}

			case SWT.HOME: {
				fEffectiveRow   = 0;
				fTableRow       = 0;
				fFirstRowOffset = 0;
				needsUpdate     = true;
				break;
			}
		}
	
		if (needsUpdate) {
			for (int i = 0; i < fTableItems.length; i++) {
				setDataItem(fTableItems[i]);
			}
		}

		fTable.setSelection(fTableRow);
		fSlider.setSelection(fEffectiveRow);

//		System.out.println("1st: " + fFirstRowOffset + ", TR: " + fTableRow + ", ER: " + fEffectiveRow +
//				", Valid: " + ((fFirstRowOffset >= 0) && (fEffectiveRow == (fFirstRowOffset + fTableRow))));
	}

	private void setDataItem(TableItem item) {
		int index = fTable.indexOf(item); 
		if( index != -1) {
			Event event = new Event();
			event.item  = item;
			event.index = index + fFirstRowOffset;
			event.doit  = true;
			notifyListeners(SWT.SetData, event);
		}
	}

	// ------------------------------------------------------------------------
	// Slider handling
	// ------------------------------------------------------------------------

	private void createSlider() {
		fSlider = new Slider(this, SWT.VERTICAL);
		fSlider.setMinimum(0);
		fSlider.setMaximum(0);

		fSlider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				setSelection();
			}
		});

		fSlider.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				switch (event.detail) {
					case SWT.ARROW_DOWN:
					case SWT.ARROW_UP:
					case SWT.NONE:
					case SWT.END:
					case SWT.HOME:
					case SWT.PAGE_DOWN:
					case SWT.PAGE_UP: {
			        	fFirstRowOffset = fSlider.getSelection();
						setSelection();
						break;
					}
		        }
			}
		});
	}

	// ------------------------------------------------------------------------
	// Simulated Table API
	// ------------------------------------------------------------------------

	public void setHeaderVisible(boolean b) {
		fTable.setHeaderVisible(b);
	}

	public void setLinesVisible(boolean b) {
		fTable.setLinesVisible(b);
	}

	public TableItem[] getSelection() {
		return fSelectedItems;
	}
	
	public void addSelectionListener(SelectionAdapter sa) {
		fTable.addSelectionListener(sa);
	}	
	
	public void setItemCount(int nbItems) {
		nbItems = Math.max(0, nbItems);
		if (nbItems != fTableItemCount) {
			fTableItemCount = nbItems;
			fSlider.setMaximum(nbItems);
			resize();
		}
	}

	public int getItemHeight() {
		return fTable.getItemHeight();
	}

	public int getTopIndex() {
		return fFirstRowOffset;
	}

	public void setTopIndex(int i) {
		fSlider.setSelection(i);
	}
	
	public int indexOf(TableItem ti) {
		return fTable.indexOf(ti) + getTopIndex();
	}
	
	public TableColumn[] getColumns() {
		return fTable.getColumns();
	}

	private void resize() {

		// Compute the numbers of rows that fit the new area
		Rectangle clientArea = getClientArea();
		int tableHeight = clientArea.height - fTable.getHeaderHeight();
		int itemHeight = fTable.getItemHeight();
		fRowsDisplayed = tableHeight / itemHeight + 1;	// For partial rows
		if (fTableItemCount == 0) {
			fRowsDisplayed = 0;
		}

		// Re-size and re-create the virtual table if needed
		int delta = fTable.getItemCount() - fRowsDisplayed;
		if (delta != 0) {
			fTable.removeAll();
			if (fTableItems != null) {
				for (int i = 0; i < fTableItems.length; i++) {
					if (fTableItems[i] != null) {
						fTableItems[i].dispose();
					}
					fTableItems[i] = null;
				}
			}
			fTableItems = new TableItem[fRowsDisplayed];
			for (int i = 0; i < fTableItems.length; i++) {
				fTableItems[i] = new TableItem(fTable, i);
			}
		}

		refresh();
	}

	// ------------------------------------------------------------------------
	// Controls interactions
	// ------------------------------------------------------------------------

	@Override
	public boolean setFocus() {
		boolean isVisible = isVisible();
		if (isVisible) {
			fTable.setFocus();
		}
		return isVisible;
	}
	
	public void refresh() {
		setSelection();
	}

	public void setColumnHeaders(ColumnData columnData[]) {
		for (int i = 0; i < columnData.length; i++) {
			TableColumn column = new TableColumn(fTable, columnData[i].alignment, i);
			column.setText(columnData[i].header);
			if (columnData[i].width > 0) {
				column.setWidth(columnData[i].width);
			} else {
				column.pack();
			}
		}
	}    

	public int removeAll() {
		fSlider.setMaximum(0);
		fTable.removeAll();
		return 0;
	}
	
	private void setSelection() {
		if ((fEffectiveRow >= fFirstRowOffset) && (fEffectiveRow < (fFirstRowOffset + fRowsDisplayed))) {
			fTableRow = fEffectiveRow - fFirstRowOffset;
			fTable.setSelection(fTableRow);
		} else {
			fTable.deselect(fTableRow);
		}

		for (int i = 0; i < fRowsDisplayed; i++) {
			setDataItem(fTableItems[i]);
		}
	}

	public void setSelection(int i) {
		if (fTableItems != null) {
			i = Math.min(i, fTableItemCount);
			i = Math.max(i, 0);
			fSlider.setSelection(i);
			setSelection();
		}
	}

}
