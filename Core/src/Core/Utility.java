package Core;

import java.util.ArrayList;

public class Utility {
    public static int selector(ArrayList<String> list, String title) {
        int i = 0;
        int opcao;
        String line;

        System.out.println("--------------------");
        System.out.println(title);
        System.out.println("--------------------");

        for (String item : list) {
            System.out.println("[" + i + "] " + item);
            ++i;
        }

        System.out.print("Opcao: ");
        line = System.console().readLine();

        try {
            opcao = Integer.parseInt(line);
        } catch (Exception e) {
            System.out.println("Opcao invalida");
            return selector(list, title);
        }

        if (opcao >= i && opcao < 0) {
            System.out.println("Opcao invalida");
            return selector(list, title);
        }

        return opcao;
    }
}
