File javaccDir = new File(basedir, 'target/generated-sources/javacc')
File jjtreeDir = new File(basedir, 'target/generated-sources/jjtree')
File nodeFile = new File(jjtreeDir, 'org/Node.java')
assert nodeFile.length() > 0 : "Could not find generated java file: $nodeFile"
File parserFile = new File(javaccDir, 'org/Simple.java')
assert parserFile.length() > 0 : "Could not find generated java file: $parserFile"
File customFile = new File(javaccDir, 'org/Token.java')
assert customFile.length() > 0 : "Could not find generated java file: $customFile"
assert customFile.getText('UTF-8').startsWith('/* CUSTOMIZED PARSER FILE') : 'Custom java file has been ignored or replaced with generated file'
