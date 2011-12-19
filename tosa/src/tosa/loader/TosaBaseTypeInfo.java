package tosa.loader;

import gw.config.CommonServices;
import gw.internal.gosu.runtime.GosuRuntimeMethods;
import gw.lang.parser.IExpression;
import gw.lang.reflect.*;
import gw.lang.reflect.gs.IGosuClass;
import gw.lang.reflect.java.IJavaType;
import gw.lang.reflect.java.JavaTypes;
import sun.plugin2.util.ParameterNames;
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

  protected void addProperty(IPropertyInfo property) {
    _propertyMap.put(property.getName(), property);
  }

  protected void addMethod(IMethodInfo method) {
    _methodList.add(method);
  }

  protected void addConstructor(IConstructorInfo constructor) {
    _constructorList.add(constructor);
  }

  protected void delegateStaticMethods(IType typeToDelegateTo) {
    if (!typeToDelegateTo.isValid()) {
      ((IGosuClass) typeToDelegateTo).getParseResultsException().printStackTrace();
    }

    List<? extends IMethodInfo> methods = typeToDelegateTo.getTypeInfo().getMethods();
    Map<String, IMethodInfo> propertyGetters = new HashMap<String, IMethodInfo>();
    Map<String, IMethodInfo> propertySetters = new HashMap<String, IMethodInfo>();
    for (IMethodInfo methodInfo : methods) {
      if (!isBuiltInMethod(methodInfo) && methodInfo.isPublic() && methodInfo.isStatic()) {
        // TODO - AHK - Validate that the method has the correct first argument
        if (isPropertyGetter(methodInfo)) {
          propertyGetters.put(methodInfo.getDisplayName().substring(3), methodInfo);
        } else if (isPropertySetter(methodInfo)) {
          propertySetters.put(methodInfo.getDisplayName().substring(3), methodInfo);
        } else {
          addMethod(createDelegatedMethod(methodInfo));
        }
      }
    }

    for (Map.Entry<String, IMethodInfo> entry : propertyGetters.entrySet()) {
      addProperty(createDelegatedProperty(entry.getValue(), propertySetters.remove(entry.getKey())));
    }

    for (Map.Entry<String, IMethodInfo> entry : propertySetters.entrySet()) {
      addProperty(createDelegatedProperty(null, entry.getValue()));
    }
  }

  private static final HashSet<String> BUILT_IN_METHODS = new HashSet<String>(Arrays.asList(
          "@IntrinsicType()",
          "equals(java.lang.Object)",
          "hashCode()",
          "notify()",
          "notifyAll()",
          "toString()",
          "wait(long)",
          "wait()",
          "wait(long, int)"
          ));

  private boolean isBuiltInMethod(IMethodInfo method) {
    return BUILT_IN_METHODS.contains(method.getName());
  }

  private boolean isPropertyGetter(IMethodInfo method) {
    return method.getDisplayName().startsWith("get") && method.getParameters().length == 1 && method.getReturnType() != JavaTypes.pVOID();
  }

  private boolean isPropertySetter(IMethodInfo method) {
    return method.getDisplayName().startsWith("set") && method.getParameters().length == 2 && method.getReturnType().equals(JavaTypes.pVOID());
  }

  private IPropertyInfo createDelegatedProperty(IMethodInfo getter, IMethodInfo setter) {
    // TODO - AHK - Sanity check that they match up, are static, and have the right first parameter

    String name = (getter != null ? getter.getDisplayName().substring(3) : setter.getDisplayName().substring(3));
    IType propertyType = (getter != null ? getter.getReturnType() : setter.getParameters()[setter.getParameters().length - 1].getFeatureType());

    final IType ownersType = (getter != null ? getter.getOwnersType() : setter.getOwnersType());
    final String getterMethodName = (getter != null ? getter.getDisplayName() : null);
    final IType[] getterParameterTypes = (getter != null ? getParameterTypes(getter.getParameters()) : null);
    final String setterMethodName = (setter != null ? setter.getDisplayName() : null);
    final IType[] setterParameterTypes = (setter != null ? getParameterTypes(setter.getParameters()) : null);

    return new PropertyInfoBuilder()
            .withName(name)
            .withType(propertyType)
            .withReadable(getter != null)
            .withWritable(setter != null)
            .withDescription(getter != null ? getter.getDescription() : setter.getDescription())
            .withStatic(true)
            .withAccessor(new IPropertyAccessor() {
              @Override
              public Object getValue(Object o) {
                if (getterMethodName == null) {
                  throw new UnsupportedOperationException();
                }

                Object[] realArgs = new Object[1];
                realArgs[0] = getFirstArgForDelegatedMethods();
                return GosuRuntimeMethods.invokeMethodInfo(ownersType, getterMethodName, getterParameterTypes, null, realArgs);
              }

              @Override
              public void setValue(Object o, Object o1) {
                if (setterMethodName == null) {
                  throw new UnsupportedOperationException();
                }

                Object[] realArgs = new Object[2];
                realArgs[0] = getFirstArgForDelegatedMethods();
                realArgs[1] = o1;
                GosuRuntimeMethods.invokeMethodInfo(ownersType, setterMethodName, setterParameterTypes, null, realArgs);
              }
            }).build(this);
  }

  private IMethodInfo createDelegatedMethod(IMethodInfo method) {
    // TODO - AHK - Sanity check that the method has the right first argument

    IParameterInfo[] parameters = method.getParameters();
    IExpression[] defaultValues;
    if (method instanceof IOptionalParamCapable) {
      defaultValues = ((IOptionalParamCapable) method).getDefaultValueExpressions();
    } else {
      defaultValues = new IExpression[parameters.length];
    }
    ParameterInfoBuilder[] params = createDelegatedMethodParameters(parameters, defaultValues);

    final IType ownersType = method.getOwnersType();
    final String methodName = method.getDisplayName();
    final IType[] parameterTypes = getParameterTypes(parameters);

    return new MethodInfoBuilder()
            .withName(method.getDisplayName())
            .withStatic(true)
            .withDescription(method.getDescription())
            .withParameters(params)
            .withReturnType(substituteDelegatedReturnType(method.getReturnType()))
            .withCallHandler(
              new IMethodCallHandler() {
                @Override
                public Object handleCall(Object o, Object... objects) {
                  Object[] realArgs = new Object[objects.length + 1];
                  realArgs[0] = getFirstArgForDelegatedMethods();
                  System.arraycopy(objects, 0, realArgs, 1, objects.length);
                  return GosuRuntimeMethods.invokeMethodInfo(ownersType, methodName, parameterTypes, null, realArgs);
                }
              }
            ).build(this);
  }

  private IType[] getParameterTypes(IParameterInfo[] parameters) {
    final IType[] parameterTypes = new IType[parameters.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      parameterTypes[i] = parameters[i].getFeatureType();
    }
    return parameterTypes;
  }

  private ParameterInfoBuilder[] createDelegatedMethodParameters(IParameterInfo[] parameters, IExpression[] defaultValues) {
    ParameterInfoBuilder[] params = new ParameterInfoBuilder[parameters.length - 1];
    for (int i = 1; i < parameters.length; i++) {
      IParameterInfo param = parameters[i];
      // TODO - AHK - Default value
      params[i - 1] = new ParameterInfoBuilder()
              .withName(param.getName())
              .withDescription(param.getDescription())
              .withDefValue(defaultValues[i])
              .withType(substituteDelegatedParameterType(param.getFeatureType()));
    }
    return params;
  }

  protected IType substituteDelegatedParameterType(IType paramType) {
    return paramType;
  }

  protected IType substituteDelegatedReturnType(IType returnType) {
    return returnType;
  }

  protected Object getFirstArgForDelegatedMethods() {
    return null;
  }
}

