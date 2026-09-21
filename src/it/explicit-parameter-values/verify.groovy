File logFile = new File(basedir, 'build.log')
assert logFile.isFile() : "Missing log file: $logFile"
assert !logFile.text.contains('Bad option') : 'JavaCC/JJTree/JJDoc reported bad option'
