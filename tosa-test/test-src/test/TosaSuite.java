package test;

import gw.lang.reflect.IHasJavaClass;
import gw.lang.reflect.TypeSystem;
import gw.lang.shell.Gosu;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import tosa.loader.DBTypeLoader;

import java.util.ArrayList;

@RunWith(TosaSuite.class)
public class TosaSuite extends Suite {

  public TosaSuite(Class clazz) throws InitializationError {
    super(init(clazz), getAllTestClasses());
  }

  private static Class init(Class clazz) {
    TestEnv.maybeInit();
    return clazz;
  }

  public static Class[] getAllTestClasses() {
    return classesFor(
      "tosa.impl.ReverseFkEntityCollectionImplTest",
      "tosa.loader.DBTypeInfoTest",
      "tosa.loader.SQLTypeInfoTest",
      "tosa.loader.parser.SelectParsingBootstrapTest",
      "tosa.loader.parser.TokenizerTest",
      "tosa.loader.parser.mysql.MySQL51SQLParserTest"
    );
  }

  private static Class[] classesFor(String... types) {
    ArrayList<Class> classes = new ArrayList<Class>();
    for (String typeName : types) {
      IHasJavaClass type = (IHasJavaClass) TypeSystem.getByFullName(typeName);
      classes.add(type.getBackingClass());
    }
    return classes.toArray(new Class[classes.size()]);
  }
}
