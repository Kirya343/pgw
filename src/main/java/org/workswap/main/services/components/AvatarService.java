package org.workswap.main.services.components;

import org.springframework.stereotype.Service;
import org.workswap.datasource.main.model.User;

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