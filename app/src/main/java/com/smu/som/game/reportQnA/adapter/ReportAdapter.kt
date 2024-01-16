package com.smu.som.game.reportQnA.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smu.som.R
import com.smu.som.game.reportQnA.model.response.ReportResponse


// 질문 배열을 목록 형태로 보여주는 Adapter
class ReportAdapter(
    var questionList: ArrayList<ReportResponse.AnswerAndQuestionList>,
    val inflater: LayoutInflater,
    val mListener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onSendButtonClick(reportResponseAndQuestionList: ReportResponse.AnswerAndQuestionList)
    }

    companion object {
        const val VIEW_TYPE_WITH_BUTTON = 1
        const val VIEW_TYPE_WITHOUT_BUTTON = 0
    }

    private var currentViewType = VIEW_TYPE_WITH_BUTTON

    fun setViewType(viewType: Int) {
        currentViewType = viewType
    }

    override fun getItemViewType(position: Int): Int {
        return currentViewType
    }


    inner class ItemWithoutButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val question: TextView
        val answer: TextView

        init {
            question = itemView.findViewById(R.id.question)
            answer = itemView.findViewById(R.id.answer)
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: ReportResponse.AnswerAndQuestionList) {
            question.text = (bindingAdapterPosition + 1).toString() + ". " + item.question
            answer.text = item.playerId + " : " + item.answer
        }
    }


    inner class ItemWithViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val question: TextView
        val answer: TextView
        val send: Button


        init {
            question = itemView.findViewById(R.id.question)
            answer = itemView.findViewById(R.id.answer)
            send = itemView.findViewById(R.id.send_btn)


            send.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onSendButtonClick(questionList[position])
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: ReportResponse.AnswerAndQuestionList) {
            question.text = (bindingAdapterPosition + 1).toString() + ". " + item.question
            answer.text = item.playerId + " : " + item.answer
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_WITH_BUTTON -> {
                val view = inflater.inflate(R.layout.dialog_report_item_send, parent, false)
                ItemWithViewHolder(view)
            }
            VIEW_TYPE_WITHOUT_BUTTON -> {
                val view = inflater.inflate(R.layout.dialog_report_item, parent, false)
                ItemWithoutButtonViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid viewType")
        }
    }

    // ViewHolder 안의 내용을 position 에 해당하는 데이터로 교체
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_WITH_BUTTON -> {
                (holder as? ItemWithViewHolder)?.bind(questionList[position])
            }
            VIEW_TYPE_WITHOUT_BUTTON -> {
                (holder as? ItemWithoutButtonViewHolder)?.bind(questionList[position])
            }
        }
    }

    // 전체 데이터 수 반환
    override fun getItemCount(): Int {
        return questionList.size
    }

}