package server.commands;

import common.auth.Credentials;
import common.network.Response;
import server.AuthManager;
import server.CollectionManager;

public class RegisterCommand implements Command{
    private final AuthManager authManager;
    public RegisterCommand(AuthManager authManager){
        this.authManager = authManager;
    }

    @Override
    public Response execute(CollectionManager collection, String arg, Object extraData, Credentials credentials){
        if (authManager.register(credentials)) {
            return new Response(true, "Регистрация прошла успешно");
        }
        return new Response(false, "Ошибка регистрации. Возможно пользователь уже существует");
    }

    @Override
    public boolean requiresTicket() { return false; }
    @Override
    public String getDescription() { return "регистрация нового пользователя"; }
    @Override
    public String getName() { return "register"; }
}
