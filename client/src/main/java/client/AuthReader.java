package client;
import common.auth.Credentials;

import java.util.Optional;
import java.util.Scanner;
public class AuthReader {
    private static final Scanner scanner = new Scanner(System.in);

    public static Credentials readCredentials(){
        System.out.println("АВТОРИЗАЦИЯ");
        System.out.println("Логин: ");
        String login = scanner.nextLine().trim();
        System.out.print("Пароль: ");
        String password = scanner.nextLine();
        return new Credentials(login,password);
    }
}
