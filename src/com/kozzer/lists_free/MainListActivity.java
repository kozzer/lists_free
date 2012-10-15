package com.kozzer.lists_free;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class MainListActivity extends Activity {
	
	private DragAndDropListView mainListView;
	private ArrayList<UserList> mainList;
	private DataSQL listDAL;
	private UserList selectedList;
	private UserListAdapter listAdapter;
	private Preferences selectedPreferences;
	private int selectedIndex;
	private ArrayList<UserAction> undoActions;
	private final int PULSE_DURATION = 20;
	private static final int VOICE_REQUEST_CODE = 1234;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d("kozzer", "*********************");
    	Log.d("kozzer", "*********************");
    	Log.d("kozzer", "______START LOAD_____");
    	
        //Get and set the theme
		ensureDALExists();
		
		if (selectedPreferences == null){
			SharedPreferences settings = getSharedPreferences("ListPrefs", 0);
			selectedPreferences = new Preferences();
			selectedPreferences.SelectedTheme = settings.getString("themePref", "dark");
			selectedPreferences.ShowNumbering = settings.getBoolean("numberingPref", false);
			String backupPathPref = settings.getString("backupPathPref", null);
			
			if (backupPathPref == null || backupPathPref.equals("") || backupPathPref.contains("ERROR:")){
				selectedPreferences.BackupPath = listDAL.getDefaultBackupPath(getApplicationContext());

				if (selectedPreferences.BackupPath.contains("ERROR:")) {
					showAlert("Backup Path", "There was an error getting your device''s external storage path!\n\nYou will not be able to back up or restore your lists!\n\nPlease make sure that your device is not connected to a PC via USB and restart Lists!");
				}
			} else {
				selectedPreferences.BackupPath = backupPathPref;
			}
		}
		
		if (selectedPreferences != null && selectedPreferences.SelectedTheme.length() > 0){	
        	if (selectedPreferences.SelectedTheme.equals("light")){
        		Log.d("kozzer", "Light theme activated");
        		setTheme(R.style.NoTitleLight);
        	} else if (selectedPreferences.SelectedTheme.equals("dark")){
        		Log.d("kozzer", "Dark theme activated");
        		setTheme(R.style.NoTitleDark);
        	} else {
        		//Default to dark
        		Log.d("kozzer", "NO THEME ACTIVATED, defaulted to dark");
        		setTheme(R.style.NoTitleDark);
        	}
        }
		
        //Instantiates the activity layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_list);
        
        //Set app title text in TextView
        TextView titleText = (TextView) findViewById(R.id.main_title_text);
        titleText.setText(R.string.app_name);
        
        //Populate the main list view
        mainListView = (DragAndDropListView) findViewById(R.id.main_list);
        populateMainList();
        
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
        	public void onItemClick(AdapterView<?> arg0, View view, int position, long id){
        		//Pulse
         		((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(PULSE_DURATION);
        		//Open list
        		onListItemClick(view, position, id);
        	}
        });
        
        mainListView.setOnItemLongClickListener(new OnItemLongClickListener(){
       	 	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id){
       	 		Log.d("kozzer", "In long click listener");
         		//Display pop up
       		 	askToDeleteOrEditList(view, position, id);
       		 	return true;
       	 	}
        });
        
   	    //Set click listener for voice button
   	    ImageView startVoice = (ImageView) findViewById(R.id.main_menu_voice);
   	    startVoice.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
        		//Pulse
         		((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(PULSE_DURATION);
   	    		//Start listening
	    		StartVoice();				
			}
   	    });
        
   	    //Set click listener for add new item menu option
   	    ImageView addNewItem = (ImageView) findViewById(R.id.main_menu_add);
   	    addNewItem.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
        		//Pulse
         		((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(PULSE_DURATION);
   	    		//Add new list
	    		ShowEntryDialog();				
			}
   	    });
   	    
   	    //Set click listener for delete marked items menu option
   	    ImageView undoAction = (ImageView) findViewById(R.id.main_menu_undo);
   	    	undoAction.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
        		//Pulse
         		((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(PULSE_DURATION);
   	    		//Ask to delete marked items
         		askToUndo();				
			}
   	    });   	    

   	   	//Set click listener for delete marked items menu option
   	    ImageView moreInfo = (ImageView) findViewById(R.id.main_menu_more);
   	    	moreInfo.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
        		//Pulse
         		((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(PULSE_DURATION);
   	    		//Ask to delete marked items
				Intent prefIntent = new Intent(v.getContext(), SetPrefs.class);
				startActivityForResult(prefIntent, 0);				
			}
   	    }); 
   	    	
   	    //Set the icon images based on the theme
   		if (selectedPreferences != null && selectedPreferences.SelectedTheme.length() > 0){
        	if (selectedPreferences.SelectedTheme.equals("light")){
        		addNewItem.setImageResource(R.drawable.light_add_list);
        		undoAction.setImageResource(R.drawable.light_undo);
        		moreInfo.setImageResource(R.drawable.light_more);
        		startVoice.setImageResource(R.drawable.light_voice);
        	} else {
        		//Dark is default
        		addNewItem.setImageResource(R.drawable.dark_add_list);
        		undoAction.setImageResource(R.drawable.dark_undo);
        		moreInfo.setImageResource(R.drawable.dark_more);
        		startVoice.setImageResource(R.drawable.dark_voice);
        	} 	
   		}

  
     	Log.d("kozzer", "_______END LOAD______");
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event){
    	if (keyCode == KeyEvent.KEYCODE_MENU){
	    	//Ask to delete marked items
			Intent prefIntent = new Intent(findViewById(R.id.main_menu_more).getContext(), SetPrefs.class);
			startActivityForResult(prefIntent, 0);	
			return true;
    	} else {
    		return super.onKeyDown(keyCode,  event);
    	}
    }
    
    private void ensureDALExists(){
       	//See if the DAL exists, if not then create it
    	if (listDAL == null){
    		listDAL = new DataSQL(this);
    		
        	Log.d("kozzer", "Created DAL");
    	}
    }
    
    private void askToUndo(){
    	if (undoActions != null && undoActions.size() > 0){
    		UserAction lastAction = undoActions.get(undoActions.size() - 1);
	    	String textAction;
	    	switch (lastAction.userAction){
	    		case ADD:
	    			textAction = "Do you want to undo adding list '" + ((UserList)lastAction.actionData).ListName + "'?";
	    			break;
	    		case EDIT:
	    			textAction = "Change list name back to '" + ((UserList)lastAction.actionData).ListName + "'?";
	    			break;
	    		case DELETE_ONE:
	    			textAction = "Undo deletion of list '" + ((UserList)lastAction.actionData).ListName + "'?";
	    			break;
	    		default:
	    			textAction = "Not sure what you want to do...";
	    	}
	       	AlertDialog.Builder alert = GetAlertDialog(textAction , android.R.drawable.ic_menu_info_details);
	     	alert.setPositiveButton("Yes!", new OnClickListener(){
	       		public void onClick(DialogInterface dialog, int whichButton){
	       			undoLastAction();
	       		}
	    	});
	    	alert.setNegativeButton("No Way!", new OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					//nothing - cancel				
				}
	    	});
	    	AlertDialog dialog = alert.create();
	    	dialog.show();
    	} else {
    		displayMessage("No action to undo...");
    	}
    }
    
    private void undoLastAction(){
    	if (undoActions != null && undoActions.size() > 0){
    		UserAction lastAction = undoActions.get(undoActions.size() - 1);
    		switch(lastAction.userAction){
    			case ADD:
    				//Undo list add - so basically remove the new item
    				UserList addedList = (UserList) lastAction.actionData;
    				listDAL.deleteList(addedList.ListID);
    				populateMainList();
    				displayMessage("Undo add list complete '" + addedList.ListName + "'");
    				break;
    			case EDIT:
    				//Undo list edit - so basically edit again with original value
    				UserList editedList = (UserList) lastAction.actionData;
    				listDAL.updateListName(editedList);
    				populateMainList();
    				displayMessage("Undo edit list complete '" + editedList.ListName + "'");
    				break;
    			case DELETE_ONE:
    				//Undo list delete - so basically add deleted list back in
    				UserList deletedList = (UserList) lastAction.actionData;
    				UserList restoredList = listDAL.restoreDeletedList(deletedList);
    				if (restoredList != null){
	    				populateMainList();
	    				displayMessage("'" + restoredList.ListName + "' has been restored!");
    				} else {
    					displayMessage("Undo list delete: there was a problem restoring the deleted list!");
    				}
    				break;
    			default:
    		}
    		//Remove the undone action
    		undoActions.remove(lastAction);
    		lastAction = null;
    	} else {
    		displayMessage("No action to undo...");
    	}
    }
    
    protected void onListItemClick(View v, int pos, long id){
    	Log.d("kozzer", "List item clicked!");
    	selectedList = (UserList) mainList.get(pos);
    	
    	Intent intent = new Intent(v.getContext(), ChildListActivity.class);
    	intent.putExtra("SelectedListID", selectedList.ListID);
    	startActivityForResult(intent, 0);
    	Log.d("kozzer", "After start activity");
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK) {
        	//Coming back from the voice recognition activity
        	
            // Populate the wordsList with the String values the recognition engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
            	//We've got at least one match, call function to use info
            	handleVoiceInput(matches);
            } else {
            	displayMessage("Voice input failed...  please try again");
            }
        } else {
			//Coming back from the Preferences activity - Save the new preferences to the object
			SharedPreferences settings = getSharedPreferences("ListPrefs", 0);
			boolean changeTheme = (selectedPreferences.SelectedTheme != settings.getString("themePref", "default") ? true : false);
	
			//Save the settings from the "session"
			selectedPreferences.SelectedTheme = settings.getString("themePref", "dark");
			selectedPreferences.ShowNumbering = settings.getBoolean("numberingPref", false);
			String backupPathPref = settings.getString("backupPathPref", null);
			if (backupPathPref == null || backupPathPref.equals("")){
				selectedPreferences.BackupPath = listDAL.getDefaultBackupPath(getApplicationContext());
				if (selectedPreferences.BackupPath.contains("ERROR:")) {
					displayMessage("External storage not available.");
				}
			} else {
				selectedPreferences.BackupPath = backupPathPref;
			}
			
			//if changeTheme is true, reload the activity with the new theme
			if (changeTheme){
				Intent intent = getIntent();
				finish();
				startActivity(intent);
				Log.d("kozzer", "Changed theme when coming back to list");
			} else {
				//No theme change, but reload the list
				populateMainList();
			}
        }
   }
 
   public void showAlert(String title, String message){
		AlertDialog.Builder alert;
		//If it's the about dialog, show app icon, otherwise show alert icon
		if (title.contains("Lists!") ==  true){
			//It's the about, show app icon
			alert = GetAlertDialog(message, R.drawable.list);
		} else {
			//Not the about, so show regular alert icon
			alert = GetAlertDialog(message, android.R.drawable.ic_dialog_alert);
		}
		alert.setTitle(Html.fromHtml(title));
		AlertDialog dialog = alert.create();
		dialog.setButton("Whatever", new DialogInterface.OnClickListener() {  
	  	      public void onClick(DialogInterface dialog, int which) {  
	  	          return;  
	  	      } });  
		dialog.show();
    }
    
	private void ShowEditDialog(){
		
		UserList selectedList = mainList.get(selectedIndex);
		
		AlertDialog.Builder alert = GetAlertDialog("Edit your list name: ", android.R.drawable.ic_menu_edit);
    	alert.setTitle("Edit List");
    	
    	LinearLayout layout = new LinearLayout(this);
    	layout.setOrientation(LinearLayout.VERTICAL);
    	LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    	params.setMargins(7, 0, 7, 0);
    	
    	final EditText input = new EditText(this);
    	input.setText(selectedList.ListName);
    	layout.addView(input, params);
    	
    	alert.setView(layout);
    	    	
    	alert.setPositiveButton("Save it!", new DialogInterface.OnClickListener(){
    		public void onClick(DialogInterface dialog, int whichButton){
    			//Save action for undo
    			UserAction lastAction = new UserAction();
    			lastAction.isMainList = true;
    			lastAction.userAction = Action.EDIT;
    			if (undoActions == null) { undoActions = new ArrayList<UserAction>(); }
    			undoActions.add(lastAction);
   			
    			//Accept new value and write to list
    			String newText = input.getText().toString();
    			UserList selectedList = mainList.get(selectedIndex);
    			
    			//Clone the old value into the last action object
    			lastAction.actionData = new UserList(selectedList.ListID, selectedList.ListName, selectedList.ListIndex);
    			((UserList)lastAction.actionData).ListItems = new ArrayList<UserListItem>();
    			if (selectedList.ListItems != null && selectedList.ListItems.size() > 0){
    				for(int i = 0; i < selectedList.ListItems.size(); i++){
    					UserListItem curItem = selectedList.ListItems.get(i);
    					((UserList)lastAction.actionData).ListItems.add(new UserListItem(curItem.ItemID, curItem.ParentListID, curItem.ListItem, curItem.ItemCleared, curItem.ItemIndex));
    				}
    			}
    			
    			//Write the new value from the text box
    			selectedList.ListName = newText;
    			if (listDAL.updateListName(selectedList)){
    				listAdapter.notifyDataSetChanged();
    				displayMessage("List updated successfully!\n<|:^)");
    			} else {
    				displayMessage("There was a problem updating your stupid list!\n>:^(");    				
    			}
    		}
    	});
    	alert.setNegativeButton("Go Away", new DialogInterface.OnClickListener(){
    		public void onClick(DialogInterface dialog, int whichButton){
    			//Canceled, hide keyboard
	    		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    		imm.hideSoftInputFromWindow(input.getWindowToken(), WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    		}
    	});

    	//Create the dialog from the builder, set it so that the keyboard is always visible, then show it
    	final AlertDialog dlg = alert.create();
    	dlg.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    	dlg.setOnShowListener(new DialogInterface.OnShowListener(){		
			public void onShow(DialogInterface dialog) {
				//Show the keyboard
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(input, WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			}
		});
    	dlg.show();

	}
	
    private void ShowEntryDialog(){
    	AlertDialog.Builder alert =  GetAlertDialog("Type in your new list\'s name: ", android.R.drawable.ic_menu_edit);
    	alert.setTitle("Add New List");
   	
    	LinearLayout layout = new LinearLayout(this);
    	layout.setOrientation(LinearLayout.VERTICAL);
    	LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    	params.setMargins(7, 0, 7, 0);
    	
    	final EditText input = new EditText(this);
    	layout.addView(input, params);
    	
    	alert.setView(layout);
    	    	
    	alert.setPositiveButton("Add it!", new DialogInterface.OnClickListener(){
    		public void onClick(DialogInterface dialog, int whichButton){
    			String newText = input.getText().toString();
    			AddNewItem(newText);
    			Log.d("kozzer", "Added new list/item through dialog");

				//Dialog is going away, so close the keyboard
	    		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    		imm.hideSoftInputFromWindow(input.getWindowToken(), WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);   				

    		}
    	});
    	alert.setNegativeButton("Go Away", new DialogInterface.OnClickListener(){
    		public void onClick(DialogInterface dialog, int whichButton){
    			//Canceled, hide keyboard
	    		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    		imm.hideSoftInputFromWindow(input.getWindowToken(), WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    		}
    	});

    	//Create the dialog from the builder, set it so that the keyboard is always visible, then show it
    	final AlertDialog dlg = alert.create();
    	dlg.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    	dlg.setOnShowListener(new DialogInterface.OnShowListener(){		
			public void onShow(DialogInterface dialog) {
				//Show the keyboard
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(input, WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			}
		});
    	dlg.show();
    }
    
    /***********************************************************************************************************************************************/
    private void StartVoice(){
    	//Runs when the user clicks the mic button in the upper right
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak command...");
        startActivityForResult(intent, VOICE_REQUEST_CODE);
    }
    
    private void handleVoiceInput(ArrayList<String> matches){
    	//If we're here, we know we've got at least one match - see if we can figure out what it is
    	boolean foundMatch = false;
    	for (int i = 0; i < matches.size(); i++){
    		String match = matches.get(i);
    		if (match.length() > 10 && (match.substring(0, 8).contains("new list") || match.substring(0, 8).contains("add list"))){
    			//add a new list using the rest of the line
    			String newListName = match.substring(9);
    			
    			//Set i to 9999 to exit loop, and set match found flag
    			i = 9999;
    			foundMatch = true;
    			
    			//Add the new item
    			AddNewItem(newListName);

    		} else if (match.length() > 11 && match.substring(0, 9).contains("open list")){
    			
    			//Set i to 9999 to exit loop, and set match found flag
    			i = 9999;
    			foundMatch = true;
    			
    			//Just display message for now
    			//displayMessage("Open: [" + match + "]");
    			
	   		} else if (match.length() > 13 && match.substring(0, 12).contains("delete list")){
	    			
				//Set i to 9999 to exit loop, and set match found flag
				i = 9999;
				foundMatch = true;
				
    			//Just display message for now
    			//displayMessage("Delete: [" + match + "]");

	   		}  			
    	}
    	if (foundMatch == false){
    		displayMessage("Sorry... [" + matches.get(0) + "] failed.");
    	}

    }

    private void AddNewItem(String newText){
    	if (newText.length() > 0){
  		
    		//Yes, at least one character in the text box - add it to the current list
			//Main list mode
    		Log.d("kozzer", "About to create new list");
    		UserList newList = listDAL.addNewList(newText);
    		Log.d("kozzer", "Back from DAL");
    		if (newList != null){
    			//New list has been added to the database - so now add it to the in memory array list
    			populateMainList();
    			
    			//Save action for undo
    			UserAction lastAction = new UserAction();
    			lastAction.isMainList = true;
    			lastAction.userAction = Action.ADD;
    			lastAction.actionData = newList;
    			if (undoActions == null) { undoActions = new ArrayList<UserAction>(); }
    			undoActions.add(lastAction);

    		} else {
    			//newList is NULL which means the db add failed, display message and that's it
    			displayMessage("There was a problem adding your dumb list the stupid database!  >:^(");
    		}
		} 

    }

    private void askToDeleteOrEditList(View view, int selPos, long id){  
    	selectedIndex = selPos;
    	selectedList = (UserList) mainList.get(selPos);
		
    	AlertDialog.Builder alert = GetAlertDialog("What do you want with '" + selectedList.ListName + "'?", android.R.drawable.ic_menu_info_details);
     	alert.setPositiveButton("Delete it!", new OnClickListener(){
       		public void onClick(DialogInterface dialog, int whichButton){
       			DeleteList();
       		}
    	});
    	alert.setNeutralButton("Edit Name!", new OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				ShowEditDialog();
			}    		
    	});
    	alert.setNegativeButton("Nothing", new OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				//nothing - cancel				
			}
    	});
    	AlertDialog dialog = alert.create();
    	dialog.show();
    }
    
	private void DeleteList(){
		if (listDAL.deleteList(selectedList.ListID))
		{
			mainList.remove(selectedList);
			
			//Save action for undo
			UserAction lastAction = new UserAction();
			lastAction.isMainList = true;
			lastAction.userAction = Action.DELETE_ONE;
			lastAction.actionData = selectedList;
			if (undoActions == null) { undoActions = new ArrayList<UserAction>(); }
			undoActions.add(lastAction);

		}
		listAdapter.notifyDataSetChanged();
	}
    
    private void populateMainList(){
    	Log.d("kozzer", "Entering populateMainList()");
    	ensureDALExists();
    	
    	//See if the data object list is null, if so then populate it
    	mainList = listDAL.getAllLists();
    	
    	//Now populate the main list view with the main list if any list exists
    	if (mainList != null && mainList.size() > 0){
    		listAdapter = new UserListAdapter(this, mainList, selectedPreferences.SelectedTheme.equals("dark"));
    		mainListView.setAdapter(listAdapter);
    		mainListView.setListAdapter(listAdapter);
    		mainListView.setListSize(mainList.size());
    	}
    }   
	
	private void displayMessage(String theMessage){
		Context context = getApplicationContext();
		CharSequence text = theMessage;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
	@SuppressLint("NewApi")
	private AlertDialog.Builder GetAlertDialog(String message, int icon){
		AlertDialog.Builder alert;
		//alert = new AlertDialog.Builder(this);
		if (selectedPreferences.SelectedTheme != null && selectedPreferences.SelectedTheme.equals("light")){
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