package com.duzhaokun123.bilibilihd.ui.universal.reply

import android.graphics.Rect
import android.os.Message
import android.view.View
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duzhaokun123.bilibilihd.Application
import com.duzhaokun123.bilibilihd.R
import com.duzhaokun123.bilibilihd.bases.BaseActivity2
import com.duzhaokun123.bilibilihd.databinding.ActivityChildReplyBinding
import com.duzhaokun123.bilibilihd.utils.ListUtil
import com.duzhaokun123.bilibilihd.utils.TipUtil
import com.duzhaokun123.bilibilihd.utils.XRecyclerViewUtil
import com.duzhaokun123.bilibilihd.utils.pBilibiliClient
import com.hiczp.bilibili.api.main.model.ChildReply2
import com.hiczp.bilibili.api.main.model.SendReplyResponse
import com.jcodecraeer.xrecyclerview.XRecyclerView

class ChildReplyActivity : BaseActivity2<ActivityChildReplyBinding>() {
    companion object {
        const val EXTRA_TYPE = "type"
        const val EXTRA_OID = "oid"
        const val EXTRA_ROOT = "root"
        const val EXTRA_PARENT = "parent"
        const val EXTRA_USERNAME = "username"

        const val WHAT_REPLY_REFRESH = 0
        const val WHAT_REPLY_REFRESH_END = 1
        const val WHAT_REPLY_LOAD_MORE = 2
        const val WHAT_REPLY_LOAD_MORE_END = 3
        const val WHAT_SET_PARENT = 4
    }

    private var type = 0
    private var oid = 0L
    private var root = 0L

    private var next = 0L
    private var isEnd = false

    private lateinit var childReply: ChildReply2

    override fun initConfig() = setOf(Config.NEED_HANDLER)

    override fun initLayout() = R.layout.activity_child_reply

    override fun initView() {
        startIntent.let {
            type = it.getIntExtra(EXTRA_TYPE, 0)
            oid = it.getLongExtra(EXTRA_OID, 0)
            root = it.getLongExtra(EXTRA_ROOT, 0)
        }
        baseBind.tvType.text = type.toString()
        baseBind.tvOid.text = oid.toString()
        baseBind.tvRoot.text = root.toString()
        baseBind.etParent.hint = root.toString()

        baseBind.xrv.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect[0, 0, 0] = resources.getDimensionPixelOffset(R.dimen.divider_height)
            }
        })
        baseBind.xrv.layoutManager = LinearLayoutManager(this)
        baseBind.xrv.setLoadingListener(object : XRecyclerView.LoadingListener {
            override fun onLoadMore() {
                if (isEnd.not()) {
                    handler?.sendEmptyMessage(RootReplyFragment.WHAT_REPLY_LOAD_MORE)
                } else {
                    baseBind.xrv.setNoMore(true)
                }
            }

            override fun onRefresh() {
                isEnd = false
                handler?.sendEmptyMessage(RootReplyFragment.WHAT_REPLY_REFRESH)
            }
        })
        baseBind.btnSend.setOnClickListener {
            Thread {
                var sendReplyResponse: SendReplyResponse? = null
                try {
                    sendReplyResponse = Application.getPBilibiliClient().pMainAPI.sendReply(oid, baseBind.etText.text.toString(),
                            baseBind.etParent.text.toString().toLongOrNull() ?: root, root, type)
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread { TipUtil.showToast(e.message) }
                }
                if (sendReplyResponse != null) {
                    runOnUiThread {
                        baseBind.xrv.refresh()
                        baseBind.etText.text = null
                        baseBind.etParent.text = null
                    }
                }
            }.start()
        }
    }

    override fun initData() {
        baseBind.xrv.refresh()
    }

    override fun initRegisterCoordinatorLayout() = baseBind.clRoot

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        baseBind.clRoot.updatePadding(top = fixTopHeight)
    }

    override fun handlerCallback(msg: Message) {
        when (msg.what) {
            WHAT_REPLY_REFRESH -> Thread {
                try {
                    childReply = pBilibiliClient.pMainAPI.childReply2(oid, root, type, next)
                    next = childReply.data.cursor.next
                    isEnd = childReply.data.cursor.isEnd
                    if (childReply.data.root.replies == null) {
                        isEnd = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread { TipUtil.showTip(this, e.message) }
                } finally {
                    handler?.sendEmptyMessage(WHAT_REPLY_REFRESH_END)
                }
            }.start()
            WHAT_REPLY_REFRESH_END -> {
                if (::childReply.isInitialized) {
                    baseBind.xrv.adapter = ChildReplyAdapter(this, childReply)
                    baseBind.xrv.refreshComplete()
                    baseBind.tvRcount.text = childReply.data.root.rcount.toString()
                }
            }
            WHAT_REPLY_LOAD_MORE -> Thread {
                try {
                    val childReply2 = pBilibiliClient.pMainAPI.childReply2(oid, root, type, next)
                    next = childReply2.data.cursor.next
                    isEnd = childReply2.data.cursor.isEnd
                    if (childReply.data.root.replies == null) {
                        isEnd = true
                    }
                    childReply2.data.root.replies?.let {
                        ListUtil.addAll(childReply.data.root.replies, it)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread { TipUtil.showTip(this, e.message) }
                } finally {
                    handler?.sendEmptyMessage(WHAT_REPLY_LOAD_MORE_END)
                }
            }.start()
            WHAT_REPLY_LOAD_MORE_END -> {
                baseBind.xrv.loadMoreComplete()
                if (::childReply.isInitialized) {
                    XRecyclerViewUtil.notifyItemsChanged(baseBind.xrv, childReply.data.root.replies!!.lastIndex)
                }
            }
            WHAT_SET_PARENT -> {
                baseBind.etParent.setText(msg.data.getLong(EXTRA_PARENT).toString())
                baseBind.etText.text = baseBind.etText.text.insert(0, "回复 @${msg.data.getString(EXTRA_USERNAME)} :")
            }
        }
    }
}