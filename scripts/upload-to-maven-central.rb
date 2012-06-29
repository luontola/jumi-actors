# Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
# This software is released under the Apache License 2.0.
# The license text is at http://www.apache.org/licenses/LICENSE-2.0

require 'json'
require 'fileutils'

def get_env_var(name)
  ENV[name] or raise "Missing environment variable: #{name}"
end

def get_file_urls(index)
  index.flat_map do |entry|
    if entry['type'] == 'folder'
      get_file_urls(entry['files'])
    else
      entry['url']
    end
  end
end

def http_get(file, source_url, username, password)
  puts "GET #{source_url}"
  system('curl',
         '--fail', '--silent', '--show-error',
         '--basic', '--user', username+':'+password,
         '--output', file,
         source_url
  ) or raise "Failed to download #{source_url}"
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

go_server_url = get_env_var('GO_SERVER_URL')
go_dependency_locator = get_env_var('GO_DEPENDENCY_LOCATOR_JUMI')

staging_url = "#{go_server_url}files/#{go_dependency_locator}/build-release/staging"
staging_username = get_env_var('STAGING_USERNAME')
staging_password = get_env_var('STAGING_PASSWORD')

# TODO: use to OSSRH repo, add steps for closing and publishing the repo
release_url = "http://omega.orfjackal.net:8081/nexus/content/repositories/releases"
release_username = get_env_var('RELEASE_USERNAME')
release_password = get_env_var('RELEASE_PASSWORD')

index_file = 'index.tmp'
http_get(index_file, "#{staging_url}.json", staging_username, staging_password)
index_json = IO.read(index_file)
FileUtils.rm_f(index_file)

source_urls = get_file_urls(JSON.parse(index_json))
source_urls.each do |source_url|
  relative_path = source_url.sub(staging_url, '').sub(/^\//, '')
  target_url = source_url.sub(staging_url, release_url)
  temp_file = 'artifact.tmp'
  begin
    puts "\nCopy #{relative_path}"
    http_get(temp_file, source_url, staging_username, staging_password)
    http_put(temp_file, target_url, release_username, release_password)
  ensure
    FileUtils.rm_f(temp_file)
  end
end
