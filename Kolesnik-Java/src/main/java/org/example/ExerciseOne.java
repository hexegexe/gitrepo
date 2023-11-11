package org.example;

import java.util.Scanner;

public class ExerciseOne {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        checkNumbers(scanner);


//        checkString(scanner);
        int [] numbers = new int[] {1,3,5,6,10,12};
        printNumbers(numbers);
    }

    static void checkNumbers(Scanner scanner){
        int number = scanner.nextInt();
        if(number == 7) {
            System.out.println("Привет");
        }
    }

    static void checkString(Scanner scanner){
        String name = scanner.nextLine();
        if(name.equalsIgnoreCase("Вячеслав")){
            System.out.println("Привет," + name);
        }else {
            System.out.println("Нет такого имени");
        }
    }

    static void printNumbers(int[] numbers){
        for(int number : numbers){
            if (number % 3 == 0) {
                System.out.print(number + " ");
            }
        }
    }
}
