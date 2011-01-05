package test;

import gw.config.CommonServices;
import gw.fs.IDirectory;
import gw.lang.init.ClasspathToGosuPathEntryUtil;
import gw.lang.init.GosuInitialization;
import gw.lang.init.GosuPathEntry;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.gs.IGosuClass;
import gw.util.GosuStringUtil;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 1/4/11
 * Time: 8:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class GosuTestRunner extends Runner {

  private BlockJUnit4ClassRunner _delegate;

  public GosuTestRunner(Class testClass) throws InitializationError {
    super();
    System.out.println("Class is " + testClass.getName());
    maybeInitGosu();
    try {
      GosuTestWrapper wrapper = (GosuTestWrapper) testClass.newInstance();
      String realTestName = wrapper.getWrappedTestName();
      System.out.println("Real test name is " + realTestName);
      IType testType = TypeSystem.getByFullName(realTestName);
      _delegate = new BlockJUnit4ClassRunner(((IGosuClass) testType).getBackingClass());
    } catch (InstantiationException e) {
      throw new InitializationError(e);
    } catch (IllegalAccessException e) {
      throw new InitializationError(e);
    }
  }

  @Override
  public Description getDescription() {
    return _delegate.getDescription();
  }

  @Override
  public void run(RunNotifier runNotifier) {
    _delegate.run(runNotifier);
  }

  public void maybeInitGosu() {
    if (!GosuInitialization.isInitialized()) {
      GosuInitialization.initializeRuntime(constructPathEntries());
    }
  }

  private List<? extends GosuPathEntry> constructPathEntries() {
    String classpath = System.getProperty("java.class.path");
    String[] classpathComponents = GosuStringUtil.split(classpath, ":");
    List<File> files = new ArrayList<File>();
    for (String s : classpathComponents) {
      File f = new File(s);
      if (!isJDKFile(f)) {
        files.add(f);
      }
    }
    List<GosuPathEntry> gosuPathEntries = new ArrayList<GosuPathEntry>();
    IDirectory rootDir = CommonServices.getFileSystem().getIDirectory(new File("/home/alan/Projects/Tosa/tosa"));
    gosuPathEntries.addAll(ClasspathToGosuPathEntryUtil.convertClasspathToGosuPathEntries(files));
    gosuPathEntries.add(new GosuPathEntry(rootDir, Collections.singletonList(rootDir.dir("test-src")), Collections.singletonList("tosa.loader.DBTypeLoader")));
    return gosuPathEntries;

//    List<GosuPathEntry> pathEntries = new ArrayList<GosuPathEntry>();
//    return pathEntries;
  }

  private boolean isJDKFile(File f) {
    return isParent(f, "ext", "lib", "jre") || isParent(f, "lib", "jre");
  }

  private boolean isParent(File f, String name1, String name2, String name3) {
    return isParent(f, name1) && isParent(f.getParentFile(), name2) && isParent(f.getParentFile().getParentFile(), name3);
  }

  private boolean isParent(File f, String name1, String name2) {
    return isParent(f, name1) && isParent(f.getParentFile(), name2);
  }

  private boolean isParent(File f, String name) {
    return f.getParentFile() != null && f.getParentFile().getName().equals(name);
  }
}
