File classesDir = new File(basedir, 'target/classes')
assert classesDir.isDirectory() : "Could not find classes directory: $classesDir"
File nodeClass1 = new File(classesDir, 'it/parser1/node/SimpleNode.class')
assert nodeClass1.isFile() : "Could not find compiled class file: $nodeClass1"
File nodeClass2 = new File(classesDir, 'it/parser2/node/SimpleNode.class')
assert nodeClass2.isFile() : "Could not find compiled class file: $nodeClass2"
