File targetDir = new File(basedir, 'target')
assert targetDir.isDirectory() : "Missing output directory: $targetDir"
File outFile = new File(targetDir, 'jj tree/org/Simple.jj')
assert outFile.isFile() : "Missing JJTree output file: $outFile"
File tsFile = new File(targetDir, 'jj tree ts/Simple.jjt')
assert tsFile.isFile() : "Missing JJTree timestamp file: $tsFile"
