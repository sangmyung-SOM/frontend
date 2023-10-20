package com.smu.som.chat.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.smu.som.R
import com.smu.som.chat.activity.ChatActivity
import com.smu.som.chat.model.response.ChatRoom

class ChatRoomAdapter(val context: Context)
    : RecyclerView.Adapter<ChatRoomAdapter.Holder>()
{

    var chatRoomList = ArrayList<ChatRoom>()
    val bundle: Bundle = Bundle()

    fun addItem(item: ChatRoom){
        chatRoomList.add(item)
        notifyDataSetChanged()
    }

    inner class Holder(itemView: View): RecyclerView.ViewHolder(itemView){

        val chatRoomNm = itemView.findViewById<TextView>(R.id.chatroom_nm)

        fun bind(chatRoom: ChatRoom, context: Context){
           chatRoomNm.text = chatRoom.roomName

            itemView.setOnClickListener {
                // 이름 입력하는 창이 뜨고, 확인을 누르면 채팅방으로 이동
                val builder = AlertDialog.Builder(context)
                val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog, null, false)
                builder.setView(dialogView)
                        .setPositiveButton("확인") { dialogInterface, i ->
                            val name = dialogView.findViewById<EditText>(R.id.name)
                            if(name != null){
                                bundle.putString("sender", name.text.toString())
                                bundle.putString("chatRoomId", chatRoom.roomId)

                                val intent = Intent(context, ChatActivity::class.java)
                                intent.putExtra("myBundle", bundle)

                                startActivity(context, intent, bundle)

                            }
                        }
                        .setNegativeButton("취소") { dialogInterface, i ->
                            /* 취소일 때 아무 액션이 없으므로 빈칸 */
                        }
                        .show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomAdapter.Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.chatroom_item, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return chatRoomList.size
    }

    override fun onBindViewHolder(holder: ChatRoomAdapter.Holder, position: Int) {
        holder.bind(chatRoomList[position], context)
    }
}