/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.language.tokenizers;

import org.languagetool.tokenizers.WordTokenizer;

/**
 * @since 2.8
 */
public class TagalogWordTokenizer extends WordTokenizer {

  @Override
  public String getTokenizingCharacters() {
    // Keep default tokenizing characters (including apostrophes) and
    // post-process tokens instead, merging only apostrophes that occur
    // between letters. Also keep hyphen as a tokenizing character for
    // Tagalog-specific handling.
    return super.getTokenizingCharacters() + "-";
  } 

  @Override
  public java.util.List<String> tokenize(String text) {
    java.util.List<String> tokens = super.tokenize(text);
    if (tokens.size() < 3) {
      return tokens;
    }
    final String APOSTS = "'\u2019\u2018\u201B\u02BC\uFF07";
    java.util.List<String> out = new java.util.ArrayList<>();
    for (int i = 0; i < tokens.size(); ) {
      if (i <= tokens.size() - 3) {
        String a = tokens.get(i);
        String b = tokens.get(i + 1);
        String c = tokens.get(i + 2);
        boolean bIsApostrophe = true;
        for (int k = 0; k < b.length(); k++) {
          if (APOSTS.indexOf(b.charAt(k)) < 0) { bIsApostrophe = false; break; }
        }
        boolean aEndsLetter = a.length() > 0 && Character.isLetterOrDigit(a.codePointAt(a.length() - 1));
        boolean cStartsLetter = c.length() > 0 && Character.isLetterOrDigit(c.codePointAt(0));
        if (bIsApostrophe && aEndsLetter && cStartsLetter) {
          out.add(a + b + c);
          i += 3;
          continue;
        }
      }
      out.add(tokens.get(i));
      i++;
    }
    return out;
  }
}
