#!/bin/bash
###############################################################################
# Copyright (c) 2016 Ericsson
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
###############################################################################

# Generates a list of plugins suitable to be put in the API baseline.
# It skips test plugins, rcp and doc.
#
# Usage  ./generate_api_baseline_unit_ids.sh

find ../.. -name "MANIFEST.MF" | grep -v -E 'doc|rcp|tests|/target/' | cut -c 3- | xargs -n1 dirname | xargs -n1 dirname  | sed -rn s/.*\\/\(.*\)/\\1/p | sort | xargs -n1 -I {} echo '<unit id="{}" version="0.0.0"/>'

