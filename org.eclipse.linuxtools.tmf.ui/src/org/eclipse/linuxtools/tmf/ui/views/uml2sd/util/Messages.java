/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation, Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 *     Alexandre Montplaisir - Renamed variables
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd.util;

import org.eclipse.osgi.util.NLS;

/**
 * Messages related to the sequence diagram
 *
 * @version 1.0
 * @author Bernd Hufmann
 * @since 2.0
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.messages"; //$NON-NLS-1$

    private Messages() {
        // Do not instantiate
    }

    public static String SequenceDiagram_LifelineNode;
    public static String SequenceDiagram_MessageNode;
    public static String SequenceDiagram_LostMessageNode;
    public static String SequenceDiagram_FoundMessageNode;
    public static String SequenceDiagram_ExecutionOccurrenceWithParams;

    public static String SequenceDiagram_Find;
    public static String SequenceDiagram_Close;
    public static String SequenceDiagram_StringNotFound;
    public static String SequenceDiagram_SequenceDiagramFind;
    public static String SequenceDiagram_SearchFor;
    public static String SequenceDiagram_MatchingString;
    public static String SequenceDiagram_CaseSensitive;
    public static String SequenceDiagram_Lifeline;
    public static String SequenceDiagram_Stop;
    public static String SequenceDiagram_SynchronousMessage;
    public static String SequenceDiagram_SynchronousMessageReturn;
    public static String SequenceDiagram_AsynchronousMessage;
    public static String SequenceDiagram_AsynchronousMessageReturn;
    public static String SequenceDiagram_or;
    public static String SequenceDiagram_PreviousPage;
    public static String SequenceDiagram_NextPage;
    public static String SequenceDiagram_GoToPreviousPage;
    public static String SequenceDiagram_GoToNextPage;
    public static String SequenceDiagram_GoToMessage;
    public static String SequenceDiagram_GoToMessageReturn;
    public static String SequenceDiagram_EditFilters;
    public static String SequenceDiagram_HidePatterns;
    public static String SequenceDiagram_Pages;

    public static String SequenceDiagram_ZoomIn;
    public static String SequenceDiagram_ZoomInTheDiagram;
    public static String SequenceDiagram_ResetZoomFactor;
    public static String SequenceDiagram_ZoomOut;
    public static String SequenceDiagram_ZoomOutTheDiagram;
    public static String SequenceDiagram_Select;
    public static String SequenceDiagram_Max;
    public static String SequenceDiagram_Min;
    public static String SequenceDiagram_ListOfHideDisplayPatterns;
    public static String SequenceDiagram_hide;
    public static String SequenceDiagram_display;
    public static String SequenceDiagram_EditIt;
    public static String SequenceDiagram_Add;
    public static String SequenceDiagram_Create;
    public static String SequenceDiagram_Update;
    public static String SequenceDiagram_Remove;
    public static String SequenceDiagram_SequenceDiagramHidePatterns;
    public static String SequenceDiagram_DefinitionOfHidePattern;
    public static String SequenceDiagram_PageNavigation;
    public static String SequenceDiagram_SequenceDiagramPages;
    public static String SequenceDiagram_IsInBetween;
    public static String SequenceDiagram_Total;
    public static String SequenceDiagram_pages;
    public static String SequenceDiagram_page;

    public static String SequenceDiagram_CurrentPage;

    public static String SequenceDiagram_Navigation;
    public static String SequenceDiagram_OpenOverviewTooltip;

    public static String SequenceDiagram_LifelineWidth;
    public static String SequenceDiagram_AaBbYyZz;
    public static String SequenceDiagram_IncreaseFontSizeWhenZooming;
    public static String SequenceDiagram_ExcludeExternalTime;
    public static String SequenceDiagram_UseGradientColor;
    public static String SequenceDiagram_Background;
    public static String SequenceDiagram_Lines;
    public static String SequenceDiagram_Text;

    public static String SequenceDiagram_ExecutionOccurrence;
    public static String SequenceDiagram_SyncMessage;
    public static String SequenceDiagram_SyncMessageReturn;
    public static String SequenceDiagram_AsyncMessage;
    public static String SequenceDiagram_AsyncMessageReturn;
    public static String SequenceDiagram_Frame;
    public static String SequenceDiagram_LifelineHeader;
    public static String SequenceDiagram_FrameTitle;
    public static String SequenceDiagram_ShowTooltips;
    public static String SequenceDiagram_Error;
    public static String SequenceDiagram_InvalidRange;
    public static String SequenceDiagram_InvalidNbVertical;
    public static String SequenceDiagram_InvalidNbHorizontal;
    public static String SequenceDiagram_NoPageSelected;
    public static String SequenceDiagram_FromPage;

    public static String SequenceDiagram_to;
    public static String SequenceDiagram_SelectedPages;
    public static String SequenceDiagram_CurrentView;
    public static String SequenceDiagram_AllPages;
    public static String TotalNumberOfPages;
    public static String SequenceDiagram_NumberOfHorizontalPages;
    public static String SequenceDiagram_NumberOfVerticalPages;
    public static String SequenceDiagram_UseCurrentZoom;
    public static String SequenceDiagram_ZoomOption;
    public static String SequenceDiagram_Print;
    public static String SequenceDiagram_Printer;
    public static String SequenceDiagram_plus;
    public static String SequenceDiagram_Page;
    public static String SequenceDiagram_PrintRange;
    public static String SequenceDiagram_Preview;

    public static String SequenceDiagram_TimeCompressionBarConfig;
    public static String SequenceDiagram_MinTime;
    public static String SequenceDiagram_MaxTime;
    public static String SequenceDiagram_Default;

    public static String SequenceDiagram_NoPrinterSelected;
    public static String SequenceDiagram_Scale;
    public static String SequenceDiagram_Precision;
    public static String SequenceDiagram_Delta;

    public static String SequenceDiagram_FirstPage;
    public static String SequenceDiagram_GoToFirstPage;
    public static String SequenceDiagram_LastPage;
    public static String SequenceDiagram_GoToLastPage;

    public static String SequenceDiagram_ShowNodeEnd;
    public static String SequenceDiagram_ShowNodeStart;
    public static String SequenceDiagram_ConfigureMinMax;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}