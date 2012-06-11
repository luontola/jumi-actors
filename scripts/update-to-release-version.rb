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

def update_version(pom, old_version, new_version)
  artifact_id = pom.elements["/project/artifactId"].text
  version_xpaths = ["/project/version[text() = '#{old_version}']",
                    "/project/parent/version[text() = '#{old_version}']"]

  version_xpaths.each { |xpath|
    version = pom.elements[xpath]
    if version != nil
      puts "    artifactId:  #{artifact_id}"
      puts "    version:     #{old_version} -> #{new_version}"
      version.text = new_version
    end
  }
  puts
end

BUILD_NUMBER = Integer(ENV['GO_PIPELINE_COUNTER'])

root_pom = REXML::Document.new(File.new("pom.xml"))
old_version = root_pom.elements["/project/version"].text
new_version = get_release_version(old_version, BUILD_NUMBER)

Dir.glob(%w(pom.xml */pom.xml)) { |pom_path|
  puts pom_path

  pom = REXML::Document.new(File.new(pom_path))
  pom.context[:attribute_quote] = :quote
  update_version(pom, old_version, new_version)

  File.open(pom_path, 'wb') { |file|
    pom.write(file)
  }
}
