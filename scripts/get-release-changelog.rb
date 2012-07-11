# Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
# This software is released under the Apache License 2.0.
# The license text is at http://www.apache.org/licenses/LICENSE-2.0

unless ARGV.length == 1
  puts "Usage: #{$0} CHANGELOG_FILE"
  exit 1
end
CHANGELOG_FILE = ARGV.shift

full_changelog = IO.read(CHANGELOG_FILE)
found = /^\*\*next release\*\*$(.+?)^\*\*Jumi/m.match(full_changelog)
unless found
  raise "changelog for next release not found in: #{full_changelog}"
end
puts found[1].strip
