package me.nikl.gamebox.utility;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.module.GameBoxGame;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.logging.Level;

/**
 * Created by nikl on 23.10.17.
 * <p>
 * Utility class for language related stuff
 */
public class FileUtility {

  /**
   * Copy all default language files to the plugin folder.
   * <p>
   * This method checks for every .yml in the language folder
   * whether it is already present in the plugins language folder.
   * If not it is copied.
   */
  public static void copyDefaultLanguageFiles() {
    URL main = GameBox.class.getResource("GameBox.class");
    try {
      JarURLConnection connection = (JarURLConnection) main.openConnection();
      JarFile jar = new JarFile(URLDecoder.decode(connection.getJarFileURL().getFile(), "UTF-8"));
      Plugin gameBox = Bukkit.getPluginManager().getPlugin("GameBox");
      for (Enumeration list = jar.entries(); list.hasMoreElements(); ) {
        JarEntry entry = (JarEntry) list.nextElement();
        if (entry.getName().split("/")[0].equals("language")) {
          String[] pathParts = entry.getName().split("/");
          if (pathParts.length < 2 || !entry.getName().endsWith(".yml")) {
            continue;
          }
          File file = new File(gameBox.getDataFolder().toString() + File.separatorChar + entry.getName());
          if (!file.exists()) {
            file.getParentFile().mkdirs();
            gameBox.saveResource(entry.getName(), false);
          }
        }
      }
      jar.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  public static boolean copyExternalResources(GameBox gameBox, GameBoxGame module) {
    try {
      JarFile jar = new JarFile(module.getJarFile());
      boolean foundDefaultLang = false;
      boolean foundConfig = false;
      for (Enumeration list = jar.entries(); list.hasMoreElements(); ) {
        JarEntry entry = (JarEntry) list.nextElement();
        String[] pathParts = entry.getName().split("/");
        String folderName = pathParts[0];
        if (folderName.equals("language")) {
          if (pathParts.length < 3 || !entry.getName().endsWith(".yml")) {
            continue;
          }
          // subfolder in language folder
          if (!pathParts[1].equalsIgnoreCase(module.getModuleID())) continue;
          String fileName = pathParts[pathParts.length - 1];
          if (fileName.equals("lang_en.yml")) {
            foundDefaultLang = true;
          }
          String gbPath = gameBox.getDataFolder().toString() + File.separatorChar
                  + "language" + File.separatorChar
                  + module.getModuleID() + File.separatorChar + fileName;
          File file = new File(gbPath);
          if (!file.exists()) {
            file.getParentFile().mkdirs();
            saveResourceToGBFolder(jar.getInputStream(entry), gbPath);
          }
        } else if (folderName.equalsIgnoreCase("games")) {
          if (entry.isDirectory()) continue;
          // only take resources from module folders
          if (pathParts.length < 3) {
            continue;
          }
          // check module folder
          if (!pathParts[1].equalsIgnoreCase(module.getModuleID())) {
            // resource of other module
            continue;
          }
          StringBuilder builder = new StringBuilder();
          for (int i = 2; i < pathParts.length; i++) {
            builder.append(pathParts[i]);
            if (i + 1 < pathParts.length) {
              builder.append(File.separatorChar);
            }
          }
          String fileName = builder.toString();
          if (fileName.equals("config.yml")) {
            foundConfig = true;
          }
          String gbPath = gameBox.getDataFolder().toString() + File.separatorChar
                  + "games" + File.separatorChar
                  + module.getModuleID() + File.separatorChar + fileName;
          File file = new File(gbPath);
          if (!file.exists()) {
            file.getParentFile().mkdirs();
            saveResourceToGBFolder(jar.getInputStream(entry), gbPath);
          }
        }
      }
      jar.close();
      if (!foundDefaultLang) {
        gameBox.warning(" Failed to locate default language file for the module '" + module.getModuleID() + "'");
        gameBox.warning(" " + jar.getName() + " is missing the file 'language/" + module.getModuleID() + "/lang_en.yml'");
      }
      if (!foundConfig) {
        gameBox.warning(" Failed to locate the configuration of the module '" + module.getModuleID() + "'");
        gameBox.warning(" " + jar.getName() + " is missing the file 'games/" + module.getModuleID() + "/config.yml'");
      }
      if (!foundConfig || !foundDefaultLang) {
        return false;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  static public InputStream getResource(File jarFile, String entry) {
    try {
      JarFile jar = new JarFile(jarFile);
      return jar.getInputStream(jar.getEntry(entry));
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Slightly adapted method from Bukkit...
   *
   * @param resource     the stream that is supposed to be saved
   * @param gbPath       wanted path for the resource in gameBox
   */
  static private void saveResourceToGBFolder(InputStream resource, String gbPath) {
    Plugin gameBox = Bukkit.getPluginManager().getPlugin("GameBox");
    File outFile = new File(gbPath);
    int lastIndex = gbPath.lastIndexOf(47);
    File outDir = new File(gameBox.getDataFolder(), gbPath.substring(0, lastIndex >= 0 ? lastIndex : 0));
    if (!outDir.exists()) {
      outDir.mkdirs();
    }
    if (outFile.exists()) return;
    try {
      OutputStream out = new FileOutputStream(outFile);
      byte[] buf = new byte[1024];
      int len;
      while ((len = resource.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
      out.close();
      resource.close();
    } catch (IOException var10) {
      gameBox.getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, var10);
    }
  }

  /**
   * Retrieves the line separator used in a file.
   * <p>
   * Using this method resolves a few problems users had with OSs that did not
   * convert the line separation chars in puzzle files.
   *
   * @param file file to check
   * @return used line separation
   */
  public static String retrieveLineSeparator(File file) {
    char current;
    String lineSeparator = "";
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      while (fis.available() > 0) {
        current = (char) fis.read();
        if ((current == '\n') || (current == '\r')) {
          lineSeparator += current;
          if (fis.available() > 0) {
            char next = (char) fis.read();
            if ((next != current)
                    && ((next == '\r') || (next == '\n'))) {
              lineSeparator += next;
            }
          }
          return lineSeparator;
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }

  /**
   * Collect all classes of the given type in the provided subfolder of the GameBox folder
   *
   * @param folder to check for classes
   * @param type classes checked for
   * @return list of found classes
   */
  public static List<Class<?>> getClasses(File folder, Class<?> type) {
    return getClasses(folder, null, type);
  }

  /**
   * Collect all classes of type `type` in the jar file with the provided name
   * in the given subfolder of the GameBox folder
   *
   * @param folder to check for classes
   * @param fileName look for jar with specific name
   * @param type classes checked for
   * @return list of found classes
   */
  public static List<Class<?>> getClasses(File folder, String fileName, Class<?> type) {
    List<Class<?>> list = new ArrayList<>();
    try {
      if (!folder.exists()) {
        return list;
      }
      FilenameFilter fileNameFilter = (dir, name) -> {
        if (fileName != null) {
          return name.endsWith(".jar") && name.replace(".jar", "")
                  .equalsIgnoreCase(fileName.replace(".jar", ""));
        }
        return name.endsWith(".jar");
      };
      File[] jars = folder.listFiles(fileNameFilter);
      if (jars == null) {
        return list;
      }
      for (File jar : jars) {
        list = gather(jar.toURI().toURL(), list, type);
      }
      return list;
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static List<Class<?>> gather(URL jar, List<Class<?>> list, Class<?> clazz) {
    if (list == null) {
      list = new ArrayList<>();
    }
    try (
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jar}, clazz.getClassLoader());
            JarInputStream jarInputStream = new JarInputStream(jar.openStream())
    ) {
      while (true) {
        JarEntry jarEntry = jarInputStream.getNextJarEntry();
        if (jarEntry == null) {
          break;
        }
        String name = jarEntry.getName();
        if (name == null || name.isEmpty()) {
          continue;
        }
        if (name.endsWith(".class")) {
          name = name.replace("/", ".");
          String className = name.substring(0, name.lastIndexOf(".class"));
          Class<?> jarEntryClass = classLoader.loadClass(className);
          if (clazz.isAssignableFrom(jarEntryClass)) {
            list.add(jarEntryClass);
          }
        }
      }
    } catch (ClassNotFoundException | IOException e) {
      e.printStackTrace();
    }
    return list;
  }

  public static List<Class<?>> getClassesFromJar(File jar, Class<?> clazz) {
    URL url = null;
    try {
      url = jar.toURI().toURL();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return gather(url, null, clazz);
  }

  private static void streamToFile(InputStream initialStream, File targetFile) throws IOException {
    byte[] buffer = new byte[initialStream.available()];
    initialStream.read(buffer);
    OutputStream outStream = new FileOutputStream(targetFile);
    outStream.write(buffer);
  }

  public static List<File> getAllJars(File folder) {
    if (!folder.exists()) {
      return new ArrayList<>();
    }
    FilenameFilter fileNameFilter = (dir, name) -> name.endsWith(".jar");
    return Arrays.asList(folder.listFiles(fileNameFilter));
  }

  public static InputStream getResource(String filename) throws IOException {
    if (filename == null || filename.isEmpty()) throw new IllegalArgumentException("Filename cannot be null or empty");
    URL url = GameBox.class.getClassLoader().getResource(filename);
    if (url == null) throw new IOException("Resource '" + filename + "' not found");
    URLConnection connection = url.openConnection();
    return connection.getInputStream();
  }

  public static void copyResource(String resourceName, File targetFile) throws IOException {
    streamToFile(getResource(resourceName), targetFile);
  }
}
