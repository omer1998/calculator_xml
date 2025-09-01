package com.example.calculator

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.calculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    var currentNumber: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        binding.plusMinusButton.setOnClickListener {
            toggleSign()
        }

        binding.plusButton.setOnClickListener {
            appendOperation(Operation.ADDITION)
        }
        binding.subtractButton.setOnClickListener {
            appendOperation(Operation.SUBTRACTION)
        }
        binding.multiplyButton.setOnClickListener {
            appendOperation(Operation.MULTIPLICATION)
        }
        binding.divideButton.setOnClickListener {
            appendOperation(Operation.DIVISION)
        }

        binding.acButton.setOnClickListener {
            binding.resultText.text = "0"
            binding.previousEquation.text = ""
            currentNumber = ""
        }
        binding.moduloButton.setOnClickListener {
            appendOperation(Operation.MODULO)
        }
        binding.backButton.setOnClickListener {
            val currentEquationText = binding.resultText.text.toString().trim()
            if (currentEquationText.isNotBlank() && currentNumber.isNotBlank()) {
                binding.resultText.text = currentEquationText.dropLast(1).trim()
                currentNumber = currentNumber.trim().dropLast(1).trim()
            } else if (currentEquationText.isNotBlank() && currentNumber.isBlank()) {
                val updatedEquation = currentEquationText.dropLast(1).trim()
                binding.resultText.text = updatedEquation
                currentNumber = updatedEquation
            }
        }

        binding.equalButton.setOnClickListener {
            try {
                currentNumber = ""
                val totalResult = calculateResult(binding.resultText.text.toString())
                binding.previousEquation.text = binding.resultText.text.toString()
                binding.resultText.text = totalResult.toString()
                currentNumber = totalResult.toString()
                Log.d("Number", currentNumber)
            }catch (_: Exception){
                Toast.makeText(this,"Invalid Equation", Toast.LENGTH_SHORT).show()
            }

        }


    }

    private fun appendOperation(operation: Operation) {
        var currentEquationText = binding.resultText.text.toString().trim()
        val isEndDigit = currentEquationText.last().isDigit()
        val isEndOperation = isOperation(currentEquationText.last())
        if (isEndOperation) {
            updateOperation(operation, currentEquationText)
        } else if (currentEquationText.isNotBlank() && isEndDigit) {
            binding.resultText.append(operation.toSymbol())
            currentNumber = ""
        }
    }

    private fun updateOperation(operation: Operation, currentEquationText: String){
        val currentOperation = currentEquationText.last()
        if (currentOperation.toString().trim() == operation.toSymbol().trim()) return
        var updatedEquation = currentEquationText.dropLast(1)
        updatedEquation = updatedEquation.trim() + operation.toSymbol()
        binding.resultText.text = updatedEquation
        currentNumber = ""
    }
    private fun isOperation(char: Char): Boolean {
        return char == '+' || char == '-' || char == 'x' || char == '/' || char == '%'
    }

    fun onNumberClick(view: View) {
        val number = (view as Button).text.toString()
        //if (binding.resultText.text.toString() == "0") binding.resultText.text = ""
        if (number == ".") {
            Log.d("Number", currentNumber)
            Log.d("Number", binding.resultText.text.toString())
            if (currentNumber.contains(".")) return
        }
        if (binding.resultText.text.toString().trim()
                .lowercase() != "nan" && binding.resultText.text.toString()
                .lowercase() != "infinity"
        ) {
            currentNumber += number
            val updatedEquation = binding.resultText.text.toString() + number
            binding.resultText.text = updatedEquation
            Log.d("Number", currentNumber)
        }

    }

    private fun calculateResult(equation: String): Double {
        var postfix = convertToPostFix(equation)
        Log.d("Postfix", postfix.toString())
        val result = evaluatePostFix(postfix)
        return result
    }

    private fun evaluatePostFix(postfixEquation: List<String>): Double {
        var numbersStack = ArrayDeque<Double>()
        for (element in postfixEquation) {
            if (element.toFloatOrNull() != null) {
                numbersStack.addLast(element.toDouble())
            } else {
                val operand2 = numbersStack.removeLast()
                val operand1 = numbersStack.removeLast()
                numbersStack.addLast(performOperation(element, operand1, operand2))
            }
        }
        return numbersStack.removeLast()
    }

    private fun performOperation(operation: String, operand1: Double, operand2: Double): Double {
        return when (operation) {
            "+" -> operand2 + operand1
            "-" -> operand1 - operand2
            "x" -> operand2 * operand1
            "/" -> operand1 / operand2
            "%" -> operand1 % operand2
            else -> 0.0
        }
    }

    private fun convertToPostFix(equation: String): List<String> {
        var operationStack = ArrayDeque<String>()
        var postfix = mutableListOf<String>()
        val tokens = equation.split(" ")
        for (i in tokens) {
            if (i.toFloatOrNull() != null) {
                postfix.add(i)
            } else {
                while (operationStack.isNotEmpty() && getOperationPriority(i) <= getOperationPriority(
                        operationStack.last()
                    )
                ) {
                    postfix.add(operationStack.removeLast())
                }
                operationStack.addLast(i)
            }
        }
        while (operationStack.isNotEmpty()) {
            postfix.add(operationStack.removeLast())
        }
        return postfix
    }

    fun getOperationPriority(operation: String): Int {
        return when (operation.trim()) {
            "+" -> 1
            "-" -> 1
            "*" -> 2
            "/" -> 2
            else -> 0
        }
    }
    
    private fun toggleSign() {
        val currentEquation = binding.resultText.text.toString()
        if (currentEquation.isBlank()) return
        
        // Split the equation into tokens
        val tokens = currentEquation.split(" ")
        if (tokens.isEmpty()) return
        
        // Get the last token which could be a number or operator
        var lastToken = tokens.last()
        
        // If the last character is an operator, we can't toggle sign
        if (isOperation(lastToken.lastOrNull() ?: ' ')) return
        
        // Toggle the sign
        val newLastToken = if (lastToken.startsWith("-")) {
            lastToken.substring(1) // Remove the minus sign
        } else {
            "-" + lastToken // Add a minus sign
        }
        
        // Rebuild the equation with the atoggled sign
        val newEquation = if (tokens.size == 1) {
            newLastToken
        } else {
            tokens.dropLast(1).joinToString(" ") + " " + newLastToken
        }
        
        binding.resultText.text = newEquation
        currentNumber = newLastToken
    }


}

enum class Operation {
    ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION, MODULO
}

fun Operation.toSymbol(): String {
    return when (this) {
        Operation.ADDITION -> " + "
        Operation.SUBTRACTION -> " - "
        Operation.MULTIPLICATION -> " x "
        Operation.DIVISION -> " / "
        Operation.MODULO -> " % "
    }
}