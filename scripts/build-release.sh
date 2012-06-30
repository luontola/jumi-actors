#!/bin/sh
set -e
: ${GO_PIPELINE_COUNTER:?}
: ${GPG_KEYNAME:?}
: ${GPG_PASSPHRASE:?}
: ${PWD:?}
set -x

ruby scripts/update-to-release-version.rb

mvn clean deploy \
    --batch-mode \
    --errors \
    -Psonatype-oss-release \
    -Dgpg.keyname="$GPG_KEYNAME" \
    -Dgpg.passphrase="$GPG_PASSPHRASE" \
    -DaltDeploymentRepository="staging::default::file://$PWD/staging"
