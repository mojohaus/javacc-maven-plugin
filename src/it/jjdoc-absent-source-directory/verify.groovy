File target = new File(basedir, 'target')
if (target.exists()) {
    String[] files = target.list()
    assert files == null || (files.length == 1 && files[0] == 'site') : "Found unexpected output: ${files as List}"
}
File jjdoc = new File(target, 'site/jjdoc')
assert !jjdoc.exists() : "Found unexpected JJDoc output: $jjdoc"
