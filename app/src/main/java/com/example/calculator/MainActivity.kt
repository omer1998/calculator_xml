package com.example.calculator

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
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

        binding.plusButton.setOnClickListener {
            binding.resultText.append(Operation.ADDITION.toSymbol())
            currentNumber = ""
        }
        binding.subtractButton.setOnClickListener {
            binding.resultText.append(Operation.SUBTRACTION.toSymbol())
            currentNumber = ""
        }
        binding.multiplyButton.setOnClickListener {
            binding.resultText.append(Operation.MULTIPLICATION.toSymbol())
            currentNumber = ""
        }
        binding.divideButton.setOnClickListener {
            binding.resultText.append(Operation.DIVISION.toSymbol())
            currentNumber = ""
        }

        binding.acButton.setOnClickListener {
            binding.resultText.text = "0"
            binding.previousEquation.text = ""
        }
        binding.moduloButton.setOnClickListener {
            binding.resultText.append(Operation.MODULO.toSymbol())
            currentNumber = ""
        }
        binding.backButton.setOnClickListener {
            val currentEquationText = binding.resultText.text.toString()
            if(currentEquationText.isNotBlank() && currentNumber.isNotBlank()){
                val updatedEquation = currentEquationText.dropLast(1)
                binding.resultText.text = updatedEquation
                currentNumber = currentNumber.dropLast(1)
            }else if (currentEquationText.isNotBlank() && currentNumber.isBlank()){
                val updatedEquation = currentEquationText.dropLast(1)
                binding.resultText.text = updatedEquation
                currentNumber = updatedEquation
            }
            Log.d("Number", currentNumber)
        }
        binding.equalButton.setOnClickListener {
            currentNumber = ""
            val totalResult = calculateResult(binding.resultText.text.toString())
            binding.previousEquation.text = binding.resultText.text.toString()
            binding.resultText.text = totalResult.toString()
            Log.d("Number", currentNumber)
        }
    }

    fun onNumberClick(view: View) {
        val number = (view as Button).text.toString()
        if (binding.resultText.text.toString() == "0") binding.resultText.text = ""
        if(number == "."){
            Log.d("Number", currentNumber)
            Log.d("Number", binding.resultText.text.toString())
            if(currentNumber.isBlank() && binding.resultText.text.contains(".")) return
            if(currentNumber.contains(".")) return
        }
        currentNumber += number
        binding.resultText.append(number)
        Log.d("Number", currentNumber)

    }

    private fun calculateResult(equation: String): Double {
        var postfix = convertToPostFix(equation)
        Log.d("Postfix", postfix.toString())
        return evaluatePostFix(postfix)
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
        val regex = Regex("(?<=[-+x/%()])|(?=[-+x/%()])")
        var postfix = mutableListOf<String>()

        val tokens = equation.replace("\\s+".toRegex(), "") // remove spaces
            .split(regex)
            .filter { it.isNotBlank() }
        Log.d("Postfix", "tokens ${tokens.toString()}")
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
        while(operationStack.isNotEmpty()){
            postfix.add(operationStack.removeLast())
        }
        return postfix
    }

    fun getOperationPriority(operation: String): Int {
        return when (operation) {
            "+" -> 1
            "-" -> 1
            "*" -> 2
            "/" -> 2
            else -> 0
        }
    }


}

enum class Operation {
    ADDITION,
    SUBTRACTION,
    MULTIPLICATION,
    DIVISION,
    MODULO
}

fun Operation.toSymbol(): String {
    return when (this) {
        Operation.ADDITION -> "+"
        Operation.SUBTRACTION -> "-"
        Operation.MULTIPLICATION -> "x"
        Operation.DIVISION -> "/"
        Operation.MODULO -> "%"
    }
}