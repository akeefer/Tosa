package tosa.impl.util;

import org.junit.Test;
import tosa.TosaLoaderTestBase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Alan
 * Date: 7/31/11
 * Time: 10:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class StringSubstituterTest extends TosaLoaderTestBase {

  @Test
  public void testInputWithNoSubstitutionTokens() {
    assertEquals(null, StringSubstituter.substitute(null, map()));
    assertEquals("", StringSubstituter.substitute("", map()));
    assertEquals("input", StringSubstituter.substitute("input", map()));
  }

  @Test
  public void testVariousBoundsConditions() {
    assertEquals("foo", StringSubstituter.substitute(":a", map("a", "foo")));
    assertEquals("foo ", StringSubstituter.substitute(":a ", map("a", "foo")));
    assertEquals(" foo", StringSubstituter.substitute(" :a", map("a", "foo")));
  }

  @Test
  public void testInputWithMultipleOccurrences() {
    assertEquals("foofoo", StringSubstituter.substitute(":a:a", map("a", "foo")));
    assertEquals("foo  foo", StringSubstituter.substitute(":a  :a", map("a", "foo")));
    assertEquals(" foo bar foo", StringSubstituter.substitute(" :a bar :a", map("a", "foo")));
    assertEquals(" foo bar foo ", StringSubstituter.substitute(" :a bar :a ", map("a", "foo")));
  }

  @Test
  public void testInputWithMultipleSubstitutionsToPerform() {
    assertEquals("foobarbaz", StringSubstituter.substitute(":a:b:c", map("a", "foo", "b", "bar", "c", "baz")));
    assertEquals("bazfoobar", StringSubstituter.substitute(":c:a:b", map("a", "foo", "b", "bar", "c", "baz")));
    assertEquals("This is a string with bar and baz and foo in it", StringSubstituter.substitute("This is a string with :b and :c and :a in it", map("a", "foo", "b", "bar", "c", "baz")));
  }

  @Test
  public void testTokenBoundaries() {
    assertEquals("Text foo,rest", StringSubstituter.substitute("Text :a,rest", map("a", "foo")));
    assertEquals("Text foo;rest", StringSubstituter.substitute("Text :a;rest", map("a", "foo")));
    assertEquals("Text foo'rest", StringSubstituter.substitute("Text :a'rest", map("a", "foo")));
  }

  @Test
  public void testMoreRealisticTokenNames() {
    assertEquals("SELECT * FROM Foo WHERE Value < 5", StringSubstituter.substitute("SELECT * FROM :table WHERE :column < :value", map("table", "Foo", "column", "Value", "value", "5")));
  }

  @Test
  public void testTokensCanIncludeNumbers() {
    assertEquals("foo rest", StringSubstituter.substitute(":a23 rest", map("a23", "foo")));
  }

  @Test
  public void testTokensCanIncludeUnderscores() {
    assertEquals("foo rest", StringSubstituter.substitute(":a2_3 rest", map("a2_3", "foo")));
  }

  @Test
  public void testColonCanBeEscaped() {
    assertEquals(":a", StringSubstituter.substitute("\\:a", map("a", "foo")));
    assertEquals(" :a", StringSubstituter.substitute(" \\:a", map("a", "foo")));
    assertEquals("foo:a", StringSubstituter.substitute(":a\\:a", map("a", "foo")));
  }

  @Test
  public void testColonFollowedByNonIdentifierCharacter() {
    assertEquals("Text :;statement", StringSubstituter.substitute("Text :;statement", map("a", "foo")));
    assertEquals("Text :", StringSubstituter.substitute("Text :", map("a", "foo")));
  }

  @Test
  public void testBackslashIsIncludedWhenItIsNotAnEscapeCharacter() {
    assertEquals("foo\\b", StringSubstituter.substitute(":a\\b", map("a", "foo")));
    assertEquals(" \\b", StringSubstituter.substitute(" \\b", map("a", "foo")));
    assertEquals("\\b", StringSubstituter.substitute("\\b", map("a", "foo")));
    assertEquals("\\b\\", StringSubstituter.substitute("\\b\\", map("a", "foo")));
  }

  @Test
  public void testCallWithNullTokenHandlerThrowsIllegalArgumentException() {
    try {
      StringSubstituter.substitute("input", null);
      fail("Expected an IllegalArgumentException to be thrown");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  // ----------------------------- Helper Methods

  private Map<String, Object> map(Object ...pairs) {
    HashMap<String, Object> map = new HashMap<String, Object>();
    for (int i = 0; i < pairs.length; i+=2) {
      map.put((String) pairs[i], pairs[i + 1]);
    }
    return map;
  }

  private StringSubstituter.TokenHandler map(String ...pairs) {
    final HashMap<String, String> map = new HashMap<String, String>();
    for (int i = 0; i < pairs.length; i+=2) {
      map.put(pairs[i], pairs[i + 1]);
    }
    return new StringSubstituter.TokenHandler() {
      @Override
      public String tokenValue(String token) {
        return map.get(token);
      }
    };
  }
}
