package me.nikl.gamebox.utility;

/**
 * @author Niklas Eicker
 *
 */
public class NumberUtil {

    public static String convertHugeNumber(double number){
        return convertHugeNumber(number, true);
    }

    public static String convertHugeNumber(double number, boolean shortNames){
        if(number >= 1000.) {
            String numberStr = String.format("%.0f", number);

            int index = (numberStr.length() - 1) / 3;

            if (index == 0) return numberStr;

            if (index > 1) {
                numberStr = numberStr.substring(0, numberStr.length() - (index - 1) * 3);
            }

            if (index > 1) {
                numberStr = new StringBuilder(numberStr).insert(numberStr.length() - 3, ".").toString();

                char[] back = numberStr.substring(numberStr.length() - 3).toCharArray();
                int numb = 0;
                for (int i = back.length - 1; i > -1; i--) {
                    if (back[i] == '0') {
                        numb++;
                    } else {
                        break;
                    }
                }

                if (numb > 0) {
                    if (numb == 3) {
                        numberStr = numberStr.replace(".000", "");
                    } else if (numb == 2) {
                        numberStr = numberStr.substring(0, numberStr.length() - 2);
                    } else {
                        numberStr = numberStr.substring(0, numberStr.length() - 1);
                    }
                }

            } else {
                numberStr = new StringBuilder(numberStr).insert(numberStr.length() - 3, ",").toString();
            }

            if(index > NAMES.length){
                return "Way too much...";
            }

            if(shortNames) {
                return numberStr + SHORTNAMES[index - 1];
            } else {
                return numberStr + NAMES[index - 1];
            }
        } else {
            String numberStr = String.valueOf(number);
            String[] split = numberStr.split("\\.");
            if (split.length == 1) return numberStr;
            if (split[1].substring(0, 1).equals("0")) return split[0];
            if (split[1].length() > 1) {
                numberStr = split[0] + "." + split[1].substring(0, 1);
            }

            return numberStr;
        }
    }

    private static final String NAMES[] = new String[]{
            "",
            " Million",
            " Billion",
            " Trillion",
            " Quadrillion",
            " Quintillion",
            " Sextillion",
            " Septillion",
            " Octillion",
            " Nonillion",
            " Decillion",
            " Undecillion",
            " Duodecillion",
            " Tredecillion",
            " Quattuordecillion",
            " Quindecillion",
            " Sexdecillion",
            " Septendecillion",
            " Octodecillion",
            " Novemdecillion",
            " Vigintillion",
    };



    private static final String SHORTNAMES[] = new String[]{
            "",
            " M",
            " B",
            " T",
            " Quad",
            " Quin",
            " Sext",
            " Sept",
            " Oct",
            " Non",
            " Dec",
            " Undec",
            " Duodec",
            " Tredec",
            " Quattuordec",
            " Quindec",
            " Sexdec",
            " Septendec",
            " Octodec",
            " Novemdec",
            " Vigint",
    };
}
