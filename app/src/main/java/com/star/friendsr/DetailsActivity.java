package com.star.friendsr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class DetailsActivity extends AppCompatActivity {

    private RatingBar mRatingBar;
    private ImageView mDetailsImageView;
    private TextView mDetailsTextView;

    private MediaPlayer mMediaPlayer;

    private Button mTakePhotoButton;
    private Button mChooseFromAlbumButton;

    private String mName;

    private Uri mImageUri;

    public static final int TAKE_PHOTO = 0;
    public static final int CROP_PHOTO = 1;
    public static final int PICK_PHOTO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        setContentView(R.layout.activity_details);

        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);
        mDetailsImageView = (ImageView) findViewById(R.id.detailsImage);
        mDetailsTextView = (TextView) findViewById(R.id.details);

        Intent intent = getIntent();

        mName = intent.getStringExtra(MainActivity.NAME);

        String[] friendNames = getResources().getStringArray(R.array.friend_names);
        String[] friendDetails = getResources().getStringArray(R.array.friend_details);

        for (int i = 0; i < friendNames.length; i++) {
            if (friendNames[i].equals(mName)) {
                mDetailsImageView.setImageResource(getResources().getIdentifier(
                        mName.toLowerCase(), "mipmap", getPackageName()));
                mDetailsTextView.setText(friendDetails[i]);

                SharedPreferences sharedPreferences = getSharedPreferences(
                        MainActivity.RATING_PREF, MODE_PRIVATE);

                mRatingBar.setRating(sharedPreferences.getFloat(friendNames[i], 0));

                String image = sharedPreferences.getString(friendNames[i] + " image", "");

                if (!"".equals(image)) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(
                                getContentResolver().openInputStream(Uri.parse(image))
                        );
                        mDetailsImageView.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }

                break;
            }
        }

        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                Intent intent = new Intent();
                intent.putExtra(MainActivity.NAME, mName);
                intent.putExtra(MainActivity.STAR, rating);
                setResult(RESULT_OK, intent);

                SharedPreferences.Editor editor = getSharedPreferences(MainActivity.RATING_PREF,
                        MODE_PRIVATE).edit();

                editor.putFloat(mName, rating).commit();

                finish();
            }
        });

        mMediaPlayer = MediaPlayer.create(this, R.raw.friends_theme);

        mTakePhotoButton = (Button) findViewById(R.id.take_photo);
        mChooseFromAlbumButton = (Button) findViewById(R.id.choose_from_album);

        mTakePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File outputImage = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), mName + ".jpg");

                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }

                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mImageUri = Uri.fromFile(outputImage);

                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                i.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                startActivityForResult(i, TAKE_PHOTO);

            }
        });

        mChooseFromAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File outputImage = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), mName + ".jpg");

                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }

                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mImageUri = Uri.fromFile(outputImage);

                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, PICK_PHOTO);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mMediaPlayer != null) {
            mMediaPlayer.setLooping(true);

            SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.RATING_PREF,
                    MODE_PRIVATE);

            mMediaPlayer.seekTo(sharedPreferences.getInt(MainActivity.POS, 0));

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

        SharedPreferences.Editor editor = getSharedPreferences(MainActivity.RATING_PREF,
                MODE_PRIVATE).edit();

        editor.putInt(MainActivity.POS, pos).commit();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(mImageUri, "image/*");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);

                    startActivityForResult(intent, CROP_PHOTO);
                }
                break;
            case CROP_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(
                                getContentResolver().openInputStream(mImageUri)
                        );
                        mDetailsImageView.setImageBitmap(bitmap);

                        SharedPreferences.Editor editor = getSharedPreferences(MainActivity.RATING_PREF,
                                MODE_PRIVATE).edit();

                        editor.putString(mName + " image", mImageUri.toString()).commit();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PICK_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri fromImageUri = null;
                    if (data != null) {
                        fromImageUri = data.getData();
                    }

                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(fromImageUri, "image/*");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);

                    startActivityForResult(intent, CROP_PHOTO);
                }
                break;
            default:
                break;
        }
    }
}
