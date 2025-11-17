package org.example.oop.Utils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;

import java.io.IOException;

/**
 * LoadingOverlay - Quản lý hiệu ứng loading overlay
 *
 * Sử dụng:
 * - LoadingOverlay.show(parentPane, "Đang tải dữ liệu...");
 * - LoadingOverlay.hide(parentPane);
 */
public class LoadingOverlay {

    private static final String LOADING_FXML = "/FXML/Components/LoadingOverlay.fxml";

    /**
     * Hiển thị loading overlay với text mặc định
     */
    public static StackPane show(StackPane parent) {
        return show(parent, "Đang tải...", "Vui lòng đợi trong giây lát");
    }

    /**
     * Hiển thị loading overlay với text tùy chỉnh
     */
    public static StackPane show(StackPane parent, String message) {
        return show(parent, message, "Vui lòng đợi trong giây lát");
    }

    /**
     * Hiển thị loading overlay với text và description tùy chỉnh
     */
    public static StackPane show(StackPane parent, String message, String description) {
        if (parent == null) {
            System.err.println("❌ LoadingOverlay: Parent is null");
            return null;
        }

        try {
            Platform.runLater(() -> {
                try {
                    // Load FXML
                    FXMLLoader loader = new FXMLLoader(LoadingOverlay.class.getResource(LOADING_FXML));
                    StackPane loadingPane = loader.load();

                    // Cập nhật text
                    Label loadingText = (Label) loadingPane.lookup("#loadingText");
                    Label loadingDescription = (Label) loadingPane.lookup("#loadingDescription");

                    if (loadingText != null && message != null) {
                        loadingText.setText(message);
                    }
                    if (loadingDescription != null && description != null) {
                        loadingDescription.setText(description);
                    }

                    // Đặt ID để dễ tìm và xóa sau
                    loadingPane.setId("loadingOverlay");

                    // Thêm vào parent
                    parent.getChildren().add(loadingPane);

                    System.out.println("✅ LoadingOverlay: Shown with message: " + message);
                } catch (IOException e) {
                    System.err.println("❌ LoadingOverlay: Failed to load FXML - " + e.getMessage());

                    // Fallback: Tạo loading overlay đơn giản bằng code
                    StackPane fallbackLoading = createSimpleLoadingOverlay(message, description);
                    parent.getChildren().add(fallbackLoading);
                }
            });

            return parent;
        } catch (Exception e) {
            System.err.println("❌ LoadingOverlay: Error showing overlay - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Ẩn loading overlay
     */
    public static void hide(StackPane parent) {
        if (parent == null) {
            System.err.println("❌ LoadingOverlay: Parent is null");
            return;
        }

        Platform.runLater(() -> {
            try {
                // Tìm và xóa loading overlay
                parent.getChildren().removeIf(node ->
                    "loadingOverlay".equals(node.getId())
                );
                System.out.println("✅ LoadingOverlay: Hidden");
            } catch (Exception e) {
                System.err.println("❌ LoadingOverlay: Error hiding overlay - " + e.getMessage());
            }
        });
    }

    /**
     * Tạo loading overlay đơn giản (fallback)
     */
    private static StackPane createSimpleLoadingOverlay(String message, String description) {
        StackPane overlay = new StackPane();
        overlay.setId("loadingOverlay");
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        // Container
        javafx.scene.layout.VBox container = new javafx.scene.layout.VBox(20);
        container.setAlignment(javafx.geometry.Pos.CENTER);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 20px; " +
                          "-fx-padding: 40px 60px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 30, 0, 0, 10);");
        container.setMaxWidth(300);

        // Progress Indicator
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(80, 80);
        spinner.setStyle("-fx-progress-color: #0EA5E9;");

        // Message Label
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #0369A1;");

        // Description Label
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");

        container.getChildren().addAll(spinner, messageLabel, descLabel);
        overlay.getChildren().add(container);

        return overlay;
    }

    /**
     * Hiển thị loading với delay (tự động ẩn sau X milliseconds)
     */
    public static void showWithDelay(StackPane parent, String message, long delayMillis) {
        show(parent, message);

        new Thread(() -> {
            try {
                Thread.sleep(delayMillis);
                hide(parent);
            } catch (InterruptedException e) {
                System.err.println("❌ LoadingOverlay: Delay interrupted - " + e.getMessage());
            }
        }).start();
    }
}

