package tosa.loader;

import gw.config.CommonServices;
import gw.lang.reflect.*;
import gw.lang.reflect.features.PropertyReference;
import gw.lang.reflect.java.IJavaType;
import gw.util.concurrent.LazyVar;
import tosa.CachedDBObject;
import tosa.api.*;
import tosa.api.query.CoreFinder;
import tosa.dbmd.DBColumnImpl;
import tosa.impl.query.CoreFinderImpl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBTypeInfo extends BaseTypeInfo implements ITypeInfo {

  public static final String ID_COLUMN = "id";
  private Map<String, IPropertyInfo> _properties;
  private List<IMethodInfo> _methods;
  private LazyVar<Map<String, IPropertyInfo>> _arrayProperties = new LazyVar<Map<String, IPropertyInfo>>() {
    @Override
    protected Map<String, IPropertyInfo> init() {
      return makeArrayProperties();
    }
  };

  private IMethodInfo _getMethod;
  private IMethodInfo _idMethod;
  private IMethodInfo _updateMethod;
  private IMethodInfo _deleteMethod;
  private IMethodInfo _countMethod;
  private IMethodInfo _countWithSqlMethod;
  private IMethodInfo _findMethod;
  private IMethodInfo _findSortedMethod;
  private IMethodInfo _findPagedMethod;
  private IMethodInfo _findSortedPagedMethod;
  private IMethodInfo _findWithSqlMethod;
  private IPropertyInfo _newProperty;
  private IConstructorInfo _ctor;
  private CoreFinder<IDBObject> _finder;

  public DBTypeInfo(IDBType dbType) {
    super(dbType);
    // TODO - AHK - Type reference?
    _finder = new CoreFinderImpl<IDBObject>(dbType);

    _getMethod = new MethodInfoBuilder().withName("fromID").withStatic()
        .withParameters(new ParameterInfoBuilder().withName(ID_COLUMN).withType(IJavaType.pLONG))
        .withReturnType(dbType)
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            return _finder.fromId((Long) args[0]);
          }
        }).build(this);
    _idMethod = new MethodInfoBuilder().withName("toID")
        .withReturnType(IJavaType.pLONG)
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            return ((CachedDBObject) ctx).getColumns().get(ID_COLUMN);
          }
        }).build(this);
    _updateMethod = new MethodInfoBuilder().withName("update")
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            try {
              ((CachedDBObject) ctx).update();
            } catch (SQLException e) {
              throw new RuntimeException(e);
            }
            return null;
          }
        }).build(this);
    _deleteMethod = new MethodInfoBuilder().withName("delete")
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            try {
              ((CachedDBObject) ctx).delete();
            } catch (SQLException e) {
              throw new RuntimeException(e);
            }
            return null;
          }
        }).build(this);
    _countWithSqlMethod = new MethodInfoBuilder().withName("countWithSql").withStatic()
        .withParameters(new ParameterInfoBuilder().withName("sql").withType(IJavaType.STRING))
        .withReturnType(IJavaType.pINT)
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            return _finder.countWithSql((String) args[0]);
          }
        }).build(this);
    _countMethod = new MethodInfoBuilder().withName("count").withStatic()
        .withParameters(new ParameterInfoBuilder().withName("template").withType(dbType))
        .withReturnType(IJavaType.pINT)
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            return _finder.count((IDBObject) args[0]);
          }
        }).build(this);
    _findWithSqlMethod = new MethodInfoBuilder().withName("findWithSql").withStatic()
        .withParameters(new ParameterInfoBuilder().withName("sql").withType(IJavaType.STRING))
        .withReturnType(IJavaType.LIST.getGenericType().getParameterizedType(dbType))
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            return _finder.findWithSql((String) args[0]);
          }
        }).build(this);
    _findMethod = new MethodInfoBuilder().withName("find").withStatic()
        .withParameters(new ParameterInfoBuilder().withName("template").withType(dbType))
        .withReturnType(IJavaType.LIST.getGenericType().getParameterizedType(dbType))
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            return _finder.find((IDBObject) args[0]);
          }
        }).build(this);
    _findSortedMethod = new MethodInfoBuilder().withName("findSorted").withStatic()
        .withParameters(new ParameterInfoBuilder().withName("template").withType(dbType),
            new ParameterInfoBuilder().withName("sortProperty").withType(TypeSystem.get(PropertyReference.class).getParameterizedType(dbType, IJavaType.OBJECT)),
            new ParameterInfoBuilder().withName("ascending").withType(IJavaType.pBOOLEAN))
        .withReturnType(IJavaType.LIST.getGenericType().getParameterizedType(dbType))
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            return _finder.findSorted((IDBObject) args[0], (PropertyReference) args[1], (Boolean) args[2]);
          }
        }).build(this);
    _findPagedMethod = new MethodInfoBuilder().withName("findPaged").withStatic()
        .withParameters(new ParameterInfoBuilder().withName("template").withType(dbType),
            new ParameterInfoBuilder().withName("pageSize").withType(IJavaType.pINT),
            new ParameterInfoBuilder().withName("offset").withType(IJavaType.pINT))
        .withReturnType(IJavaType.LIST.getGenericType().getParameterizedType(dbType))
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            return _finder.findPaged((IDBObject) args[0], (Integer) args[1], (Integer) args[2]);
          }
        }).build(this);
    _findSortedPagedMethod = new MethodInfoBuilder().withName("findSortedPaged").withStatic()
        .withParameters(new ParameterInfoBuilder().withName("template").withType(dbType),
            new ParameterInfoBuilder().withName("sortProperty").withType(TypeSystem.get(PropertyReference.class).getParameterizedType(dbType, IJavaType.OBJECT)),
            new ParameterInfoBuilder().withName("ascending").withType(IJavaType.pBOOLEAN),
            new ParameterInfoBuilder().withName("pageSize").withType(IJavaType.pINT),
            new ParameterInfoBuilder().withName("offset").withType(IJavaType.pINT))
        .withReturnType(IJavaType.LIST.getGenericType().getParameterizedType(dbType))
        .withCallHandler(new IMethodCallHandler() {
          @Override
          public Object handleCall(Object ctx, Object... args) {
            return _finder.findSortedPaged((IDBObject) args[0], (PropertyReference) args[1], (Boolean) args[2], (Integer) args[3], (Integer) args[4]);
          }
        }).build(this);

    _newProperty = new PropertyInfoBuilder().withName("_New").withType(IJavaType.pBOOLEAN)
        .withWritable(false).withAccessor(new IPropertyAccessor() {
          @Override
          public void setValue(Object ctx, Object value) {
          }

          @Override
          public Object getValue(Object ctx) {
            return ((CachedDBObject) ctx).isNew();
          }
        }).build(this);
    _properties = new HashMap<String, IPropertyInfo>();
    for (IDBColumn column : dbType.getTable().getColumns()) {
      // TODO - AHK - Ideally this cast wouldn't be necessary
      IPropertyInfo prop = makeProperty((DBColumnImpl) column);
      _properties.put(prop.getName(), prop);
    }

    _ctor = new ConstructorInfoBuilder()
        .withConstructorHandler(new IConstructorHandler() {
          @Override
          public Object newInstance(Object... args) {
            return new CachedDBObject(getOwnersType(), true);
          }
        }).build(this);

    _methods = new ArrayList<IMethodInfo>(Arrays.asList(_getMethod, _idMethod, _updateMethod, _deleteMethod, _countWithSqlMethod,
        _countMethod, _findWithSqlMethod, _findMethod, _findSortedMethod, _findPagedMethod,
        _findSortedPagedMethod));

    CommonServices.getEntityAccess().addEnhancementMethods(dbType, _methods);
    CommonServices.getEntityAccess().addEnhancementProperties(dbType, _properties, true);
  }

  @Override
  public List<? extends IPropertyInfo> getProperties() {
    List<IPropertyInfo> props = new ArrayList<IPropertyInfo>(_properties.values());
    props.addAll(_arrayProperties.get().values());
    props.add(_newProperty);
    return props;
  }

  @Override
  public IPropertyInfo getProperty(CharSequence propName) {
    if (propName.equals("_New")) {
      return _newProperty;
    }
    IPropertyInfo prop = _properties.get(propName.toString());
    if (prop == null) {
      prop = _arrayProperties.get().get(propName.toString());
    }
    return prop;
  }

  @Override
  public CharSequence getRealPropertyName(CharSequence propName) {
    for (String key : _properties.keySet()) {
      if (key.equalsIgnoreCase(propName.toString())) {
        return key;
      }
    }
    for (String key : _arrayProperties.get().keySet()) {
      if (key.equalsIgnoreCase(propName.toString())) {
        return key;
      }
    }
    return propName;
  }

  @Override
  public List<? extends IMethodInfo> getMethods() {
    return _methods;
  }

  @Override
  public IMethodInfo getMethod(CharSequence methodName, IType... params) {
    if ("fromID".equals(methodName) && params != null && params.length == 1 && params[0].equals(IJavaType.pLONG)) {
      return _getMethod;
    }
    if ("toID".equals(methodName) && (params == null || params.length == 0)) {
      return _idMethod;
    }
    if ("update".equals(methodName) && (params == null || params.length == 0)) {
      return _updateMethod;
    }
    if ("delete".equals(methodName) && (params == null || params.length == 0)) {
      return _deleteMethod;
    }
    if ("findWithSql".equals(methodName) && params != null && params.length == 1 && params[0].equals(IJavaType.STRING)) {
      return _findWithSqlMethod;
    }
    if ("find".equals(methodName) && params != null && params.length == 1 && params[0].equals(getOwnersType())) {
      return _findMethod;
    }
    if ("findSorted".equals(methodName) && params != null && params.length == 3 && params[0].equals(getOwnersType()) && TypeSystem.get(PropertyReference.class).isAssignableFrom(params[1]) && params[2].equals(IJavaType.pBOOLEAN)) {
      return _findSortedMethod;
    }
    if ("findPaged".equals(methodName) && params != null && params.length == 3 && params[0].equals(getOwnersType()) && params[1].equals(IJavaType.pINT) && params[2].equals(IJavaType.pINT)) {
      return _findPagedMethod;
    }
    if ("findSortedPaged".equals(methodName) && params != null && params.length == 5 && params[0].equals(getOwnersType()) && TypeSystem.get(PropertyReference.class).isAssignableFrom(params[1]) && params[2].equals(IJavaType.pBOOLEAN) && params[3].equals(IJavaType.pINT) && params[4].equals(IJavaType.pINT)) {
      return _findSortedPagedMethod;
    }
    if ("count".equals(methodName) && params != null && params.length == 1 && params[0].equals(getOwnersType())) {
      return _countMethod;
    }
    if ("countWithSql".equals(methodName) && params != null && params.length == 1 && params[0].equals(IJavaType.STRING)) {
      return _countWithSqlMethod;
    }
    return null;
  }

  @Override
  public IMethodInfo getCallableMethod(CharSequence method, IType... params) {
    return getMethod(method, params);
  }

  @Override
  public List<? extends IConstructorInfo> getConstructors() {
    return Collections.singletonList(_ctor);
  }

  @Override
  public IConstructorInfo getConstructor(IType... params) {
    if (params == null || params.length == 0) {
      return _ctor;
    }
    return null;
  }

  @Override
  public IConstructorInfo getCallableConstructor(IType... params) {
    return getConstructor(params);
  }

  private Map<String, IPropertyInfo> makeArrayProperties() {
    Map<String, IPropertyInfo> arrayProps = new HashMap<String, IPropertyInfo>();
    for (IDBArray dbArray : getOwnersType().getTable().getArrays()) {
      IPropertyInfo arrayProp = makeArrayProperty(dbArray);
      arrayProps.put(arrayProp.getName(), arrayProp);
    }
    return arrayProps;
  }

  private DBPropertyInfo makeProperty(DBColumnImpl column) {
    return new DBPropertyInfo(this, column);
  }

  private IPropertyInfo makeArrayProperty(IDBArray dbArray) {
    return new DBArrayPropertyInfo(this, dbArray);
  }

  @Override
  public IDBType getOwnersType() {
    return (IDBType) super.getOwnersType();
  }


}
