package app.com.example.alexnutu.birthdaysms;


import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Calendar;

import app.com.example.alexnutu.birthdaysms.data.BirthdayContract.BirthdayEntry;
import app.com.example.alexnutu.birthdaysms.data.BirthdayProvider;

/**
 * A placeholder fragment containing a simple view.
 */
public class BirthdayFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private ArrayAdapter<String> birthdayAdapter;
    private SimpleCursorAdapter adapter;

    public BirthdayFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);



//
//        Cursor cursor = getContentResolver().query(People.CONTENT_URI, new String[] {People._ID, People.NAME, People.NUMBER}, null, null, null);
//        startManagingCursor(cursor);


        String[] from = new String[]{BirthdayEntry.COLUMN_NAME, BirthdayEntry.COLUMN_BIRTHDATE, BirthdayEntry.COLUMN_ID, BirthdayEntry.COLUMN_NOTIFICATION};
        // Fields on the UI to which we map
        int[] to = new int[]{R.id.tvName, R.id.tvDate, R.id.birthdayID, R.id.tvAge};

        getLoaderManager().initLoader(0, null, this);

        adapter = new SimpleCursorAdapter(getActivity(), R.layout.listview_item, null, from,
                to, 0);

        ListView listViewPeople = (ListView) rootView.findViewById(R.id.listview_people);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                                  @Override
                                  public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                                      if (view.getId() == R.id.tvAge) {
                                          int getIndex = cursor.getColumnIndex(BirthdayEntry.COLUMN_BIRTHDATE);
                                          String date = cursor.getString(getIndex);
                                          TextView age = (TextView) view;
                                          age.setText(calculateAge(date) + " years");
                                          return true;
                                      }
                                      return false;
                                  }});
        listViewPeople.setAdapter(adapter);

        listViewPeople.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView bID = (TextView) view.findViewById(R.id.birthdayID);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("bID", bID.getText().toString());
                intent.putExtra("action", "edit");
                startActivity(intent);

            }
        });

        listViewPeople.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                removeDialog(view);

                return false;
            }
        });


        adapter.notifyDataSetChanged();

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection =
                {BirthdayEntry.COLUMN_ID, BirthdayEntry.COLUMN_NAME, BirthdayEntry.COLUMN_BIRTHDATE, BirthdayEntry.COLUMN_PHONE_NUMBER, BirthdayEntry.COLUMN_MESSAGE,
                        BirthdayEntry.COLUMN_FACEBOOK_ACCOUNT, BirthdayEntry.COLUMN_NOTIFICATION};

        CursorLoader cursorLoader = new CursorLoader(getActivity().getApplicationContext(),
                BirthdayEntry.CONTENT_URI, projection,  null, null, "NAME ASC");

        return cursorLoader;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public int calculateAge(String date)
    {
        String[] dateParsed = date.split("/");
        if (dateParsed.length == 3) {
            int year = Integer.valueOf(dateParsed[2]);
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            return currentYear - year;
        }
        else return 0;

    }

    public void removeDialog(final View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("Delete person?");
        alertDialogBuilder.setPositiveButton("Delete",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        TextView textView = (TextView) view.findViewById(R.id.birthdayID);

                        Uri uri = Uri.parse(BirthdayProvider.CONTENT_URI + "/"
                                + textView.getText());
                        getActivity().getContentResolver().delete(uri, null, null);
                    }
                });

        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

}
