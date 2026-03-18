package com.exam.examportal.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Map;

@Controller
@CrossOrigin("*")
public class SignalingController {

    // 1. WebRTC Signaling: Forwarding WebRTC Offers, Answers, and ICE Candidates
    // The student sends an offer to a specific admin room based on the quiz ID.
    // E.g., /app/webrtc/{quizId} -> /topic/webrtc/{quizId}
    // Expected payload: { "type": "offer" | "answer" | "candidate", "quizId":
    // "123", "userId": "456", "sdp": "...", "candidate": "..." }

    @MessageMapping("/webrtc/signal")
    @SendTo("/topic/webrtc/signal")
    public Map<String, Object> handleSignaling(@Payload Map<String, Object> message) {
        // Broadcast the WebRTC signaling message to everyone subscribed to
        // /topic/webrtc/signal
        // Clients will filter based on payload.quizId and payload.targetUserId
        return message;
    }

    // 2. Live Logs: Forwarding suspicious activity directly to connected admins
    // E.g., Student scores +70 for pulling out a phone -> Broadcasts an alert
    @MessageMapping("/proctoring/alert")
    @SendTo("/topic/proctoring/alerts")
    public Map<String, Object> handleProctoringAlert(@Payload Map<String, Object> alertMessage) {
        // Broadcast alert to admin dashboard (subscribed to /topic/proctoring/alerts)
        return alertMessage;
    }
}
