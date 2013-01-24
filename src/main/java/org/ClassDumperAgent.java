package org;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ClassDumperAgent implements ClassFileTransformer {

  // directory where we would write .class files
  private static String dumpDir;
  // classes with name matching this pattern
  // will be dumped
  private static Pattern classes;

  public static void premain(String agentArgs, Instrumentation inst) {
    agentmain(agentArgs, inst);
  }

  public static void agentmain(String agentArgs, Instrumentation inst) {
    parseArgs(agentArgs);
    inst.addTransformer(new ClassDumperAgent(), true);

    // by the time we are attached, the classes to be
    // dumped may have been loaded already. So, check
    // for candidates in the loaded classes.
    Class[] classes = inst.getAllLoadedClasses();
    List<Class> candidates = new ArrayList<Class>();
    for (Class c : classes) {
      if (isCandidate(c.getName())) {
        candidates.add(c);
      }
    }
    try {
      // if we have matching candidates, then
      // retransform those classes so that we
      // will get callback to transform.
      if (! candidates.isEmpty()) {
        inst.retransformClasses(candidates.toArray(new Class[0]));
      }
    } catch (UnmodifiableClassException uce) {
    }
  }

  public byte[] transform(ClassLoader loader, String className,
                          Class redefinedClass, ProtectionDomain protDomain,
                          byte[] classBytes) {
    // check and dump .class file
    if (isCandidate(className)) {
      dumpClass(className, classBytes);
    }

    // we don't mess with .class file, just 
    // return null
    return null;
  }

  private static boolean isCandidate(String className) {
    // ignore array classes
    if (className.charAt(0) == '[') {
      return false;
    }

    // convert the class name to external name
    className = className.replace('/', '.');
    // check for name pattern match
    return classes.matcher(className).matches();
  }

  private static void dumpClass(String className, byte[] classBuf) {
    try {
      // create package directories if needed
      className = className.replace("/", File.separator);
      StringBuilder buf = new StringBuilder();
      buf.append(dumpDir);
      buf.append(File.separatorChar);
      int index = className.lastIndexOf(File.separatorChar);
      if (index != -1) {
        buf.append(className.substring(0, index));
      }
      String dir = buf.toString();
      new File(dir).mkdirs();

      // write .class file
      String fileName = dumpDir +
                        File.separator + className + ".class";
      FileOutputStream fos = new FileOutputStream(fileName);
      fos.write(classBuf);
      fos.close();
    } catch (Exception exp) {
      exp.printStackTrace();
    }
  }

  // parse agent args of the form arg1=value1,arg2=value2
  private static void parseArgs(String agentArgs) {
    if (agentArgs != null) {
      String[] args = agentArgs.split(",");
      for (String arg: args) {
        String[] tmp = arg.split("=");
        if (tmp.length == 2) {
          String name = tmp[0];
          String value = tmp[1];
          if (name.equals("dumpDir")) {
            dumpDir = value;
          } else if (name.equals("classes")) {
            classes = Pattern.compile(value);
          }
        }
      }
    }

    if (dumpDir == null) {
      dumpDir = ".";
    }

    if (classes == null) {
      classes = Pattern.compile(".\\*");
    }
  }
}