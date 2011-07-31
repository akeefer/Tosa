package tosa.loader;

import gw.lang.parser.ISymbol;
import gw.lang.reflect.*;
import gw.lang.reflect.java.IJavaType;
import gw.util.GosuExceptionUtil;
import tosa.api.IPreparedStatementParameter;
import tosa.api.IQueryResultProcessor;
import tosa.db.execution.QueryExecutor;
import tosa.dbmd.DatabaseImpl;
import tosa.dbmd.PreparedStatementParameterImpl;
import tosa.loader.parser.SQLParseException;
import tosa.loader.parser.tree.*;

import java.sql.*;
import java.util.*;

public class SQLTypeInfo extends BaseTypeInfo {

  private List<IMethodInfo> _methods;
  private ISQLType _sqlType;
  private SQLParseException _sqlpe;
  QueryExecutor _queryExecutor = new QueryExecutor();
  private IMethodInfo _selectMethod;

  public SQLTypeInfo(ISQLType sqlType) {
    super(sqlType);
    _sqlType = sqlType;
    _methods = new ArrayList<IMethodInfo>();
    ParameterInfoBuilder[] queryParameters = determineParameters();
    _sqlpe = _sqlType.getData().getSelect().getSQLParseException(_sqlType.getData().getFileName());

    final IType selectReturnType = getResultsType();

    _selectMethod = new MethodInfoBuilder().withName("select")
      .withStatic()
      .withParameters(queryParameters)
      .withReturnType(IJavaType.ITERABLE.getParameterizedType(selectReturnType))
      .withCallHandler(new IMethodCallHandler() {
        @Override
        public Object handleCall(Object ctx, Object... args) {
          return invokeQuery(selectReturnType, args);
        }
      }).build(this);
    _methods.add(_selectMethod);
  }

  public Object invokeQuery(IType returnType, Object... args) {
    verifySql();
    HashMap<String, Object> values = makeArgMap(args);

    DatabaseImpl database = _sqlType.getData().getDatabase();
    String sql = _sqlType.getData().getSQL(values);
    List<VariableExpression> vars = _sqlType.getData().getVariables();
    List<IPreparedStatementParameter> params = new ArrayList<IPreparedStatementParameter>();

    for (VariableExpression var : vars) {
      if (var.shouldApply(values)) {
        Object value = values.get(var.getName());
        if (var.isList()) {
          if (value != null) {
            List valueList = (List) value;
            for (Object listValue : valueList) {
              params.add(new PreparedStatementParameterImpl(listValue, PreparedStatementParameterImpl.UNKNOWN));
            }
          }
        } else {
          params.add(new PreparedStatementParameterImpl(value, PreparedStatementParameterImpl.UNKNOWN));
        }
      }
    }

    String featureName = _selectMethod == null ? "select" : _selectMethod.getName();
    return _queryExecutor.findFromSql(database,
      featureName, sql, params, new SQLTypeInfoQueryProcessor(returnType));
  }

  private void verifySql() {
    if (_sqlpe != null) {
      throw _sqlpe;
    }
  }

  private HashMap<String, Object> makeArgMap(Object[] args) {
    List<SQLParameterInfo> pis = _sqlType.getData().getParameterInfos();
    HashMap<String, Object> values = new HashMap<String, Object>();
    for (int i = 0; i < pis.size(); i++) {
      SQLParameterInfo pi = pis.get(i);
      values.put(pi.getName(), args[i]);
    }
    return values;
  }

  private IType getResultsType() {
    SelectStatement select = _sqlType.getData().getSelect();
    if (select.hasSingleTableTarget()) {
      IType type = TypeSystem.getByFullNameIfValid(_sqlType.getData().getDatabase().getNamespace() + "." + select.getPrimaryTableName());
      if (type != null) {
        return type;
      }
    } else if(select.hasSpecificColumns()) {
      return getStructResultType();
    }
    return getMapType();
  }

  private IType getStructResultType() {
    //TODO cgross - register this type and make it a type ref
    return new StructType(_sqlType.getTypeLoader(), _sqlType.getName() + "Result", _sqlType.getData().getSelect().getColumnMap());
  }

  private IType getMapType() {
    return IJavaType.MAP.getGenericType().getParameterizedType(IJavaType.STRING, IJavaType.OBJECT);
  }

  private Object constructResultElement(ResultSet resultSet, IType returnType) {
    try {
      if (getMapType().equals(returnType)) {
        int count = resultSet.getMetaData().getColumnCount();
        Map<String, Object> hashMap = new HashMap<String, Object>();
        while (count > 0) {
          hashMap.put(resultSet.getMetaData().getColumnName(count), resultSet.getObject(count));
          count--;
        }
        return hashMap;
      } else if (returnType instanceof StructType) {
        Map<String, IType> propMap = ((StructType) returnType).getPropMap();
        Map vals = new HashMap();
        for (String name : propMap.keySet()) {
          Object val = resultSet.getObject(resultSet.findColumn(name));
          vals.put(name, val);
        }
        return ((StructType) returnType).newInstance(vals);
      } else if (returnType instanceof IDBType) {
        return QueryExecutor.buildObject((IDBType) returnType, resultSet);
      } else {
        throw new IllegalStateException("Do not know how to construct objects of type " + returnType.getName());
      }
    } catch (SQLException e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
  }

  private ParameterInfoBuilder[] determineParameters() {
    ArrayList<ParameterInfoBuilder> builders = new ArrayList<ParameterInfoBuilder>();
    List<SQLParameterInfo> pis = _sqlType.getData().getParameterInfos();
    for (SQLParameterInfo pi : pis) {
      builders.add(new ParameterInfoBuilder().withName(pi.getName().substring(1)).withType(pi.getGosuType()).withDefValue(ISymbol.NULL_DEFAULT_VALUE));
    }
    return builders.toArray(new ParameterInfoBuilder[0]);
  }

  @Override
  public List<? extends IMethodInfo> getMethods() {
    return _methods;
  }

  @Override
  public IMethodInfo getMethod(CharSequence methodName, IType... params) {
    return ITypeInfo.FIND.callableMethod(getMethods(), methodName, params);
  }

  @Override
  public IMethodInfo getCallableMethod(CharSequence method, IType... params) {
    return getMethod(method, params);
  }

  private class SQLTypeInfoQueryProcessor implements IQueryResultProcessor<Object> {
    private IType _returnType;

    public SQLTypeInfoQueryProcessor(IType type) {
      _returnType = type;
    }

    @Override
    public Object processResult(ResultSet result) throws SQLException {
      return constructResultElement(result, _returnType);
    }
  }
}
