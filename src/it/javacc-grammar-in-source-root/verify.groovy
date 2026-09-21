File file = new File(basedir, 'target/generated-sources/javacc/test/parser/BasicParser.java')
assert file.isFile() : "Could not find generated java file: $file"
