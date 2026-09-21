File file = new File(basedir, 'target/generated-sources/javacc/org/codehaus/javacc/simple/Token.java')
println "Checking for absence of customized java file: $file"
assert !file.exists() : "Customized java file was copied: $file"
