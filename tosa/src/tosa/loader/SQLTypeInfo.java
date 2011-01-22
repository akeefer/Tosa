package tosa.loader;

import gw.lang.reflect.*;
import gw.lang.reflect.java.IJavaType;
import gw.util.GosuExceptionUtil;
import tosa.loader.parser.tree.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SQLTypeInfo extends BaseTypeInfo {

  private IMethodInfo _theOneTrueMethod;
  private SQLType _sqlType;
  private static final Object NOT_PRESENT_SENTINAL = new Object();
  private IType _resultType;

  public SQLTypeInfo(SQLType sqlType) {
    super(sqlType);
    _sqlType = sqlType;
    _resultType = determineResultType();
    _theOneTrueMethod = new MethodInfoBuilder().withName(getMethodName()).withStatic()
      .withStatic()
      .withParameters(determineParameters())
      .withReturnType(determineReturnType())
      .withCallHandler(new IMethodCallHandler() {
        @Override
        public Object handleCall(Object ctx, Object... args) {
          Connection c = null;
          try {
            c = _sqlType.getData().getDatabase().getConnection().connect();
            String sql = _sqlType.getData().getSQL();
            PreparedStatement stmt = c.prepareStatement(sql);
            ResultSet resultSet = stmt.executeQuery();
            List lst = new LinkedList();
            while (resultSet.next()) {
              lst.add(constructResultElement(resultSet));
            }
            return lst;
          } catch (SQLException e) {
            throw new RuntimeException(e);
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
      }).build(this);
  }

  private IType determineResultType() {
    SQLFileInfo data = _sqlType.getData();
    SelectStatement select = data.getSelect();
    SQLParsedElement selectList = select.getSelectList();
    if (selectList instanceof AsteriskSelectList) {
      TableFromClause from = select.getTableExpression().getFrom();
      List<SimpleTableReference> tableRefs = from.getTableRefs();
      if (tableRefs.size() == 1) {
        String baseName = _sqlType.getData().getDatabase().getNamespace() + "." + tableRefs.get(0).getName();
        IType type = TypeSystem.getByFullNameIfValid(baseName);
        if (type != null) {
          return type;
        }
      }
    }
    return IJavaType.OBJECT;
  }

  private Object constructResultElement(ResultSet resultSet) {
    try {
      IType returnType = get_resultType();
      if (IJavaType.OBJECT.equals(returnType)) {
        int count = resultSet.getMetaData().getColumnCount();
        Map hashMap = new HashMap();
        while (count > 0) {
          hashMap.put(resultSet.getMetaData().getColumnName(count), resultSet.getObject(count));
          count--;
        }
        return hashMap;
      } else if (returnType instanceof IDBType) {
        DBTypeInfo typeInfo = (DBTypeInfo) returnType.getTypeInfo();
        return typeInfo.buildObject(resultSet);
      } else {
        throw new IllegalStateException("Do not know how to construct objects of type " + returnType.getName());
      }
    } catch (SQLException e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
  }

  private IType determineReturnType() {
    return IJavaType.ITERABLE.getParameterizedType(get_resultType());
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
    return IJavaType.STRING;
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

  public IType get_resultType() {
    return _resultType;
  }

  public String getMethodName() {
    return "select"; //TODO "insert" etc.
  }
}
