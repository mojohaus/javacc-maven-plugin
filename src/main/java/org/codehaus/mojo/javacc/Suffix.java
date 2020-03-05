package org.codehaus.mojo.javacc;

public enum Suffix {
	Java("java"),
	Cpp("cc"),
	CSharp("cs");
	
	private	String suffix;
	
	Suffix(String suffix)
	{
		this.suffix = suffix;
	}

	public String string() {
		 return suffix;
	}
}
