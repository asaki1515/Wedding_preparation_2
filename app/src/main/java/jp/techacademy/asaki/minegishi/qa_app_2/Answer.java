package jp.techacademy.asaki.minegishi.qa_app_2;

import java.io.Serializable;

public class Answer implements Serializable {
    private String mBody;   // Firebaseから取得した回答本文
    private String mName;   // Firebaseから取得した回答者の名前
    private String mUid;   // Firebaseから取得した回答者のUID
    private String mAnswerUid;   // Firebaseから取得した回答のUID

    public Answer(String body, String name, String uid, String answerUid) {
        mBody = body;
        mName = name;
        mUid = uid;
        mAnswerUid = answerUid;
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

    public String getAnswerUid() {
        return mAnswerUid;
    }
}
