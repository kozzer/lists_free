package com.kozzer.lists_free;

import android.view.View;
import android.widget.ListView;


public interface IDragListener {
	
	void onDrag(int x, int y, ListView listView);
	void onStopDrag(View itemView);
	void onStartDrag(View item);

}
