package com.example.sudokugame

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class HomeActivity : AppCompatActivity() {

    private lateinit var nameEditText: TextInputEditText
    private lateinit var levelSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        nameEditText = findViewById(R.id.textInputEditText)
        levelSpinner = findViewById(R.id.levelSpinner)

        val levels = arrayOf("Easy", "Medium", "Hard")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, levels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        levelSpinner.adapter = adapter

        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {
            validateAndNavigate()
        }
    }

    private fun validateAndNavigate() {
        val name = nameEditText.text.toString()
        val level = levelSpinner.selectedItem.toString()

        if (name.isNotEmpty()) {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("playerName", name)
                putExtra("difficultyLevel", level)
            }
            startActivity(intent)
        } else {
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please choose a level", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
