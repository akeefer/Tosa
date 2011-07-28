package tosa.loader;

import gw.internal.gosu.runtime.GosuRuntimeMethods;
import gw.lang.reflect.*;
import gw.lang.reflect.java.IJavaArrayType;
import gw.lang.reflect.java.IJavaType;
import tosa.api.IDatabase;
import tosa.dbmd.DatabaseImpl;

import java.awt.geom.IllegalPathStateException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseAccessTypeInfo extends TosaBaseTypeInfo {

  // TODO - AHK - Something around recreating tables and the like
  // TODO - AHK - An "instance" property to get a hold of the underlying IDatabase


  public DatabaseAccessTypeInfo(IDatabaseAccessType type) {
    super(type);

    // All the static methods on this type are actually found on the DatabaseAccessTypeDelegate class
    delegateStaticMethods(TypeSystem.getByFullName("tosa.loader.DatabaseAccessTypeDelegate"));

    lockDataStructures();
  }

  private IDatabase getDb() {
    return ((IDatabaseAccessType) getOwnersType()).getDatabaseInstance();
  }

  private void delegateStaticMethods(IType typeToDelegateTo) {
    List<? extends IMethodInfo> methods = typeToDelegateTo.getTypeInfo().getMethods();
    Map<String, IMethodInfo> propertyGetters = new HashMap<String, IMethodInfo>();
    Map<String, IMethodInfo> propertySetters = new HashMap<String, IMethodInfo>();
    for (IMethodInfo methodInfo : methods) {
      if (!isBuiltInMethod(methodInfo)) {
        // TODO - AHK - Validate that the method has the necessary number of args and is static
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
    return method.getDisplayName().startsWith("get") && method.getParameters().length == 1 && method.getReturnType() != IJavaType.pVOID;
  }

  private boolean isPropertySetter(IMethodInfo method) {
    return method.getDisplayName().startsWith("set") && method.getParameters().length == 2 && method.getReturnType().equals(IJavaType.pVOID);
  }

  private IPropertyInfo createDelegatedProperty(IMethodInfo getter, IMethodInfo setter) {
    // TODO - AHK - Sanity check that they match up

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
                realArgs[0] = getDb();
                return GosuRuntimeMethods.invokeMethodInfo(ownersType, getterMethodName, getterParameterTypes, null, realArgs);
              }

              @Override
              public void setValue(Object o, Object o1) {
                if (setterMethodName == null) {
                  throw new UnsupportedOperationException();
                }

                Object[] realArgs = new Object[2];
                realArgs[0] = getDb();
                realArgs[1] = o1;
                GosuRuntimeMethods.invokeMethodInfo(ownersType, setterMethodName, setterParameterTypes, null, realArgs);
              }
            }).build(this);
  }

  private IMethodInfo createDelegatedMethod(IMethodInfo method) {
    IParameterInfo[] parameters = method.getParameters();
    ParameterInfoBuilder[] params = createDelegatedMethodParameters(parameters);

    final IType ownersType = method.getOwnersType();
    final String methodName = method.getDisplayName();
    final IType[] parameterTypes = getParameterTypes(parameters);

    return new MethodInfoBuilder()
            .withName(method.getDisplayName())
            .withStatic(true)
            .withDescription(method.getDescription())
            .withParameters(params)
            .withReturnType(method.getReturnType())
            .withCallHandler(
              new IMethodCallHandler() {
                @Override
                public Object handleCall(Object o, Object... objects) {
                  Object[] realArgs = new Object[objects.length + 1];
                  realArgs[0] = getDb();
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

  private ParameterInfoBuilder[] createDelegatedMethodParameters(IParameterInfo[] parameters) {
    ParameterInfoBuilder[] params = new ParameterInfoBuilder[parameters.length - 1];
    for (int i = 1; i < parameters.length; i++) {
      IParameterInfo param = parameters[i];
      params[i - 1] = new ParameterInfoBuilder().like(param);
    }
    return params;
  }
}

