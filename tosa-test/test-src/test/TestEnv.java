package test;

import gw.lang.reflect.TypeSystem;
import gw.lang.shell.Gosu;
import tosa.loader.DBTypeLoader;

public class TestEnv {
  private static boolean _inited = false;
  public static synchronized void maybeInit() {
    if (!_inited) {
      Gosu.init();
      TypeSystem.pushGlobalTypeLoader(new DBTypeLoader());
      _inited = true;
    }
  }

}
