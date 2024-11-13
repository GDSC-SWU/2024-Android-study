package com.example.todolist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.databinding.ActivityAddTodoBinding
import com.example.todolist.databinding.ItemTodoBinding
import com.example.todolist.db.TodoEntity


class TodoRecyclerViewAdapter(private val todoList : ArrayList<TodoEntity>,private val listner: OnItemLongClickListner)
    :RecyclerView.Adapter<TodoRecyclerViewAdapter.MyViewHolder>()
{
    inner class MyViewHolder(binding: ItemTodoBinding):RecyclerView.ViewHolder(binding.root)
    {
        val tv_importance = binding.tvImportance
        val tv_title = binding.tvTitle

        val root = binding.root //아이디가 없더라도 루트 레이아웃을 뜻함.
    }

    //RecyclerView.Adapter 상속 시 구현해야하는 함수들

    //MyViewHolder를 객체로 반환. 여기서 뷰객체를 만드는 역할
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding : ItemTodoBinding = ItemTodoBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        //여기서 parent는 리사이클러 뷰가 위치한 부모 레이아웃(컨스트레인트 레이아웃) 말함.
        //parent가 가진 context를 가져와서 parent에 추가하지만 바로 추가하지 않고 설정 후 추가
        return MyViewHolder(binding)
    }


    //아이템의 개수 반환 함수
    override fun getItemCount(): Int {
        return todoList.size
    }

    //만든 MyViewHolder와 데이터를 묶어주는 역할
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val todoData = todoList[position]

        when (todoData.importance){
            1->{holder.tv_importance.setBackgroundResource(R.color.red)}
            2->{holder.tv_importance.setBackgroundResource(R.color.yellow)}//리사이클러뷰는 재활용이라 재설정 필요
            3->{holder.tv_importance.setBackgroundResource(R.color.green)}
        }

        holder.tv_importance.text = todoData.importance.toString()
        holder.tv_title.text = todoData.title

        holder.root.setOnLongClickListener {
            listner.onLongClick(position)
            false //false로 해야지 다른 클릭 이벤트도 실행됨. true는 이 이벤트만 실행됨.
        }
    }
}