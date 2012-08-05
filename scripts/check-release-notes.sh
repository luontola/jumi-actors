#!/bin/sh
set -eu
set -x

RELEASE_NOTES=`cat build/release-notes`

# Require the release notes to contain something else than just the TBD placeholder
if echo "$RELEASE_NOTES" | grep --line-regexp --quiet "\- TBD"
then
    echo "Release notes not filled in"
    exit 1
fi
