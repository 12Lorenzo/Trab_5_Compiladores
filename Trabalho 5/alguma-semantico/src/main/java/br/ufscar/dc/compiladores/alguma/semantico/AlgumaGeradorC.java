package br.ufscar.dc.compiladores.alguma.semantico;

import java.util.ArrayList;
import java.util.Arrays;

import org.antlr.v4.runtime.tree.TerminalNode;

import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.CmdAtribuicaoContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.CmdCasoContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.CmdChamadaContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.CmdContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.CmdEnquantoContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.CmdEscrevaContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.CmdFacaContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.CmdLeiaContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.CmdParaContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.CmdRetorneContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.CmdSeContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.CmdSenaoContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.CorpoContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Decl_local_globalContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Declaracao_constanteContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Declaracao_globalContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Declaracao_localContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Declaracao_tipoContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Declaracao_variavelContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.DimensaoContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Exp_aritmeticaContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Exp_relacionalContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.ExpressaoContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.FatorContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Fator_logicoContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.IdentificadorContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Item_selecaoContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.ParametroContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.ParcelaContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Parcela_logicaContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Parcela_nao_unarioContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Parcela_unarioContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.RegistroContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.SelecaoContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.TermoContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Termo_logicoContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.TipoContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Tipo_basico_identContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Tipo_estendidoContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.Valor_constanteContext;
import br.ufscar.dc.compiladores.alguma.semantico.AlgumaParser.VariavelContext;
import br.ufscar.dc.compiladores.alguma.semantico.TabelaSimbolos.EntradaTabelaDeSimbolos;

public class AlgumaGeradorC extends AlgumaBaseVisitor<Void> {
    StringBuilder saida;
    TabelaSimbolos tabela;

    public AlgumaGeradorC() {
        saida = new StringBuilder();
        this.tabela = new TabelaSimbolos();
    }

    @Override
    public Void visitPrograma(AlgumaParser.ProgramaContext ctx) {//constroi a estrutura padrao do programa, visitando declaracoes e corpo
        saida.append("#include <stdio.h>\n");
        saida.append("#include <stdlib.h>\n");
        saida.append("\n");
        ctx.declaracoes().decl_local_global().forEach(dec -> visitDecl_local_global(dec));
        saida.append("\n");
        saida.append("int main() {\n");

        visitCorpo(ctx.corpo());
        saida.append("return 0;\n");
        saida.append("}\n");
        return null;
    }

    @Override
    public Void visitDecl_local_global(Decl_local_globalContext ctx) {//define se a declaracao e local, ou global e redireciona
        
        if(ctx.declaracao_local() != null){
            visitDeclaracao_local(ctx.declaracao_local());
        }
        else if(ctx.declaracao_global() != null){
            visitDeclaracao_global(ctx.declaracao_global());
        }
        return null;
    }

    @Override
    public Void visitCorpo(CorpoContext ctx) {// visita cada declaracao no corpo do codigo, em seguida todos os comandos
        for(AlgumaParser.Declaracao_localContext dec : ctx.declaracao_local()) {
            visitDeclaracao_local(dec);
        }

        for(AlgumaParser.CmdContext com : ctx.cmd()) {
            visitCmd(com);
        }

        return null;
    }

    @Override
    public Void visitDeclaracao_global(Declaracao_globalContext ctx) {//declara ou procedimento ou funcoes
        
        if(ctx.getText().contains("procedimento")){
            saida.append("void " + ctx.IDENT().getText() + "(");
        }
        else{
            String cTipo = AlgumaSemanticoUtil.getCType(ctx.tipo_estendido().getText().replace("^", ""));
            TabelaSimbolos.TipoAlguma tipo = AlgumaSemanticoUtil.getTipo(ctx.tipo_estendido().getText());
            visitTipo_estendido(ctx.tipo_estendido());
            if(cTipo == "char"){
                saida.append("[80]");
            }
            saida.append(" " + ctx.IDENT().getText() + "(");
            tabela.adicionar(ctx.IDENT().getText(), tipo, TabelaSimbolos.Estrutura.FUNC);
        }
            ctx.parametros().parametro().forEach(var -> visitParametro(var));
            saida.append("){\n");
            ctx.declaracao_local().forEach(var -> visitDeclaracao_local(var));
            ctx.cmd().forEach(var -> visitCmd(var));
            saida.append("}\n");

        return null;
    }

    @Override
    public Void visitIdentificador(IdentificadorContext ctx) {//criado para imprimir identificadores com dimensoes
     
        saida.append(" ");
        int i = 0;
        for(TerminalNode id : ctx.IDENT()){
            if(i++ > 0)
                saida.append(".");
            saida.append(id.getText());
        }
        visitDimensao(ctx.dimensao());
        return null;
    }

    @Override
    public Void visitDimensao(DimensaoContext ctx) {//imprime a dimensao
       
        for(Exp_aritmeticaContext exp : ctx.exp_aritmetica()){
            saida.append("[");
            visitExp_aritmetica(exp);
            saida.append("]");
        }

        return null;
    }

    @Override
    public Void visitParametro(ParametroContext ctx) {//para converter parametros de funções 1 a 1
       
        int i = 0;
        String cTipo = AlgumaSemanticoUtil.getCType(ctx.tipo_estendido().getText().replace("^", ""));
        TabelaSimbolos.TipoAlguma tipo = AlgumaSemanticoUtil.getTipo(ctx.tipo_estendido().getText());
        for(IdentificadorContext id : ctx.identificador()){
            if(i++ > 0)
                saida.append(",");
            visitTipo_estendido(ctx.tipo_estendido());
            // saida.append(" " + id.getText());
            visitIdentificador(id);

            if(cTipo == "char"){
                saida.append("[80]");
            }
            tabela.adicionar(id.getText(),tipo,TabelaSimbolos.Estrutura.VAR);
        }
        return null;
    }

    @Override
    public Void visitDeclaracao_local(Declaracao_localContext ctx) {// ve o tipo de declaracao e redireciona
        //System.out.println("Declaring " + ctx.getText());
        if(ctx.declaracao_variavel() != null){
            visitDeclaracao_variavel(ctx.declaracao_variavel());
        }
        if(ctx.declaracao_constante() != null){
            visitDeclaracao_constante(ctx.declaracao_constante());
        } 
        else if(ctx.declaracao_tipo() != null){
            visitDeclaracao_tipo(ctx.declaracao_tipo());
        }

        return null;
    }

    @Override
    public Void visitDeclaracao_tipo(Declaracao_tipoContext ctx) {//cria um tipo (typedef)
      
        saida.append("typedef ");
        String cTipo = AlgumaSemanticoUtil.getCType(ctx.tipo().getText().replace("^", ""));
        TabelaSimbolos.TipoAlguma tipo = AlgumaSemanticoUtil.getTipo(ctx.tipo().getText());
       
        if(ctx.tipo().getText().contains("registro")){
            for(VariavelContext sub : ctx.tipo().registro().variavel()){
                for(IdentificadorContext idIns : sub.identificador()){
                    TabelaSimbolos.TipoAlguma tipoIns = AlgumaSemanticoUtil.getTipo(sub.tipo().getText());
                    System.out.println("Inserting reg " + sub.getText() + "." + idIns.getText());
                    tabela.adicionar(ctx.IDENT().getText() + "." + idIns.getText(), tipoIns, TabelaSimbolos.Estrutura.VAR);
                    tabela.adicionar(ctx.IDENT().getText(), tabela.new EntradaTabelaDeSimbolos(idIns.getText(), tipoIns, TabelaSimbolos.Estrutura.TIPO));
                }
            }
        }
        tabela.adicionar(ctx.IDENT().getText(), tipo, TabelaSimbolos.Estrutura.VAR);
        visitTipo(ctx.tipo());
        saida.append(ctx.IDENT() + ";\n");
        return null;
    }

    @Override
    public Void visitDeclaracao_variavel(Declaracao_variavelContext ctx) {//declaracao variavel, chama a variavel
        visitVariavel(ctx.variavel());
        return null;
    }

    @Override
    public Void visitVariavel(VariavelContext ctx) {//aqui onde realmente as variaveis sao desclarados
      
        String cTipo = AlgumaSemanticoUtil.getCType(ctx.tipo().getText().replace("^", ""));
        System.out.println("Visiting " + ctx.getText());
        TabelaSimbolos.TipoAlguma tipo = AlgumaSemanticoUtil.getTipo(ctx.tipo().getText());
        for(AlgumaParser.IdentificadorContext id: ctx.identificador()) {
            if(ctx.tipo().getText().contains("registro")){
                for(VariavelContext sub : ctx.tipo().registro().variavel()){
                    for(IdentificadorContext idIns : sub.identificador()){
                        TabelaSimbolos.TipoAlguma tipoIns = AlgumaSemanticoUtil.getTipo(sub.tipo().getText());
                        tabela.adicionar(id.getText() + "." + idIns.getText(), tipoIns, TabelaSimbolos.Estrutura.VAR);
                    }
                }
            }
            else if(cTipo == null && tipo == null){
                ArrayList<EntradaTabelaDeSimbolos> arg = tabela.getTipoProp(ctx.tipo().getText());
                if(arg != null){
                    for(TabelaSimbolos.EntradaTabelaDeSimbolos val : arg){
                        tabela.adicionar(id.getText() + "." + val.getNome(), val.getTipo(), TabelaSimbolos.Estrutura.VAR);
                    }
                }
            }
            if(id.getText().contains("[")){
                int ini = id.getText().indexOf("[", 0);
                int end = id.getText().indexOf("]", 0);
                System.out.println("ini = " + (ini+1) + " end = " + (end-1) + " out of " + id.getText());
                String tam;
                if(end-ini == 2)
                    tam = String.valueOf(id.getText().charAt(ini+1));
                else
                    tam = id.getText().substring(ini + 1, end - 1);
                String name = id.IDENT().get(0).getText();
                for(int i = 0; i < Integer.parseInt(tam); i++){
                    System.out.println("Cadastrano " + name + "[" + i + "]");
                    tabela.adicionar(name + "[" + i + "]", tipo, TabelaSimbolos.Estrutura.VAR);
                }

            }
            else{
                tabela.adicionar(id.getText(), tipo, TabelaSimbolos.Estrutura.VAR);
            }
            visitTipo(ctx.tipo());
            // saida.append(id.getText());
            visitIdentificador(id);
            if(cTipo == "char"){
                saida.append("[80]");
            }
            saida.append(";\n");
        }
        return null;
    }

    @Override
    public Void visitTipo(TipoContext ctx) {//visita o tipo para definir se é registro, ou estendido, ou normal
        
        String cTipo = AlgumaSemanticoUtil.getCType(ctx.getText().replace("^", ""));
        TabelaSimbolos.TipoAlguma tipo = AlgumaSemanticoUtil.getTipo(ctx.getText());
        boolean pointer = ctx.getText().contains("^");
        if(cTipo != null){
            saida.append(cTipo);
        }
        else if(ctx.registro() != null){
            visitRegistro(ctx.registro());
        }
        else{
            visitTipo_estendido(ctx.tipo_estendido());
        }
        if(pointer)
            saida.append("*");
        saida.append(" ");

        return null;
    }
    @Override
    public Void visitTipo_estendido(Tipo_estendidoContext ctx) {//imprime tipo estendido, nada mais é que um ponteiro
       
        visitTipo_basico_ident(ctx.tipo_basico_ident());
        if(ctx.getText().contains("^"))
            saida.append("*");
        return null;
    }
    @Override
    public Void visitTipo_basico_ident(Tipo_basico_identContext ctx) {//tipos basicos, ou palavras reservadas, ou variaveis tipo
        
        if(ctx.IDENT() != null){
            saida.append(ctx.IDENT().getText());
        }
        else{
            saida.append(AlgumaSemanticoUtil.getCType(ctx.getText().replace("^", "")));
        }
        return null;
    }

    @Override
    public Void visitRegistro(RegistroContext ctx) {//cria o struct
        
        saida.append("struct {\n");
        ctx.variavel().forEach(var -> visitVariavel(var));
        saida.append("} ");
        return null;
    }

    @Override
    public Void visitDeclaracao_constante(Declaracao_constanteContext ctx) {//declara constante, por meioi do prefixo const
        
        String type = AlgumaSemanticoUtil.getCType(ctx.tipo_basico().getText());
        TabelaSimbolos.TipoAlguma typeVar = AlgumaSemanticoUtil.getTipo(ctx.tipo_basico().getText());
        tabela.adicionar(ctx.IDENT().getText(),typeVar,TabelaSimbolos.Estrutura.VAR);
        saida.append("const " + type + " " + ctx.IDENT().getText() + " = ");
        visitValor_constante(ctx.valor_constante());
        saida.append(";\n");
        return null;
    }

    @Override
    public Void visitValor_constante(Valor_constanteContext ctx) {// retorna o valor, convertendo para sintaxe de c
        
        if(ctx.getText().equals("verdadeiro")){
            saida.append("true");
        }
        else if(ctx.getText().equals("falso")){
            saida.append("false");
        }
        else{
            saida.append(ctx.getText());
        }
        return null;
    }

    @Override
    public Void visitCmd(CmdContext ctx) {// redireciona para o cmd
        if(ctx.cmdLeia() != null){
            visitCmdLeia(ctx.cmdLeia());
        } else if(ctx.cmdEscreva() != null){
            visitCmdEscreva(ctx.cmdEscreva());
        } else if(ctx.cmdAtribuicao() != null){
            visitCmdAtribuicao(ctx.cmdAtribuicao());
        } 
        else if(ctx.cmdSe() != null){
            visitCmdSe(ctx.cmdSe());
        }
        else if(ctx.cmdCaso() != null){
            visitCmdCaso(ctx.cmdCaso());
        }
        else if(ctx.cmdPara() != null){
            visitCmdPara(ctx.cmdPara());
        }
        else if(ctx.cmdEnquanto() != null){
            visitCmdEnquanto(ctx.cmdEnquanto());
        }
        else if(ctx.cmdFaca() != null){
            visitCmdFaca(ctx.cmdFaca());
        }
        else if(ctx.cmdChamada() != null){
            visitCmdChamada(ctx.cmdChamada());
        }
        else if(ctx.cmdRetorne() != null){
            visitCmdRetorne(ctx.cmdRetorne());
        }
        return null;
    }

    @Override
    public Void visitCmdRetorne(CmdRetorneContext ctx) {//adiciona return, e pega a expressao que vai retornar
       
        saida.append("return ");
        visitExpressao(ctx.expressao());
        saida.append(";\n");
        return null;
    }

    @Override
    public Void visitCmdChamada(CmdChamadaContext ctx) {//comando de chamada de função
       
        saida.append(ctx.IDENT().getText() + "(");
        int i = 0;
        for(ExpressaoContext exp : ctx.expressao()){
            if(i++ > 0)
                saida.append(",");
            visitExpressao(exp);
        }
        saida.append(");\n");
        return null;
    }

    @Override
    public Void visitCmdLeia(CmdLeiaContext ctx) {// comando de ler variavel
        for(AlgumaParser.IdentificadorContext id: ctx.identificador()) {
            TabelaSimbolos.TipoAlguma idType = tabela.verificar(id.getText());
            if(idType != TabelaSimbolos.TipoAlguma.CADEIA){
                saida.append("scanf(\"%");
                saida.append(AlgumaSemanticoUtil.getCTypeSymbol(idType));
                saida.append("\", &");
                saida.append(id.getText());
                saida.append(");\n");
            } else {
                saida.append("gets(");
                // saida.append(id.getText());
                visitIdentificador(id);
                saida.append(");\n");
            }
        }
        
        return null;
    }

    @Override
    public Void visitCmdEscreva(CmdEscrevaContext ctx) { // comando para escrever a variavel, verifica o tipo ou na tabela, ou no utils
        for(AlgumaParser.ExpressaoContext exp: ctx.expressao()) {
                Escopo escopo = new Escopo(tabela);
                System.out.println("Searching for " + exp.getText());
                System.out.println("Does it exists in table? " + tabela.existe(exp.getText()));
                String cType = AlgumaSemanticoUtil.getCTypeSymbol(AlgumaSemanticoUtil.verificarTipo(escopo, exp));
                if(tabela.existe(exp.getText())){
                    TabelaSimbolos.TipoAlguma tip = tabela.verificar(exp.getText());
                    cType = AlgumaSemanticoUtil.getCTypeSymbol(tip);
                }
                saida.append("printf(\"%");
                saida.append(cType);
                saida.append("\", ");
                saida.append(exp.getText());
                saida.append(");\n");
        }
        return null;
    }

    @Override
    public Void visitCmdAtribuicao(CmdAtribuicaoContext ctx) {//atribui valores para a variavel, e usa strcpy para strings
        if(ctx.getText().contains("^"))
            saida.append("*");
        try{
            TabelaSimbolos.TipoAlguma tip = tabela.verificar(ctx.identificador().getText());

            if(tip != null && tip == TabelaSimbolos.TipoAlguma.CADEIA){
                // saida.append("strcpy(" + ctx.identificador().getText()+","+ctx.expressao().getText()+");\n");
                saida.append("strcpy(");
                visitIdentificador(ctx.identificador());
                saida.append(","+ctx.expressao().getText()+");\n");
            }
            else{
                // saida.append(ctx.identificador().getText());
                visitIdentificador(ctx.identificador());
                saida.append(" = ");
                saida.append(ctx.expressao().getText());
                saida.append(";\n");
            }
        }
        catch(Exception e){
            System.out.println(e.getMessage() +  " q ocorreu");
        }
        return null;
    }

    @Override
    public Void visitCmdSe(CmdSeContext ctx) {//transcrição do comando if else
        saida.append("if(");
        visitExpressao(ctx.expressao());
        saida.append(") {\n");
        for(CmdContext cmd : ctx.cmd()) {
            visitCmd(cmd);
        }
        saida.append("}\n");
        if(ctx.cmdSenao() != null){
            saida.append("else {\n");
            for(CmdContext cmd : ctx.cmdSenao().cmd()) {
                visitCmd(cmd);
            }
            saida.append("}\n");
        }
        
        return null;
    }

    @Override
    public Void visitExpressao(ExpressaoContext ctx) {//usado para visitar uma expressao, que e constituida de termos e operadores
        if(ctx.termo_logico() != null){
            visitTermo_logico(ctx.termo_logico(0));

            for(int i = 1; i < ctx.termo_logico().size(); i++){
                AlgumaParser.Termo_logicoContext termo = ctx.termo_logico(i);
                saida.append(" || ");
                visitTermo_logico(termo);
            }
        }

        return null;
    }

    @Override
    public Void visitTermo_logico(Termo_logicoContext ctx) {//usado para visitar termos logicos
        visitFator_logico(ctx.fator_logico(0));

        for(int i = 1; i < ctx.fator_logico().size(); i++){
            AlgumaParser.Fator_logicoContext fator = ctx.fator_logico(i);
            saida.append(" && ");
            visitFator_logico(fator);
        }
        
        return null;
    }

    @Override
    public Void visitFator_logico(Fator_logicoContext ctx) {// usado para visitar fatores logicos
        if(ctx.getText().startsWith("nao")){
            saida.append("!");
        }
        visitParcela_logica(ctx.parcela_logica());
        
        return null;
    }

    @Override
    public Void visitParcela_logica(Parcela_logicaContext ctx) {//usado para visitar parcelas logicas
        if(ctx.exp_relacional() != null){
            visitExp_relacional(ctx.exp_relacional());
        } else{
            if(ctx.getText() == "verdadeiro"){
                saida.append("true");
            } else {
                saida.append("false");
            }
        }
        
        return null;
    }

    // usado para expressoes relacionais, convertendo o simbolo de igualdade para o equivalente em c
    @Override
    public Void visitExp_relacional(Exp_relacionalContext ctx) {
         visitExp_aritmetica(ctx.exp_aritmetica(0));
        for(int i = 1; i < ctx.exp_aritmetica().size(); i++){
            AlgumaParser.Exp_aritmeticaContext termo = ctx.exp_aritmetica(i);
            if(ctx.op_relacional().getText().equals("=")){
                saida.append(" == ");
            } else{
                saida.append(ctx.op_relacional().getText());
            }
            visitExp_aritmetica(termo);
        }
        
        return null;
    }

    @Override
    public Void visitExp_aritmetica(Exp_aritmeticaContext ctx) {//visitar expressoes aritmeticas
        visitTermo(ctx.termo(0));

        for(int i = 1; i < ctx.termo().size(); i++){
            AlgumaParser.TermoContext termo = ctx.termo(i);
            saida.append(ctx.op1(i-1).getText());
            visitTermo(termo);
        }
        return null;
    }

    @Override
    public Void visitTermo(TermoContext ctx) {//visita o termo para verificar se tem fatores
       visitFator(ctx.fator(0));

        for(int i = 1; i < ctx.fator().size(); i++){
            AlgumaParser.FatorContext fator = ctx.fator(i);
            saida.append(ctx.op2(i-1).getText());
            visitFator(fator);
        }
        return null;
    }

    @Override
    public Void visitFator(FatorContext ctx) {//visita o fator para verificar se tem parcelas
        visitParcela(ctx.parcela(0));

        for(int i = 1; i < ctx.parcela().size(); i++){
            AlgumaParser.ParcelaContext parcela = ctx.parcela(i);
            saida.append(ctx.op3(i-1).getText());
            visitParcela(parcela);
        }
        return null;
    }

    @Override
    public Void visitParcela(ParcelaContext ctx) {//redireciona parcela para unaria ou nao unaria
        if(ctx.parcela_unario() != null){
            if(ctx.op_unario() != null){
                saida.append(ctx.op_unario().getText());
            }
            visitParcela_unario(ctx.parcela_unario());
        } else{
            visitParcela_nao_unario(ctx.parcela_nao_unario());
        }
        
        return null;
    }

    @Override
    public Void visitParcela_unario(Parcela_unarioContext ctx) {
        //visitar parcela unario imprimindo todos os identificadores, ou redireciona caso chegou aqui com uma expressao ent
        if(ctx.IDENT() != null){
            saida.append(ctx.IDENT().getText());
            saida.append("(");
            for(int i = 0; i < ctx.expressao().size(); i++){
                visitExpressao(ctx.expressao(i));
                if(i < ctx.expressao().size()-1){
                    saida.append(", ");
                }
            }
        } else if(ctx.parentesis_expressao() != null){
            saida.append("(");
            visitExpressao(ctx.parentesis_expressao().expressao());
            saida.append(")");
        }
        else {
            saida.append(ctx.getText());
        }
        
        return null;
    }

    @Override
    public Void visitParcela_nao_unario(Parcela_nao_unarioContext ctx) {//parcela nao unaria é só o valor do campo
       
        saida.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitCmdCaso(CmdCasoContext ctx) {//switch case, tratando intervalos, com visita a expressao aritmetica
        
        saida.append("switch(");
        visit(ctx.exp_aritmetica());
        saida.append("){\n");
        visit(ctx.selecao());
        if(ctx.cmdSenao() != null){
            visit(ctx.cmdSenao());
        }
        saida.append("}\n");
        return null;
    }
    @Override
    public Void visitSelecao(SelecaoContext ctx) {//visita todas os itens da selecao
        
        ctx.item_selecao().forEach(var -> visitItem_selecao(var));
        return null;
    }
    @Override
    public Void visitItem_selecao(Item_selecaoContext ctx) {// cadda item deve ser tratado para caso seja um intervalo imprima todos os cases do mesmo
       
        ArrayList<String> intervalo = new ArrayList<>(Arrays.asList(ctx.constantes().getText().split("\\.\\.")));
        String first = intervalo.size() > 0 ? intervalo.get(0) : ctx.constantes().getText();
        String last = intervalo.size() > 1 ? intervalo.get(1) : intervalo.get(0);
        for(int i = Integer.parseInt(first); i <= Integer.parseInt(last); i++){
            saida.append("case " + i + ":\n");
            ctx.cmd().forEach(var -> visitCmd(var));
            saida.append("break;\n");
        }
        return null;
    }
    @Override
    public Void visitCmdSenao(CmdSenaoContext ctx) {//o senao é traduzido como default em c para o cmdcaso
       
        saida.append("default:\n");
        ctx.cmd().forEach(var -> visitCmd(var));
        saida.append("break;\n");
        return null;
    }

    @Override
    public Void visitCmdPara(CmdParaContext ctx) {//criando loop for, ate o valor passado depois do literall ate
      
        String id = ctx.IDENT().getText();
        saida.append("for(" + id + " = ");
        visitExp_aritmetica(ctx.exp_aritmetica(0));
        saida.append("; " + id + " <= ");
        visitExp_aritmetica(ctx.exp_aritmetica(1));
        saida.append("; " + id + "++){\n");
        ctx.cmd().forEach(var -> visitCmd(var));
        saida.append("}\n");
        return null;
    }

    @Override
    public Void visitCmdEnquanto(CmdEnquantoContext ctx) {//cmd enquando loop while em c
      
        saida.append("while(");
        visitExpressao(ctx.expressao());
        saida.append("){\n");
        ctx.cmd().forEach(var -> visitCmd(var));
        saida.append("}\n");
        return null;
    }

    @Override
    public Void visitCmdFaca(CmdFacaContext ctx) {//comando faca loop do while em c
        
        saida.append("do{\n");
        ctx.cmd().forEach(var -> visitCmd(var));
        saida.append("} while(");
        visitExpressao(ctx.expressao());
        saida.append(");\n");
        return null;
    }


}

