package app.com.example.alexnutu.birthdaysms;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.OutputStreamWriter;

import app.com.example.alexnutu.birthdaysms.data.BirthdayContract;
import app.com.example.alexnutu.birthdaysms.data.BirthdayProvider;


public class DetailActivity extends AppCompatActivity {

    String LOG_TAG = "DetailActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    // mshareProvider for SHARE BUTTON
    private ShareActionProvider mShareActionProvider;
    SharedPreferences prefs;
    String tz1;
    String tz2;


    @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.




            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

    private Intent createShareIntent(String tz1, String tz2) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "I am using TimezoneAlarm. I set " + tz1 + " and " + tz2 + " to add alarms! You should check it! https://plus.google.com/+CristinaNicolae/posts/erjLJKMiury " + "#TimezoneAlarm");
        return shareIntent;
    }

        public void savetext () {
            try {

                OutputStreamWriter out = new OutputStreamWriter(openFileOutput("TextFile", MODE_APPEND));
                Button saveButton = (Button) findViewById(R.id.saveButton);
                String text = saveButton.getText().toString();
                out.write(text);
                out.write('\n');
                out.close();
                Toast.makeText(this, "The contents are saved in the file.", Toast.LENGTH_LONG).show();

            } catch (Throwable t) {

                Toast.makeText(this, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();

            }
            //Toast.makeText(this, "Save not implemented yet.", Toast.LENGTH_SHORT).show();
        }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(getApplicationContext(),SettingsActivity.class);
                startActivity(i);
                Toast.makeText(this, "You opened Settings.",Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_save:

                savetext();

                break;

        }
        //noinspection SimplifiableIfStatement

        // STOP CODUL PENTRU SAVE

        return super.onOptionsItemSelected(item);


    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public class DetailFragment extends Fragment {

        private  final String LOG_TAG = DetailFragment.class.getSimpleName();
        private String mBirthdayStr;

        private String action;
        Uri birthdayURI;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            // The detail activity called via Intent . Inspect the intent for forecast data.



            Intent intent = getActivity().getIntent();
            View rootView = inflater.inflate(R.layout.birthday_detail, container, false);

            birthdayURI = (savedInstanceState == null) ? null : (Uri) savedInstanceState
                    .getParcelable(BirthdayProvider.CONTENT_ITEM_TYPE);


            final EditText phoneNumber = (EditText) rootView.findViewById(R.id.etPhoneNumb);
            final EditText name = (EditText) rootView.findViewById(R.id.etName);
            final EditText birthDate = (EditText) rootView.findViewById(R.id.etDate);
            final EditText facebookAccount = (EditText) rootView.findViewById(R.id.etFacebookAccount);
            final EditText message = (EditText) rootView.findViewById(R.id.etAddMessage);
            final ToggleButton notification = (ToggleButton) rootView.findViewById(R.id.notification);

            if (intent != null) {
                action = intent.getStringExtra("action");
                if(action != null){
                    if(action.contains("new")){

                    }else {
                    String birthdayId = intent.getStringExtra("bID");

                        Uri human = BirthdayProvider.CONTENT_URI.withAppendedPath(BirthdayProvider.CONTENT_URI, birthdayId);
                        Cursor c = managedQuery(human, null, null, null, "name");

                        while(c.moveToNext()){
                            name.setText(c.getString(c.getColumnIndexOrThrow(BirthdayContract.BirthdayEntry.COLUMN_NAME)));
                            birthDate.setText(c.getString(c.getColumnIndexOrThrow(BirthdayContract.BirthdayEntry.COLUMN_BIRTHDATE)));
                            phoneNumber.setText(c.getString(c.getColumnIndexOrThrow(BirthdayContract.BirthdayEntry.COLUMN_PHONE_NUMBER)));
                            facebookAccount.setText(c.getString(c.getColumnIndexOrThrow(BirthdayContract.BirthdayEntry.COLUMN_FACEBOOK_ACCOUNT)));
                            message.setText(c.getString(c.getColumnIndexOrThrow(BirthdayContract.BirthdayEntry.COLUMN_MESSAGE)));



                            if (c.getInt((c.getColumnIndexOrThrow(BirthdayContract.BirthdayEntry.COLUMN_NOTIFICATION)))==1) {
                                notification.setChecked(true);
                            }
                            else notification.setChecked(false);

                        }


                    }
                }

                mBirthdayStr =  message.getText().toString();
            }

            LinearLayout sendMessage = (LinearLayout) rootView.findViewById(R.id.sendMessage);



            Button saveButton = (Button) rootView.findViewById(R.id.saveButton);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ContentValues values = new ContentValues();
                    values.put(BirthdayContract.BirthdayEntry.COLUMN_NAME, name.getText().toString());
                    values.put(BirthdayContract.BirthdayEntry.COLUMN_PHONE_NUMBER, phoneNumber.getText().toString());
                    values.put(BirthdayContract.BirthdayEntry.COLUMN_BIRTHDATE, birthDate.getText().toString());
                    values.put(BirthdayContract.BirthdayEntry.COLUMN_FACEBOOK_ACCOUNT, facebookAccount.getText().toString());
                    values.put(BirthdayContract.BirthdayEntry.COLUMN_MESSAGE, message.getText().toString());

                    if(notification.isChecked())
                        values.put(BirthdayContract.BirthdayEntry.COLUMN_NOTIFICATION, 1);
                    else
                        values.put(BirthdayContract.BirthdayEntry.COLUMN_NOTIFICATION, 0);
                    if (getActivity().getIntent().getStringExtra("action").contains("new"))
                    {
                       birthdayURI = getActivity().getContentResolver().insert(BirthdayProvider.CONTENT_URI, values);
                    }
                    else {
                        getActivity().getContentResolver().update(BirthdayProvider.CONTENT_URI, values, BirthdayContract.BirthdayEntry.COLUMN_ID+"=?",new String[] {String.valueOf(getActivity().getIntent().getStringExtra("bID"))});
                    }

                    Intent myIntent = new Intent(getActivity(), MainActivity.class);
                    getActivity().startActivity(myIntent);
                }
            });

            sendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                    smsIntent.setType("vnd.android-dir/mms-sms");
                    smsIntent.putExtra("address", phoneNumber.getText().toString());
                    smsIntent.putExtra("sms_body", message.getText().toString());
                    startActivity(smsIntent);
                }
            });

                return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detail, menu);


            MenuItem menuItem = menu.findItem(R.id.action_share);
            ShareActionProvider mShareActionProvider =
                    (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);


            if (mShareActionProvider != null ) {
                mShareActionProvider.setShareIntent(createShareBirthdayIntent());
            } else {
                Log.d(LOG_TAG, "Share Action Provider is null?");
            }
        }

    private Intent createShareBirthdayIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,mBirthdayStr + " YEEY");

        return shareIntent;
    }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {

            int id = item.getItemId();


            return super.onOptionsItemSelected(item);
        }

    }
}
