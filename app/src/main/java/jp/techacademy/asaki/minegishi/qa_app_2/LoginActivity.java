package jp.techacademy.asaki.minegishi.qa_app_2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText mEmailEditText;
    EditText mPasswordEditText;
    EditText mNameEditText;
    ProgressDialog mProgress;

    FirebaseAuth mAuth; // FirebaseAuthにサインアップまたはサインインするクラス
    OnCompleteListener<AuthResult> mCreateAccountListener;  // アカウント作成処理の完了を受け取るリスナー
    OnCompleteListener<AuthResult> mLoginListener;  // ログイン処理の完了を受け取るリスナー
    DatabaseReference mDataBaseReference;  // データベースへの読み書きに必要

    // アカウント作成時にフラグを立て、ログイン処理後に名前をFirebaseに保存する
    boolean mIsCreateAccount = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mDataBaseReference = FirebaseDatabase.getInstance().getReference();

        // 最初に、getInstance（）を呼び出してFirebaseAuthのオブジェクトを取得する
        mAuth = FirebaseAuth.getInstance();

        // アカウント作成処理のリスナー
        // アカウント作成処理後に呼び出される
        mCreateAccountListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // 成功した場合
                    // ログインを行う
                    String email = mEmailEditText.getText().toString();  // EditTextに記載されているメアドを取得
                    String password = mPasswordEditText.getText().toString();  // EditTextに記載されているパスワードを取得
                    login(email, password);
                } else {

                    // 失敗した場合
                    // エラーを表示する
                    View view = findViewById(android.R.id.content);
                    Snackbar.make(view, "アカウント作成に失敗しました", Snackbar.LENGTH_LONG).show();

                    // プログレスダイアログを非表示にする
                    mProgress.dismiss();
                }
            }
        };

        // ログイン処理のリスナー
        // ログイン後に呼び出される
        mLoginListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    // 成功した場合
                    FirebaseUser user = mAuth.getCurrentUser();
                    // FirebaseUser:Firebaseプロジェクトのユーザデータベースにあるユーザのプロファイル情報を表す

                    DatabaseReference userRef = mDataBaseReference.child(Const.UsersPATH).child(user.getUid());
                    //データベースにデータを書き込むには、DatabaseReference のインスタンスが必要
                    // データベースのUsersPATH="users"の下のuserUidの下をuserRefとする


                    if (mIsCreateAccount) {
                        // アカウント作成がされた後のログインだったなら

                        // アカウント作成の時は表示名をFirebaseに保存する
                        String name = mNameEditText.getText().toString();

                        Map<String, String> data = new HashMap<String, String>();
                        data.put("name", name);
                        userRef.setValue(data);

                        // 表示名をPrefarenceにも保存する
                        saveName(name);

                    } else {
                        // アカウント作成された後のログインでなかったら
                        // Firebaseからデータを一度だけ取得する場合はDatabaseReferenceクラスが実装しているQueryクラスのaddListenerForSingleValueEventメソッドを使う
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                // データベースのuserRefの"name"キーに対応して入っている表示名を持ってきて、Preferenceに保存
                                Map data = (Map) snapshot.getValue();
                                saveName((String)data.get("name"));
                            }
                            @Override
                            public void onCancelled(DatabaseError firebaseError) {
                            }
                        });
                    }

                    // プログレスダイアログを非表示にする
                    mProgress.dismiss();

                    // ログイン状態を知らせる
                    MainActivity.UserLoaded = true;
                    // Activityを閉じる
                    finish();

                } else {
                    // 失敗した場合
                    // エラーを表示する
                    View view = findViewById(android.R.id.content);
                    Snackbar.make(view, "ログインに失敗しました", Snackbar.LENGTH_LONG).show();

                    // プログレスダイアログを非表示にする
                    mProgress.dismiss();
                }
            }
        };

        // UIの準備
        setTitle("ログイン");

        mEmailEditText = (EditText) findViewById(R.id.emailText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordText);
        mNameEditText = (EditText) findViewById(R.id.nameText);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("処理中...");

        Button createButton = (Button) findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
                String name = mNameEditText.getText().toString();

                if (email.length() != 0 && password.length() >= 6 && name.length() != 0) {
                    // ログイン時に表示名を保存するようにフラグを立てる
                    mIsCreateAccount = true;  // アカウントを作成した時にフラグをtrueにする

                    createAccount(email, password);
                } else {
                    // エラーを表示する
                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                if (email.length() != 0 && password.length() >= 6) {
                    // フラグを落としておく
                    mIsCreateAccount = false;

                    login(email, password);
                } else {
                    // エラーを表示する
                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void createAccount(String email, String password) {
        // プログレスダイアログを表示する
        mProgress.show();

        // アカウントを作成する
        // 新しいユーザーのメールアドレスとパスワードを createUserWithEmailAndPassword に渡して、新しいアカウントを作成
        // アカウント作成処理完了のリスナーにmCreateAccountListenerを設定
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(mCreateAccountListener);
    }

    private void login(String email, String password) {
        // プログレスダイアログを表示する
        mProgress.show();

        // ログインする
        // ユーザーのメールアドレスとパスワードを signInWithEmailAndPassword に渡して、ログイン
        // ログイン処理完了のリスナーにmLoginListenerを設定
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mLoginListener);
    }

    private void saveName(String name) {
        // Preference:アプリの設定データをデバイス内に保存するための仕組み に保存する

        //　インスタンス取得
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        // SharedPreferences.Editorオブジェクトを取得
        // このオブジェクトでは、メソッドを使ってデータの保存をすることができる
        SharedPreferences.Editor editor = sp.edit();

        // Preferenceに"name"キーでEditTextから取得した表示名を保存
        editor.putString(Const.NameKEY, name);
        editor.commit();
    }
}
