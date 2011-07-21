package tosa.loader;

import gw.lang.reflect.*;
import tosa.dbmd.DatabaseImpl;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseAccessTypeInfo extends BaseTypeInfo {

  private Map<CharSequence, IPropertyInfo> _propertyMap;
  private List<IPropertyInfo> _propertyList;
  private List<IMethodInfo> _methodList;
  // TODO - AHK - Something around recreating tables and the like
  // TODO - AHK - An "instance" property to get a hold of the underlying IDatabase


  public DatabaseAccessTypeInfo(DatabaseAccessType type) {
    super(type);
    _propertyList = new ArrayList<IPropertyInfo>();
    _propertyMap = new HashMap<CharSequence, IPropertyInfo>();
    _methodList = new ArrayList<IMethodInfo>();

    addProperty(createUrlProperty());
    addMethod(createCreateTablesMethod());
    addMethod(createDropTablesMethod());

    _propertyList = Collections.unmodifiableList(_propertyList);
    _propertyMap = Collections.unmodifiableMap(_propertyMap);
    _methodList = Collections.unmodifiableList(_methodList);
  }

  private IPropertyInfo createUrlProperty() {
    return new PropertyInfoBuilder()
        .withName("JdbcUrl")
        .withStatic()
        .withReadable(true)
        .withWritable(true)
        .withDescription("The jdbc url for this database.")
        .withType(String.class)
        .withAccessor(new IPropertyAccessor() {
          @Override
          public Object getValue(Object o) {
            return getJdbcUrl();
          }

          @Override
          public void setValue(Object o, Object o1) {
            setJdbcUrl((String) o1);
          }
        }).build(this);
  }

  private IMethodInfo createCreateTablesMethod() {
    return new MethodInfoBuilder()
        .withName("createTables")
        .withParameters()
        .withReturnType(void.class)
        .withStatic()
        .withDescription("Creates the tables for this database, specific by executing the DDL statements.")
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object o, Object... objects) {
            getDb().createTables();
            return null;
          }
        }).build(this);
  }

  private IMethodInfo createDropTablesMethod() {
    return new MethodInfoBuilder()
        .withName("dropTables")
        .withParameters()
        .withReturnType(void.class)
        .withStatic()
        .withDescription("Drops all tables in this database.")
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object o, Object... objects) {
            getDb().dropTables();
            return null;
          }
        }).build(this);
  }

  private DatabaseImpl getDb() {
    return ((DatabaseAccessType) getOwnersType()).getDatabaseImpl();
  }

  private void addProperty(IPropertyInfo property) {
    _propertyList.add(property);
    _propertyMap.put(property.getName(), property);
  }

  private void addMethod(IMethodInfo method) {
    _methodList.add(method);
  }

  @Override
  public List<? extends IMethodInfo> getMethods() {
    return _methodList;
  }

  @Override
  public IMethodInfo getMethod(CharSequence methodName, IType... params) {
    for (IMethodInfo method : _methodList) {
      if (methodMatches(method, methodName, params)) {
        return method;
      }
    }
    return null;
  }

  private boolean methodMatches(IMethodInfo potentialMatch, CharSequence methodName, IType[] paramTypes) {
    return potentialMatch.getDisplayName().equals(methodName) && parametersMatch(potentialMatch.getParameters(), paramTypes);
  }

  private boolean parametersMatch(IParameterInfo[] parameters, IType[] paramTypes) {
    if (parameters.length == 0 && paramTypes == null) {
      return true;
    }

    if (parameters.length == paramTypes.length) {
      for (int i = 0; i < parameters.length; i++) {
        // TODO - AHK - Assignability, or equality?
        if (!parameters[i].getFeatureType().equals(paramTypes[i])) {
          return false;
        }
      }
    } else {
      return false;
    }

    return true;
  }

  @Override
  public IMethodInfo getCallableMethod(CharSequence method, IType... params) {
    return getMethod(method, params);
  }

  @Override
  public List<? extends IPropertyInfo> getProperties() {
    return _propertyList;
  }

  @Override
  public IPropertyInfo getProperty(CharSequence propName) {
    return _propertyMap.get(propName);
  }

  @Override
  public CharSequence getRealPropertyName(CharSequence propName) {
    return propName;
  }

  private String getJdbcUrl() {
    return ((DatabaseAccessType) getOwnersType()).getDatabaseImpl().getJdbcUrl();
  }

  private void setJdbcUrl(String url) {
    ((DatabaseAccessType) getOwnersType()).getDatabaseImpl().setJdbcUrl(url);
  }

}

