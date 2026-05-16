package server;

import common.network.Request;
import common.network.Response;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static final int RECEIVE_THREADS = 2;
    private final int port;
    private final NetworkProvider network;
    private final RequestHandler requestHandler;
    private final ExecutorService receivePool;
    private final CollectionManager collectionManager;
    private final CommandExecutor executor;
    private volatile boolean running = true;

    public Server(int port, String dbUser, String dbPassword) throws SocketException {
        this.port = port;
        DatabaseManager dbManager = new DatabaseManager(dbUser, dbPassword);
        AuthManager authManager = new AuthManager(dbManager);
        this.collectionManager = new CollectionManager(dbManager);
        this.network = new NetworkProvider(port);
        this.executor = new CommandExecutor(collectionManager, dbManager, AuthManager());
        this.requestHandler = new RequestHandler(executor, authManager);
        this.receivePool = Executors.newFixedThreadPool(RECEIVE_THREADS);
        collectionManager.loadFromDB();
        logger.info("Сервер инициализирован на порту" + port);
    }

    public void start() {
        logger.info("Сервер запущен...");

        byte[] buffer = new byte[65507];
        while (running) {
            try {
                DatagramPacket packet = network.receive();
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();
                byte[] data = network.getData(packet);
                receivePool.submit(()-> {
                    try {
                        Request request = network.deserialRequest(data);
                        logger.info("Запрос " + request.getCommandName() + "от" + clientAddress);
                        Response response = requestHandler.handle(request, clientAddress, clientPort);
                        new Thread(() ->{
                            try {
                                network.sendResponse(response, clientAddress, clientPort);
                            } catch (Exception e){
                                logger.severe("Ошибка отправки " + e.getMessage());
                            }
                        }).start();
                    } catch (Exception e){
                        logger.severe("Ошибка обработки" + e.getMessage());
                        try {
                            network.sendResponse(new Response(false, "Ошибка " + e.getMessage()), clientAddress, clientPort);
                        } catch (Exception ex){
                            logger.severe("Ошибка отправки неисправности" + ex.getMessage());
                        }
                    }
                });

            } catch (Exception e) {
                if (running) logger.severe("Ошибка цикла: " + e.getMessage());
            }
        }
        stop();
    }

    public void stop() {
        running = false;
        receivePool.shutdown();
        try {
            if (!receivePool.awaitTermination(5, TimeUnit.SECONDS)) {
                receivePool.shutdownNow();
            }
        } catch (InterruptedException e) {
            receivePool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        network.close();
        logger.info("Сервер остановлен.");
    }

    public static void main(String[] args){
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        String dbUser = System.getenv().getOrDefault("DB_USER", "studs");
        String dbPassword = System.getenv().getOrDefault("DB_PASSWORD", "studs");

        try {
            Server server = new Server(port, dbUser, dbPassword);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Завершение работы сервера...");
                server.stop();
            }));
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}