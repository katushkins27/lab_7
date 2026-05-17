package server.commands;
import common.auth.Credentials;
import common.network.Response;
import server.AuthManager;
import server.CollectionManager;
public class AuthCommand implements Command{
    private final AuthManager authManager;

    public AuthCommand(AuthManager authManager){
        this.authManager=authManager;
    }
    @Override
    public Response execute(CollectionManager collection, String arg, Object extraData, Credentials credentials) {
        if (authManager.authenticate(credentials)) {
            return new Response(true, "Авторизация успешна");
        }
        return new Response(false, "Неверный логин или пароль");
    }

    @Override
    public boolean requiresTicket() { return false; }
    @Override
    public String getDescription() { return "авторизация пользователя"; }
    @Override
    public String getName() { return "auth"; }


}
