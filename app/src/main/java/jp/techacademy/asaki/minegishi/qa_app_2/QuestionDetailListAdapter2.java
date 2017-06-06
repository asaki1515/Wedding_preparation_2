package jp.techacademy.asaki.minegishi.qa_app_2;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class QuestionDetailListAdapter2 extends BaseAdapter{
    private final static int TYPE_QUESTION = 0;
    private final static int TYPE_ANSWER = 1;

    private LayoutInflater mLayoutInflater = null;
    private Question mQustion;

    private TextView filelinkTextView;

    private WebView webView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private FrameLayout customViewContainer;
    private View mCustomView;


    public QuestionDetailListAdapter2(Context context, Question question) {
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mQustion = question;

    }

    @Override
    public int getCount() {
        return 1 + mQustion.getAnswers().size();
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_QUESTION;
        } else {
            return TYPE_ANSWER;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return mQustion;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_question_detail2, parent, false);
            }
            String body = mQustion.getBody();
            String name = mQustion.getName();
            String link = mQustion.getLink();
            String url = mQustion.getUrl();
            String fileName = mQustion.getFileName();
            String file = mQustion.getFile();
            String video = mQustion.getVideo();

            // youtube再生
            webView = (WebView) convertView.findViewById(R.id.myWebView);
            WebViewClient mWebViewClient = new WebViewClient();
            //リンクをタップしたときに標準ブラウザを起動させない
            webView.setWebViewClient(mWebViewClient);

            WebChromeClient mWebChromeClient = new myWebChromeClient();
            webView.setWebChromeClient(mWebChromeClient);

            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setAppCacheEnabled(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setSaveFormData(true);
            webView.loadUrl("https://www.youtube.com/watch?v="+ video);



            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);


            TextView fileTextView = (TextView) convertView.findViewById(R.id.fileTextView2);
            filelinkTextView = (TextView) convertView.findViewById(R.id.filelinkTextView);


            if (file.length() != 0) {
                fileTextView.setText(file);

                // ストレージ設定
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://qaapp4-4bf13.appspot.com");

                storageRef.child(fileName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        filelinkTextView.setText(uri.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            }else {
                fileTextView.setText("");
                filelinkTextView.setText("資料なし");
            }


            TextView urlTextView = (TextView) convertView.findViewById(R.id.urlTextView);
            TextView linkTextView = (TextView) convertView.findViewById(R.id.TextView2);
            if (url.length() != 0) {
                linkTextView.setText(link);
                urlTextView.setText(url);
            }else {
                urlTextView.setText("なし");
            }

        } else {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_answer, parent, false);
            }

            Answer answer = mQustion.getAnswers().get(position - 1);
            String body = answer.getBody();
            String name = answer.getName();

            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);
        }

        return convertView;
    }

    class myWebChromeClient extends WebChromeClient {

        @Override
        public void onShowCustomView(View view,CustomViewCallback callback) {
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }

            final FrameLayout frame = ((FrameLayout) view);

            final View v1 = frame.getChildAt(0);
            view.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
            v1.setOnKeyListener(new View.OnKeyListener() {

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                        onHideCustomView();
                        return true;
                    }
                    return false;
                }
            });

            mCustomView = view;
            customViewContainer.setBackgroundColor(Color.BLACK);
            customViewContainer.bringToFront();
            webView.setVisibility(View.GONE);
            customViewContainer.setVisibility(View.VISIBLE);

            customViewContainer.addView(view);
            customViewCallback = callback;
        }
    }
}
