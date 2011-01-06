package test;

import gw.config.CommonServices;
import gw.fs.IDirectory;
import gw.lang.init.ClasspathToGosuPathEntryUtil;
import gw.lang.init.GosuInitialization;
import gw.lang.init.GosuPathEntry;
import gw.util.GosuStringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alan
 * Date: 1/5/11
 * Time: 7:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class GosuInitHelper {

  public static List<? extends GosuPathEntry> constructPathEntriesFromSystemClasspath(String... globalTypeloadersToPush) {
    String classpath = System.getProperty("java.class.path");
    String[] classpathComponents = GosuStringUtil.split(classpath, ":");
    List<File> files = new ArrayList<File>();
    for (String s : classpathComponents) {
      File f = new File(s);
      if (!isJDKFile(f)) {
        files.add(f);
      }
    }

    List<? extends GosuPathEntry> originalPathEntries = ClasspathToGosuPathEntryUtil.convertClasspathToGosuPathEntries(files);
    List<GosuPathEntry> resultPathEntries = new ArrayList<GosuPathEntry>();
    for (GosuPathEntry originalEntry : originalPathEntries) {
      List<String> typeloaders = new ArrayList<String>();
      typeloaders.addAll(originalEntry.getTypeloaders());
      typeloaders.addAll(Arrays.asList(globalTypeloadersToPush));
      resultPathEntries.add(new GosuPathEntry(originalEntry.getRoot(), originalEntry.getSources(), typeloaders));
    }

    return resultPathEntries;
  }

  private static boolean isJDKFile(File f) {
    return isParent(f, "ext", "lib", "jre") || isParent(f, "lib", "jre");
  }

  private static boolean isParent(File f, String name1, String name2, String name3) {
    return isParent(f, name1) && isParent(f.getParentFile(), name2) && isParent(f.getParentFile().getParentFile(), name3);
  }

  private static boolean isParent(File f, String name1, String name2) {
    return isParent(f, name1) && isParent(f.getParentFile(), name2);
  }

  private static boolean isParent(File f, String name) {
    return f.getParentFile() != null && f.getParentFile().getName().equals(name);
  }
}
