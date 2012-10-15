package com.kozzer.lists_free;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserListItemAdapter extends BaseAdapter implements IRemoveListener, IDropListener {
	
	private Context context;
	private ArrayList<UserListItem> listItems;
	private boolean showNumbers;
	private boolean isDarkTheme;
	
	public UserListItemAdapter(Context context, ArrayList<UserListItem> userListItems, boolean showNumbers, boolean isDarkTheme){
		this.context = context;
		this.listItems = userListItems;
		this.showNumbers = showNumbers;
		this.isDarkTheme = isDarkTheme;
	}

	//Untouched for drag and drop
    public View getView(int position, View convertView, ViewGroup parent) {
    	
	    UserListItem listItem = listItems.get(position);
	    //Log.d("kozzer", "Item: " + listItem.ListItem + ", ID: " + String.valueOf(listItem.ItemID) + ", Index: " + String.valueOf(listItem.ItemIndex));

	    
	    if (convertView == null) {
	        LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        convertView = inflater.inflate(R.layout.child_list_item, null);
	    }
	    
	    //Set the list item text
	    TextView itemNumber = (TextView) convertView.findViewById(R.id.item_number);
	    //Log.d("kozzer", "About to get ImageView");  
	    ImageView listItemGrabber = (ImageView) convertView.findViewById(R.id.item_grabber);
	    listItemGrabber.setImageResource(this.isDarkTheme ? R.drawable.dark_grabber : R.drawable.light_grabber);
	    
	    //Log.d("kozzer", "About to get the TextView");
	    TextView listItemView = (TextView) convertView.findViewById(R.id.list_item);
	    //Log.d("kozzer", "After getting the TextView");
	    if (showNumbers){
	    	itemNumber.setText(String.format("%2s", String.valueOf(position + 1)));
	    } else {
	    	itemNumber.setVisibility(View.GONE);
	    }
	    listItemView.setText(listItem.ListItem);
	    
	    //Get the marked/unmarked colors from the active theme
	    TypedArray atts = context.obtainStyledAttributes(R.styleable.childListItemColors);
	    ColorStateList unmarked = atts.getColorStateList(R.styleable.childListItemColors_unmarkedItemColor);
	    ColorStateList marked = atts.getColorStateList(R.styleable.childListItemColors_markedItemColor);
	    
	    if (listItem.ItemCleared == true){
	    	listItemView.setTextColor(marked);
	    } else {
	    	listItemView.setTextColor(unmarked);
	    }
	    
	    return convertView;
    }

	public int getCount() {
		//  Auto-generated method stub
		return this.listItems.size();
	}

	public UserListItem getItem(int position) {
		//  Auto-generated method stub
		return listItems.get(position);
	}

	public long getItemId(int position) {
		//  Auto-generated method stub
		return position;
	}
	
	//All below code added for drag and drop ******************************************************************
	public void onDrop(int from, int to) {
		//Get the moved item from the original index
		UserListItem itemToMove = listItems.get(from);
		
		//Database operation successful, update memory array
		listItems.remove(from);
		listItems.add(to,itemToMove);		
		//Make sure DAL is instantiated
		DataSQL listDAL = new DataSQL(this.context);
		//Update the in-memory indexes
		for(int i=0; i < listItems.size(); i++){
			UserListItem item = listItems.get(i);
			item.ItemIndex = i + 1;
			//Update the database
			if (listDAL.updateListItemIndex(item.ParentListID, item.ItemID, item.ItemIndex) != true){
				break;
			}
		}
		//Notify UI to update list
		this.notifyDataSetChanged();
	}

	public void onRemove(int which) {
		//  Auto-generated method stub
		if (which < 0 || which > listItems.size()) return;		
		listItems.remove(which);
	}
	
    static class ViewHolder {
        TextView text;
    }
}
