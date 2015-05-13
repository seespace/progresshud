import re, inspect, os

from os.path import basename, normpath, dirname
from xml.dom.minidom import parseString

include_defs('//build/DEFS')

# CONFIG
MANIFEST = 'src/main/AndroidManifest.xml'
NAME = get_default_name()
PACKAGE = get_package_id(MANIFEST)

deps = maven_deps([
  'org.jdeferred:jdeferred-core:jar:1.2.3/f6723c14355ab64c62a4460fbc74a44fe017ad2c'
])
# END

# IDE integration
project_config(
  src_target = ':' + NAME,
  src_roots = [ 'src/main/java' ]
)

# AIDLs
android_library(
  name = 'inair-aidls',
  srcs = get_aidls(),
)

# rule for BuildConfig.java
android_build_config(
  name = 'build-config',
  package = PACKAGE,
)

# local jars
jars = local_jars()

# rule for res
android_resource(
  name = 'res',
  package = PACKAGE,
  res = 'src/main/res',
  deps = [
    '//system/framework:res',
  ],
  visibility = ['PUBLIC'],
)

android_library(
  name = NAME+'_lib',
  manifest = MANIFEST,
  srcs = glob(['src/**/*.java']) + get_aidls() + deps,
  deps = [
    ':build-config',
    ':res',
    jars,
    '//system/framework:framework',
    '//system/framework:res',
  ] + get_aidls() + deps,
  visibility = ['PUBLIC'],
)

android_aar(
  name = NAME,
  manifest_skeleton = MANIFEST,
  deps = [
    ':build-config',
    ':res',
    ':'+NAME+'_lib'
  ],
  visibility = ['PUBLIC']
)