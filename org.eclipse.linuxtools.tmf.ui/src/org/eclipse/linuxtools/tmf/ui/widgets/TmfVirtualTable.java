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

import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.signal.TmfTimeSynchSignal;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
	private Table   fTable;
	private int     fTableRows         = 0;      // Number of table rows
	private boolean fPartialRowVisible = false;  // Indicates that a row is partially displayed
	private int     fSelectedRow       = 0;      // Currently selected row in the table 

	private int     fTableTopEventRank = 0;      // Global rank of the first entry displayed
	private int     fSelectedEventRank = 0;      // Global rank of the selected event

	private TableItem fSelectedItems[] = new TableItem[1];
	private int       fTableItemCount  = 0;
	private TableItem fTableItems[];

	// The slider
	private Slider fSlider;

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
		createTable(style);
		createSlider();

		// Set the layout
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing   = 0;
		gridLayout.marginWidth  = 0;
		gridLayout.marginHeight = 0;
		setLayout(gridLayout);
		
		GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		fTable.setLayoutData(tableGridData);

		GridData sliderGridData = new GridData(SWT.FILL, SWT.FILL, false, true);
		fSlider.setLayoutData(sliderGridData);

		// Add the listeners
		fTable.addMouseWheelListener(new MouseWheelListener() {
			public void mouseScrolled(MouseEvent event) {
				fTableTopEventRank -= event.count;
				if (fTableTopEventRank < 0) {
					fTableTopEventRank = 0;
				}
				int latestFirstRowOffset = fTableItemCount - fTableRows;
				if (fTableTopEventRank > latestFirstRowOffset) {
					fTableTopEventRank = latestFirstRowOffset;
				}

				fSlider.setSelection(fTableTopEventRank);
				refreshTable();
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
	private void createTable(int style) {

//		int tableStyle = SWT.NO_SCROLL | SWT.H_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION; 
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
		fSelectedRow       = fTable.getSelectionIndices()[0];
		fSelectedEventRank = fTableTopEventRank + fSelectedRow;
		fSelectedItems[0]  = fTable.getSelection()[0];
	}

	/**
	 * Handle key-based navigation in table.
	 * 
	 * @param event
	 */
	private void handleTableKeyEvent(KeyEvent event) {

		int lastEventRank        = fTableItemCount - 1;
		int lastPageTopEntryRank = fTableItemCount - fTableRows;

		int lastRowIndex = ((fTableItemCount < fTableRows) ? fTableItemCount : fTableRows) - 1;
		int numberOfFullyVisibleRows = fTableRows - ((fPartialRowVisible) ? 1 : 0);

		boolean needsRefresh = false;

		// We are handling things...
		event.doit = false;

		// In all case, perform the following steps:
		// - Update the selected entry rank (within valid range)
		// - Update the selected row
		// - Update the page's top entry if necessary (which also adjusts the selected row)
		// - If the top displayed entry was changed, table refresh is needed
		switch (event.keyCode) {

			case SWT.ARROW_DOWN: {
				if (fSelectedEventRank < lastEventRank) {
					fSelectedEventRank++;
					fSelectedRow = fSelectedEventRank - fTableTopEventRank;
					if (fSelectedRow > lastRowIndex) {
						fTableTopEventRank++;
						fSelectedRow = lastRowIndex;
						needsRefresh = true;
					}
				}
				break;
			}

			case SWT.ARROW_UP: {
				if (fSelectedEventRank > 0) {
					fSelectedEventRank--;
					fSelectedRow = fSelectedEventRank - fTableTopEventRank;
					if (fSelectedRow < 0) {
						fTableTopEventRank--;
						fSelectedRow = 0;
						needsRefresh = true;
					}
				}
				break;
			}

			case SWT.END: {
				fTableTopEventRank = lastPageTopEntryRank;
				fSelectedEventRank = lastEventRank;
				fSelectedRow = lastRowIndex;
				needsRefresh = true;
				break;
			}

			case SWT.HOME: {
				fSelectedEventRank = 0;
				fSelectedRow       = 0;
				fTableTopEventRank = 0;
				needsRefresh       = true;
				break;
			}

			case SWT.PAGE_DOWN: {
				if (fSelectedEventRank < lastEventRank) {
					fSelectedEventRank += numberOfFullyVisibleRows;
					if (fSelectedEventRank > lastEventRank) {
						fSelectedEventRank = lastEventRank;
					}
					fSelectedRow = fSelectedEventRank - fTableTopEventRank;
					if (fSelectedRow > numberOfFullyVisibleRows - 1) {
						fTableTopEventRank += numberOfFullyVisibleRows;
						if (fTableTopEventRank > lastPageTopEntryRank) {
							fTableTopEventRank = lastPageTopEntryRank;
						}
						fSelectedRow = fSelectedEventRank - fTableTopEventRank;
						needsRefresh = true;
					}
				}
				break;
			}

			case SWT.PAGE_UP: {
				if (fSelectedEventRank > 0) {
					fSelectedEventRank -= numberOfFullyVisibleRows;
					if (fSelectedEventRank < 0) {
						fSelectedEventRank = 0;
					}
					fSelectedRow = fSelectedEventRank - fTableTopEventRank;
					if (fSelectedRow < 0) {
						fSelectedRow = 0;
						fTableTopEventRank -= numberOfFullyVisibleRows;
						if (fTableTopEventRank < 0) {
							fTableTopEventRank = 0;
						}
						fSelectedRow = fSelectedEventRank - fTableTopEventRank;
						needsRefresh = true;
					}
				}
				break;
			}
		}
	
		if (needsRefresh) {
			for (int i = 0; i < fTableItems.length; i++) {
				setDataItem(i, fTableItems[i]);
			}
		}

		fSlider.setSelection(fSelectedEventRank);
		fTable.setSelection(fSelectedRow);
		fTable.showSelection();
		fSelectedItems[0] = fTable.getSelection()[0];

        TmfTimestamp ts = (TmfTimestamp) fSelectedItems[0].getData();
        TmfSignalManager.dispatchSignal(new TmfTimeSynchSignal(this, ts));
	}

	private void setDataItem(int index, TableItem item) {
		if( index != -1) {
			Event event = new Event();
			event.item  = item;
			event.index = index + fTableTopEventRank;
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
			        	fTableTopEventRank = fSlider.getSelection();
						refreshTable();
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
		return fTableTopEventRank;
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
		int tableHeight = fTable.getClientArea().height - fTable.getHeaderHeight();

		if (tableHeight < 0) tableHeight = 0;
		int itemHeight  = fTable.getItemHeight();
		fTableRows  = tableHeight / itemHeight;
		fPartialRowVisible = false;
		if (fTableRows * itemHeight < tableHeight) {
			fTableRows++;	// For partial rows
			fPartialRowVisible = true;
		}
		if (fTableRows > fTableItemCount) {
			fTableRows = fTableItemCount;
		}
		
		// If we are at the end, get elements before to populate
		if (fTableTopEventRank + fTableRows >= fTableItemCount) {
			fTableTopEventRank = fTableItemCount - fTableRows;
		}
		
		// Set the slider thumb size
		if (fTableItemCount > 0) {
			fSlider.setThumb(fTableRows);
		}
		
		// Re-size and re-create the virtual table if needed
		int delta = fTable.getItemCount() - fTableRows;
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
			fTableItems = new TableItem[fTableRows];
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
		refreshTable();
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
	
	private void refreshTable() {
		int lastRowOffset = fTableTopEventRank + fTableRows - 1;
		if ((fSelectedEventRank >= fTableTopEventRank) && (fSelectedEventRank <= lastRowOffset)) {
			fSelectedRow = fSelectedEventRank - fTableTopEventRank;
			fTable.setSelection(fSelectedRow);
		} else {
			fTable.deselect(fSelectedRow);
		}

		for (int i = 0; i < fTableRows; i++) {
			setDataItem(i, fTableItems[i]);
		}
	}

	public void setSelection(int i) {
		if (fTableItems != null) {
			i = Math.min(i, fTableItemCount);
			i = Math.max(i, 0);
			fSlider.setSelection(i);

			fSelectedEventRank = i;
			fTableTopEventRank = i - (fTableRows / 2);
			if (fTableTopEventRank < 0) {
				fTableTopEventRank = 0;
			}
			fSelectedRow = fSelectedEventRank - fTableTopEventRank;
			
			refreshTable();
		}
	}

}
