package tosa.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  tosa.impl.parser.data.DBDataValidatorTest.class,
  tosa.loader.parser.DDLParserTest.class,
  tosa.loader.parser.SelectParsingBootstrapTest.class,
  tosa.loader.parser.TokenizerTest.class
})
public class TosaLoaderSuite {}
