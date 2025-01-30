package com.mud.tipmateapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mud.tipmateapp.component.InputField
import com.mud.tipmateapp.ui.theme.TipMateAppTheme
import com.mud.tipmateapp.utils.calculateTotalPerPerson
import com.mud.tipmateapp.widgets.RoundIconButton

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                MainContent()
            }
        }
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    TipMateAppTheme {
        Surface(color = Color.Transparent) {
            content()
        }
    }
}

@Composable
fun formatToRupiah(amount: Double): String {
    return "Rp " + String.format("%,.2f", amount).replace(',', '.')
}

@Preview
@Composable
fun TopHeader(totalPerPerson: Double = 1.0) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(150.dp)
            .clip(shape = CircleShape.copy(all = CornerSize(12.dp))),
        color = Color(0xFF125EFA)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val totalFormatted = formatToRupiah(totalPerPerson)
            Text(
                text = "Total Per Orang",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = totalFormatted,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Preview
@Composable
fun MainContent() {
    val splitByState = remember { mutableStateOf(1) }
    val totalBillsState = remember { mutableStateOf("") }
    val sliderPositionState = remember { mutableStateOf(0f) }
    val tipAmountState = remember { mutableStateOf(0.0) }
    val totalPerPerson = remember { mutableStateOf(0.0) }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopHeader(totalPerPerson = totalPerPerson.value)
        Spacer(modifier = Modifier.height(16.dp))
        BillForm(
            totalBillsState = totalBillsState,
            splitByState = splitByState,
            sliderPositionState = sliderPositionState,
            tipAmountState = tipAmountState,
            totalPerPerson = totalPerPerson
        )
    }
}

@Composable
fun BillForm(
    totalBillsState: MutableState<String>,
    splitByState: MutableState<Int>,
    sliderPositionState: MutableState<Float>,
    tipAmountState: MutableState<Double>,
    totalPerPerson: MutableState<Double>,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val validState = totalBillsState.value.trim().isNotEmpty()
    var tipPercentage = (sliderPositionState.value * 100).toInt()
    val range = 1..100

    Surface(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(corner = CornerSize(12.dp)),
        border = BorderStroke(width = 1.dp, color = Color.LightGray),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            InputField(
                valueState = totalBillsState,
                labelId = "Masukkan Bill",
                enabled = true,
                isSingeleLine = true,
                onAction = KeyboardActions {
                    if (!validState) return@KeyboardActions
                    keyboardController?.hide()
                }
            )

            if (validState) {
                Row(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.Start) {
                    Text(text = "Split", modifier = Modifier.align(Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(120.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        RoundIconButton(imageVector = Icons.Default.Remove, onClick = {
                            if (splitByState.value > 1) splitByState.value -= 1
                            totalPerPerson.value = calculateTotalPerPerson(
                                totalBill = totalBillsState.value.toDouble(),
                                splitBy = splitByState.value,
                                tipPercentage = tipPercentage
                            )
                        })
                        Text(
                            text = "${splitByState.value}",
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(horizontal = 10.dp)
                        )
                        RoundIconButton(imageVector = Icons.Default.Add, onClick = {
                            if (splitByState.value < range.last) {
                                splitByState.value += 1
                                totalPerPerson.value = calculateTotalPerPerson(
                                    totalBill = totalBillsState.value.toDouble(),
                                    splitBy = splitByState.value,
                                    tipPercentage = tipPercentage
                                )
                            }
                        })
                    }
                }

                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Tip",
                        modifier = Modifier.align(alignment = Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(200.dp))
                    Text(
                        text = formatToRupiah(tipAmountState.value),
                        modifier = Modifier.align(alignment = Alignment.CenterVertically)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "$tipPercentage %")
                    Spacer(modifier = Modifier.height(14.dp))

                    Slider(
                        value = sliderPositionState.value,
                        onValueChange = { newVal ->
                            sliderPositionState.value = newVal
                            val totalBill = totalBillsState.value.toDoubleOrNull() ?: 0.0
                            tipPercentage = (newVal * 100).toInt()
                            tipAmountState.value = totalBill * (tipPercentage / 100.0)
                            totalPerPerson.value =
                                (totalBill + tipAmountState.value) / splitByState.value
                        },
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                        steps = 4
                    )
                }
            } else {
                Box { }
            }
        }
    }
}
