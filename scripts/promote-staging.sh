#!/bin/sh
set -e
: ${GO_DEPENDENCY_LOCATOR_JUMI:?}
: ${STAGING_USERNAME:?}
: ${STAGING_PASSWORD:?}
: ${RELEASE_USERNAME:?}
: ${RELEASE_PASSWORD:?}
set -x

ruby scripts/upload-to-maven-central.rb
