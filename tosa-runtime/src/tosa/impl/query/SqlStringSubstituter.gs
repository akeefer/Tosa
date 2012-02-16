package tosa.impl.query

uses java.util.Map
uses gw.util.Pair
uses tosa.impl.util.StringSubstituter
uses java.util.ArrayList
uses java.lang.IllegalArgumentException
uses tosa.api.IDBTable
uses tosa.api.IDBColumn
uses tosa.api.IPreparedStatementParameter
uses java.sql.PreparedStatement

/**
 * Created by IntelliJ IDEA.
 * User: Alan
 * Date: 2/16/12
 * Time: 8:59 AM
 * To change this template use File | Settings | File Templates.
 */
class SqlStringSubstituter {
  
  static class SqlAndParams {
    var _sql : String as readonly Sql
    var _paramObjects : Object[] as readonly ParamObjects
    
    construct(sqlArg : String, paramObjectsArg : Object[]) {
      _sql = sqlArg
      _paramObjects = paramObjectsArg
    }

    property get Params() : IPreparedStatementParameter[] {
      return _paramObjects.map( \ p -> wrapParameter(p) )
    }

    private function wrapParameter(objectParameter : Object) : IPreparedStatementParameter {
      if (objectParameter == null) {
        throw new IllegalArgumentException("Query methods cannot be called with null passed in for a prepared statement parameter.  " +
            "You almost certainly want to generate a query explicitly with X IS NULL or X IS NOT NULL rather than " +
            "using X = ? or X <> ? and passing null as the bind variable.");
      }

      if (objectParameter typeis IPreparedStatementParameter) {
        return objectParameter;
      } else {
        // Here we're just relying on the JDBC connection's auto-coercion
        return new SimplePreparedStatementParameter(objectParameter)
      }
    }
  }

  static class SimplePreparedStatementParameter implements IPreparedStatementParameter {
    var _value : Object

    construct(value : Object) {
      _value = value
    }

    override function setParameter(statement : PreparedStatement, idx : int) {
      statement.setObject(idx, _value)
    }
  }
  
  static function substitute(query : String, values : Map<String, Object>) : SqlAndParams {
    var paramValues = new ArrayList<Object>()
    var result = StringSubstituter.substitute(query, \s -> substituteToken(s, values, paramValues));
    return new SqlAndParams(result, paramValues.toArray());
  }
  
  private static function substituteToken(token : String, tokenValues : Map<String, Object>, paramValues : List<Object>) : String {
    if (!tokenValues.containsKey(token)) {
      // TODO - AHK - Improve error message
      throw new IllegalArgumentException("No value for the token " + token + " was found in the map");
    }

    var value = tokenValues.get(token);

    if (value typeis IDBTable) {
      return value.PossiblyQuotedName
    } else if (value typeis IDBColumn) {
      return value.PossiblyQuotedName
    } else {
      paramValues.add(value);
      return "?"
    }
  }
}