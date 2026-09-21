File targetDir = new File(basedir, 'target')
assert targetDir.isDirectory() : "Missing output directory: $targetDir"
File outFile = new File(targetDir, 'java cc/org/codehaus/javacc/simple/BasicParser.java')
assert outFile.isFile() : "Missing JavaCC output file: $outFile"
