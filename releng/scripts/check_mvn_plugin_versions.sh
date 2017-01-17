#!/bin/bash
###############################################################################
# Copyright (c) 2015 Ericsson, EfficiOS Inc. and others
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Marc-André Laperle - Initial version
#     Alexandre Montplaisir - Initial version
###############################################################################

# A script that checks for outdated Maven plugins being used by the
# build. The build specifies exact version of Maven plugins, for
# stability and reproducibility reasons. When a new plugin is
# available, the version has to be manually updated and the build has
# to be tested.
#
# What should be tested? Depending on the plugins that are updated:
#  - Are the update sites OK? Categories, content, etc. (Tycho, JBoss)
#  - Is the product OK? Starts, splash screen, etc. (Tycho)
#  - Is code coverage still working? (Jacoco)
#
# The script simply formats the output of
# 'mvn versions:display-plugin-updates' in a nicer summary.
#
# Usage  ./check_mvn_plugin_versions.sh

# Point ourselves to the script's directory (so it can be run "out-of-tree")
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
output=`mvn versions:display-plugin-updates -U -f $DIR/../../pom.xml`

#filter only updates and show unique
summary=`echo "${output}" | grep "\\->" | sort | uniq`
echo -e "Summary:\n${summary}"

#remove empty lines and count lines
outdatedNb=`echo "${summary}" | sed '/^\s*$/d' | wc -l`
echo Number of outdated plugins: "${outdatedNb}"
exit ${outdatedNb}
