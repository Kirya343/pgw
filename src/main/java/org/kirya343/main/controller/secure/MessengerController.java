package org.kirya343.main.controller.secure;

import java.util.Locale;

import org.kirya343.main.model.*;
import org.kirya343.main.model.chat.Conversation;
import org.kirya343.main.services.*;
import org.kirya343.main.services.chat.ChatService;
import org.kirya343.main.services.components.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MessengerController {

    private final ChatService chatService;
    private final AuthService authService;
    private final UserService userService;
    private final ListingService listingService;

    @GetMapping("/secure/messenger")
    public String messengerPage(@RequestParam(required = false) Long conversationId,
                                @AuthenticationPrincipal OAuth2User oauth2User,
                                Model model, 
                                Locale locale) {

        authService.validateAndAddAuthentication(model, oauth2User);

        // Переменная для отображения активной страницы
        model.addAttribute("activePage", "messenger");
        model.addAttribute("locale", locale.toString());

        return "secure/messenger";
    }

    @GetMapping("/secure/messenger/chat")
    public String startNewChat(@RequestParam("sellerId") Long sellerId,
                               @RequestParam(value = "listingId", required = false) Long listingId,
                               @AuthenticationPrincipal OAuth2User oauth2User) {

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
}


