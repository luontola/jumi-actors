# Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
# This software is released under the Apache License 2.0.
# The license text is at http://www.apache.org/licenses/LICENSE-2.0

require 'date'

unless ARGV.length == 2
  puts "Usage: #{$0} CHANGELOG_FILE RELEASE_VERSION"
  exit 1
end
CHANGELOG_FILE = ARGV.shift
RELEASE_VERSION = ARGV.shift

release_title_line = "**Jumi #{RELEASE_VERSION}** (released #{Date.today.strftime('%F')})"
placeholder_line = "- TBD"

old_changelog = IO.read(CHANGELOG_FILE)
if /^#{placeholder_line}$/ =~ old_changelog
  raise "Changelog not ready for release, contains '#{placeholder_line}'"
end
new_changelog = old_changelog.sub(/^\*\*next release\*\*$/,
                                  "**next release**\n\n#{placeholder_line}\n\n#{release_title_line}")

File.open(CHANGELOG_FILE, 'wb') { |file|
  file.write(new_changelog)
}
