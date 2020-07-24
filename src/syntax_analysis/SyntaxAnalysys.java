/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syntax_analysis;

import java.util.ArrayList;
import model.Register;
import model.SymbolTable;
import model.Token;

/**
 *
 * @author lucas
 */
public class SyntaxAnalysys {

    private ArrayList<Token> tokenList;

    int address = 0;

    public ArrayList<Register> tabSimList = new ArrayList<>();
    public ArrayList<Register> allRegisters = new ArrayList<>();
    public ArrayList<SymbolTable> allTables = new ArrayList<>();
    private ArrayList<String> createdVars = new ArrayList<>();
    private ArrayList<String> createdFunctions = new ArrayList<>();
    private ArrayList<String> createdProcedures = new ArrayList<>();
    public boolean isError = false;
    public String errorMessage = "";
    private String headerAssembly = "";
    private String bodyAssembly = "";
    private String dataAssembly = "section .data\n";
    private int nivel;
    private int offsetVariavel;
    private int newoffsetVariavel;
    private int newoffsetVariavelFunction;
    private int stringCounter = 0;
    private int rotForCounter = 0;
    private int rotFimCounter = 0;
    private int rotRepeatCounter = 0;
    private int rotWhileCounter = 0;
    private int rotElseCounter = 0;
    private int rotVerdadeCounter = 0;
    private int rotSaidaCounter = 0;
    private int rotFalsoCounter = 0;
    private int paramCounter = 0;
    private boolean hasIntegerOnData = false;
    private boolean isWrite = false;

    public SyntaxAnalysys(ArrayList<Token> tokenList) {
        this.tokenList = tokenList;

    }
    
    
    public String getAssembly(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.headerAssembly);
        stringBuilder.append(this.bodyAssembly);
        stringBuilder.append(this.dataAssembly);
        return stringBuilder.toString();
    }
    

    //<programa> ::= program id  ; <corpo> .
    public void programa() {
        if (tokenList.isEmpty()) {
            this.isError = true;
            this.errorMessage = "Nao ha nenhum elemento na lista de tokens";

        } else {

            if (tokenList.get(0).getLexema().equals("program")) {
                lex();
                if (tokenList.get(0).getTokenClass().equals("Identificador")) {
                    addTabela(tokenList.get(0).getLexema(), "program", "", "");
                    
                    //ACAO {A01}
                    SymbolTable table = new SymbolTable();
                    table.setDadSymbolTable(null);
                    this.nivel = 0;
                    this.offsetVariavel = 0;
                    table.addRegister(tokenList.get(0).getLexema(), "program", nivel, 0, 0, "main", table);
                    this.allTables.add(table);
                    this.headerAssembly = "global main\n"
                            + "extern printf\n"
                            + "extern putchar\n"
                            + "extern scanf \n"
                            + "section .text\n\n";
                    // FINAL DA ACAO {A01}
                    lex();
                    if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(";")) {
                        lex();
                        corpo();
                        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(".")) {
                            lex();
                            //ACAO {A45}
                            this.dataAssembly += "_@DSP: times " + this.allTables.size() * 4 + " db 0";
                            //FIM {A45}
                        } else {
                            error("program", ". nao encontrando");
                        }

                    } else {
                        error("program", "; nao encontrado");
                    }
                } else {
                    error("program", " Identificador nao reconhecido");
                }

            } else {
                error("program", "palavra reservada 'program' nao encontrada");
            }
        }

    }

    //<corpo> ::= <declara> <rotina>  <bloco> 
    public void corpo() {
        declara();
        rotina();
        //ACAO {A44}
        SymbolTable symbolTable = this.allTables.get(this.nivel);
        ArrayList<Register> registers = new ArrayList<>(symbolTable.getRegistersList());
        Register register = registers.get(0);
        String rotulo = register.getRotulo();
        this.bodyAssembly += rotulo + ":\n";
        this.bodyAssembly += "push ebp\n"
                + "push dword [_@DSP + (" + nivel * 4 + ")]\n"
                + "mov ebp, esp\n";
        this.bodyAssembly += "mov [_@DSP + " + nivel * 4 + "], ebp\n";
        this.bodyAssembly += "sub esp, " + symbolTable.getSpaceNeeded()+ " \n";
        //FIM {A44}
        bloco();

        //ACAO {A46}
        this.bodyAssembly +=  "add esp, " + symbolTable.getSpaceNeeded()+ "\n"
                + "mov esp, ebp\n"
                + "pop dword [_@DSP + " + register.getNivel() * 4 + "]\n"
                + "pop ebp\n"
                + "ret\n";

        //FIM {A46}
    }

    //<bloco> ::= begin <sentencas> end
    public void bloco() {
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("begin")) {
            lex();
            sentencas();

            if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("end")) {
                lex();
            } else {
                error("bloco", "palavra reservada 'end' nao encontrada");
            }

        } else {
            error("bloco", "palavra reservada 'begin' nao encontrada");
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
                error("dvar", ": nao encontrado");
            }

        } else {
            error("dvar", "; nao encontrado");
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

            for (Register tabSim : tabSimList) {
                if (tabSim.getAddress() != "" || tabSim.getCategory() == "Funcao") {
                    tabSim.setType("Inteiro");
                }
            }

            lex();
        } else {
            error("tipo", "palavra reservada integer nao encontrada");
        }

    }

    //<variaveis> ::= id <variaveis1> 
    public void variaveis() {
        if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador")) {
            if (!this.createdVars.contains(tokenList.get(0).getLexema())) {

                this.createdVars.add(tokenList.get(0).getLexema());
                String category = "Variavel";
                addTabela(tokenList.get(0).getLexema(), category, "", "" + address);
                address++;
            }

            //ACAO {A03}
            SymbolTable symbolTable = this.allTables.get(this.nivel);
            boolean hasSymbol = symbolTable.tableHasSymbol(tokenList.get(0).getLexema());

            this.offsetVariavel += -4;

            if (!hasSymbol) {
                symbolTable.addRegister(tokenList.get(0).getLexema(), "Variavel", this.nivel, this.offsetVariavel, 0, "", symbolTable);
            }
            else{
                error("variaveis","Simoblo ja existente");
                return;
            }
            //FIM {A03}
            lex();
            variaveis1();
        } else {
            error("variaveis", "Identificador nao reconhecido");
        }

    }

    //<variaveis1> ::= , <variaveis> | E
    public void variaveis1() {
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(",")) {
            lex();
            variaveis();
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
                if (!this.createdProcedures.contains(tokenList.get(0).getLexema())) {
                    this.createdProcedures.add(tokenList.get(0).getLexema());
                }
                String category = "Procedimento";
                addTabela(tokenList.get(0).getLexema(), category, "", "");

                //INICIO {A04}
                SymbolTable symbolTable = this.allTables.get(this.nivel);
                boolean hasProcedure = symbolTable.tableHasProcedure(symbolTable, tokenList.get(0).getLexema());

                if (!hasProcedure) {
                    this.nivel++;
                    SymbolTable newTable = new SymbolTable();
                    this.allTables.add(newTable);
                    newTable.setDadSymbolTable(symbolTable);
                    this.newoffsetVariavel = 0;
                    symbolTable.addRegister(tokenList.get(0).getLexema(), "Procedimento", this.nivel, 0, 0, "_" + tokenList.get(0).getLexema(), newTable);
                    newTable.addRegister(tokenList.get(0).getLexema(), "Procedimento", this.nivel, 0, 0, "_" + tokenList.get(0).getLexema(), newTable);
                    
                }
                //FIM {A04}
                lex();

                parametros();

                //ACAO {A48}
                 SymbolTable st = this.allTables.get(this.nivel);
                 st.attParamNumber();
                 st.calcOffSet();
                        
                //FIM {A48}
                if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(";")) {
                    lex();
                    corpo();

                    //ACAO {A56}
                    this.nivel--;
                    //FIM {A56}
                    if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(";")) {
                        lex();
                        rotina();

                    } else {
                        error("declaraProcedimento", "; nao reconhecido");
                    }

                } else {
                    error("declaraProcedimento", "; nao reconhecido");
                }
            } else {
                error("declaraProcedimento", "Identificador nao reconhecido");
            }

        } else {
            error("declaraProcedimento", "palavra reservada 'procedure' nao encontrada");
        }

    }

    // <declara_funcao> ::= function id  <parametros>  : <tipo_simples>  ; <corpo>  ; <rotina>
    public void declaraFuncao() {
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("function")) {
            lex();
            if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador")) {
                if (!this.createdFunctions.contains(tokenList.get(0).getLexema())) {
                    this.createdFunctions.add(tokenList.get(0).getLexema());
                    String category = "Funcao";
                    addTabela(tokenList.get(0).getLexema(), category, "", "");
                }

                //INICIO {A05}
                SymbolTable symbolTable = this.allTables.get(this.nivel);
                boolean hasFunction = symbolTable.tableHasFunction(symbolTable, tokenList.get(0).getLexema());

                if (!hasFunction) {
                    this.nivel++;
                    SymbolTable newTable = new SymbolTable();
                    this.allTables.add(newTable);
                    newTable.setDadSymbolTable(symbolTable);
                    this.newoffsetVariavelFunction = 0;
                    symbolTable.addRegister(tokenList.get(0).getLexema(), "Funcao", this.nivel, 0, 0, "_" + tokenList.get(0).getLexema(), newTable);
                    newTable.addRegister(tokenList.get(0).getLexema(), "Funcao", this.nivel, 0, 0, "_" + tokenList.get(0).getLexema(), newTable);

                }
                else{
                    error("declara_funcao","ja tem funçao com esse nome");
                    return;
                }

                //FIM {A05}
                lex();
                parametros();
                //ACAO {A48}
                SymbolTable st = this.allTables.get(this.nivel);
                st.attParamNumber();
                st.calcOffSet();
                //FIM {A48}
                if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(":")) {
                    lex();
                    tipo(); // Tipo_simples nao existe.

                    //INICIO ACAO {A47}
                    
                    //FIM ACAO  {A47}
                    if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(";")) {
                        lex();
                        corpo();

                        //ACAO {A56}
                        this.nivel--;
                        //FIM {A56}

                        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(";")) {
                            lex();
                            rotina();

                        } else {
                            error("declaraFuncao", "; nao reconhecido");
                        }

                    } else {
                        error("declaraFuncao", "; nao reconhecido");
                    }

                } else {
                    error("declaraFuncao", ": nao encontrado");
                }
            } else {
                error("declaraFuncao", "Identificador nao reconhecido");
            }
        } else {
            error("declaraFuncao", "palavra reservada function nao reconhecida");
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
                error("parametros", ") nao encontrado");
            }
        }

    }

    //<lista_parametros> ::= <lista_id> : <tipo> <lista_parametros1>
    public void listaParametros() {
        listaId();
        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(":")) {
            lex();
            tipo();

            //ACAO {A06}
            //FIM {A06}
            listaParametros1();
        } else {
            error("listaParametros", ": nao reconhecido");
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

            //ACAO {A07}
            System.out.println("UHSFOHOFHUO "+this.allTables.get(0) );
            SymbolTable table = this.allTables.get(this.nivel);

            boolean hasSymbol = table.tableHasSymbol(tokenList.get(0).getLexema());

            if (!hasSymbol) {
                table.addRegister(tokenList.get(0).getLexema(), "Parametro", this.nivel, 0, 0, "", table);
                table.addOffsetVariable(-4);
            }
            else{
                error("listaId","registrador ja existente");
                return;
            }

            //FIM {A07}
            lex();
            listaId1();
        } else {
            error("listaId", "Identificador nao reconhecido");
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
            error("sentencas", "; nao reconhecido");
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
            //ACAO {A08}
            SymbolTable st = this.allTables.get(this.nivel);
            Register register = st.tableHasId(st, tokenList.get(0).getLexema());

            if (register != null) {
                if (register.getNivel() != this.nivel) {
                    this.bodyAssembly += "push ebp \n"
                            + "push dword [_@DSP + " + (register.getNivel() * 4) + "] \n"
                            + "mov ebp, esp \n";
                }

                this.bodyAssembly += "mov edx, ebp \n"
                        + "lea eax, [edx + " + register.getOffset() + "] \n"
                        + "push eax \n"
                        + "push _@Integer \n"
                        + "call scanf \n"
                        + "add esp, 8 \n";

                if (register != null) {
                    if (register.getNivel() != this.nivel) {
                        this.bodyAssembly += "mov esp, ebp\n"
                                + "pop dword [_@DSP + " + (register.getNivel() * 4) + "]\n"
                                + "pop ebp\n";
                    }
                }
                
                if(!hasIntegerOnData)
                    this.dataAssembly += "_@Integer: db '%d',0 \n";
                
                this.hasIntegerOnData = true;

            } else {
                error("var_read", "Id nao esta ou nao e variavel nem parametro");
                return;
            }

            //FIM ACAO {A08}
            lex();
            varRead1();
        } else {
            error("varRead", "Identificador nao reconhecido");
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
            //ACAO {A59}
            this.stringCounter++;
            this.dataAssembly += "@message" + this.stringCounter + ": db " + tokenList.get(0).getLexema() + ",";

            if (this.isWrite) {
                this.dataAssembly += "0 \n";
                this.isWrite = false;
            } else {

                this.dataAssembly += "10, 0 \n ";

            }

            this.bodyAssembly += "push @message" + this.stringCounter + "\n"
                    + "call printf\n"
                    + "add esp, 4\n";
            //FIM {A59}

            lex();
        } else {
            varWrite();
        }
    }

    //<var_write> ::= id <var_write1>
    public void varWrite() {
        if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador")) {
            //INICIO {A09}
            SymbolTable st = this.allTables.get(this.nivel);
            Register register = st.tableHasId(st, tokenList.get(0).getLexema());

            if (register != null) {
                this.stringCounter++;
                this.dataAssembly += "@message" + this.stringCounter + ": db '%d',";

                if (this.isWrite) {
                    this.dataAssembly += "0 \n";
                    this.isWrite = false;
                } else {
                    this.dataAssembly += "10, 0 \n";

                }

                this.bodyAssembly += "mov dword[_@DSP +" + register.getNivel() * 4 + " ], ebp\n"
                        + "push dword[ebp + (" + register.getOffset() + ") ]\n"
                        + "push @message" + this.stringCounter + "\n"
                        + "call printf\n"
                        + "add esp, 8\n";

            } else {
                error("varWrite", "Identificador nao esta na tabela / nao e variavel nem parametro.");
                return;
            }

            //FIM {A09}
            lex();
            varWrite1();
        } else {
            error("varWrite", "Identificador nao reconhecido");
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
                    error("comando", ") nao reconhecido");
                }
            } else {
                error("comando", "( nao reconhecido");
            }
        } //write ( <exp_write> )
        else if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("write")) {
            this.isWrite = true;
            lex();
            if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("(")) {
                lex();
                expWrite();
                if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(")")) {
                    lex();
                } else {
                    error("comando", ") nao reconhecido");
                }
            } else {
                error("comando", "( nao reconhecido");
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
                    error("comando", ") nao reconhecido");
                }
            } else {
                error("comando", "( nao reconhecido");
            }
        } //for id  := <expressao>  to <expressao>  do <bloco>
        else if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("for")) {
            lex();
            if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador")) {

                //INICIO {A57}
                SymbolTable st = this.allTables.get(this.nivel);
                Register register = st.tableHasId(st, tokenList.get(0).getLexema());

                if (register == null) {
                    error("comando", "Variavel nao encontrada");
                    return;
                }

                //FIM {A57}
                lex();
                if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(":=")) {
                    lex();
                    expressao();
                    //INICIO {A11}
                    this.bodyAssembly += "pop eax \n";

                    if (register.getNivel() != this.nivel) {
                        this.bodyAssembly += "push ebp \n"
                                + "push dword [_@DSP + " + (register.getNivel() * 4) + "] \n"
                                + "mov ebp, esp \n"
                                + "mov dword [EBP + " + register.getOffset() + "], eax \n"
                                + "mov esp, ebp\n"
                                + "pop dword [_@DSP + " + (register.getNivel() * 4) + "]\n"
                                + "pop ebp\n";
                    } else {
                        this.bodyAssembly += "mov dword [EBP +" + register.getOffset() + "], eax \n";
                    }

                    this.bodyAssembly += "rotuloFor" + this.rotForCounter + ": \n";

                    //FIM {A11}
                    if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("to")) {
                        lex();
                        expressao();

                        //ACAO {A12}
                        this.bodyAssembly += "pop eax \n";
                        if (register.getNivel() != this.nivel) {
                            this.bodyAssembly += "push ebp \n"
                                    + "push dword [_@DSP + " + (register.getNivel() * 4) + "] \n"
                                    + "mov ebp, esp \n"
                                    + "cmp dword [EBP + " + register.getOffset() + "], eax \n"
                                    + "mov esp, ebp\n"
                                    + "pop dword [_@DSP + " + (register.getNivel() * 4) + "]\n"
                                    + "pop ebp \n"
                                    + "jg rotuloFim" + this.rotFimCounter + " \n";

                        } else {
                            this.bodyAssembly += "cmp dword [EBP + " + register.getOffset() + "], eax \n"
                                    + "jg rotuloFim" + this.rotFimCounter + " \n";
                        }

                        //FIM {A12}
                        if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("do")) {
                            lex();
                            bloco();

                            // INICIO {A13}
                            if (register.getNivel() != this.nivel) {
                                this.bodyAssembly += "push ebp \n"
                                        + "push dword [_@DSP + " + (register.getNivel() * 4) + "] \n"
                                        + "mov ebp, esp \n"
                                        + "add dword [EBP + " + register.getOffset() + "], 1  \n"
                                        + "mov esp, ebp\n"
                                        + "pop dword [_@DSP + " + (register.getNivel() * 4) + "]\n"
                                        + "pop ebp\n";
                            } else {
                                this.bodyAssembly += "add dword [EBP + " + register.getOffset() + "], 1  \n";
                            }

                            this.bodyAssembly += "jmp rotuloFor" + this.rotForCounter++ + " \n";
                            this.bodyAssembly += "rotuloFim" + this.rotFimCounter++ + ": \n";

                            //FIM {A13}
                        } else {
                            error("comando", "palavra reservada 'do' nao reconhecida");
                        }

                    } else {
                        error("comando", "palavra reservada 'to' nao reconhecida");
                    }
                } else {
                    error("comando", "simbolo de atribuicao ':=' nao reconhecido");
                }
            } else {
                error("comando", "Identificador nao reconhecido");
            }
        } //repeat  <sentencas> until <expressao_logica> 
        else if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("repeat")) {

            //ACAO {A14}
            this.bodyAssembly += "rotuloRepeat" + this.rotRepeatCounter + ": \n";

            //FIM {A14}
            lex();
            sentencas();
            if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("until")) {
                lex();
                expressaoLogica();

                //ACAO {A15}
                this.bodyAssembly += "pop eax \n"
                        + "cmp eax, 1 \n"
                        + "jne rotuloRepeat" + this.rotRepeatCounter++ + " \n";

                //FIM {A15}
            } else {
                error("comando", "palavra reservada 'until' nao reconhecida");
            }
        } //while  <expressao_logica>  do <bloco>
        else if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("while")) {

            //ACAO {A16}
            this.bodyAssembly += "rotuloWhile" + this.rotWhileCounter + ": \n";

            //FIM {A16}
            lex();
            expressaoLogica();

            //ACAO {A17}
            this.bodyAssembly += "pop eax \n"
                    + "cmp eax, 1 \n"
                    + "jne rotuloFim" + this.rotFimCounter + "\n";
            //FIM {A17}

            if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("do")) {
                lex();
                bloco();

                //ACAO {A18}
                this.bodyAssembly += "jmp rotuloWhile" + this.rotWhileCounter++ + "\n";
                this.bodyAssembly += "rotuloFim" + this.rotFimCounter++ + ":\n";

                //FIM  {A18}
            } else {
                error("comando", "palavra reservada 'do' nao reconhecido");
            }

        } //if <expressao_logica>  then <bloco>  <pfalsa>
        else if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("if")) {
            lex();
            expressaoLogica();
            boolean hasElse = this.hasElse();
            if(hasElse){
                //ACAO {A19}
                this.bodyAssembly += "pop eax \n"
                        + "cmp eax, 1 \n"
                        + "jne rotuloElse" + this.rotElseCounter + " \n";

                //FIM {A19}                
            }
            
            //Caso nao tenha else, tenho que pular pro rotulo fim quando a comparaçao for falsa.
            else{
                
                this.bodyAssembly += "pop eax \n"
                        + "cmp eax, 1 \n"
                        + "jne rotuloFim" + this.rotFimCounter + " \n";
            }

            if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("then")) {
                lex();
                bloco();

                //ACAO {A20}
                this.bodyAssembly += "jmp rotuloFim" + this.rotFimCounter + "\n";

                //FIM ACAO {A20}
                pfalsa();

                //ACAO {A21}
                this.bodyAssembly += "rotuloFim" + this.rotFimCounter++ + ": \n";

                //FIM ACAO {A21}
            } else {
                error("comando", "palavra reservada 'if' nao reconhecida");
            }

        } //<variavel>  := <expressao>
        else if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador") && createdVars.contains(tokenList.get(0).getLexema())) {
            
            String id  = tokenList.get(0).getLexema();
            
            lex();

            //ACAO {A49}
            SymbolTable st = this.allTables.get(this.nivel);
        
            Register register = st.tableHasId(st, id);
            
            if (register == null) {
                error("comando", "identificador nao encontrado");
                return;
            }
            
            System.out.println("Teste offset" + register);

            //FIM {A49}
            if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(":=")) {
                lex();
                expressao();

                //ACAO {A22}
                this.bodyAssembly += "pop dword [EBP + (" + register.getOffset() + ")] \n";

                //FIM {A22}
            } else {
                error("comando", "simbolo de atribuicao ':=' nao reconhecido");
            }

        }//<funcao>  := <expressao>
        else if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador") && createdFunctions.contains(tokenList.get(0).getLexema())) {

            //ACAO {A58}
            SymbolTable st = this.allTables.get(this.nivel);

            Register register = st.tableHasFunctionOnCurrentNivel(st, tokenList.get(0).getLexema());

            if (register == null) {
                error("comando", "Identificador de funçao nao encontrado no nivel corrente");
                return;
            }

            //FIM {A58}
            lex();

            if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals(":=")) {
                lex();
                expressao();
                //ACAO {A22}

                this.bodyAssembly += "pop dword [EBP + (" + register.getOffset() + ")] \n";

                //FIM {A22}
            } else {
                error("comando", "simbolo de atribuicao ':=' nao reconhecido");
            }

        }//<procedimento>  <argumentos> 
        else if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador") && createdProcedures.contains(tokenList.get(0).getLexema())) {

            //ACAO {A50}
            SymbolTable st = this.allTables.get(this.nivel);
            Register r = st.tableHasProcedureReturningRegister(st, tokenList.get(0).getLexema());

            if (r == null) {
                error("comandos", "Procedimento nao encontrado");
                return;
            }

            //FIM {A50}
            lex();
            argumentos();

            //ACAO {A23}
            if (r.getParamNumbers() == this.paramCounter) {
                this.bodyAssembly += "call " + r.getRotulo() + "\n"
                        + "add esp, " + r.getParamNumbers() * 4 + " \n";
            } else {
                error("comandos", "Numero de argumentos invalido");
            }
            this.paramCounter = 0;

            //FIM {A23}
        } else if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador") && !createdVars.contains(tokenList.get(0).getLexema())
                && !createdFunctions.contains(tokenList.get(0).getLexema()) && !createdProcedures.contains(tokenList.get(0).getLexema())) {
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
                error("argumentos", ") nao reconhecido");
            }
        }

    }

    //<lista_arg> ::= <expressao> <lista_arg1>
    public void listaArg() {
        expressao();

        this.paramCounter++;
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

            //ACAO {A25}
            this.bodyAssembly += "rotuloElse" + rotElseCounter++ + ": \n";

            //FIM {A25}
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

            //ACAO {A26}
            this.bodyAssembly += "cmp dword [ESP + 4], 1 \n"
                    + "je rotVerdade" + this.rotVerdadeCounter + " \n"
                    + "cmp dword [ESP], 1 \n"
                    + "je rotVerdade" + this.rotVerdadeCounter + " \n"
                    + "mov dword [ESP + 4], 0 \n"
                    + "jmp rotSaida" + this.rotSaidaCounter + " \n"
                    + "rotVerdade" + this.rotVerdadeCounter++ + ": \n"
                    + "mov dword [ESP + 4], 1 \n"
                    + "rotSaida" + this.rotSaidaCounter++ + ": \n"
                    + "add esp, 4 \n";

            //FIM {A26}
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

            //ACAO {A27}
            this.bodyAssembly += "cmp dword [ESP + 4], 1 \n"
                    + "jne rotFalso" + this.rotFalsoCounter + " \n"
                    + "cmp dword [ESP], 1 \n"
                    + "je rotVerdade" + this.rotVerdadeCounter + " \n"
                    + "rotFalso" + this.rotFalsoCounter++ + ": \n"
                    + "mov dword [ESP + 4], 0 \n"
                    + "jmp rotSaida" + this.rotSaidaCounter + " \n"
                    + "rotVerdade" + this.rotVerdadeCounter++ + ": \n"
                    + "mov dword [ESP + 4], 1 \n"
                    + "rotSaida" + this.rotSaidaCounter++ + ": \n"
                    + "add esp, 4 \n";

            //FIM {A27}
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
                error("fatorLogico", ") nao reconhecido");
            }
        } else if (!tokenList.isEmpty() && tokenList.get(0).getLexema().equals("not")) {
            lex();
            fatorLogico();

            //ACAO {A28}
            this.bodyAssembly += "cmp dword [ESP], 1 \n"
                    + "jne Falso" + this.rotFalsoCounter + " \n"
                    + "mov dword [ESP], 0 \n"
                    + "jmp Fim" + this.rotFimCounter + " \n"
                    + "Falso" + this.rotFalsoCounter++ + ": \n"
                    + "mov dword [ESP], 1 \n"
                    + "Fim" + this.rotFimCounter++ + ": \n";

            //FIM {A28}
        } else if (!tokenList.isEmpty() && (tokenList.get(0).getLexema().equals("true") || tokenList.get(0).getLexema().equals("false"))) {

            if (tokenList.get(0).getLexema().equals("true")) {
                //ACAO {A29}

                this.bodyAssembly += "push 1 \n ";

                //FIM {A29}
            } else {
                //ACAO {A30}
                this.bodyAssembly += "push 0 \n";
                //FIM {A30}

            }

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
            //ACAO {A31}
            this.bodyAssembly += "pop eax\n"
                    + "cmp dword [ESP], eax \n"
                    + "jne Falso" + this.rotFalsoCounter + " \n"
                    + "mov dword [ESP], 1\n"
                    + "jmp Fim" + this.rotFimCounter + " \n"
                    + "Falso" + this.rotFalsoCounter++ + ": \n"
                    + "mov dword [ESP], 0 \n"
                    + "Fim" + this.rotFimCounter++ + ": \n";
            //FIM {A31}

        } else if (!tokenList.isEmpty() && (tokenList.get(0).getLexema().equals(">=") || tokenList.get(0).getLexema().equals(">") || tokenList.get(0).getLexema().equals("<") || tokenList.get(0).getLexema().equals("<>") || tokenList.get(0).getLexema().equals("<="))) {
            String actualToken = tokenList.get(0).getLexema();
            lex();
            expressao();

            if (actualToken.equals(">")) {
                //ACAO {A32}
                this.bodyAssembly += "pop eax\n"
                        + "cmp dword [ESP], eax \n"
                        + "jng Falso" + this.rotFalsoCounter + " \n"
                        + "mov dword [ESP], 1\n"
                        + "jmp Fim" + this.rotFimCounter + " \n"
                        + "Falso" + this.rotFalsoCounter++ + ": \n"
                        + "mov dword [ESP], 0 \n"
                        + "Fim" + this.rotFimCounter++ + ": \n";
                //FIM {A32}
            } else if (actualToken.equals(">=")) {
                //ACAO {A33}
                this.bodyAssembly += "pop eax\n"
                        + "cmp dword [ESP], eax \n"
                        + "jnge Falso" + this.rotFalsoCounter + " \n"
                        + "mov dword [ESP], 1\n"
                        + "jmp Fim" + this.rotFimCounter + " \n"
                        + "Falso" + this.rotFalsoCounter++ + ": \n"
                        + "mov dword [ESP], 0 \n"
                        + "Fim" + this.rotFimCounter++ + ": \n";
                //FIM {A33}
            } else if (actualToken.equals("<")) {
                //ACAO {A34}

                this.bodyAssembly += "pop eax\n"
                        + "cmp dword [ESP], eax \n"
                        + "jnl Falso" + this.rotFalsoCounter + " \n"
                        + "mov dword [ESP], 1\n"
                        + "jmp Fim" + this.rotFimCounter + " \n"
                        + "Falso" + this.rotFalsoCounter++ + ": \n"
                        + "mov dword [ESP], 0 \n"
                        + "Fim" + this.rotFimCounter++ + ": \n";

                //FIM {A34}
            } else if (actualToken.equals("<=")) {

                //ACAO {A35}
                this.bodyAssembly += "pop eax\n"
                        + "cmp dword [ESP], eax \n"
                        + "jnle Falso" + this.rotFalsoCounter + " \n"
                        + "mov dword [ESP], 1\n"
                        + "jmp Fim" + this.rotFimCounter + " \n"
                        + "Falso" + this.rotFalsoCounter++ + ": \n"
                        + "mov dword [ESP], 0 \n"
                        + "Fim" + this.rotFimCounter++ + ": \n";

                //FIM {A35}
            } else if (actualToken.equals("<>")) {

                //ACAO {A36}
                this.bodyAssembly += "pop eax\n"
                        + "cmp dword [ESP], eax \n"
                        + "je Falso" + this.rotFalsoCounter + " \n"
                        + "mov dword [ESP], 1\n"
                        + "jmp Fim" + this.rotFimCounter + " \n"
                        + "Falso" + this.rotFalsoCounter++ + ": \n"
                        + "mov dword [ESP], 0 \n"
                        + "Fim" + this.rotFimCounter++ + ": \n";

                //FIM {A36}
            }

        } else {
            error("relacional1", "operador relacional " + "'" + tokenList.get(0).getLexema() + "'" + " nao encontrado");
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

            String actualToken = tokenList.get(0).getLexema();

            lex();
            termo();

            if (actualToken.equals("+")) {

                //ACAO {A37}
                this.bodyAssembly += "pop eax \n"
                        + "add dword[ESP], eax \n";

                //FIM {A37}                
            } else if (actualToken.equals("-")) {

                //ACAO {A38}
                this.bodyAssembly += "pop eax \n"
                        + "sub dword[ESP], eax \n";

                //FIM {A38}
            }

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
        if (!tokenList.isEmpty() && (tokenList.get(0).getLexema().equals("*") || tokenList.get(0).getLexema().equals("/"))) {
            String actualToken = tokenList.get(0).getLexema();
            lex();
            fator();

            if (actualToken.equals("*")) {
                //ACAO {A39}

                this.bodyAssembly += "pop eax \n"
                        + "imul eax, dword [ESP] \n"
                        + "mov dword [ESP], eax \n";

                //FIM {A39}
            } else if (actualToken.equals("/")) {
                //ACAO {A40}
                this.bodyAssembly += "pop ecx \n"
                        + "pop eax \n"
                        + "idiv ecx \n"
                        + "push eax \n";

                //FIM {A40}
            }

            maisTermo();
        }

    }

    //<fator> ::= <variavel>  | <funcao>  <argumentos>  | num  | ( <expressao> )
    public void fator() {
        // Lembrar de tirar o (, se o prof n mandar nada
        if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador") && createdVars.contains(tokenList.get(0).getLexema())) {
            String actualToken = tokenList.get(0).getLexema();
            lex();
            
            //ACAO {A55}
            
            SymbolTable st = this.allTables.get(this.nivel);
            Register register = st.tableHasId(st, actualToken);
            this.bodyAssembly += "push dword [EBP + (" + register.getOffset() + ")] \n";
            
            //FIM {A55}
            
        } else if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador") && createdFunctions.contains(tokenList.get(0).getLexema())) {
            String actualToken = tokenList.get(0).getLexema();
            lex();
            
            //ACAO {A60}
            SymbolTable st = this.allTables.get(this.nivel);
            boolean x = st.tableHasFunction(st, actualToken);
            if(x){
                this.bodyAssembly += "sub esp, 4 \n";
            }
            
            //FIM {A60}
            
            argumentos();
            
            //ACAO {A42}
            st = this.allTables.get(this.nivel);
            Register register = st.tableHasFunctionOnCurrentNivel(st, actualToken);
            if(register != null){
                if(register.getParamNumbers() == this.paramCounter){
                    this.bodyAssembly += "call " + register.getRotulo() + "\n"
                        + "add esp, " + register.getParamNumbers()* 4 + " \n";
                }
                else{
                    error("fator","Numero de argumentos invalido");
                }
                
            }else{
                error("fator","Funcao n declarada");
            }
            
            this.paramCounter = 0;
            
            //FIM {A42}
            

        } else if (!tokenList.isEmpty() && tokenList.get(0).getTokenClass().equals("Identificador")) {
            String actualToken = tokenList.get(0).getLexema();
            lex();
            //ACAO {A55}
            SymbolTable st = this.allTables.get(this.nivel);
            Register register = st.tableHasId(st, actualToken);
            this.bodyAssembly += "push dword [EBP + (" + register.getOffset() + ")] \n";
            
            //FIM {A55} 
            
        } else if (!tokenList.isEmpty() && (tokenList.get(0).getTokenClass().equals("Inteiro") || tokenList.get(0).getTokenClass().equals("Real"))) {
            //ACAO {A41}
                
            this.bodyAssembly += "push " + tokenList.get(0).getLexema() + " \n";

            //FIM {A41}
            
            lex();
        } else if (tokenList.get(0).getLexema().equals("(")) {
            lex();
            expressao();
            if (tokenList.get(0).getLexema().equals(")")) {
                lex();
            } else {
                error("fator", ") nao encontrado");
            }
        }

    }

    public void error(String functionWhereErrorOccours, String message) {
        if (!isError) {
            this.isError = true;
            if (tokenList.isEmpty()) {
                this.errorMessage = "Esta faltando simbolo, pilha esta vazia antes do previsto";
                return;
            } else {
                this.errorMessage = "Funcao que o erro ocorreu - " + functionWhereErrorOccours + "\n"
                        + "Messagem : " + message + "\n"
                        + "Linha: " + tokenList.get(0).getLine() + "\n"
                        + "Coluna: " + tokenList.get(0).getColumn();
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
        Token tokenRemoved = tokenList.remove(0);
        /*if(tokenList.isEmpty() && !tokenRemoved.getLexema().equals(".") ){
            error("Lista Vazia","Simbolo final nao e .");
            
        }*/

    }

    public void addTabela(String lexema, String category, String type, String address) {
        Register register = new Register(lexema, category, type, address);
        tabSimList.add(register);

    }

    private boolean hasElse() {

        for (Token token : this.tokenList){
            
            if(token.getLexema().equalsIgnoreCase("else")){
                return true;
            }
            
            if(token.getLexema().equalsIgnoreCase("if")){
                return false;
            }
        }
        return false;
    }

}
