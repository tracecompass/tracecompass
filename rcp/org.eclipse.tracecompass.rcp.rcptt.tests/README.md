# tracecompass-rcptt-test

Blackbox UI test using RCPTT for Trace Compass

## How to run ?

You can run this project either via maven or via the RCP Testing Tool.

### Local maven run

`mvn clean install -Drun-rcptt=true`
or, with skipping of other unit tests:
`mvn clean install -Drun-rcptt=true -Dskip-short-tc-ui-tests=true -Dskip-long-tc-ui-tests=true -Dskip-tc-core-tests=true`

Results will be located under `./target/results`.

By default maven will use the RCP built in `../rcp/org.eclipse.tracecompass.rcp.product`.
Therefore, if you only want to execute the tests in this folder and not the rest of the build,
you need to make sure that the product has been built first (`mvn clean install -Dmaven.test.skip=true` in the git repository root folder).

Test traces are being made available in a path defined as the `dataPath` system property. The test traces are being downloaded as part of the maven build and extracted in `./target/dependency/tracecompass-test-traces-ctf-jar/`.

### Via RCPTT

In order to have the RCP and test traces, it is better to run with Maven first.
Once this is done, the project (this directory) can be imported in RCPTT.

To run the tests, open `test_suite/open_trace_suite` then click Execute at the top-right corner

A predefined launch is provided to ease with launching the tests.
This launch sets up the AUT path and dataPath (test traces).

If you are not using the default paths make sure to pass the correct arguments to
the AUT in the run configurations.

![](http://i.imgur.com/J4ohsPE.png)