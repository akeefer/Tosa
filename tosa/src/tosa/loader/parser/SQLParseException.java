package tosa.loader.parser;

import tosa.loader.parser.tree.SQLParseError;

import java.util.*;

public class SQLParseException extends RuntimeException {
  public SQLParseException(String fileName, Set<SQLParseError> errors) {
    super(makeMessage(fileName, errors));
  }

  private static String makeMessage(String fileName, Set<SQLParseError> errors) {
    List<SQLParseError> orderedErrors = new ArrayList<SQLParseError>(errors);
    Collections.sort(orderedErrors, new Comparator<SQLParseError>() {
      @Override
      public int compare(SQLParseError o1, SQLParseError o2) {
        return o1.getStart().getStart() - o2.getStart().getStart();
      }
    });
    StringBuilder sb = new StringBuilder();
    sb.append("SQL Parse Errors found in ");
    sb.append(fileName);
    sb.append(":\n\n");
    for (SQLParseError error : orderedErrors) {
      sb.append("  * line ");
      sb.append(error.getStart().getLine());
      sb.append(", col ");
      sb.append(error.getStart().getColumn());
      sb.append(": ");
      sb.append(error.getMessage());
    }
    sb.append("\n\n");
    return sb.toString();
  }
}
