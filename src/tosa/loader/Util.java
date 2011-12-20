package tosa.loader;

import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaType;
import org.slf4j.profiler.Profiler;
import org.slf4j.profiler.ProfilerRegistry;

import java.sql.Types;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 12/29/10
 * Time: 10:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class Util {

  public static Profiler newProfiler(String name) {
    Profiler parent = ProfilerRegistry.getThreadContextInstance().get("_REQUEST");
    if (parent != null) {
      return parent.startNested(name);
    } else {
      return new Profiler(name);
    }
  }

}
