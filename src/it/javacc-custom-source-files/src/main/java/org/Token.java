/* CUSTOMIZED PARSER FILE - MUST NOT BE OVERWRITTEN BY JAVACC */
package org;

public class Token {

  public int kind;

  public int beginLine, beginColumn, endLine, endColumn;

  public String image;

  public Token next;

  public Token specialToken;

  public String toString()
  {
     return image;
  }

  public static final Token newToken(int ofKind)
  {
     switch(ofKind)
     {
       default : return new Token();
     }
  }

}
