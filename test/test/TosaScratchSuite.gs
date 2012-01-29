package test

uses gw.lang.reflect.TypeSystem
uses gw.lang.shell.Gosu
uses org.junit.runner.RunWith
uses org.junit.runners.Suite
uses org.junit.runners.model.InitializationError
uses tosa.loader.DBTypeLoader

uses java.util.ArrayList
uses java.lang.Class
uses junit.textui.TestRunner
uses org.junit.runners.Suite.SuiteClasses
uses junit.framework.Test
uses junit.framework.TestSuite
uses tosa.loader.SQLTypeInfoTest

@RunWith(Suite)
@SuiteClasses({SQLTypeInfoTest})
public class TosaScratchSuite {}
