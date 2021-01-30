package com.duzhaokun123.bilibilihd.ui.welcome;

import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;

import com.duzhaokun123.bilibilihd.BuildConfig;
import com.duzhaokun123.bilibilihd.R;
import com.duzhaokun123.bilibilihd.bases.BaseFragment;
import com.duzhaokun123.bilibilihd.databinding.FragmentWelcomeAdBinding;
import com.duzhaokun123.bilibilihd.utils.ApiUtil;
import com.duzhaokun123.bilibilihd.utils.BrowserUtil;
import com.duzhaokun123.bilibilihd.utils.GlideUtil;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.hiczp.bilibili.api.app.model.SplashList;

import java.util.Objects;

public class WelcomeAdFragment extends BaseFragment<FragmentWelcomeAdBinding> {

    public WelcomeAdFragment() {
    }

    WelcomeAdFragment(@NonNull SplashList splashList) {
        this.splashList = splashList;
    }

    private SimpleExoPlayer player;

    private SplashList splashList;

    private boolean keep = false;

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
            if (getBaseActivity2() != null && getBaseActivity2().getHandler() != null) {
                getBaseActivity2().getHandler().sendEmptyMessage(1);
            }
        });
        baseBind.btnKeep.setOnClickListener(v -> {
            keep = true;
            if (getBaseActivity2() != null && getBaseActivity2().getHandler() != null) {
                getBaseActivity2().getHandler().removeCallbacksAndMessages(null);
            }
        });
    }

    @Override
    protected void initData() {
        baseBind.tvVersion.setText(BuildConfig.VERSION_NAME);
        SplashList.Data.List_ list_ = ApiUtil.INSTANCE.getShowList(splashList);
        if (list_ != null) {
            if (list_.isAd()) {
                baseBind.tvAd.setVisibility(View.VISIBLE);
            }
            baseBind.iv.setVisibility(View.VISIBLE);
            GlideUtil.loadUrlInto(getContext(), list_.getThumb(), baseBind.iv, false);
            baseBind.tvTitle.setText(list_.getUriTitle());
            if (list_.getUri() != null) {
                baseBind.iv.setOnClickListener(v -> {
                    BrowserUtil.openCustomTab(requireContext(), list_.getUri());
                    baseBind.btnKeep.callOnClick();
                });
            }
            if (list_.getVideoUrl() != null) {
                baseBind.pv.setVisibility(View.VISIBLE);
                player = new SimpleExoPlayer.Builder(requireContext()).build();
                baseBind.pv.setPlayer(player);
                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(requireContext(), Util.getUserAgent(requireContext(), getString(R.string.app_name)));
                MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(list_.getVideoUrl()));
                player.prepare(mediaSource);
                player.addListener(new Player.EventListener() {
                    @Override
                    public void onIsPlayingChanged(boolean isPlaying) {
                        if (player.getContentDuration() - player.getContentPosition() < 100 && !keep) {
                            if (getBaseActivity2() != null && getBaseActivity2().getHandler() != null) {
                                getBaseActivity2().getHandler().sendEmptyMessage(1);
                            }
                        }
                    }
                });
                player.setPlayWhenReady(true);
                player.addListener(new Player.EventListener() {
                    @Override
                    public void onPlayerError(@NonNull ExoPlaybackException error) {
                        Objects.requireNonNull(requireBaseActivity().getHandler()).sendEmptyMessageDelayed(1, 2000);
                    }
                });
            } else if (getBaseActivity2() != null && getBaseActivity2().getHandler() != null) {
                getBaseActivity2().getHandler().sendEmptyMessageDelayed(1, 2000);
            }
        } else {
            Objects.requireNonNull(requireBaseActivity().getHandler()).sendEmptyMessageDelayed(1, 2000);
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
