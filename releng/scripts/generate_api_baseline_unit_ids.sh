#!/bin/bash
###############################################################################
# Copyright (c) 2016 Ericsson
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
###############################################################################

# Generates a list of plugins suitable to be put in the API baseline. It skips test plugins, rcp and doc.

find ../.. -name "MANIFEST.MF" | grep -v -E 'doc|rcp|tests' | cut -c 3- | xargs -n1 dirname | xargs -n1 dirname  | sed -rn s/.*\\/\(.*\)/\\1/p | sort | xargs -n1 -I {} echo '<unit id="{}" version="0.0.0"/>'

