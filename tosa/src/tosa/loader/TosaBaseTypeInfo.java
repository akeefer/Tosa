package tosa.loader;

import gw.config.CommonServices;
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
public abstract class TosaBaseTypeInfo extends FeatureManagerTypeInfoBase {

  private List<IPropertyInfo> _propertyList;
  private List<IMethodInfo> _methodList;
  private List<IConstructorInfo> _constructorList;

  public TosaBaseTypeInfo(IType type) {
    super(type);
    _propertyList = new ArrayList<IPropertyInfo>();
    _methodList = new ArrayList<IMethodInfo>();
    _constructorList = new ArrayList<IConstructorInfo>();
  }

  protected void lockDataStructures() {
    _propertyList = Collections.unmodifiableList(_propertyList);
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

  protected void addProperty(IPropertyInfo property) {
    _propertyList.add(property);
  }

  protected void addMethod(IMethodInfo method) {
    _methodList.add(method);
  }

  protected void addConstructor(IConstructorInfo constructor) {
    _constructorList.add(constructor);
  }

  @Override
  public List<? extends IPropertyInfo> getDeclaredProperties() {
    return _propertyList;
  }

  @Override
  public List<? extends IMethodInfo> getDeclaredMethods() {
    return _methodList;
  }

  public List<? extends IConstructorInfo> getDeclaredConstructors() {
    return _constructorList;
  }
}

