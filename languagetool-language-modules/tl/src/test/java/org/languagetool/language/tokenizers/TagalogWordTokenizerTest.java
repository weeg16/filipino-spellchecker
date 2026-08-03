/* LanguageTool - Tagalog tokenizer tests
 * Copyright (C) 2026
 */
package org.languagetool.language.tokenizers;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TagalogWordTokenizerTest {

  private final TagalogWordTokenizer tokenizer = new TagalogWordTokenizer();

  @Test
  public void testApostropheStaysInToken() {
    List<String> tokens = tokenizer.tokenize("bagama’t");
    // should be a single token (no split on apostrophe)
    assertEquals(1, tokens.size());
    assertEquals("bagama’t", tokens.get(0));

    tokens = tokenizer.tokenize("bagama't");
    assertEquals(1, tokens.size());
    assertEquals("bagama't", tokens.get(0));

    tokens = tokenizer.tokenize("hangga’t");
    assertEquals(1, tokens.size());
    assertEquals("hangga’t", tokens.get(0));
  }
}
