File parentTarget = new File(basedir, 'target')
if (parentTarget.exists()) {
    String[] files = parentTarget.list()
    assert files == null || (files.length == 1 && files[0] == 'site') : "Parent project has unexpected output: ${files as List}"
}
File parentJjdoc = new File(parentTarget, 'site/jjdoc')
assert !parentJjdoc.exists() : "Parent project has unexpected JJDoc output: $parentJjdoc"

File target = new File(basedir, 'module/target')
assert target.isDirectory() : "Sub module has no output folder: $target"
File jjdoc = new File(target, 'site/jjdoc/BasicParser.html')
assert jjdoc.isFile() : "Sub module has no JJDoc output: $jjdoc"
