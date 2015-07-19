package com.star.friendsr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.FileNotFoundException;


public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 0;
    public static final String NAME = "NAME";
    public static final String STAR = "STAR";

    public static final String RATING_PREF = "rating";

    public static final String POS = "pos";

    private ImageView[] mImageViews;
    private TextView[] mTextViews;
    private RatingBar[] mRatingBars;

    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

        mImageViews = new ImageView[6];
        mTextViews = new TextView[6];
        mRatingBars = new RatingBar[6];

        mImageViews[0] = (ImageView) findViewById(R.id.chandlerImage);
        mImageViews[1] = (ImageView) findViewById(R.id.joeyImage);
        mImageViews[2] = (ImageView) findViewById(R.id.monicaImage);
        mImageViews[3] = (ImageView) findViewById(R.id.phoebeImage);
        mImageViews[4] = (ImageView) findViewById(R.id.rachelImage);
        mImageViews[5] = (ImageView) findViewById(R.id.rossImage);

        mTextViews[0] = (TextView) findViewById(R.id.chandler);
        mTextViews[1] = (TextView) findViewById(R.id.joey);
        mTextViews[2] = (TextView) findViewById(R.id.monica);
        mTextViews[3] = (TextView) findViewById(R.id.phoebe);
        mTextViews[4] = (TextView) findViewById(R.id.rachel);
        mTextViews[5] = (TextView) findViewById(R.id.ross);

        mRatingBars[0] = (RatingBar) findViewById(R.id.chandlerRatingBar);
        mRatingBars[1] = (RatingBar) findViewById(R.id.joeyRatingBar);
        mRatingBars[2] = (RatingBar) findViewById(R.id.monicaRatingBar);
        mRatingBars[3] = (RatingBar) findViewById(R.id.phoebeRatingBar);
        mRatingBars[4] = (RatingBar) findViewById(R.id.rachelRatingBar);
        mRatingBars[5] = (RatingBar) findViewById(R.id.rossRatingBar);

        final String[] friendNames = getResources().getStringArray(R.array.friend_names);

        SharedPreferences sharedPreferences = getSharedPreferences(RATING_PREF, MODE_PRIVATE);

        for (int i = 0; i < mRatingBars.length; i++) {
            Float rating = sharedPreferences.getFloat(friendNames[i], 0);
            mRatingBars[i].setRating(rating);
        }

        for (int i = 0; i < mImageViews.length; i++) {
            final int finalI = i;
            mImageViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                    intent.putExtra(NAME, friendNames[finalI]);
                    startActivityForResult(intent, REQUEST_CODE);
                }
            });

            String image = sharedPreferences.getString(friendNames[i] + " image", "");

            if (!"".equals(image)) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(
                            getContentResolver().openInputStream(Uri.parse(image))
                    );
                    mImageViews[i].setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }

        mMediaPlayer = MediaPlayer.create(this, R.raw.friends_theme);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        String[] friendNames = getResources().getStringArray(R.array.friend_names);

        switch (requestCode) {
            case REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    String name = data.getStringExtra(NAME);
                    for (int i = 0; i < friendNames.length; i++) {
                        if (friendNames[i].equals(name)) {
                            mRatingBars[i].setRating(data.getFloatExtra(STAR, 0));
                        }
                    }
                }
        }

        SharedPreferences sharedPreferences = getSharedPreferences(RATING_PREF, MODE_PRIVATE);

        for (int i = 0; i < mImageViews.length; i++) {

            String image = sharedPreferences.getString(friendNames[i] + " image", "");

            if (!"".equals(image)) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(
                            getContentResolver().openInputStream(Uri.parse(image))
                    );
                    mImageViews[i].setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mMediaPlayer != null) {
            mMediaPlayer.setLooping(true);

            SharedPreferences sharedPreferences = getSharedPreferences(RATING_PREF, MODE_PRIVATE);

            mMediaPlayer.seekTo(sharedPreferences.getInt(POS, 0));

            mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    mp.start();
                }
            });
//            mMediaPlayer.start();

        }

    }

    @Override
    protected void onPause() {
        mMediaPlayer.pause();
        int pos = mMediaPlayer.getCurrentPosition();

        SharedPreferences.Editor editor = getSharedPreferences(RATING_PREF, MODE_PRIVATE)
                .edit();

        editor.putInt(POS, pos).commit();

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }

        super.onDestroy();
    }
}
