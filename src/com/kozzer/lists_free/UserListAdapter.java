package com.kozzer.lists_free;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserListAdapter extends BaseAdapter implements IRemoveListener, IDropListener {
	
	private Context context;
	private ArrayList<UserList> mainList;
	private boolean isDarkTheme;
	
	public UserListAdapter(Context context, ArrayList<UserList> userList, boolean isDarkTheme){
		this.context = context;
		this.mainList = userList;
		this.isDarkTheme = isDarkTheme;
	}
	
    public View getView(int position, View convertView, ViewGroup parent) {
	    UserList userlist = mainList.get(position);
	    //Log.d("kozzer", "List: " + userlist.ListName + ", ID: " + String.valueOf(userlist.ListID) + ", Index: " + String.valueOf(userlist.ListIndex));
	    
	    if (convertView == null) {
	        LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        convertView = inflater.inflate(R.layout.main_list_item, null);
	        convertView.setTag(userlist);
	    }
	    
	    TextView listItem = (TextView) convertView.findViewById(R.id.list_name);
	    listItem.setText(userlist.ListName);
	    
	    ImageView listItemGrabber = (ImageView) convertView.findViewById(R.id.list_grabber);
	    listItemGrabber.setImageResource(this.isDarkTheme ? R.drawable.dark_grabber : R.drawable.light_grabber);
	    
	    int listSize = userlist.ListItems.size();
	    TextView itemCount = (TextView) convertView.findViewById(R.id.item_count);
	    itemCount.setText("(" + String.valueOf(listSize) + (listSize == 1 ? " item" : " items") + ")");
    
	    return convertView;
    }

	public int getCount() {
		//  Auto-generated method stub
		return this.mainList.size();
	}

	public Object getItem(int position) {
		//  Auto-generated method stub
		return mainList.get(position);
	}

	public long getItemId(int position) {
		//  Auto-generated method stub
		return position;
	}

	public void onDrop(int from, int to) {
		//Get the moved item from the original index
		UserList itemToMove = mainList.get(from);
		
		//Log.d("kozzer", "UserListAdatper -> OnDrop");
		
		//Update memory array
		mainList.remove(from);
		mainList.add(to,itemToMove);
		//Make sure DAL is instantiated
		DataSQL listDAL = new DataSQL(this.context);
		//Update the in-memory indexes
		for(int i=0; i < mainList.size(); i++){
			UserList list = mainList.get(i);
			list.ListIndex = i + 1;
			//Update the database
			if (listDAL.updateListIndex(list.ListID, list.ListIndex) != true){
				break;
			}
		}
		//Notify UI to update list
		this.notifyDataSetChanged();
	}

	public void onRemove(int which) {
		//  Auto-generated method stub
		if (which < 0 || which > mainList.size()) return;		
		mainList.remove(which);
	}
}
