File outputFolder = new File(basedir, 'target/generated-sources/javacc')
File nodeFile = new File(outputFolder, 'Node.java')
assert nodeFile.isFile() : "Could not find generated java file: $nodeFile"
File parserFile = new File(outputFolder, 'Simple.java')
assert parserFile.isFile() : "Could not find generated java file: $parserFile"
