package com.kozzer.lists_free;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;

public class DragAndDropListView extends ListView {

	//********************************************************************************************************
	//THIS ENTIRE INHERITED CLASS IS FOR DRAG AND DROP FUNCTIONALITY *****************************************
	//********************************************************************************************************
	
	boolean mDragMode;
	UserListItemAdapter mItemAdapter;
	UserListAdapter mListAdapter;
	Context theContext;

	int mListSize;
	int mStartPosition;
	int mEndPosition;
	int mDragPointOffset;		//Used to adjust drag view location
	
	ImageView mDragView;
	GestureDetector mGestureDetector;
	
	IDropListener mDropListener;
	IRemoveListener mRemoveListener;
	IDragListener mDragListener;

	public DragAndDropListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		theContext = context;
	}
	
	public void setDropListener(IDropListener l) {
		mDropListener = l;
	}

	public void setRemoveListener(IRemoveListener l) {
		mRemoveListener = l;
	}
	
	public void setDragListener(IDragListener l) {
		mDragListener = l;
	}

	public void setItemAdapter(UserListItemAdapter adapter)
	{
		mItemAdapter = adapter;
	}
	
	public void setListAdapter(UserListAdapter adapter){
		mListAdapter = adapter;
	}
	
	public void setListSize(int listSize){
		mListSize = listSize;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		final int x = (int) ev.getX();
		final int y = (int) ev.getY();	
		
		if (action == MotionEvent.ACTION_DOWN && x > ((this.getWidth() * 8)/10)) {
			mDragMode = true;
		}

		if (!mDragMode) 
			return super.onTouchEvent(ev);

		//Get the height of the screen
		DisplayMetrics metrics = theContext.getResources().getDisplayMetrics();
		int height = metrics.heightPixels;	
	
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				//Get the positions of the visible top and bottom
				mStartPosition = pointToPosition(x,y);				
				if (mStartPosition != INVALID_POSITION) {
					int mItemPosition = mStartPosition - getFirstVisiblePosition();
                    mDragPointOffset = y - getChildAt(mItemPosition).getTop();
                    mDragPointOffset -= ((int)ev.getRawY()) - y;
					startDrag(mItemPosition,y);
					drag(0,y);// replace 0 with x if desired
				}	
				break;
			case MotionEvent.ACTION_MOVE:
				//Move the visual drag item with the user
				drag(0,y); 	
				//See what the current screen position of the drag is						
				if (y > height - 172){
					//Bottom of list, scroll down
					if (getLastVisiblePosition() < mListSize){
						this.smoothScrollToPosition(getLastVisiblePosition() + 1);
					}
				} else if (y < 0){
					//Above list, scroll up
					if (getFirstVisiblePosition() > 0){
						this.smoothScrollToPosition(getFirstVisiblePosition() - 1);
					}
				} else {
					//Not above or below list - on the list.  so no scrolling needed, and we've already moved the visual dragged item, so do nothing
					//this 'else' here just for documentation purposes
				}
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			default:
				//Get the positions of the visible top and bottom
				mEndPosition = pointToPosition(x,y);
				mDragMode = false;
				stopDrag(mStartPosition - getFirstVisiblePosition());
				if (mEndPosition == -1){
					//-1 means user dragged off the list.  so, get the y-coordinate and determine if it was above the list or below it
					if (y < 0){
						//Less than 0 means above the list
						mEndPosition = getFirstVisiblePosition();
					} else {
						//Probably below the list, get screen dimensions and check against y-coord
						if (y > height - 172){																			
							//172 is kind of arbitrary, but I'm guessing the total of the notification bar, app title bar, bottom menu bar, and height of a single-line list item
							mEndPosition = getLastVisiblePosition();
						}
					}
				}
				if (mItemAdapter != null && mStartPosition != INVALID_POSITION && mEndPosition != INVALID_POSITION) {
					mItemAdapter.onDrop(mStartPosition, mEndPosition);
				} else {
					if (mListAdapter != null && mStartPosition != INVALID_POSITION && mEndPosition != INVALID_POSITION){
						mListAdapter.onDrop(mStartPosition, mEndPosition);
					}
				}

				break;
		}
		return true;
	}	
	
	// move the drag view
	private void drag(int x, int y) {
		if (mDragView != null) {
			WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mDragView.getLayoutParams();
			layoutParams.x = x;
			layoutParams.y = y - mDragPointOffset;
			WindowManager mWindowManager = (WindowManager) getContext()
					.getSystemService(Context.WINDOW_SERVICE);
			mWindowManager.updateViewLayout(mDragView, layoutParams);

			if (mDragListener != null)
				mDragListener.onDrag(x, y, null);// change null to "this" when ready to use
		}
	}

	// enable the drag view for dragging
	private void startDrag(int itemIndex, int y) {
		stopDrag(itemIndex);

		View item = (View) getChildAt(itemIndex);
		if (item == null) return;
		item.setDrawingCacheEnabled(true);
		if (mDragListener != null)
			mDragListener.onStartDrag(item);
		
        // Create a copy of the drawing cache so that it does not get recycled
        // by the framework when the list tries to clean up memory
        Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());
        
        WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.gravity = Gravity.TOP;
        mWindowParams.x = 0;
        mWindowParams.y = y - mDragPointOffset;

        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = 0;
        
        Context context = getContext();
        ImageView v = new ImageView(context);
        v.setImageBitmap(bitmap);      

        WindowManager mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(v, mWindowParams);
        mDragView = v;
	}

	// destroy drag view
	private void stopDrag(int itemIndex) {
		if (mDragView != null) {
			if (mDragListener != null)
				mDragListener.onStopDrag(getChildAt(itemIndex));
            mDragView.setVisibility(GONE);
            WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(mDragView);
            mDragView.setImageDrawable(null);
            mDragView = null;
        }
	}
	
}
