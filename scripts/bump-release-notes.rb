# Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
# This software is released under the Apache License 2.0.
# The license text is at http://www.apache.org/licenses/LICENSE-2.0

require 'date'

unless ARGV.length == 1
  puts "Usage: #{$0} RELEASE_NOTES_FILE"
  exit 1
end
RELEASE_NOTES_FILE = ARGV.shift

old_release_notes = IO.read(RELEASE_NOTES_FILE)
new_release_notes = old_release_notes.sub(/^(### Jumi)/,
                                          "### Upcoming Changes\n\n- TBD\n\n\\1")

File.open(RELEASE_NOTES_FILE, 'wb') { |file|
  file.write(new_release_notes)
}
