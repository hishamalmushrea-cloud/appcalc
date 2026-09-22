package com.valdio.valdioveliu.recyclerview;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class CalculatorActivity extends BaseActivity {

    private TextView display;
    private String current = "";
    private String operator = "";
    private double first = 0;
    private boolean newInput = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        setTitle(R.string.calculator_title);

        display = findViewById(R.id.display);

        int[] digits = {R.id.b0, R.id.b1, R.id.b2, R.id.b3, R.id.b4,
                R.id.b5, R.id.b6, R.id.b7, R.id.b8, R.id.b9};
        for (int i = 0; i < digits.length; i++) {
            final int d = i;
            findViewById(digits[i]).setOnClickListener(v -> digit(d));
        }

        findViewById(R.id.badd).setOnClickListener(v -> op("+"));
        findViewById(R.id.bsub).setOnClickListener(v -> op("-"));
        findViewById(R.id.bmul).setOnClickListener(v -> op("*"));
        findViewById(R.id.bdiv).setOnClickListener(v -> op("/"));
        findViewById(R.id.beq).setOnClickListener(v -> equals());
        findViewById(R.id.bc).setOnClickListener(v -> clear());
    }

    private void digit(int d) {
        if (newInput) {
            current = String.valueOf(d);
            newInput = false;
        } else {
            if (current.equals("0")) current = "";
            current += d;
        }
        display.setText(current);
    }

    private void op(String o) {
        if (!current.isEmpty()) {
            first = Fmt.parse(current);
        }
        operator = o;
        newInput = true;
    }

    private void equals() {
        if (operator.isEmpty() || current.isEmpty()) return;
        double second = Fmt.parse(current);
        double result = 0;
        switch (operator) {
            case "+":
                result = first + second;
                break;
            case "-":
                result = first - second;
                break;
            case "*":
                result = first * second;
                break;
            case "/":
                if (second == 0) {
                    display.setText("∞");
                    clear();
                    return;
                }
                result = first / second;
                break;
        }
        current = trim(result);
        display.setText(current);
        operator = "";
        newInput = true;
    }

    private void clear() {
        current = "";
        operator = "";
        first = 0;
        newInput = true;
        display.setText("0");
    }

    private String trim(double v) {
        if (v == (long) v) return String.valueOf((long) v);
        return String.valueOf(v);
    }
}
