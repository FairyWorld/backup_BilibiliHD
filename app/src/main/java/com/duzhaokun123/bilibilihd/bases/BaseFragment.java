package com.duzhaokun123.bilibilihd.bases;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

import com.duzhaokun123.bilibilihd.utils.Handler;

public abstract class BaseFragment<layout extends ViewDataBinding> extends Fragment implements Handler.IHandlerMessageCallback {

    protected static final int NEED_HANDLER = 0b010;

    protected final String CLASS_NAME = this.getClass().getSimpleName();
    protected layout baseBind;
    @Nullable
    protected Handler handler;
    protected boolean isStopped = true;

    private boolean firstCreate = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            firstCreate = false;
        }

        super.onCreate(savedInstanceState);

        int config = initConfig();
        if ((config & NEED_HANDLER) == NEED_HANDLER) {
            handler = new Handler(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        baseBind = DataBindingUtil.inflate(inflater, initLayout(), container, false);
        View parentView = baseBind.getRoot();

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        findViews(parentView);
        initView();
        initData();
        return parentView;
    }

    @Override
    public void onStart() {
        isStopped = false;
        super.onStart();
    }

    @Override
    public void onStop() {
        isStopped = true;
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.destroy();
            handler = null;
        }
    }

    @Nullable
    public BaseActivity<?> getBaseActivity() {
        Activity activity = getActivity();
        if (activity instanceof BaseActivity) {
            return (BaseActivity<?>) activity;
        } else {
            return null;
        }
    }

    @Nullable
    public BaseActivity2<?> getBaseActivity2() {
        Activity activity = getActivity();
        if (activity instanceof BaseActivity2) {
            return (BaseActivity2<?>) activity;
        } else {
            return null;
        }
    }

    @Nullable
    public Handler getHandler() {
        return handler;
    }

    @NonNull
    public BaseActivity<?> requireBaseActivity() {
        BaseActivity<?> activity = getBaseActivity();
        if (activity == null) {
            throw new IllegalStateException("Fragment " + this + " not attached to an baseActivity.");
        }
        return activity;
    }

    @NonNull
    public BaseActivity2<?> requireBaseActivity2() {
        BaseActivity2<?> activity = getBaseActivity2();
        if (activity == null) {
            throw new IllegalStateException("Fragment " + this + " not attached to an baseActivity2.");
        }
        return activity;
    }

    public boolean isFirstCreate() {
        return firstCreate;
    }

    protected abstract int initConfig();

    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
    }

    protected abstract int initLayout();

    protected void findViews(@NonNull View parentView) {
    }

    protected abstract void initView();

    protected abstract void initData();

}
