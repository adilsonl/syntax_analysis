/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.Objects;

/**
 *
 * @author lucas
 */
public class Register implements Comparable {
    private String lexema;
    private String category;
    private int nivel;
    private int offset;
    private int paramNumbers;
    private String rotulo;
    private SymbolTable symbolTable;
    
    private String type;
    private String address;

    
    public Register(String lexema, String category, int nivel, int offset, int paramNumbers, String rotulo, SymbolTable symbolTable, String type, String address) {
        this.lexema = lexema;
        this.category = category;
        this.nivel = nivel;
        this.offset = offset;
        this.paramNumbers = paramNumbers;
        this.rotulo = rotulo;
        this.symbolTable = symbolTable;
        this.type = type;
        this.address = address;
    }
    
    

    
        public Register() {

    }

        public Register(String lexema, String category, String type, String address) {
        this.lexema = lexema;
        this.category = category;
        this.type = type;
        this.address = address;
    }

    public String getLexema() {
        return lexema;
    }

    public void setLexema(String lexema) {
        this.lexema = lexema;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getParamNumbers() {
        return paramNumbers;
    }

    public void setParamNumbers(int paramNumbers) {
        this.paramNumbers = paramNumbers;
    }

    public String getRotulo() {
        return rotulo;
    }

    public void setRotulo(String rotulo) {
        this.rotulo = rotulo;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public void setSymbolTable(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }
    

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int compareTo(Object t) {
        if (t == null) {
            return -1;
        }
        if (getClass() != t.getClass()) {
            return -1;
        }
        final Register received = (Register) t;
        if (!Objects.equals(this.lexema, received.lexema)) {
            return -1;
        }
        if (!Objects.equals(this.category, received.category)) {
            return -1;
        }
        return 0;
    }
    
    
    
    
    
}
