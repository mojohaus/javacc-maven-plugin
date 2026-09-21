File file = new File(basedir, 'target/generated-sources/javacc/org/codehaus/javacc/simple/BasicParser.java')
assert file.isFile() : "Could not find generated java file: $file"
