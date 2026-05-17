package client;

import common.network.*;
import java.io.*;
import org.jline.reader.*;
import org.jline.terminal.*;
import common.auth.Credentials;
public class Client implements AutoCloseable {
    private NetworkManager network;
    private RequestBuilder builder;
    private ScriptExecutor scriptExecutor;
    private LineReader reader;
    private boolean running = true;



    public Client (String host, int port, Credentials credentials) throws IOException{
            this.network = new NetworkManager(host, port);
            this.builder = new RequestBuilder(reader, credentials);
            this.scriptExecutor = new ScriptExecutor(builder, network);
            initConsole();


    }
    private void initConsole(){
        try {
            Terminal terminal = TerminalBuilder.builder().system(true).build();
            Completer completer = (reader, line, candidates) -> {
                String buffer = line.line();
                String[] parts = buffer.split("\\s+");
                String prefix = parts[0].toLowerCase();
                String[] commands = {"add", "show", "update", "remove_by_id", "remove_head",
                        "head", "clear", "remove_greater", "remove_all_by_price",
                        "remove_any_by_type", "min_by_venue", "help", "info", "exit", "execute_script"};

                for (String cmd : commands) {
                    if (cmd.startsWith(prefix)) {
                        candidates.add(new Candidate(cmd));
                    }
                }
            };
            reader = LineReaderBuilder.builder().terminal(terminal).completer(completer).build();
        } catch (IOException e) {
            System.out.println("Ошибка инициализации jline");
            reader = null;
        }
    }

    public void start(){
        System.out.println("Клиент запущен.");
        System.out.println("Введите 'help' для списка команд, 'exit' для выхода");

        while (running){
            try{
                String inputLine = reader.readLine("> ");
                if (inputLine == null || inputLine.trim().isEmpty()) continue;

                String[] parts = inputLine.trim().split("\\s+", 2);
                String command = parts[0].toLowerCase();
                String arg = parts.length > 1 ? parts[1] : "";

                if (command.equals("exit")) {
                    running = false; continue;
                }

                if (command.equals("execute_script")) {
                    scriptExecutor.executeScript(arg);
                } else {
                    Request request = builder.buildRequest(command, arg);
                    if (request != null) {
                        Response response = network.sendWithRetry(request);
                        printResponse(response);
                    }
                }
            } catch (UserInterruptException | EndOfFileException e) {
                break;
            }
        }
    }

    private void printResponse(Response response) {
        if (response == null) {
            System.err.println("Ошибка! Сервер не отвечает");
            return;
        }
        if (response.isSuccess()){
            System.out.println(response.getMessage());
            if (response.getData() != null){
                System.out.println(response.getData());
            }
        } else{
            System.err.println("Ошибка: " + response.getMessage());
        }
    }

    public Response sendRequest(Request request) throws IOException{
        return network.sendWithRetry(request);
    }


    @Override
    public void close() {
        try {
            network.close();
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
        }
        System.out.println("Сеанс завершен!");
    }
}