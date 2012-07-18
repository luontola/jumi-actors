# Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
# This software is released under the Apache License 2.0.
# The license text is at http://www.apache.org/licenses/LICENSE-2.0

require 'date'

unless ARGV.length == 2
  puts "Usage: #{$0} RELEASE_NOTES_FILE RELEASE_VERSION"
  exit 1
end
RELEASE_NOTES_FILE = ARGV.shift
RELEASE_VERSION = ARGV.shift

release_title_line = "### Jumi #{RELEASE_VERSION} (#{Date.today.strftime('%F')})"
placeholder_line = "- TBD"

old_release_notes = IO.read(RELEASE_NOTES_FILE)
new_release_notes = old_release_notes.sub(/^### next release$/,
                                          "### next release\n\n#{placeholder_line}\n\n#{release_title_line}")

File.open(RELEASE_NOTES_FILE, 'wb') { |file|
  file.write(new_release_notes)
}
