# Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
# This software is released under the Apache License 2.0.
# The license text is at http://www.apache.org/licenses/LICENSE-2.0

def get_env_var(name)
  ENV[name] or raise "Missing environment variable: #{name}"
end

def http_put(file, target_url, username, password)
  puts "PUT #{target_url}"
  system('curl',
         '--fail', '--silent', '--show-error',
         '--basic', '--user', username+':'+password,
         '--upload-file', file,
         target_url
  ) or raise "Failed to upload #{target_url}"
end

unless ARGV.length == 2
  puts "Usage: #{$0} SOURCE_DIR DEPLOY_URL"
  exit 1
end
SOURCE_DIR = ARGV.shift
DEPLOY_URL = ARGV.shift

deploy_username = get_env_var('DEPLOY_USERNAME')
deploy_password = get_env_var('DEPLOY_PASSWORD')

puts "Copying from #{SOURCE_DIR} to #{DEPLOY_URL}"

Dir.chdir(SOURCE_DIR) do
  Dir.glob('**/*').select { |path| File.file?(path) }.each do |path|
    http_put(path, "#{DEPLOY_URL}/#{path}", deploy_username, deploy_password)
  end
end

puts "Done"
