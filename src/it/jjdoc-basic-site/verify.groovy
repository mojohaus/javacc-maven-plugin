File mojoOutputDir = new File(basedir, 'target/it-site')
assert !mojoOutputDir.exists() : 'Report was generated into wrong output directory!'
File siteOutputDir = new File(basedir, 'target/site')
File indexFile = new File(siteOutputDir, 'jjdoc/index.html')
assert indexFile.length() > 0 : "File is empty: $indexFile"
File jjdocFile = new File(siteOutputDir, 'jjdoc/MyParser.html')
assert jjdocFile.length() > 0 : "File is empty: $jjdocFile"
