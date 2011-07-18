package tosa.loader;

import gw.lang.reflect.*;
import tosa.api.IDBConnection;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseAccessTypeInfo extends BaseTypeInfo {

  private IPropertyInfo _urlProperty;
  private Map<CharSequence, IPropertyInfo> _propertyMap;
  private List<IPropertyInfo> _propertyList;
  // TODO - AHK - Something around recreating tables and the like
  // TODO - AHK - An "instance" property to get a hold of the underlying IDatabase


  public DatabaseAccessTypeInfo(DatabaseAccessType type) {
    super(type);

    _urlProperty = createUrlProperty();

    mapProperties(_urlProperty);
  }

  private IPropertyInfo createUrlProperty() {
    return new PropertyInfoBuilder()
        .withName("Url")
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

  private void mapProperties(IPropertyInfo... properties) {
    _propertyList = new ArrayList<IPropertyInfo>();
    _propertyMap = new HashMap<CharSequence, IPropertyInfo>();
    for (IPropertyInfo property : properties) {
      _propertyList.add(property);
      _propertyMap.put(property.getName(), property);
    }

    _propertyList = Collections.unmodifiableList(_propertyList);
    _propertyMap = Collections.unmodifiableMap(_propertyMap);
  }

  @Override
  public List<? extends IMethodInfo> getMethods() {
    return Collections.emptyList();
  }

  @Override
  public IMethodInfo getMethod(CharSequence methodName, IType... params) {
    return null;
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
    return ((DatabaseAccessType) getOwnersType()).getDatabaseImpl().getUrl();
  }

  private void setJdbcUrl(String url) {
    ((DatabaseAccessType) getOwnersType()).getDatabaseImpl().setUrl(url);
  }

}

