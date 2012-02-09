package tosa.loader;

import gw.config.CommonServices;
import gw.internal.gosu.runtime.GosuRuntimeMethods;
import gw.lang.parser.IExpression;
import gw.lang.reflect.*;
import gw.lang.reflect.gs.IGosuClass;
import gw.lang.reflect.java.JavaTypes;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:37 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class TosaBaseTypeInfo extends FeatureManagerTypeInfoBase {

  private List<IPropertyInfo> _propertyList;
  private Map<CharSequence, IPropertyInfo> _propertyMap;
  private List<IMethodInfo> _methodList;
  private List<IConstructorInfo> _constructorList;

  public TosaBaseTypeInfo(IType type) {
    super(type);
    _propertyMap = new HashMap<CharSequence, IPropertyInfo>();
    _methodList = new ArrayList<IMethodInfo>();
    _constructorList = new ArrayList<IConstructorInfo>();
  }

  @Override
  public List<? extends IPropertyInfo> getDeclaredProperties() {
    return _propertyList;
  }

  @Override
  public List<? extends IMethodInfo> getDeclaredMethods() {
    return _methodList;
  }

  @Override
  public List<? extends IConstructorInfo> getDeclaredConstructors() {
    return _constructorList;
  }

  protected void lockDataStructures() {
    CommonServices.getEntityAccess().addEnhancementMethods(getOwnersType(), _methodList);
    CommonServices.getEntityAccess().addEnhancementProperties(getOwnersType(), _propertyMap, true);

    _propertyMap = Collections.unmodifiableMap(_propertyMap);
    _propertyList = Collections.unmodifiableList(new ArrayList<IPropertyInfo>(_propertyMap.values()));
    _methodList = Collections.unmodifiableList(_methodList);
    _constructorList = Collections.unmodifiableList(_constructorList);
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

  protected ParameterInfoBuilder param(String name, IType type, String description) {
    return new ParameterInfoBuilder().withName(name)
        .withType(type)
        .withDescription(description);
  }

  protected ParameterInfoBuilder param(String name, IType type, String description, IExpression defaultValue) {
    return new ParameterInfoBuilder().withName(name)
            .withType(type)
            .withDescription(description)
            .withDefValue(defaultValue);
  }

  protected void addProperty(IPropertyInfo property) {
    _propertyMap.put(property.getName(), property);
  }

  protected void addMethod(IMethodInfo method) {
    _methodList.add(method);
  }

  protected void addConstructor(IConstructorInfo constructor) {
    _constructorList.add(constructor);
  }
}

