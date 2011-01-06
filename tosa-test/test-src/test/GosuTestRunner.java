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
    try {
      ITestWrapper wrapper = (ITestWrapper) testClass.newInstance();
      if (!GosuInitialization.isInitialized()) {
        wrapper.initializeGosu();
      }
      String realTestName = wrapper.getWrappedTestName();
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
}
