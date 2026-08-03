/* LanguageTool - Tagalog reduplication rule
 */
package org.languagetool.rules.tl;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.JLanguageTool;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReduplicationRule extends Rule {

  private final Language language;
  private static final Pattern REDUP = Pattern.compile("^(.+?)\\1$", Pattern.UNICODE_CHARACTER_CLASS);

  public ReduplicationRule(ResourceBundle messages, Language language) {
    super(messages);
    this.language = language;
    setLocQualityIssueType(ITSIssueType.Misspelling);
  }

  private static final java.util.Map<String, java.util.Set<String>> PLAIN_SPELLING_CACHE = new java.util.HashMap<>();

  @Override
  public String getId() {
    return "TL_REDUPLICATION";
  }

  @Override
  public String getDescription() {
    return "Suggest hyphenation for reduplicated words (e.g., araw-araw)";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> matches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    SpellingCheckRule spellingRule = language.getDefaultSpellingRule();

    // If we don't have a spelling backend available at runtime, be conservative
    // and do not suggest hyphenation (avoid false positives).
    if (spellingRule == null) {
      return new RuleMatch[0];
    }

    for (int i = 1; i < tokens.length; i++) {
      AnalyzedTokenReadings atr = tokens[i];
      if (atr.isNonWord()) {
        continue;
      }
      String token = atr.getToken();
      if (token.length() < 4) { // too short to be reduplication like a-a
        continue;
      }
      Matcher m = REDUP.matcher(token);
      if (!m.matches()) {
        continue;
      }
      String part = m.group(1);
      String hyphenated = part + "-" + part;

      // If the original unhyphenated form is valid (e.g. "pagpag"), don't flag it
      // regardless of whether the hyphenated form exists.
      // If the original is not valid, always suggest the hyphenated form —
      // even when the hyphenated form itself isn't in the dictionary yet.
      if (existsInPlainOrBackend(spellingRule, token)) {
        continue;
      }

      RuleMatch match = new RuleMatch(this, sentence, atr.getStartPos(), atr.getEndPos(),
          messages.getString("reduplication"));
      match.addSuggestedReplacement(hyphenated);
      matches.add(match);
    }

    return matches.toArray(new RuleMatch[0]);
  }

  private boolean isDictionaryWord(SpellingCheckRule spellingRule, String word) throws IOException {
    if (!spellingRule.isMisspelled(word)) {
      return true;
    }
    String lowered = StringTools.lowercaseFirstChar(word);
    return !word.equals(lowered) && !spellingRule.isMisspelled(lowered);
  }

  private boolean existsInPlainOrBackend(SpellingCheckRule spellingRule, String word) throws IOException {
    String[] candidates = new String[] {
      "/org/languagetool/resource/" + language.getShortCode() + "/hunspell/spelling.txt",
      "/" + language.getShortCode() + "/hunspell/spelling.txt"
    };
    for (String resPath : candidates) {
      java.util.Set<String> set = PLAIN_SPELLING_CACHE.get(resPath);
      if (set == null) {
        java.io.InputStream is = getClass().getResourceAsStream(resPath);
        if (is == null) {
          continue;
        }
        set = new java.util.HashSet<>();
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))) {
          String line;
          while ((line = br.readLine()) != null) {
            if (line.startsWith("#")) continue;
            String w = line.split("#")[0].trim();
            if (!w.isEmpty()) {
              set.add(w.toLowerCase());
            }
          }
        }
        PLAIN_SPELLING_CACHE.put(resPath, set);
      }
      if (set.contains(word.toLowerCase()) || set.contains(StringTools.lowercaseFirstChar(word).toLowerCase())) {
        return true;
      }
    }
    // fallback: check spelling backend
    if (!spellingRule.isMisspelled(word)) {
      return true;
    }
    String lowered = StringTools.lowercaseFirstChar(word);
    return !word.equals(lowered) && !spellingRule.isMisspelled(lowered);
  }

}
