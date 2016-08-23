###############################################################################
# Copyright (c) 2016 Ericsson
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
###############################################################################

oldVersion=$1
newVersion=$2

if [ -z "$oldVersion" -o -z "$newVersion" ]; then
	echo "usage: $0 oldversion newversion"
	exit 1
fi

echo Changing $oldVersion to $newVersion

#Update root pom version
find ../.. -maxdepth 1 -name "pom.xml" -exec python update_root_pom_versions.py {} $oldVersion $newVersion \;
#Update pom.xml with <parent> tag with the new version of the root pom
find ../.. -name "pom.xml" -type f -exec python update_parent_pom_versions.py {} $oldVersion $newVersion \;
#Update doc plugin versions
find ../../doc -name "MANIFEST.MF" -exec sed -i -e s/$oldVersion.qualifier/$newVersion.qualifier/g {} \;

#Update feature versions (feature.xml)
find ../.. -name "feature.xml" -exec sed -i -e s/$oldVersion.qualifier/$newVersion.qualifier/g {} \;

#Update branding plugin manifest.MF
sed -i -e s/$oldVersion.qualifier/$newVersion.qualifier/g ../../rcp/org.eclipse.tracecompass.rcp.branding/META-INF/MANIFEST.MF
#rcp/org.eclipse.tracecompass.rcp.branding/plugin.xml aboutText
sed -i -e s/$oldVersion/$newVersion/g ../../rcp/org.eclipse.tracecompass.rcp.branding/plugin.xml

#Update .product rcp/org.eclipse.tracecompass.rcp.product/tracing.product
sed -i -e s/$oldVersion/$newVersion/g ../../rcp/org.eclipse.tracecompass.rcp.product/tracing.product

#Update rcp.ui plugin rcp/org.eclipse.tracecompass.rcp.ui/META-INF/MANIFEST.MF
sed -i -e s/$oldVersion.qualifier/$newVersion.qualifier/g ../../rcp/org.eclipse.tracecompass.rcp.ui/META-INF/MANIFEST.MF
