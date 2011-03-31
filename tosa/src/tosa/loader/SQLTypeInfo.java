package tosa.loader;

import gw.lang.reflect.*;
import gw.lang.reflect.java.IJavaType;
import gw.util.GosuExceptionUtil;
import gw.util.Pair;
import tosa.api.IDBColumnType;
import tosa.db.execution.QueryExecutor;
import tosa.loader.parser.tree.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SQLTypeInfo extends BaseTypeInfo {

  private IMethodInfo _theOneTrueMethod;
  private ISQLType _sqlType;
  private static final Object NOT_PRESENT_SENTINAL = new Object();
  private IType _resultType;

  public SQLTypeInfo(ISQLType sqlType) {
    super(sqlType);
    _sqlType = sqlType;
    _resultType = determineResultType();
    _theOneTrueMethod = new MethodInfoBuilder().withName("select").withStatic()
      .withStatic()
      .withParameters(determineParameters())
      .withReturnType(determineReturnType())
      .withCallHandler(new IMethodCallHandler() {
        @Override
        public Object handleCall(Object ctx, Object... args) {
          return invokeQuery(args);
        }
      }).build(this);
  }

  public Object invokeQuery(Object... args) {
    Connection c = null;
    try {
      HashMap<String, Object> values = makeArgMap(args);

      c = _sqlType.getData().getDatabase().getConnection().connect();
      String sql = _sqlType.getData().getSQL(values);
      PreparedStatement stmt = c.prepareStatement(sql);

      List<VariableExpression> vars = _sqlType.getData().getVariables();
      int position = 1;
      for (VariableExpression var : vars) {
        Object value = values.get(var.getName());
        if (var.isList()) {
          if (value instanceof List) {
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

      ResultSet resultSet = stmt.executeQuery();
      List lst = new LinkedList();
      while (resultSet.next()) {
        lst.add(constructResultElement(resultSet));
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

  private HashMap<String, Object> makeArgMap(Object[] args) {
    List<SQLParameterInfo> pis = _sqlType.getData().getParameterInfos();
    HashMap<String, Object> values = new HashMap<String, Object>();
    for (int i = 0; i < pis.size(); i++) {
      SQLParameterInfo pi = pis.get(i);
      values.put(pi.getName(), args[i]);
    }
    return values;
  }

  private IType determineResultType() {
    SQLFileInfo data = _sqlType.getData();
    SelectStatement select = data.getSelect();
    if (select.isSimpleSelect()) {
      IType type = TypeSystem.getByFullNameIfValid(_sqlType.getData().getDatabase().getNamespace() + "." + select.getSimpleTableName());
      if (type != null) {
        return type;
      }
    } else if(select.isComplexSelect()) {
      //TODO cgross - register this type and make it a type ref
      return new StructType(_sqlType.getTypeLoader(), _sqlType.getName() + "Result", select.getColumnMap());
    }
    return IJavaType.OBJECT;
  }

  private Object constructResultElement(ResultSet resultSet) {
    try {
      IType returnType = getResultType();
      if (IJavaType.OBJECT.equals(returnType)) {
        int count = resultSet.getMetaData().getColumnCount();
        Map hashMap = new HashMap();
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

  private IType determineReturnType() {
    return IJavaType.ITERABLE.getParameterizedType(getResultType());
  }

  private ParameterInfoBuilder[] determineParameters() {
    ArrayList<ParameterInfoBuilder> builders = new ArrayList<ParameterInfoBuilder>();
    List<SQLParameterInfo> pis = _sqlType.getData().getParameterInfos();
    for (SQLParameterInfo pi : pis) {
      builders.add(new ParameterInfoBuilder().withName(pi.getName()).withType(determineTypeOfParam(pi)).withDefValue(NOT_PRESENT_SENTINAL));
    }
    return builders.toArray(new ParameterInfoBuilder[0]);
  }

  private IType determineTypeOfParam(SQLParameterInfo pi) {
    if (pi.isList()) {
      return IJavaType.LIST.getGenericType().getParameterizedType(IJavaType.STRING);
    } else {
      return IJavaType.STRING;
    }
  }

  @Override
  public List<? extends IMethodInfo> getMethods() {
    return Collections.singletonList(_theOneTrueMethod);
  }

  @Override
  public IMethodInfo getMethod(CharSequence methodName, IType... params) {
    return ITypeInfo.FIND.callableMethod(getMethods(), methodName, params);
  }

  @Override
  public IMethodInfo getCallableMethod(CharSequence method, IType... params) {
    return getMethod(method, params);
  }

  public IType getResultType() {
    return _resultType;
  }

}
