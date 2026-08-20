package password_vault_backend.Util;

public class PasswordStrengthChecker {

    public enum Strength { WEAK, MEDIUM, STRONG, VERY_STRONG }

    public static Strength check(String password) {
        int score = 0;
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*()\\-_=+\\[\\]{}?].*")) score++;

        return switch (score) {
            case 0, 1, 2 -> Strength.WEAK;
            case 3, 4 -> Strength.MEDIUM;
            case 5 -> Strength.STRONG;
            default -> Strength.VERY_STRONG;
        };
    }
}