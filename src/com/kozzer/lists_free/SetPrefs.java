package com.kozzer.lists_free;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class SetPrefs extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	private Preferences UserPrefs;
	private Preference lstThemes;
	private Preference txtBackupPath;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName("ListPrefs");
        
		addPreferencesFromResource(R.xml.preferences);
        
		SharedPreferences settings = getSharedPreferences("ListPrefs", 0);
		UserPrefs = new Preferences();
		UserPrefs.SelectedTheme = settings.getString("themePref", "dark");
		UserPrefs.ShowNumbering = settings.getBoolean("numberingPref", false);
		
		//Get the default external storage path
    	DataSQL listDAL = new DataSQL(this); 
		String backupPathPref = settings.getString("backupPathPref", null);
		if (backupPathPref == null || backupPathPref.equals("")){
			UserPrefs.BackupPath = listDAL.getDefaultBackupPath(getApplicationContext());
		} else {
			UserPrefs.BackupPath = backupPathPref;
		}
		listDAL.closeDatabase();
		listDAL = null;
		
		lstThemes = findPreference("themePref");
		lstThemes.setSummary("You currently have the " + UserPrefs.SelectedTheme.toUpperCase() + " theme active.");
		
		txtBackupPath = findPreference("backupPathPref");
		txtBackupPath.setSummary(UserPrefs.BackupPath);
		
		settings.registerOnSharedPreferenceChangeListener(this);
		
		Preference backupPref = (Preference) findPreference("runBackupPref");
		backupPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
		             public boolean onPreferenceClick(Preference preference) {
		            	 askForBackup();
		            	 return true;
		             }
		         });
		
		Preference restorePref = (Preference) findPreference("runRestorePref");
		restorePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
		             public boolean onPreferenceClick(Preference preference) {
		            	 askForRestore();
		            	 return true;
		             }
		         });
		
		Preference appInfoPref = (Preference) findPreference("appInfoPref");
		appInfoPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
		             public boolean onPreferenceClick(Preference preference) {
		            	 showAppInfo();
		            	 return true;
		             }
		         });
    }
	
	@Override
	protected void onStop(){
		super.onStop();
	}
	
	public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {

		if (key.equals("themePref")){
			lstThemes.setSummary(("You currently have the " + preferences.getString(key, "[default]").toUpperCase() + " theme active."));
			displayMessage("Active theme changed to " + preferences.getString(key, "[default]").toUpperCase());
			
		} else if (key.equals("numberingPref")){
			//Nothing really to do here except display message
			displayMessage("List items displayed " + (preferences.getBoolean(key, false) ? "WITH" : "WITHOUT") + " numbering.");
			
		} else if (key.equals("backupPathPref")){
			Log.d("kozzer", key + " backup path changed to " + preferences.getString(key, "[default path]"));
			txtBackupPath.setSummary(preferences.getString(key, "[default path]"));
			displayMessage("Backup path changed to \n'" + preferences.getString(key, "[default path]") + "'");

		} else if (key.equals("runBackupPref")){
			Log.d("kozzer", "Run Backup");
			askForBackup();
		
		} else if (key.equals("runRestorePref")){
			Log.d("kozzer", "Run Restore");
			askForRestore();
		
		} else if (key.equals("appInfoPref")){
			Log.d("kozzer", "App info");
			showAppInfo();
			
		} else {
			Log.d("kozzer", key + " ??? changed to " + preferences.getString(key, "[default path]"));	
		}
	}
	
	@SuppressLint("NewApi")
	public void showAppInfo(){
		AlertDialog.Builder alert;
		if (UserPrefs.SelectedTheme != null && UserPrefs.SelectedTheme.equals("light")){
			Log.d("kozzer", "Dialog for Light theme");
			alert = (android.os.Build.VERSION.SDK_INT < 11) ? new AlertDialog.Builder(this) : new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
		} else {
			Log.d("kozzer", "Dialog for Dark theme (default)");
			alert = (android.os.Build.VERSION.SDK_INT < 11) ? new AlertDialog.Builder(this) : new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK);			
		}
		//Configure App Info screen
		alert.setMessage(getString(R.string.about_description));
		alert.setIcon(R.drawable.list);
		alert.setTitle("Lists!");
		AlertDialog dialog = alert.create();
		dialog.setButton("Whatever", new DialogInterface.OnClickListener() {  
	  	      public void onClick(DialogInterface dialog, int which) {  
	  	          return;  
	  	      } });  
		dialog.show();
    }
   
	private void displayMessage(String theMessage){
		Context context = getApplicationContext();
		CharSequence text = theMessage;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
    private void askForBackup(){
    	Log.d("kozzer", "In SetPrefs.askForBackup()");
    	if (UserPrefs.BackupPath.contains("ERROR:")){
    		displayMessage("Cannot do back up - external storage not available.");
    	} else {
	    	AlertDialog.Builder alert = GetAlertDialog("This will overwrite any previous back up, and cannot be undone.", android.R.drawable.ic_menu_save);
	    	alert.setTitle("Back Up Lists");
	    	alert.setPositiveButton("Do It!", new OnClickListener(){
	       		public void onClick(DialogInterface dialog, int whichButton){
	       			doBackupRestore(true);
	       		}
	    	});
	    	alert.setNegativeButton("Nah", new OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					//nothing - cancel				
				}
	    	});
	    	AlertDialog dialog = alert.create();
	    	dialog.show();
    	}
    }
    
    private void askForRestore(){
    	Log.d("kozzer", "In SetPrefs.askForRestore()");
    	if (UserPrefs.BackupPath.contains("ERROR:")){
    		displayMessage("Cannot do restore - external storage not available.");
    	} else {
	    	AlertDialog.Builder alert = GetAlertDialog("The back up will overwrite your lists, and cannot be undone.", android.R.drawable.ic_menu_rotate);
	    	alert.setTitle("Restore Lists");
	    	alert.setPositiveButton("Do It!", new OnClickListener(){
	       		public void onClick(DialogInterface dialog, int whichButton){
	       			doBackupRestore(false);
	    		}
	    	});
	    	alert.setNegativeButton("Nah", new OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					//nothing - cancel				
				}
	    	});
	    	AlertDialog dialog = alert.create();
	    	dialog.show();
	    	}
    	}
    
    @SuppressWarnings("unused")
	private void doBackupRestore(boolean isBackup){
		String ret = "";
		String title = "";
		String message = "";
		DataSQL listDAL = new DataSQL(this);
		if (isBackup){
			ret = listDAL.createBackup(UserPrefs.BackupPath);
			title = "Back up created!";
			message = "Lists backed up successfully! \n<|:^)";
		} else {
			ret = listDAL.restoreBackup(UserPrefs.BackupPath);
			title = "Lists restored!";
			message = "Lists restored from back up successfully! \n<|:^)";
		}
		if (ret == "success"){
			//Display success message
			displayMessage(message);
		} else {
			//Display failure message
			displayMessage("Back up / Restore failed.  It\'s probably all your fault! >:^( \n\n" + ret);
		}
  	
    }
    
	@SuppressLint("NewApi")
	private AlertDialog.Builder GetAlertDialog(String message, int icon){
		AlertDialog.Builder alert;
		//alert = new AlertDialog.Builder(this);
		if (UserPrefs.SelectedTheme != null && UserPrefs.SelectedTheme.equals("light")){
			Log.d("kozzer", "Dialog for Light theme");
			alert = (android.os.Build.VERSION.SDK_INT < 11) ? new AlertDialog.Builder(this) : new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
		} else {
			Log.d("kozzer", "Dialog for Dark theme (default)");
			alert = (android.os.Build.VERSION.SDK_INT < 11) ? new AlertDialog.Builder(this) : new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK);			
		}
		alert.setTitle("Lists!");
		alert.setMessage(message);
		alert.setIcon(icon);
		return alert;
	}

}
