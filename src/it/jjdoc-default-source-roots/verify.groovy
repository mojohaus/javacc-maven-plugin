['BasicParser', 'Simple', 'SubScheme'].each { name ->
    File file = new File(basedir, "target/site/jjdoc/${name}.html")
    assert file.length() > 0 : "Could not find generated jjdoc file: $file"
}
true
