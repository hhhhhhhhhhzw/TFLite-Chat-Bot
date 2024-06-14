import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hwl.chatbotapp.Message
import com.hwl.chatbotapp.databinding.ItemMessageBinding

class MessageAdapter(private val messages: List<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        // 使用ViewBinding加载布局
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount() = messages.size

    class MessageViewHolder(private val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            if (message.isUser) {
                // 用户消息显示在右侧
                binding.tvMessage.apply {
                    text = message.content
                    visibility = View.VISIBLE
                }
                binding.layoutBotMessage.visibility = View.GONE
            } else {
                // AI消息显示在左侧
                binding.tvBotMessage.apply {
                    text = message.content
                }
                binding.layoutBotMessage.apply {
                    visibility = View.VISIBLE
                }
                binding.tvMessage.visibility = View.GONE
            }
        }
    }
}
