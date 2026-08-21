package com.valdio.valdioveliu.recyclerview;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Fmt {
    private Fmt() {
    }

    public static String num(double v) {
        DecimalFormat df = new DecimalFormat("#,##0.##", new DecimalFormatSymbols(Locale.US));
        return df.format(v);
    }

    public static String signed(double v) {
        String s = num(v);
        return v > 0 ? "+" + s : s;
    }

    public static String today() {
        return new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(new Date());
    }

    public static double parse(String s) {
        try {
            return Double.parseDouble(s.replace(",", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
