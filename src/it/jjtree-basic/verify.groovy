File outputFolder = new File(basedir, 'target/generated-sources/jjtree')
File nodeFile = new File(outputFolder, 'Node.java')
assert nodeFile.length() > 0 : "Could not find generated java file: $nodeFile"
File parserFile = new File(outputFolder, 'Simple.jj')
assert parserFile.length() > 0 : "Could not find generated jj file: $parserFile"
