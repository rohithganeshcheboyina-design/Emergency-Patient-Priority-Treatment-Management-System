package com.hospital.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Non-blocking in-app notification manager for real-time triage alerts.
 */
public class NotificationManager {

    private static Pane notificationContainer;

    public static void setContainer(Pane container) {
        notificationContainer = container;
    }

    public static void showCriticalAlert(String message) {
        showToast("CRITICAL PATIENT ADDED TO EMERGENCY QUEUE", message, true);
    }

    public static void showError(String title, String message) {
        showToast(title, message, true);
    }

    public static void showInfo(String title, String message) {
        showToast(title, message, false);
    }

    private static void showToast(String title, String message, boolean isCritical) {
        Platform.runLater(() -> {
            if (notificationContainer == null) {
                System.out.println("[ALERT] " + title + ": " + message);
                return;
            }

            VBox card = new VBox(4);
            card.getStyleClass().add("toast-notification");
            if (!isCritical) {
                card.setStyle("-fx-background-color: #0284C7; -fx-background-radius: 8px; -fx-padding: 12px 20px;");
            }

            Label lblTitle = new Label(title);
            lblTitle.getStyleClass().add("toast-title");

            Label lblMsg = new Label(message);
            lblMsg.getStyleClass().add("toast-message");

            card.getChildren().addAll(lblTitle, lblMsg);
            card.setMaxWidth(480);

            notificationContainer.getChildren().clear();
            notificationContainer.getChildren().add(card);
            notificationContainer.setVisible(true);

            // Fade in, pause, fade out
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), card);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            PauseTransition pause = new PauseTransition(Duration.seconds(4));

            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), card);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                notificationContainer.getChildren().remove(card);
                if (notificationContainer.getChildren().isEmpty()) {
                    notificationContainer.setVisible(false);
                }
            });

            SequentialTransition seq = new SequentialTransition(fadeIn, pause, fadeOut);
            seq.play();
        });
    }
}
