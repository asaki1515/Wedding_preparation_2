package jp.techacademy.asaki.minegishi.qa_app_2;

import java.io.Serializable;
import java.util.ArrayList;

// Firebaseから取得した質問のデータを保持するモデルクラス
public class Question implements Serializable {
    private String mTitle;  // Firebaseから取得したタイトル
    private String mBody;   // Firebaseから取得した質問本文
    private String mName;   // Firebaseから取得した質問者の名前
    private String mUid;    // Firebaseから取得した質問者のUID
    private String mQuestionUid;   //Firebaseから取得した質問のUID
    private String mUrl;////////
    private String mLink;/////
    private String mFileName;//----
    private String mFile;
    private String mVideo;///----///

    private int mGenre;   // 質問のジャンル
    private byte[] mBitmapArray;   // Firebaseから取得した画像をbyte型の配列にしたもの
    private ArrayList<Answer> mAnswerArrayList;   // Firebaseから取得した質問のモデルクラスであるAnswerのArrayList

    public String getTitle() {
        return mTitle;
    }

    public String getBody() {
        return mBody;
    }

    public String getName() {
        return mName;
    }

    public String getUid() {
        return mUid;
    }

    public String getQuestionUid() {
        return mQuestionUid;
    }

    public String getUrl(){////////
        return mUrl;
    }

    public String getLink(){
        return mLink;
    }

    public String getFileName(){
        return mFileName;
    }

    public String getFile(){
        return mFile;

    }
    public String getVideo(){
        return mVideo;
    }

    public int getGenre() {
        return mGenre;
    }

    public byte[] getImageBytes() {
        return mBitmapArray;
    }

    public ArrayList<Answer> getAnswers() {
        return mAnswerArrayList;
    }

    public Question(String title, String body, String name, String uid, String questionUid, String video, String link, String url, String fileName, String file, int genre, byte[] bytes, ArrayList<Answer> answers) {
        mTitle = title;
        mBody = body;
        mName = name;
        mUid = uid;
        mQuestionUid = questionUid;
        mVideo = video;
        mLink = link;
        mUrl = url;
        mFile = file;
        mFileName = fileName;
        mGenre = genre;
        mBitmapArray = bytes.clone();
        mAnswerArrayList = answers;
    }
}