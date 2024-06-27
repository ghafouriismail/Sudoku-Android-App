package com.example.sudokugame

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val sudokuGrid = Array(9) { IntArray(9) }
    private val originalGrid = Array(9) { IntArray(9) }
    private val hiddenCells = mutableListOf<Pair<Int, Int>>()
    private var selectedButton: Button? = null
    private var hintCounter = 0
    private val maxHints = 3
    private var incorrectGuessCounter = 0
    private val maxIncorrectGuesses = 4
    private var difficultyLevel: String? = null
    private lateinit var myGrid: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val homeButton: Button = findViewById(R.id.home)
        homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        val playerNameTextView: TextView = findViewById(R.id.name)

        val playerName = intent.getStringExtra("playerName")
        playerNameTextView.text = playerName
        myGrid = findViewById(R.id.my_grid)
        myGrid.setBackgroundResource(R.drawable.gridbackground)
        val margin = resources.getDimensionPixelSize(R.dimen.grid_margin)
        myGrid.setPadding(margin, margin, margin, margin)
        myGrid.clipToPadding = false

        val gridSize = resources.displayMetrics.widthPixels + 40
        val buttonSize = gridSize / 11

        generateSudokuGrid()

        difficultyLevel = intent.getStringExtra("difficultyLevel")
        hideCells(difficultyLevel)

        for (i in 0..8) {
            for (j in 0.. 8) {

                val button = Button(this)
                val params = GridLayout.LayoutParams()
                params.width = buttonSize
                params.height = buttonSize
                params.columnSpec = GridLayout.spec(j)
                params.rowSpec = GridLayout.spec(i)
                button.layoutParams = params

                val number = sudokuGrid[i][j]
                if (number != 0) {
                    button.text = number.toString()
                } else {
                    button.setOnClickListener {
                        selectedButton = it as Button
                        showCustomKeyboard()
                    }
                }
                val tagValue = i * 9 + j
                button.tag = tagValue

                val buttonMargin = resources.getDimensionPixelSize(R.dimen.grid_margin)
                val buttonLayoutParams = button.layoutParams as GridLayout.LayoutParams
                buttonLayoutParams.setMargins(buttonMargin, buttonMargin, buttonMargin, buttonMargin)

                button.setBackgroundResource(R.drawable.cell_border)
                if ((i / 3 + j / 3) % 2 == 0) {
                    button.setBackgroundResource(R.drawable.button_background2)
                } else {
                    button.setBackgroundResource(R.drawable.button_background)
                }

                myGrid.addView(button)
            }
        }

        val hintButton = findViewById<Button>(R.id.Hint)

        hintButton.setOnClickListener {
            if (hintCounter < maxHints) {
                if (hiddenCells.isNotEmpty()) {
                    val randomIndex = (0 until hiddenCells.size).random()
                    val (row, col) = hiddenCells[randomIndex]

                    val originalNum = originalGrid[row][col]
                    sudokuGrid[row][col] = originalNum

                    val buttonIndex = row * 9 + col
                    val button = myGrid.getChildAt(buttonIndex) as Button

                    button.text = originalNum.toString()

                    button.setTextColor(Color.BLUE)
                    hiddenCells.removeAt(randomIndex)

                    hintCounter++
                } else {

                    Toast.makeText(this, "No more hidden cells to reveal", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Maximum hints used", Toast.LENGTH_SHORT).show()
            }
        }

        val finishButton = findViewById<Button>(R.id.finish)
        finishButton.setOnClickListener {
            if (isSudokuSolvedCorrectly()) {
                Toast.makeText(this, "Congratulations! You win!", Toast.LENGTH_LONG).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 4000)
            } else {
                Toast.makeText(this, "The Sudoku puzzle is not solved correctly. Keep trying!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateSudokuGrid() {
        generateSudokuGridHelper(0, 0)
        for (i in 0 until 9) {
            for (j in 0 until 9) {
                originalGrid[i][j] = sudokuGrid[i][j]
            }
        }
    }

    private fun generateSudokuGridHelper(row: Int, col: Int): Boolean {
        if (row == 9) return true

        val nextRow = if (col == 8) row + 1 else row
        val nextCol = if (col == 8) 0 else col + 1

        val numbers = (1..9).shuffled()

        for (num in numbers) {
            if (isValidMove(row, col, num)) {
                sudokuGrid[row][col] = num
                if (generateSudokuGridHelper(nextRow, nextCol)) return true
                sudokuGrid[row][col] = 0
            }
        }
        return false
    }

    private fun isValidMove(row: Int, col: Int, num: Int): Boolean {

        for (i in 0 until 9) {
            if (sudokuGrid[row][i] == num || sudokuGrid[i][col] == num) {
                return false
            }
        }
        val startRow = row - row % 3
        val startCol = col - col % 3
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (sudokuGrid[startRow + i][startCol + j] == num) {
                    return false
                }
            }
        }
        return true
    }

    private fun hideCells(difficultyLevel: String?) {
        val numToHide = when (difficultyLevel) {
            "Easy" -> 42
            "Medium" -> 48
            "Hard" -> 61
            else -> 40
        }

        repeat(numToHide) {
            var row: Int
            var col: Int
            do {
                row = (0..8).random()
                col = (0..8).random()
            } while (sudokuGrid[row][col] == 0)
            sudokuGrid[row][col] = 0
            hiddenCells.add(Pair(row, col))
        }
        Log.d("SudokuGrid", "Hidden Cells: $hiddenCells")
    }

    @SuppressLint("DiscouragedApi", "ResourceAsColor")
    private fun showCustomKeyboard() {
        val clearButton = findViewById<Button>(R.id.delete)
        for (i in 1..9) {
            val keyboardButton = findViewById<Button>(resources.getIdentifier("button_$i", "id", packageName))
            keyboardButton.setOnClickListener {
                if (selectedButton != null) {
                    if (selectedButton!!.text.isNotEmpty()) {
                        selectedButton!!.text = ""
                        val row = selectedButton!!.tag.toString().toInt() / 9
                        val col = selectedButton!!.tag.toString().toInt() % 9
                        sudokuGrid[row][col] = 0
                    }
                    selectedButton!!.text = i.toString()
                    val row = selectedButton!!.tag.toString().toInt() / 9
                    val col = selectedButton!!.tag.toString().toInt() % 9
                    val originalNum = originalGrid[row][col]
                    if (i == originalNum) {
                        selectedButton!!.setTextColor(resources.getColor(R.color.green, null))
                    } else {
                        selectedButton!!.setTextColor(Color.RED)
                        incorrectGuessCounter++
                        if (incorrectGuessCounter >= maxIncorrectGuesses) {
                            Toast.makeText(this, "You have made 4 incorrect guesses. The grid will be reloaded.", Toast.LENGTH_SHORT).show()
                            incorrectGuessCounter = 0
                            generateSudokuGrid()
                            hideCells(difficultyLevel)
                            myGrid.removeAllViews()
                            onCreate(null)
                        }
                    }

                    clearButton.setOnClickListener {
                        selectedButton!!.text = ""
                    }
                }
            }
        }
        selectedButton?.requestFocus()
    }

    private fun isSudokuSolvedCorrectly(): Boolean {
        for (i in 0..8) {
            for (j in 0.. 8) {
                return sudokuGrid[i][j] == originalGrid[i][j]
            }
        }
        return true
    }
}
