package tosa.loader;

import gw.lang.reflect.*;
import tosa.dbmd.DatabaseImpl;

import java.security.PrivateKey;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:37 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class TosaBaseTypeInfo extends BaseTypeInfo {

  private Map<CharSequence, IPropertyInfo> _propertyMap;
  private List<IPropertyInfo> _propertyList;
  private List<IMethodInfo> _methodList;


  public TosaBaseTypeInfo(IType type) {
    super(type);
    _propertyList = new ArrayList<IPropertyInfo>();
    _propertyMap = new HashMap<CharSequence, IPropertyInfo>();
    _methodList = new ArrayList<IMethodInfo>();
  }

  protected void lockDataStructures() {
    _propertyList = Collections.unmodifiableList(_propertyList);
    _propertyMap = Collections.unmodifiableMap(_propertyMap);
    _methodList = Collections.unmodifiableList(_methodList);
  }

  protected static enum Writeable {
    ReadOnly(true, false), WriteOnly(false, true), ReadWrite(true, true);

    private Writeable(boolean readable, boolean writeable) {
      _readable = readable;
      _writeable = writeable;
    }

    private boolean _readable;
    private boolean _writeable;
  }

  protected static enum Modifiers {
    PublicStatic(true), PublicInstance(false);

    private Modifiers(boolean aStatic) {
      _static = aStatic;
    }

    private boolean _static;
  }

  protected void createProperty(String name, IType type, Modifiers modifiers, Writeable writeable, String description, IPropertyAccessor accessor) {
    addProperty(new PropertyInfoBuilder()
        .withName(name)
        .withType(type)
        .withStatic(modifiers._static)
        .withReadable(writeable._readable)
        .withWritable(writeable._writeable)
        .withDescription(description)
        .withAccessor(accessor)
        .build(this));
  }

  protected void createMethod(String name, ParameterInfoBuilder[] parameters, IType returnType, Modifiers modifiers,
                                     String description, IMethodCallHandler callHandler) {
    addMethod(new MethodInfoBuilder()
        .withName(name)
        .withParameters(parameters)
        .withReturnType(returnType)
        .withStatic(modifiers._static)
        .withDescription(description)
        .withCallHandler(callHandler)
        .build(this));
  }

  protected ParameterInfoBuilder[] params(ParameterInfoBuilder... params) {
    return params;
  }

  protected void addProperty(IPropertyInfo property) {
    _propertyList.add(property);
    _propertyMap.put(property.getName(), property);
  }

  protected void addMethod(IMethodInfo method) {
    _methodList.add(method);
  }

  @Override
  public List<? extends IMethodInfo> getMethods() {
    return _methodList;
  }

  @Override
  public IMethodInfo getMethod(CharSequence methodName, IType... params) {
    return ITypeInfo.FIND.method(_methodList, methodName, params);
  }

  @Override
  public IMethodInfo getCallableMethod(CharSequence method, IType... params) {
    return ITypeInfo.FIND.callableMethod(_methodList, method, params);
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

}

