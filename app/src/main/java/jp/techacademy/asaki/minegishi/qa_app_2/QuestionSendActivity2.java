package jp.techacademy.asaki.minegishi.qa_app_2;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.lang.RandomStringUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class QuestionSendActivity2 extends AppCompatActivity implements View.OnClickListener, DatabaseReference.CompletionListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int PERMISSIONS_REQUEST_CODE2 = 101;
    private static final int CHOOSER_REQUEST_CODE = 100;
    private static final int INTENT_REQUEST_CODE = 101;

    private ProgressDialog mProgress;
    private EditText mTitleText;
    private EditText mBodyText;
    private EditText mLink;///////
    private EditText mURLText;////////
    private EditText mPDFText;//////
    private EditText mVideoText;///---///
    private ImageView mImageView;
    private Button mSendButton;
    private Button mPDFButton;//----

    private int mGenre;
    private int mFileCheck = 0;
    private String mFile = "";
    private String fileName;
    private Uri mPictureUri;
    //////
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference riversRef;//////
    /////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_send2);

        // 渡ってきたジャンルの番号を保持する

        // Bundle: オブジェクトの入れ物
        //Intent のインスタンスは内部的に Bundle を持っており、その内部の Bundle に対し値を設定しているだけ
        // Intent#getExtras()メソッドでBundleを受け取り、そのBundle#getxxメソッドでデータを受け取る
        Bundle extras = getIntent().getExtras();
        mGenre = extras.getInt("genre");

        // UIの準備
        setTitle("動画紹介作成");

        mTitleText = (EditText) findViewById(R.id.titleText);
        mBodyText = (EditText) findViewById(R.id.bodyText);
        mLink = (EditText) findViewById(R.id.linkEditText);/////
        mURLText = (EditText) findViewById(R.id.urlEditText);//////
        mPDFText = (EditText) findViewById(R.id.PDFEditText);//----
        mVideoText = (EditText) findViewById(R.id.videoText);////----////

        mSendButton = (Button) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(this);

        mPDFButton = (Button) findViewById(R.id.PDFButton);//----
        mPDFButton.setOnClickListener(this);//----


        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageView.setOnClickListener(this);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("投稿中...");

        /////
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://qaapp4-4bf13.appspot.com");
        /////

    }

    @Override
    // Intent連携から戻ってきた時に画像を取得し、ImageViewに設定する
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSER_REQUEST_CODE) {

            if (resultCode != RESULT_OK) {  // 結果がOKでなかったらデータ削除
                if (mPictureUri != null) {
                    getContentResolver().delete(mPictureUri, null, null);
                    mPictureUri = null;
                }
                return;
            }

            // 画像を取得
            // dataがnullかdata.getData()がnullの場合はカメラで撮影したときなので画像の取得にmPictureUriを使う
            // data.getData()で取得出来た場合はそのURIを使う
            Uri uri = (data == null || data.getData() == null) ? mPictureUri : data.getData();

            // URIからBitmapを取得する
            Bitmap image;
            // tryブロックの中で例外が発生すると残りの処理は行われずにcatchブロックへ処理が移る
            try {
                ContentResolver contentResolver = getContentResolver();
                InputStream inputStream = contentResolver.openInputStream(uri);
                image = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } catch (Exception e) {
                return;
            }

            // 取得したBimapの長辺を500ピクセルにリサイズする
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            float scale = Math.min((float) 500 / imageWidth, (float) 500 / imageHeight); // (1)

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);

            Bitmap resizedImage =  Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, matrix, true);

            // BitmapをImageViewに設定する
            mImageView.setImageBitmap(resizedImage);

            mPictureUri = null;

        }else if (requestCode == INTENT_REQUEST_CODE){//---

            if (resultCode == RESULT_OK){
                Uri uri = null;
                if (data != null) {
                    uri = data.getData();
                }

                mPDFText.setEnabled(false);
                mPDFText.setFocusable(false);


               fileName = RandomStringUtils.randomAlphanumeric(20);

                /////
                riversRef = storageRef.child(fileName);/////
                UploadTask uploadTask = riversRef.putFile(uri);
                mFileCheck = 1;

                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    }
                });
                /////

            }
        }//----
    }

    @Override
    public void onClick(View v) {
        if (v == mImageView) {

            // Android 6.0以降の場合
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // パーミッションの許可状態を確認する
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // 許可されている
                    showChooser();
                } else {
                    // 許可されていないので許可ダイアログを表示する
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);

                    return;
                }
                // Android 5系以下の場合
            } else {
                showChooser();
            }
        } else if (v == mSendButton) {
            // キーボードが出てたら閉じる
            InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference genreRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));

            Map<String, String> data = new HashMap<String, String>();

            // UID　投稿者のUidを取得
            data.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

            // タイトルと本文を取得する
            String title = mTitleText.getText().toString();
            String body = mBodyText.getText().toString();
            String link = mLink.getText().toString();
            String url = mURLText.getText().toString();
            String video = mVideoText.getText().toString();

            if (title.length() == 0) {
                // 質問が入力されていない時はエラーを表示するだけ
                Snackbar.make(v, "タイトルを入力して下さい", Snackbar.LENGTH_LONG).show();
                return;
            }

            if (body.length() == 0) {
                // 質問が入力されていない時はエラーを表示するだけ
                Snackbar.make(v, "説明を入力して下さい", Snackbar.LENGTH_LONG).show();
                return;
            }

            if (mFile.length() == 0){
                mFile = "";
            }
            // ファイル説明は記載したものの、ファイルをアップロードしなかった場合はファイル名削除
            if (mFileCheck == 0){
                mFile = "";
            }

            if (video.length() == 0) {
                Snackbar.make(v, "動画IDを入力して下さい", Snackbar.LENGTH_LONG).show();
                return;
            }


            // Preferenceから名前を取る
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            String name = sp.getString(Const.NameKEY, "");

            data.put("title", title);
            data.put("body", body);
            data.put("name", name);
            data.put("link", link);
            data.put("url", url);//////
            data.put("fileName", fileName);/////
            data.put("file", mFile);
            data.put("video", video);

            // 添付画像を取得する
            BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();

            // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
            if (drawable != null) {
                // Bitmapで画像取得
                Bitmap bitmap = drawable.getBitmap();

                // バイト配列出力ストリーム(→データをバイト単位で読み書きできるもの)作成
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                // compress:ビットマップの圧縮したバージョンを指定された出力ストリームに書き込む
                // compress( Bitmap.CompressFormat format, int quality（品質）, OutputStream stream)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);

                // 指定されたデータをBase64（データを文字列に変換する仕組み）でエンコードし、その結果で新たに割り当てられたStringを返す
                String bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                data.put("image", bitmapString);
            }

            // dataをFireBaseに保存
            // 保存が完了したタイミングで何か処理を差し込みたい場合、第2引数にはCompletionListenerクラスを指定
            // 今回は、ActivityがCompletionListenerクラスを実装している＝this
            // CompletionListenerクラスのonCompleteメソッドが実行される
            // push()を使うと一意のキーが発行されて、その下に値が入る
            genreRef.push().setValue(data, this);
            mProgress.show();

        }else if (v==mPDFButton){//----

            mFile = mPDFText.getText().toString();
            if(mFile.length() == 0){
                Snackbar.make(v, "ファイルの説明を入力して下さい", Snackbar.LENGTH_LONG).show();
                return;
            }else {
                // Android 6.0以降の場合
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // パーミッションの許可状態を確認する
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        // 許可されている
                        fileUpload();
                    } else {
                        // 許可されていないので許可ダイアログを表示する
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE2);

                        return;
                    }
                    // Android 5系以下の場合
                } else {
                    fileUpload();
                }
            }
        }//----
    }

    @Override
    // 許可を求めるダイアログからの結果を受け取る
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ユーザーが許可したとき
                    showChooser();
                }
                return;
            }
            case PERMISSIONS_REQUEST_CODE2: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ユーザーが許可したとき
                    fileUpload();
                }
                return;
            }
        }
    }

    private void showChooser() {
        // ギャラリーから選択するIntent
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);

        // カメラで撮影するIntent
        String filename = System.currentTimeMillis() + ".jpg";
        // ContentValuesに投入する画像データを用意
        ContentValues values = new ContentValues();
        // 画像のタイトルに上で取得した撮影時間名を入れる
        values.put(MediaStore.Images.Media.TITLE, filename);
        // 画像のタイプにjpegを入れる
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");//ここを音楽ファイル、PDF
        // insert():行を追加。戻り値は新たに追加された行のURI.

        mPictureUri = getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri);

        // ギャラリー選択のIntentを与えてcreateChooserメソッドを呼ぶ
        Intent chooserIntent = Intent.createChooser(galleryIntent, "画像を取得");
        // createChooserメソッドの第1引数に1つ目のIntentを指定し、第2引数にダイアログに表示するタイトルを指定

        // EXTRA_INITIAL_INTENTS にカメラ撮影のIntentを追加
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});
        // 上のIntentに対し、chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});と
        // 2つ目のIntentを指定することで2つのIntentを選択するダイアログが表示される


        //  startActivityForResult:開いたアクティビティから何かしらの情報を受け取る
        // chooserIntentからどちらを選んだかを受け取りコードをつける
        startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE);
    }

    private void fileUpload(){
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("*/*");
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(galleryIntent, INTENT_REQUEST_CODE);
    }
    @Override
    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
        mProgress.dismiss();

        if (databaseError == null) {
            finish();
        } else {
            Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show();
        }
    }
}
