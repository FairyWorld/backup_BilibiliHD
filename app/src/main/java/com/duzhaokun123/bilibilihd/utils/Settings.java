package com.duzhaokun123.bilibilihd.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

public class Settings {
    private static LoginUserInfoMap loginUserInfoMap;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static boolean inited = false;

    private static final int INT = 0;
    private static final int FLOAT = 1;
    private static final int STRING = 2;
    private static final int BOOLEAN = 3;
    private static final int STRING_SET = 4;

    private static final String TAG = "SettingsManager";

    public static boolean isUninited() {
        return !inited;
    }

    @SuppressLint("CommitPrefEdits")
    public static void init(Context context) {
        Settings.sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        Settings.editor = sharedPreferences.edit();

        Settings.firstStart = sharedPreferences.getBoolean("firstStart", true);
        Settings.layout.column = sharedPreferences.getInt("column", 0);
        Settings.layout.columnLand = sharedPreferences.getInt("column_land", 0);
        Settings.download.downloader = sharedPreferences.getInt("downloader", Download.GLIDE_CACHE_FIRST);

        Settings.inited = true;
    }

    public static final Layout layout = new Layout();
    public static final Download download = new Download();

    public static LoginUserInfoMap getLoginUserInfoMap(Context context) {
        if (loginUserInfoMap == null) {
            File file = new File(context.getFilesDir(), "LoginUserInfoMap");
            if (file.exists()) {
                FileInputStream fileInputStream = null;
                ObjectInputStream objectInputStream = null;
                try {
                    fileInputStream = new FileInputStream(file);
                    objectInputStream = new ObjectInputStream(fileInputStream);
                    loginUserInfoMap = (LoginUserInfoMap) objectInputStream.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    loginUserInfoMap = new LoginUserInfoMap();
                } finally {
                    try {
                        if (objectInputStream != null) {
                            objectInputStream.close();
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                loginUserInfoMap = new LoginUserInfoMap();
            }
        }
        return loginUserInfoMap;
    }

    public static void saveLoginUserInfoMap(Context context) {
        if (loginUserInfoMap != null) {
            File file = new File(context.getFilesDir(), "LoginUserInfoMap");
            FileOutputStream fileOutputStream = null;
            ObjectOutputStream objectOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(file);
                objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(loginUserInfoMap);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (objectOutputStream != null) {
                        objectOutputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void save(String key, int type, Object value) {
        if (editor == null) {
            Log.e(TAG, "editor is null, settings cannot be saved");
        } else {
            switch (type) {
                case INT:
                    editor.putInt(key, Integer.parseInt(value.toString()));
                    break;
                case FLOAT:
                    editor.putFloat(key, Float.parseFloat(value.toString()));
                    break;
                case BOOLEAN:
                    editor.putBoolean(key, Boolean.parseBoolean(value.toString()));
                    break;
                case STRING:
                    editor.putString(key, (String) value);
                    break;
                case STRING_SET:
                    editor.putStringSet(key, (Set<String>) value);
                    break;
                default:
                    Log.wtf(TAG, type + " is not defined");
                    break;
            }
            editor.apply();
        }
    }

    private static boolean firstStart = true;

    public static boolean isFirstStart() {
        return firstStart;
    }

    public static void setFirstStart(boolean firstStart) {
        Settings.firstStart = firstStart;
        save("firstStart", BOOLEAN, firstStart);
    }

    public static class Layout {
        private int column = 0;
        private int columnLand = 0;


        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
            save("column", INT, column);
        }

        public int getColumnLand() {
            return columnLand;
        }
        public void setColumnLand(int columnLand) {
            this.columnLand = columnLand;
            save("column_land", INT, columnLand);
        }

    }

    public static class Download {
        public static final int DOWNLOAD_MANAGER = 1;
        public static final int GLIDE_CACHE_FIRST = 2;

        private int downloader = GLIDE_CACHE_FIRST;


        public int getDownloader() {
            return downloader;
        }
        public void setDownloader(int downloader) {
            this.downloader = downloader;
            save("downloader", INT, downloader);
        }

    }
}
