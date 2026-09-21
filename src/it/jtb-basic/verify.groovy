File outputFolder = new File(basedir, 'target/generated-sources/jtb')
File nodeFile = new File(outputFolder, 'org/syntaxtree/Node.java')
assert nodeFile.isFile() : "Could not find generated node file: $nodeFile"
File visitorFile = new File(outputFolder, 'org/visitor/Visitor.java')
assert visitorFile.isFile() : "Could not find generated visitor file: $visitorFile"
File parserFile = new File(outputFolder, 'org/SubScheme.jj')
assert parserFile.isFile() : "Could not find generated jj file: $parserFile"
