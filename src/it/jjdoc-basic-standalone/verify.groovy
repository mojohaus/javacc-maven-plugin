File siteOutputDir = new File(basedir, 'target/site')
assert !siteOutputDir.exists() : 'Report was generated into wrong output directory!'
File mojoOutputDir = new File(basedir, 'target/it-site')
File jjdocFile = new File(mojoOutputDir, 'jjdoc/MyParser.html')
assert jjdocFile.length() > 0 : "File is empty: $jjdocFile"
