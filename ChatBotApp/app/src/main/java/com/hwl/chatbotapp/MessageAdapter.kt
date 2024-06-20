package com.hwl.chatbotapp

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hwl.chatbotapp.app.App
import com.hwl.chatbotapp.databinding.ItemMessageBinding
import io.noties.markwon.Markwon

class MessageAdapter(private var messages: List<ChatMessage>, private val mainViewModel: MainViewModel) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    // 创建Markwon实例
    private val markwon = Markwon.create(App.INSTANCE)
    @SuppressLint("NotifyDataSetChanged")
    fun setMessages(newMessages: List<ChatMessage>) {
        this.messages = newMessages
        notifyDataSetChanged()  // 通知数据变更，刷新整个列表
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding,markwon,mainViewModel)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        Log.d("MessageAdapter", "Binding message at position $position")
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    class MessageViewHolder(private val binding: ItemMessageBinding,private val markwon: Markwon,private val mainViewModel: MainViewModel) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            if (message.isFromUser) {
                // 用户消息显示在右侧
                binding.tvMessage.apply {
                    text = message.message
                    visibility = View.VISIBLE
                }
                binding.layoutBotMessage.visibility = View.GONE
            } else {
                // AI消息显示在左侧
                binding.tvBotMessage.apply {
                    // 渲染md格式
                    markwon.setMarkdown(this, message.message)
                    Log.d("TAG", "bind: ${message.message}")
                }
                binding.layoutBotMessage.apply {
                    visibility = View.VISIBLE
                }
                binding.tvMessage.visibility = View.GONE
                binding.btnPlay.setOnClickListener {
                    // 处理点击事件
                    mainViewModel.sayText(binding.tvBotMessage.text.toString())
                }
            }
        }
    }
}
