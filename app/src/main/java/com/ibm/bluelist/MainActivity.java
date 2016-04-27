package com.ibm.bluelist;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudant.sync.datastore.BasicDocumentRevision;
import com.cloudant.sync.datastore.DocumentRevision;
import com.cloudant.sync.notifications.ReplicationCompleted;
import com.cloudant.sync.notifications.ReplicationErrored;
import com.cloudant.sync.query.QueryResult;
import com.cloudant.sync.replication.Replicator;
import com.google.common.eventbus.Subscribe;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.ibm.bluelist.BlueListApplication.TODO_ITEM_NAME_KEY;


/**
 * The {@code MainActivity} is the primary visual activity shown when the app is being interacted with. Most of the code is UI and visuals.
 */
public class MainActivity extends Activity {

    private static final int CAMERA_REQUEST_CODE = 1;

    private static final String TAG = MainActivity.class.getSimpleName();

    private ListView mListView; // Main ListView
    private List<DocumentRevision> mTodoItemList; // The list of TodoItems
    private TodoItemAdapter mTodoItemAdapter; // Adapter for bridging the list of TodoItems with the ListView

    private SwipeRefreshLayout mSwipeLayout; // Swipe refresh for data replication

    private ActionBar mActionBar; // Action bar for navigating between tabs

    private BlueListApplication mApplication; // Application holds global variables for working with data

    private boolean push; // Keeps track of current push state (registered : not registered)


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApplication = BlueListApplication.getInstance();

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView titleView = (TextView) findViewById(titleId);
        titleView.setTypeface(mApplication.getTypeFace());

        initListView();
        initSwipeRefresh();
        initTabs();

        push = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.push_switch, menu);

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        // If MainActivity is resumed from a stopped state, ensure the Datastore and pull/push Replicators are configured
        mApplication.initialize();

        // load list of TodoItems
        loadList();
    }

    @Override
    public void onStop() {

        // When MainActvity is stopped ensure Datastore is closed and other global variables are torn down
        mApplication.tearDown();
        super.onStop();
    }

    @SuppressWarnings("Convert2Diamond")
    private void initListView() {
        // Get MainActivity's ListView
        mListView = (ListView) findViewById(R.id.listView);

        // Init array to hold TodoItem DocumentRevisions
        mTodoItemList = new ArrayList<DocumentRevision>();

        // Set ListView adapter for displaying TodoItems
        mTodoItemAdapter = new TodoItemAdapter(getBaseContext(), mTodoItemList);
        mListView.setAdapter(mTodoItemAdapter);

        // Set long click listener for TodoItems to be deleted
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(android.widget.AdapterView<?> parent, View view, int position, long id) {

                // Grab TodoItem to delete from current showing list
                DocumentRevision todoItem = mTodoItemList.get(position);

                // Delete TodoItem from Datastore
                DocumentRevision deletedRevision = mApplication.removeTodoItem(todoItem);

                loadList();

                // Callback is consumed if the revision is successfully deleted
                return ((BasicDocumentRevision) deletedRevision).isDeleted();
            }
        });
    }

    private void initSwipeRefresh() {

        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        mSwipeLayout.setColorSchemeResources(R.color.white, R.color.black, R.color.light_blue);

        // Set swipe refresh listener for Cloudant sync on pull-down of the list
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sync();
            }
        });

        mSwipeLayout.setProgressBackgroundColorSchemeResource(R.color.blue);
    }

    private void initTabs() {
        mActionBar = getActionBar();

        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.TabListener listener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                loadList();
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
        };

        // Configure All priority tab
        ActionBar.Tab allTab = mActionBar.newTab();
        TextView allTextView = (TextView) getLayoutInflater().inflate(R.layout.tab_title, null);
        allTextView.setText("All");
        allTextView.setTypeface(mApplication.getTypeFace());
        allTab.setCustomView(allTextView);
        allTab.setTabListener(listener);

        // Configure Medium priority tab
        ActionBar.Tab mediumTab = mActionBar.newTab();
        TextView mediumTextView = (TextView) getLayoutInflater().inflate(R.layout.tab_title, null);
        mediumTextView.setText("Medium");
        mediumTextView.setTypeface(mApplication.getTypeFace());
        mediumTab.setCustomView(mediumTextView);
        mediumTab.setTabListener(listener);

        // Configure High priority tab
        ActionBar.Tab highTab = mActionBar.newTab();
        TextView highTextView = (TextView) getLayoutInflater().inflate(R.layout.tab_title, null);
        highTextView.setText("High");
        highTextView.setTypeface(mApplication.getTypeFace());
        highTab.setCustomView(highTextView);
        highTab.setTabListener(listener);

        mActionBar.addTab(allTab);
        mActionBar.addTab(mediumTab);
        mActionBar.addTab(highTab);

        mActionBar.selectTab(allTab);// TODO gives null pointer on rotation
    }

    private void loadList() {

        // Set the list based on which button was toggled
        QueryResult result = mApplication.getAllTodoItems();

        mTodoItemList.clear();
        if (result != null) {
            for (DocumentRevision todoItem : result) {
                mTodoItemList.add(todoItem);
            }
        }
        mTodoItemAdapter.notifyDataSetChanged();
    }

    public void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                final Bitmap photo = (Bitmap)data.getExtras().get("data");
                final Dialog addDialog = new Dialog(this);

                addDialog.setContentView(R.layout.add_edit_dialog);
                addDialog.setTitle("Add Todo");

                ImageView photoView = (ImageView) addDialog.findViewById(R.id.photo);
                photoView.setImageBitmap(photo);

                TextView textView = (TextView) addDialog.findViewById(R.id.todo);
                textView.setTypeface(mApplication.getTypeFace());
                textView = (TextView) addDialog.findViewById(android.R.id.title);
                if (textView != null) {
                    textView.setGravity(Gravity.CENTER);
                    textView.setTypeface(mApplication.getTypeFace());
                }

                addDialog.setCancelable(true);
                Button add = (Button) addDialog.findViewById(R.id.Add);
                add.setTypeface(mApplication.getTypeFace());
                addDialog.show();

                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText itemToAdd = (EditText) addDialog.findViewById(R.id.todo);
                        final String name = itemToAdd.getText().toString();
                        // If text was added, continue with normal operations
                        if (!name.isEmpty()) {

                            // Reload list for new data
                            loadList();
                        }

                        // Kill dialog when finished, or if no text was added
                        addDialog.dismiss();
                    }
                });
            } else if (resultCode == RESULT_CANCELED) {
                System.out.println("CANCELED NO PHOTOOOO");
            }
        }
    }

    private String getStringFromBitmap(Bitmap bitmapPicture) {
 /*
 * This functions converts Bitmap picture to a string which can be
 * JSONified.
 * */
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }

    /**
     * Launches a dialog for adding a new TodoItem. Called when plus button is tapped.
     *
     * @param view The plus button that is tapped.
     */
    public void addTodo(View view) {
        openCamera();
    }

    /**
     * Registers app with GCM service
     * @param pushToggle The Menu button selected
     */
    public void togglePush(final MenuItem pushToggle) {

        // Uses a callback to react to push registration success/fail cases asynchronously
        mApplication.enablePush(push, new BlueListApplication.Callback() {
            @Override
            public void success(Object object) {

                final String message;
                if (push) {
                    message = "Push enabled!";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pushToggle.setTitle("Disable Push");
                        }
                    });
                } else {
                    message = "Push disabled!";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pushToggle.setTitle("Enable Push");
                        }
                    });
                }

                push = !push;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void error(final Throwable e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Toggle Push failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    /**
     * Launches a dialog for updating the TodoItem name. Called when the list item is tapped.
     *
     * @param view The TodoItem that is tapped.
     */
    public void editTodoName(View view) {
        // Gets position in list view of tapped item
        final Integer pos = mListView.getPositionForView(view);
        final Dialog addDialog = new Dialog(this);

        addDialog.setContentView(R.layout.add_edit_dialog);
        addDialog.setTitle("Edit Todo");
        TextView textView = (TextView) addDialog.findViewById(android.R.id.title);
        if (textView != null) {
            textView.setGravity(Gravity.CENTER);
            textView.setTypeface(mApplication.getTypeFace());
        }
        addDialog.setCancelable(true);
        EditText et = (EditText) addDialog.findViewById(R.id.todo);
        et.setTypeface(mApplication.getTypeFace());

        final BasicDocumentRevision todoItem = (BasicDocumentRevision) mTodoItemList.get(pos);
        final String name = (String) todoItem.getBody().asMap().get(TODO_ITEM_NAME_KEY);
        et.setText(name);

        Button addDone = (Button) addDialog.findViewById(R.id.Add);
        addDone.setTypeface(mApplication.getTypeFace());
        addDialog.show();

        addDone.setOnClickListener(new View.OnClickListener() {
            // Save text inputted when done is tapped
            @Override
            public void onClick(View view) {
                EditText editedText = (EditText) addDialog.findViewById(R.id.todo);

                String newName = editedText.getText().toString();

                if (!newName.isEmpty()) {
                    mApplication.editTodoItem(todoItem, newName, null);

                    // Reload list for new data
                    loadList();
                }
                addDialog.dismiss();
            }
        });
    }

    /**
     * Synchronize Datastore and remote database
     */
    private void sync() {
        final Replicator pullReplicator = mApplication.getPullReplicator();
        final Replicator pushReplicator = mApplication.getPushReplicator();

        // Start pull replication
        pullReplicator.getEventBus().register(new Object() {

            // After pull replication completes start push replication
            @Subscribe
            public void complete(ReplicationCompleted event) {
                pullReplicator.getEventBus().unregister(this);
                Log.d(TAG, String.format("Pull replication complete. %d documents replicated.", event.documentsReplicated));

                pushReplicator.getEventBus().register(new Object() {

                    // After push replication completes
                    @Subscribe
                    public void complete(ReplicationCompleted event) {
                        pushReplicator.getEventBus().unregister(this);
                        Log.d(TAG, String.format("Push replication complete. %d documents replicated.", event.documentsReplicated));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Reload list for new data
                                loadList();

                                // Notify refresh spinner that replication has stopped
                                mSwipeLayout.setRefreshing(false);
                            }
                        });
                    }

                    @Subscribe
                    public void error(ReplicationErrored event) {
                        pushReplicator.getEventBus().unregister(this);
                        Log.e(TAG, "Failed to complete push replication", event.errorInfo.getException());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                // Notify refresh spinner that replication has stopped
                                mSwipeLayout.setRefreshing(false);
                            }
                        });
                    }
                });
                pushReplicator.start();
            }

            @Subscribe
            public void error(ReplicationErrored event) {
                pullReplicator.getEventBus().unregister(this);
                Log.e(TAG, "Failed to complete pull replication", event.errorInfo.getException());

                // Notify refresh spinner that replication has stopped
                mSwipeLayout.setRefreshing(false);
            }
        });

        pullReplicator.start();
    }

}
