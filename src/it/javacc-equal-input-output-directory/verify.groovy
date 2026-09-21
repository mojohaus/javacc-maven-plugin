File tokenFile = new File(basedir, 'target/generated-sources/javacc/Token.java')
assert tokenFile.isFile() : "Could not find generated Token.java file: $tokenFile"
assert tokenFile.length() > 0 : "File is empty, it is being overwritten by javacc: $tokenFile"
