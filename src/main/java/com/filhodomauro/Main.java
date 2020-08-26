package com.filhodomauro;

import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.certificado.CertificadoService;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import br.com.swconsultoria.nfe.Nfe;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.Evento;
import br.com.swconsultoria.nfe.dom.EventoEpec;
import br.com.swconsultoria.nfe.dom.enuns.AmbienteEnum;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import br.com.swconsultoria.nfe.exception.NfeException;
import br.com.swconsultoria.nfe.schema.envEpec.TEnvEvento;
import br.com.swconsultoria.nfe.schema.envEpec.TRetEnvEvento;
import br.com.swconsultoria.nfe.util.EpecUtil;
import br.com.swconsultoria.nfe.util.RetornoUtil;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Objects;

public class Main {

    /**
     * Projeto referência -> https://github.com/Samuel-Oliveira/Java_NFe
     * Exemplo de geração -> https://github.com/Samuel-Oliveira/Java_NFe/wiki/14-:-Envio-Epec
     * @param args
     */
    public static void main(String[] args) {
        try {
            // Inicia As Configurações - ver https://github.com/Samuel-Oliveira/Java_NFe/wiki/1-:-Configuracoes
            ConfiguracoesNfe config = iniciaConfigurações();

            //Agora o evento pode aceitar uma lista de cancelaemntos para envio em Lote.
            //Para isso Foi criado o Objeto Epec
            Evento epec = new Evento();
            //Informe a chave da Epec
            epec.setChave("52190310732644000128550010000125491000125491");
            //Informe o CNPJ do emitente
            epec.setCnpj("99999999999999");
            //Informe a data do EPEC
            epec.setDataEvento(LocalDateTime.now());
            //Preenche os Dados do Evento EPEC
            EventoEpec eventoEpec = new EventoEpec();
            eventoEpec.setCnpjDestinatario("X");
            eventoEpec.setvST("X");
            eventoEpec.setvNF("X");
            eventoEpec.setvICMS("X");
            eventoEpec.setTipoNF("X");
            eventoEpec.setIeEmitente("X");
            eventoEpec.setIeDestinatario("X");
            eventoEpec.setEstadoDestinatario(EstadosEnum.SP);
            epec.setEventoEpec(eventoEpec);

            //Monta o Evento de Cancelamento
            TEnvEvento enviEvento = EpecUtil.montaEpec(epec,config);

            //Envia Evento EPEC
            TRetEnvEvento retorno = Nfe.enviarEpec(config, enviEvento, true);

            //Valida o Retorno do Cancelamento
            RetornoUtil.validaEpec(retorno);

            //Resultado
            System.out.println();
            retorno.getRetEvento().forEach( resultado -> {
                System.out.println("# Chave: " + resultado.getInfEvento().getChNFe());
                System.out.println("# Status: " + resultado.getInfEvento().getCStat() + " - " + resultado.getInfEvento().getXMotivo());
                System.out.println("# Protocolo: " + resultado.getInfEvento().getNProt());
            });
            //Cria ProcEvento de Cacnelamento
            //String proc = EpecUtil.criaProcEventoEpec(config, enviEvento, retorno);
            System.out.println();
            //System.out.println("# ProcEvento : " + proc);

        } catch (Exception e) {
            System.err.println();
            System.err.println("# Erro: "+e.getMessage());
        }
    }

    public static ConfiguracoesNfe iniciaConfigurações() throws NfeException, FileNotFoundException, CertificadoException, URISyntaxException {
        URI uri = Objects.requireNonNull(Main.class.getClassLoader().getResource("CertificadoTesteCNPJ.pfx")).toURI();

        Certificado certificado = CertificadoService.certificadoPfx(Paths.get(uri).toString(), "123456");

        String path =
                Objects.requireNonNull(
                        Main.class.getClassLoader().getResource("schemas/envEPEC_v1.00.xsd"))
                        .getPath().replace("envEPEC_v1.00.xsd","");

        System.out.println(path);

        return ConfiguracoesNfe.criarConfiguracoes(EstadosEnum.SP , AmbienteEnum.PRODUCAO,certificado, path);
    }
}
