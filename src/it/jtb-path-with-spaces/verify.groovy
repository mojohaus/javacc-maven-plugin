File targetDir = new File(basedir, 'target')
assert targetDir.isDirectory() : "Missing output directory: $targetDir"
File outFile = new File(targetDir, 'j t b/org/SubScheme.jj')
assert outFile.isFile() : "Missing JTB output file: $outFile"
File tsFile = new File(targetDir, 'j t b ts/SubScheme.jtb')
assert tsFile.isFile() : "Missing JTB timestamp file: $tsFile"
