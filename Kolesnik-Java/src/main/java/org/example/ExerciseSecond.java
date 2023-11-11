package org.example;

import java.util.Stack;

public class ExerciseSecond {
    public static void main(String[] args) {
        // Задание, ответ в текстовой форме:
        //Дана скобочная последовательность: [((())()(())]]
        //- Можно ли считать эту последовательность правильной?
        //- Если ответ на предыдущий вопрос “нет”, то что необходимо в ней изменить, чтоб она стала правильной?


        // Ответ нет, так как на каждую открывающую скобку  должна быть закрывающая. Подобная задача была на leetcode
        String s = "[((())()(())]]";
        System.out.println(isValid(s));

    }
    public static boolean isValid(String s) {
        Stack<Character> stack = new Stack<>();
        if (s.length() <= 1) {
            return false;
        }
        char[] ch = s.toCharArray();
        for (char c : ch) {
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else if (!stack.isEmpty()) {
                if ((c == ')' && stack.peek() == '(') ||
                        (c == ']' && stack.peek() == '[') ||
                        (c == '}' && stack.peek() == '{')) {
                    stack.pop();
                } else {
                    return false;
                }
            }
            else {
                return false;
            }
        }
        return stack.isEmpty();
    }
}
