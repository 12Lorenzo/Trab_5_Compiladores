package br.ufscar.dc.compiladores.alguma.semantico;

import java.util.ArrayList;
import java.util.HashMap;

public class TabelaSimbolos {
    public TabelaSimbolos.TipoAlguma retorTipo;

    public enum TipoAlguma {
        //Tipo de variavel
        INT,
        REAL,
        CADEIA,
        LOGICO,
        INVALIDO,
        REG,
        VOID
    }

    public enum Estrutura{
        //Estruturas
        VAR, 
        CONST, 
        PROC, 
        FUNC, 
        TIPO
    }
    
    class EntradaTabelaDeSimbolos {
        private String nome;
        private TipoAlguma tipo;
        Estrutura estrut;

        public EntradaTabelaDeSimbolos(String nome, TipoAlguma tipo, Estrutura estrut) {
            this.nome = nome;
            this.tipo = tipo;
            this.estrut = estrut;
        }

        public EntradaTabelaDeSimbolos(TipoAlguma tipo) {
            this.tipo = tipo;
        }

        public String getNome() {
            return nome;
        }

        public TipoAlguma getTipo(){
            return this.tipo;
        }
    }
    
    private final HashMap<String, EntradaTabelaDeSimbolos> tabela;
    private final HashMap<String, ArrayList<EntradaTabelaDeSimbolos>> tipoTabela;
    
    public TabelaSimbolos(){
        this.tabela = new HashMap<>();
        this.tipoTabela = new HashMap<>();
    }

    public TabelaSimbolos(TabelaSimbolos.TipoAlguma retorTipo) {
        this.tabela = new HashMap<>();
        this.tipoTabela = new HashMap<>();
        this.retorTipo = retorTipo;
    }
    
    public void adicionar(String nome, TipoAlguma tipo, Estrutura estrut) {
        EntradaTabelaDeSimbolos ets = new EntradaTabelaDeSimbolos(nome, tipo, estrut);
        tabela.put(nome, ets);
    }

    public void adicionar(EntradaTabelaDeSimbolos ets) {
        tabela.put(ets.nome, ets);
    }

    public void adicionar(String tipoName, EntradaTabelaDeSimbolos ets){
        if(tipoTabela.containsKey(tipoName)){
            tipoTabela.get(tipoName).add(ets);
        }else{
            ArrayList<EntradaTabelaDeSimbolos> list = new ArrayList<>();
            list.add(ets);
            tipoTabela.put(tipoName, list);
        }
    }
    
    public boolean existe(String nome) {
        return tabela.containsKey(nome);
    }
    
    public TipoAlguma verificar(String nome) {
        return tabela.get(nome).tipo;
    }

    public ArrayList<EntradaTabelaDeSimbolos> getTipoProp(String nome){
        return tipoTabela.get(nome);
    }
}
