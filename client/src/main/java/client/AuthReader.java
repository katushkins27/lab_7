package client;
import common.auth.Credentials;

//import java.util.Optional;
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

    public static Credentials readLoginOnly(){
        System.out.println("Логин: ");
        String login = scanner.nextLine().trim();
        return new Credentials(login, "");
    }

    public static Credentials readPasswordOnly(String login){
        System.out.println("Пароль: ");
        String password = scanner.nextLine();
        return new Credentials(login, password);
    }

    public static String readChoice(){
        System.out.print("> ");
        return scanner.nextLine().trim().toLowerCase();
    }

    public static String readYesNo(String prompt){
        System.out.print(prompt);
        return scanner.nextLine().trim().toLowerCase();
    }

}
