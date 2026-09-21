File outputDir = new File(basedir, 'target/generated-sources/jtb')
// the visitor package is org by default
File visitorDir = new File(outputDir, 'org/visitor')
assert visitorDir.isDirectory() : "Visitor directory not found: $visitorDir"
File objectDepthFirstFile = new File(visitorDir, 'ObjectDepthFirst.java')
assert objectDepthFirstFile.length() > 0 : "ObjectDepthFirst.java not found or empty: $objectDepthFirstFile"
String contents = objectDepthFirstFile.getText('UTF-8')
assert contents.contains('class ObjectDepthFirst') : 'ObjectDepthFirst.java does not contain class declaration'
assert contents.contains('extends GJDepthFirst<Object, Object>') : 'ObjectDepthFirst.java does not extend GJDepthFirst<Object, Object>'
println 'ObjectDepthFirst.java successfully generated with correct content'
