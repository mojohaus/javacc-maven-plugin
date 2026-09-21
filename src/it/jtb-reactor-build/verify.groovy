File parentTarget = new File(basedir, 'target')
assert !parentTarget.exists() : "Parent project has unexpected output: ${parentTarget.list()}"

File target = new File(basedir, 'module/target')
assert target.isDirectory() : "Sub module has no output folder: $target"
File jtb = new File(target, 'jtb/org/SubScheme.jj')
assert jtb.isFile() : "Sub module has no JTB output: $jtb"
File timestamp = new File(target, 'jtb-timestamps/SubScheme.jtb')
assert timestamp.isFile() : "Sub module has no JTB timestamp: $timestamp"
