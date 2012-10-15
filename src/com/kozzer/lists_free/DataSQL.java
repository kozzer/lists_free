package com.kozzer.lists_free;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataSQL extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "ListsDatabase.db";
	private static String DATABASE_PATH = "";
	private static final int DATABASE_VERSION = 1;
	private static final String BACKUP_NAME = "ListsDatabase.db";
	private String BACKUP_PATH;
	@SuppressWarnings("unused")
	private Context context;
	private SQLiteDatabase listDB;
	
	public DataSQL(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		String internalStoragePath = context.getFilesDir().getAbsolutePath();
		Log.d("kozzer", "context.getFilesDir().getAbsolutePath(): " + context.getFilesDir().getAbsolutePath());
		DATABASE_PATH = internalStoragePath;
		this.context = context;
		
		//Make sure the db file exists
		if (checkDatabase() == false){
			//It does not exist
			createDatabase();
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//Make sure the db file exists
		if (checkDatabase() == false){
			//It does not exist
			createDatabase();
		}
	}
	
	private boolean checkDatabase(){
		//Log.d("kozzer", "in checkDatabase()");
		if (listDB != null){
			closeDatabase();
		}
		listDB = null;
		try{
			//Log.d("kozzer", "About to open database");
			listDB = SQLiteDatabase.openDatabase(GET_DATABASE_PATH(), null, SQLiteDatabase.OPEN_READWRITE);
		}catch(SQLiteException e){
			//Database doesn't exist yet
			Log.d("kozzer", "DB does not exist");
		}
		
		return listDB != null ? true : false;
	}
	
	public void closeDatabase(){
		if (listDB != null){
			listDB.close();
		}
		listDB = null;
	}
	
	private String GET_DATABASE_PATH(){
		return DATABASE_PATH + "/" + DATABASE_NAME;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//  Auto-generated method stub

	}
	
	private void createDatabase() {
		try{
	
			//Make sure the folder exists
			File dbDir = new File(DATABASE_PATH);
			if (!dbDir.exists()){
				dbDir.mkdirs();
			} 
			listDB = SQLiteDatabase.openOrCreateDatabase(GET_DATABASE_PATH(), null);
			
			//Create database tables and populate with the documentation
			createTables();
			populateTablesWithDoc();	
			
		} catch(Exception ex){
			Log.d("kozzer", "***ERROR in createDatabase(): " + ex.getMessage());
		}
	}
	
	private void createTables(){
		listDB.execSQL("CREATE TABLE 'lists' (ListID INTEGER, ListName TEXT, ListIndex INTEGER, PRIMARY KEY(ListID ASC));");		
		listDB.execSQL("CREATE TABLE 'listItems' (ItemID INTEGER, ListID INTEGER, ListItem TEXT, Cleared INTEGER, ItemIndex INTEGER, PRIMARY KEY(ItemID ASC));");	
	}
	
	private void populateTablesWithDoc(){
		//Main Screen / general documentation
		listDB.execSQL("INSERT INTO 'lists' (ListName, ListIndex) VALUES ('Welcome to Lists!', 0);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (1, 'Main screen is the listing of all the lists', 0, 0);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (1, 'Tap a list to view it', 0, 1);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (1, 'Long press a list to change its name or delete it', 0, 2);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (1, 'Tap and drag the grip icon on the right to reorder your lists', 0, 3);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (1, 'Tap the ''+'' icon in the lower left to add a new list', 0, 4);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (1, 'Tap the undo icon to undo any previous actions.  Once you enter a list or exit the app, all previous actions will be lost.', 0, 5);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (1, 'Tap the microphone icon in the upper-right to use voice commands.  Currently, the only supported commands are ''ADD LIST [new list name]'' (or ''NEW LIST [new list name]''), ''OPEN LIST [list name]'', and ''DELETE LIST [list name]''.', 0, 6);");	
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (1, 'Tap the 3 dots to go to the Preferences screen', 0, 7);");
		
		//List item screen
		listDB.execSQL("INSERT INTO 'lists' (ListName, ListIndex) VALUES ('For each list', 1);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (2, 'Tap an item to ''mark'' done', 0, 0);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (2, 'Tap again to ''unmark''', 0, 1);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (2, 'Long press a list item to edit or delete it', 0, 2);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (2, 'Tap and drag the grip icon on the right to reorder a list', 0, 3);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (2, 'Tap the ''+'' icon in the lower left to add a new list item', 0, 4);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (2, 'Tap the undo icon to undo any previous actions.  Once you exit the list''s screen, all previous actions will be lost.', 0, 5);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (2, 'Tap the 3 dots to go to the ''List Actions'' menu.', 0, 6);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (2, 'List Actions Menu: ''Sort list alphabetically'': tap to sort the list items in alphabetical order.', 0, 7);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (2, 'List Actions Menu: ''Push marked to bottom'': tap to push all marked items below unmarked items, otherwise the sort is preserved.', 0, 8);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (2, 'List Actions Menu: ''Unmark all items'': tap to set all list items to their original, unmarked, state.', 0, 9);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (2, 'List Actions Menu: ''Delete marked items only'': tap to remove all marked items from the list.', 0, 10);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (2, 'List Actions Menu: ''Delete all items'': tap to remove all items from the list, but leave the list itself intact.', 0, 11);");

		//Preferences Screen
		listDB.execSQL("INSERT INTO 'lists' (ListName, ListIndex) VALUES ('Preferences Screen', 2);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (3, 'Theme selection: choose between Light and Dark themes.  (Default: Dark)', 0, 0);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (3, 'Display #s for list items: check to display numbering for all lists. (Default: unchecked)', 0, 1);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (3, 'Backup Path: Press to change the path where the backups are saved (Default: Android default external storage)', 0, 2);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (3, 'Backup Lists: Press to back up your lists to external storage (or whatever path is chosen above).  Backing up your lists will overwrite any existing backup.', 0, 3);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (3, 'Restore Lists: Press to restore your lists from the backup.  This will overwrite your existing lists.', 0, 4);");
		listDB.execSQL("INSERT INTO 'listItems' (ListID, ListItem, Cleared, ItemIndex) VALUES (3, 'App Info: Press to view information about this application', 0, 5);");
		
	}
	
	public ArrayList<UserList> getAllLists(){
		Cursor listCur = null;
		ArrayList<UserList> userList = null;
		try{		
			if (checkDatabase()){
				listCur = listDB.rawQuery("SELECT rowid as _id, ListID, ListName, ListIndex FROM lists ORDER BY ListIndex, ListName ASC;", null);
				if (listCur != null && listCur.getCount() > 0){
					listCur.moveToFirst();
					
					//Create the array
					userList = new ArrayList<UserList>();
					do{
						//Create the new List object
						UserList newList = new UserList(listCur.getInt(1), listCur.getString(2), listCur.getInt(3));
						//Add new list to the list of lists
						userList.add(newList);
	
					} while (listCur.moveToNext());
					
					//Now loop through all the lists and get the items for each
					for(int i=0; i<userList.size();i++){
						UserList curList = userList.get(i);
						curList.ListItems = new ArrayList<UserListItem>();
						Cursor itemCur = listDB.rawQuery("SELECT ItemID, ListItem, Cleared, ItemIndex FROM listItems WHERE ListID = " + curList.ListID + " ORDER BY ItemIndex, ListItem ASC;", null);
						
						if (itemCur != null && itemCur.getCount() > 0){
							//At least one child list item
							itemCur.moveToFirst();
							
							do{
								int itemID = itemCur.getInt(0);
								int intCleared = itemCur.getInt(2);
								boolean cleared = intCleared > 0 ? true : false;
								int index = itemCur.getInt(3);
								UserListItem newItem = new UserListItem(itemID, curList.ListID, itemCur.getString(1), cleared, index);
								//Log.d("kozzer", "Item '" + itemCur.getString(1) + "', ID " + String.valueOf(itemID) + ", Index " + String.valueOf(index));
								curList.ListItems.add(newItem);							
							}while (itemCur.moveToNext());
							
						}
					}
					
				}	
			}
		}catch(Exception ex){
			Log.d("kozzer", "***ERROR Query error: " + ex.getMessage());
		}
		closeDatabase();
		return userList;
	}
	
	public UserList getListForID(int listID){
		UserList theList = null;
		Cursor listCur = null;
		try{
			if (checkDatabase()){
				listCur = listDB.rawQuery("SELECT ListName, ListIndex FROM lists WHERE ListID = " + listID, null);
				Log.d("kozzer", "List ID: " + String.valueOf(listID));
				if (listCur != null && listCur.getCount() == 1){
					//Only one row should be returned
					listCur.moveToFirst();
					theList = new UserList(listID, listCur.getString(0), listCur.getInt(1));
					
					//Now that we have the list, get all the list items
					theList.ListItems = new ArrayList<UserListItem>();
					Cursor itemCur = listDB.rawQuery("SELECT ItemID, ListItem, Cleared, itemIndex FROM listItems WHERE ListID = " + theList.ListID + " ORDER BY ItemIndex, ListItem ASC;", null);
					
					if (itemCur != null && itemCur.getCount() > 0){
						//At least one child list item
						itemCur.moveToFirst();
						
						do{
							int itemID = itemCur.getInt(0);
							int intCleared = itemCur.getInt(2);
							boolean cleared = intCleared > 0 ? true : false;
							int index = itemCur.getInt(3);
							UserListItem newItem = new UserListItem(itemID, theList.ListID, itemCur.getString(1), cleared, index);
							theList.ListItems.add(newItem);							
						} while (itemCur.moveToNext());
						
					}
				}
			}
		} catch (Exception ex){
			Log.d("kozzer", "ERROR in getListForID(): " + ex.getMessage());
		}
		closeDatabase();
		return theList;
	}
	
	public UserList addNewList(String newListName){
		UserList newList = null;
		String phase = "Get new list index";
		try{
			if (checkDatabase()){		
				//Get the highest index of all list
				String maxIndex = "SELECT MAX(ListIndex) FROM lists;";
				int index = 0;
				Cursor cur = listDB.rawQuery(maxIndex, null);
				if (cur != null && cur.getCount() == 1){
					cur.moveToFirst();
					index = cur.getInt(0);
					index++;
				}
				cur.close();
			
				phase = "Add new list with incremented index";
				
				//Add the new list name
				String insert ="INSERT INTO lists (ListName, ListIndex) VALUES (?, " + String.valueOf(index) + ");";
				String escaped = newListName.replace("'", "\'").trim();
				listDB.execSQL(insert, new String[]{ escaped });
				
				//Now get the new list's id
				phase = "Get new list's ID";
				cur = listDB.rawQuery("SELECT ListID, ListIndex FROM lists WHERE ListName = ?;", new String[]{ escaped });
				if (cur != null && cur.getCount() == 1){
					//1 record was returned
					cur.moveToFirst();
					//grab the integer
					int newListID = cur.getInt(0);
					int newListIndex = cur.getInt(1);
					//Create new list object
					newList = new UserList(newListID, newListName, newListIndex);
				} 
			}
		}catch(Exception ex){
			Log.d("kozzer", "ERROR in addNewList(): Phase: " + phase + " ~~ Message: " + ex.getMessage());
			newList = null;
		}
		closeDatabase();
		return newList;
	}
	
	public UserList restoreDeletedList(UserList deletedList){
		Log.d("kozzer", "In restoreDeletedList()");
		UserList restoredList = null;
		try{
			if (checkDatabase()){
				//Insert the record with all the values it contained before
				String insert = "INSERT INTO lists (ListName, ListIndex) VALUES (?, " + String.valueOf(deletedList.ListIndex) + ");";
				listDB.execSQL(insert, new String[]{ deletedList.ListName });
				
				Log.d("kozzer", "Just added list back into lists table");
				
				//Now create the new/restored list object, and get the new ID
				String getID = "SELECT ListID FROM lists WHERE ListName = ? AND ListIndex = " + String.valueOf(deletedList.ListIndex) + ";";
				Cursor cur = listDB.rawQuery(getID, new String[]{ deletedList.ListName });
				if (cur != null && cur.getCount() == 1){
					
					//1 record was returned, as expected
					cur.moveToFirst();
					int restoredListID = cur.getInt(0);
					Log.d("kozzer", "Found added list, about to add any items back");
					restoredList = new UserList(restoredListID, deletedList.ListName, deletedList.ListIndex);
					restoredList.ListItems = new ArrayList<UserListItem>();
					
					//Now restore each list item
					if (deletedList.ListItems != null && deletedList.ListItems.size() > 0){
						for(int i = 0; i < deletedList.ListItems.size(); i++){
							UserListItem curItem = (UserListItem) deletedList.ListItems.get(i);
							restoredList.ListItems.add(restoreDeletedItem(curItem, restoredListID));
						}
					}
				} 
			}
		} catch(Exception ex){
			Log.d("kozzer", "ERROR in restoreDeletedList(): " + ex.getMessage());
		}
		return restoredList;
	}
	
	public UserListItem addNewItem(UserList selList, String newItem){
		UserListItem newListItem = null;
		try{
			if (checkDatabase()){
				//First, get the new index
				String getIndex = "SELECT MAX(ItemIndex), COUNT(listItem) FROM listItems WHERE ListID = " + selList.ListID + ";";
				Cursor idx = listDB.rawQuery(getIndex, null);
				int maxIndex = 0;
				if (idx != null && idx.getCount() > 0){
					//Get have current max index for the list
					idx.moveToFirst();
					maxIndex = idx.getInt(0);
					
					if (idx.getInt(1) > 0){		
						//Only increment the index if there's already a record, otherwise the max will be 0 and it should stay that way
						Log.d("kozzer", "Incremented index from " + String.valueOf(maxIndex) + " to " + String.valueOf(maxIndex + 1));
					
						//Increment the index
						maxIndex++;
					}
				}			
				//Destroy the cursor
				idx.close();
				idx = null;		
				
				//Now, add new list item
				String insert = "INSERT INTO listItems (ListID, ListItem, Cleared, ItemIndex) VALUES (" + selList.ListID + ", ?, 0, " + String.valueOf(maxIndex) + ");";
				
				String escaped = newItem.replace("'", "\'").trim();
				listDB.execSQL(insert, new String[] { escaped } );
				
				//Get back the new item id
				String getID = "SELECT MAX(ItemID) FROM listItems WHERE ListID = " + selList.ListID + ";";
				Cursor newCur = listDB.rawQuery(getID, null);
				int newID = 999;
				if (newCur != null && newCur.getCount() > 0){
					newCur.moveToFirst();
					newID = newCur.getInt(0);
				}
				
				//Since no exception was thrown, create a new list item object and add it to the list
				newListItem = new UserListItem(newID, selList.ListID, newItem, false, maxIndex);
				
				if (selList.ListItems == null){
					selList.ListItems = new ArrayList<UserListItem>();
				}
				selList.ListItems.add(newListItem);
			} else {
				Log.d("kozzer", "addNewItem() - checkDatabase returned false");
			}
		}catch(Exception ex){
			Log.d("kozzer", "ERROR in addNewItem(): " + ex.getMessage());
			newListItem = null;
		}
		closeDatabase();
		return newListItem;
	}
	
	public UserListItem restoreDeletedItem(UserListItem deletedItem, int restoredParentListID){
		UserListItem restoredItem = null;
		try{
			if (checkDatabase()){
				//Now, add deleted list item back into the table
				int clear = deletedItem.ItemCleared == true ? 1 : 0;
				String insert = "INSERT INTO listItems (ListID, ListItem, Cleared, ItemIndex) VALUES (" + String.valueOf(restoredParentListID) + ", ?, " + String.valueOf(clear) + ", " + String.valueOf(deletedItem.ItemIndex) + ");";
				Log.d("kozzer", "After creating SQL: " + insert + " --- " + deletedItem.ListItem);
				listDB.execSQL(insert, new String[] { deletedItem.ListItem } );
				
				Log.d("kozzer", "Just added [" + deletedItem.ListItem + "] back into listItems table");
				
				//Get back the restored item id
				String getID = "SELECT MAX(ItemID) FROM listItems WHERE ListID = " + String.valueOf(deletedItem.ParentListID) + ";";
				Cursor newCur = listDB.rawQuery(getID, null);
				int newID = 999;
				if (newCur != null && newCur.getCount() > 0){
					newCur.moveToFirst();
					newID = newCur.getInt(0);
				}
				//Create the new/restored list item object
				restoredItem = new UserListItem(newID, deletedItem.ParentListID, deletedItem.ListItem, deletedItem.ItemCleared, deletedItem.ItemIndex);
			}
		}catch (Exception ex){
			Log.d("kozzer", "ERROR in restoreDeletedItem(): " + ex.getMessage());
			restoredItem = null;
		}
		closeDatabase();
		return restoredItem;
	}
	
	
	public boolean deleteList(int listID){
		boolean ret = false;
		if (checkDatabase()){
			try{
				String delete = "DELETE FROM listItems WHERE ListID = " + String.valueOf(listID) + ";";
				listDB.execSQL(delete);

				delete = "DELETE FROM lists WHERE ListID = " + String.valueOf(listID) + ";";
				listDB.execSQL(delete);
				
				ret = true;
			}catch(Exception ex){
				Log.d("kozzer", "ERROR in deleteList(): " + ex.getMessage());
				ret = false;
			}
		}
		closeDatabase();
		return ret;
	}
	
	public boolean updateListName(UserList list){
		boolean ret = false;
		
		if (checkDatabase()){
			try {
				String escaped = list.ListName.replace("'", "\'").trim();
				String update = "UPDATE lists SET ListName = ? WHERE ListID = " + String.valueOf(list.ListID) + ";";
				listDB.execSQL(update, new String[] { escaped } );
				ret = true;
			} catch (Exception ex){
				Log.d("kozzer", "ERROR in updateListName(): " + ex.getMessage());	
				ret = false; //opposite of poassed in, so in-mem value doesn't change				
			}
		}

		return ret;
	}
	
	public boolean deleteAllItemsOnList(int listID){
		boolean ret = false;
		if (checkDatabase()){
			try{
				String delete = "DELETE FROM listItems WHERE ListID = " + String.valueOf(listID) + ";";
				listDB.execSQL(delete);
				
				ret = true;
			}catch(Exception ex){
				Log.d("kozzer", "ERROR in deleteAllItemsOnList(): " + ex.getMessage());
				ret = false;
			}
		}
		closeDatabase();
		return ret;
	}

	public boolean deleteClearedItems(int listID){
		boolean ret = false;
		if (checkDatabase()){
			try{
				String delete = "DELETE FROM listItems WHERE ListID = " + String.valueOf(listID) + " AND Cleared = 1;";
				listDB.execSQL(delete);
				
				ret = true;
			}catch(Exception ex){
				Log.d("kozzer", "ERROR in deleteClearedItems(): " + ex.getMessage());
				ret = false;
			}
		}
		closeDatabase();
		return ret;
	}
	
	public boolean deleteItem(UserListItem item){
		boolean ret = false;
		
		if (checkDatabase()){
			try {
				String delete = "DELETE FROM listItems WHERE ListID = " + String.valueOf(item.ParentListID) + " AND ItemID = " + String.valueOf(item.ItemID) + ";";
				listDB.execSQL(delete);
				ret = true;
			} catch (Exception ex){
				Log.d("kozzer", "ERROR in deleteItem(): " + ex.getMessage());	
				ret = false; //opposite of poassed in, so in-mem value doesn't change				
			}
		}
		return ret;
	}
	
	public boolean updateItem(UserListItem item){
		boolean ret = false;
		
		if (checkDatabase()){
			try {
				String escaped = item.ListItem.replace("'", "\'").trim();
				String update = "UPDATE listItems SET ListItem = ? WHERE ListID = " + String.valueOf(item.ParentListID) + " AND ItemID = " + String.valueOf(item.ItemID) + ";";
				listDB.execSQL(update, new String[] { escaped } );
				ret = true;
			} catch (Exception ex){
				Log.d("kozzer", "ERROR in updateItem(): " + ex.getMessage());	
				ret = false; //opposite of poassed in, so in-mem value doesn't change				
			}
		}
		return ret;		
	}

	public boolean setItemCleared(UserListItem item, boolean cleared){
		boolean ret = !cleared;
		if (checkDatabase()){
			try{
				int clear = cleared == true ? 1 : 0;
				String setClear = "UPDATE listItems SET Cleared = " + String.valueOf(clear) + " WHERE ListID = " + String.valueOf(item.ParentListID) + " AND ItemID = " + String.valueOf(item.ItemID) + ";";
				listDB.execSQL(setClear);

				ret = cleared; //passed in value since it was written to DB
			}catch(Exception ex){
				Log.d("kozzer", "ERROR in setItemCleared(): " + ex.getMessage());	
				ret = !cleared; //opposite of poassed in, so in-mem value doesn't change
			}
		}
		closeDatabase();
		return ret;
	}
	
	public boolean updateListIndex(int ListID, int newListIndex){
		boolean ret = false;
		
		if (checkDatabase()){
			try {
				String setIndex = "UPDATE lists SET ListIndex = " + String.valueOf(newListIndex) + " WHERE ListID = " + String.valueOf(ListID) + ";";
				listDB.execSQL(setIndex);
				ret = true;
			} catch (Exception e){
				Log.d("kozzer", "Error in updateListIndex(): " + e.getMessage());
			}
		}
		closeDatabase();
		return ret;
	}
	
	public boolean updateListItemIndex(int ListID, int ItemID, int newItemIndex){
		boolean ret = false;
		
		if (checkDatabase()){
			try {
				String setIndex = "UPDATE listItems SET ItemIndex = " + String.valueOf(newItemIndex) + " WHERE ListID = " + String.valueOf(ListID) + " AND ItemID = " + String.valueOf(ItemID) + ";";
				listDB.execSQL(setIndex);
				ret = true;
			} catch (Exception e){
				Log.d("kozzer", "Error in updateListItemIndex(): " + e.getMessage());
			}
		}
		closeDatabase();
		return ret;
	}

	public ArrayList<UserListItem> SaveCurrentListSort(int ListID, ArrayList<UserListItem> listItems){
		try{
			if (checkDatabase()){
				for(int i = 0; i < listItems.size(); i++){
					UserListItem item = listItems.get(i);
					
					//Change list item index to current place in list					
					UserListItem tempItem = new UserListItem(item.ItemID, item.ParentListID, item.ListItem, item.ItemCleared, i);

					//Update the item in the database
					String update = "UPDATE listItems SET ItemIndex = " + String.valueOf(i) + " WHERE ListID = "  + String.valueOf(ListID) + " AND ItemID = " + String.valueOf(tempItem.ItemID) + ";";
					listDB.execSQL(update);
					
					//Now get the list again
					listItems = getListForID(ListID).ListItems;
				}
			}
		} catch (Exception e){
			Log.d("kozzer", "Error in sortListAlpha(): " + e.getMessage());
			closeDatabase();
			return null;
		}
		return listItems;
	}
	
    public String getDefaultBackupPath(Context context){
		//Get the path to the SDcard or external storage
    	try {
    		BACKUP_PATH = context.getExternalFilesDir(null).getPath() + "/Lists_Backup/";		
    	} catch (Exception ex) {
    		BACKUP_PATH = "ERROR: " + ex.getMessage();
    		Log.d("kozzer", "Error getting ext storage path: " + ex.getMessage());
    	}
    	Log.d("kozzer", "Got default backup path from system: " + BACKUP_PATH);
    	
		return BACKUP_PATH;
    }
	
	public boolean backupExists(String backupPath){
		boolean exists = false;
		BACKUP_PATH = backupPath;
		File backupDir = new File(BACKUP_PATH);
		
		//Check that the path to the backup exists
		if (!backupDir.exists()){
			Log.d("kozzer", "Backup folder doesn't exist");		
		} else {
			//Backup folder exists, so see if the backup file exists
			File backupDB = new File(BACKUP_PATH + BACKUP_NAME);
			if (backupDB.exists()){
				//Backup file does exist
				if (backupDB.length() > 0){
					//Backup file is larger than zero, so it exists!
					exists = true;
				}
			}
		}
		//Return whether the backup already exists or not
		return exists;
	}
	
	public void DeleteDatabase(){
		File dbFile = new File(GET_DATABASE_PATH());
		if (dbFile.exists()){
			dbFile.delete();
		}
	}
	
	public String createBackup(String backupPath){
		String ret = "[n/a]";
		
		//Backup does not exist, so check to see if the path exists.  If not, then create the path
		BACKUP_PATH = backupPath;
		File backupFilePath = new File(BACKUP_PATH);
		if (!backupFilePath.exists()){
			backupFilePath.mkdirs();
			if (!backupFilePath.exists()){
				ret = "Cannot create backup dir!";
				return ret;
			}
		}

		//Now the path should exist, so check to see if there's an existing backup there, if so then delete it
		File backupFile = new File(BACKUP_PATH + BACKUP_NAME);
		if (backupExists(BACKUP_PATH) == true){
			if (backupFile.exists()){
				backupFile.delete();
			}
		}
		
		//At this point, we've got an existing backup folder on the SD card, but the file does not exist
		File dbFile = new File(GET_DATABASE_PATH());
		if (dbFile.exists()){
			//copy the raw file (apparently the only way?!?)
			String phase = "Create empty file";
			try {
	
				//Copy the database file to the backup file
                FileChannel src = new FileInputStream(dbFile).getChannel();
                FileChannel dst = new FileOutputStream(backupFile).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
				
				//File should have copied successfully
				ret = "success";
			} catch (FileNotFoundException e) {
				//  Auto-generated catch block
				e.printStackTrace();
				Log.d("kozzer", "Backup process FAILED (FileNotFoundException)!");	
				ret = phase + "\n\n" + e.getMessage();
			} catch (IOException e) {
				//  Auto-generated catch block
				e.printStackTrace();
				Log.d("kozzer", "Backup process FAILED (IOException)!");	
				ret = phase + "\n\n" + e.getMessage();
			}
			
		}
		
		return ret;
	}
	
	public String restoreBackup(String backupPath){
		String ret = "[n/a]";
		if (backupExists(backupPath)){
			//Make sure again that the backup file exists, so now copy the file
			BACKUP_PATH = backupPath;
			File backupFile = new File(BACKUP_PATH + BACKUP_NAME);
			
			try {
				//delete the existing list db
				File dbFile = new File(GET_DATABASE_PATH());
				if (dbFile.exists()){
					//If the database file exists - and it should - delete the file
					dbFile.delete();
				}
				
				//Copy the backup file to the database file
                FileChannel src = new FileInputStream(backupFile).getChannel();
                FileChannel dst = new FileOutputStream(dbFile).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
				
				//Backup should have been restored successfully
				ret = "success";

			} catch (IOException e) {
				//  Auto-generated catch block
				e.printStackTrace();
				ret = e.getMessage();
				Log.d("kozzer", "Database restore FAILED! (IOException)");
			}
		}
		
		return ret;
	}
}
