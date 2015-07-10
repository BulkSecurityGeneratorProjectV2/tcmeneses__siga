package br.gov.jfrj.siga.integration.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;


//Bibliotecas para o saucelabs
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import br.gov.jfrj.siga.page.objects.AssinaturaDigitalPage;
import br.gov.jfrj.siga.page.objects.JuntadaDocumentoPage;
import br.gov.jfrj.siga.page.objects.OficioPage;
import br.gov.jfrj.siga.page.objects.OperacoesDocumentoPage;
import br.gov.jfrj.siga.page.objects.PrincipalPage;
import br.gov.jfrj.siga.page.objects.ProcessoFinanceiroPage;
import br.gov.jfrj.siga.page.objects.TransferenciaPage;
import br.gov.jfrj.siga.page.objects.VisualizacaoDossiePage;

import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.testng.SauceOnDemandAuthenticationProvider;
//fim saucelabs

//O listener envia o resultado do testng para o saucelab
//@Listeners({SauceOnDemandTestListener.class})
public class ProcessoAdministrativoFisicoIT extends IntegrationTestBase implements SauceOnDemandSessionIdProvider, SauceOnDemandAuthenticationProvider {	
	private String codigoDocumento;
	private String codigoProcesso;
	
	public ProcessoAdministrativoFisicoIT() throws FileNotFoundException, IOException {
		super();
	}
	
	@BeforeClass(dependsOnMethods={"iniciaWebDriver"})
	public void setUp() {
		try{
			PrincipalPage principalPage =  efetuaLogin();			
			principalPage.clicarBotaoNovoDocumentoEx();

			OficioPage oficioPage = PageFactory.initElements(driver, OficioPage.class);
			operacoesDocumentoPage = oficioPage.criaOficio(propDocumentos);		
			codigoDocumento = operacoesDocumentoPage.getTextoVisualizacaoDocumento();
						
			operacoesDocumentoPage.clicarLinkFinalizar();
			codigoDocumento = operacoesDocumentoPage.getTextoVisualizacaoDocumento();
			
			AssinaturaDigitalPage assinaturaDigitalPage = operacoesDocumentoPage.clicarLinkAssinarDigitalmente();			
			operacoesDocumentoPage = assinaturaDigitalPage.registrarAssinaturaDigital(baseURL, codigoDocumento);			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exce��o no m�todo setUp!");
		}
	}
	
	@BeforeMethod
	public void paginaInicial(Method method) {
		try {
			System.out.println("BeforeMethod: " + method.getName() + " - Titulo p�gina: " + driver.getTitle());
			if(!driver.getCurrentUrl().contains("exibir.action") || driver.getTitle().contains("SIGA - Erro Geral")) {
				System.out.println("Efetuando busca!");
				
				if(codigoProcesso != null) {	
					driver.get(baseURL + "/sigaex/expediente/doc/exibir.action?sigla=" + codigoProcesso);		
				} else {
					driver.get(baseURL + "/sigaex/expediente/doc/exibir.action?sigla=" + codigoDocumento);		
				}	
			}
			
			codigoProcesso = operacoesDocumentoPage.getTextoVisualizacaoDocumento();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test(enabled = true)
	public void autuarProcesso(){
		super.autuar(Boolean.FALSE, "Processo de Outros Assuntos Administrativos");	
	}
	
	@Test(enabled = true, priority = 1, dependsOnMethods = {"autuarProcesso"})
	public void visualizarImpressao() throws Exception {
		// Clicar em Visualizar Impress�o - Garantir que n�o retorne um erro
		Assert.assertTrue(operacoesDocumentoPage.clicarLinkVisualizarImpressao());
	}
	
	@Test(enabled = true, priority = 1, dependsOnMethods = {"autuarProcesso"})
	public void finalizar() {
		super.finalizarProcesso();
	}
	
	@Test(enabled = true, priority = 2, dependsOnMethods = {"finalizar"})
	public void registrarAssinaturaManualProcesso() {
		super.registrarAssinaturaManual();
	}
	
	@Test(enabled = true, priority = 3, dependsOnMethods = {"registrarAssinaturaManualProcesso"})
	public void juntar() {
		// Acessar o documento anterior
		driver.get(baseURL + "/sigaex/expediente/doc/exibir.action?sigla=" + codigoDocumento);	
		operacoesDocumentoPage = PageFactory.initElements(driver, OperacoesDocumentoPage.class);
/*		PrincipalPage principalPage = PageFactory.initElements(driver, PrincipalPage.class);
		principalPage.buscarDocumento(codigoDocumento);*/
		
		// Clicar em "Juntar"
		JuntadaDocumentoPage juntadaDocumentoPage = operacoesDocumentoPage.clicarlinkJuntar();
		
		// Selecionar o processo - Clicar "OK"
		operacoesDocumentoPage = juntadaDocumentoPage.juntarDocumento(propDocumentos, codigoProcesso);	
		
		// Clicar no link com o n�mero do processo ao qual o documento foi juntado para retornar � visualiza��o das movimenta��es do processo
		operacoesDocumentoPage = operacoesDocumentoPage.clicarLinkDocumentoJuntado(codigoProcesso);
		
		// Clicar em "Visualizar Dossi�"
		VisualizacaoDossiePage visualizacaoDossiePage = operacoesDocumentoPage.clicarLinkVisualizarDossie();
		
		// Garantir que alguma parte do texto do documento juntado apare�a na tela	
		Assert.assertTrue(visualizacaoDossiePage.visualizaConteudoDocumento(codigoDocumento), "Conte�do do documento juntado n�o encontrado!");
		operacoesDocumentoPage = visualizacaoDossiePage.clicarLinkVisualizarMovimentacoes();
	}
	
	@Test(enabled = true, priority = 4, dependsOnMethods = {"juntar"})
	public void cancelarJuntada() {
		// Acessar o documento juntado, por meio do link existente no TR do evento de juntada
/*		WebElement linkDocumentoJuntado = util.getClickableElement(driver, By.partialLinkText(codigoDocumento));
		linkDocumentoJuntado.click();*/
		operacoesDocumentoPage = operacoesDocumentoPage.clicarLinkDocumentoJuntado(codigoDocumento);
		
		// Clicar em "Desentranhar"
		operacoesDocumentoPage = operacoesDocumentoPage.clicarLinkDesentranhar();
		Assert.assertTrue(operacoesDocumentoPage.isDocumentoJuntadoInvisivel(), "Evento de juntada continua vis�vel!");
		
		validaDesentranhamento(codigoProcesso);		
	}
	
	@Test(enabled = true, priority = 2, dependsOnMethods = {"finalizar"})
	public void anexarArquivo() {
		String nomeArquivo = propDocumentos.getProperty("arquivoAnexo");
		super.anexarArquivo(nomeArquivo);
		
		// Clicar em "Visualizar Dossi�"
		VisualizacaoDossiePage visualizacaoDossiePage = operacoesDocumentoPage.clicarLinkVisualizarDossie();
		
		// Garantir que o nome do anexo apare�a na tela (� a se��o OBJETO, da capa do processo)
		//Assert.assertNotNull(util.getWebElement(driver, By.linkText(nomeArquivo.substring(0, nomeArquivo.indexOf(".")).toLowerCase())), "Nome do arquivo selecionado n�o encontrado na visualiza��o do Dossi�!");
		Assert.assertNotNull(visualizacaoDossiePage.isAnexoVisivel(nomeArquivo.toLowerCase()), "Nome do arquivo selecionado n�o encontrado na visualiza��o do Dossi�!");

		// Clicar em "Visualizar Movimenta��es"
		visualizacaoDossiePage.clicarLinkVisualizarMovimentacoes();
	}
	
	@Test(enabled = true, priority = 3, dependsOnMethods = {"anexarArquivo"})
	public void assinarAnexo() {
		super.assinarAnexo(codigoProcesso);
	}
	
	@Test(enabled = false, priority = 4)
	public void cancelarAnexo() {
		super.cancelarAnexo();
		
		// Clicar em "Visualizar Movimenta��es"
		VisualizacaoDossiePage visualizacaoDossiePage = PageFactory.initElements(driver, VisualizacaoDossiePage.class);
		visualizacaoDossiePage.clicarLinkVisualizarMovimentacoes();		
	}
	
	@Test(enabled = true, priority = 4, dependsOnMethods = {"cancelarJuntada"} )
	public void encerrarVolumeProcesso() {
		super.encerrarVolume();
	}
	
	@Test(enabled = true, priority = 5, dependsOnMethods = {"encerrarVolumeProcesso"})
	public void criarVolume() {
		// Clicar em "Abrir Novo Volume"
		operacoesDocumentoPage = operacoesDocumentoPage.clicarLinkAbrirNovoVolume();
						
		// Garantir que os textos "1� Volume - Apensado" e "2� Volume - Aguardando Andamento" apare�am na tela	
/*		WebElement volume1 = util.getWebElement(driver, By.xpath("//div[h3 = 'Volumes']/ul/li[1][contains(., 'Apensado')]"));
		WebElement volume2 = util.getWebElement(driver, By.xpath("//div[h3 = 'Volumes']/ul/li[2][contains(., 'Aguardando Andamento')]"));*/
		
		Assert.assertTrue(operacoesDocumentoPage.isNovoVolumeVisivel("Apensado", "Aguardando Andamento"), "Textos 'V01  -  Apensado' e 'V02  -  Aguardando Andamento' n�o encontrados!");
		
		// Clicar sobre a segunda ocorr�ncia do link "Despachar/Transferir"
		TransferenciaPage transferenciaPage = operacoesDocumentoPage.clicarLinkDespacharTransferir();
		
		// Selecionar um atendente qualquer - Clicar "OK"
		transferenciaPage.transferirDocumento(propDocumentos);
		
		// Garantir que os textos "1� Volume - Apensado" e "2� Volume - Caixa de Entrada (Digital)"		
/*		volume1 = util.getWebElement(driver, By.xpath("//div[h3 = 'Volumes']/ul/li[1][contains(., 'Apensado')]"));
		volume2 = util.getWebElement(driver, By.xpath("//div[h3 = 'Volumes']/ul/li[2][contains(., 'A Receber (F�sico)')]"));*/
		
		Assert.assertTrue(operacoesDocumentoPage.isNovoVolumeVisivel("Apensado", "A Receber (F�sico)"), "Textos 'V01  -  Apensado' e 'V02  -  A Receber (F�sico)' n�o encontrados!");
		
		// Clicar em "Desfazer Transfer�ncia"
		operacoesDocumentoPage = operacoesDocumentoPage.clicarLinkDesfazerTransferencia();
		
		// Garantir que "2� Volume - Aguardando Andamento" apare�a na tela
		Assert.assertTrue(operacoesDocumentoPage.isVolumeAssinado(),
				"'Texto 2� Volume - Aguardando Andamento' n�o encontrado!");
	}
	
	@Test(enabled = true, priority = 6, dependsOnMethods = {"criarVolume"})
	public void criarSubprocesso() {
		// Clicar em "Criar Subprocesso"
		ProcessoFinanceiroPage processoFinanceiroPage = operacoesDocumentoPage.clicarLinkCriarSubprocesso();
		
		// Selecionar um subscritor qualquer - Clicar "OK"
		operacoesDocumentoPage = processoFinanceiroPage.criaProcessoFinanceiro(propDocumentos, Boolean.FALSE, "Processo de Execu��o Or�ament�ria e Financeira");
		
		// Clicar em Finalizar
		operacoesDocumentoPage = operacoesDocumentoPage.clicarLinkFinalizar();
		
		// Garantir que o texto "<N�mero do processo principal>.01" apare�a na tela 
		Assert.assertNotNull(operacoesDocumentoPage.isNumeroProcessoVisivel(codigoProcesso + ".01"));		
	}
	// os m�todos abaixo s�o necess�rios para implementar as interfaces SauceOnDemandSessionIdProvider, SauceOnDemandAuthenticationProvider
	@Override
	public String getSessionId() {
	    SessionId sessionId = ((RemoteWebDriver)driver).getSessionId();
	    return (sessionId == null) ? null : sessionId.toString();
	}

	@Override
	public SauceOnDemandAuthentication getAuthentication() {
	    return authentication;
	}
}