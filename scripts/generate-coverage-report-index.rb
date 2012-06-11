# Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
# This software is released under the Apache License 2.0.
# The license text is at http://www.apache.org/licenses/LICENSE-2.0

require 'fileutils'

def module_list_item(module_index)
  module_name = module_index.split('/').first
  "    <li><a href=\"#{module_index}\">#{module_name}</a></li>"
end

def write(file, content)
  FileUtils.mkdir_p(File.dirname(file))
  File.open(file, 'wb') { |f| f.write(content) }
end

OUTPUT_FILE = ARGV.shift or raise 'Missing argument: OUTPUT_FILE'

module_list_items = Dir.glob('*/target/pit-reports/*/index.html').
        map { |module_index| module_list_item(module_index) }.
        join("\n")

write OUTPUT_FILE, <<eos
<html>
<head>
    <title>Pit Test Coverage Report</title>
</head>
<body>

<h1>Pit Test Coverage Report</h1>

<h3>Modules</h3>

<ul>
#{module_list_items}
</ul>

</body>
</html>
eos
