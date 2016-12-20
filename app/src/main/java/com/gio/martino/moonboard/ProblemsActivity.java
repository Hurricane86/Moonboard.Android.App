package com.gio.martino.moonboard;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class ProblemsActivity extends Activity {

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        ProblemsActivity owner;
        Random rand = new Random();

        public GestureListener(ProblemsActivity owner)
        {
            this.owner = owner;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {

            int position = rand.nextInt(owner.problemsCount);
            owner.gotoProblem(position, INTERACTION_TYPE.DOUBLE_TAP);

            Toast.makeText(getApplicationContext(), "new random problem!", Toast.LENGTH_SHORT).show();

            return true;
        }
    }

    private AppDbAdapter db = null;
    private UserDbHelper userDbHelper = null;
    ProblemUserDbRecord problemUserDbRecord = null;
    private Cursor problemsCursor = null;
    private int currentProblemIndex = 0;
    public int problemsCount = 0;
    private int currentProblemId = 0;
    private byte[] holds = null;
    private boolean sendingFreezed = false;
    private GestureDetector gestureDetector;;

    public enum INTERACTION_TYPE
    {
        VIEW_PAGER,
        SEEK_BAR,
        VOLUME_BUTTONS,
        DOUBLE_TAP,
        MOONBOARD,
        REFRESHING
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return gestureDetector.onTouchEvent(e);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gestureDetector = new GestureDetector(getApplicationContext(), new GestureListener(this));

        //Remove title bar
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //set content view AFTER ABOVE sequence (to avoid crash)
        setContentView(R.layout.activity_problems);

        ViewPager viewPager = (ViewPager) findViewById(R.id.problemViewPager);
        ProblemsImageViewAdapter adapter = new ProblemsImageViewAdapter(this, getIntent());
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                gotoProblem(position + 1, INTERACTION_TYPE.VIEW_PAGER);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return gestureDetector.onTouchEvent(event);
            }
        });

        Intent intent = getIntent();

        int holdsType = intent.getIntExtra("holdsType", 0);
        int holdsSetup = intent.getIntExtra("holdsSetup", 0);
        int fromGrade = intent.getIntExtra("fromGrade", 0);
        int toGrade = intent.getIntExtra("toGrade", 0);
        String author = intent.getStringExtra("author");

        db = new AppDbAdapter(getApplicationContext());
        db.createDatabase();
        db.open();

        userDbHelper = new UserDbHelper(getApplicationContext());

        problemsCursor = db.getProblems(holdsType, holdsSetup, fromGrade, toGrade, author);
        problemsCount = problemsCursor.getCount();
        currentProblemIndex = 1;

        SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setMax(problemsCount - 1);
        seekBar.setEnabled(problemsCount >= 2);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    gotoProblem(progress + 1, INTERACTION_TYPE.SEEK_BAR);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                sendingFreezed = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendingFreezed = false;
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                boolean realtimeUpdate = settings.getBoolean("realtimeUpdate", false);
                if (realtimeUpdate)
                    sendProblemToMoonboard(false);
            }
        });

        MoonboardCommunicationService service = ((MoonboardApplication)getApplication()).getMoonboardCommunicationService();
        if(service != null)
        {
            service.messageReceiver = new MoonboardCommunicationService.MessageReceiver() {
                @Override
                public void onMessageReceived(int messageType, byte[] buffer) {
                    switch (messageType) {
                        case 1:
                            gotoProblem(currentProblemIndex + 1, INTERACTION_TYPE.MOONBOARD);
                            break;
                        case 2:
                            gotoProblem(currentProblemIndex - 1, INTERACTION_TYPE.MOONBOARD);
                            break;
                    }

                    try {
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                        r.play();
                    } catch (Exception e) {
                    }
                }
            };
        }

        gotoProblem(currentProblemIndex, INTERACTION_TYPE.REFRESHING);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    gotoProblem(currentProblemIndex+1, INTERACTION_TYPE.VOLUME_BUTTONS);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    gotoProblem(currentProblemIndex-1, INTERACTION_TYPE.VOLUME_BUTTONS);
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    private void sendProblemToMoonboard(boolean notifyToUser)
    {
        if(sendingFreezed)
            return;

        boolean result = ((MoonboardApplication)getApplication()).getMoonboardCommunicationService().send(MoonboardCommunicationService.MESSAGE_TYPE_SET_PROBLEM, holds);

        if(notifyToUser)
        {
            if(result)
                Toast.makeText(ProblemsActivity.this, "This problem was send to the Moonboard!", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(ProblemsActivity.this, "Error sending the problem to the Moonboard!", Toast.LENGTH_SHORT).show();
        }
    }

    private void markProblemAsDone()
    {
        Toast.makeText(ProblemsActivity.this, "This problem was marked as DONE!", Toast.LENGTH_SHORT).show();

        problemUserDbRecord.setIsDone(true);
    }

    private void markProblemAsNotDone()
    {
        Toast.makeText(ProblemsActivity.this, "This problem was marked as NOT DONE!", Toast.LENGTH_SHORT).show();

        problemUserDbRecord.setIsDone(false);
    }

    private void addProblemToCircuit(String circuitName)
    {
        Toast.makeText(ProblemsActivity.this, "This problem was added to the circuit " + circuitName, Toast.LENGTH_SHORT).show();

        //@TODO:...
    }

    private void removeProblemFromCircuit(String circuitName)
    {
        Toast.makeText(ProblemsActivity.this, "This problem was removed to the circuit " + circuitName, Toast.LENGTH_SHORT).show();

        //@TODO:...
    }

    private void addProblemToWishlist()
    {
        Toast.makeText(ProblemsActivity.this, "This problem was added to your wishlist", Toast.LENGTH_SHORT).show();

        problemUserDbRecord.setIsInWishlist(true);
    }

    private void removeProblemToWishlist()
    {
        Toast.makeText(ProblemsActivity.this, "This problem was removed from your wishlist", Toast.LENGTH_SHORT).show();

        problemUserDbRecord.setIsInWishlist(false);
    }

    public void gotoProblem(int newProblemIndex, INTERACTION_TYPE interactionType)
    {
        if(newProblemIndex == currentProblemIndex &&
                interactionType != INTERACTION_TYPE.REFRESHING)
            return;

        if(newProblemIndex <= 0)
            currentProblemIndex = 1;
        else if(newProblemIndex > problemsCount)
            currentProblemIndex = problemsCount;
        else
            currentProblemIndex = newProblemIndex;

        problemsCursor.moveToPosition(currentProblemIndex - 1);

        switch(interactionType)
        {

            case VIEW_PAGER: {
                SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
                seekBar.setProgress(currentProblemIndex - 1);
                break;
            }
            case SEEK_BAR: {
                ViewPager viewPager = (ViewPager) findViewById(R.id.problemViewPager);
                viewPager.setCurrentItem(currentProblemIndex - 1, false);
                break;
            }
            case DOUBLE_TAP:
            case MOONBOARD:
            case VOLUME_BUTTONS: {
                SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
                seekBar.setProgress(currentProblemIndex - 1);

                ViewPager viewPager = (ViewPager) findViewById(R.id.problemViewPager);
                viewPager.setCurrentItem(currentProblemIndex - 1, false);
                break;
            }
            case REFRESHING: {
                ViewPager viewPager = (ViewPager) findViewById(R.id.problemViewPager);
                ((ProblemsImageViewAdapter)viewPager.getAdapter()).updateView(viewPager.getCurrentItem());
                viewPager.getAdapter().notifyDataSetChanged();
                break;
            }
        }

        showProblem();
    }

    private static String gradeToString(int grade)
    {
        switch(grade)
        {
            case 0:
                return "6A";
            case 1:
                return "6A+";
            case 2:
                return "6B";
            case 3:
                return "6B+";
            case 4:
                return "6C";
            case 5:
                return "6C+";
            case 6:
                return "7A";
            case 7:
                return "7A+";
            case 8:
                return "7B";
            case 9:
                return "7B+";
            case 10:
                return "7C";
            case 11:
                return "7C+";
            case 12:
                return "8A";
            case 13:
                return "8A+";
            case 14:
                return "8B";
        }

        return "?";
    }

    private void refreshProblem(boolean send)
    {
        // force a refresh
        //@TODO: find a better way to refresh the board if needed

        sendingFreezed = !send;
        gotoProblem(currentProblemIndex, INTERACTION_TYPE.REFRESHING);
        sendingFreezed = false;
    }

    private void showProblem()
    {
        if(problemsCursor.getCount() == 0) {

            TextView problemText = (TextView) findViewById(R.id.problemText);
            problemText.setText(R.string.noProblemsFound);

            TextView authorText = (TextView) findViewById(R.id.authorText);
            authorText.setText("");

            TextView indexText = (TextView) findViewById(R.id.indexText);
            indexText.setText("");

            return;
        }

        currentProblemId = problemsCursor.getInt(problemsCursor.getColumnIndex("id"));
        int grade = problemsCursor.getInt(problemsCursor.getColumnIndex("grade"));
        holds = problemsCursor.getBlob(problemsCursor.getColumnIndex("holds"));

        problemUserDbRecord = new ProblemUserDbRecord(userDbHelper, currentProblemId);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean realtimeUpdate = settings.getBoolean("realtimeUpdate", false);
        if(realtimeUpdate)
            sendProblemToMoonboard(false);

        String problemName = problemsCursor.getString(problemsCursor.getColumnIndex("name"));
        String author = problemsCursor.getString(problemsCursor.getColumnIndex("author"));

        TextView problemText = (TextView) findViewById(R.id.problemText);
        String gradeText = problemName + " (" + gradeToString(grade);
        int userGrade = problemUserDbRecord.getUserGrade();
        if(userGrade != -1 && userGrade != grade)
            gradeText += " / " + gradeToString(userGrade);
        gradeText += ")";
        problemText.setText(gradeText);

        TextView authorText = (TextView) findViewById(R.id.authorText);
        authorText.setText(author);

        TextView indexText = (TextView) findViewById(R.id.indexText);
        indexText.setText(currentProblemIndex + " / " + problemsCount);
    }

    @Override
    protected void onDestroy()
    {
        db.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_problems, menu);

        if(problemsCount == 0)
        {
            menu.findItem(R.id.action_userRecord).setEnabled(false);
            menu.findItem(R.id.action_sendToMoonboard).setEnabled(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivityForResult(intent, R.id.action_settings);

            return true;
        }
        else if(id == R.id.action_sendToMoonboard)
        {
            sendProblemToMoonboard(true);
            return true;
        }
        else if(id == R.id.action_userRecord)
        {
            final Dialog dialog = new Dialog(ProblemsActivity.this);
            dialog.setContentView(R.layout.dialog_user_record);
            dialog.setTitle("User record");

            boolean done = problemUserDbRecord.isDone();
            boolean wishlist = problemUserDbRecord.isInWishlist();

            //final String[] circuits = new String[] { "CIRCUI 1", "CIRCUIT 2", "NEW CIRCUIT..."};

            /*final ListView circuitsList = (ListView)dialog.findViewById(R.id.circuits);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_multiple_choice, circuits);
            circuitsList.setAdapter(adapter);
            circuitsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);*/

            String[] attemptsStr = new String[99];
            for(int i = 0; i < attemptsStr.length; ++i)
                attemptsStr[i] = Integer.toString(i+1);

            final Spinner attemptsList = (Spinner)dialog.findViewById(R.id.attemptsSpinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, attemptsStr);
            attemptsList.setAdapter(adapter);

            int attempts = Math.max(0, problemUserDbRecord.getAttempts() - 1);
            attemptsList.setSelection(attempts);
            attemptsList.setEnabled(done);

            final RadioGroup resultRadioGroup = (RadioGroup)dialog.findViewById(R.id.result_list);
            if(done)
                ((RadioButton)resultRadioGroup.getChildAt(1)).setChecked(true);
            else if(wishlist)
                ((RadioButton)resultRadioGroup.getChildAt(2)).setChecked(true);

            int currentProblemVote = problemUserDbRecord.getVote();
            if(currentProblemVote == 0)
                currentProblemVote = 3;

            final RadioGroup voteRadioGroup = (RadioGroup)dialog.findViewById(R.id.vote_list);
            ((RadioButton)voteRadioGroup.getChildAt(currentProblemVote)).setChecked(true);
            for (int i = 0; i < voteRadioGroup.getChildCount(); i++) {
                voteRadioGroup.getChildAt(i).setEnabled(done);
            }

            int userGrade = problemUserDbRecord.getUserGrade();
            if(userGrade == -1)
                userGrade = problemsCursor.getInt(problemsCursor.getColumnIndex("grade"));

            final Spinner userGradeSpinner = (Spinner)dialog.findViewById(R.id.user_grade);
            userGradeSpinner.setSelection(userGrade);
            userGradeSpinner.setEnabled(done);

            resultRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int id) {
                    boolean enabled = id == R.id.radio_done;
                    userGradeSpinner.setEnabled(enabled);
                    attemptsList.setEnabled(enabled);
                    for (int i = 0; i < voteRadioGroup.getChildCount(); i++) {
                        voteRadioGroup.getChildAt(i).setEnabled(enabled);
                    }
                }
            });

            Button okButton = (Button) dialog.findViewById(R.id.okButton);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean done = resultRadioGroup.getCheckedRadioButtonId() == R.id.radio_done;
                    boolean wishlist = resultRadioGroup.getCheckedRadioButtonId() == R.id.radio_wishlist;
                    int userGrade = userGradeSpinner.getSelectedItemPosition();
                    int vote = voteRadioGroup.indexOfChild(voteRadioGroup.findViewById(voteRadioGroup.getCheckedRadioButtonId()));
                    int attempts = attemptsList.getSelectedItemPosition()+1;

                    problemUserDbRecord.setIsDone(done);
                    problemUserDbRecord.setIsInWishlist(wishlist);
                    if (done) {
                        problemUserDbRecord.setUserGrade(userGrade);
                        problemUserDbRecord.setVote(vote);
                        problemUserDbRecord.setAttempts(attempts);
                    } else {
                        problemUserDbRecord.setUserGrade(-1);
                        problemUserDbRecord.setVote(0);
                        problemUserDbRecord.setAttempts(0);
                    }
                    problemUserDbRecord.commit();

                    refreshProblem(false);

                    dialog.dismiss();
                }
            });

            Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case R.id.action_settings:
            {
                refreshProblem(true);
                break;
            }
        }
    }
}
