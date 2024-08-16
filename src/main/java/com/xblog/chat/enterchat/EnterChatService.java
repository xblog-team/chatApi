package com.xblog.chat.enterchat;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.xblog.chat.chatroom.ChatRoom;
import com.xblog.chat.user.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnterChatService {
	private final SimpMessagingTemplate messagingTemplate;
	private final UserService userService;

	public void findChatRoom(SimpMessageHeaderAccessor headerAccessor) {
		String nickname = setNickname(headerAccessor);
		String sessionId = headerAccessor.getSessionId();

		ChatRoom chatRoom = userService.getChatRoomMap().get(sessionId);
		if (Objects.isNull(chatRoom)) {
			userService.enterChat(sessionId);
			chatRoom = userService.getChatRoomMap().get(sessionId);
		}

		log.info("enterChat nickname: {}", nickname);
		String roomId = chatRoom.getId();

		messagingTemplate.convertAndSendToUser(sessionId, "/queue/findRoom", roomId);
	}

	public String enterRoom(String roomId, SimpMessageHeaderAccessor headerAccessor) {
		String nickname = (String) headerAccessor.getSessionAttributes().get("nickname");

		return nickname + "님이 " + roomId + " 채팅방에 입장하셨습니다.";
	}

	public String setNickname(SimpMessageHeaderAccessor headerAccessor) {
		Map<String, Object> attributes = headerAccessor.getSessionAttributes();
		String nickname;
		if (Objects.isNull(attributes)) {
			attributes = new HashMap<>();
			headerAccessor.setSessionAttributes(attributes);
		}
		if (attributes.containsKey("userId")) {
			nickname = "nickname";
		} else {
			nickname = userService.generateGuestNickname(headerAccessor.getSessionId());
		}
		attributes.put("nickname", nickname);

		return nickname;
	}

}


