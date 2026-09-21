File file = new File(basedir, 'target/generated-sources/javacc/parser/BasicParser.java')
assert file.isFile() : "Could not find generated java file: $file"
assert file.getText('UTF-8').startsWith('/* UP-TO-DATE PARSER FILE') : 'Parser file was overwritten although it was newer than the grammar'
