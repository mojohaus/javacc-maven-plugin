File parentTarget = new File(basedir, 'target')
assert !parentTarget.exists() : "Parent project has unexpected output: ${parentTarget.list()}"

File target = new File(basedir, 'module/target')
assert target.isDirectory() : "Sub module has no output folder: $target"
File javacc = new File(target, 'javacc/org/codehaus/javacc/simple/BasicParser.java')
assert javacc.isFile() : "Sub module has no JavaCC output: $javacc"
