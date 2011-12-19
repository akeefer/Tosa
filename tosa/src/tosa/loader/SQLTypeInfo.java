package tosa.loader;

import gw.lang.GosuShop;
import gw.lang.parser.ISymbol;
import gw.lang.reflect.*;
import gw.lang.reflect.java.IJavaType;
import gw.lang.reflect.java.JavaTypes;
import gw.util.GosuExceptionUtil;
import tosa.db.execution.QueryExecutor;
import tosa.loader.parser.SQLParseException;
import tosa.loader.parser.tree.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SQLTypeInfo extends BaseTypeInfo {

  private List<IMethodInfo> _methods;
  private ISQLType _sqlType;
  private SQLParseException _sqlpe;

  public SQLTypeInfo(ISQLType sqlType) {
    super(sqlType);
    _sqlType = sqlType;
    _methods = new ArrayList<IMethodInfo>();
    ParameterInfoBuilder[] queryParameters = determineParameters();
    _sqlpe = _sqlType.getData().getSelect().getSQLParseException(_sqlType.getData().getFileName());

    final IType selectReturnType = getResultsType();

    _methods.add(new MethodInfoBuilder().withName("select")
      .withStatic()
      .withParameters(queryParameters)
      .withReturnType(JavaTypes.ITERABLE().getParameterizedType(selectReturnType))
      .withCallHandler(new IMethodCallHandler() {
        @Override
        public Object handleCall(Object ctx, Object... args) {
          return invokeQuery(selectReturnType, args);
        }
      }).build(this));

    if (addSelectAsStructMethod()) {

      final IType structReturnType = getStructResultType();

      _methods.add(new MethodInfoBuilder().withName("selectAsStruct")
        .withStatic()
        .withParameters(queryParameters)
        .withReturnType(JavaTypes.ITERABLE().getParameterizedType(structReturnType))
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            return invokeQuery(structReturnType, args);
          }
        }).build(this));
    }
  }

  private boolean addSelectAsStructMethod() {
    return _sqlType.getData().getSelect().hasMultipleTableTargets();
  }

  public Object invokeQuery(IType returnType, Object... args) {
    verifySql();
    Connection c = null;
    try {
      HashMap<String, Object> values = makeArgMap(args);

      c = _sqlType.getData().getDatabase().getConnection().connect();
      String sql = _sqlType.getData().getSQL(values);
      PreparedStatement stmt = c.prepareStatement(sql);

      List<VariableExpression> vars = _sqlType.getData().getVariables();
      int position = 1;
      for (VariableExpression var : vars) {
        if (var.shouldApply(values)) {
          Object value = values.get(var.getName());
          if (var.isList()) {
            if (value != null) {
              List valueList = (List) value;
              for (Object listValue : valueList) {
                stmt.setObject(position, listValue);
                position++;
              }
            }
          } else {
            stmt.setObject(position, value);
            position++;
          }
        }
      }

      ResultSet resultSet = stmt.executeQuery();
      List lst = new LinkedList();
      while (resultSet.next()) {
        lst.add(constructResultElement(resultSet, returnType));
      }
      return lst;
    } catch (SQLException e) {
      throw GosuExceptionUtil.forceThrow(e);
    } finally {
      if(c != null){
        try {
          c.close();
        } catch (SQLException e) {
          throw GosuExceptionUtil.forceThrow(e);
        }
      }
    }
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
    return JavaTypes.MAP().getGenericType().getParameterizedType(JavaTypes.STRING(), JavaTypes.OBJECT());
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
      builders.add(new ParameterInfoBuilder().withName(pi.getName().substring(1)).withType(pi.getGosuType()).withDefValue(GosuShop.getNullExpressionInstance()));
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
}
