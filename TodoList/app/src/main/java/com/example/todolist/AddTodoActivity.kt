package com.example.todolist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.todolist.databinding.ActivityAddTodoBinding
import com.example.todolist.databinding.ActivityMainBinding
import com.example.todolist.db.AppDatabase
import com.example.todolist.db.TodoDao
import com.example.todolist.db.TodoEntity

class AddTodoActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAddTodoBinding
    lateinit var db : AppDatabase
    lateinit var todoDao : TodoDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivityAddTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)!!
        todoDao=db.getTodoDao()

        binding.btnComplete.setOnClickListener {
            insertTodo()
        }
    }

    private fun insertTodo(){
        val todoTitle = binding.edtTitle.text.toString()
        //라디오 그룹에서 체크된 라디오 버튼의 id 값을 가져온다
        var todoImportance=binding.radioGroup.checkedRadioButtonId

        var imData =0;
        when(todoImportance){
            R.id.btn_high -> {
                imData = 1;
            }
            R.id.btn_middle -> {
                imData = 2;
            }
            R.id.btn_low -> {
                imData = 3;
            }
        }

        if(imData==0 || todoTitle.isBlank()){
            Toast.makeText(this,"모든 항목을 채워주세요.",Toast.LENGTH_SHORT).show()
        }else{
            Thread{
                todoDao.insertTodo(TodoEntity(null,todoTitle,imData))
                runOnUiThread{
                    Toast.makeText(this,"할 일이 추가되었습니다.",Toast.LENGTH_SHORT).show()
                    finish()
                }
            }.start()
        }

    }
}