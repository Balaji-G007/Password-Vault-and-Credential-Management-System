package password_vault_backend.controller;

import password_vault_backend.Util.PasswordGeneratorUtil;
import password_vault_backend.Util.PasswordStrengthChecker;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password-tools")
public class PasswordToolsController {

    @GetMapping("/generate")
    public String generate(
            @RequestParam(defaultValue = "16") int length,
            @RequestParam(defaultValue = "true") boolean upper,
            @RequestParam(defaultValue = "true") boolean digits,
            @RequestParam(defaultValue = "true") boolean symbols) {
        return PasswordGeneratorUtil.generate(length, upper, digits, symbols);
    }

    @GetMapping("/strength")
    public PasswordStrengthChecker.Strength strength(@RequestParam String password) {
        return PasswordStrengthChecker.check(password);
    }
}