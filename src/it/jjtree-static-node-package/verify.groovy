File classesDir = new File(basedir, 'target/classes')
assert classesDir.isDirectory() : "Could not find classes directory: $classesDir"
File wrongNodeClass = new File(classesDir, 'it/SimpleNode.class')
assert !wrongNodeClass.isFile() : "Found unexpected compiled class file: $wrongNodeClass"
File nodeClass = new File(classesDir, 'it/nodes/SimpleNode.class')
assert nodeClass.isFile() : "Could not find compiled class file: $nodeClass"
