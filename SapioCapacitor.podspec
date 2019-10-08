
  Pod::Spec.new do |s|
    s.name = 'SapioCapacitor'
    s.version = '0.0.1'
    s.summary = 'Sapio Plugin'
    s.license = 'MIT'
    s.homepage = 'https://github.com/indra2341/sapio-capacitor.git'
    s.author = 'Indra'
    s.source = { :git => 'https://github.com/indra2341/sapio-capacitor.git', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
  end