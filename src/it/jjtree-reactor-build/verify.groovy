File parentTarget = new File(basedir, 'target')
assert !parentTarget.exists() : "Parent project has unexpected output: ${parentTarget.list()}"

File target = new File(basedir, 'module/target')
assert target.isDirectory() : "Sub module has no output folder: $target"
File jjtree = new File(target, 'jjtree/org/Simple.jj')
assert jjtree.isFile() : "Sub module has no JJTree output: $jjtree"
File timestamp = new File(target, 'jjtree-timestamps/Simple.jjt')
assert timestamp.isFile() : "Sub module has no JJTree timestamp: $timestamp"
