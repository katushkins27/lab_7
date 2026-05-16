package server.commands;

import common.network.Response;
import server.CollectionManager;
import common.auth.Credentials;

public class ShowCommand implements Command {

    @Override
    public Response execute(CollectionManager collection, String arg, Object extraData, Credentials credentials) {
        String res = collection.showAll();
        return new Response(true, res);
    }

    @Override
    public String getDescription() {
        return "Вывод всех элементов коллекции";
    }
    @Override
    public String getName() {
        return "show";
    }
    @Override
    public boolean requiresTicket() { return false; }
}