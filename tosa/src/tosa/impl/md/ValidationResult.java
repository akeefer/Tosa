package tosa.impl.md;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 6/28/11
 * Time: 9:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class ValidationResult {

  private List<String> _errors = new ArrayList<String>();
  private List<String> _warnings = new ArrayList<String>();

  public void addError(String errorMessage) {
    _errors.add(errorMessage);
  }

  public void addWarning(String warningMessage) {
    _warnings.add(warningMessage);
  }

  public List<String> getErrors() {
    return _errors;
  }

  public List<String> getWarnings() {
    return _warnings;
  }

  public boolean hasErrors() {
    return !_errors.isEmpty();
  }

  public boolean hasWarnings() {
    return !_warnings.isEmpty();
  }

  public boolean hasIssues() {
    return hasErrors() || hasWarnings();
  }
}
