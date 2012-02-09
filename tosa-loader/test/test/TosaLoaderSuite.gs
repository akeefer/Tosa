package test

uses org.junit.runner.RunWith
uses org.junit.runners.Suite
uses org.junit.runners.Suite.SuiteClasses

@RunWith(Suite)
@SuiteClasses({
    tosa.impl.parser.data.DBDataValidatorTest,
    tosa.impl.util.StringSubstituterTest
})
public class TosaLoaderSuite {}
