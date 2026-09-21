File file = new File(basedir, 'target/generated-sources/javacc/org/codehaus/javacc/simple/Token.java')
assert file.isFile() : "Could not find generated java file: $file"
assert file.getText('UTF-8').startsWith('/* CUSTOMIZED PARSER FILE') : 'Customized Token.java has been ignored or replaced with generated file'
