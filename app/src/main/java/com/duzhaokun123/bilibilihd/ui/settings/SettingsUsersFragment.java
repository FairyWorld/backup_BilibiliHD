package com.duzhaokun123.bilibilihd.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.duzhaokun123.bilibilihd.R;
import com.duzhaokun123.bilibilihd.databinding.FragmentSettingsUsersBinding;
import com.duzhaokun123.bilibilihd.mybilibiliapi.MyBilibiliClient;
import com.duzhaokun123.bilibilihd.mybilibiliapi.space.SpaceAPI;
import com.duzhaokun123.bilibilihd.mybilibiliapi.space.model.Space;
import com.duzhaokun123.bilibilihd.pbilibiliapi.api.PBilibiliClient;
import com.duzhaokun123.bilibilihd.ui.LoginActivity;
import com.duzhaokun123.bilibilihd.ui.widget.BaseFragment;
import com.duzhaokun123.bilibilihd.utils.LoginUserInfoMap;
import com.duzhaokun123.bilibilihd.utils.OtherUtils;
import com.duzhaokun123.bilibilihd.utils.Settings;
import com.duzhaokun123.bilibilihd.utils.ToastUtil;
import com.duzhaokun123.bilibilihd.utils.XRecyclerViewUtil;
import com.hiczp.bilibili.api.app.model.MyInfo;
import com.hiczp.bilibili.api.passport.model.LoginResponse;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsUsersFragment extends BaseFragment<FragmentSettingsUsersBinding> {

    private LoginUserInfoMap loginUserInfoMap;
    private MyInfo myInfo;
    private LoginResponse exportLoginResponse;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        loginUserInfoMap = Settings.getLoginUserInfoMap(getContext());
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected int initConfig() {
        return NEED_HANDLER;
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_settings_users;
    }

    @Override
    protected void initView() {
        baseBind.xrv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        baseBind.xrv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.set(0, 0, 0, getResources().getDimensionPixelOffset(R.dimen.divider_height));
            }
        });
        baseBind.xrv.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new UserCardHolder(LayoutInflater.from(getContext()).inflate(R.layout.layout_user_card_item, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                ((UserCardHolder) holder).mTvContent.setText(String.valueOf(loginUserInfoMap.getByIndex(position).getUserId()));
                ((UserCardHolder) holder).mCv.setOnClickListener(v -> {
                    PBilibiliClient.Companion.getInstance().getBilibiliClient().setLoginResponse(loginUserInfoMap.getByIndex(position));
                    loginUserInfoMap.setLoggedUid(loginUserInfoMap.getByIndex(position).getUserId());
                    Settings.saveLoginUserInfoMap(getContext());
                    reloadLoggedUserInfo();
                });
                ((UserCardHolder) holder).mCv.setOnLongClickListener(v -> {
                    PopupMenu popupMenu = new PopupMenu(getContext(), ((UserCardHolder) holder).mCv);
                    popupMenu.getMenuInflater().inflate(R.menu.settings_user, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        switch (item.getItemId()) {
                            case R.id.export:
                                exportLoginResponse = loginUserInfoMap.getByIndex(position);
                                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.setType("application/json");
                                intent.putExtra(Intent.EXTRA_TITLE, "loginResponse.json");
                                startActivityForResult(intent, 1);
                                break;
                            case R.id.delete:
                                new AlertDialog.Builder(getContext()).setIcon(R.drawable.ic_info)
                                        .setTitle(R.string.delete)
                                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                            loginUserInfoMap.remove(loginUserInfoMap.getByIndex(position).getUserId());
                                            XRecyclerViewUtil.notifyItemsChanged(baseBind.xrv, loginUserInfoMap.size());
                                            Settings.saveLoginUserInfoMap(getContext());
                                            PBilibiliClient.Companion.getInstance().getBilibiliClient().setLoginResponse(loginUserInfoMap.getLoggedLoginResponse());
                                            reloadLoggedUserInfo();
                                        })
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .show();
                                break;
                        }
                        return true;
                    });
                    popupMenu.show();
                    return true;
                });
                // FIXME: 20-3-31 有没有返回小一点的可以查指定uid的头像和用户名的接口
                new Thread() {

                    private long uid = loginUserInfoMap.getByIndex(position).getUserId();

                    @Override
                    public void run() {
                        SpaceAPI.getInstance().getSpace(uid, new MyBilibiliClient.ICallback<Space>() {
                            @Override
                            public void onException(Exception e) {
                                e.printStackTrace();
                                if (getContext() != null) {
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> ToastUtil.sendMsg(getContext(), e.getMessage()));
                                    }
                                }
                            }

                            @Override
                            public void onSuccess(Space space) {
                                Message message = Message.obtain(null, () -> {
                                    if (getContext() != null) {
                                        Glide.with(getContext()).load(space.getData().getCard().getFace()).into(((UserCardHolder) holder).mCivFace);
                                    }
                                    ((UserCardHolder) holder).mTvName.setText(space.getData().getCard().getName());
                                });
                                message.what = 0;
                                if (handler != null) {
                                    handler.sendMessage(message);
                                }
                            }
                        });
                    }
                }.start();

            }

            @Override
            public int getItemCount() {
                if (loginUserInfoMap != null) {
                    return loginUserInfoMap.size();
                } else {
                    return 0;
                }
            }

            class UserCardHolder extends RecyclerView.ViewHolder {

                private CircleImageView mCivFace;
                private TextView mTvName, mTvContent;
                private CardView mCv;

                UserCardHolder(@NonNull View itemView) {
                    super(itemView);
                    mCivFace = itemView.findViewById(R.id.civ_face);
                    mTvName = itemView.findViewById(R.id.tv_name);
                    mTvContent = itemView.findViewById(R.id.tv_content);
                    mCv = itemView.findViewById(R.id.cv);
                }
            }
        });
        baseBind.xrv.setLoadingMoreEnabled(false);
        baseBind.xrv.setPullRefreshEnabled(false);

        baseBind.ibDelete.setOnClickListener(v -> {
            loginUserInfoMap.setLoggedUid(0);
            Settings.saveLoginUserInfoMap(getContext());
            PBilibiliClient.Companion.getInstance().getBilibiliClient().setLoginResponse(loginUserInfoMap.getLoggedLoginResponse());
            reloadLoggedUserInfo();
        });

        baseBind.ibAdd.setOnClickListener(v -> new AlertDialog.Builder(getContext()).setTitle(R.string.add)
                .setIcon(R.drawable.ic_add_circle)
                .setItems(new String[]{
                        getString(R.string.login),
                        getString(R.string.import_)
                }, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            Intent intent = new Intent(getContext(), LoginActivity.class);
                            startActivity(intent);
                            break;
                        case 1:
                            Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                            intent1.setType("*/*");
                            intent1.addCategory(Intent.CATEGORY_OPENABLE);
                            startActivityForResult(intent1, 0);
                            break;
                    }
                })
                .show());

    }

    @Override
    protected void initData() {
        reloadLoggedUserInfo();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case 0:
                if (data != null && resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    LoginResponse loginResponse = OtherUtils.readLoginResponseFromUri(getContext(), uri);
                    if (loginResponse != null) {
                        loginUserInfoMap.put(loginResponse.getUserId(), loginResponse);
                        Settings.saveLoginUserInfoMap(getContext());
                        XRecyclerViewUtil.notifyItemsChanged(baseBind.xrv, loginUserInfoMap.size());
                    } else {
                        ToastUtil.sendMsg(getContext(), R.string.bad_file);
                    }
                }
                break;
            case 1:
                if (data != null) {
                    Uri uri = data.getData();
                    if (!OtherUtils.writeLoginResponseToUri(getContext(), exportLoginResponse, uri)) {
                        ToastUtil.sendMsg(getContext(), R.string.failure);
                    }
                    exportLoginResponse = null;
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadLoggedUserInfo();
    }

    private void reloadLoggedUserInfo() {
        if (loginUserInfoMap.getLoggedUdi() != 0) {
            baseBind.tvContent.setText(String.valueOf(loginUserInfoMap.getLoggedLoginResponse().getUserId()));
            new Thread() {
                @Override
                public void run() {
                    try {
                        myInfo = PBilibiliClient.Companion.getInstance().getPAppAPI().getMyInfo();
                        if (handler != null) {
                            handler.sendEmptyMessage(2);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (getContext() != null) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> ToastUtil.sendMsg(getContext(), e.getMessage()));
                            }
                        }
                    }
                }
            }.start();
        } else {
            baseBind.tvContent.setText(null);
            baseBind.tvName.setText(R.string.not_logged_in);
            baseBind.civFace.setImageDrawable(null);
        }
    }

    @Override
    public void handlerCallback(@NonNull Message msg) {
        switch (msg.what) {
            case 0:
            case 1:
                msg.getCallback().run();
                break;
            case 2:
                baseBind.tvName.setText(myInfo.getData().getName());
                if (getContext() != null) {
                    Glide.with(getContext()).load(myInfo.getData().getFace()).into(baseBind.civFace);
                }
                break;
        }
    }
}