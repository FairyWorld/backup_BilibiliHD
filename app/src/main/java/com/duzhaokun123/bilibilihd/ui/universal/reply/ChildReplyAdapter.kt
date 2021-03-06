package com.duzhaokun123.bilibilihd.ui.universal.reply

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Message
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.widget.PopupMenu
import com.bumptech.glide.Glide
import com.duzhaokun123.bilibilihd.Application
import com.duzhaokun123.bilibilihd.R
import com.duzhaokun123.bilibilihd.bases.BaseSimpleAdapter
import com.duzhaokun123.bilibilihd.databinding.ItemReplyBinding
import com.duzhaokun123.bilibilihd.ui.userspace.UserSpaceActivity
import com.duzhaokun123.bilibilihd.utils.*
import com.github.salomonbrys.kotson.get
import com.hiczp.bilibili.api.main.model.ChildReply2

class ChildReplyAdapter(context: Context, private val childReply: ChildReply2) : BaseSimpleAdapter<ItemReplyBinding>(context) {
    override fun getItemCount() = childReply.data.root.replies?.size ?: 0

    override fun initLayout() = R.layout.item_reply

    override fun initView(baseBind: ItemReplyBinding, position: Int) {
        childReply.data.root.replies?.get(position)?.let { reply ->
            baseBind.civFace.setOnClickListener {
                UserSpaceActivity.enter(activity!!, reply.mid, baseBind.civFace, null)
            }
        }
    }

    override fun initData(baseBind: ItemReplyBinding, position: Int) {
        childReply.data.root.replies?.get(position)?.let { reply ->
            baseBind.tvUsername.text = reply.member.uname
            baseBind.tvDate.text = DateTimeFormatUtil.getFormat1().format(reply.ctime.toLong() * 1000)
            baseBind.tvFloor.text = context.getString(R.string.hashtagd, reply.floor)
            baseBind.tvLike.text = reply.like.toString()
            baseBind.tvReply.text = reply.rcount.toString()
            GlideUtil.loadUrlInto(context, reply.member.avatar, baseBind.civFace, false)
            ImageViewUtil.setLevelDrawable(baseBind.ivLevel, reply.member.levelInfo.currentLevel)
            when (reply.action) {
                1 -> baseBind.tvAction.setText(R.string.liked)
                2 -> baseBind.tvAction.setText(R.string.disliked)
            }

            baseBind.tvContent.text = reply.content.message
            Thread {
                val messageSSB = SpannableStringBuilder(reply.content.message)
                reply.content.emote?.let { emotes ->
                    for (key in emotes.keySet()) {
                        val emote = emotes.get(key)
                        val emoteText = emote["text"].asString
                        var emoteBitmap: Bitmap? = null
                        val emoteSize = OtherUtils.dp2px(20f) * emote["meta"]["size"].asInt
                        try {
                            emoteBitmap = Glide.with(context).asBitmap().load(emote["url"].asString).submit(emoteSize, emoteSize).get()
                        } catch (e: Exception) {
                            //ignore
                        }
                        if (emoteBitmap != null) {
                            var index = -1
                            while (messageSSB.indexOf(emoteText, index + 1).also { index = it } != -1) {
                                messageSSB.setSpan(ImageSpan(context, emoteBitmap), index, index + emoteText.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                            }
                        }
                    }
                }
                activity?.runOnUiThread {
                    baseBind.tvContent.text = messageSSB
                    LinkifyUtil.addAllLinks(baseBind.tvContent)
                }
            }.start()

            baseBind.btnAction.setOnClickListener {
                val popupMenu = PopupMenu(context, baseBind.btnAction)
                popupMenu.menuInflater.inflate(R.menu.child_reply_action, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.like -> {
                            Thread {
                                try {
                                    Application.getPBilibiliClient().pMainAPI.likeReply(1, reply.oid, reply.rpid, reply.type)
                                    activity?.runOnUiThread {
                                        TipUtil.showTip(context, R.string.liked)
                                        baseBind.tvAction.setText(R.string.liked)
                                    }
                                    reply.action = 1
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    activity?.runOnUiThread { TipUtil.showTip(context, e.message) }
                                }
                            }.start()
                        }
                        R.id.dislike -> {
                            Thread {
                                try {
                                    Application.getPBilibiliClient().pMainAPI.dislikeReply(1, reply.oid, reply.rpid, reply.type)
                                    activity?.runOnUiThread {
                                        TipUtil.showTip(context, R.string.disliked)
                                        baseBind.tvAction.setText(R.string.disliked)
                                    }
                                    reply.action = 2
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    activity?.runOnUiThread { TipUtil.showTip(context, e.message) }
                                }
                            }.start()
                        }
                        R.id.cancel_action -> {
                            Thread {
                                when (reply.action) {
                                    0 -> activity?.runOnUiThread { TipUtil.showTip(context, R.string.no_action) }
                                    1, 2 -> {
                                        try {
                                            if (reply.action == 1) {
                                                Application.getPBilibiliClient().pMainAPI.likeReply(0, reply.oid, reply.rpid, reply.type)
                                            } else {
                                                Application.getPBilibiliClient().pMainAPI.dislikeReply(0, reply.oid, reply.rpid, reply.type)
                                            }
                                            reply.action = 0
                                            activity?.runOnUiThread {
                                                TipUtil.showTip(context, R.string.canceled)
                                                baseBind.tvAction.text = null
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            activity?.runOnUiThread { TipUtil.showTip(context, e.message) }
                                        }
                                    }
                                }
                            }.start()
                        }
                        R.id.reply -> {
                            val message = Message()
                            val bundle = Bundle()
                            bundle.putLong(ChildReplyActivity.EXTRA_PARENT, reply.rpid)
                            bundle.putString(ChildReplyActivity.EXTRA_USERNAME, reply.member.uname)
                            message.data = bundle
                            message.what = ChildReplyActivity.WHAT_SET_PARENT
                            baseActivity2?.handler?.sendMessage(message)
                        }
                        R.id.check_dialog -> {
                            // TODO: 20-8-15
                        }
                        R.id.delete ->
                            Thread {
                                try {
                                    pBilibiliClient.pMainAPI.delReply(reply.type, reply.oid, reply.rpid)
                                    runOnUiThread {
                                        TipUtil.showTip(context, R.string.deleted)
                                        baseBind.tvAction.setText(R.string.deleted)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    runOnUiThread { TipUtil.showTip(context, e.message) }
                                }
                            }.start()
                        R.id.report -> BrowserUtil.openWebViewActivity(context,
                                "https://www.bilibili.com/h5/comment/report?&pageType=${reply.type}&oid=${reply.oid}&rpid=${reply.rpid}", false, false)
                    }
                    true
                }
                popupMenu.show()
            }
        }
    }
}