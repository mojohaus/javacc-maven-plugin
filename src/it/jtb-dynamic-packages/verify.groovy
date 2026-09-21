File outputFolder = new File(basedir, 'target/generated-sources/jtb')
['parser1', 'parser2'].each { name ->
    File nodeFile = new File(outputFolder, "${name}/ast/Node.java")
    assert nodeFile.isFile() : "Could not find generated node file: $nodeFile"
    File visitorFile = new File(outputFolder, "${name}/visit/Visitor.java")
    assert visitorFile.isFile() : "Could not find generated visitor file: $visitorFile"
    File parserFile = new File(outputFolder, "${name}/${name.capitalize()}.jj")
    assert parserFile.isFile() : "Could not find generated jj file: $parserFile"
}
true
