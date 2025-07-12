package org.workswap.main.controller.secure;

import java.util.Locale;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.workswap.datasource.main.model.*;
import org.workswap.datasource.main.model.ModelsSettings.SearchParamType;
import org.workswap.datasource.main.model.chat.Conversation;
import org.workswap.main.services.*;
import org.workswap.main.services.chat.ChatService;
import org.workswap.main.services.components.AuthService;

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
        User seller = userService.findUser(sellerId.toString(), SearchParamType.ID);

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


