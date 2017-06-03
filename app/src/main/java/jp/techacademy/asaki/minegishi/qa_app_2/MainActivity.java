package jp.techacademy.asaki.minegishi.qa_app_2;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    static boolean UserLoaded;

    private Toolbar mToolbar;
    private int mGenre = 0;
    private int count;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mGenreRef;
    private DatabaseReference mFavoriteGenreRef;
    private DatabaseReference mFavoriteRef;
    private DatabaseReference mAllRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    private ArrayList<Favorite> mFavoriteArrayList;

    private ArrayList<Question> mAllArrayList;

    private QuestionsListAdapter mAdapter;
    private NavigationView navigationView;

    // お気に入りデータに変更があった時に受け取るリスナー
    private ChildEventListener mFavoriteEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();
            String questionUid = (String) map.get("questionUid");

            Favorite favorite = new Favorite(questionUid, dataSnapshot.getKey());
            mFavoriteArrayList.add(favorite);

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

            for (Favorite favorite: mFavoriteArrayList) {
                if (dataSnapshot.getKey().equals(favorite.getFavoriteUid())){
                    mFavoriteArrayList.remove(favorite);
                    return;
                }
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    // 質問データに変更があった時に受け取るリスナー
    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        // 要素（質問）が追加されたとき呼ばれるメソッド
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            // 追加されたデータをmapに取得
            HashMap map = (HashMap) dataSnapshot.getValue();
            // そのmapから各情報を取得
            String title = (String) map.get("title");
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");
            String link = (String) map.get("link");
            String url = (String) map.get("url");
            String file = (String) map.get("file");
            String fileName = (String) map.get("fileName");
            String video = (String) map.get("video");
            String imageString = (String) map.get("image");
            byte[] bytes;
            if (imageString != null) {
                bytes = Base64.decode(imageString, Base64.DEFAULT);
            } else {
                bytes = new byte[0];
            }

            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
            HashMap answerMap = (HashMap) map.get("answers");
            if (answerMap != null) {

                for (Object key : answerMap.keySet()) {
                    HashMap temp = (HashMap) answerMap.get((String) key);
                    String answerBody = (String) temp.get("body");
                    String answerName = (String) temp.get("name");
                    String answerUid = (String) temp.get("uid");
                    Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                    answerArrayList.add(answer);
                }
            }

            // QuestionsListAdapterにデータを設定
            Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), video, link, url, fileName, file, mGenre, bytes, answerArrayList);
            mQuestionArrayList.add(question);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        // 要素（質問）に変化があった時受けるリスナー
        // ここでは質問に対して回答が投稿された時に呼ばれる
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            HashMap map = (HashMap) dataSnapshot.getValue();
            // 変更があったQuestionを探す
            for (Question question: mQuestionArrayList) {

                // 投稿された質問のUIDがFirebaseから取得した質問のUIDと一緒なら
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {

                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.getAnswers().clear();
                    // もう一度回答を設定
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            for (Question question: mQuestionArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())){
                    mQuestionArrayList.remove(question);
                    mAdapter.notifyDataSetChanged();
                    return;
                }
            }
    }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };



   private ChildEventListener mAllEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    // 追加されたデータをmapに取得
                    HashMap map = (HashMap) dataSnapshot.getValue();
                    // そのmapから各情報を取得
                    String title = (String) map.get("title");
                    String body = (String) map.get("body");
                    String name = (String) map.get("name");
                    String uid = (String) map.get("uid");
                    String link = (String) map.get("link");
                    String url = (String) map.get("url");
                    String fileName = (String) map.get("fileName");
                    String file = (String) map.get("file");
                    String video = (String) map.get("video");
                    String imageString = (String) map.get("image");
                    byte[] bytes;
                    if (imageString != null) {
                        bytes = Base64.decode(imageString, Base64.DEFAULT);
                    } else {
                        bytes = new byte[0];
                    }

                    ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        //
                        for (Object key : answerMap.keySet()) {
                            // Firebaseから取得した回答のUIDの回答をtempに取得
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            answerArrayList.add(answer);
                        }
                    }

                    // QuestionsListAdapterにデータを設定
                    Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), video, link, url, fileName, file, 1, bytes, answerArrayList);
                    mAllArrayList.add(question);
                    mAdapter.notifyDataSetChanged();


        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    HashMap map = (HashMap) dataSnapshot.getValue();
                    // 変更があったQuestionを探す
                    for (Question question : mAllArrayList) {

                        // 投稿された質問のUIDがFirebaseから取得した質問のUIDと一緒なら
                        if (dataSnapshot.getKey().equals(question.getQuestionUid())) {

                            // このアプリで変更がある可能性があるのは回答(Answer)のみ
                            question.getAnswers().clear();
                            // もう一度回答を設定
                            HashMap answerMap = (HashMap) map.get("answers");
                            if (answerMap != null) {
                                for (Object key : answerMap.keySet()) {
                                    HashMap temp = (HashMap) answerMap.get((String) key);
                                    String answerBody = (String) temp.get("body");
                                    String answerName = (String) temp.get("name");
                                    String answerUid = (String) temp.get("uid");
                                    Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                                    question.getAnswers().add(answer);
                                }
                            }

                            mAdapter.notifyDataSetChanged();
                        }
                    }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            for (Question question: mAllArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())){
                    mAllArrayList.remove(question);
                    mAdapter.notifyDataSetChanged();
                    return;
                }
            }

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
   };


    private ChildEventListener mAllEventListener2 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            // 追加されたデータをmapに取得
            HashMap map = (HashMap) dataSnapshot.getValue();
            // そのmapから各情報を取得
            String title = (String) map.get("title");
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");
            String link = (String) map.get("link");
            String url = (String) map.get("url");
            String fileName = (String) map.get("fileName");
            String file = (String) map.get("file");
            String video = (String) map.get("video");
            String imageString = (String) map.get("image");
            byte[] bytes;
            if (imageString != null) {
                bytes = Base64.decode(imageString, Base64.DEFAULT);
            } else {
                bytes = new byte[0];
            }

            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
            HashMap answerMap = (HashMap) map.get("answers");
            if (answerMap != null) {
                //
                for (Object key : answerMap.keySet()) {
                    // Firebaseから取得した回答のUIDの回答をtempに取得
                    HashMap temp = (HashMap) answerMap.get((String) key);
                    String answerBody = (String) temp.get("body");
                    String answerName = (String) temp.get("name");
                    String answerUid = (String) temp.get("uid");
                    Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                    answerArrayList.add(answer);
                }
            }

            // QuestionsListAdapterにデータを設定
            Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), video, link, url, fileName, file, 2, bytes, answerArrayList);
            mAllArrayList.add(question);
            mAdapter.notifyDataSetChanged();


        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            HashMap map = (HashMap) dataSnapshot.getValue();
            // 変更があったQuestionを探す
            for (Question question : mAllArrayList) {

                // 投稿された質問のUIDがFirebaseから取得した質問のUIDと一緒なら
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {

                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.getAnswers().clear();
                    // もう一度回答を設定
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            for (Question question: mAllArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())){
                    mAllArrayList.remove(question);
                    mAdapter.notifyDataSetChanged();
                    return;
                }
            }

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    private ChildEventListener mAllEventListener3 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            // 追加されたデータをmapに取得
            HashMap map = (HashMap) dataSnapshot.getValue();
            // そのmapから各情報を取得
            String title = (String) map.get("title");
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");
            String link = (String) map.get("link");
            String url = (String) map.get("url");
            String fileName = (String) map.get("fileName");
            String file = (String) map.get("file");
            String video = (String) map.get("video");
            String imageString = (String) map.get("image");
            byte[] bytes;
            if (imageString != null) {
                bytes = Base64.decode(imageString, Base64.DEFAULT);
            } else {
                bytes = new byte[0];
            }

            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
            HashMap answerMap = (HashMap) map.get("answers");
            if (answerMap != null) {
                //
                for (Object key : answerMap.keySet()) {
                    // Firebaseから取得した回答のUIDの回答をtempに取得
                    HashMap temp = (HashMap) answerMap.get((String) key);
                    String answerBody = (String) temp.get("body");
                    String answerName = (String) temp.get("name");
                    String answerUid = (String) temp.get("uid");
                    Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                    answerArrayList.add(answer);
                }
            }

            // QuestionsListAdapterにデータを設定
            Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), video, link, url, fileName, file, 3, bytes, answerArrayList);
            mAllArrayList.add(question);
            mAdapter.notifyDataSetChanged();


        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            HashMap map = (HashMap) dataSnapshot.getValue();
            // 変更があったQuestionを探す
            for (Question question : mAllArrayList) {

                // 投稿された質問のUIDがFirebaseから取得した質問のUIDと一緒なら
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {

                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.getAnswers().clear();
                    // もう一度回答を設定
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

            for (Question question: mAllArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())){
                    mAllArrayList.remove(question);
                    mAdapter.notifyDataSetChanged();
                    return;
                }
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener mAllEventListener4 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            // 追加されたデータをmapに取得
            HashMap map = (HashMap) dataSnapshot.getValue();
            // そのmapから各情報を取得
            String title = (String) map.get("title");
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");
            String link = (String) map.get("link");
            String url = (String) map.get("url");
            String fileName = (String) map.get("fileName");
            String file = (String) map.get("file");
            String video = (String) map.get("video");
            String imageString = (String) map.get("image");
            byte[] bytes;
            if (imageString != null) {
                bytes = Base64.decode(imageString, Base64.DEFAULT);
            } else {
                bytes = new byte[0];
            }

            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
            HashMap answerMap = (HashMap) map.get("answers");
            if (answerMap != null) {
                //
                for (Object key : answerMap.keySet()) {
                    // Firebaseから取得した回答のUIDの回答をtempに取得
                    HashMap temp = (HashMap) answerMap.get((String) key);
                    String answerBody = (String) temp.get("body");
                    String answerName = (String) temp.get("name");
                    String answerUid = (String) temp.get("uid");
                    Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                    answerArrayList.add(answer);
                }
            }

            // QuestionsListAdapterにデータを設定
            Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), video, link, url, fileName, file, 4, bytes, answerArrayList);
            mAllArrayList.add(question);
            mAdapter.notifyDataSetChanged();


        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            HashMap map = (HashMap) dataSnapshot.getValue();
            // 変更があったQuestionを探す
            for (Question question : mAllArrayList) {

                // 投稿された質問のUIDがFirebaseから取得した質問のUIDと一緒なら
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {

                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.getAnswers().clear();
                    // もう一度回答を設定
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

            for (Question question: mAllArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())){
                    mAllArrayList.remove(question);
                    mAdapter.notifyDataSetChanged();
                    return;
                }
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
                if (mGenre == 0 || mGenre == 5) {
                    Snackbar.make(view, "ジャンルを選択して下さい", Snackbar.LENGTH_LONG).show();
                    return;
                }

                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // ジャンルを渡して質問作成画面を起動する
                    if (mGenre == 1 || mGenre == 2) {///---///
                        Intent intent = new Intent(getApplicationContext(), QuestionSendActivity.class);
                        intent.putExtra("genre", mGenre);
                        startActivity(intent);
                    }else if (mGenre == 3 || mGenre == 4){///---///
                        Intent intent = new Intent(getApplicationContext(), QuestionSendActivity2.class);
                        intent.putExtra("genre", mGenre);
                        startActivity(intent);
                    }///---///
                }
            }
        });

        // ナビゲーションドロワーの設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // ActionBarDrawerToggleクラスのコンストラクタ
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name);
        // アクションバーをDrawerLayoutに登録
        drawer.addDrawerListener(toggle);
        // アクティビティの状態とActionBarDrawerToggleの状態を同期
        toggle.syncState();

        // NavigationView：アプリケーションの標準ナビゲーションメニューを表示
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        // ログイン状態取得
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null){
            // ログインされていなかったらお気に入りメニュー非表示
            Menu menu = navigationView.getMenu();
            MenuItem favoritemenuItem = menu.findItem(R.id.nav_favorite);
            favoritemenuItem.setVisible(false);
            favoritemenuItem.setEnabled(false);
            UserLoaded = false;
        }else {
            // ログインされていたらお気に入りメニュー表示
            Menu menu = navigationView.getMenu();
            MenuItem favoritemenuItem = menu.findItem(R.id.nav_favorite);
            favoritemenuItem.setVisible(true);
            favoritemenuItem.setEnabled(true);
            UserLoaded = true;
        }


        // setNavigationItemSelectedListener:メニュー項目が選択されたときに通知されるリスナー
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {

                ImageView imageView = (ImageView) findViewById(R.id.pictureView);
                imageView.setVisibility(View.GONE);

                int id = item.getItemId();  // 押された時のメニューのアイテムのidを取得

                if (id == R.id.nav_hobby) {
                    mToolbar.setTitle("手作り小物");
                    mGenre = 1;
                } else if (id == R.id.nav_life) {
                    mToolbar.setTitle("アレンジ小物");
                    mGenre = 2;
                } else if (id == R.id.nav_health) {
                    mToolbar.setTitle("手作りムービー");
                    mGenre = 3;
                } else if (id == R.id.nav_compter) {
                    mToolbar.setTitle("余興");
                    mGenre = 4;
                } else if (id == R.id.nav_favorite) {
                    mToolbar.setTitle("お気に入り");
                    mGenre = 5;
                }

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);


                // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
                mQuestionArrayList.clear();
                // アダプターにmQuestionArrayList（データ）をセットする
                mAdapter.setQuestionArrayList(mQuestionArrayList);
                // 質問のmListView用のアダプタに渡す
                mListView.setAdapter(mAdapter);

                // 選択したジャンルにリスナーを登録する
                if (mGenreRef != null) {
                    // イベントリスナーを取り除く
                    mGenreRef.removeEventListener(mEventListener);
                }

                if (mGenre != 5) {//
                    mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
                    mGenreRef.addChildEventListener(mEventListener);

                }else if (mGenre == 5){

                    for (Favorite favorite : mFavoriteArrayList) {
                        for (Question question : mAllArrayList) {
                            if (favorite.getQuestionUid().equals(question.getQuestionUid())) {
                                mQuestionArrayList.add(question);
                                break;
                            }
                        }
                    }

                    // 投稿が消された時、他人のフェイバレットを消したかったが消せなかった
                   /* int count = 1;
                    int size = mAllArrayList.size();

                    int count_f = 1;
                    int size_f = mFavoriteArrayList.size();


                    if(size > size_f) {
                    for (Favorite favorite : mFavoriteArrayList) {
                            for (Question question : mAllArrayList) {
                                if (favorite.getQuestionUid().equals(question.getQuestionUid())) {
                                    mQuestionArrayList.add(question);
                                    count = 1;
                                    break;
                                } else if (count == size) {
                                    mFavoriteArrayList.remove(favorite);
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    DatabaseReference FavoriteRef = mDatabaseReference.child(Const.FavoritePATH).child(user.getUid()).child(favorite.getFavoriteUid());
                                    FavoriteRef.removeValue();
                                    count = 1;
                                    break;
                                } else {
                                    count++;
                                }
                            }
                        }
                    }else if (size <= size_f){
                        for (Favorite favorite : mFavoriteArrayList) {
                            for (Question question : mAllArrayList) {
                                if (favorite.getQuestionUid().equals(question.getQuestionUid())) {
                                    mQuestionArrayList.add(question);
                                    break;
                                }
                            }*/
                        /*else if (count == size) {
                                    mFavoriteArrayList.remove(favorite);
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    DatabaseReference FavoriteRef = mDatabaseReference.child(Const.FavoritePATH).child(user.getUid()).child(favorite.getFavoriteUid());
                                    FavoriteRef.removeValue();
                                    count = 1;
                                    break;
                                } else {
                                    count++;
                                }*/



                    mAdapter.notifyDataSetChanged();
                }

                return true;
            }
        });

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();


        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mFavoriteArrayList = new ArrayList<Favorite>();
        mAllArrayList = new ArrayList<Question>();


        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        // ここではデータ更新は行なっていないが、念のためコーディング
        mAdapter.notifyDataSetChanged();

        // ログインされていたら、Firebaseとリスナー設定
       /*if (user != null) {
            mFavoriteRef = mDatabaseReference.child(Const.FavoritePATH).child(user.getUid());
            mFavoriteRef.addChildEventListener(mFavoriteEventListener);
        }*/

        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        /*mAllArrayList.clear();
        // アダプターにmQuestionArrayList（データ）をセットする
        mAdapter.setQuestionArrayList(mAllArrayList);
        // 質問のmListView用のアダプタに渡す
        mListView.setAdapter(mAdapter);*/


        for (count = 1; count < 5; count++) {
            mAllRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(count));
            if (count == 1) {
                mAllRef.addChildEventListener(mAllEventListener);
            }else if (count == 2){
                mAllRef.addChildEventListener(mAllEventListener2);
            }else if (count == 3){
                mAllRef.addChildEventListener(mAllEventListener3);
            }else if (count == 4){
                mAllRef.addChildEventListener(mAllEventListener4);
            }
        }


        // 質問一覧の内、特定の質問をクリックしたら
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Questionのインスタンスを渡して質問詳細画面を起動する
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                /*if (mGenre == 0){
                    intent.putExtra("question", mAllArrayList.get(position));
                }else {*/
                intent.putExtra("question", mQuestionArrayList.get(position));
                //}
                intent.putExtra("favorite", mFavoriteArrayList);
                startActivity(intent);
            }
        });
    }

    // Activity再開時に呼ばれるメソッド
    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        if (UserLoaded) {
            // ログイン状態ならお気に入りメニューを表示
            Menu menu = navigationView.getMenu();
            MenuItem favoritemenuItem = menu.findItem(R.id.nav_favorite);
            favoritemenuItem.setVisible(true);
            favoritemenuItem.setEnabled(true);
            navigationView.invalidate();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null && mFavoriteArrayList.size() == 0) {
                mFavoriteRef = mDatabaseReference.child(Const.FavoritePATH).child(user.getUid());
                mFavoriteRef.addChildEventListener(mFavoriteEventListener);
            }

        } else {
            // ログインされていないならお気に入りメニューを非表示
            Menu menu = navigationView.getMenu();
            MenuItem favoritemenuItem = menu.findItem(R.id.nav_favorite);
            favoritemenuItem.setVisible(false);
            favoritemenuItem.setEnabled(false);

            mFavoriteArrayList.clear();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // 右上の設定アイテムを選択したらSettingActivity.classへ移動
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
