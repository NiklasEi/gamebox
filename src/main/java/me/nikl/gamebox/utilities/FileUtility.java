/*
 * GameBox
 * Copyright (C) 2019  Niklas Eicker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.nikl.gamebox.utilities;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.module.NewGameBoxModule;
import me.nikl.gamebox.module.local.LocalModule;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

/**
 * @author Niklas Eicker
 */
public class FileUtility {

    public static void copyDefaultLanguageFiles() {
        URL main = GameBox.class.getResource("GameBox.class");
        try {
            JarURLConnection connection = (JarURLConnection) main.openConnection();
            GameBox gameBox = (GameBox) Bukkit.getPluginManager().getPlugin("GameBox");
            copyDefaultLanguageFiles(URLDecoder.decode(connection.getJarFileURL().getFile(), "UTF-8"), gameBox.getLanguageDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyDefaultLanguageFiles(NewGameBoxModule module, LocalModule localModule) {
        try {
            String jarFile = URLDecoder.decode(localModule.getModuleJar().getAbsolutePath(), "UTF-8");
            copyDefaultLanguageFiles(jarFile, module.getLanguageFolder());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Copy all default language files to the language folder.
     *
     * This method looks for .yml files in the language folder inside the jar
     * and checks whether they are already present in the language folder.
     * If not they are copied.
     */
    private static void copyDefaultLanguageFiles(String jarFile, File languageFolder) {
        try {
            JarFile jar = new JarFile(jarFile);
            for (Enumeration list = jar.entries(); list.hasMoreElements(); ) {
                JarEntry entry = (JarEntry) list.nextElement();
                if (entry.getName().split("/")[0].equals("language")) {
                    String[] pathParts = entry.getName().split("/");
                    if (pathParts.length < 2 || (!entry.getName().endsWith(".yml") && !entry.getName().endsWith(".yaml"))) {
                        continue;
                    }
                    File file = new File(languageFolder, pathParts[pathParts.length - 1]);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        streamToFile(jar.getInputStream(entry), file);
                    }
                }
            }
            jar.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
