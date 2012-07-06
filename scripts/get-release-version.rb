# Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
# This software is released under the Apache License 2.0.
# The license text is at http://www.apache.org/licenses/LICENSE-2.0

require 'rexml/document'

def get_release_version(snapshot_version, build_number)
  snapshot_suffix = /-SNAPSHOT$/
  unless snapshot_version =~ snapshot_suffix
    raise "Not a snapshot version: #{snapshot_version}"
  end
  snapshot_version.gsub(snapshot_suffix, ".#{build_number}")
end

unless ARGV.length == 1
  puts "Usage: #{$0} BUILD_NUMBER"
  exit 1
end
BUILD_NUMBER = Integer(ARGV.shift)

root_pom = REXML::Document.new(File.new("pom.xml"))
old_version = root_pom.elements["/project/version"].text
new_version = get_release_version(old_version, BUILD_NUMBER)

puts new_version
