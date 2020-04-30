package com.duzhaokun123.bilibilihd.ui.welcome;

import android.net.Uri;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.duzhaokun123.bilibilihd.BuildConfig;
import com.duzhaokun123.bilibilihd.R;
import com.duzhaokun123.bilibilihd.bases.BaseFragment;
import com.duzhaokun123.bilibilihd.databinding.FragmentWelcomeAdBinding;
import com.duzhaokun123.bilibilihd.mybilibiliapi.welcomeAd.WelcomeAdApi;
import com.duzhaokun123.bilibilihd.mybilibiliapi.welcomeAd.model.WelcomeAd;
import com.duzhaokun123.bilibilihd.utils.BrowserUtil;
import com.duzhaokun123.bilibilihd.utils.GlideUtil;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.Objects;

public class WelcomeAdFragment extends BaseFragment<FragmentWelcomeAdBinding> {

    public WelcomeAdFragment() {
    }

    WelcomeAdFragment(@NonNull WelcomeAd welcomeAd) {
        this.welcomeAd = welcomeAd;
    }

    private SimpleExoPlayer player;

    private WelcomeAd welcomeAd;

    @Override
    protected int initConfig() {
        return 0;
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_welcome_ad;
    }

    @Override
    protected void initView() {

        baseBind.btnSkip.setOnClickListener(v -> {
            if (getBaseActivity() != null && getBaseActivity().getHandler() != null) {
                getBaseActivity().getHandler().sendEmptyMessage(1);
            }
        });
        baseBind.btnKeep.setOnClickListener(v -> {
            if (getBaseActivity() != null && getBaseActivity().getHandler() != null) {
                getBaseActivity().getHandler().removeCallbacksAndMessages(null);
            }
        });
    }

    @Override
    protected void initData() {
        baseBind.tvVersion.setText(BuildConfig.VERSION_NAME);
//        WelcomeAd.Data.List_ list_ = WelcomeAdApi.getShowList(welcomeAd);
        WelcomeAd.Data.List_ list_ = welcomeAd.getData().getList().get(7);
        if (list_ != null) {
            if (list_.isAd()) {
                baseBind.tvAd.setVisibility(View.VISIBLE);
            }
            baseBind.iv.setVisibility(View.VISIBLE);
            GlideUtil.loadUrlInto(getContext(), list_.getThumb(), baseBind.iv, false);
            if (list_.getUri() != null) {
                baseBind.iv.setOnClickListener(v -> BrowserUtil.openDefault(getContext(), list_.getUri()));
            }
            if (list_.getVideoUrl() != null) {
                baseBind.pv.setVisibility(View.VISIBLE);
                player = new SimpleExoPlayer.Builder(Objects.requireNonNull(getContext())).build();
                baseBind.pv.setPlayer(player);
                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), getString(R.string.app_name)));
                MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(list_.getVideoUrl()));
                player.prepare(mediaSource);
                player.addListener(new Player.EventListener() {
                    @Override
                    public void onIsPlayingChanged(boolean isPlaying) {
                        if ( player.getContentDuration() - player.getContentPosition() < 100) {
                            if (getBaseActivity() != null && getBaseActivity().getHandler() != null) {
                                getBaseActivity().getHandler().sendEmptyMessage(1);
                            }
                        }
                    }
                });
                player.setPlayWhenReady(true);
            } else if (getBaseActivity() != null && getBaseActivity().getHandler() != null) {
                getBaseActivity().getHandler().sendEmptyMessageDelayed(1, 2000);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
    }
}
