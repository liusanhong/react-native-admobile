# react-native-admobile.podspec

require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-admobile"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  react-native-admobile
                   DESC
  s.homepage     = "https://github.com/github_account/react-native-admobile"
  # brief license entry:
  s.license      = "MIT"
  # optional - use expanded license entry instead:
  # s.license    = { :type => "MIT", :file => "LICENSE" }
  s.authors      = { "Your Name" => "yourname@email.com" }
  s.platforms    = { :ios => "9.0" }
  s.source       = { :git => "https://github.com/github_account/react-native-admobile.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,c,cc,cpp,m,mm,swift}"
  s.requires_arc = true

  s.dependency "React"
  # ...
  s.dependency "ADSuyiSDK",'~> 3.6.0.0'
  s.dependency "ADSuyiSDK/ADSuyiSDKPlatforms/tianmu"
  s.dependency 'ADSuyiSDK/ADSuyiSDKPlatforms/bu' # 穿山甲(头条)
  s.dependency 'ADSuyiSDK/ADSuyiSDKPlatforms/gdt' # 优量汇(广点通）
  s.dependency "ADSuyiSDK/ADSuyiSDKPlatforms/admobile"
  s.dependency "EcookSDK"

end

