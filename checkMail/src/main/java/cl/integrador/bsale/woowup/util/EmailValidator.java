package cl.integrador.bsale.woowup.util;

import java.util.regex.Pattern;

public class EmailValidator {
    // Expresión regular actualizada según RFC 5322
    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    private static final Pattern PATTERN = Pattern.compile(EMAIL_REGEX);

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return PATTERN.matcher(email).matches();
    }
    public static String getDominio (String email) {
         if(email.indexOf("") > -1) {
             return email.split("@")[1];
         }
         return email;
    }
}
