package comp5216.sydney.edu.au.grocerylist;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    // Define variables
    ListView listView;
    ArrayList<String> dates;
    ArrayList<ArrayList<String>> allItems;
    ArrayList<String> items;
    ArrayAdapter<String> itemsAdapter;
    EditText addItemEditText;
    CalendarView calendarView;
    String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use "activity_main.xml" as the layout
        setContentView(R.layout.activity_main);

        // Reference the "listView" variable to the id "lstView" in the layout
        listView = (ListView) findViewById(R.id.lstView);
        addItemEditText = (EditText) findViewById(R.id.txtNewItem);
        calendarView = (CalendarView) findViewById(R.id.calendarView);

        // Set current date
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        selectedDate = currentYear + "-" + currentMonth + "-" + currentDay;

        // Create an ArrayList of String
        items = new ArrayList<String>();
        dates = new ArrayList<String>();
        allItems = new ArrayList<ArrayList<String>>();

        // Create an adapter for the list view using Android's built-in item layout
        itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                items);
        // Connect the listView and the adapter
        listView.setAdapter(itemsAdapter);

        setupListViewListener();
        setupCalanderViewListener();

    }

    public void onAddItemClick(View view) {
        String toAddString = addItemEditText.getText().toString();
        if (toAddString != null && toAddString.length() > 0) {
            itemsAdapter.add(toAddString); // Add text to list view adapter
            addItemEditText.setText("");
        }
    }

    private void setupListViewListener() {
        // Register a request to start an activity for result and register the result callback
        ActivityResultLauncher<Intent> mLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Extract name value from result extras
                        String editedItem = result.getData().getExtras().getString("item");
                        int position = result.getData().getIntExtra("position", -1);
                        items.set(position, editedItem);
                        Log.i("Updated item in list ", editedItem + ", position: " + position);
                        // Make a standard toast that just contains text
                        Toast.makeText(getApplicationContext(), "Updated: " + editedItem,
                                Toast.LENGTH_SHORT).show();
                        itemsAdapter.notifyDataSetChanged();
                    }
                }
        );

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int
                    position, long rowId)
            {
                Log.i("MainActivity", "Long Clicked item " + position);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.dialog_delete_title)
                    .setMessage(R.string.dialog_delete_msg)
                    .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            items.remove(position); // Remove item from the ArrayList
                            itemsAdapter.notifyDataSetChanged(); // Notify listView adapter to update the list
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User cancelled the dialog
                            // Nothing happens
                        }
                    });
                builder.create().show();
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String updateItem = (String) itemsAdapter.getItem(position);
                Log.i("MainActivity", "Clicked item " + position + ": " + updateItem);
                Intent intent = new Intent(MainActivity.this, EditToDoItemActivity.class);
                if (intent != null) {
                    // put "extras" into the bundle for access in the edit activity
                    intent.putExtra("item", updateItem);
                    intent.putExtra("position", position);
                    // brings up the second activity
                    mLauncher.launch(intent);
                    itemsAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void setupCalanderViewListener() {
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // save the data
                int index = dates.indexOf(selectedDate);
                ArrayList<String> itemsToSave = new ArrayList<String>(items);
                if (index >= 0) {
                    allItems.set(index, itemsToSave);
                }
                else {
                    if (itemsToSave.size() > 0) {
                        dates.add(selectedDate);
                        allItems.add(itemsToSave);
                    }
                }

                // updates the list view with new data
                selectedDate = year + "-" + month + "-" + dayOfMonth;
                index = dates.indexOf(selectedDate);
                items.clear();
                if (index >= 0) {
                    ArrayList<String> savedItems = allItems.get(index);
                    items.addAll(savedItems);
                }
                itemsAdapter.notifyDataSetChanged();
            }
        });
    }

}
