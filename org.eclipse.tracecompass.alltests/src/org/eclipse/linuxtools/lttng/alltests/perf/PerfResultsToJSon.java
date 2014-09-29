/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.alltests.perf;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.linuxtools.lttng.alltests.Activator;
import org.eclipse.test.internal.performance.PerformanceTestPlugin;
import org.eclipse.test.internal.performance.data.Dim;
import org.eclipse.test.internal.performance.db.DB;
import org.eclipse.test.internal.performance.db.Scenario;
import org.eclipse.test.internal.performance.db.SummaryEntry;
import org.eclipse.test.internal.performance.db.TimeSeries;
import org.eclipse.test.internal.performance.db.Variations;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

/**
 * Convert results from the database to JSON suitable for display.
 *
 * Normal charts:
 *
 * Individual charts are generated into JSON files in the form chart#.json where
 * # is incremented for each new chart. A chart contains data points consisting
 * of X and Y values suitable for a line chart. Each point can also have
 * additional data, for example the commit id. This format is compatible with
 * nvd3. For example:
 *
 * <pre>
 * <code>
 * [{
 *   "key": "Experiment Benchmark:84 traces",
 *   "values": [{
 *       "label": {"commit": "fe3c142"},
 *       "x": 1405024320000,
 *       "y": 17592
 *   }]
 * }]
 * </code>
 * </pre>
 *
 * Normal charts metadata:
 *
 * Each chart has an entry in the metada.js file which organizes the charts per
 * component and contains additional information to augment the format expected
 * by nvd3. Each entry contains the combination of OS and JVM, the filename (in
 * JSON format), the title of the chart, the unit (seconds, etc) and the
 * dimension (CPU time, used heap, etc).
 *
 * <pre>
 *  <code>
 *  var MetaData = {
 *     "applicationComponents": {
 *         "Experiment benchmark": {
 *             "name": "Experiment benchmark",
 *             "tests": [
 *                 {
 *                     "dimension": "CPU Time",
 *                     "file": "chart12",
 *                     "jvm": "1.7",
 *                     "os": "linux",
 *                     "title": "Experiment Benchmark:84 traces",
 *                     "unit": "s"
 *                 },
 *                 {
 *                     "dimension": "CPU Time",
 *                     "file": "chart11",
 *                     "jvm": "1.7",
 *                     "os": "linux",
 *                     "title": "Experiment Benchmark:6 traces",
 *                     "unit": "s"
 *                 },
 * ...
 *  </code>
 * </pre>
 *
 * Overview charts:
 *
 * In addition to the normal charts, overview charts are generated. An overview
 * chart presents a summary of the scenarios ran for a given OS and JVM
 * combination. Only scenarios marked as "global" are added to the overview
 * because of space concerns. Overview charts are generated under the
 * chart_overview#.json name and look similar in structure to the normal charts
 * except that they contain more than one series.
 *
 * <pre>
 *   <code>
 * [
 *   {
 *       "key": "CTF Read & Seek Benchmark (500 seeks):tr",
 *       "values": [
 *           {
 *               "label": {"commit": "4d34345"},
 *               "x": 1405436820000,
 *               "y": 5382.5
 *           },
 *           ...
 *       ]
 *   },
 *   {
 *       "key": "CTF Read Benchmark:trace-kernel",
 *       "values": [
 *           {
 *               "label": {"commit": "4d34345"},
 *               "x": 1405436820000,
 *               "y": 1311.5
 *           },
 *           ...
 *       ]
 *   },
 *   ...
 *   </code>
 * </pre>
 *
 * Overview charts metadata:
 *
 * Overview charts also have similar metadata entries to normal charts except
 * they are not organized by component.
 *
 * <pre>
 *   <code>
 * var MetaData = {
 * ...
 *   "overviews": {
 *       "1": {
 *           "dimension": "",
 *           "file": "chart_overview0",
 *           "jvm": "1.7",
 *           "os": "linux",
 *           "title": "linux / 1.7",
 *           "unit": ""
 *       },
 *       "2": {
 *           "dimension": "",
 *           "file": "chart_overview1",
 *           "jvm": "1.7",
 *           "os": "windows",
 *           "title": "windows / 1.7",
 *           "unit": ""
 *       },
 *       ...
 *   </code>
 * </pre>
 *
 * Finally, since we want to be able to filter all the charts by OS/JVM
 * combination, there is a section in the metadata that lists all the
 * combinations:
 *
 * <pre>
 *   <code>
 *     "osjvm": {
 *       "1": {
 *           "description": "linux / 1.7",
 *           "jvm": "1.7",
 *           "os": "linux"
 *       },
 *       "2": {
 *           "description": "windows / 1.7",
 *           "jvm": "1.7",
 *           "os": "windows"
 *       },
 *       "3": {
 *           "description": "mac / 1.7",
 *           "jvm": "1.7",
 *           "os": "mac"
 *       }
 *   },
 *   </code>
 * </pre>
 *
 * All of this data is meant to be view on a website. Specifically, the source
 * code for our implementation is available on GitHub at
 * https://github.com/PSRCode/ITCFYWebsite
 *
 * It makes use of the NVD3 project to display the charts based on the data
 * generated by this class.
 */
public class PerfResultsToJSon {

    /*
     * Labels
     */
    private static final String APPLICATION_COMPONENTS_LABEL = "applicationComponents";
    private static final String BUILD_LABEL = "build";
    private static final String COMMIT_LABEL = "commit";
    private static final String CONFIG_LABEL = "config";
    private static final String DESCRIPTION_LABEL = "description";
    private static final String DIMENSION_LABEL = "dimension";
    private static final String FILE_LABEL = "file";
    private static final String HOST_LABEL = "host";
    private static final String JVM_LABEL = "jvm";
    private static final String KEY_LABEL = "key";
    private static final String LABEL_LABEL = "label";
    private static final String NAME_LABEL = "name";
    private static final String OS_LABEL = "os";
    private static final String OSJVM_LABEL = "osjvm";
    private static final String OVERVIEWS_LABEL = "overviews";
    private static final String TESTS_LABEL = "tests";
    private static final String TITLE_LABEL = "title";
    private static final String UNIT_LABEL = "unit";
    private static final String VALUES_LABEL = "values";
    private static final String X_LABEL = "x";
    private static final String Y_LABEL = "y";

    private static final String BUILD_DATE_FORMAT = "yyyyMMdd-HHmm";
    private static final String OVERVIEW_CHART_FILE_NAME = "chart_overview";
    private static final String METADATA_FILE_NAME = "meta";
    private static final String METADATA_FILE_NAME_EXTENSION = ".js";
    private static final String CHART_FILE_NAME = "chart";
    private static final String CHART_FILE_NAME_EXTENSION = ".json";
    private static final String WILDCARD_PATTERN = "%";
    private static final String COMPONENT_SEPARATOR = "#";
    private static final String META_DATA_JAVASCRIPT_START = "var MetaData = ";

    private static Pattern BUILD_DATE_PATTERN = Pattern.compile("(\\w+-\\w+)(-\\w+)?");
    private static Pattern COMMIT_PATTERN = Pattern.compile(".*-.*-(.*)");

    private JSONObject fApplicationComponents = new JSONObject();
    private JSONObject fOverviews = new JSONObject();

    private int fNumChart = 0;
    private int fNumOverviewChart = 0;

    /**
     * Convert results from the database to JSON suitable for display
     *
     * <pre>
     * For each variant (os/jvm combination)
     *    - For each summary entry (scenario)
     *      - Generate a chart
     *      - Add it to global summary (if needed)
     *      - Create the metadata for this test
     *    - Create an overview chart for this os/jvm
     * </pre>
     *
     * @throws JSONException
     *             JSON error
     * @throws IOException
     *             IO error
     */
    @Test
    public void parseResults() throws JSONException, IOException {
        Variations configVariations = PerformanceTestPlugin.getVariations();
        JSONObject osJvmVariants = createOsJvm();

        Iterator<?> keysIt = osJvmVariants.keys();
        while (keysIt.hasNext()) {
            JSONArray overviewSummarySeries = new JSONArray();

            JSONObject variant = osJvmVariants.getJSONObject((String) keysIt.next());
            String seriesKey = PerformanceTestPlugin.BUILD;

            // Clone the variations from the environment because it might have
            // extra parameters like host=, etc.
            Variations buildVariations = (Variations) configVariations.clone();
            buildVariations.setProperty(JVM_LABEL, variant.getString(JVM_LABEL));
            buildVariations.setProperty(CONFIG_LABEL, variant.getString(OS_LABEL));
            buildVariations.setProperty(BUILD_LABEL, WILDCARD_PATTERN);

            Scenario[] scenarios = DB.queryScenarios(buildVariations, WILDCARD_PATTERN, seriesKey, null);
            SummaryEntry[] summaryEntries = DB.querySummaries(buildVariations, WILDCARD_PATTERN);
            for (SummaryEntry entry : summaryEntries) {
                Scenario scenario = getScenario(entry.scenarioName, scenarios);
                JSONObject scenarioSeries = createScenarioChart(scenario, entry, buildVariations);
                // Add to global summary
                if (scenarioSeries != null && entry.isGlobal) {
                    overviewSummarySeries.put(scenarioSeries);
                }
            }

            JSONObject overviewMetadata = createOverviewChart(overviewSummarySeries, buildVariations);
            fOverviews.put(Integer.toString(fNumOverviewChart), overviewMetadata);
        }

        // Create the matadata javascript file that includes OS/JVM combinations
        // (for filtering), application components and overviews (one of OS/JVM
        // combination)
        JSONObject rootMetadata = new JSONObject();
        rootMetadata.put(OSJVM_LABEL, osJvmVariants);
        rootMetadata.put(APPLICATION_COMPONENTS_LABEL, fApplicationComponents);
        rootMetadata.put(OVERVIEWS_LABEL, fOverviews);
        try (FileWriter fw1 = new FileWriter(METADATA_FILE_NAME + METADATA_FILE_NAME_EXTENSION)) {
            fw1.write(META_DATA_JAVASCRIPT_START + rootMetadata.toString(4));
        }
    }

    /**
     * Create chart for a scenario instance and add it to the relevant metadatas
     *
     * @param scenario
     *            the scenario. For example,
     *            "CTF Read & Seek Benchmark (500 seeks)".
     * @param entry
     *            an entry from the summary. Only scenarios that are part of the
     *            summary are processed.
     * @param variations
     *            all variations to consider to create the scenario chart. For
     *            example build=%;jvm=1.7;config=linux will generate a chart for
     *            all builds on Linux / JVM 1.7
     *
     * @return
     * @throws JSONException
     *             JSON error
     * @throws IOException
     *             IO error
     */
    private JSONObject createScenarioChart(Scenario scenario, SummaryEntry entry, Variations variations) throws JSONException, IOException {
        if (scenario == null) {
            return null;
        }
        String[] split = entry.scenarioName.split(COMPONENT_SEPARATOR);
        if (split.length < 3) {
            Activator.logError("Invalid scenario name \"" + entry.scenarioName + "\", it must be in format: org.package.foo#component#test");
            return null;
        }

        // Generate individual chart
        JSONArray rootScenario = new JSONArray();
        JSONObject series = createSerie(scenario, variations, entry.shortName, entry.dimension);
        rootScenario.put(series);
        int numChart = fNumChart++;
        try (FileWriter fw = new FileWriter(CHART_FILE_NAME + numChart + CHART_FILE_NAME_EXTENSION)) {
            fw.write(rootScenario.toString(4));
        }

        // Create the metadata
        JSONObject testMetadata = new JSONObject();
        testMetadata.put(TITLE_LABEL, entry.shortName);
        testMetadata.put(FILE_LABEL, CHART_FILE_NAME + numChart);
        testMetadata.put(OS_LABEL, variations.getProperty(CONFIG_LABEL));
        testMetadata.put(JVM_LABEL, variations.getProperty(JVM_LABEL));
        testMetadata.put(DIMENSION_LABEL, entry.dimension.getName());
        testMetadata.put(UNIT_LABEL, entry.dimension.getUnit().getShortName());

        // Add the scenario to the metadata, under the correct component
        String componentName = split[1];
        JSONObject componentObject = null;
        if (fApplicationComponents.has(componentName)) {
            componentObject = fApplicationComponents.getJSONObject(componentName);
        } else {
            componentObject = new JSONObject();
            componentObject.put(NAME_LABEL, componentName);
            componentObject.put(TESTS_LABEL, new JSONArray());
            fApplicationComponents.put(componentName, componentObject);
        }
        JSONArray tests = componentObject.getJSONArray(TESTS_LABEL);
        tests.put(testMetadata);

        return series;
    }

    /**
     * Create an overview chart for this OS / JVM combination. The chart is made
     * of multiple series (scenarios) that were marked as global.
     *
     * @param overviewSummarySeries
     *            an array of series to include in the chart (multiple
     *            scenarios)
     * @param variations
     *            the variations used to generate the series to be included in
     *            this overview chart. For example build=%;jvm=1.7;config=linux
     *            will generate an overview chart for Linux / JVM 1.7
     * @return the overview metadata JSON object
     * @throws JSONException
     *             JSON error
     * @throws IOException
     *             io error
     */
    private JSONObject createOverviewChart(JSONArray overviewSummarySeries, Variations variations) throws IOException, JSONException {
        int numOverviewChart = fNumOverviewChart++;
        try (FileWriter fw = new FileWriter(OVERVIEW_CHART_FILE_NAME + numOverviewChart + CHART_FILE_NAME_EXTENSION)) {
            fw.write(overviewSummarySeries.toString(4));
        }

        String os = variations.getProperty(CONFIG_LABEL);
        String jvm = variations.getProperty(JVM_LABEL);

        // Create the overview metadata
        JSONObject overviewMetadata = new JSONObject();
        overviewMetadata.put(TITLE_LABEL, os + " / " + jvm);
        overviewMetadata.put(FILE_LABEL, OVERVIEW_CHART_FILE_NAME + numOverviewChart);
        overviewMetadata.put(OS_LABEL, os);
        overviewMetadata.put(JVM_LABEL, jvm);
        overviewMetadata.put(DIMENSION_LABEL, "");
        overviewMetadata.put(UNIT_LABEL, "");

        return overviewMetadata;
    }

    private static Scenario getScenario(String scenarioName, Scenario[] scenarios) {
        for (int i = 0; i < scenarios.length; i++) {
            Scenario s = scenarios[i];
            if (s.getScenarioName().equals(scenarioName)) {
                return s;
            }

        }
        return null;
    }

    /**
     * Get all combinations of OS / JVM. This will be used for filtering.
     *
     * @return the JSON object containing all the combinations
     * @throws JSONException
     *             JSON error
     */
    private static JSONObject createOsJvm() throws JSONException {
        JSONObject osjvm = new JSONObject();
        List<String> oses = getDistinctOses();

        int osJvmIndex = 1;
        for (String os : oses) {
            String key = JVM_LABEL;
            Variations v = new Variations();

            v.setProperty(BUILD_LABEL, WILDCARD_PATTERN);
            v.setProperty(HOST_LABEL, WILDCARD_PATTERN);
            v.setProperty(CONFIG_LABEL, os);
            v.setProperty(JVM_LABEL, WILDCARD_PATTERN);

            List<String> jvms = new ArrayList<>();
            DB.queryDistinctValues(jvms, key, v, WILDCARD_PATTERN);
            for (String jvm : jvms) {
                JSONObject osjvmItem = new JSONObject();
                osjvmItem.put(OS_LABEL, os);
                osjvmItem.put(JVM_LABEL, jvm);
                osjvmItem.put(DESCRIPTION_LABEL, os + " / " + jvm);
                osjvm.put(Integer.toString(osJvmIndex), osjvmItem);
                osJvmIndex++;
            }
        }

        return osjvm;
    }

    /**
     * Get all the distinct OS values
     *
     * @return the distinct OS values
     */
    private static List<String> getDistinctOses() {
        List<String> configs = new ArrayList<>();
        String key = PerformanceTestPlugin.CONFIG;
        Variations v = new Variations();
        v.setProperty(WILDCARD_PATTERN, WILDCARD_PATTERN);
        DB.queryDistinctValues(configs, key, v, WILDCARD_PATTERN);
        return configs;
    }

    /**
     * This main can be run from within Eclipse provided everything is on the
     * class path.
     *
     * @param args
     *            the arguments
     * @throws JSONException
     *             JSON error
     * @throws IOException
     *             io error
     */
    public static void main(String[] args) throws JSONException, IOException {
        new PerfResultsToJSon().parseResults();
    }

    /**
     * Create a series of data points for a given scenario through variations
     *
     * @param scenario
     *            the scenario. For example,
     *            "CTF Read & Seek Benchmark (500 seeks)".
     * @param variations
     *            all variations to consider to create the series. For example
     *            build=%;jvm=1.7;config=linux will generate the series for all
     *            builds on Linux / JVM 1.7
     * @param shortName
     *            the short name of the scenario
     * @param dimension
     *            the dimension of interest (CPU time, used java heap, etc).
     * @return the generated JSON object representing a series of data points
     *         for this scenario
     * @throws JSONException
     */
    private static JSONObject createSerie(Scenario scenario, Variations variations, String shortName, Dim dimension) throws JSONException {
        JSONObject o = new JSONObject();
        o.putOpt(KEY_LABEL, shortName);
        o.putOpt(VALUES_LABEL, createDataPoints(scenario, variations, dimension));
        return o;
    }

    /**
     * Create data points for a given scenario and variations.
     *
     * @param s
     *            the scenario. For example,
     *            "CTF Read & Seek Benchmark (500 seeks)".
     * @param variations
     *            all variations to consider to create the data points. For
     *            example build=%;jvm=1.7;config=linux will generate the data
     *            points for all builds on Linux / JVM 1.7
     * @param dimension
     *            the dimension of interest (CPU time, used java heap, etc).
     *
     * @return the generated JSON array of points
     * @throws JSONException
     *             JSON error
     */
    private static JSONArray createDataPoints(Scenario s, Variations variations, Dim dimension) throws JSONException {
        // Can be uncommented to see raw dump
        //s.dump(System.out, PerformanceTestPlugin.BUILD);

        String[] builds = DB.querySeriesValues(s.getScenarioName(), variations, PerformanceTestPlugin.BUILD);
        Date[] dates = new Date[builds.length];
        String[] commits = new String[builds.length];
        for (int i = 0; i < builds.length; i++) {
            dates[i] = parseBuildDate(builds[i]);
            commits[i] = parseCommit(builds[i]);
        }

        TimeSeries timeSeries = s.getTimeSeries(dimension);
        JSONArray dataPoints = new JSONArray();
        int length = timeSeries.getLength();
        for (int i = 0; i < length; i++) {
            JSONObject point = new JSONObject();
            if (dates[i] == null) {
                continue;
            }
            point.put(X_LABEL, dates[i].getTime());
            double value = 0;
            if (timeSeries.getCount(i) > 0) {
                value = timeSeries.getValue(i);
                if (Double.isNaN(value)) {
                    value = 0;
                }
            }
            point.put(Y_LABEL, value);
            dataPoints.put(point);
            point.put(LABEL_LABEL, createLabel(commits[i]));
        }
        return dataPoints;
    }

    /**
     * Create a label JSONObject which is used to attach more information to a
     * data point.
     *
     * @param commit
     *            the commit id for this data point
     * @return the resulting JSON object
     * @throws JSONException
     *             JSON error
     */
    private static JSONObject createLabel(String commit) throws JSONException {
        /*
         * Here we could add more information about this specific data point
         * like the commit author, the commit message, etc.
         */
        JSONObject label = new JSONObject();
        if (commit != null && !commit.isEmpty()) {
            label.put(COMMIT_LABEL, commit);
        }
        return label;
    }

    /**
     * Get the commit id out of the build= string
     *
     * @param build
     *            the build string
     * @return the parsed commit id
     */
    private static String parseCommit(String build) {
        Matcher matcher = COMMIT_PATTERN.matcher(build);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Get the Date out of the build= string
     *
     * @param build
     *            the build string
     * @return the parsed Date
     */
    private static Date parseBuildDate(String build) {
        Matcher matcher = BUILD_DATE_PATTERN.matcher(build);
        Date date = null;
        if (matcher.matches()) {
            String dateStr = matcher.group(1);
            SimpleDateFormat f = new SimpleDateFormat(BUILD_DATE_FORMAT);
            try {
                date = dateStr.length() > BUILD_DATE_FORMAT.length() ?
                        f.parse(dateStr.substring(dateStr.length() - BUILD_DATE_FORMAT.length())) :
                        f.parse(dateStr);
            } catch (ParseException e) {
                return null;
            }
        }
        return date;
    }
}
