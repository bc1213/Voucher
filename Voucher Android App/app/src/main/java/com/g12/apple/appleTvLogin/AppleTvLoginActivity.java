package com.g12.apple.appleTvLogin;


import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.g12.apple.R;
import com.g12.apple.appleTvLogin.datamodel.TokenTransferRequest;
import com.g12.apple.customviews.PinEntryEditText;
import com.g12.apple.customviews.VoucherCustomDialog;
import com.g12.apple.customviews.VoucherProgressDialog;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AppleTvLoginActivity extends AppCompatActivity {

    public static final String TAG = "AppleTvLoginActivity";

    @BindView(R.id.pinEntry)
    PinEntryEditText txtPinEntry;

    @BindView(R.id.llAnimation)
    LinearLayout llAnimation;

    @BindView(R.id.tvSearch)
    TextView tvSearchText;

    @BindView(R.id.submit)
    Button btnsubmit;

    private String pin = "";
    private AppleTvLoginHelper mTvLoginHelper;
    private AppleTvConnection mTvConnection;
    private TextView mStatusView;
    private Handler mUpdateHandler;
    private Context mContext;
    private String appleTvName = "TV";
    private final Handler stopDiscoveryhandler = new Handler();
    private CountDownTimer countDownTimer;
    private Disposable subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apple_tv_login);
        mContext = this;
        ButterKnife.bind(this);

        getSupportActionBar().setTitle("Apple TV App Login");
        getSupportActionBar().setHomeButtonEnabled(true);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mStatusView = (TextView) findViewById(R.id.status);

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                addChatLine(chatLine);
                if(chatLine.contains("them")){
                    stopDiscoveryhandler.removeCallbacksAndMessages(null);
                    subscription.dispose();
                    String [] ids = chatLine.split("#");
                    if(ids[1]!=null){
                        appleTvName = ids[1].replace("them:","");
                        tvSearchText.setText("Connecting to "+appleTvName);
                    }

                    if(ids[2]!=null){
                        makeApiCall(ids[2]);
                    }else{
                        Log.d(TAG,"Failed to get UDID of apple");
                    }

                }
            }
        };

        mTvConnection = new AppleTvConnection(mUpdateHandler);
        mTvLoginHelper = new AppleTvLoginHelper(this);
        mTvLoginHelper.initializeNsd();


        txtPinEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                pin = s.toString();
                if(s.toString().length()==4){
                    btnsubmit.setVisibility(View.VISIBLE);
                }else{
                    btnsubmit.setVisibility(View.INVISIBLE);
                    llAnimation.setVisibility(View.INVISIBLE);
                    tvSearchText.setVisibility(View.INVISIBLE);
                }

            }
        });

        txtPinEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(pin==null||pin.length()<4){
                        Toast.makeText(getApplicationContext(), "Enter the pin shown in TV",Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    llAnimation.setVisibility(View.VISIBLE);
                    tvSearchText.setVisibility(View.VISIBLE);
                    startAppleTvDiscoveryService(pin);
                }
                return false;
            }
        });

        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pin == null || pin.length() < 4) {
                    Toast.makeText(getApplicationContext(), "Enter the pin shown in TV", Toast.LENGTH_SHORT).show();
                    return;
                }
                InputMethodManager imm = (InputMethodManager) txtPinEntry.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(txtPinEntry.getWindowToken(), 0);

                llAnimation.setVisibility(View.VISIBLE);
                tvSearchText.setVisibility(View.VISIBLE);
                startAppleTvDiscoveryService(pin);
            }
        });

        showKeyboard(txtPinEntry);
    }

    public void showKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }


    private void makeApiCall(String UUID) {
        VoucherProgressDialog.show(this);

        String dummyResponse = "somethingToken";
        afterGettingAPIResponse(dummyResponse);

    }

    private void afterGettingAPIResponse(String dummyResponse) {
        VoucherProgressDialog.dismissDialog();

        try {
            if(dummyResponse != null ){

                if (countDownTimer != null)
                    countDownTimer.cancel();

                tvSearchText.setText("Connecting to "+appleTvName.replace("them:",""));

                final TokenTransferRequest transferRequest = new TokenTransferRequest();
                transferRequest.setToken(dummyResponse);
                transferRequest.setUserID("User_1212");
                transferRequest.setDeviceID("Android|ANANJAN1256");

                final VoucherCustomDialog dialog = new VoucherCustomDialog(mContext);
                dialog.setCancelable(false);
                dialog.setTitle("App Login !");
                dialog.setMessage("Your credentials will be used to login to Voucher on "+appleTvName.replace("them:","")+" \nDo you want to continue?");
                dialog.setPositiveButton("Ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Gson gson = new Gson();

                        mTvConnection.sendMessage(gson.toJson(transferRequest));
                        Toast.makeText(mContext,"Connected to : "+appleTvName,Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

                dialog.setNegativeButton("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        finish();
                    }
                });
                dialog.show();

            }else{
                Toast.makeText(mContext,"Failed to connect",Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startAppleTvDiscoveryService(String code) {
        VoucherProgressDialog.show(mContext,false);
        btnsubmit.setClickable(false);
        startExpiryTimer(10);//Not starting timer as of now
        if(mTvConnection.getLocalPort() > -1) {
            mTvLoginHelper.registerService(mTvConnection.getLocalPort(), code);
        } else {
            Log.d(TAG, "ServerSocket isn't bound.");
        }

        stopServiceFunctionRxJava(10000);//Not starting timer as of now
    }

    private void stopServiceFunctionRxJava(long delayMilliSeconds){
        subscription = Observable.timer(delayMilliSeconds, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> showNotFoundAlert());
    }

    private void showNotFoundAlert() {
        VoucherProgressDialog.dismissDialog();
        final VoucherCustomDialog dialog = new VoucherCustomDialog(mContext);
        dialog.setCancelable(false);
        dialog.setTitle("No Apple TV found");
        dialog.setMessage("Make sure your Android device and Apple TV are connected to the same WiFi network");
        dialog.setPositiveButton("Try Again !", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                llAnimation.setVisibility(View.VISIBLE);

//                            mTvConnection.tearDown();
                mTvLoginHelper.tearDown();
                llAnimation.setVisibility(View.INVISIBLE);
                txtPinEntry.setText("");
                showKeyboard(txtPinEntry);
                btnsubmit.setClickable(true);
            }
        });

        dialog.setNegativeButton("Close", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
        dialog.show();
    }

    private void startExpiryTimer(int timeToExpireInSeconds) {
        if (timeToExpireInSeconds > 0) {
            timeToExpireInSeconds = timeToExpireInSeconds * 1000;
            if (countDownTimer != null)
                countDownTimer.cancel();

            countDownTimer = new CountDownTimer(timeToExpireInSeconds, 1000) {

                public void onTick(long millisUntilFinished) {
                    millisUntilFinished = millisUntilFinished / 1000;
                    long min = millisUntilFinished / 60;
                    long sec = millisUntilFinished % 60;
                    String arg1 = String.valueOf(min);
                    String arg2 = String.valueOf(sec);
                    if (String.valueOf(arg1).length() == 1)
                        arg1 = "0" + arg1;
                    if (arg2.toString().length() == 1)
                        arg2 = "0" + arg2;

                    tvSearchText.setText(getString(R.string.search_wifi)+" ("+arg1+":"+arg2+")");
                }

                public void onFinish() {
                    tvSearchText.setText("No Apple TV found");
                    llAnimation.setVisibility(View.INVISIBLE);
                }
            }.start();

        }
    }

    private void stopServiceAfter(int milliSeconds) {

        stopDiscoveryhandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    VoucherProgressDialog.dismissDialog();
                    final VoucherCustomDialog dialog = new VoucherCustomDialog(mContext);
                    dialog.setCancelable(false);
                    dialog.setTitle("No Apple TV found");
                    dialog.setMessage("Make sure your Android device and Apple TV are connected to the same WiFi network");
                    dialog.setPositiveButton("Try Again !", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
//                            mTvConnection.tearDown();
                            mTvLoginHelper.tearDown();
                            llAnimation.setVisibility(View.INVISIBLE);
                            txtPinEntry.setText("");
                            showKeyboard(txtPinEntry);
                            btnsubmit.setClickable(true);
                        }
                    });

                    dialog.setNegativeButton("Close", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    dialog.show();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, milliSeconds);
    }


    public void addChatLine(String line) {
        mStatusView.append("\n" + line);
    }


    //Activity life cycles
    @Override
    protected void onPause() {
        Log.d(TAG, "onPause called");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume called");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");
        mTvLoginHelper.tearDown();
        subscription.dispose();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop called");
        super.onStop();
    }
}
