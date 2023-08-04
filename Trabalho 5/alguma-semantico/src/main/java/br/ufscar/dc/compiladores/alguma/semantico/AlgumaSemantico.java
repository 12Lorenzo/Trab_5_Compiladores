package br.ufscar.dc.compiladores.alguma.semantico;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;


import br.ufscar.dc.compiladores.alguma.semantico.TabelaSimbolos.EntradaTabelaDeSimbolos;


import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.CmdAtribuicaoContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Declaracao_constanteContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Declaracao_globalContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Declaracao_tipoContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Declaracao_variavelContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.IdentificadorContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.ProgramaContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Tipo_basico_identContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.VariavelContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.ParametroContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.CmdRetorneContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Parcela_unarioContext;


public class AlgumaSemantico extends AlgumaBaseVisitor{
    //TabelaSimbolos tabela;
    Escopo escopos = new Escopo(TabelaSimbolos.TipoAlguma.VOID);

    @Override
    public Object visitPrograma(ProgramaContext ctx) {
        //System.out.println("Entrei na visitPrograma");
        return super.visitPrograma(ctx);
    }
    
    @Override
    public Object visitDeclaracao_constante(Declaracao_constanteContext ctx) {
        //System.out.println("Entrei na visitDeclaracao_constante");
        TabelaSimbolos escopoAtual = escopos.getEscopo();
        if (escopoAtual.existe(ctx.IDENT().getText())) {
            AlgumaSemanticoUtil.adicionarErroSemantico(ctx.start, "constante" + ctx.IDENT().getText()
                    + " ja declarado anteriormente");
        } else {
            TabelaSimbolos.TipoAlguma tipo = TabelaSimbolos.TipoAlguma.INT;
            TabelaSimbolos.TipoAlguma aux = AlgumaSemanticoUtil.getTipo(ctx.tipo_basico().getText()) ;
            if(aux != null){
                tipo = aux;
            }
            escopoAtual.adicionar(ctx.IDENT().getText(), tipo, TabelaSimbolos.Estrutura.CONST);
        }

        return super.visitDeclaracao_constante(ctx);
    }

    @Override
    public Object visitDeclaracao_tipo(Declaracao_tipoContext ctx) {
        //System.out.println("Entrei na visitDeclaracao_tipo");
        TabelaSimbolos escopoAtual = escopos.getEscopo();
        if (escopoAtual.existe(ctx.IDENT().getText())) {
             AlgumaSemanticoUtil.adicionarErroSemantico(ctx.start, "tipo " + ctx.IDENT().getText()
                    + " declarado duas vezes num mesmo escopo");
        } else {
            TabelaSimbolos.TipoAlguma tipo = AlgumaSemanticoUtil.getTipo(ctx.tipo().getText());
            if(tipo != null)
                escopoAtual.adicionar(ctx.IDENT().getText(), tipo, TabelaSimbolos.Estrutura.TIPO);
            else if(ctx.tipo().registro() != null){
                ArrayList<TabelaSimbolos.EntradaTabelaDeSimbolos> varReg = new ArrayList<>();
                for(VariavelContext va : ctx.tipo().registro().variavel()){
                    TabelaSimbolos.TipoAlguma tipoReg =  AlgumaSemanticoUtil.getTipo(va.tipo().getText());
                    for(IdentificadorContext id2 : va.identificador()){
                        varReg.add(escopoAtual.new EntradaTabelaDeSimbolos(id2.getText(), tipoReg, TabelaSimbolos.Estrutura.TIPO));
                    }
                }

                if (escopoAtual.existe(ctx.IDENT().getText())) {
                    AlgumaSemanticoUtil.adicionarErroSemantico(ctx.start, "identificador " + ctx.IDENT().getText()
                            + " ja declarado anteriormente");
                }
                else{
                    escopoAtual.adicionar(ctx.IDENT().getText(), TabelaSimbolos.TipoAlguma.REG, TabelaSimbolos.Estrutura.TIPO);
                }

                for(TabelaSimbolos.EntradaTabelaDeSimbolos re : varReg){
                    String nameVar = ctx.IDENT().getText() + '.' + re.getNome();
                    if (escopoAtual.existe(nameVar)) {
                        //System.out.println("Entrei na visitDeclaracao_tipo");
                        AlgumaSemanticoUtil.adicionarErroSemantico(ctx.start, "identificador " + nameVar
                                + " ja declarado anteriormente");
                    }
                    else{
                        // SemanticoUtils.adicionarErroSemantico(id.start, "oi rs tamo adicionando " + re.name );
                        escopoAtual.adicionar(re);
                        escopoAtual.adicionar(ctx.IDENT().getText(), re);
                    }
                }
                // escopoAtual.insert(ctx.IDENT().getText(), Table.Tipos.REG, Table.Structure.TIPO);
            }
            TabelaSimbolos.TipoAlguma t =  AlgumaSemanticoUtil.getTipo(ctx.tipo().getText());
            escopoAtual.adicionar(ctx.IDENT().getText(), t, TabelaSimbolos.Estrutura.TIPO);
        }
        return super.visitDeclaracao_tipo(ctx);
    }

    @Override
    public Object visitDeclaracao_variavel(Declaracao_variavelContext ctx) {
        //System.out.println("Entrei na visitDeclaracao_variavel");
        TabelaSimbolos escopoAtual = escopos.getEscopo();
        for (IdentificadorContext id : ctx.variavel().identificador()) {
            String nomeId = "";
            int i = 0;
            for(TerminalNode ident : id.IDENT()){
                if(i++ > 0)
                    nomeId += ".";
                nomeId += ident.getText();
            }
            if (escopoAtual.existe(nomeId)) {
                AlgumaSemanticoUtil.adicionarErroSemantico(id.start, "identificador " + nomeId
                        + " ja declarado anteriormente");
            } else {
                TabelaSimbolos.TipoAlguma tipo = AlgumaSemanticoUtil.getTipo(ctx.variavel().tipo().getText());
                if(tipo != null)
                    escopoAtual.adicionar(nomeId, tipo, TabelaSimbolos.Estrutura.VAR);
                else{
                    TerminalNode identTipo =    ctx.variavel().tipo() != null
                                                && ctx.variavel().tipo().tipo_estendido() != null 
                                                && ctx.variavel().tipo().tipo_estendido().tipo_basico_ident() != null  
                                                && ctx.variavel().tipo().tipo_estendido().tipo_basico_ident().IDENT() != null 
                                                ? ctx.variavel().tipo().tipo_estendido().tipo_basico_ident().IDENT() : null;
                    if(identTipo != null){
                        ArrayList<TabelaSimbolos.EntradaTabelaDeSimbolos> regVars = null;
                        boolean found = false;
                        for(TabelaSimbolos t: escopos.getPilha()){
                            if(!found){
                                if(t.existe(identTipo.getText())){
                                    regVars = t.getTipoProp(identTipo.getText());
                                    found = true;
                                }
                            }
                        }
                        if(escopoAtual.existe(nomeId)){
                            AlgumaSemanticoUtil.adicionarErroSemantico(id.start, "identificador " + nomeId
                                        + " ja declarado anteriormente");
                        } else{
                            escopoAtual.adicionar(nomeId, TabelaSimbolos.TipoAlguma.REG, TabelaSimbolos.Estrutura.VAR);
                            for(TabelaSimbolos.EntradaTabelaDeSimbolos s: regVars){
                                escopoAtual.adicionar(nomeId + "." + s.getNome(), s.getTipo(), TabelaSimbolos.Estrutura.VAR);
                            }   
                        }
                    }
                    else if(ctx.variavel().tipo().registro() != null){
                        ArrayList<TabelaSimbolos.EntradaTabelaDeSimbolos> varReg = new ArrayList<>();
                        for(VariavelContext va : ctx.variavel().tipo().registro().variavel()){
                            TabelaSimbolos.TipoAlguma tipoReg =  AlgumaSemanticoUtil.getTipo(va.tipo().getText());
                            for(IdentificadorContext id2 : va.identificador()){
                                varReg.add(escopoAtual.new EntradaTabelaDeSimbolos(id2.getText(), tipoReg, TabelaSimbolos.Estrutura.VAR));
                            }
                        }  
                        escopoAtual.adicionar(nomeId, TabelaSimbolos.TipoAlguma.REG, TabelaSimbolos.Estrutura.VAR);

                        for(TabelaSimbolos.EntradaTabelaDeSimbolos re : varReg){
                            String nameVar = nomeId + '.' + re.getNome();
                            if (escopoAtual.existe(nameVar)) {
                                AlgumaSemanticoUtil.adicionarErroSemantico(id.start, "identificador " + nameVar
                                        + " ja declarado anteriormente");
                            }
                            else{
                                // SemanticoUtils.adicionarErroSemantico(id.start, "oi rs tamo adicionando " + re.name );
                                escopoAtual.adicionar(re);
                                escopoAtual.adicionar(nameVar, re.getTipo(), TabelaSimbolos.Estrutura.VAR);
                            }
                        }

                    }
                    else{//tipo registro estendido
                        escopoAtual.adicionar(id.getText(), TabelaSimbolos.TipoAlguma.INT, TabelaSimbolos.Estrutura.VAR);
                    }
                }
            }
        }
        return super.visitDeclaracao_variavel(ctx);
    }

    @Override
    public Object visitDeclaracao_global(Declaracao_globalContext ctx) {
        //System.out.println("Entrei na visitDeclaracao_global");
        TabelaSimbolos escopoAtual = escopos.getEscopo();
        Object ret;
        if (escopoAtual.existe(ctx.IDENT().getText())) {
            AlgumaSemanticoUtil.adicionarErroSemantico(ctx.start, ctx.IDENT().getText()
                    + " ja declarado anteriormente");
            ret = super.visitDeclaracao_global(ctx);
        } else {
            TabelaSimbolos.TipoAlguma returnTypeFunc = TabelaSimbolos.TipoAlguma.VOID;
            if(ctx.getText().startsWith("funcao")){
                returnTypeFunc = AlgumaSemanticoUtil.getTipo(ctx.tipo_estendido().getText());
                escopoAtual.adicionar(ctx.IDENT().getText(), returnTypeFunc, TabelaSimbolos.Estrutura.FUNC);
            }
            else{
                returnTypeFunc = TabelaSimbolos.TipoAlguma.VOID;
                escopoAtual.adicionar(ctx.IDENT().getText(), returnTypeFunc, TabelaSimbolos.Estrutura.PROC);
            }
            escopos.criar(returnTypeFunc);
            TabelaSimbolos escopoAntigo = escopoAtual;
            escopoAtual = escopos.getEscopo();
            if(ctx.parametros() != null){
                for(ParametroContext p : ctx.parametros().parametro()){
                    for (IdentificadorContext id : p.identificador()) {
                        String nomeId = "";
                        int i = 0;
                        for(TerminalNode ident : id.IDENT()){
                            if(i++ > 0)
                                nomeId += ".";
                            nomeId += ident.getText();
                        }
                        if (escopoAtual.existe(nomeId)) {
                            AlgumaSemanticoUtil.adicionarErroSemantico(id.start, "identificador " + nomeId
                                    + " ja declarado anteriormente");
                        } else {
                            TabelaSimbolos.TipoAlguma tipo = AlgumaSemanticoUtil.getTipo(p.tipo_estendido().getText());
                            if(tipo != null){
                                EntradaTabelaDeSimbolos in = escopoAtual.new EntradaTabelaDeSimbolos(nomeId, tipo, TabelaSimbolos.Estrutura.VAR);
                                escopoAtual.adicionar(in);
                                escopoAntigo.adicionar(ctx.IDENT().getText(), in);
                            }
                            else{
                                TerminalNode identTipo =    p.tipo_estendido().tipo_basico_ident() != null  
                                                            && p.tipo_estendido().tipo_basico_ident().IDENT() != null 
                                                            ? p.tipo_estendido().tipo_basico_ident().IDENT() : null;
                                if(identTipo != null){
                                    ArrayList<TabelaSimbolos.EntradaTabelaDeSimbolos> regVars = null;
                                    boolean found = false;
                                    for(TabelaSimbolos t: escopos.getPilha()){
                                        if(!found){
                                            if(t.existe(identTipo.getText())){
                                                regVars = t.getTipoProp(identTipo.getText());
                                                found = true;
                                            }
                                        }
                                    }
                                    if(escopoAtual.existe(nomeId)){
                                        AlgumaSemanticoUtil.adicionarErroSemantico(id.start, "identificador " + nomeId
                                                    + " ja declarado anteriormente");
                                    } else{
                                        EntradaTabelaDeSimbolos in = escopoAtual.new EntradaTabelaDeSimbolos(nomeId, TabelaSimbolos.TipoAlguma.REG, TabelaSimbolos.Estrutura.VAR);
                                        escopoAtual.adicionar(in);
                                        escopoAntigo.adicionar(ctx.IDENT().getText(), in);

                                        for(TabelaSimbolos.EntradaTabelaDeSimbolos s: regVars){
                                            escopoAtual.adicionar(nomeId + "." + s.getNome(), s.getTipo(), TabelaSimbolos.Estrutura.VAR);
                                        }   
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ret = super.visitDeclaracao_global(ctx);
            escopos.dropEscopo();

        }
        return ret;
    }


    @Override
    public Object visitTipo_basico_ident(Tipo_basico_identContext ctx) {
        //System.out.println("Entrei na visitTipo_basico_ident");
        if(ctx.IDENT() != null){
            boolean exists = false;
            for(TabelaSimbolos escopo : escopos.getPilha()) {
                if(escopo.existe(ctx.IDENT().getText())) {
                    exists = true;
                }
            }
            if(!exists){
                AlgumaSemanticoUtil.adicionarErroSemantico(ctx.start, "tipo " + ctx.IDENT().getText()
                            + " nao declarado");
            }
        }
        return super.visitTipo_basico_ident(ctx);
    }

    @Override
    public Object visitIdentificador(IdentificadorContext ctx) {
        //System.out.println("Entrei na visitIdentificador");
        String nomeVar = "";
        int i = 0;
        for(TerminalNode id : ctx.IDENT()){
            if(i++ > 0)
                nomeVar += ".";
            nomeVar += id.getText();
        }
        boolean erro = true;
        for(TabelaSimbolos escopo : escopos.getPilha()) {

            if(escopo.existe(nomeVar)) {
                erro = false;
            }
        }
        if(erro)
            AlgumaSemanticoUtil.adicionarErroSemantico(ctx.start, "identificador " + nomeVar + " nao declarado");
        return super.visitIdentificador(ctx);
    }

    @Override
    public Object visitCmdAtribuicao(CmdAtribuicaoContext ctx) {
        //System.out.println("Entrei na visitCmdAtribuicao");
        TabelaSimbolos.TipoAlguma tipoExpressao = AlgumaSemanticoUtil.verificarTipo(escopos, ctx.expressao());
        boolean error = false;
        String pointerChar = ctx.getText().charAt(0) == '^' ? "^" : "";
        String nomeVar = "";
        int i = 0;
        for(TerminalNode id : ctx.identificador().IDENT()){
            if(i++ > 0)
                nomeVar += ".";
            nomeVar += id.getText();
        }
        if (tipoExpressao != TabelaSimbolos.TipoAlguma.INVALIDO) {
            boolean found = false;
            for(TabelaSimbolos escopo : escopos.getPilha()){
                if (escopo.existe(nomeVar) && !found)  {
                    found = true;
                    TabelaSimbolos.TipoAlguma tipoVariavel = AlgumaSemanticoUtil.verificarTipo(escopos, nomeVar);
                    Boolean varNumeric = tipoVariavel == TabelaSimbolos.TipoAlguma.REAL || tipoVariavel == TabelaSimbolos.TipoAlguma.INT;
                    Boolean expNumeric = tipoExpressao == TabelaSimbolos.TipoAlguma.REAL || tipoExpressao == TabelaSimbolos.TipoAlguma.INT;
                    if  (!(varNumeric && expNumeric) && tipoVariavel != tipoExpressao && tipoExpressao != TabelaSimbolos.TipoAlguma.INVALIDO) {
                        error = true;
                    }
                } 
            }
        } else{
            error = true;
        }

        if(error){
            nomeVar = ctx.identificador().getText();
            AlgumaSemanticoUtil.adicionarErroSemantico(ctx.identificador().start, "atribuicao nao compativel para " + pointerChar + nomeVar );
        }

        return super.visitCmdAtribuicao(ctx);
    }

    @Override
    public Object visitCmdRetorne(CmdRetorneContext ctx) {
        //System.out.println("Entrei na visitCmdRetorne");
        if(escopos.getEscopo().retorTipo == TabelaSimbolos.TipoAlguma.VOID){
            AlgumaSemanticoUtil.adicionarErroSemantico(ctx.start, "comando retorne nao permitido nesse escopo");
        } 
        return super.visitCmdRetorne(ctx);
    }

    //para parcela unarios, verificamos se a variavel existe
    @Override
    public Object visitParcela_unario(Parcela_unarioContext ctx) {
        //System.out.println("Entrei na visitParcela_unario");
        TabelaSimbolos escopoAtual = escopos.getEscopo();
        if(ctx.IDENT() != null){
            String name = ctx.IDENT().getText();
            if(escopoAtual.existe(ctx.IDENT().getText())){
                List<EntradaTabelaDeSimbolos> params = escopoAtual.getTipoProp(name);
                boolean error = false;
                if(params.size() != ctx.expressao().size()){
                    error = true;
                } else {
                    for(int i = 0; i < params.size(); i++){
                        if(params.get(i).getTipo() != AlgumaSemanticoUtil.verificarTipo(escopos, ctx.expressao().get(i))){
                            error = true;
                        }
                    }
                }
                if(error){
                    AlgumaSemanticoUtil.adicionarErroSemantico(ctx.start, "incompatibilidade de parametros na chamada de " + name);
                    
                }
            }
        }

        return super.visitParcela_unario(ctx);
    }
}
