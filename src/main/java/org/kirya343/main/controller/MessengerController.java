package org.kirya343.main.controller;

import org.kirya343.main.model.*;
import org.kirya343.main.repository.MessageRepository;
import org.kirya343.main.services.AvatarService;
import org.kirya343.main.services.ChatService;
import org.kirya343.main.services.ListingService;
import org.kirya343.main.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class MessengerController {
    private static final Logger logger = LoggerFactory.getLogger(MessengerController.class);

    @Autowired
    private ChatService chatService;
    private final AvatarService avatarService;
    @Autowired
    private UserService userService;
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ListingService listingService;

    public MessengerController(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @GetMapping("/secure/messenger")
    public String messengerPage(@RequestParam(required = false) Long conversationId,
                                @AuthenticationPrincipal OAuth2User oauth2User,
                                Model model) {
        if (oauth2User == null) {
            return "redirect:/login";
        }

        User currentUser = userService.findUserFromOAuth2(oauth2User);
        model.addAttribute("user", currentUser);
        model.addAttribute("avatarPath", avatarService.resolveAvatarPath(currentUser));
        model.addAttribute("userName", currentUser.getName() != null ? currentUser.getName() : "Пользователь");

        // Получаем список диалогов с актуальными данными собеседников
        List<Conversation> conversations = chatService.getUserConversations(currentUser);
        List<ConversationDTO> conversationDTOs = conversations.stream()
                .map(conv -> convertToDTO(conv, currentUser))
                .collect(Collectors.toList());

        model.addAttribute("conversations", conversationDTOs);

        // Выбираем активный диалог
        ConversationDTO selectedConversation = selectActiveConversation(conversationId, conversationDTOs);
        model.addAttribute("selectedConversation", selectedConversation);

        if (selectedConversation != null) {
            List<Message> messages = chatService.getMessages(chatService.getConversationById(selectedConversation.getId()));
            messages.forEach(msg -> {
                msg.setIsOwn(msg.isOwn(currentUser)); // Используем существующий метод isOwn()
            });
            model.addAttribute("messages", messages);
        }

        logger.debug("Loaded messenger page. Current user: {}, selected conversation: {}",
                currentUser.getId(),
                selectedConversation != null ? selectedConversation.getId() : "none");

        return "secure/messenger";
    }

    private ConversationDTO convertToDTO(Conversation conversation, User currentUser) {
        User interlocutor = conversation.getOtherParticipant(currentUser);
        // Получаем актуальные данные из базы
        User freshInterlocutor = userService.findById(interlocutor.getId());

        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setInterlocutorName(freshInterlocutor.getName());
        dto.setInterlocutorAvatar(avatarService.resolveAvatarPath(freshInterlocutor));
        dto.setUnreadCount(chatService.getUnreadMessageCount(conversation, currentUser));
        dto.setListing(conversation.getListing());

        if (!conversation.getMessages().isEmpty()) {
            Message lastMessage = conversation.getMessages().get(conversation.getMessages().size() - 1);
            dto.setLastMessagePreview(lastMessage.getText());
            dto.setLastMessageTime(lastMessage.getSentAt().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        return dto;
    }

    private ConversationDTO selectActiveConversation(Long requestedId, List<ConversationDTO> conversations) {
        if (requestedId != null) {
            return conversations.stream()
                    .filter(c -> c.getId().equals(requestedId))
                    .findFirst()
                    .orElseGet(() -> !conversations.isEmpty() ? conversations.get(0) : null);
        }
        return !conversations.isEmpty() ? conversations.get(0) : null;
    }

    @PostMapping("/secure/messenger/send")
    @ResponseBody
    public ResponseEntity<String> sendMessageAjax(
            @RequestParam("conversationId") Long conversationId,
            @RequestParam("text") String text,
            @AuthenticationPrincipal OAuth2User oauth2User) {

        if (oauth2User == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = userService.findUserFromOAuth2(oauth2User);
        Conversation conversation = chatService.getConversationById(conversationId);

        if (conversation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        chatService.sendMessage(conversation, currentUser, text);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/secure/messenger/getMessages")
    public String getMessagesFragment(
            @RequestParam("conversationId") Long conversationId,
            @AuthenticationPrincipal OAuth2User oauth2User,
            Model model) {

        User currentUser = userService.findUserFromOAuth2(oauth2User);

        // Проверка доступа к чату
        if (!chatService.hasAccessToConversation(currentUser.getEmail(), conversationId)) {
            throw new AccessDeniedException("No access to this conversation");
        }

        // Загружаем сообщения (без пагинации для начала)
        List<Message> messages = chatService.getMessages(chatService.getConversationById(conversationId))
                .stream()
                .peek(message -> {
                    if (message.getSender() == null) {
                        logger.error("Message {} has no sender", message.getId());
                    }
                    message.setIsOwn(message.getSender() != null &&
                            message.getSender().equals(currentUser));
                })
                .collect(Collectors.toList());

        model.addAttribute("messages", messages);
        model.addAttribute("user", currentUser);
        return "secure/messenger :: messages-container";
    }
    @PostMapping("/secure/messenger/markAsRead")
    @ResponseBody
    public ResponseEntity<String> markMessagesAsRead(
            @RequestParam("conversationId") Long conversationId,
            @AuthenticationPrincipal OAuth2User oauth2User) {

        if (oauth2User == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = userService.findUserFromOAuth2(oauth2User);
        chatService.markMessagesAsRead(conversationId, currentUser);

        return ResponseEntity.ok().build();
    }
    @GetMapping("/secure/messenger/getChatInfo")
    @ResponseBody
    public Map<String, String> getChatInfo(
            @RequestParam("conversationId") Long conversationId,
            @AuthenticationPrincipal OAuth2User oauth2User) {

        User currentUser = userService.findUserFromOAuth2(oauth2User);
        Conversation conversation = chatService.getConversationById(conversationId);

        if (conversation == null || !chatService.hasAccessToConversation(currentUser.getEmail(), conversationId)) {
            throw new AccessDeniedException("No access to this conversation");
        }

        User interlocutor = conversation.getOtherParticipant(currentUser);
        User freshInterlocutor = userService.findById(interlocutor.getId());

        return Map.of(
                "interlocutorName", freshInterlocutor.getName() != null ? freshInterlocutor.getName() : "Собеседник",
                "interlocutorAvatar", avatarService.resolveAvatarPath(freshInterlocutor)
        );
    }
    @GetMapping("/secure/messenger/chat")
    public String startNewChat(@RequestParam("sellerId") Long sellerId,
                               @RequestParam("listingId") Long listingId,
                               @AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return "redirect:/login";
        }

        User currentUser = userService.findUserFromOAuth2(oauth2User);
        User seller = userService.findById(sellerId);

        if (seller == null || currentUser == null || currentUser.equals(seller)) {
            return "redirect:/catalog";
        }

        Listing listing = listingService.getListingById(listingId);
        Conversation conversation = chatService.getOrCreateConversation(currentUser, seller, listing);

        return "redirect:/secure/messenger?conversationId=" + conversation.getId();
    }
}

