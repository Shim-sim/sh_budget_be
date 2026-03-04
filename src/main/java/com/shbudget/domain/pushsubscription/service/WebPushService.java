package com.shbudget.domain.pushsubscription.service;

import com.shbudget.domain.pushsubscription.entity.PushSubscription;
import com.shbudget.domain.pushsubscription.repository.PushSubscriptionRepository;
import com.shbudget.global.config.VapidConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.Security;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebPushService {

    private final VapidConfig vapidConfig;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private PushService pushService;

    @PostConstruct
    public void init() {
        try {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            pushService = new PushService(
                    vapidConfig.getPublicKey(),
                    vapidConfig.getPrivateKey(),
                    vapidConfig.getSubject()
            );
        } catch (Exception e) {
            log.error("Failed to initialize PushService", e);
        }
    }

    @Async
    public void sendPush(PushSubscription subscription, String title, String body) {
        if (pushService == null) {
            log.warn("PushService not initialized, skipping push");
            return;
        }

        try {
            String payload = String.format("{\"title\":\"%s\",\"body\":\"%s\"}",
                    title.replace("\"", "\\\""),
                    body.replace("\"", "\\\""));

            Notification notification = new Notification(
                    subscription.getEndpoint(),
                    subscription.getP256dh(),
                    subscription.getAuth(),
                    payload
            );

            var response = pushService.send(notification);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 410 || statusCode == 404) {
                log.info("Push subscription expired, removing: {}", subscription.getEndpoint());
                pushSubscriptionRepository.deleteById(subscription.getId());
            } else if (statusCode >= 400) {
                log.warn("Push failed with status {}: {}", statusCode, subscription.getEndpoint());
            }
        } catch (Exception e) {
            log.warn("Push send failed for endpoint: {}", subscription.getEndpoint(), e);
        }
    }
}
