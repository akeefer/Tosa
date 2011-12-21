package tosa.impl.util;

/**
 * Created by IntelliJ IDEA.
 * User: Alan
 * Date: 7/31/11
 * Time: 10:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class StringSubstituter {

  /**
   * Callback interface that's invoked for each token
   */
  public interface TokenHandler {
    /**
     * Given a token, return the appropriate value.  The token will be passed through without the ":" character,
     * so :foo in the input String will result in a call to this method with "foo" as the argument.
     *
     * @param token the token to return a value for, without ":" pre-pended
     * @return the value to insert into the output String
     */
    String tokenValue(String token);
  }

  // Mini-state-machine for parsing
  private static enum ParsingState {
    Text, Token
  }

  /**
   * For the given input String, this method will compose an output buffer consisting of this input, with all
   * values of the form :token substituted for the values returned by calling into the tokenHandler.  Valid
   * tokens consist of a : character followed by valid java identifier parts (alphanumeric characters or '_').
   * For each token, the tokenHandler callback will be invoked.
   *
   * @param input the input String
   * @param tokenHandler the callback interface to invoke for each token
   * @return input String with substitutions performed on it
   */
  public static String substitute(String input, TokenHandler tokenHandler) {
    if (tokenHandler == null) {
      throw new IllegalArgumentException("The tokenHandler input is not allowed to be null");
    }

    if (input == null) {
      return null;
    }


    StringBuilder result = new StringBuilder();
    int length = input.length();
    int tokenStart = 0;
    ParsingState state = ParsingState.Text;
    for (int i = 0; i < length; i++) {
      char currentChar = input.charAt(i);

      if (state == ParsingState.Text) {
        switch (currentChar) {
          case ':':
            if (i < length - 1 && Character.isJavaIdentifierPart(input.charAt(i + 1))) {
              state = ParsingState.Token;
              tokenStart = i;
            } else {
              result.append(currentChar);
            }
            break;
          case '\\':
            if (i < length - 1 && input.charAt(i + 1) == ':') {
              // Swallow the \, append the :, and advance the token counter so we don't see it next time through
              result.append(':');
              i++;
            } else {
              result.append(currentChar);
            }
            break;
          default:
            result.append(currentChar);
            break;
        }
      } else if (state == ParsingState.Token) {
        if (!Character.isJavaIdentifierPart(currentChar)) {
          result.append(tokenHandler.tokenValue(input.substring(tokenStart + 1, i)));
          state = ParsingState.Text;
          i--; // Push the character back on the stack, effectively
        }
      }
    }

    // If we end with the parsing state still on "token", then we need to add it in
    if (state == ParsingState.Token) {
      result.append(tokenHandler.tokenValue(input.substring(tokenStart + 1)));
    }

    return result.toString();
  }
}
