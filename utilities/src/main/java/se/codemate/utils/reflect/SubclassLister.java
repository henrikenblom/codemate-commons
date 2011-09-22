package se.codemate.utils.reflect;

import sun.net.www.protocol.file.FileURLConnection;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.Collator;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SubclassLister {

    private static final String CLASS_NAME_ENDING = ".class";

    /**
     * @param <T>                 the class type.
     * @param superClass          the class which the resulting classes must implement
     *                            or extend.
     * @param lookInThesePackages an optional collection of which java packages
     *                            to search in. If null is specified then all packages are searched.
     * @return all classes (in the class path) which extends or implements a certain class.
     * @since 1.5
     */
    public static <T> Collection<Class<? extends T>> getSubclasses(Class<? extends T> superClass, Collection<String> lookInThesePackages) {

        Collection<Class<? extends T>> classes = new TreeSet<Class<? extends T>>(new Comparator<Class<? extends T>>() {

            Collator collator = Collator.getInstance();

            public int compare(Class<? extends T> class1, Class<? extends T> class2) {
                return collator.compare(class1.getName(), class2.getName());
            }

        });

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (String lookInThisPackage : lookInThesePackages) {
            URL url = classLoader.getResource(lookInThisPackage.replace('.', File.separatorChar));
            if (url != null) {
                try {
                    URLConnection connection = url.openConnection();
                    if (FileURLConnection.class.isInstance(connection)) {
                        File file = new File(url.getFile());
                        scanDirectory(classes, superClass, lookInThisPackage, file);
                    } else if (JarURLConnection.class.isInstance(connection)) {
                        JarFile file = ((JarURLConnection) connection).getJarFile();
                        Enumeration<JarEntry> entries = file.entries();
                        while (entries.hasMoreElements()) {
                            String entry = entries.nextElement().getName().replace(File.separatorChar, '.');
                            if (entry.startsWith(lookInThisPackage) && entry.endsWith(".class")) {
                                tryCollectClass(classes, superClass, trimClassNameEnding(entry));
                            }
                        }
                    }
                } catch (IOException exception) {
                    // no-op
                }
            }
        }

        return Collections.unmodifiableCollection(classes);

    }

    private static <T> void scanDirectory(Collection<Class<? extends T>> classes, Class<? extends T> superClass, String classPackage, File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                scanDirectory(classes, superClass, appendToPackage(classPackage, file.getName()), file);
            } else {
                tryCollectClass(classes, superClass, appendToPackage(classPackage, trimClassNameEnding(file.getName())));
            }
        }
    }

    private static String appendToPackage(String classPackage, String child) {
        classPackage = classPackage == null ? "" : classPackage;
        if (classPackage.length() > 0) {
            classPackage += ".";
        }
        classPackage += child;
        return classPackage;
    }

    private static String trimClassNameEnding(String className) {
        if (className.endsWith(CLASS_NAME_ENDING)) {
            className = className.substring(0, className.length() - CLASS_NAME_ENDING.length());
        }
        return className;
    }

    private static <T> void tryCollectClass(Collection<Class<? extends T>> classes, Class<? extends T> superClass, String className) {
        try {
            Class<? extends T> subClass = Class.forName(className).asSubclass(superClass);
            if (!Modifier.isInterface(subClass.getModifiers()) && !Modifier.isAbstract(subClass.getModifiers()) && superClass.isAssignableFrom(subClass)) {
                classes.add(subClass);
            }
        } catch (ClassCastException exception) {
            // NO-OP
        } catch (ClassNotFoundException exception) {
            // NO-OP
        }
    }

}