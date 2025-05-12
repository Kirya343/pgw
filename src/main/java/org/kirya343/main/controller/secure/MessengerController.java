package org.kirya343.main.controller.secure;

import org.kirya343.main.model.*;
import org.kirya343.main.model.chat.Conversation;
import org.kirya343.main.model.DTOs.ConversationDTO;
import org.kirya343.main.model.chat.Message;
import org.kirya343.main.model.DTOs.MessageDTO;
import org.kirya343.main.services.*;
import org.kirya343.main.services.chat.ChatService;
import org.kirya343.main.services.components.AdminCheckService;
import org.kirya343.main.services.components.AvatarService;
import org.kirya343.main.services.components.StatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@Controller
public class MessengerController {
    private static final Logger logger = LoggerFactory.getLogger(MessengerController.class);

    private final ChatService chatService;
    private final AvatarService avatarService;
    private final UserService userService;
    private final ListingService listingService;
    private final AdminCheckService adminCheckService;

    public MessengerController(ChatService chatService, AvatarService avatarService, UserService userService, ListingService listingService, AdminCheckService adminCheckService) {
        this.chatService = chatService;
        this.avatarService = avatarService;
        this.userService = userService;
        this.listingService = listingService;
        this.adminCheckService = adminCheckService;
    }

    @GetMapping("/secure/messenger")
    public String messengerPage(@RequestParam(required = false) Long conversationId,
                                @AuthenticationPrincipal OAuth2User oauth2User,
                                Model model) {
        // Проверка на авторизацию
        if (oauth2User == null) {
            logger.warn("User is not authenticated, redirecting to login.");
            return "redirect:/login";  // Редирект должен быть первым
        }

        boolean isAdmin = adminCheckService.isAdmin(oauth2User);
        model.addAttribute("isAdmin", isAdmin);


        // Авторизованный пользователь
        User currentUser = userService.findUserFromOAuth2(oauth2User);
        logger.info("Authenticated user: {}", currentUser.getId());

        // Добавляем данные в модель
        model.addAttribute("user", currentUser);
        model.addAttribute("avatarPath", avatarService.resolveAvatarPath(currentUser));
        model.addAttribute("userName", currentUser.getName() != null ? currentUser.getName() : "Пользователь");

        // Переменная для отображения активной страницы
        model.addAttribute("activePage", "messenger");

        return "secure/messenger";
    }

    @GetMapping("/secure/messenger/chat")
    public String startNewChat(@RequestParam("sellerId") Long sellerId,
                               @RequestParam(value = "listingId", required = false) Long listingId,
                               @AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return "redirect:/login";
        }

        User currentUser = userService.findUserFromOAuth2(oauth2User);
        User seller = userService.findById(sellerId);

        if (seller == null || currentUser == null || currentUser.equals(seller)) {
            return "redirect:/catalog";
        }

        Listing listing = null;
        if (listingId != null) {
            listing = listingService.getListingById(listingId);
        }

        Conversation conversation = chatService.getOrCreateConversation(currentUser, seller, listing);

        return "redirect:/secure/messenger?conversationId=" + conversation.getId();
    }
    @GetMapping("/secure/messenger/{conversationId}")
    @ResponseBody
    public List<MessageDTO> getMessages(@PathVariable Long conversationId, Principal principal) {
        logger.info("Получение сообщений для разговора с ID: {}", conversationId);

        Conversation conversation = chatService.getConversationById(conversationId);
        if (conversation == null) {
            logger.error("Разговор с ID: {} не найден", conversationId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }

        // Получаем текущего пользователя
        User currentUser = userService.findBySub(principal.getName());

        List<Message> messages = chatService.getMessages(conversation);

        return messages.stream()
                .map(msg -> new MessageDTO(
                        msg.getId(),
                        msg.getText(),
                        msg.getSentAt(),
                        msg.getSender().getId(),
                        conversation.getId(),
                        msg.getReceiver().getId(),
                        msg.getSender().equals(currentUser)
                ))
                .toList();
    }
}


