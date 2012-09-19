# Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
# This software is released under the Apache License 2.0.
# The license text is at http://www.apache.org/licenses/LICENSE-2.0

unless ARGV.length == 1
  puts "Usage: #{$0} RELEASE_NOTES_FILE"
  exit 1
end
RELEASE_NOTES_FILE = ARGV.shift

all_releases = IO.read(RELEASE_NOTES_FILE)
next_release = /^### Upcoming Changes$(.+?)^### Jumi/m.match(all_releases)
unless next_release
  raise "release notes for the upcoming release not found in: #{all_releases}"
end
puts next_release[1].strip
