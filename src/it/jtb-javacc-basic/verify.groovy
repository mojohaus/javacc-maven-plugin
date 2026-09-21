File javaccDir = new File(basedir, 'target/generated-sources/javacc')
File jtbDir = new File(basedir, 'target/generated-sources/jtb')
File nodeFile = new File(jtbDir, 'org/syntaxtree/Node.java')
assert nodeFile.length() > 0 : "Could not find generated java file: $nodeFile"
File visitorFile = new File(jtbDir, 'org/visitor/Visitor.java')
assert visitorFile.length() > 0 : "Could not find generated java file: $visitorFile"
File parserFile = new File(javaccDir, 'org/SubScheme.java')
assert parserFile.length() > 0 : "Could not find generated java file: $parserFile"
File customFile = new File(javaccDir, 'org/Token.java')
assert customFile.length() > 0 : "Could not find generated java file: $customFile"
assert customFile.getText('UTF-8').startsWith('/* CUSTOMIZED PARSER FILE') : 'Custom java file has been ignored or replaced with generated file'
