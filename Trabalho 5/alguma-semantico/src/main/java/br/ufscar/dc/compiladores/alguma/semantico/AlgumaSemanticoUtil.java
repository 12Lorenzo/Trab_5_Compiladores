package br.ufscar.dc.compiladores.alguma.semantico;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.Token;

import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Exp_aritmeticaContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.ExpressaoContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.FatorContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Fator_logicoContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.ParcelaContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.TermoContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Termo_logicoContext;



public class AlgumaSemanticoUtil {
    public static List<String> errosSemanticos = new ArrayList<>();
    
    public static void adicionarErroSemantico(Token t, String mensagem) {
        //System.out.println("Entrei na adicionarErroSemantico");
        int linha = t.getLine();
        //int coluna = t.getCharPositionInLine();
        errosSemanticos.add(String.format("Linha %d: %s", linha, mensagem));
    }
    
    //verifica o tipo de uma expressao, os termos devem ser do mesmo tipo
    public static TabelaSimbolos.TipoAlguma verificarTipo(Escopo escopos, AlgumaParser.ExpressaoContext ctx) {
        //System.out.println("Entrei na verificarTipo");
        TabelaSimbolos.TipoAlguma ret = null;
        for (Termo_logicoContext ta : ctx.termo_logico()) {
            TabelaSimbolos.TipoAlguma aux = verificarTipo(escopos, ta);
            if (ret == null) {
                ret = aux;
            } else if (ret != aux && aux != TabelaSimbolos.TipoAlguma.INVALIDO) {
                ret = TabelaSimbolos.TipoAlguma.INVALIDO;
            }
        }
        return ret;
    }

    //verifica o tipo de um termo logico, os fatores devem ser do mesmo tipo
    public static TabelaSimbolos.TipoAlguma verificarTipo(Escopo escopos, AlgumaParser.Termo_logicoContext ctx) {//
        //System.out.println("Entrei na verificarTipo");
        TabelaSimbolos.TipoAlguma ret = null;
        for (Fator_logicoContext ta : ctx.fator_logico()) {
            TabelaSimbolos.TipoAlguma aux = verificarTipo(escopos, ta);
            if (ret == null) {
                ret = aux;
            } else if (ret != aux && aux != TabelaSimbolos.TipoAlguma.INVALIDO) {
                ret = TabelaSimbolos.TipoAlguma.INVALIDO;
            }
        }

        return ret;
    }

    //para verificar um fator, como ele e constituido por uma parcela, ele simplesmente verifica esta.
    public static TabelaSimbolos.TipoAlguma verificarTipo(Escopo escopos, AlgumaParser.Fator_logicoContext ctx) {
        //System.out.println("Entrei na verificarTipo");
        //SemanticoUtils.adicionarErroSemantico(ctx.start, ctx.getText() + verificarTipo(escopos, ctx.parcela_logica()));
        return verificarTipo(escopos, ctx.parcela_logica());
    }

    public static TabelaSimbolos.TipoAlguma verificarTipo(Escopo escopos, AlgumaParser.Parcela_logicaContext ctx) {
        TabelaSimbolos.TipoAlguma ret = null;
        if(ctx.exp_relacional() != null){
            ret = verificarTipo(escopos, ctx.exp_relacional());
        } else{
            ret = TabelaSimbolos.TipoAlguma.LOGICO;
        }


        return ret;
    }

    //verifica o tipo da expressao relacional, que é constituida por expressoes aritmeticas.
    public static TabelaSimbolos.TipoAlguma verificarTipo(Escopo escopos, AlgumaParser.Exp_relacionalContext ctx) {
        //System.out.println("Entrei na verificarTipo");
        TabelaSimbolos.TipoAlguma ret = null;
        if(ctx.op_relacional() != null){
            for (Exp_aritmeticaContext ta : ctx.exp_aritmetica()) {
                TabelaSimbolos.TipoAlguma aux = verificarTipo(escopos, ta);
                Boolean auxNumeric = aux == TabelaSimbolos.TipoAlguma.REAL || aux == TabelaSimbolos.TipoAlguma.INT; //casos numericos inteiros e reais se correlacionam
                Boolean retNumeric = ret == TabelaSimbolos.TipoAlguma.REAL || ret == TabelaSimbolos.TipoAlguma.INT;
                if (ret == null) {
                    ret = aux;
                } else if (!(auxNumeric && retNumeric) && aux != ret) {
                    ret = TabelaSimbolos.TipoAlguma.INVALIDO;
                }
            }
            if(ret != TabelaSimbolos.TipoAlguma.INVALIDO){
                ret = TabelaSimbolos.TipoAlguma.LOGICO;
            }
        } else {
            ret = verificarTipo(escopos, ctx.exp_aritmetica(0));
        }


        return ret;
    }

    //verifica a expressão aritmetica, verificando cada termo se são compativeis
    public static TabelaSimbolos.TipoAlguma verificarTipo(Escopo escopos, AlgumaParser.Exp_aritmeticaContext ctx) {
        //System.out.println("Entrei na verificarTipo");
        TabelaSimbolos.TipoAlguma ret = null;
        for (TermoContext ta : ctx.termo()) {
            TabelaSimbolos.TipoAlguma aux = verificarTipo(escopos, ta);
            if (ret == null) {
                ret = aux;
            } else if (ret != aux && aux != TabelaSimbolos.TipoAlguma.INVALIDO) {
                ret = TabelaSimbolos.TipoAlguma.INVALIDO;
            }
        }


        return ret;
    }

    //verifica um termo, composto por fatores que devem ser compativeis.
    public static TabelaSimbolos.TipoAlguma verificarTipo(Escopo escopos, AlgumaParser.TermoContext ctx) {
        //System.out.println("Entrei na verificarTipo");
        TabelaSimbolos.TipoAlguma ret = null;

        for (FatorContext fa : ctx.fator()) {
            TabelaSimbolos.TipoAlguma aux = verificarTipo(escopos, fa);
            Boolean auxNumeric = aux == TabelaSimbolos.TipoAlguma.REAL || aux == TabelaSimbolos.TipoAlguma.INT; //casos numericos inteiros e reais se correlacionam
            Boolean retNumeric = ret == TabelaSimbolos.TipoAlguma.REAL || ret == TabelaSimbolos.TipoAlguma.INT;
            if (ret == null) {
                ret = aux;
            } else if (!(auxNumeric && retNumeric) && aux != ret) {
                ret = TabelaSimbolos.TipoAlguma.INVALIDO;
            }
        }

        return ret;
    }

    //para cada fator devemos verificar se as parcelas que os compoem são compativeis.
    public static TabelaSimbolos.TipoAlguma verificarTipo(Escopo escopos, AlgumaParser.FatorContext ctx) {
        //System.out.println("Entrei na verificarTipo");
        TabelaSimbolos.TipoAlguma ret = null;

        for (ParcelaContext fa : ctx.parcela()) {
            TabelaSimbolos.TipoAlguma aux = verificarTipo(escopos, fa);
            if (ret == null) {
                ret = aux;
            } else if (ret != aux && aux != TabelaSimbolos.TipoAlguma.INVALIDO) {
                ret = TabelaSimbolos.TipoAlguma.INVALIDO;
            }
        }

        return ret;
    }

    //para caso de parcelas vamos verificar dependendo de seu tipo, pode ser unaria ou nao, um if para verificar o tipo em cada
    //caso foi utilizado.
    public static TabelaSimbolos.TipoAlguma verificarTipo(Escopo escopos, AlgumaParser.ParcelaContext ctx) {
        //System.out.println("Entrei na verificarTipo");
        TabelaSimbolos.TipoAlguma ret = TabelaSimbolos.TipoAlguma.INVALIDO;

        if(ctx.parcela_nao_unario() != null){
            ret = verificarTipo(escopos, ctx.parcela_nao_unario());
        }
        else {

            ret = verificarTipo(escopos, ctx.parcela_unario());
        }
        return ret;
    }

    //na parcela nao unaria, temos um identificador ou uma cadeia, no caso de identificador temos de verificar seu tipo
    public static TabelaSimbolos.TipoAlguma verificarTipo(Escopo escopos, AlgumaParser.Parcela_nao_unarioContext ctx) {
        //System.out.println("Entrei na verificarTipo");
        if (ctx.identificador() != null) {
            return verificarTipo(escopos, ctx.identificador());
        }
        return TabelaSimbolos.TipoAlguma.CADEIA;
    }

    //para verificar um identificador, verificamos seu nome completo, composto por exemplo NOME1.NOME2.NOME...
    //tendo o nome pronto, vemos se esse existe em algum escopo.
    public static TabelaSimbolos.TipoAlguma verificarTipo(Escopo escopos, AlgumaParser.IdentificadorContext ctx) {
        //System.out.println("Entrei na verificarTipo");
        String nomeVar = "";
        TabelaSimbolos.TipoAlguma ret = TabelaSimbolos.TipoAlguma.INVALIDO;
        for(int i = 0; i < ctx.IDENT().size(); i++){
            nomeVar += ctx.IDENT(i).getText();
            if(i != ctx.IDENT().size() - 1){
                nomeVar += ".";
            }
        }
        for(TabelaSimbolos tabela : escopos.getPilha()){
            if (tabela.existe(nomeVar)) {
                ret = verificarTipo(escopos, nomeVar);
            }
        }

        return ret;
    }
    
    //Para parcelas unarias, vemos qual seu tipo, ou seja o que esta escrito e o retornamos
    public static TabelaSimbolos.TipoAlguma verificarTipo(Escopo escopos, AlgumaParser.Parcela_unarioContext ctx) {
        //System.out.println("Entrei na verificarTipo");
        if (ctx.NUM_INT() != null) {
            return TabelaSimbolos.TipoAlguma.INT;
        }
        if (ctx.NUM_REAL() != null) {
            return TabelaSimbolos.TipoAlguma.REAL;
        }
        if(ctx.identificador() != null){
            return verificarTipo(escopos, ctx.identificador());
        }
        if (ctx.IDENT() != null) {
            return verificarTipo(escopos, ctx.IDENT().getText());
        } else {
            TabelaSimbolos.TipoAlguma ret = null;
            for (ExpressaoContext fa : ctx.expressao()) {
                TabelaSimbolos.TipoAlguma aux = verificarTipo(escopos, fa);
                if (ret == null) {
                    ret = aux;
                } else if (ret != aux && aux != TabelaSimbolos.TipoAlguma.INVALIDO) {
                    ret = TabelaSimbolos.TipoAlguma.INVALIDO;
                }
            }
            return ret;
        }
    }
    
    //No caso de receber so uma string, vemos se ela existe, para descobrir se o nome da variavel foi criado ao ser utilizado.
    public static TabelaSimbolos.TipoAlguma verificarTipo(Escopo escopos, String nomeVar) {
        //System.out.println("Entrei na verificarTipo");
        TabelaSimbolos.TipoAlguma type = TabelaSimbolos.TipoAlguma.INVALIDO;
        for(TabelaSimbolos tabela : escopos.getPilha()){
            if(tabela.existe(nomeVar)){
                return tabela.verificar(nomeVar);
            }
        }

        return type;
    }

    public static TabelaSimbolos.TipoAlguma getTipo(String val){
        TabelaSimbolos.TipoAlguma tipo = null;
                switch(val) {
                    case "literal": 
                        tipo = TabelaSimbolos.TipoAlguma.CADEIA;
                        break;
                    case "inteiro": 
                        tipo = TabelaSimbolos.TipoAlguma.INT;
                        break;
                    case "real": 
                        tipo = TabelaSimbolos.TipoAlguma.REAL;
                        break;
                    case "logico": 
                        tipo = TabelaSimbolos.TipoAlguma.LOGICO;
                        break;
                    default:
                        break;
                }
        return tipo;
    }

     public static String getCType(String val){
        String tipo = null;
                switch(val) {
                    case "literal": 
                        tipo = "char";
                        break;
                    case "inteiro": 
                        tipo = "int";
                        break;
                    case "real": 
                        tipo = "float";
                        break;
                    default:
                        break;
                }
        return tipo;
    }

    public static String getCTypeSymbol(TabelaSimbolos.TipoAlguma tipo){
        String type = null;
                switch(tipo) {
                    case CADEIA: 
                        type = "s";
                        break;
                    case INT: 
                        type = "d";
                        break;
                    case REAL: 
                        type = "f";
                        break;
                    default:
                        break;
                }
        return type;
    }
}
