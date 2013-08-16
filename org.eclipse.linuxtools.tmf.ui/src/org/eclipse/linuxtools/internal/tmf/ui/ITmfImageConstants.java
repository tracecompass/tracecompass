/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernd Hufmann - initial API and implementation
 *     Patrick Tasse - added icons
 *******************************************************************************/
package org.eclipse.linuxtools.internal.tmf.ui;

/**
 * Names for generic icons and buttons used in TMF
 */
@SuppressWarnings({"nls", "javadoc"})
public interface ITmfImageConstants {

    public static final String ICONS_PATH = "icons/"; //$NON-NLS-1$

    /* elcl16 */
    public static final String IMG_UI_HOME_MENU = ICONS_PATH + "elcl16/home_nav.gif";
    public static final String IMG_UI_SELECT_MENU = ICONS_PATH + "elcl16/select_menu.gif";
    public static final String IMG_UI_ZOOM_IN_MENU = ICONS_PATH + "elcl16/zoomin_nav.gif";
    public static final String IMG_UI_ZOOM_OUT_MENU = ICONS_PATH + "elcl16/zoomout_nav.gif";
    public static final String IMG_UI_FILTERS = ICONS_PATH + "elcl16/filter_items.gif";
    public static final String IMG_UI_SEARCH_SEQ = ICONS_PATH + "elcl16/search_seqdiag_menu.gif";
    public static final String IMG_UI_NEXT_PAGE = ICONS_PATH + "elcl16/next_menu.gif";
    public static final String IMG_UI_PREV_PAGE = ICONS_PATH + "elcl16/prev_menu.gif";
    public static final String IMG_UI_GOTO_PAGE = ICONS_PATH + "elcl16/gotopage_menu.gif";
    public static final String IMG_UI_NODE_START = ICONS_PATH + "elcl16/node_end.gif";
    public static final String IMG_UI_NODE_END = ICONS_PATH + "elcl16/node_start.gif";
    public static final String IMG_UI_SEARCH_MATCH = ICONS_PATH + "elcl16/search_match.gif";
    public static final String IMG_UI_FIRST_PAGE = ICONS_PATH + "elcl16/backward_nav.gif";
    public static final String IMG_UI_LAST_PAGE = ICONS_PATH + "elcl16/forward_nav.gif";
    public static final String IMG_UI_SHOW_LEGEND = ICONS_PATH + "elcl16/show_legend.gif";
    public static final String IMG_UI_NEXT_EVENT = ICONS_PATH + "elcl16/next_event.gif";
    public static final String IMG_UI_PREV_EVENT = ICONS_PATH + "elcl16/prev_event.gif";
//    public static final String IMG_UI_NEXT_ITEM = ICONS_PATH + "elcl16/next_item.gif";
//    public static final String IMG_UI_PREV_ITEM = ICONS_PATH + "elcl16/prev_item.gif";
    public static final String IMG_UI_NEXT_ITEM = IMG_UI_NEXT_PAGE;
    public static final String IMG_UI_PREV_ITEM = IMG_UI_PREV_PAGE;
    public static final String IMG_UI_PIN_VIEW = ICONS_PATH + "elcl16/pin_view.gif";
    public static final String IMG_UI_HIDE_ARROWS = ICONS_PATH + "elcl16/hide_arrows.gif";
    public static final String IMG_UI_FOLLOW_ARROW_FORWARD = ICONS_PATH + "elcl16/follow_arrow_fwd.gif";
    public static final String IMG_UI_FOLLOW_ARROW_BACKWARD = ICONS_PATH + "elcl16/follow_arrow_bwd.gif";

    /* eview16 */
    public static final String IMG_UI_SEQ_DIAGRAM_OBJ = ICONS_PATH + "eview16/sequencediagram_view.gif";

    /* obj16 */
    public static final String IMG_UI_ZOOM = ICONS_PATH + "obj16/zoom_mask.bmp";
    public static final String IMG_UI_ZOOM_IN = ICONS_PATH + "obj16/zoomin_obj.bmp";
    public static final String IMG_UI_ZOOM_OUT = ICONS_PATH + "obj16/zoomout_obj.bmp";
    public static final String IMG_UI_ARROW_COLLAPSE_OBJ = ICONS_PATH + "obj16/arrow_colapse.bmp";
    public static final String IMG_UI_ARROW_UP_OBJ = ICONS_PATH + "obj16/arrow_up.bmp";

    /* wizban */
    public static final String IMG_UI_CONFLICT = ICONS_PATH + "wizban/conflict_stat.gif";
}