/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syntax_analysis;

import java.util.ArrayList;
import model.TRecTabSim;
import model.Token;

/**
 *
 * @author lucas
 */
public class SyntaxAnalysys {

    private ArrayList<Token> tokenList;

    int address = 0;

    public ArrayList<TRecTabSim> tabSimList = new ArrayList<>();
    private ArrayList<String> createdVars = new ArrayList<>();
    private ArrayList<String> createdFunctions = new ArrayList<>();
    private ArrayList<String> createdProcedures = new ArrayList<>();
    boolean isError  = false;
    String errorMessage = "";
    

    public SyntaxAnalysys(ArrayList<Token> tokenList) {
        this.tokenList = tokenList;

    }

    //<programa> ::= program id  ; <corpo> .
    public void programa() {
        if(tokenList.isEmpty()){
            this.isError = true;
            this.errorMessage = "Nao ha nenhum elemento na lista de tokens";
            
        }else{
               
        if (tokenList.get(0).getLexema().equals("program")) {
            lex();
            if (tokenList.get(0).getTokenClass().equals("Identificador")) {
                addTabela(tokenList.get(0).getLexema(), "program", "", "");
                lex();
                if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(";")  ) {
                    lex();
                    corpo();
                    if (!tokenList.isEmpty() &&  tokenList.get(0).getLexema().equals(".")  ) {
                        lex();
                    } else {
                        error("program",". nao encontrando");
                    }

                } else {
                    error("program","; nao encontrado");
                }
            } else {
                error("program", " Identificador nao reconhecido");
            }

        } else {
            error("program","palavra reservada 'program' nao encontrada");
        }
      }

    }

    //<corpo> ::= <declara> <rotina>  <bloco> 
    public void corpo() {
        declara();
        rotina();
        bloco();

    }

    //<bloco> ::= begin <sentencas> end
    public void bloco() {
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("begin")) {
            lex();
            sentencas();

            if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("end")) {
                lex();
            } else {
                error("bloco","palavra reservada 'end' nao encontrada");
            }

        } else {
            error("bloco","palavra reservada 'begin' nao encontrada");
        }
    }

    //<declara> ::= var <dvar> <declara> | E
    public void declara() {
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("var")) {
            lex();
            dvar();
            declara();
        }
    }

    //<dvar> ::= <variaveis> : <tipo>  ; <dvar1>
    public void dvar() {
        variaveis();
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(":")) {
            lex();
            tipo();

            if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(";")) {
                lex();
                dvar1();
            } else {
                error("dvar",": nao encontrado");
            }

        } else {
            error("dvar","; nao encontrado");
        }

    }

    //<dvar1> ::= <dvar> | E
    public void dvar1() {
        // To test if its empty or a a repetition of dvar function, we need to check if its starts with an ID
        if (tokenList.get(0).getTokenClass().equals("Identificador")) {
            dvar();
        }

    }

    //<tipo> ::= integer
    public void tipo() {
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("integer")) {

            for (TRecTabSim tabSim : tabSimList) {
                if (tabSim.getAddress()!= "") {
                    tabSim.setType("Inteiro");
                }
            }

            lex();
        } else {
            error("tipo","palavra reservada integer nao encontrada");
        }

    }

    //<variaveis> ::= id <variaveis1> 
    public void variaveis() {
        if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador")) {
            if(!this.createdVars.contains(tokenList.get(0).getLexema())){
                
                this.createdVars.add(tokenList.get(0).getLexema());
                String category = "Variavel";
                addTabela(tokenList.get(0).getLexema(), category, "", "" + address);
                address++;
            }
                

            lex();
            variaveis1();
        } else {
            error("variaveis","Identificador nao reconhecido");
        }

    }

    //<variaveis1> ::= ,id <variaveis> | E
    public void variaveis1() {
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(",")) {
            lex();
            if (tokenList.get(0).getTokenClass().equals("Identificador")) {
                if(!this.createdVars.contains(tokenList.get(0).getLexema())){
                    this.createdVars.add(tokenList.get(0).getLexema());
                    String category = "Variavel";
                    addTabela(tokenList.get(0).getLexema(), category, "", "" + address);
                    address++;
                     
                }
                
                lex();
            } else {
                error("variaveis1","Identificador nao reconhecido");
            }
        }

    }

    //<rotina> ::= <declara_procedimento> | <declara_funcao> | E
    public void rotina() {
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("procedure")) {
            declaraProcedimento();

        } else if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("function")) {
            declaraFuncao();
        }

    }

    // <declara_procedimento> ::= procedure id  <parametros>  ; <corpo>  ; <rotina>
    public void declaraProcedimento() {
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("procedure")) {
            lex();
            if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador")) {
                if(!this.createdProcedures.contains(tokenList.get(0).getLexema()))
                    this.createdProcedures.add(tokenList.get(0).getLexema());
                String category = "Procedimento";
                addTabela(tokenList.get(0).getLexema(), category, "", "");
                lex();
                parametros();
                if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(";")) {
                    lex();
                    corpo();
                    if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(";")) {
                        lex();
                        rotina();

                    } else {
                        error("declaraProcedimento","; nao reconhecido");
                    }

                } else {
                    error("declaraProcedimento","; nao reconhecido");
                }
            } else {
                error("declaraProcedimento","Identificador nao reconhecido");
            }

        } else {
            error("declaraProcedimento","palavra reservada 'procedure' nao encontrada");
        }

    }

    // <declara_funcao> ::= function id  <parametros>  : <tipo_simples>  ; <corpo>  ; <rotina>
    public void declaraFuncao() {
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("function")) {
            lex();
            if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador")) {
                if(!this.createdFunctions.contains(tokenList.get(0).getLexema())){
                    this.createdFunctions.add(tokenList.get(0).getLexema());
                    String category = "Funcao";
                    addTabela(tokenList.get(0).getLexema(), category, "", "");
                }
                    
                lex();
                parametros();
                if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(":")) {
                    lex();
                    tipo(); // Tipo_simples nao existe.
                    if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(";")) {
                        lex();
                        corpo();

                        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(";")) {
                            lex();
                            rotina();

                        } else {
                            error("declaraFuncao","; nao reconhecido");
                        }

                    } else {
                        error("declaraFuncao","; nao reconhecido");
                    }

                } else {
                    error("declaraFuncao",": nao encontrado");
                }
            } else {
                error("declaraFuncao","Identificador nao reconhecido");
            }
        } else {
            error("declaraFuncao","palavra reservada function nao reconhecida");
        }

    }

    //<parametros> ::= ( <lista_parametros> ) | E
    public void parametros() {
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("(")) {
            lex();
            listaParametros();
            if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(")")) {
                lex();
            } else {
                error("parametros",") nao encontrado");
            }
        }

    }

    //<lista_parametros> ::= <lista_id> : <tipo> <lista_parametros1>
    public void listaParametros() {
        listaId();
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(":")) {
            lex();
            tipo();
            listaParametros1();
        } else {
            error("listaParametros",": nao reconhecido");
        }
    }

    //<lista_parametros1> ::= ;<lista_parametros>  | E
    public void listaParametros1() {
        if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador")) {
            listaParametros();
        }
    }

    //<lista_id> ::= id  <lista_id1> 
    public void listaId() {
        if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador")) {
            String category = "Parametro";
            addTabela(tokenList.get(0).getLexema(), category, "", "" + this.address);
            this.address++;
            lex();
            listaId1();
        } else {
            error("listaId","Identificador nao reconhecido");
        }
    }

    //<lista_id1> ::=  , <lista_id>| E 
    public void listaId1() {
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(",")) {
            lex();
            listaId();
        }

    }

    //<sentencas> ::= <comando> ; <sentenças1>
    public void sentencas() {
        comando();
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(";")) {
            lex();
            sentencas1();
        } else {
            error("sentencas","; nao reconhecido");
        }
    }

    //<sentencas1> ::= <sentenças> | E
    public void sentencas1() {
        if (!tokenList.isEmpty() && (tokenList.get(0).getLexema().equals("read") || tokenList.get(0).getLexema().equals("write") || tokenList.get(0).getLexema().equals("writeln")
                || tokenList.get(0).getLexema().equals("for") || tokenList.get(0).getLexema().equals("while") || tokenList.get(0).getLexema().equals("if")
                || tokenList.get(0).getLexema().equals("repeat") || tokenList.get(0).getTokenClass().equals("Identificador"))) {
            sentencas();
        }

    }

    //<var_read> ::= id <var_read1>
    public void varRead() {
        if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador")) {
            lex();
            varRead1();
        } else {
            error("varRead","Identificador nao reconhecido");
        }
    }

    //<var_read1> ::= ,  <var_read> | E  
    public void varRead1() {
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(",")) {
            lex();
            varRead();
        }
    }

    //<exp_write> ::= string  | <var_write>
    public void expWrite() {
        if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("String")) {
            lex();
        } else {
            varWrite();
        }
    }

    //<var_write> ::= id <var_write1>
    public void varWrite() {
        if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador")) {
            lex();
            varWrite1();
        } else {
            error("varWrite","Identificador nao reconhecido");
        }

    }

    //<var_write1> ::= ,  <var_write> | E
    public void varWrite1() {
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(",")) {
            lex();
            varWrite();
        }

    }

    /*<comando> ::=
read ( <var_read> ) | write ( <exp_write> ) | writeln ( <exp_write> ) | for id  := <expressao>  to <expressao>  do <bloco>  | repeat  <sentencas> until <expressao_logica>  
    | while  <expressao_logica>  do <bloco>  | if <expressao_logica>  then <bloco>  <pfalsa>  | <variavel>  := <expressao>  
    | <funcao>  := <expressao>  | <procedimento>  <argumentos> 
     */
    public void comando() {
        //read ( <var_read> )

        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("read")) {
            lex();
            if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("(")) {
                lex();
                varRead();
                if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(")")) {
                    lex();
                } else {
                    error("comando",") nao reconhecido");
                }
            } else {
                error("comando","( nao reconhecido");
            }
        } //write ( <exp_write> )
        else if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("write")) {
            lex();
            if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("(")) {
                lex();
                expWrite();
                if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(")")) {
                    lex();
                } else {
                    error("comando",") nao reconhecido");
                }
            } else {
                error("comando","( nao reconhecido");
            }
        } //writeln ( <exp_write> )
        else if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("writeln")) {
            lex();
            if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("(")) {
                lex();
                expWrite();
                if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(")")) {
                    lex();
                } else {
                    error("comando",") nao reconhecido");
                }
            } else {
                error("comando","( nao reconhecido");
            }
        } //for id  := <expressao>  to <expressao>  do <bloco>
        else if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("for")) {
            lex();
            if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador")) {
                lex();
                if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(":=")) {
                    lex();
                    expressao();
                    if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("to")) {
                        lex();
                        expressao();
                        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("do")) {
                            lex();
                            bloco();
                        } else {
                            error("comando","palavra reservada 'do' nao reconhecida");
                        }

                    } else {
                        error("comando","palavra reservada 'to' nao reconhecida");
                    }
                } else {
                    error("comando","simbolo de atribuicao ':=' nao reconhecido");
                }
            } else {
                error("comando","Identificador nao reconhecido");
            }
        } //repeat  <sentencas> until <expressao_logica> 
        else if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("repeat")) {
            lex();
            sentencas();
            if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("until")) {
                lex();
                expressaoLogica();
            } else {
                error("comando","palavra reservada 'until' nao reconhecida");
            }
        } //while  <expressao_logica>  do <bloco>
        else if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("while")) {
            lex();
            expressaoLogica();
            if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("do")) {
                lex();
                bloco();
            } else {
                error("comando","palavra reservada 'do' nao reconhecido");
            }

        } //if <expressao_logica>  then <bloco>  <pfalsa>
        else if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("if")) {
            lex();
            expressaoLogica();
            if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("then")) {
                lex();
                bloco();
                pfalsa();
            } else {
                error("comando","palavra reservada 'if' nao reconhecida");
            }

        } //<variavel>  := <expressao>
        else if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador") && createdVars.contains(tokenList.get(0).getLexema())) {

            variaveis();
            if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(":=")) {
                lex();
                expressao();

            } else {
                error("comando","simbolo de atribuicao ':=' nao reconhecido");
            }

        }//<funcao>  := <expressao>
        else if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador") && createdFunctions.contains(tokenList.get(0).getLexema())) {
            lex();
            if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(":=")) {
                lex();
                expressao();

            } else {
                error("comando","simbolo de atribuicao ':=' nao reconhecido");
            }

        }//<procedimento>  <argumentos> 
        else if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador") && createdProcedures.contains(tokenList.get(0).getLexema())) {
            lex();
            argumentos();
        }
            
        else if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador") && !createdVars.contains(tokenList.get(0).getLexema()) && 
                !createdFunctions.contains(tokenList.get(0).getLexema()) && !createdProcedures.contains(tokenList.get(0).getLexema())){
                error("comando", "Variavel/Funcao/procedimento nao declarado");
            
        }

    }

    //<argumentos> ::= ( <lista_arg> ) | E
    public void argumentos() {
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("(")) {
            lex();
            listaArg();
            if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(")")) {
                lex();
            } else {
                error("argumentos",") nao reconhecido");
            }
        }

    }

    //<lista_arg> ::= <expressao> <lista_arg1>
    public void listaArg() {
        expressao();
        listaArg1();

    }

    // <lista_arg1> ::= , <lista_arg> | E
    public void listaArg1() {
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(",")) {
            lex();
            listaArg();

        }

    }

    //<pfalsa> ::= else  <bloco> | E
    public void pfalsa() {
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("else")) {
            lex();
            bloco();
        }

    }
    //<expressao_logica> ::= <termo_logico> <expressao_logica1>

    public void expressaoLogica() {
        termoLogico();
        expressaoLogica1();

    }

    //<expressao_logica1> ::= or <termo_logico> <expressao_logica1>  | E
    public void expressaoLogica1() {
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("or")) {
            lex();
            termoLogico();
            expressaoLogica1();
        }

    }

    //<termo_logico> ::= <fator_logico> <termo_logico1>
    public void termoLogico() {
        fatorLogico();
        termoLogico1();

    }

    //<termo_logico1> ::= and <fator_logico> <termo_logico1> | E
    public void termoLogico1() {
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("and")) {
            lex();
            fatorLogico();
            termoLogico1();
        }

    }

    //<fator_logico> ::= <relacional> | ( <expressao_logica> ) | not <fator_logico>  | true  | false 
    public void fatorLogico() {
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("(")) {
            lex();
            expressaoLogica();
            if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(")")) {
                lex();
            } else {
                error("fatorLogico",") nao reconhecido");
            }
        } else if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("not")) {
            lex();
            fatorLogico();
        } else if (!tokenList.isEmpty() && (tokenList.get(0).getLexema().equals("true") || tokenList.get(0).getLexema().equals("false"))) {
            lex();

        } else {
            relacional();
        }

    }

    //<relacional> ::= <expressao> <relacional1>
    public void relacional() {
        expressao();
        relacional1();

    }

    //<relacional1> ::=    = <expressao>  | > <relacional2> | < <relacional3>
    public void relacional1() {

        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("=")) {
            lex();
            expressao();

        } else if (!tokenList.isEmpty() && (tokenList.get(0).getLexema().equals(">=") || tokenList.get(0).getLexema().equals(">") || tokenList.get(0).getLexema().equals("<") || tokenList.get(0).getLexema().equals("<>") || tokenList.get(0).getLexema().equals("<="))) {
            lex();
            expressao();
        } else {
            error("relacional1","operador relacional " +"'" + tokenList.get(0).getLexema() + "'" + " nao encontrado" );
        }

    }

    // Nao vou tenho separei entre > e dps = somente >=, entao sera melhor usar tudo no recional 1
    //<relacional2> ::=   <expressao>  | = <expressao>
    /* public void relacional2(){
        
        
        
    }*/
    // Nao vou tenho separei entre <, > e igual  entao sera melhor usar tudo no recional 1
    /* 
    //<relacional3> ::=   <expressao>  | = <expressao> | > <expressao>
    public void relacional3(){
        
    }*/
    //<expressao> ::= <termo> <mais_expressao>
    public void expressao() {
        termo();
        maisExpressao();

    }

    // <mais_expressao> ::= + <termo>  <mais_expressao> | - <termo> <mais_expressao> | E
    public void maisExpressao() {
        if (!tokenList.isEmpty() && (tokenList.get(0).getLexema().equals("-") || tokenList.get(0).getLexema().equals("+"))) {
            lex();
            termo();
            maisExpressao();
        }

    }

    //<termo> ::= <fator> <mais_termo>
    public void termo() {
        fator();
        maisTermo();

    }

    //<mais_termo> ::= * <fator>  <mais_termo> | / <fator>  <mais_termo> | E
    public void maisTermo() {
        if (!tokenList.isEmpty() &&  (tokenList.get(0).getLexema().equals("*") || tokenList.get(0).getLexema().equals("/"))) {
            lex();
            fator();
            maisTermo();
        }

    }

    //<fator> ::= <variavel>  | <funcao>  <argumentos>  | num  | ( <expressao> )
    public void fator() {
        // Lembrar de tirar o (, se o prof n mandar nada
        if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador") && createdVars.contains(tokenList.get(0).getLexema())) {
            variaveis();
        }
        
        else if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador") && createdFunctions.contains(tokenList.get(0).getLexema())) {
            lex();
            argumentos();

        } 
        
        else if (!tokenList.isEmpty() && (tokenList.get(0).getTokenClass().equals("Inteiro") || tokenList.get(0).getTokenClass().equals("Real"))) {
            lex();
        } 
        else if (tokenList.get(0).getLexema().equals("(")) {
            lex();
            expressao();
            if (tokenList.get(0).getLexema().equals(")")) {
                lex();
            } else {
                error("fator",") nao encontrado");
            }
        } 
        
    }

    public void error(String functionWhereErrorOccours, String message) {
        if(!isError){
          this.isError = true;
          if(tokenList.isEmpty()){
              this.errorMessage = "Esta faltando simbolo, pilha esta vazia antes do previsto";
              return;
          }
          else{
              this.errorMessage = "Funcao que o erro ocorreu - " + functionWhereErrorOccours + "\n"
                  + "Messagem : " + message + "\n"+ 
                  "Linha: "  + tokenList.get(0).getLine() + "\n"+
                  "Coluna: " + tokenList.get(0).getColumn();
                   System.out.println("ERRO - " + functionWhereErrorOccours);
                   System.out.println("ERROR MESSAGE - " + message);
                   System.out.println("Lexema Erro:" + tokenList.get(0).getLexema());
                   System.out.println("Lexema Erro Line:" + tokenList.get(0).getLine());
                   System.out.println("Lexema Erro Column:" + tokenList.get(0).getColumn());              
          }

        }

        return;

    }

    public void lex() {
       Token tokenRemoved =  tokenList.remove(0);
        /*if(tokenList.isEmpty() && !tokenRemoved.getLexema().equals(".") ){
            error("Lista Vazia","Simbolo final nao e .");
            
        }*/

    }

    public void addTabela(String lexema, String category, String type, String address) {
        TRecTabSim tabSim = new TRecTabSim(lexema, category, type, address);
        tabSimList.add(tabSim);

    }

}
