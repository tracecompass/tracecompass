/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Factored out from events view
 *   Francois Chouinard - Replaced Table by TmfVirtualTable
 *   Patrick Tasse - Filter implementation (inspired by www.eclipse.org/mat)
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.linuxtools.tmf.component.ITmfDataProvider;
import org.eclipse.linuxtools.tmf.component.TmfComponent;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfEventContent;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.filter.ITmfFilter;
import org.eclipse.linuxtools.tmf.filter.model.ITmfFilterTreeNode;
import org.eclipse.linuxtools.tmf.filter.model.TmfFilterAndNode;
import org.eclipse.linuxtools.tmf.filter.model.TmfFilterMatchesNode;
import org.eclipse.linuxtools.tmf.filter.model.TmfFilterNode;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.signal.TmfExperimentUpdatedSignal;
import org.eclipse.linuxtools.tmf.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.signal.TmfTraceUpdatedSignal;
import org.eclipse.linuxtools.tmf.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.internal.Messages;
import org.eclipse.linuxtools.tmf.ui.views.colors.ColorSetting;
import org.eclipse.linuxtools.tmf.ui.views.colors.ColorSettingsManager;
import org.eclipse.linuxtools.tmf.ui.views.colors.IColorSettingsListener;
import org.eclipse.linuxtools.tmf.ui.views.filter.FilterManager;
import org.eclipse.linuxtools.tmf.ui.widgets.ColumnData;
import org.eclipse.linuxtools.tmf.ui.widgets.TmfRawEventViewer;
import org.eclipse.linuxtools.tmf.ui.widgets.TmfVirtualTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.themes.ColorUtil;

/**
 * <b><u>TmfEventsTable</u></b>
 */
public class TmfEventsTable extends TmfComponent implements IGotoMarker, IColorSettingsListener, ITmfEventsFilterProvider {

	private static final Image BOOKMARK_IMAGE = TmfUiPlugin.getDefault().getImageFromPath("icons/elcl16/bookmark_obj.gif"); //$NON-NLS-1$
	private static final Image SEARCH_IMAGE = TmfUiPlugin.getDefault().getImageFromPath("icons/elcl16/search.gif"); //$NON-NLS-1$
	private static final Image SEARCH_MATCH_IMAGE = TmfUiPlugin.getDefault().getImageFromPath("icons/elcl16/search_match.gif"); //$NON-NLS-1$
	private static final Image SEARCH_MATCH_BOOKMARK_IMAGE = TmfUiPlugin.getDefault().getImageFromPath("icons/elcl16/search_match_bookmark.gif"); //$NON-NLS-1$
	private static final Image FILTER_IMAGE = TmfUiPlugin.getDefault().getImageFromPath("icons/elcl16/filter_items.gif"); //$NON-NLS-1$
	private static final Image STOP_IMAGE = TmfUiPlugin.getDefault().getImageFromPath("icons/elcl16/stop.gif"); //$NON-NLS-1$
	private static final String SEARCH_HINT = Messages.TmfEventsTable_SearchHint;
	private static final String FILTER_HINT = Messages.TmfEventsTable_FilterHint;
    
	public interface Key {
		String SEARCH_TXT = "$srch_txt"; //$NON-NLS-1$
		String SEARCH_OBJ = "$srch_obj"; //$NON-NLS-1$
		String FILTER_TXT = "$fltr_txt"; //$NON-NLS-1$
		String FILTER_OBJ = "$fltr_obj"; //$NON-NLS-1$
		String TIMESTAMP = "$time"; //$NON-NLS-1$
		String RANK = "$rank"; //$NON-NLS-1$
		String FIELD_ID = "$field_id"; //$NON-NLS-1$
	}
	
	public static enum HeaderState {
		SEARCH,
		FILTER
	}
	
	interface Direction {
		int FORWARD  = +1;
		int BACKWARD = -1;
	}
	
    // ------------------------------------------------------------------------
    // Table data
    // ------------------------------------------------------------------------

    protected Composite fComposite;
    protected SashForm fSashForm;
    protected TmfVirtualTable fTable;
    protected TmfRawEventViewer fRawViewer;
    protected ITmfTrace fTrace;
    protected boolean fPackDone = false;
    protected HeaderState fHeaderState = HeaderState.SEARCH;
    protected long fSelectedRank = 0;
    
    // Filter data
    protected ArrayList<FilteredEvent> fFilteredEventCache = new ArrayList<FilteredEvent>();
    protected long fFilterMatchCount;
    protected long fFilterCheckCount;
    protected FilterThread fFilterThread;
    protected final Object fFilterSyncObj = new Object();
    protected SearchThread fSearchThread;
    protected final Object fSearchSyncObj = new Object();
    protected ArrayList<ITmfEventsFilterListener> fEventsFilterListeners = new ArrayList<ITmfEventsFilterListener>();
    
    // Bookmark map <Rank, MarkerId>
    protected Map<Long, Long> fBookmarksMap = new HashMap<Long, Long>();
    protected IResource fBookmarksResource;
    protected long fPendingGotoRank = -1;
    
    // SWT resources
    protected LocalResourceManager fResourceManager = new LocalResourceManager(JFaceResources.getResources());
    protected Color fGrayColor;
    protected Color fGreenColor;
    protected Font fBoldFont;

    // Table column names
    static private final String[] COLUMN_NAMES =  new String[] {
        Messages.TmfEventsTable_TimestampColumnHeader,
        Messages.TmfEventsTable_SourceColumnHeader,
        Messages.TmfEventsTable_TypeColumnHeader,
        Messages.TmfEventsTable_ReferenceColumnHeader,
        Messages.TmfEventsTable_ContentColumnHeader
    };

    static private ColumnData[] COLUMN_DATA = new ColumnData[] {
        new ColumnData(COLUMN_NAMES[0], 100, SWT.LEFT),
        new ColumnData(COLUMN_NAMES[1], 100, SWT.LEFT),
        new ColumnData(COLUMN_NAMES[2], 100, SWT.LEFT),
        new ColumnData(COLUMN_NAMES[3], 100, SWT.LEFT),
        new ColumnData(COLUMN_NAMES[4], 100, SWT.LEFT)
    };

    private class FilteredEvent {
    	TmfEvent event;
    	long rank;

    	public FilteredEvent (TmfEvent event, long rank) {
    		this.event = event;
    		this.rank = rank;
    	}
    }
    
    // ------------------------------------------------------------------------
    // Event cache
    // ------------------------------------------------------------------------

    private final int  fCacheSize;
    private TmfEvent[] fCache;
    private int fCacheStartIndex = 0;
    private int fCacheEndIndex   = 0;

    private boolean fDisposeOnClose;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public TmfEventsTable(Composite parent, int cacheSize) {
    	this(parent, cacheSize, COLUMN_DATA);
    }

    public TmfEventsTable(Composite parent, int cacheSize, ColumnData[] columnData) {
        super("TmfEventsTable"); //$NON-NLS-1$
        
        fComposite = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.verticalSpacing = 0;
        fComposite.setLayout(gl);

        fSashForm = new SashForm(fComposite, SWT.HORIZONTAL);
        fSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        // Create a virtual table
        final int style = SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION;
        fTable = new TmfVirtualTable(fSashForm, style);

        // Set the table layout
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        fTable.setLayoutData(layoutData);

        // Some cosmetic enhancements
        fTable.setHeaderVisible(true);
        fTable.setLinesVisible(true);

        // Set the columns
        setColumnHeaders(columnData);
        
        // Set the default column field ids if this is not a subclass
		if (Arrays.equals(columnData, COLUMN_DATA)) {
			fTable.getColumns()[0].setData(Key.FIELD_ID, TmfEventContent.FIELD_ID_TIMESTAMP);
			fTable.getColumns()[1].setData(Key.FIELD_ID, TmfEventContent.FIELD_ID_SOURCE);
			fTable.getColumns()[2].setData(Key.FIELD_ID, TmfEventContent.FIELD_ID_TYPE);
			fTable.getColumns()[3].setData(Key.FIELD_ID, TmfEventContent.FIELD_ID_REFERENCE);
			fTable.getColumns()[4].setData(Key.FIELD_ID, TmfEventContent.FIELD_ID_CONTENT);
		}

        // Set the frozen row for header row
        fTable.setFrozenRowCount(1);

        // Create the header row cell editor
        createHeaderEditor();
        
        // Handle the table item selection
        fTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
        		TableItem[] selection = fTable.getSelection();
        		if (selection.length > 0) {
        			TableItem selectedTableItem = selection[0];
        			if (selectedTableItem != null) {
        				if (selectedTableItem.getData(Key.RANK) instanceof Long) {
        					fSelectedRank = (Long) selectedTableItem.getData(Key.RANK);
        					fRawViewer.selectAndReveal((Long) selectedTableItem.getData(Key.RANK));
        				}
        				if (selectedTableItem.getData(Key.TIMESTAMP) instanceof TmfTimestamp) {
        					TmfTimestamp ts = (TmfTimestamp) selectedTableItem.getData(Key.TIMESTAMP);
        					broadcast(new TmfTimeSynchSignal(fTable, ts));
        				}
        			}
        		}
            }
        });

        fCacheSize = Math.max(cacheSize, Display.getDefault().getBounds().height / fTable.getItemHeight());
        fCache = new TmfEvent[fCacheSize];

        // Handle the table item requests 
        fTable.addListener(SWT.SetData, new Listener() {

            @Override
			public void handleEvent(Event event) {

                final TableItem item = (TableItem) event.item;
                final int index = event.index - 1; // -1 for the header row

                if (index == -1) {
                    String txtKey = null;
                    if (fHeaderState == HeaderState.SEARCH) {
                        item.setImage(SEARCH_IMAGE);
                        txtKey = Key.SEARCH_TXT;
                    } else if (fHeaderState == HeaderState.FILTER) {
                        item.setImage(FILTER_IMAGE);
                        txtKey = Key.FILTER_TXT;
                    }
                    item.setForeground(fGrayColor);
                    for (int i = 0; i < fTable.getColumns().length; i++) {
                        TableColumn column = fTable.getColumns()[i];
                        String filter = (String) column.getData(txtKey);
                        if (filter == null) {
                            if (fHeaderState == HeaderState.SEARCH) {
                                item.setText(i, SEARCH_HINT);
                            } else if (fHeaderState == HeaderState.FILTER) {
                                item.setText(i, FILTER_HINT);
                            }
                            item.setForeground(i, fGrayColor);
                            item.setFont(i, fTable.getFont());
                        } else {
                            item.setText(i, filter);
                            item.setForeground(i, fGreenColor);
                            item.setFont(i, fBoldFont);
                        }
                    }
                    return;
                }

                // If available, return the cached data  
                if ((index >= fCacheStartIndex) && (index < fCacheEndIndex)) {
                    int i = index - fCacheStartIndex;
                    setItemData(item, fCache[i], index);
                    return;
                }

                // If filter is applied, use the filtered event cache
                if (fTable.getData(Key.FILTER_OBJ) != null) {
                	setFilteredItemData(item, index);
                	return;
                }

                // Else, fill the cache asynchronously (and off the UI thread)
                populateCache(index);
                event.doit = false;
            }
        });

        fTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent event) {
				if (event.button != 1) {
					return;
				}
				// Identify the selected row
				Point point = new Point(event.x, event.y);
				TableItem item = fTable.getItem(point);
            	if (item != null) {
            		Rectangle imageBounds = item.getImageBounds(0);
            		imageBounds.width = BOOKMARK_IMAGE.getBounds().width;
            		if (imageBounds.contains(point)) {
                		Long rank = (Long) item.getData(Key.RANK);
                		if (rank != null) {
                			toggleBookmark(rank);
                		}
            		}
            	}
			}
        });
        
        // Create resources
        createResources();

        ColorSettingsManager.addColorSettingsListener(this);
        
        fTable.setItemCount(+1); // +1 for header row
        
        fRawViewer = new TmfRawEventViewer(fSashForm, SWT.H_SCROLL | SWT.V_SCROLL);
        
        fRawViewer.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.data instanceof Long) {
                    long rank = (Long) e.data;
                    int index = (int) rank;
                    if (fTable.getData(Key.FILTER_OBJ) != null) {
                    	index = getFilteredEventIndex(rank) + 1; // +1 for top filter status row
                    }
                    fTable.setSelection(index + 1); // +1 for header row
                    fSelectedRank = rank;
                } else if (e.data instanceof ITmfLocation<?>) {
                	// DOES NOT WORK: rank undefined in context from seekLocation()
//                    ITmfLocation<?> location = (ITmfLocation<?>) e.data;
//                    TmfContext context = fTrace.seekLocation(location);
//                    fTable.setSelection((int) context.getRank());
                    return;
                } else {
                    return;
                }
                TableItem[] selection = fTable.getSelection();
                if (selection != null && selection.length > 0) {
                    TmfTimestamp ts = (TmfTimestamp) fTable.getSelection()[0].getData(Key.TIMESTAMP);
                    if (ts != null) {
                    	broadcast(new TmfTimeSynchSignal(fTable, ts));
                    }
                }
            }
        });

        fSashForm.setWeights(new int[] {1, 1});
        fRawViewer.setVisible(false);

        createPopupMenu();
    }

    protected void createPopupMenu() {
        final IAction showTableAction = new Action(Messages.TmfEventsTable_ShowTableActionText) {
            @Override
            public void run() {
                fTable.setVisible(true);
                fSashForm.layout();
            }
        };
        
        final IAction hideTableAction = new Action(Messages.TmfEventsTable_HideTableActionText) {
            @Override
            public void run() {
                fTable.setVisible(false);
                fSashForm.layout();
            }
        };
        
        final IAction showRawAction = new Action(Messages.TmfEventsTable_ShowRawActionText) {
            @Override
            public void run() {
                fRawViewer.setVisible(true);
                fSashForm.layout();
                int index = fTable.getSelectionIndex();
                if (index >= +1) { // +1 for header row
                	fRawViewer.selectAndReveal(index - 1);
                }
            }
        };
            
        final IAction hideRawAction = new Action(Messages.TmfEventsTable_HideRawActionText) {
            @Override
            public void run() {
                fRawViewer.setVisible(false);
                fSashForm.layout();
            }
        };

        final IAction showSearchBarAction = new Action(Messages.TmfEventsTable_ShowSearchBarActionText) {
            @Override
            public void run() {
            	fHeaderState = HeaderState.SEARCH;
            	fTable.refresh();
            }
        };

        final IAction showFilterBarAction = new Action(Messages.TmfEventsTable_ShowFilterBarActionText) {
            @Override
            public void run() {
            	fHeaderState = HeaderState.FILTER;
            	fTable.refresh();
            }
        };

        final IAction clearFiltersAction = new Action(Messages.TmfEventsTable_ClearFiltersActionText) {
            @Override
            public void run() {
            	stopFilterThread();
            	stopSearchThread();
            	clearFilters();
            }
        };

        class ToggleBookmarkAction extends Action {
        	long fRank;
        	public ToggleBookmarkAction(String text, long rank) {
        		super(text);
        		fRank = rank;
        	}
        	
        	@Override
            public void run() {
            	toggleBookmark(fRank);
            }
        }

        final MenuManager tablePopupMenu = new MenuManager();
        tablePopupMenu.setRemoveAllWhenShown(true);
        tablePopupMenu.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
            	if (fTable.getSelectionIndex() == 0) {
            		// Right-click on header row
            		if (fHeaderState == HeaderState.FILTER) {
            			tablePopupMenu.add(showSearchBarAction);
            		} else {
                        tablePopupMenu.add(showFilterBarAction);
            		}
            		return;
            	}
				Point point = fTable.toControl(Display.getDefault().getCursorLocation());
				TableItem item = fTable.getItem(point);
            	if (item != null) {
            		Rectangle imageBounds = item.getImageBounds(0);
            		imageBounds.width = BOOKMARK_IMAGE.getBounds().width;
            		if (imageBounds.contains(point)) {
            			// Right-click on left margin 
                		Long rank = (Long) item.getData(Key.RANK);
                		if (rank != null && fBookmarksResource != null) {
                			if (fBookmarksMap.containsKey(rank)) {
                                tablePopupMenu.add(new ToggleBookmarkAction(Messages.TmfEventsTable_RemoveBookmarkActionText, rank));
                			} else {
                                tablePopupMenu.add(new ToggleBookmarkAction(Messages.TmfEventsTable_AddBookmarkActionText, rank));
                			}
                		}
                		return;
            		}
            	}
            	// Right-click on table
                if (fTable.isVisible() && fRawViewer.isVisible()) {
                    tablePopupMenu.add(hideTableAction);
                    tablePopupMenu.add(hideRawAction);
                } else if (!fTable.isVisible()) {
                    tablePopupMenu.add(showTableAction);
                } else if (!fRawViewer.isVisible()) {
                    tablePopupMenu.add(showRawAction);
                }
                tablePopupMenu.add(new Separator());
                tablePopupMenu.add(clearFiltersAction);
                ITmfFilterTreeNode[] savedFilters = FilterManager.getSavedFilters();
                if (savedFilters.length > 0) {
                    MenuManager subMenu = new MenuManager(Messages.TmfEventsTable_ApplyPresetFilterMenuName);
                    for (ITmfFilterTreeNode node : savedFilters) {
                    	if (node instanceof TmfFilterNode) {
                    		final TmfFilterNode filter = (TmfFilterNode) node;
                    		subMenu.add(new Action(filter.getFilterName()) {
                    			@Override
                    			public void run() {
                					synchronized (fFilteredEventCache) {
                						fFilteredEventCache.clear();
                						fFilterMatchCount = 0;
                						fFilterCheckCount = 0;
                					}
                					fCacheStartIndex = 0;
                					fCacheEndIndex = 0;
            						fTable.clearAll();
            						fTable.setData(Key.FILTER_OBJ, filter);
            						fTable.setItemCount(3); // +1 for header row, +2 for top and bottom filter status rows
            						startFilterThread();
            						fireFilterApplied(filter);
                    			}});
                    	}
                    }
                    tablePopupMenu.add(subMenu);
                }
                appendToTablePopupMenu(tablePopupMenu, item);
            }
        });
        
        final MenuManager rawViewerPopupMenu = new MenuManager();
        rawViewerPopupMenu.setRemoveAllWhenShown(true);
        rawViewerPopupMenu.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                if (fTable.isVisible() && fRawViewer.isVisible()) {
                	rawViewerPopupMenu.add(hideTableAction);
                	rawViewerPopupMenu.add(hideRawAction);
                } else if (!fTable.isVisible()) {
                	rawViewerPopupMenu.add(showTableAction);
                } else if (!fRawViewer.isVisible()) {
                	rawViewerPopupMenu.add(showRawAction);
                }
                appendToRawPopupMenu(tablePopupMenu);
            }
        });
        
        Menu menu = tablePopupMenu.createContextMenu(fTable);
        fTable.setMenu(menu);
        
        menu = rawViewerPopupMenu.createContextMenu(fRawViewer);
        fRawViewer.setMenu(menu);
    }

    protected void appendToTablePopupMenu(MenuManager tablePopupMenu, TableItem selectedItem) {
    	// override to append more actions
    }
    
    protected void appendToRawPopupMenu(MenuManager rawViewerPopupMenu) {
    	// override to append more actions
    }
    
    @Override
	public void dispose() {
    	stopSearchThread();
    	stopFilterThread();
        ColorSettingsManager.removeColorSettingsListener(this);
    	fComposite.dispose();
        if (fTrace != null && fDisposeOnClose) {
            fTrace.dispose();
        }
        fResourceManager.dispose();
        super.dispose();
    }

    public void setLayoutData(Object layoutData) {
    	fComposite.setLayoutData(layoutData);
    }
    
    public TmfVirtualTable getTable() {
        return fTable;
    }
    
    /**
     * @param table
     * 
     * FIXME: Add support for column selection
     */
    protected void setColumnHeaders(ColumnData[] columnData) {
    	fTable.setColumnHeaders(columnData);
    }

    protected void setItemData(TableItem item, TmfEvent event, long rank) {
    	item.setText(extractItemFields(event));
    	item.setData(Key.TIMESTAMP, new TmfTimestamp(event.getTimestamp()));
    	item.setData(Key.RANK, rank);
    	
    	boolean bookmark = false;
    	if (fBookmarksMap.containsKey(rank)) {
    		bookmark = true;
    	}
    	
    	boolean searchMatch = false;
    	boolean searchNoMatch = false;
    	ITmfFilter searchFilter = (ITmfFilter) fTable.getData(Key.SEARCH_OBJ);
    	if (searchFilter != null) {
    		if (searchFilter.matches(event)) {
    			searchMatch = true;
    		} else {
    			searchNoMatch = true;
    		}
    	}
    	
    	ColorSetting colorSetting = ColorSettingsManager.getColorSetting(event);
    	if (searchNoMatch) {
        	item.setForeground(colorSetting.getDimmedForegroundColor());
        	item.setBackground(colorSetting.getDimmedBackgroundColor());
    	} else {
    		item.setForeground(colorSetting.getForegroundColor());
    		item.setBackground(colorSetting.getBackgroundColor());
    	}
    	
    	if (searchMatch) {
    		if (bookmark) {
    			item.setImage(SEARCH_MATCH_BOOKMARK_IMAGE);
    		} else {
    			item.setImage(SEARCH_MATCH_IMAGE);
    		}
    	} else if (bookmark) {
			item.setImage(BOOKMARK_IMAGE);
    	} else {
			item.setImage((Image) null);
    	}
    }
    
    protected void setFilteredItemData(final TableItem item, int index) {
    	synchronized (fFilteredEventCache) {
			if (index > 0 && --index < fFilteredEventCache.size()) { // top and bottom rows are filter status rows
				FilteredEvent filteredEvent = fFilteredEventCache.get(index);
				setItemData(item, filteredEvent.event, filteredEvent.rank);
				return;
			}
			for (int i = 0; i < fTable.getColumns().length; i++) {
	    		if (i == 0) {
	    			if (fFilterCheckCount == fTrace.getNbEvents()) {
	    				item.setImage(FILTER_IMAGE);
	    			} else {
	    				item.setImage(STOP_IMAGE);
	    			}
	    			item.setText(0, fFilterMatchCount + "/" + fFilterCheckCount); //$NON-NLS-1$
	    		} else {
	    			item.setText(i, ""); //$NON-NLS-1$
	    		}
	    	}
    	}
    	item.setData(Key.TIMESTAMP, null);
    	item.setData(Key.RANK, null);
    	item.setForeground(null);
    	item.setBackground(null);
    }
    
    protected int getFilteredEventIndex(long rank) {
    	synchronized (fFilteredEventCache) {
	        for (int i = 0; i < fFilteredEventCache.size(); i++) {
	        	FilteredEvent filteredEvent = fFilteredEventCache.get(i);
	        	if (filteredEvent.rank >= rank) {
	        		return i;
	        	}
	        }
    	}
    	return fFilteredEventCache.size();
    }
    
    protected void createHeaderEditor() {
    	final TableEditor tableEditor = fTable.createTableEditor();
    	tableEditor.horizontalAlignment = SWT.LEFT;
    	tableEditor.verticalAlignment = SWT.CENTER;
    	tableEditor.grabHorizontal = true;
    	tableEditor.minimumWidth = 50;

        // Handle the header row selection
        fTable.addMouseListener(new MouseAdapter() {
        	int columnIndex;
        	TableColumn column;
        	TableItem item;
        	
			@Override
			public void mouseDown(MouseEvent event) {
				if (event.button != 1) {
					return;
				}
				// Identify the selected row
				Point point = new Point(event.x, event.y);
				item = fTable.getItem(point);
				
				// Header row selected
            	if (item != null && fTable.indexOf(item) == 0) {

            		// Icon selected
            		if (item.getImageBounds(0).contains(point)) {
            			if (fHeaderState == HeaderState.SEARCH) {
            				fHeaderState = HeaderState.FILTER;
            			} else if (fHeaderState == HeaderState.FILTER) {
            				fHeaderState = HeaderState.SEARCH;
            			}
            			fTable.refresh();
            			return;
            		}
            		
        			// Identify the selected column
        			columnIndex = -1;
        			for (int i = 0; i < fTable.getColumns().length; i++) {
        				Rectangle rect = item.getBounds(i);
        				if (rect.contains(point)) {
        					columnIndex = i;
        					break;
        				}
        			}
        			
        			if (columnIndex == -1) {
        				return;
        			}
        			
        			column = fTable.getColumns()[columnIndex];
        			
    				String txtKey = null;
        			if (fHeaderState == HeaderState.SEARCH) {
        				txtKey = Key.SEARCH_TXT;
        			} else if (fHeaderState == HeaderState.FILTER) {
        				txtKey = Key.FILTER_TXT;
        			}
        			
        			// The control that will be the editor must be a child of the Table
        			final Text newEditor = (Text) fTable.createTableEditorControl(Text.class);
        			String headerString = (String) column.getData(txtKey);
					if (headerString != null) {
						newEditor.setText(headerString);
					}
        			newEditor.addFocusListener(new FocusAdapter() {
        				@Override
						public void focusLost(FocusEvent e) {
							boolean changed = updateHeader(newEditor.getText());
							if (changed) {
								applyHeader();
							}
						}
        			});
        			newEditor.addKeyListener(new KeyAdapter() {
						@Override
						public void keyPressed(KeyEvent e) {
							if (e.character == SWT.CR) {
								updateHeader(newEditor.getText());
								applyHeader();
							} else if (e.character == SWT.ESC) {
								tableEditor.getEditor().dispose();
							}
						}
					});
        			newEditor.selectAll();
        			newEditor.setFocus();
        			tableEditor.setEditor(newEditor, item, columnIndex);
            	}
			}
			
			/*
			 * returns true is value was changed
			 */
			private boolean updateHeader(String text) {
				String objKey = null;
				String txtKey = null;
    			if (fHeaderState == HeaderState.SEARCH) {
    				objKey = Key.SEARCH_OBJ;
    				txtKey = Key.SEARCH_TXT;
    			} else if (fHeaderState == HeaderState.FILTER) {
    				objKey = Key.FILTER_OBJ;
    				txtKey = Key.FILTER_TXT;
    			}
				if (text.trim().length() > 0) {
					try {
						String regex = TmfFilterMatchesNode.regexFix(text);
						Pattern.compile(regex);
						if (regex.equals(column.getData(txtKey))) {
							tableEditor.getEditor().dispose();
							return false;
						}
						TmfFilterMatchesNode filter = new TmfFilterMatchesNode(null);
						String fieldId = (String) column.getData(Key.FIELD_ID);
						if (fieldId == null) {
							fieldId = column.getText();
						}
						filter.setField(fieldId);
						filter.setRegex(regex);
						column.setData(objKey, filter);
						column.setData(txtKey, regex);
					} catch (PatternSyntaxException ex) {
						tableEditor.getEditor().dispose();
						MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), ex.getDescription(), ex.getMessage());
						return false;
					}
				} else {
					if (column.getData(txtKey) == null) {
						tableEditor.getEditor().dispose();
						return false;
					}
					column.setData(objKey, null);
					column.setData(txtKey, null);
				}
				return true;
			}
			
			private void applyHeader() {
				stopSearchThread();
				if (fHeaderState == HeaderState.SEARCH) {
					final TmfFilterAndNode filter = new TmfFilterAndNode(null);
					for (TableColumn column : fTable.getColumns()) {
						Object filterObj = column.getData(Key.SEARCH_OBJ);
						if (filterObj instanceof ITmfFilterTreeNode) {
							filter.addChild((ITmfFilterTreeNode) filterObj);
						}
					}
					if (filter.getChildrenCount() > 0) {
						fTable.setData(Key.SEARCH_OBJ, filter);
						fTable.refresh();
						searchNext();
						fireSearchApplied(filter);
					} else {
						fTable.setData(Key.SEARCH_OBJ, null);
						fTable.refresh();
						fireSearchApplied(null);
					}
				} else if (fHeaderState == HeaderState.FILTER) {
					synchronized (fFilteredEventCache) {
						fFilteredEventCache.clear();
						fFilterMatchCount = 0;
						fFilterCheckCount = 0;
					}
					fCacheStartIndex = 0;
					fCacheEndIndex = 0;
					TmfFilterAndNode filter = new TmfFilterAndNode(null);
					for (TableColumn column : fTable.getColumns()) {
						Object filterObj = column.getData(Key.FILTER_OBJ);
						if (filterObj instanceof ITmfFilterTreeNode) {
							filter.addChild((ITmfFilterTreeNode) filterObj);
						}
					}
					if (filter.getChildrenCount() > 0) {
						fTable.clearAll();
						fTable.setData(Key.FILTER_OBJ, filter);
						fTable.setItemCount(3); // +1 for header row, +2 for top and bottom filter status rows
						startFilterThread();
						fireFilterApplied(filter);
					} else {
						stopFilterThread();
						fTable.clearAll();
						fTable.setData(Key.FILTER_OBJ, null);
                        fTable.setItemCount((int) fTrace.getNbEvents() + 1); // +1 for header row
						fireFilterApplied(null);
					}
				}
				
				tableEditor.getEditor().dispose();
			}
        });
        
        fTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				e.doit = false;
				if (e.character == SWT.ESC) {
					stopFilterThread();
					stopSearchThread();
					fTable.refresh();
				} else if (e.character == SWT.DEL) {
					stopFilterThread();
					stopSearchThread();
					if (fHeaderState == HeaderState.SEARCH) {
						for (TableColumn column : fTable.getColumns()) {
							column.setData(Key.SEARCH_OBJ, null);
							column.setData(Key.SEARCH_TXT, null);
						}
						fTable.setData(Key.SEARCH_OBJ, null);
						fTable.refresh();
						fireSearchApplied(null);
					} else if (fHeaderState == HeaderState.FILTER) {
						clearFilters();
					}
				} else if (e.character == SWT.CR) {
					if ((e.stateMask & SWT.SHIFT) == 0) {
						searchNext();
					} else {
						searchPrevious();
					}
				}
			}
        });
	}

    protected void fireFilterApplied(ITmfFilter filter) {
        for (ITmfEventsFilterListener listener : fEventsFilterListeners) {
            listener.filterApplied(filter, fTrace);
        }
    }
    
    protected void fireSearchApplied(ITmfFilter filter) {
        for (ITmfEventsFilterListener listener : fEventsFilterListeners) {
            listener.searchApplied(filter, fTrace);
        }
    }
    
    protected void startFilterThread() {
		synchronized (fFilterSyncObj) {
			if (fFilterThread != null) {
				fFilterThread.cancel();
			}
	    	final ITmfFilterTreeNode filter = (ITmfFilterTreeNode) fTable.getData(Key.FILTER_OBJ);
			fFilterThread = new FilterThread(filter);
			fFilterThread.start();
		}
    }

    protected void stopFilterThread() {
		synchronized (fFilterSyncObj) {
			if (fFilterThread != null) {
				fFilterThread.cancel();
			}
		}
    }

    protected void clearFilters() {
		if (fTable.getData(Key.FILTER_OBJ) == null) {
			return;
		}
		long selectedRank = -1;
		synchronized (fFilteredEventCache) {
			int selectedIndex = fTable.getSelectionIndex() - 2; // -1 for header row, -1 for top filter status row
			if (selectedIndex >= 0 && selectedIndex < fFilteredEventCache.size()) {
				selectedRank = fFilteredEventCache.get(selectedIndex).rank;
			}
		}
		fTable.clearAll();
		for (TableColumn column : fTable.getColumns()) {
			column.setData(Key.FILTER_OBJ, null);
			column.setData(Key.FILTER_TXT, null);
		}
		fTable.setData(Key.FILTER_OBJ, null);
        fTable.setItemCount((int) fTrace.getNbEvents() + 1); // +1 for header row
		synchronized (fFilteredEventCache) {
			fFilteredEventCache.clear();
			fFilterMatchCount = 0;
			fFilterCheckCount = 0;
		}
		if (selectedRank >= 0) {
			fTable.setSelection((int) selectedRank + 1); // +1 for header row
		} else {
			fTable.setSelection(0);
		}
		fireFilterApplied(null);
    }
    
    protected class FilterThread extends Thread {
    	private final ITmfFilterTreeNode filter;
    	private TmfDataRequest<TmfEvent> request;
    	private long lastRefreshTime;

    	public FilterThread(ITmfFilterTreeNode filter) {
    		super("Filter Thread"); //$NON-NLS-1$
    		this.filter = filter;
    	}

    	@SuppressWarnings("unchecked")
    	@Override
    	public void run() {
    		final Display display = Display.getDefault();
    		lastRefreshTime = System.currentTimeMillis();

    		request = new TmfDataRequest<TmfEvent>(TmfEvent.class, 0, Integer.MAX_VALUE, ExecutionType.BACKGROUND) {
    			@Override
    			public void handleData(TmfEvent event) {
    				super.handleData(event);
    				fFilterCheckCount++;
    				if (filter.matches(event)) {
    					synchronized (fFilteredEventCache) {
    						fFilteredEventCache.add(new FilteredEvent(event.clone(), getNbRead() - 1));
    						fFilterMatchCount++;
    					}
    					display.asyncExec(new Runnable() {
    						@Override
    						public void run() {
    							if (request.isCancelled()) return;
    							if (fTable.isDisposed()) return;
    							fTable.setItemCount(fFilteredEventCache.size() + 3); // +1 for header row, +2 for top and bottom filter status rows
    						}
    					});
    				}
    				if (fFilterCheckCount % 100 == 0) {
    					long currentTime = System.currentTimeMillis();
    					if (currentTime - lastRefreshTime > 1000) {
    						lastRefreshTime = currentTime;
    						display.asyncExec(new Runnable() {
    							@Override
    							public void run() {
    								if (request.isCancelled()) return;
    								if (fTable.isDisposed()) return;
    								fTable.refresh();
    							}
    						});
    					}
    				}
    			}
    		};
    		((ITmfDataProvider<TmfEvent>) fTrace).sendRequest(request);
    		try {
    			request.waitForCompletion();
    		} catch (InterruptedException e) {
    		}
    		display.asyncExec(new Runnable() {
    			@Override
    			public void run() {
    				if (fTable.isDisposed()) return;
    				fTable.refresh();
    			}
    		});
    	}

    	public void cancel() {
    		request.cancel();
    	}
    }

    protected void searchNext() {
		synchronized (fSearchSyncObj) {
			if (fSearchThread != null) {
				return;
			}
	    	final ITmfFilterTreeNode filter = (ITmfFilterTreeNode) fTable.getData(Key.SEARCH_OBJ);
	    	if (filter == null) {
	    		return;
	    	}
	    	int selectionIndex = fTable.getSelectionIndex();
			int startIndex;
			if (selectionIndex > 0) {
				startIndex = selectionIndex; // -1 for header row, +1 for next event
			} else {
				// header row is selected, start at top event
				startIndex = Math.max(0, fTable.getTopIndex() - 1); // -1 for header row
			}
			if (fTable.getData(Key.FILTER_OBJ) != null) {
				fSearchThread = new SearchFilteredThread(filter, Math.max(0, startIndex - 1), Direction.FORWARD); // -1 for top filter status row
			} else {
				fSearchThread = new SearchThread(filter, startIndex, Direction.FORWARD);
			}
			fSearchThread.schedule();
		}
    }
    
    protected void searchPrevious() {
		synchronized (fSearchSyncObj) {
			if (fSearchThread != null) {
				return;
			}
	    	final ITmfFilterTreeNode filter = (ITmfFilterTreeNode) fTable.getData(Key.SEARCH_OBJ);
	    	if (filter == null) {
	    		return;
	    	}
	    	int selectionIndex = fTable.getSelectionIndex();
			int startIndex;
			if (selectionIndex > 0) {
				startIndex = selectionIndex - 2; // -1 for header row, -1 for previous event
			} else {
				// header row is selected, start at precedent of top event
				startIndex = fTable.getTopIndex() - 2; // -1 for header row, -1 for previous event
			}
			if (fTable.getData(Key.FILTER_OBJ) != null) {
				fSearchThread = new SearchFilteredThread(filter, startIndex - 1, Direction.BACKWARD); // -1 for top filter status row
			} else {
				fSearchThread = new SearchThread(filter, startIndex, Direction.BACKWARD);
			}
			fSearchThread.schedule();
		}
    }
    
    protected void stopSearchThread() {
    	fPendingGotoRank = -1;
		synchronized (fSearchSyncObj) {
			if (fSearchThread != null) {
				fSearchThread.cancel();
				fSearchThread = null;
			}
		}
    }
    
    protected class SearchThread extends Job {
    	protected ITmfFilterTreeNode filter;
    	protected long startRank;
    	protected int direction;
    	protected long rank = -1;
    	protected TmfDataRequest<TmfEvent> request;

    	public SearchThread(ITmfFilterTreeNode filter, int startIndex, int direction) {
    		super(Messages.TmfEventsTable_SearchingJobName);
    		this.filter = filter;
    		this.startRank = startIndex;
    		this.direction = direction;
    	}

    	@SuppressWarnings("unchecked")
    	@Override
    	protected IStatus run(final IProgressMonitor monitor) {
    		final Display display = Display.getDefault();
    		if (startRank < 0) {
    			startRank = fTrace.getNbEvents() - 1;
    		} else if (startRank > fTrace.getNbEvents() - 1) {
    			startRank = 0;
    		}
    		int startIndex = (int) startRank;
    		int nbRequested = (direction == Direction.FORWARD ? Integer.MAX_VALUE : 1);
    		while (!monitor.isCanceled() && rank == -1) {
    			request = new TmfDataRequest<TmfEvent>(TmfEvent.class, startIndex, nbRequested) {
    				@Override
    				public void handleData(TmfEvent event) {
    					super.handleData(event);
    					if (filter.matches(event)) {
    						rank = startRank + getNbRead() - 1;
    						done();
    					}
    				}
    			};
    			((ITmfDataProvider<TmfEvent>) fTrace).sendRequest(request);
    			try {
    				request.waitForCompletion();
    				if (request.isCancelled()) {
    					return Status.OK_STATUS;
    				}
    			} catch (InterruptedException e) {
    				return Status.OK_STATUS;
    			}
    			if (rank == -1) {
    				if (direction == Direction.FORWARD) {
    					if (startIndex == 0) {
    						return Status.OK_STATUS;
    					} else {
    						nbRequested = startIndex;
    						startIndex = 0;
    					}
    				} else {
    					startIndex--;
    					if (startIndex < 0) {
    						startIndex = (int) fTrace.getNbEvents() - 1;
    					}
    					if (startIndex == startRank) {
    						return Status.OK_STATUS;
    					}
    				}
    			}
    		}
    		final int selection = (int) rank + 1; // +1 for header row

    		display.asyncExec(new Runnable() {
    			@Override
    			public void run() {
    				if (monitor.isCanceled()) return;
    				if (fTable.isDisposed()) return;
    				fTable.setSelection(selection);
    				synchronized (fSearchSyncObj) {
    					fSearchThread = null;
    				}
    			}
    		});
    		return Status.OK_STATUS;
    	}

    	@Override
    	protected void canceling() {
    		request.cancel();
    	}
    }

    protected class SearchFilteredThread extends SearchThread {

		public SearchFilteredThread(ITmfFilterTreeNode filter, int startIndex, int direction) {
			super(filter, startIndex, direction);
		}
    	
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			final Display display = Display.getDefault();
			if (fFilteredEventCache.size() == 0) {
				return Status.OK_STATUS;
			}
			if (startRank < 0) {
				startRank = fFilteredEventCache.size() - 1;
			}
			int index = (int) startRank;
			if (direction == Direction.FORWARD) {
    			if (index >= fFilteredEventCache.size()) {
    				index = 0;
    			}
			} else {
				if (index < 0) {
					index = fFilteredEventCache.size() - 1;
				}
			}
			TmfEvent event = null;
			while (!monitor.isCanceled()) {
				event = fFilteredEventCache.get(index).event;
				if (filter.matches(event)) {
					break;
				}
				if (direction == Direction.FORWARD) {
    				index++;
    				if (index == startRank) {
    					return Status.OK_STATUS;
    				}
    				if (index >= fFilteredEventCache.size()) {
    					index = 0;
    				}
				} else {
    				index--;
    				if (index == startRank) {
    					return Status.OK_STATUS;
    				}
    				if (index < 0) {
    					index = fFilteredEventCache.size() - 1;
    				}
				}
			}
			final int selection = (int) index + 2; // +1 for header row, +1 for top filter status row
			
			display.asyncExec(new Runnable() {
				@Override
                public void run() {
					if (monitor.isCanceled()) return;
					if (fTable.isDisposed()) return;
					fTable.setSelection(selection);
					synchronized (fSearchSyncObj) {
						fSearchThread = null;
					}
				}
			});
			return Status.OK_STATUS;
		}
    }
    
    protected void createResources() {
    	fGrayColor = fResourceManager.createColor(ColorUtil.blend(fTable.getBackground().getRGB(), fTable.getForeground().getRGB()));
    	fGreenColor = fTable.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
    	fBoldFont = fResourceManager.createFont(FontDescriptor.createFrom(fTable.getFont()).setStyle(SWT.BOLD));
    }
    
    protected void packColumns() {
        if (fPackDone) return;
        for (TableColumn column : fTable.getColumns()) {
            int headerWidth = column.getWidth();
            column.pack();
            if (column.getWidth() < headerWidth) {
                column.setWidth(headerWidth);
            }
        }
        fPackDone = true;
    }
    
    /**
     * @param event
     * @return
     * 
     * FIXME: Add support for column selection
     */
    protected String[] extractItemFields(TmfEvent event) {
        String[] fields = new String[0];
        if (event != null) {
            fields = new String[] {
                new Long(event.getTimestamp().getValue()).toString(),       
                event.getSource().getSourceId().toString(),
                event.getType().getTypeId().toString(),
                event.getReference().getReference().toString(),
                event.getContent().toString()
            };
        }
        return fields;
    }

    public void setFocus() {
        fTable.setFocus();
    }

    /**
     * @param trace
     * @param disposeOnClose true if the trace should be disposed when the table is disposed
     */
    public void setTrace(ITmfTrace trace, boolean disposeOnClose) {
        if (fTrace != null && fDisposeOnClose) {
            fTrace.dispose();
        }
        fTrace = trace;
	    fPackDone = false;
	    fHeaderState = HeaderState.SEARCH;
	    fSelectedRank = 0;
        fDisposeOnClose = disposeOnClose;
        
        // Perform the updates on the UI thread
        fTable.getDisplay().syncExec(new Runnable() {
            @Override
			public void run() {
                fTable.removeAll();
                fTable.setData(Key.FILTER_OBJ, null);
                fTable.setData(Key.FILTER_TXT, null);
                fTable.setData(Key.SEARCH_OBJ, null);
                fTable.setData(Key.SEARCH_TXT, null);
                fCacheStartIndex = fCacheEndIndex = 0; // Clear the cache
                if (fTrace != null) {
                    if (!fTable.isDisposed() && fTrace != null) {
                        fTable.setItemCount((int) fTrace.getNbEvents() + 1); // +1 for header row
                    }
                    fRawViewer.setTrace(fTrace);
                }
            }
        });
    }

    // ------------------------------------------------------------------------
    // Event cache population
    // ------------------------------------------------------------------------
    
    // The event fetching job
    private Job job;
    private synchronized void populateCache(final int index) {

        /* Check if the current job will fetch the requested event:
         * 1. The job must exist
         * 2. It must be running (i.e. not completed)
         * 3. The requested index must be within the cache range
         * 
         * If the job meets these conditions, we simply exit.
         * Otherwise, we create a new job but we might have to cancel
         * an existing job for an obsolete range.
         */
        if (job != null) {
            if (job.getState() != Job.NONE) {
                if (index >= fCacheStartIndex && index < (fCacheStartIndex + fCacheSize)) {
                    return;
                }
                // The new index is out of the requested range
                // Kill the job and start a new one
                job.cancel();
            }
        }
        
        fCacheStartIndex = index;
        fCacheEndIndex   = index;

        job = new Job("Fetching Events") { //$NON-NLS-1$
            @Override
            @SuppressWarnings("unchecked")
            protected IStatus run(final IProgressMonitor monitor) {

                TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(TmfEvent.class, index, fCacheSize) {
                    private int count = 0;
                    @Override
                    public void handleData(TmfEvent event) {
                        // If the job is canceled, cancel the request so waitForCompletion() will unlock
                        if (monitor.isCanceled()) {
                            cancel();
                            return;
                        }
                        super.handleData(event);
                        if (event != null) {
                            fCache[count++] = event.clone();
                            fCacheEndIndex++;   // TODO: Need to protect this??
                        }
                    }
                };

                ((ITmfDataProvider<TmfEvent>) fTrace).sendRequest(request);
                try {
                    request.waitForCompletion();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Event cache is now updated. Perform update on the UI thread
                if (!fTable.isDisposed() && !monitor.isCanceled()) {
                    fTable.getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            if (!fTable.isDisposed()) {
                                fTable.refresh();
                                packColumns();
                            }
//                            fTrace.seekEvent(fSelectedRank);
                        }
                    });
                }
                
                // Flag the UI thread that the cache is ready
                if (monitor.isCanceled()) {
                	return Status.CANCEL_STATUS;
                } else {
                    return Status.OK_STATUS;
                }
            }
        };
        //job.setSystem(true);
        job.setPriority(Job.SHORT);
        job.schedule();
    }
    
    // ------------------------------------------------------------------------
    // Bookmark handling
    // ------------------------------------------------------------------------
    
	public void addBookmark(IResource resource) {
		fBookmarksResource = resource;
		TableItem[] selection = fTable.getSelection();
		if (selection.length > 0) {
			TableItem tableItem = selection[0];
			if (tableItem.getData(Key.RANK) != null) {
				StringBuffer defaultMessage = new StringBuffer();
				for (int i = 0; i < fTable.getColumns().length; i++) {
					if (i > 0) {
						defaultMessage.append(", "); //$NON-NLS-1$
					}
					defaultMessage.append(tableItem.getText(i));
				}
				InputDialog dialog = new InputDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						Messages.TmfEventsTable_AddBookmarkDialogTitle, Messages.TmfEventsTable_AddBookmarkDialogText, defaultMessage.toString(), null);
				if (dialog.open() == Dialog.OK) {
					String message = dialog.getValue();
    				try {
    	                IMarker bookmark = resource.createMarker(IMarker.BOOKMARK);
    	                if (bookmark.exists()) {
    	                	bookmark.setAttribute(IMarker.MESSAGE, message.toString());
    	                	long rank = (Long) tableItem.getData(Key.RANK);
    	                	int location = (int) rank;
    	                	bookmark.setAttribute(IMarker.LOCATION, (Integer) location);
    	                	fBookmarksMap.put(rank, bookmark.getId());
    	                	fTable.refresh();
    	                }
                    } catch (CoreException e) {
    	                e.printStackTrace();
                    }
				}
			}
		}
		
    }

	public void removeBookmark(IMarker bookmark) {
		for (Entry<Long, Long> entry : fBookmarksMap.entrySet()) {
			if (entry.getValue().equals(bookmark.getId())) {
				fBookmarksMap.remove(entry.getKey());
				fTable.refresh();
				return;
			}
		}
	}

	private void toggleBookmark(long rank) {
		if (fBookmarksResource == null) {
			return;
		}
		if (fBookmarksMap.containsKey(rank)) {
			Long markerId = fBookmarksMap.remove(rank);
			fTable.refresh();
			try {
	            IMarker bookmark = fBookmarksResource.findMarker(markerId);
	            if (bookmark != null) {
	            	bookmark.delete();
	            }
            } catch (CoreException e) {
	            e.printStackTrace();
            }
		} else {
			addBookmark(fBookmarksResource);
		}
	}
	
	public void refreshBookmarks(IResource resource) {
		fBookmarksResource = resource;
		try {
			fBookmarksMap.clear();
	        for (IMarker bookmark : resource.findMarkers(IMarker.BOOKMARK, false, IResource.DEPTH_ZERO)) {
	        	int location = bookmark.getAttribute(IMarker.LOCATION, -1);
	        	if (location != -1) {
	        		long rank = (long) location;
	        		fBookmarksMap.put(rank, bookmark.getId());
	        	}
	        }
	        fTable.refresh();
        } catch (CoreException e) {
	        e.printStackTrace();
        }
    }
	
	@Override
	public void gotoMarker(IMarker marker) {
        int rank = marker.getAttribute(IMarker.LOCATION, -1);
        if (rank != -1) {
        	int index = (int) rank;
        	if (fTable.getData(Key.FILTER_OBJ) != null) {
            	index = getFilteredEventIndex(rank) + 1; //+1 for top filter status row
        	} else if (rank >= fTable.getItemCount()) {
        		fPendingGotoRank = rank;
        	}
        	fTable.setSelection(index + 1); // +1 for header row
        }
	}
	
    // ------------------------------------------------------------------------
    // Listeners
    // ------------------------------------------------------------------------
    
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.ui.views.colors.IColorSettingsListener#colorSettingsChanged(org.eclipse.linuxtools.tmf.ui.views.colors.ColorSetting[])
	 */
	@Override
    public void colorSettingsChanged(ColorSetting[] colorSettings) {
		fTable.refresh();
	}
    
    @Override
    public void addEventsFilterListener(ITmfEventsFilterListener listener) {
    	if (!fEventsFilterListeners.contains(listener)) {
    		fEventsFilterListeners.add(listener);
    	}
    }

	@Override
    public void removeEventsFilterListener(ITmfEventsFilterListener listener) {
		fEventsFilterListeners.remove(listener);
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------
    
	@TmfSignalHandler
    public void experimentUpdated(TmfExperimentUpdatedSignal signal) {
        if ((signal.getExperiment() != fTrace) || fTable.isDisposed()) return;
        // Perform the refresh on the UI thread
		Display.getDefault().asyncExec(new Runnable() {
            @Override
			public void run() {
                if (!fTable.isDisposed() && fTrace != null) {
//                	System.out.println("TmfEventsTable.experimentUpdated() - nbEvents = " + fTrace.getNbEvents());
                	if (fTable.getData(Key.FILTER_OBJ) == null) {
                		fTable.setItemCount((int) fTrace.getNbEvents() + 1); // +1 for header row
                	}
            		if (fPendingGotoRank != -1 && fPendingGotoRank + 1 < fTable.getItemCount()) { // +1 for header row
            			fTable.setSelection((int) fPendingGotoRank + 1); // +1 for header row
            			fPendingGotoRank = -1;
            		}
                }
                if (!fRawViewer.isDisposed() && fTrace != null) {
                    fRawViewer.refreshEventCount();
                }
            }
        });
    }
    
    @TmfSignalHandler
    public void traceUpdated(TmfTraceUpdatedSignal signal) {
        if ((signal.getTrace() != fTrace ) || fTable.isDisposed()) return;
        // Perform the refresh on the UI thread
        Display.getDefault().asyncExec(new Runnable() {
            @Override
			public void run() {
                if (!fTable.isDisposed() && fTrace != null) {
                	if (fTable.getData(Key.FILTER_OBJ) == null) {
                		fTable.setItemCount((int) fTrace.getNbEvents() + 1); // +1 for header row
                		if (fPendingGotoRank != -1 && fPendingGotoRank + 1 < fTable.getItemCount()) { // +1 for header row
                			fTable.setSelection((int) fPendingGotoRank + 1); // +1 for header row
                			fPendingGotoRank = -1;
                		}
                	}
                }
                if (!fRawViewer.isDisposed() && fTrace != null) {
                    fRawViewer.refreshEventCount();
                }
            }
        });
    }

    private boolean fRefreshPending = false;
    @TmfSignalHandler
    public synchronized void rangeSynched(TmfRangeSynchSignal signal) {
        if (!fRefreshPending && !fTable.isDisposed()) {
            // Perform the refresh on the UI thread
            fRefreshPending = true;
            Display.getDefault().asyncExec(new Runnable() {
                @Override
				public void run() {
                    fRefreshPending = false;
                    if (!fTable.isDisposed() && fTrace != null) {
                        fTable.setItemCount((int) fTrace.getNbEvents() + 1); // +1 for header row
                    }
                }
            });
        }
    }
    
    @SuppressWarnings("unchecked")
    @TmfSignalHandler
    public void currentTimeUpdated(final TmfTimeSynchSignal signal) {
    	if ((signal.getSource() != fTable) && (fTrace != null) && (!fTable.isDisposed())) {

    		// Create a request for one event that will be queued after other ongoing requests. When this request is completed 
    		// do the work to select the actual event with the timestamp specified in the signal. This procedure prevents 
    		// the method fTrace.getRank() from interfering and delaying ongoing requests.
    		final TmfDataRequest<TmfEvent> subRequest = new TmfDataRequest<TmfEvent>(TmfEvent.class, 0, 1, ExecutionType.FOREGROUND) {

    			@Override
    			public void handleData(TmfEvent event) {
    				super.handleData(event);
    			}

    			@Override
    			public void handleCompleted() {

    				// Verify if event is within the trace range
    				final TmfTimestamp timestamp[] = new TmfTimestamp[1];
    				timestamp[0] = signal.getCurrentTime();
    				if (timestamp[0].compareTo(fTrace.getStartTime(), true) == -1) {
    					timestamp[0] = fTrace.getStartTime();
    				}
    				if (timestamp[0].compareTo(fTrace.getEndTime(), true) == 1) {
    					timestamp[0] = fTrace.getEndTime();
    				}

    				// Get the rank for the event selection in the table
    				final long rank = fTrace.getRank(timestamp[0]);
    				fSelectedRank = rank;

    				fTable.getDisplay().asyncExec(new Runnable() {
    					@Override
                        public void run() {
    						// Return if table is disposed
    						if (fTable.isDisposed()) return;

                            int index = (int) rank;
                            if (fTable.isDisposed()) return;
                            if (fTable.getData(Key.FILTER_OBJ) != null) {
                            	index = getFilteredEventIndex(rank) + 1; //+1 for top filter status row
                            }
                            fTable.setSelection(index + 1); // +1 for header row
                            fRawViewer.selectAndReveal(rank);
    					}
    				});
    				super.handleCompleted();
    			}
    		};

            ((ITmfDataProvider<TmfEvent>) fTrace).sendRequest(subRequest);
    	}
	}

}