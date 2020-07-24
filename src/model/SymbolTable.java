/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author lucas
 */
public class SymbolTable {

    private int memory;
    private ArrayList<Register> registersList;
    private SymbolTable dadSymbolTable;
    private int paramNumber;
    private int offsetVariable;
    private int spaceNeeded;

    public SymbolTable() {
        this.registersList = new ArrayList<>();
        this.spaceNeeded = 4;
        this.offsetVariable = 0;

    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public ArrayList<Register> getRegistersList() {
        return registersList;
    }

    public void setRegistersList(ArrayList<Register> registersList) {
        this.registersList = registersList;
    }

    public SymbolTable getDadSymbolTable() {
        return dadSymbolTable;
    }

    public void setDadSymbolTable(SymbolTable dadSymbolTable) {
        this.dadSymbolTable = dadSymbolTable;
    }

    public int getParamNumber() {
        return paramNumber;
    }

    public void setParamNumber(int paramNumber) {
        this.paramNumber = paramNumber;
    }

    public int getOffsetVariable() {
        return offsetVariable;
    }

    public void setOffsetVariable(int offset) {
        this.offsetVariable = offset;
    }

    public int getSpaceNeeded() {
        return spaceNeeded;
    }

    public void setSpaceNeeded(int spaceNeeded) {
        this.spaceNeeded = spaceNeeded;
    }
    
    public void addOffsetVariable(int value){
        this.offsetVariable += value;
    }
    

    public void addRegister(String lexema, String category, int nivel, int offset, int paramNumber, String rotulo, SymbolTable symbolTable) {

        if (category.equalsIgnoreCase("Variavel")) {
            this.spaceNeeded += 4;
        }

        if (category.equalsIgnoreCase("Parametro")) {
            this.paramNumber++;
        }

        Register register = new Register(lexema, category, nivel, offset, paramNumber, rotulo, symbolTable, "integer", "");

        this.registersList.add(register);

    }

    public boolean tableHasSymbol(String lexema) {

        for (Register r : this.registersList) {
            if (r.getLexema().equalsIgnoreCase(lexema)) {
                return true;
            }
        }

        return false;

    }

    public boolean tableHasProcedure(SymbolTable symbolTable, String lexema) {

        if (symbolTable.dadSymbolTable == null) {
            for (Register r : symbolTable.getRegistersList()) {
                if (r.getLexema().equalsIgnoreCase(lexema) && r.getCategory().equalsIgnoreCase("Procedimento")) {
                    return true;
                }
            }
            return false;
        }

        for (Register r : symbolTable.getRegistersList()) {
            if (r.getLexema().equalsIgnoreCase(lexema) && r.getCategory().equalsIgnoreCase("Procedimento")) {
                return true;
            }
        }

        return tableHasProcedure(symbolTable.getDadSymbolTable(), lexema);

    }

    public Register tableHasProcedureReturningRegister(SymbolTable symbolTable, String lexema) {

        if (symbolTable.dadSymbolTable == null) {
            for (Register r : symbolTable.getRegistersList()) {
                if (r.getLexema().equalsIgnoreCase(lexema) && r.getCategory().equalsIgnoreCase("Procedimento")) {
                    return r;
                }
            }
            return null;
        }

        for (Register r : symbolTable.getRegistersList()) {
            if (r.getLexema().equalsIgnoreCase(lexema) && r.getCategory().equalsIgnoreCase("Procedimento")) {
                return r;
            }
        }

        return tableHasProcedureReturningRegister(symbolTable.getDadSymbolTable(), lexema);

    }

    public boolean tableHasFunction(SymbolTable symbolTable, String lexema) {

        if (symbolTable.dadSymbolTable == null) {
            for (Register r : symbolTable.getRegistersList()) {
                if (r.getLexema().equalsIgnoreCase(lexema) && r.getCategory().equalsIgnoreCase("Funcao")) {
                    return true;
                }
            }
            return false;
        }

        for (Register r : symbolTable.getRegistersList()) {
            if (r.getLexema().equalsIgnoreCase(lexema) && r.getCategory().equalsIgnoreCase("Funcao")) {
                return true;
            }
        }

        return tableHasFunction(symbolTable.getDadSymbolTable(), lexema);

    }
    
      public Register tableHasFunctionReturningRegister(SymbolTable symbolTable, String lexema) {

        if (symbolTable.dadSymbolTable == null) {
            for (Register r : symbolTable.getRegistersList()) {
                if (r.getLexema().equalsIgnoreCase(lexema) && r.getCategory().equalsIgnoreCase("Funcao")) {
                    return r;
                }
            }
            return null;
        }

        for (Register r : symbolTable.getRegistersList()) {
            if (r.getLexema().equalsIgnoreCase(lexema) && r.getCategory().equalsIgnoreCase("Funcao")) {
                return r;
            }
        }

        return tableHasFunctionReturningRegister(symbolTable.getDadSymbolTable(), lexema);

    }

    public Register tableHasFunctionOnCurrentNivel(SymbolTable symbolTable, String lexema) {

        for (Register r : symbolTable.getRegistersList()) {
            if (r.getLexema().equalsIgnoreCase(lexema) && r.getCategory().equalsIgnoreCase("Funcao")) {
                return r;
            }
        }

        return null;

    }

    public Register tableHasId(SymbolTable symbolTable, String lexema) {

        if (symbolTable.dadSymbolTable == null) {
            for (Register r : symbolTable.getRegistersList()) {
                if (r.getLexema().equalsIgnoreCase(lexema) && (r.getCategory().equalsIgnoreCase("Variavel") || (r.getCategory().equalsIgnoreCase("Parametro")))) {

                    return r;

                }
            }
            return null;
        }

        for (Register r : symbolTable.getRegistersList()) {
            if (r.getLexema().equalsIgnoreCase(lexema) && (r.getCategory().equalsIgnoreCase("Variavel") || (r.getCategory().equalsIgnoreCase("Parametro")))) {
                return r;

            }
        }

        return tableHasId(symbolTable.getDadSymbolTable(), lexema);

    }

    public void attParamNumber() {
        ArrayList<Register> listCopy = new ArrayList<>(this.getRegistersList());

        

        Register register = listCopy.get(0);
        register.setParamNumbers(this.paramNumber);

        SymbolTable st = this.getDadSymbolTable();

        for (Register r : st.getRegistersList()) {

            if (r.getLexema().equals(register.getLexema())) {
                r.setParamNumbers(this.paramNumber);
            }

        }

        setRegistersList(listCopy);

    }

    public void calcOffSet() {
        ArrayList<Register> listCopy = new ArrayList<>(this.getRegistersList());

        

        Register function = listCopy.get(0);

        for (int i = 1; i < listCopy.size(); i++) {
            Register r = listCopy.get(i);
            if (r.getCategory().equalsIgnoreCase("Parametro")) {

                int offset = 12 + (this.paramNumber - i) * 4;
                r.setOffset(offset);

                if (i == 1) {
                    function.setOffset(offset + 4);
                }

            }
        }
        
        setRegistersList(listCopy);
        

    }

}
