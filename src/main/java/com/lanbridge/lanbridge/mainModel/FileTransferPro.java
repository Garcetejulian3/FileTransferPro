package com.lanbridge.lanbridge.mainModel;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.*;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class FileTransferPro extends Application {

    private File archivoSeleccionado;
    private TextArea logArea;
    private Label lblStatusServidor;
    private ProgressBar progressBar;

    @Override
    public void start(Stage primaryStage) {
        // codigo para la programacion del icono
        Image icono = new Image(getClass().getResourceAsStream("/iconoTransLAN.png"));
        primaryStage.getIcons().add(icono);

        // --- 1. PANEL LATERAL (SIDEBAR) ---
        VBox sidebar = new VBox(20);
        sidebar.setPrefWidth(200);
        sidebar.getStyleClass().add("sidebar");

        Label lblLogo = new Label("TransfArchPro");
        lblLogo.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox userProfile = new VBox(5, new Label("Local Admin"), new Label("Status: Online"));
        userProfile.setStyle("-fx-text-fill: #808080;");

        sidebar.getChildren().addAll(lblLogo, new Separator(), userProfile);

        // --- 2. CONTENIDO CENTRAL ---
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30));
        mainContent.setStyle("-fx-background-color: #0f0f12;");

        // Card de Estado del Servidor
        VBox serverCard = new VBox(10);
        serverCard.getStyleClass().add("card");
        Label lblServerTitle = new Label("ESTADO DEL SISTEMA");
        lblServerTitle.getStyleClass().add("label-header");
        lblStatusServidor = new Label("Iniciando servidor...");
        lblStatusServidor.setStyle("-fx-text-fill: #a38cf4;");
        serverCard.getChildren().addAll(lblServerTitle, lblStatusServidor);

        // Card de Envío (Cliente)
        VBox formEnvio = new VBox(15);
        formEnvio.getStyleClass().add("card");
        formEnvio.setCursor(javafx.scene.Cursor.HAND);



        TextField txtIp = new TextField();
        txtIp.setPromptText("Ej: 192.168.1.50");

        Button btnSeleccionar = new Button("Seleccionar archivo");
        btnSeleccionar.getStyleClass().add("button-primary");
        btnSeleccionar.setMaxWidth(Double.MAX_VALUE);

        Button btnEnviar = new Button("➤ Enviar archivo");
        btnEnviar.getStyleClass().add("button-primary");
        btnEnviar.setMaxWidth(Double.MAX_VALUE);

        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setVisible(false);

        formEnvio.getChildren().addAll(new Label("Destination IP Address"), txtIp, btnSeleccionar, btnEnviar, progressBar);

        // Sección de Logs
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(250);
        VBox logBox = new VBox(10, new Label("Transmission Details & Logs"), logArea);
        VBox.setVgrow(logBox, Priority.ALWAYS);

        mainContent.getChildren().addAll(serverCard, formEnvio, logBox);

        // --- 3. ENSAMBLAJE FINAL ---
        HBox layoutPrincipal = new HBox(sidebar, mainContent);
        HBox.setHgrow(mainContent, Priority.ALWAYS);

        Scene scene = new Scene(layoutPrincipal, 1000, 700);

        // Carga de CSS (Asegúrate que el archivo existe en resources)
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("No se pudo cargar el archivo CSS. Usando estilos por defecto.");
        }

        primaryStage.setTitle("Archivos Transferencia Pro - LAN Bridge");
        primaryStage.setScene(scene);
        primaryStage.show();

        // --- 4. ACTIVACIÓN DE LÓGICA ---

        // Creamos la alerta
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Conexión LanBridge");
        alert.setHeaderText("¡Casi listo para conectar tu celular!");

// Mensaje amigable y estructurado
        String mensaje = "Para que puedas enviar archivos entre tu PC y tu celular, " +
                "necesitamos que Windows nos dé un permiso especial de red.\n\n" +
                "¿Qué pasará ahora?\n" +
                "1. Al darle a 'Aceptar', Windows te hará una pregunta de seguridad.\n" +
                "2. Debes pulsar en 'SÍ' para habilitar la conexión.\n\n" +
                "¡Es un proceso seguro y solo se hace una vez!";

        alert.setContentText(mensaje);

// Esto asegura que el cuadro se adapte al tamaño del texto y no se corte
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();

        configurarFirewallAuto();
        configurarLogica(btnSeleccionar, btnEnviar, txtIp, primaryStage);
        iniciarServidorBackground();
    }

    private void configurarLogica(Button sel, Button env, TextField ipField, Stage stage) {
        sel.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            archivoSeleccionado = fc.showOpenDialog(stage);
            if(archivoSeleccionado != null) {
                logArea.appendText("[INFO] Seleccionado: " + archivoSeleccionado.getName() + "\n");
            }
        });

        env.setOnAction(e -> {
            if (archivoSeleccionado == null) {
                logArea.appendText("[ADVERTENCIA] Selecciona un archivo primero.\n");
                return;
            }
            if (ipField.getText().isEmpty()) {
                logArea.appendText("[ADVERTENCIA] Ingresa una IP de destino.\n");
                return;
            }
            ejecutarTareaEnvio(ipField.getText());
        });
    }

    private void iniciarServidorBackground() {
        Task<Void> serverTask = new Task<>() {
            @Override protected Void call() throws Exception {
                try (ServerSocket ss = new ServerSocket(8080)) {
                    String ip = InetAddress.getLocalHost().getHostAddress();
                    Platform.runLater(() -> lblStatusServidor.setText("Servidor escuchando en IP: " + ip));

                    while (true) {
                        try (Socket s = ss.accept()) {
                            DataInputStream dis = new DataInputStream(s.getInputStream());
                            String nombre = dis.readUTF();
                            long tamano = dis.readLong();

                            Platform.runLater(() -> logArea.appendText("[RECEPCIÓN] Recibiendo: " + nombre + "\n"));

                            File carpeta = new File("descargas_lan");
                            if (!carpeta.exists()) carpeta.mkdir();

                            File destino = new File(carpeta, nombre);
                            try (FileOutputStream fos = new FileOutputStream(destino)) {
                                byte[] buffer = new byte[8192];
                                int leidos;
                                long totalRecibido = 0;
                                while (totalRecibido < tamano && (leidos = dis.read(buffer)) != -1) {
                                    fos.write(buffer, 0, leidos);
                                    totalRecibido += leidos;
                                }
                            }
                            Platform.runLater(() -> logArea.appendText("[ÉXITO] Guardado en carpeta 'descargas_lan'\n"));
                        }
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> logArea.appendText("[ERROR] Servidor: " + e.getMessage() + "\n"));
                }
                return null;
            }
        };
        Thread t = new Thread(serverTask);
        t.setDaemon(true);
        t.start();
    }

    private void ejecutarTareaEnvio(String ip) {
        progressBar.setVisible(true);
        logArea.appendText("[SISTEMA] Conectando a " + ip + "...\n");

        Task<Void> envioTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try (Socket socket = new Socket(ip, 8080)) {
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    dos.writeUTF(archivoSeleccionado.getName());
                    dos.writeLong(archivoSeleccionado.length());

                    try (FileInputStream fis = new FileInputStream(archivoSeleccionado)) {
                        byte[] buffer = new byte[8192];
                        int leidos;
                        long totalEnviado = 0;
                        long tamanoTotal = archivoSeleccionado.length();

                        while ((leidos = fis.read(buffer)) != -1) {
                            dos.write(buffer, 0, leidos);
                            totalEnviado += leidos;
                            updateProgress(totalEnviado, tamanoTotal);
                        }
                    }
                    dos.flush();
                }
                return null;
            }
        };

        progressBar.progressProperty().bind(envioTask.progressProperty());

        envioTask.setOnSucceeded(e -> {
            logArea.appendText("[ÉXITO] Archivo enviado correctamente.\n");
            progressBar.setVisible(false);
            progressBar.progressProperty().unbind();
        });

        envioTask.setOnFailed(e -> {
            logArea.appendText("[ERROR] No se pudo enviar: " + envioTask.getException().getMessage() + "\n");
            progressBar.setVisible(false);
        });

        new Thread(envioTask).start();
    }

    public void configurarFirewallAuto() {
        try {
            // Comando para abrir el puerto 8080 en el Firewall de Windows
            String comando = "netsh advfirewall firewall add rule name=\"LanBridge_Acceso\" dir=in action=allow protocol=TCP localport=8080";

            // Ejecutar como administrador
            ProcessBuilder pb = new ProcessBuilder("powershell", "-Command", "Start-Process cmd -ArgumentList '/c " + comando + "' -Verb RunAs");
            pb.start();
        } catch (Exception e) {
            System.out.println("No se pudo automatizar el firewall: " + e.getMessage());
        }
    }

    public static void main(String[] args) { launch(args); }
}