package azuka.com.learndsa.Preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Ivana Situmorang on 12/24/2018.
 */
public class Preferences {
    private static Preferences preferences;
    private SharedPreferences sharedPreferences;
    private static String FILENAME = "PREF";
    Editor editor;

    private Preferences(Context context){
        //sharedPreferences = context.getSharedPreferences(FILENAME, 0);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static Preferences getInstance(Context context){
        if (preferences == null){
            Log.v("PREFERENCES","NEW PREFERENCES");
            preferences = new Preferences(context);
        }
        return preferences;
    }

    public void put(String key, String value){
        editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String get(String key){
        return sharedPreferences.getString(key, "test");
    }

    public void clear(){
        editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void remove(){
        editor = sharedPreferences.edit();
        editor.remove(FILENAME);
        editor.apply();
    }
}
