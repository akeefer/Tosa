package tosa.impl.query;

import gw.util.Pair;
import tosa.api.IDBColumn;
import tosa.api.IDBTable;
import tosa.impl.util.StringSubstituter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Alan
 * Date: 7/31/11
 * Time: 1:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class SqlStringSubstituter {

  public static Pair<String, Object[]> substitute(final String query, final Map<String, Object> values) {
    SqlTokenHandler tokenHandler = new SqlTokenHandler(values);
    String result = StringSubstituter.substitute(query, tokenHandler);
    return new Pair<String, Object[]>(result, tokenHandler._paramValues.toArray());
  }

  public static class SqlTokenHandler implements StringSubstituter.TokenHandler {
    private Map<String, Object> _tokenValues;
    private List<Object> _paramValues;

    public SqlTokenHandler(Map<String, Object> tokenValues) {
      _tokenValues = tokenValues;
      _paramValues = new ArrayList<Object>();
    }

    @Override
    public String tokenValue(String token) {
      if (!_tokenValues.containsKey(token)) {
        // TODO - AHK - Improve error message
        throw new IllegalArgumentException("No value for the token " + token + " was found in the map");
      }

      Object value = _tokenValues.get(token);

      if (value instanceof IDBTable) {
        return ((IDBTable) value).getPossiblyQuotedName();
      } else if (value instanceof IDBColumn) {
        return ((IDBColumn) value).getPossiblyQuotedName();
      } else {
        _paramValues.add(value);
        return "?";
      }
    }
  }


}
