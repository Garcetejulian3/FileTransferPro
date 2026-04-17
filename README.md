# 📦 LAN Bridge - FileTransferPro

Aplicación de escritorio desarrollada en **Java + JavaFX** que permite transferir archivos entre dispositivos dentro de la misma red local (LAN) utilizando **Sockets TCP**.

---

# 🧠 Enfoque de este proyecto

Este proyecto no es solo una app funcional, sino una **guía práctica para entender cómo funciona la comunicación en red en Java**, combinada con una interfaz gráfica real.

Al terminar de entender este proyecto vas a manejar:

- Comunicación cliente-servidor con sockets
- Transferencia de archivos por red
- Manejo de hilos (multithreading)
- JavaFX (interfaz gráfica moderna en Java)
- Flujo completo de datos en una app real

---

# 🏗️ ¿Cómo funciona la aplicación?

Cada instancia del programa cumple dos roles al mismo tiempo:

### 🖥️ Servidor
- Escucha conexiones entrantes en el puerto `8080`
- Recibe archivos y los guarda

### 📤 Cliente
- Se conecta a otra IP
- Envía archivos

📌 Esto permite que cualquier dispositivo pueda enviar y recibir.

---

# 🔁 Flujo completo (explicado paso a paso)

1. Se inicia la aplicación
2. Se construye la interfaz (JavaFX)
3. Se levanta el servidor en segundo plano
4. El usuario:
   - Selecciona un archivo
   - Ingresa la IP destino
   - Presiona enviar
5. Se crea un socket cliente
6. Se envía:
   - Nombre del archivo
   - Tamaño
   - Datos en bytes
7. El servidor:
   - Acepta conexión
   - Lee los datos
   - Reconstruye el archivo
   - Lo guarda en disco

---

# 🧩 Clase principal: `FileTransferPro`

```java
public class FileTransferPro extends Application

¿Por qué extiende Application?

Porque JavaFX funciona con este ciclo de vida:

launch() → inicia la app
start(Stage) → construye la UI
🚀 Método start(Stage primaryStage)

Es el corazón de la aplicación.

¿Qué hace?
Construye toda la interfaz gráfica
Configura eventos (botones)
Inicia el servidor
Prepara la lógica de envío
🎨 Construcción de la interfaz
📌 Layout principal
HBox layoutPrincipal = new HBox(sidebar, mainContent);

👉 Divide la pantalla en:

Sidebar (izquierda)
Contenido principal (derecha)
📌 Componentes importantes
🔹 TextField txtIp

Campo donde el usuario escribe la IP destino

🔹 Button btnSeleccionar

Abre el explorador de archivos

🔹 Button btnEnviar

Inicia el proceso de envío

🔹 TextArea logArea

Muestra logs en tiempo real:

Ejemplo:

[INFO] Seleccionado: archivo.txt
[SISTEMA] Conectando a 192.168.1.5
🔹 ProgressBar progressBar

Muestra el progreso del envío

⚙️ Método: configurarLogica(...)

Este método conecta la UI con la lógica real.

📂 Selección de archivo
FileChooser fc = new FileChooser();
archivoSeleccionado = fc.showOpenDialog(stage);
¿Qué hace?
Abre una ventana del sistema
Permite elegir un archivo
Guarda el archivo en memoria
📤 Evento de envío
env.setOnAction(e -> { ... });
Flujo:
Verifica que haya archivo
Verifica que haya IP
Llama a:
ejecutarTareaEnvio(ip)
🌐 Método: iniciarServidorBackground()

Este método levanta el servidor en segundo plano.

🔁 ¿Por qué usar Task?
Task<Void> serverTask = new Task<>()

Porque JavaFX tiene un problema importante:

👉 Si bloqueás el hilo principal, la UI se congela

Entonces:

El servidor corre en otro hilo
La interfaz sigue funcionando
🔌 Creación del servidor
ServerSocket ss = new ServerSocket(8080);
¿Qué significa?
Se abre el puerto 8080
El programa queda esperando conexiones
🔄 Loop infinito
while (true)

👉 El servidor nunca se detiene

📥 Aceptar conexión
Socket s = ss.accept();

👉 Se bloquea hasta que alguien se conecta

📦 Recepción de datos
DataInputStream dis = new DataInputStream(s.getInputStream());
Luego:
String nombre = dis.readUTF();
long tamano = dis.readLong();

👉 Primero recibe metadata (nombre + tamaño)

💾 Guardado del archivo
FileOutputStream fos = new FileOutputStream(destino);
🔁 Lectura en bloques
while (totalRecibido < tamano)

👉 Esto es clave:

No carga todo el archivo en memoria
Lo procesa en partes (buffer)
📤 Método: ejecutarTareaEnvio(String ip)

Este método implementa el cliente.

🔁 Uso de Task

Se usa nuevamente para no congelar la UI.

🔌 Conexión
Socket socket = new Socket(ip, 8080);

👉 Intenta conectarse al servidor remoto

📤 Envío de información
dos.writeUTF(nombre);
dos.writeLong(tamano);

👉 Esto permite que el receptor sepa:

Cómo llamar al archivo
Cuánto debe leer
📦 Envío del archivo
while ((leidos = fis.read(buffer)) != -1)

👉 Lee el archivo en bloques y los envía

📊 Progreso
updateProgress(totalEnviado, tamanoTotal);

👉 Se conecta automáticamente con la barra de progreso

🔄 Comunicación entre hilos
🔹 Platform.runLater()
Platform.runLater(() -> logArea.appendText(...));
¿Por qué es necesario?

Porque:

👉 JavaFX NO permite actualizar la UI desde otros hilos

Entonces usamos esto para volver al hilo principal

🔥 Método: configurarFirewallAuto()

Ejecuta:

netsh advfirewall firewall add rule ...
¿Qué hace?
Abre el puerto 8080 en Windows
Evita bloqueos de red
⚠️ Problemas comunes
❌ Host unreachable

Significa:

👉 No hay conexión con la IP

Causas:
No están en la misma red
IP incorrecta
Firewall bloqueando
Servidor no activo
🧪 Cómo probar correctamente
Conectar ambos dispositivos al mismo WiFi
Ejecutar la app en ambos
Ver la IP del receptor
Enviar archivo
📁 Archivos recibidos

Se guardan en:

descargas_lan/
🧠 Conceptos importantes que aprendés
🔹 Socket

Conexión directa entre dos dispositivos

🔹 ServerSocket

Servidor que espera conexiones

🔹 Streams

Permiten enviar datos estructurados

🔹 Multithreading

Ejecutar múltiples tareas al mismo tiempo

🔹 Buffer

Permite transferir archivos grandes sin romper memoria

🚀 Posibles mejoras
Transferencia múltiple
Descubrimiento automático de IPs
Encriptación
Interfaz más profesional
Soporte móvil completo
🎓 Conclusión

Este proyecto es una base sólida para entender:

Cómo funcionan las redes en Java
Cómo crear aplicaciones reales
Cómo manejar UI + lógica + red correctamente
👨‍💻 Autor

Julian Garcete
Desarrollador Backend en formación (Java + Spring)
