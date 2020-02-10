package com.g12.apple.customviews;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.g12.apple.R;

public class VoucherCustomDialog extends Dialog {
    private Context mContext;
    private TextView tvTitle;
    private TextView tvMessage;
    private TextView tvIcon;
    private ListView lvListView;

    private Button btnNegative;
    private Button btnPositive;

    private ArrayAdapter<String> arrayAdapter;

    public VoucherCustomDialog(@NonNull Context context) {
        super(context, R.style.Theme_Dialog);
        mContext = context;
        init();
    }

    public VoucherCustomDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
        init();
    }

    public VoucherCustomDialog(@NonNull Context context, boolean cancelable) {
        super(context, cancelable, null);
        mContext = context;
        init();
    }

    protected VoucherCustomDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
        init();
    }

    private void init() {
        setContentView(R.layout.view_dialog);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvMessage = (TextView) findViewById(R.id.tvMessage);
        tvIcon = (TextView) findViewById(R.id.tvIcon);
        lvListView = (ListView) findViewById(R.id.lvListNames);

        btnNegative = (Button) findViewById(R.id.btnNegative);
        btnPositive = (Button) findViewById(R.id.btnPositive);
    }

    public VoucherCustomDialog setNegativeButton(String text, View.OnClickListener listener) {
        btnNegative.setVisibility(View.VISIBLE);
        btnNegative.setText(text);
        btnNegative.setOnClickListener(listener);
        return this;
    }

    public VoucherCustomDialog setPositiveButton(String text, View.OnClickListener listener) {
        btnPositive.setVisibility(View.VISIBLE);
        btnPositive.setText(text);
        btnPositive.setOnClickListener(listener);
        btnPositive.setFocusable(true);
        btnPositive.setFocusableInTouchMode(true);
        btnPositive.requestFocus();

        return this;
    }

    public void setTitle(int titleId) {
        tvTitle.setText(titleId);
    }

    public void setTitle(@Nullable CharSequence title) {
        tvTitle.setText(title);
    }


    public void setMessage(int titleId) {
        tvMessage.setText(titleId);
    }


    public void setMessage(@Nullable CharSequence title) {
        tvMessage.setText(title);
    }

    public void setIcon(int iconId) {
        tvIcon.setVisibility(View.VISIBLE);
        tvIcon.setText(iconId);
    }
}
