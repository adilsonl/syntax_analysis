/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syntax_analysis;

import java.util.ArrayList;
import java.util.Arrays;
import model.Token;

/**
 *
 * @author lucas falta add string e os erros
 */
public class LexicalAnalysis {

    String[] palavrasReservadas = new String[]{"program", "begin", "end", "var", "integer", "procedure",
        "function", "read", "write", "writeln", "for", "to", "do", "if",
        "repeat", "until", "while", "then", "else", "or", "and", "not", "true", "false"};
    ArrayList<Token> tokens = new ArrayList<>();
    
    public boolean error =  false;

    public ArrayList<Token> realizeLexicalAnalysis(String input) {

        String[] lines = input.split("\\n");
        int countLine = 0;
        int countColumn = 0;
        int indexAfterId = 0;
        boolean wasLastVerified = true;

        for (String s : lines) {
            for (int i = 0; i < s.length(); i++) {

                if (!(s.charAt(0) == '{')) {

                    if (Character.isLetter(s.charAt(i))) {
                        indexAfterId = this.Identifier(i, s, countLine, countColumn);
                        i = indexAfterId;
                        countColumn++;
                    }

                    if (i < s.length()) {

                        if (s.charAt(i) == '\'') {
                            indexAfterId = this.addString(i, s, countLine, countColumn);
                            i = indexAfterId;
                            countColumn++;
                        }
                        if (i < s.length()) {
                            if (s.charAt(i) == '*' || s.charAt(i) == '/' || s.charAt(i) == '+' || s.charAt(i) == '-' || s.charAt(i) == '=') {
                                this.addOperator(s.charAt(i), countLine, countColumn);
                                countColumn++;
                            }

                            if (s.charAt(i) == '>') {

                                if (i != (s.length() - 1) && s.charAt(i + 1) == '=') {

                                    this.addRelationalOperator(">=", countLine, countColumn);
                                    i++;
                                    countColumn++;

                                } else {

                                    this.addRelationalOperator(">", countLine, countColumn);
                                    countColumn++;
                                }
                            }

                            if (s.charAt(i) == '<') {
                                if (i != (s.length() - 1) && s.charAt(i + 1) == '=') {
                                    this.addRelationalOperator("<=", countLine, countColumn);
                                    i++;
                                    countColumn++;

                                } else if (i != (s.length() - 1) && s.charAt(i + 1) == '>') {
                                    this.addRelationalOperator("<>", countLine, countColumn);
                                    i++;
                                    countColumn++;

                                } else {
                                    this.addRelationalOperator("<", countLine, countColumn);
                                    countColumn++;

                                }

                            }

                            if (s.charAt(i) == ':') {
                                if (i != (s.length() - 1) && s.charAt(i + 1) == '=') {
                                    this.addDifferentChar(":=", countLine, countColumn);
                                    i++;
                                    countColumn++;
                                } else {
                                    this.addDifferentChar(":", countLine, countColumn);
                                    countColumn++;
                                }
                            }

                            if (s.charAt(i) == ';' || s.charAt(i) == '(' || s.charAt(i) == ')' || s.charAt(i) == ',' || s.charAt(i) == '.') {
                                this.addDifferentChar(Character.toString(s.charAt(i)), countLine, countColumn);
                                countColumn++;

                            }

                            if (Character.isDigit(s.charAt(i))) {
                                indexAfterId = Numbers(i, s, countLine, countColumn);
                                i = indexAfterId;
                                countColumn++;
                                wasLastVerified = false;
                                if (!wasLastVerified) {
                                    i--;
                                }
                            }

                        }

                    }
                }

            }
            wasLastVerified = true;
            countLine++;
            countColumn = 0;
        }

        for (Token t : tokens) {
            System.out.println("Lexema: " + t.getLexema() + " -  Classe: " + t.getTokenClass() + "- Line:" + t.getLine() + "- Coluna:" + t.getColumn());

        }
        System.out.println("Tokens Length:" + tokens.size());
        return tokens;
    }

    public int Identifier(int initialIndex, String s, int line, int column) {
        int firstIndex = 0;
        int secondIndex = 0;
        int i = initialIndex;
        boolean result = false;

        firstIndex = i;
        while (i < s.length() && (Character.isDigit(s.charAt(i)) || Character.isLetter(s.charAt(i)))) {
            i++;
        }
        secondIndex = i;

        if (firstIndex == 0 || !Character.isDigit(s.charAt(firstIndex - 1))) {
            Token token = new Token();
            String lexema = s.substring(firstIndex, secondIndex);
            token.setLexema(lexema);
            // Java 8 +
            result = Arrays.stream(palavrasReservadas).anyMatch(lexema::equals);

            if (result) {
                token.setTokenClass("Palavra Reservada");
            } else {
                token.setTokenClass("Identificador");
            }

            token.setColumn(column);
            token.setLine(line);
            tokens.add(token);
        } else {
            this.error = true;
        }

        return i;
    }

    public int Numbers(int initialIndex, String s, int line, int column) {
        int firstIndex = 0;
        int secondIndex = 0;
        int i = initialIndex;
        boolean isValid = false;
        ArrayList<String> isValidAndTokenClass = new ArrayList<>();

        firstIndex = i;
        while (i < s.length() && (Character.isDigit(s.charAt(i)) || s.charAt(i) == '.')) {
            i++;
        }
        secondIndex = i;
        isValidAndTokenClass = this.AutomatonDigits(s.substring(firstIndex, secondIndex));
        isValid = Boolean.parseBoolean(isValidAndTokenClass.get(0));
        if (isValid && (secondIndex == s.length() || !Character.isLetter(s.charAt(secondIndex)))) {
            Token token = new Token();
            String lexema = s.substring(firstIndex, secondIndex);
            token.setTokenClass(isValidAndTokenClass.get(1));
            token.setLexema(lexema);
            token.setColumn(column);
            token.setLine(line);
            tokens.add(token);

        } else {
            this.error = true;
        }

        return i;

    }

    public ArrayList<String> AutomatonDigits(String s) {
        char actualChar;
        boolean error;
        int actualIndex;
        int actualState;
        ArrayList<String> isValidAndTokenClass = new ArrayList<>();

        actualState = 0;
        actualIndex = 0;
        error = false;

        while (actualIndex < s.length() && !error) {
            actualChar = s.charAt(actualIndex);

            switch (actualState) {
                case 0:
                    if (Character.isDigit(actualChar)) {
                        actualState = 1;
                    } else {
                        error = true;
                    }
                    break;
                case 1:
                    if (Character.isDigit(actualChar)) {
                        actualState = 1;
                    } else if (actualChar == '.') {
                        actualState = 2;
                    } else {
                        error = true;
                    }
                    break;
                case 2:
                    if (Character.isDigit(actualChar)) {
                        actualState = 3;
                    } else {
                        error = true;
                    }
                    break;

                case 3:
                    if (Character.isDigit(actualChar)) {
                        actualState = 3;
                    } else {
                        error = true;
                    }
                    break;
            }
            actualIndex++;
            //System.out.println("Char: " + actualChar + " State: " + actualState);

            //System.out.println("Erro: " + error + " State: " + actualState);
        }
        if ((actualState == 1 || actualState == 3) && !error) {
            isValidAndTokenClass.add(0, "TRUE");
            if (actualState == 1) {

                isValidAndTokenClass.add(1, "Inteiro");

            } else {
                isValidAndTokenClass.add(1, "Real");
            }
            return isValidAndTokenClass;
        }
        isValidAndTokenClass.add(0, "FALSE");

        return isValidAndTokenClass;

    }

    private void addOperator(char operator, int line, int column) {
        Token token = new Token();
        String lexema = Character.toString(operator);
        token.setLexema(lexema);
        if (operator == '=') {
            token.setTokenClass("Comparacao");
        } else {
            token.setTokenClass("Operator Matematico");
        }
        token.setLine(line);
        token.setColumn(column);

        tokens.add(token);

    }

    private void addRelationalOperator(String operator, int line, int column) {

        Token token = new Token();
        String lexema = operator;
        token.setLexema(lexema);
        token.setTokenClass("Operator Relacional");
        token.setLine(line);
        token.setColumn(column);

        tokens.add(token);

    }

    private void addDifferentChar(String c, int line, int column) {
        Token token = new Token();
        String lexema = c;
        token.setLexema(lexema);
        if (c.equals(";")) {
            token.setTokenClass("Ponto e Virgula");

        }
        if (c.equals("(")) {
            token.setTokenClass("Abre Parenteses");
        }

        if (c.equals(")")) {
            token.setTokenClass("Fecha Parenteses");
        }
        if (c.equals(":")) {
            token.setTokenClass("Dois pontos");
        }
        if (c.equals(":=")) {
            token.setTokenClass("Atribuicao");
        }
        if (c.equals(",")) {
            token.setTokenClass("Virgula");
        }
        if (c.equals(".")) {
            token.setTokenClass("Ponto final");
        }

        token.setLine(line);
        token.setColumn(column);

        tokens.add(token);

    }

    private int addString(int initialIndex, String s, int line, int column) {
        int firstIndex = 0;
        int secondIndex = 0;
        int i = initialIndex;
        int indexString = 0;
        firstIndex = i;
        i++;
        while ((i < s.length() &&  indexString <= 255) && s.charAt(i) != '\'') {
            i++;
            indexString++;
        }
        if(i == s.length() || indexString > 255){
            this.error = true;
            System.out.println("indexString " + indexString);
        }
        else{
            secondIndex = i + 1;
            Token token = new Token();
            String lexema = s.substring(firstIndex, secondIndex);
            token.setLexema(lexema);
            token.setTokenClass("String");
            token.setLine(line);
            token.setColumn(column);
            tokens.add(token);
       
        }
        //255
        
        
        return i;
    }

}
