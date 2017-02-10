<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />

This file lists the current project committers and assigned maintainers.

To contribute patches to the Trace Compass project, please see the
[contributor guidelines](https://wiki.eclipse.org/Trace_Compass/Contributor_Guidelines).


Current project committers
--------------------------

* Geneviève Bastien <gbastien@versatic.net>
* Francis Giraldeau <francis.giraldeau@gmail.com>
* Bernd Hufmann <bernd.hufmann@ericsson.com>
* Matthew Khouzam <matthew.khouzam@ericsson.com>
* Jean-Christian Kouamé <jean-christian.kouame@ericsson.com>
* Marc-André Laperle <marc-andre.laperle@ericsson.com>
* Alexandre Montplaisir <alexmonthy@efficios.com>
* Patrick Tassé <patrick.tasse@gmail.com>


Maintainers
-----------

Every component, typically a set of related plugins, can have a maintainer and
co-maintainer assigned to them.

Maintainers are expected to review patches posted to Gerrit that
affect the code they are responsible for. If a second review is needed
(external patches, patches from the maintainer itself, etc), the co-maintainer
is the expected reviewer.

Together, the maintainer and co-maintainer should agree on and take the final
decisions as to what happens to the code they are responsible for.


The list below shows the list of components, and the maintainer and
co-maintainer(s) assigned to each one, in that order.

*Consensus* means that no particular maintainer is assigned to this
area of the code by design, and modifications require a consensus
among all committers.

*Open* means that there is nobody specifically maintaining this part
of the code, but the position is available to anyone interested. In the mean
time, the review process for this code is the same as consensus.


    analysis/graph          | Geneviève + Matthew
    analysis/lami           | *Open*
    analysis/os.linux       | Matthew + Geneviève (core) + Patrick (ui)
    analysis/timing         | Matthew + Bernd
    btf                     | Bernd + Matthew
    common                  | *Consensus*
    ctf                     | Matthew + Marc-André
    doc                     | *follows the runtime code*
    gdbtrace                | Patrick + Marc-André
    lttng.control           | Bernd + Marc-André
    lttng.kernel            | *Open*
    lttng.kernel.{vm+graph} | *same os.linux, should move there eventually*
    lttng.ust               | *Open*
    pcap                    | Marc-Andre + Matthew
    rcp                     | Bernd + Marc-André
    releng                  | Marc-André + Bernd
    ss.segmentstore         | Jean-Christian + Geneviève
    ss.statesystem          | Alexandre + Geneviève
    tmf.xml                 | Geneviève + Jean-Christian
    tmf.remote              | Bernd + Patrick
    tmf.core                | *Consensus*, except for:
        analysis            | Geneviève + Matthew
        indexer             | Marc-André + Patrick
        custom parser       | Patrick + Bernd
    tmf.ui                  | Patrick + Bernd

