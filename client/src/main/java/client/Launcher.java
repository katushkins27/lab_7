package client;
import common.auth.Credentials;
import common.network.Request;
import common.network.Response;

import java.io.IOException;

public class Launcher {
    public static void main(String[] args){
        String host = "localhost";
        int port = 8080;
        if (args.length>0) host = args[0];
        if (args.length>1) port = Integer.parseInt(args[1]);

        Credentials credentials = null;
        boolean authenticated = false;

        System.out.println("ДОБРО ПОЖАЛОВАТЬ");
        System.out.println("Для работы с приложением необходимо авторизоваться или зарегистрироваться.\\n");

        while (!authenticated){
            System.out.println("Введите 'login' для входа или 'register' для регистрации");
            System.out.println("Для выхода введите 'exit'");
            String command = AuthReader.readChoice();
            if (command.equals("exit")){
                System.out.println("Сеанс завершен");
                return;
            }

            if (command.equals("login")){
                credentials = handleLogin(host,port);
                if (credentials != null){
                    authenticated = true;
                }
            } else if (command.equals("register")){
                handleRegister(host,port);
            } else {
                System.out.println("Неизвестная команда. Введите 'login' или 'register'");
            }
        }
        try (Client client = new Client(host, port, credentials)) {
            client.start();
        } catch (IOException e) {
            System.err.println("Ошибка подключения к серверу: " + e.getMessage());
        }
    }

    private static Credentials handleLogin(String host, int port){
        Credentials loginCreds = AuthReader.readLoginOnly();
        String login = loginCreds.getLogin();

        try (Client tempClient = new Client(host, port, loginCreds)){
            Request checkRequest = new Request("check_user", "", loginCreds);
            Response checkResponse = tempClient.sendRequest(checkRequest);
            if (!checkResponse.isSuccess()){
                System.out.println("Пользователь '" + login + "' не найден.");
                System.out.print("Зарегистрироваться? (y/n): ");
                String answer = AuthReader.readYesNo("");

                if (answer.equals("y") || answer.equals("yes") || answer.equals("да")){
                    Credentials newCreds = AuthReader.readPasswordOnly(login);
                    Request regRequest = new Request("register", "", newCreds);
                    Response regResponse = tempClient.sendRequest(regRequest);
                    if (regResponse.isSuccess()) {
                        System.out.println("Регистрация успешна! Теперь войдите.");
                    } else {
                        System.out.println("Ошибка: " + regResponse.getMessage());
                    }
                }
                return null;
            }
        } catch (Exception e){
            System.err.println("Ошибка проверки: "+e.getMessage());
            return null;
        }
        while (true){
            Credentials creds = AuthReader.readPasswordOnly(login);
            try (Client tempClient = new Client(host,port,creds)){
                Request authRequest = new Request("auth", "", creds);
                Response authResponse = tempClient.sendRequest(authRequest);

                if (authResponse.isSuccess()) {
                    System.out.println("Авторизация успешна!");
                    return creds;
                } else {
                    System.out.println("Ошибка: " + authResponse.getMessage());
                    System.out.print("Повторить ввод пароля? (y/n) или 'exit' для выхода: ");
                    String choice = AuthReader.readChoice();
                    if (choice.equals("exit")) {
                        return null;
                    }
                    if (!choice.equals("y") && !choice.equals("yes") && !choice.equals("да")) {
                        return null;
                    }
                }
            } catch (Exception e){
                System.err.println("Ошибка: "+e.getMessage());
                return null;
            }
        }
    }

    private static void handleRegister(String host , int port){
        Credentials newCreds = AuthReader.readCredentials();

        try (Client tempClient = new Client(host,port,newCreds)){
            Request regRequest = new Request("register","",  newCreds);
            Response regResponse = tempClient.sendRequest(regRequest);
            if (regResponse.isSuccess()) {
                System.out.println("Регистрация успешна! Теперь войдите.");
            } else {
                System.out.println("Ошибка: " + regResponse.getMessage());
            }
        } catch (Exception e){
            System.err.println("Ошибка: "+e.getMessage());
        }


    }
}
