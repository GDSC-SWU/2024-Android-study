package com.example.todolist

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolist.databinding.ActivityMainBinding
import com.example.todolist.db.AppDatabase
import com.example.todolist.db.TodoDao
import com.example.todolist.db.TodoEntity

class MainActivity : AppCompatActivity(), OnItemLongClickListener {
    //뷰 바인딩
    private lateinit var binding : ActivityMainBinding

    private lateinit var db : AppDatabase
    private lateinit var todoDao: TodoDao
    private lateinit var todoList: ArrayList<TodoEntity>
    private lateinit var adapter : TodoRecyclerViewAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db=AppDatabase.getInstance(this)!!
        todoDao=db.getTodoDao()

        getAllTodoList()

        binding.btnAddTodo.setOnClickListener {
            val intent = Intent(this,AddTodoActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getAllTodoList(){
        Thread{
            todoList= ArrayList(todoDao.getAllTodo())
            setRecylcerView()
        }.start()
    }

    private fun setRecylcerView(){

        //메인 스레드에서 실행하기 위해
        runOnUiThread{
            //리스트 삭제 리스너를 this(activity)로 넘김
            adapter= TodoRecyclerViewAdapter(todoList,this)
            binding.recyclerview.adapter=adapter
            binding.recyclerview.layoutManager=LinearLayoutManager(this)

        }
    }

    override fun onRestart() {
        super.onRestart()
        //페이지 전환시 리스트 갱신
        getAllTodoList()
    }

    override fun onLongClick(position: Int) {
        val builder : AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.alert_title))
        builder.setMessage(getString(R.string.alert_message))
        builder.setNegativeButton(getString(R.string.alert_no),null)
        builder.setPositiveButton(getString(R.string.alert_yes),
            object : DialogInterface.OnClickListener{
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    deleteTodo(position)
                }
            })
        builder.show()
    }
    private fun deleteTodo(position: Int){
        Thread{
            todoDao.deleteTodo(todoList[position])
            todoList.removeAt(position)
            runOnUiThread{
                adapter.notifyDataSetChanged()
                Toast.makeText(this,"삭제되었습니다.",Toast.LENGTH_SHORT).show()
            }
        }.start()
    }
}