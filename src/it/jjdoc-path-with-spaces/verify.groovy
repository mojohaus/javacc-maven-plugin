File targetDir = new File(basedir, 'target')
assert targetDir.isDirectory() : "Missing output directory: $targetDir"
File file1 = new File(targetDir, 'jj doc/jjdoc/BasicParser.html')
assert file1.isFile() : "Missing JJDoc output file: $file1"
