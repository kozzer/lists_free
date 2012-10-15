package com.kozzer.lists_free;

import java.util.ArrayList;
import java.util.Collections;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class ChildListActivity extends Activity {

	private int childListID;
	private UserList childList;
	private String childListName;
	private DragAndDropListView childListView; // Changed from regular ListView
												// for drag and drop
												// ****************************
	private UserListItemAdapter listAdapter;
	private DataSQL listDAL;
	private Preferences selectedPreferences;
	private ArrayList<UserAction> undoActions;
	private int selectedIndex;
	private final int PULSE_DURATION = 20;
	private static final int VOICE_REQUEST_CODE = 1234;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("kozzer", "***** Loading Child List *****");

		// Get and set the theme
		ensureDALExists();
		if (selectedPreferences == null) {
			SharedPreferences settings = getSharedPreferences("ListPrefs", 0);
			selectedPreferences = new Preferences();
			selectedPreferences.SelectedTheme = settings.getString("themePref",
					"dark");
			selectedPreferences.ShowNumbering = settings.getBoolean(
					"numberingPref", false);
			String backupPathPref = settings.getString("backupPathPref", null);
			if (backupPathPref == null || backupPathPref.equals("")) {
				selectedPreferences.BackupPath = listDAL
						.getDefaultBackupPath(getApplicationContext());
			} else {
				selectedPreferences.BackupPath = backupPathPref;
			}
		}
		if (selectedPreferences != null
				&& selectedPreferences.SelectedTheme.length() > 0) {
			// Save preferences to shared settings
			SharedPreferences settings = getSharedPreferences("ListPrefs", 0);
			SharedPreferences.Editor edit = settings.edit();
			edit.putString("themePref", selectedPreferences.SelectedTheme);
			edit.putBoolean("numberingPref", selectedPreferences.ShowNumbering);
			edit.putString("backupPathPref", selectedPreferences.BackupPath);
			edit.commit();

			if (selectedPreferences.SelectedTheme.equals("light")) {
				this.setTheme(R.style.NoTitleLight);
			} else if (selectedPreferences.SelectedTheme.equals("dark")) {
				this.setTheme(R.style.NoTitleDark);
			} else {
				// Default to dark
				this.setTheme(R.style.NoTitleDark);
			}
		}

		getListInfo();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.child_list);

		childListView = (DragAndDropListView) findViewById(R.id.child_list); // Cast
																				// changed
																				// from
																				// regular
																				// ListView
																				// for
																				// drag
																				// and
																				// drop
																				// ***************
		// childListView.setItemArray(childList.ListItems);

		TextView titleText = (TextView) findViewById(R.id.child_title_text);
		titleText.setText(this.getTitle());

		populateChildList();

		childListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> av, View view,
							int pos, long id) {
						toggleMark(pos);
					}
				});

		childListView
				.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
					public boolean onItemLongClick(AdapterView<?> av,
							View view, int pos, long id) {
						askToDeleteOrEdit(pos);
						return true;
					}
				});

		// Set click listener for voice button
		ImageView startVoice = (ImageView) findViewById(R.id.child_menu_voice);
		startVoice.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Pulse
				((Vibrator) getSystemService(VIBRATOR_SERVICE))
						.vibrate(PULSE_DURATION);
				// Start listening
				StartVoice();
			}
		});

		// Set click listener for add new item menu option
		ImageView addNewItem = (ImageView) findViewById(R.id.child_menu_add);
		addNewItem.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Pulse
				((Vibrator) getSystemService(VIBRATOR_SERVICE))
						.vibrate(PULSE_DURATION);
				// Add new list
				ShowEntryDialog();
			}
		});

		// Set click listener for push marked to bottom menu option
		ImageView undoAction = (ImageView) findViewById(R.id.child_menu_undo);
		undoAction.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.d("kozzer", "in the listener");
				// Pulse
				((Vibrator) getSystemService(VIBRATOR_SERVICE))
						.vibrate(PULSE_DURATION);
				// Ask to delete marked items
				askToUndo();
			}
		});

		// Set click listener for additional actions menu
		ImageView showMore = (ImageView) findViewById(R.id.child_menu_more);
		showMore.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Pulse
				((Vibrator) getSystemService(VIBRATOR_SERVICE))
						.vibrate(PULSE_DURATION);
				// Show menu
				// v.showContextMenu();
				displayActionsMenu();
			}
		});
		// Register the addition actions item for the context menu
		registerForContextMenu(findViewById(R.id.child_menu_more));

		// Set the icon images based on the theme
		if (selectedPreferences != null
				&& selectedPreferences.SelectedTheme.length() > 0) {
			if (selectedPreferences.SelectedTheme.equals("light")) {
				addNewItem.setImageResource(R.drawable.light_add_list);
				undoAction.setImageResource(R.drawable.light_undo);
				showMore.setImageResource(R.drawable.light_more);
				startVoice.setImageResource(R.drawable.light_voice);
			} else {
				// Dark is default
				addNewItem.setImageResource(R.drawable.dark_add_list);
				undoAction.setImageResource(R.drawable.dark_undo);
				showMore.setImageResource(R.drawable.dark_more);
				startVoice.setImageResource(R.drawable.dark_voice);
			}
		}

		// Log.d("kozzer", "End of onCreate() for child list!");

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			// Show menu
			// findViewById(R.id.child_menu_more).showContextMenu();
			displayActionsMenu();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Should only fire when voice input is used
		if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK) {
			// Populate the wordsList with the String values the recognition
			// engine thought it heard
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (matches != null && matches.size() > 0) {
				// We've got at least one match, call function to use info
				handleVoiceInput(matches);
			} else {
				displayMessage("Voice input failed...  please try again");
			}
		}
	}

	private void askToUndo() {
		if (undoActions != null && undoActions.size() > 0) {
			UserAction lastAction = undoActions.get(undoActions.size() - 1);
			String textAction;
			switch (lastAction.userAction) {
			case ADD:
				textAction = "Do you want to undo adding item '"
						+ ((UserListItem) lastAction.actionData).ListItem
						+ "'?";
				break;
			case EDIT:
				textAction = "Change item text back to '"
						+ ((UserListItem) lastAction.actionData).ListItem
						+ "'?";
				break;
			case DELETE_ONE:
				textAction = "Undo deletion of item '"
						+ ((UserListItem) lastAction.actionData).ListItem
						+ "'?";
				break;
			case DELETE_MULTIPLE:
				textAction = "Undo deletion of multiple items?";
				break;
			case CHANGE_SORT:
				textAction = "Undo change to list sort?";
				break;
			case TOGGLE_MARK:
				textAction = "Undo change to whether '"
						+ ((UserListItem) lastAction.actionData).ListItem
						+ "' is marked?";
				break;
			case UNMARK_MULTIPLE:
				textAction = "Undo unmark all?";
				break;
			default:
				textAction = "Not sure what you want to do...";
			}
			AlertDialog.Builder alert = GetAlertDialog(textAction,
					android.R.drawable.ic_menu_info_details);
			alert.setPositiveButton("Yup!", new OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					undoLastAction();
				}
			});
			alert.setNegativeButton("No Way!", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// nothing - cancel
				}
			});
			AlertDialog dialog = alert.create();
			dialog.show();
		} else {
			displayMessage("No action to undo...");
		}
	}

	@SuppressWarnings("unchecked")
	private void undoLastAction() {
		Log.d("kozzer", "in undo last action");
		if (undoActions != null && undoActions.size() > 0) {
			UserAction lastAction = undoActions.get(undoActions.size() - 1);
			Log.d("kozzer", "Undo: " + lastAction.userAction.toString());
			switch (lastAction.userAction) {
			case ADD:
				// Undo list add - so basically remove the new item
				UserListItem addedItem = (UserListItem) lastAction.actionData;
				listDAL.deleteItem(addedItem);
				populateChildList();
				displayMessage("Undo add item complete '" + addedItem.ListItem
						+ "'");
				break;
			case EDIT:
				// Undo list edit - so basically edit again with original value
				UserListItem editedItem = (UserListItem) lastAction.actionData;
				listDAL.updateItem(editedItem);
				populateChildList();
				//listAdapter.notifyDataSetChanged();
				displayMessage("Undo edit list complete '"
						+ editedItem.ListItem + "'");
				break;
			case DELETE_ONE:
				// Undo list delete - so basically add deleted list back in
				UserListItem deletedItem = (UserListItem) lastAction.actionData;
				UserListItem restoredItem = listDAL.restoreDeletedItem(
						deletedItem, deletedItem.ParentListID);
				if (restoredItem != null) {
					populateChildList();
					displayMessage("'" + restoredItem.ListItem
							+ "' has been restored!");
				} else {
					displayMessage("Undo item delete: there was a problem restoring the deleted item!");
				}
				break;
			case DELETE_MULTIPLE:
				// Undo multiple item delete
				ArrayList<UserListItem> deletedItems = (ArrayList<UserListItem>) lastAction.actionData;
				if (deletedItems != null && deletedItems.size() > 0) {
					try {
						for (int i = 0; i < deletedItems.size(); i++) {
							UserListItem thisItem = deletedItems.get(i);
							Log.d("kozzer",
									"about to restore '"
											+ thisItem.ListItem
											+ "', parent "
											+ String.valueOf(thisItem.ParentListID));
							UserListItem thisRestoredItem = listDAL
									.restoreDeletedItem(thisItem,
											thisItem.ParentListID);
							childList.ListItems.add(thisRestoredItem);
						}
						// If we've gotten past the loop, then there were no
						// errors
						//listAdapter.notifyDataSetChanged();
						populateChildList();
						displayMessage(String.valueOf(deletedItems.size())
								+ " item(s) have been restored!");
					} catch (Exception ex) {
						displayMessage("There was a problem - None, some, or all of the items may have been restored.");
					}
				}
				break;
			case CHANGE_SORT:
				// Undo sort change
				ArrayList<UserListItem> unsortedItems = (ArrayList<UserListItem>) lastAction.actionData;
				if (unsortedItems != null && unsortedItems.size() > 0) {
					listDAL.SaveCurrentListSort(
							unsortedItems.get(0).ParentListID, unsortedItems);
					childList.ListItems = unsortedItems;
					//listAdapter.notifyDataSetChanged();
					populateChildList();
					displayMessage("List sort restored!");
				}
				break;
			case TOGGLE_MARK:
				// Undo toggle
				UserListItem origItem = (UserListItem) lastAction.actionData;
				if (origItem != null) {
					listDAL.setItemCleared(origItem, origItem.ItemCleared);
					for (int i = 0; i < childList.ListItems.size(); i++) {
						UserListItem curItem = childList.ListItems.get(i);
						if (origItem.ItemID == curItem.ItemID) {
							curItem.ItemCleared = origItem.ItemCleared;
							i = 9999;
						}
					}
					//listAdapter.notifyDataSetChanged();
					populateChildList();
					displayMessage("Marked status restored for '"
							+ origItem.ListItem + "'!");
				}
				break;
			case UNMARK_MULTIPLE:
				// Undo multiple unmark
				ArrayList<UserListItem> unmarkedItems = (ArrayList<UserListItem>) lastAction.actionData;
				if (unmarkedItems != null && unmarkedItems.size() > 0) {
					try {
						for (int i = 0; i < unmarkedItems.size(); i++) {
							UserListItem thisItem = unmarkedItems.get(i);
							listDAL.setItemCleared(thisItem,
									thisItem.ItemCleared);
							for (int c = 0; c < childList.ListItems.size(); c++) {
								UserListItem curItem = childList.ListItems
										.get(c);
								if (curItem.ItemID == thisItem.ItemID) {
									curItem.ItemCleared = thisItem.ItemCleared;
								}
							}
						}
						// If we've gotten past the loop, then there were no
						// errors
						//listAdapter.notifyDataSetChanged();
						populateChildList();
						displayMessage(String.valueOf(unmarkedItems.size())
								+ " item"
								+ ((unmarkedItems.size() > 1) ? "s" : "")
								+ " have had their status restored!");
					} catch (Exception ex) {
						displayMessage("There was a problem - None, some, or all of the items may have been restored.");
					}
				}
				break;
			default:
			}
			// Since the undo has been performed, remove it from the list
			undoActions.remove(lastAction);
			lastAction = null;

		} else {
			displayMessage("No action to undo...");
		}
	}

	private void ensureDALExists() {
		// See if the DAL exists, if not then create it
		if (listDAL == null) {
			listDAL = new DataSQL(this);
		}
	}

	@Override
	public void onBackPressed() {
		// User clicked back button, so set the activity result
		Intent returnIntent = new Intent();
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	/******************* CONTEXT MENU EVENT HANDLERS *************************************************************************/

	private void displayActionsMenu() {
		final String[] actions = { "Sort list alphabetically",
				"Push marked to bottom", "Unmark all items in list",
				"Delete marked items", "Delete all items in list" };

		boolean isDarkTheme;
		if (selectedPreferences.SelectedTheme.equals("dark")) {
			isDarkTheme = true;
		} else {
			isDarkTheme = false;
		}

		ChildListActionsAdapter actionsAdapter = new ChildListActionsAdapter(
				getApplicationContext(), isDarkTheme);
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("List Actions");
		alert.setAdapter(actionsAdapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				String action = String.valueOf(actions[item]);
				if (action == "Sort list alphabetically") {
					askToSortList();
				} else if (action == "Push marked to bottom") {
					askToPushMarked();
				} else if (action == "Unmark all items in list") {
					askForClearMarked();
				} else if (action == "Delete marked items") {
					askForDeleteCleared(false);
				} else if (action == "Delete all items in list") {
					askForDeleteCleared(true);
				} else {
					Toast.makeText(getApplicationContext(),
							"Unknown Action... [" + action + "]",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		AlertDialog dialog = alert.create();
		dialog.show();
	}

	/***********************************************************************************************************************/

	public void showAlert(String title, String message) {
		AlertDialog.Builder alert = GetAlertDialog(message,
				android.R.drawable.ic_dialog_alert);
		alert.setTitle(Html.fromHtml(title));
		AlertDialog dialog = alert.create();
		dialog.setButton("Whatever", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		dialog.show();
	}

	private void getListInfo() {
		Bundle extras = getIntent().getExtras();
		childListID = extras.getInt("SelectedListID");

		// As long as we have an ID, get the list for that ID from the database
		if (listDAL == null) {
			listDAL = new DataSQL(this);
		}
		// Move the info from the UserList object into the class fields
		childList = listDAL.getListForID(childListID);
		if (childList != null) {
			childListName = childList.ListName;
			this.setTitle(childListName);
		}

	}

	private void ShowEditDialog() {
		UserListItem selectedItem = childList.ListItems.get(selectedIndex);

		AlertDialog.Builder alert = GetAlertDialog("Edit your list item: ",
				android.R.drawable.ic_menu_edit);
		alert.setTitle("Edit Item");

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(7, 0, 7, 0);

		final EditText input = new EditText(this);
		input.setText(selectedItem.ListItem);
		layout.addView(input, params);

		alert.setView(layout);

		alert.setPositiveButton("Save!", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String newText = input.getText().toString();
				UserListItem selectedItem = childList.ListItems
						.get(selectedIndex);
				UserListItem origItem = CloneItem(selectedItem);
				selectedItem.ListItem = newText;
				if (listDAL.updateItem(selectedItem)) {
					listAdapter.notifyDataSetChanged();
					// Create last action object to hold this action
					UserAction lastAction = new UserAction();
					lastAction.isMainList = false;
					lastAction.userAction = Action.EDIT;
					lastAction.actionData = origItem;
					if (undoActions == null) {
						undoActions = new ArrayList<UserAction>();
					}
					undoActions.add(lastAction);
					// Notify user edit was successful
					displayMessage("Item updated successfully!\n<|:^)");
				} else {
					displayMessage("There was a problem updating your stupid item!\n>:^(");
				}
			}
		});
		alert.setNegativeButton("Nevermind",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled, hide keyboard
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(
								input.getWindowToken(),
								WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
					}
				});

		// Create the dialog from the builder, set it so that the keyboard is
		// always visible, then show it
		final AlertDialog dlg = alert.create();
		dlg.getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		dlg.setOnShowListener(new DialogInterface.OnShowListener() {
			public void onShow(DialogInterface dialog) {
				// Show the keyboard
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(input,
						WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			}
		});
		dlg.show();

	}

	private void ShowEntryDialog() {
		AlertDialog.Builder alert = GetAlertDialog(
				"Type in the new list item: ", android.R.drawable.ic_menu_edit);
		alert.setTitle("Add New Item");

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(7, 0, 7, 0);

		final EditText input = new EditText(this);
		layout.addView(input, params);

		alert.setView(layout);

		alert.setPositiveButton("Add it!",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String newText = input.getText().toString();
						AddNewItem(newText);
						// Log.d("kozzer", "Added new item through dialog");

						// Keep allowing user to add items
						ShowEntryDialog();
					}
				});
		alert.setNegativeButton("Go Away",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled, hide keyboard
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(
								input.getWindowToken(),
								WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
					}
				});

		// Create the dialog from the builder, set it so that the keyboard is
		// always visible, then show it
		final AlertDialog dlg = alert.create();
		dlg.getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		dlg.setOnShowListener(new DialogInterface.OnShowListener() {
			public void onShow(DialogInterface dialog) {
				// Show the keyboard
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(input,
						WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			}
		});
		dlg.show();
	}

	/***********************************************************************************************************************************************/
	private void StartVoice() {
		// Runs when the user clicks the mic button in the upper right
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak command...");
		startActivityForResult(intent, VOICE_REQUEST_CODE);
	}

	private void handleVoiceInput(ArrayList<String> matches) {
		// If we're here, we know we've got at least one match - see if we can
		// figure out what it is
		boolean foundMatch = false;
		for (int i = 0; i < matches.size(); i++) {
			String match = matches.get(i);
			if (match.length() > 10
					&& (match.substring(0, 8).contains("new item") || match
							.substring(0, 8).contains("add item"))) {
				// add a new list using the rest of the line
				String newItemName = match.substring(9);

				// Set i to 9999 to exit loop, and set match found flag
				i = 9999;
				foundMatch = true;

				// Add the new item
				AddNewItem(newItemName);

			} else if (match.length() > 11
					&& match.substring(0, 9).contains("mark item")) {

				// Set i to 9999 to exit loop, and set match found flag
				i = 9999;
				foundMatch = true;

				// Just display message for now
				// displayMessage("Mark: [" + match + "]");

			} else if (match.length() > 13
					&& match.substring(0, 11).contains("unmark item")) {

				// Set i to 9999 to exit loop, and set match found flag
				i = 9999;
				foundMatch = true;

				// Just display message for now
				// displayMessage("Unmark: [" + match + "]");

			} else if (match.length() > 13
					&& match.substring(0, 12).contains("delete item")) {

				// Set i to 9999 to exit loop, and set match found flag
				i = 9999;
				foundMatch = true;

				// Just display message for now
				// displayMessage("Delete: [" + match + "]");

			}

		}
		if (foundMatch == false) {
			displayMessage("Sorry... [" + matches.get(0) + "] failed.");
		}

	}

	private void AddNewItem(String newText) {
		if (newText.length() > 0) {
			// Yes, at least one character in the text box - add it to the
			// current list
			// Main list mode
			UserListItem newItem = listDAL.addNewItem(childList, newText);
			if (newItem != null) {
				// New list has been added to the database - so now add it to
				// the in memory array list
				// if (childList.ListItems == null){ childList.ListItems = new
				// ArrayList<UserListItem>(); }
				// childList.ListItems.add(newItem);

				// Save action
				UserAction lastAction = new UserAction();
				lastAction.isMainList = false;
				lastAction.userAction = Action.ADD;
				lastAction.actionData = CloneItem(newItem);
				if (undoActions == null) {
					undoActions = new ArrayList<UserAction>();
				}
				undoActions.add(lastAction);

				// Re-populate the list view
				populateChildList();
			} else {
				// newList is NULL which means the db add failed, display
				// message and that's it
				displayMessage("There was a problem adding your dumb list the stupid database!  >:^(");
			}
		}

	}

	private void askToSortList() {
		AlertDialog.Builder alert = GetAlertDialog("Sort list alphabetically?",
				android.R.drawable.ic_menu_sort_alphabetically);
		alert.setNegativeButton("Nah", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Do nothing
			}
		});
		alert.setPositiveButton("Do it!", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Save the list pre-sort in case user wants to undo
				UserAction lastAction = new UserAction();
				lastAction.isMainList = false;
				lastAction.userAction = Action.CHANGE_SORT;
				lastAction.actionData = CloneList(childList.ListItems);
				if (undoActions == null) {
					undoActions = new ArrayList<UserAction>();
				}
				undoActions.add(lastAction);

				// Sort the list
				childList.SortListItems();
				childList.ListItems = listDAL.SaveCurrentListSort(
						childList.ListID, childList.ListItems);
				listAdapter.notifyDataSetChanged();
				displayMessage("List sorted alphabetically");
			}
		});
		AlertDialog dialog = alert.create();
		dialog.show();
	}

	private void askToPushMarked() {
		AlertDialog.Builder alert = GetAlertDialog(
				"Push marked items to bottom of list?",
				android.R.drawable.ic_menu_agenda);
		alert.setNegativeButton("Nah", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Do nothing
			}
		});
		alert.setPositiveButton("Do it!", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Save the list pre-sort in case user wants to undo
				UserAction lastAction = new UserAction();
				lastAction.isMainList = false;
				lastAction.userAction = Action.CHANGE_SORT;
				lastAction.actionData = CloneList(childList.ListItems);
				if (undoActions == null) {
					undoActions = new ArrayList<UserAction>();
				}
				undoActions.add(lastAction);

				// Push marked to bottom
				childList.PushMarkedToBottom();
				childList.ListItems = listDAL.SaveCurrentListSort(
						childList.ListID, childList.ListItems);
				listAdapter.notifyDataSetChanged();
				displayMessage("Marked items pushed to bottom");
			}
		});
		AlertDialog dialog = alert.create();
		dialog.show();
	}

	private void askForClearMarked() {
		AlertDialog.Builder alert = GetAlertDialog(
				"Restore all items to unmarked?",
				android.R.drawable.ic_menu_set_as);
		alert.setPositiveButton("I guess", new OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				unmarkItems();
			}
		});
		alert.setNegativeButton("No way!", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// nothing - cancel
			}
		});
		AlertDialog dialog = alert.create();
		dialog.show();

	}

	private void unmarkItems() {
		// Save the list pre-sort in case user wants to undo
		UserAction lastAction = new UserAction();
		lastAction.isMainList = false;
		lastAction.userAction = Action.UNMARK_MULTIPLE;
		lastAction.actionData = CloneList(childList.ListItems);
		if (undoActions == null) {
			undoActions = new ArrayList<UserAction>();
		}
		undoActions.add(lastAction);

		for (int i = 0; i < childList.ListItems.size(); i++) {
			UserListItem curItem = childList.ListItems.get(i);
			if (curItem != null) {
				curItem.ItemCleared = false;
				listDAL.setItemCleared(curItem, false);
			}
		}
		// Now that all the items have been reset, re-populate the listview
		listAdapter.notifyDataSetChanged();
		displayMessage("All items unmarked");
	}

	private void toggleMark(int pos) {
		UserListItem selectedItem = childList.ListItems.get(pos);

		UserAction lastAction = new UserAction();
		lastAction.isMainList = false;
		lastAction.userAction = Action.TOGGLE_MARK;
		lastAction.actionData = CloneItem(selectedItem);
		if (undoActions == null) {
			undoActions = new ArrayList<UserAction>();
		}
		undoActions.add(lastAction);

		selectedItem.ItemCleared = listDAL.setItemCleared(selectedItem,
				!selectedItem.ItemCleared);
		listAdapter.notifyDataSetChanged();
	}

	private void askToDeleteOrEdit(int pos) {
		selectedIndex = pos;
		UserListItem selectedItem = childList.ListItems.get(pos);
		AlertDialog.Builder alert = GetAlertDialog("What do you want with '"
				+ selectedItem.ListItem + "'?",
				android.R.drawable.ic_dialog_info);
		alert.setPositiveButton("Delete it!", new OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				deleteItem();
			}
		});
		alert.setNeutralButton("Edit it!", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				ShowEditDialog();
			}
		});
		alert.setNegativeButton("Nothing", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Nothing - cancel
			}
		});
		AlertDialog dialog = alert.create();
		dialog.show();
	}

	private void askForDeleteCleared(Boolean deleteAll) {
		AlertDialog.Builder alert = GetAlertDialog(
				deleteAll ? "Delete all list items?"
						: "Delete marked list items?",
				android.R.drawable.ic_menu_delete);
		if (deleteAll) {
			alert.setPositiveButton("Clear 'em all!", new OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					deleteAllItems();
				}
			});
		} else {
			alert.setPositiveButton("Wipe them out!", new OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					deleteClearedItems();
				}
			});
		}
		alert.setNegativeButton("What?  No!", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// nothing - cancel
			}
		});
		AlertDialog dialog = alert.create();
		dialog.show();
	}

	private void deleteAllItems() {
		// Save the list before deleting them
		UserAction lastAction = new UserAction();
		lastAction.isMainList = false;
		lastAction.userAction = Action.DELETE_MULTIPLE;
		lastAction.actionData = CloneList(childList.ListItems);
		if (undoActions == null) {
			undoActions = new ArrayList<UserAction>();
		}
		undoActions.add(lastAction);

		// in child list, delete cleared
		if (listDAL.deleteAllItemsOnList(childList.ListID)) {
			// Delete of items worked, now remove from memory
			childList.ListItems.clear();
			//populateChildList();
			listAdapter.notifyDataSetChanged();
			displayMessage("All list items removed");
		}
	}

	@SuppressWarnings("unchecked")
	private void deleteClearedItems() {
		// Create last action object
		UserAction lastAction = new UserAction();
		lastAction.isMainList = false;
		lastAction.userAction = Action.DELETE_MULTIPLE;
		lastAction.actionData = new ArrayList<UserListItem>();
		if (undoActions == null) {
			undoActions = new ArrayList<UserAction>();
		}
		undoActions.add(lastAction);

		// in child list, delete cleared
		if (listDAL.deleteClearedItems(childList.ListID)) {
			// Delete of cleared items worked, now remove from memory
			int i = 0;
			while (i < childList.ListItems.size()) {
				UserListItem item = childList.ListItems.get(i);
				if (item.ItemCleared) {
					// Remove item from child list, add it to last action list
					childList.ListItems.remove(item);
					((ArrayList<UserListItem>) lastAction.actionData)
							.add(CloneItem(item));
				} else {
					i++;
				}
			}
			//listAdapter.notifyDataSetChanged();
			populateChildList();
			displayMessage("Marked items removed");
		}
	}

	private void deleteItem() {
		UserListItem selectedItem = childList.ListItems.get(selectedIndex);

		// Save the item, in case user wants to undo
		UserAction lastAction = new UserAction();
		lastAction.isMainList = false;
		lastAction.userAction = Action.DELETE_ONE;
		lastAction.actionData = CloneItem(selectedItem);
		if (undoActions == null) {
			undoActions = new ArrayList<UserAction>();
		}
		undoActions.add(lastAction);

		if (listDAL.deleteItem(selectedItem)) {
			childList.ListItems.remove(selectedItem);
			listAdapter.notifyDataSetChanged();
			displayMessage("Item deleted successfully!\n<):^D");
		} else {
			displayMessage("There was a problem deleting your stupid item!\n>:^(");
		}
	}

	@SuppressWarnings("unchecked")
	private void populateChildList() {
		// See if the DAL exists, if not then create it
		ensureDALExists();

		// See if the data object list is null, if so then populate it
		childList = listDAL.getListForID(childListID);

		// Now populate the main list view with the main list if any list exists
		if (childList != null && childList.ListItems.size() > 0) {
			Collections.sort(childList.ListItems);
			boolean isDarkTheme = selectedPreferences.SelectedTheme
					.equals("dark") ? true : false;
			// Log.d("kozzer", "Theme: " + selectedPreferences.SelectedTheme);
			listAdapter = new UserListItemAdapter(this, childList.ListItems,
					selectedPreferences.ShowNumbering, isDarkTheme);
			childListView.setAdapter(listAdapter);
			childListView.setItemAdapter(listAdapter);
			childListView.setListSize(childList.ListItems.size());
		}
	}

	private void displayMessage(String theMessage) {
		Context context = getApplicationContext();
		CharSequence text = theMessage;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	@SuppressLint("NewApi")
	private AlertDialog.Builder GetAlertDialog(String message, int icon) {
		AlertDialog.Builder alert;
		// alert = new AlertDialog.Builder(this);
		if (selectedPreferences.SelectedTheme != null
				&& selectedPreferences.SelectedTheme.equals("light")) {
			Log.d("kozzer", "Dialog for Light theme");
			alert = (android.os.Build.VERSION.SDK_INT < 11) ? new AlertDialog.Builder(
					this) : new AlertDialog.Builder(this,
					AlertDialog.THEME_HOLO_LIGHT);
		} else {
			Log.d("kozzer", "Dialog for Dark theme (default)");
			alert = (android.os.Build.VERSION.SDK_INT < 11) ? new AlertDialog.Builder(
					this) : new AlertDialog.Builder(this,
					AlertDialog.THEME_HOLO_DARK);
		}
		alert.setTitle(childListName);
		alert.setMessage(message);
		alert.setIcon(icon);
		return alert;
	}

	// Used to clone a single list item, for "undo" purposes
	private UserListItem CloneItem(UserListItem origItem) {
		UserListItem newItem = new UserListItem(origItem.ItemID,
				origItem.ParentListID, origItem.ListItem, origItem.ItemCleared,
				origItem.ItemIndex);
		return newItem;
	}

	// Used to clone an arraylist of list items, for "undo" purposes
	private ArrayList<UserListItem> CloneList(ArrayList<UserListItem> origList) {
		ArrayList<UserListItem> newList = new ArrayList<UserListItem>();
		if (origList != null && origList.size() > 0) {
			for (int i = 0; i < origList.size(); i++) {
				newList.add(CloneItem(origList.get(i)));
			}
		}
		return newList;
	}

}
