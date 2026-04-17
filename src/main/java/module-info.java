module com.lanbridge.lanbridge {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.lanbridge.lanbridge to javafx.fxml;
    exports com.lanbridge.lanbridge;
    exports com.lanbridge.lanbridge.mainModel;
}