package org.kirya343.main.services.components;

import org.kirya343.main.model.User;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class AvatarService {

    public String resolveAvatarPath(User user) {

        // Обычная логика для других страниц
        if (user.getAvatarType() == null || user.getAvatarType().equals("uploaded")) {
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                Path filePath = Paths.get("uploads", user.getAvatarUrl()).toAbsolutePath();
                if (filePath.toFile().exists()) {
                    return "/" + user.getAvatarUrl();
                }
            }
        }

        if ((user.getAvatarType() == null || user.getAvatarType().equals("google"))
                && user.getPicture() != null) {
            return user.getPicture();
        }

        return "/images/avatar-placeholder.png";
    }
}