package server;

import common.auth.Credentials;
import common.network.Request;
import common.network.Response;

import java.net.InetAddress;
import java.util.logging.Logger;

public class RequestHandler {
    private static final Logger logger = Logger.getLogger(RequestHandler.class.getName());
    private final CommandExecutor executor;
    private final AuthManager authManager;

    public RequestHandler(CommandExecutor executor, AuthManager authManager) {
        this.executor = executor;
        this.authManager = authManager;
    }
    public Response handle(Request request, InetAddress clientAdress, int clientPort){
        Credentials credentials = request.getCredentials();
        if (credentials == null || !authManager.authenticate(credentials)){
            logger.warning("Неавторизованный запрос от " + clientAdress);
            return new Response(false, "Пользователь не авторизован. Проверьте логин или пароль :) ");
        }
        return executor.execute(request);
    }
}