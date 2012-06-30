#!/bin/sh
set -ex

ruby scripts/update-to-release-version.rb

mvn clean deploy \
    --batch-mode \
    --errors \
    -Psonatype-oss-release \
    -Dgpg.passphrase=$GPG_PASSPHRASE \
    -DaltDeploymentRepository=staging::default::file://$PWD/staging
