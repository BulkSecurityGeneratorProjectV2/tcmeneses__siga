package br.gov.jfrj.siga.integration.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
//Necess�rio para o saucelabs 
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

//As libs abaixo s�o necess�rias para que o sessionid seja passado ao jenkins ou bamboo
//e tamb�m para que o testlistener invoque o sauce rest api no qual notificar� o sauce
//se o teste passou ou n�o
import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.testng.SauceOnDemandAuthenticationProvider;
import com.saucelabs.testng.SauceOnDemandTestListener;
// fim saucelabs 

import br.gov.jfrj.siga.integration.test.util.IntegrationTestUtil;
import br.gov.jfrj.siga.page.objects.AnexoPage;
import br.gov.jfrj.siga.page.objects.AssinaturaAnexoPage;
import br.gov.jfrj.siga.page.objects.AssinaturaDigitalPage;
import br.gov.jfrj.siga.page.objects.CancelamentoMovimentacaoPage;
import br.gov.jfrj.siga.page.objects.LoginPage;
import br.gov.jfrj.siga.page.objects.OperacoesDocumentoPage;
import br.gov.jfrj.siga.page.objects.ProcessoAssuntosAdministrativosPage;
import br.gov.jfrj.siga.page.objects.RegistraAssinaturaManualPage;
import br.gov.jfrj.siga.page.objects.TransferenciaPage;

//O listener envia o resultado do testng para o saucelab
@Listeners({SauceOnDemandTestListener.class})
public class IntegrationTestBase implements SauceOnDemandSessionIdProvider, SauceOnDemandAuthenticationProvider {
	protected WebDriver driver;
	protected OperacoesDocumentoPage operacoesDocumentoPage;
	protected IntegrationTestUtil util;
	protected String baseURL;
	protected Properties propDocumentos = new Properties();
	//Necess�rio para o saucelabs  
	public SauceOnDemandAuthentication authentication;

	public IntegrationTestBase() throws FileNotFoundException, IOException {
		this.baseURL = System.getProperty("baseURL");
		util = new IntegrationTestUtil();
		File file = new File(System.getProperty("infoDocumentos"));
		propDocumentos.load(new FileInputStream(file));
		propDocumentos.setProperty("siglaSubscritor", System.getProperty("userSiga"));
	}
	
	// Necess�rio para o saucelabs
	@BeforeClass
	@Parameters({"Operating_System", "Browser_Name", "Browser_Version"})
	public void iniciaWebDriver(@Optional() String operatingSystem, @Optional() String browserName, @Optional() String browserVersion) throws MalformedURLException { 
		// Necess�rio para acesso ao saucelabs
		System.out.println("================> OperatingSystem: " + operatingSystem);
		System.out.println("================> browserName: " + browserName);
		System.out.println("================> browserVersion: " + browserVersion);
		String USUARIO_SAUCELAB = "sigadoc";
		String ACCESSKEY_SAUCELAB = "6b7f5b5c-0a1e-4c59-b9b5-e3dba4a198d8";
		authentication = new SauceOnDemandAuthentication(USUARIO_SAUCELAB , ACCESSKEY_SAUCELAB);
		DesiredCapabilities capabilities = new DesiredCapabilities();
	    capabilities.setBrowserName(browserName);
		capabilities.setCapability("version", browserVersion);
	    System.out.println("Dados: " + operatingSystem + " - " + browserName + " - " + browserVersion );
	    capabilities.setCapability("platform", Platform.valueOf(operatingSystem));
	    capabilities.setCapability("screen-resolution", "1024x768");
	    capabilities.setCapability("name", "teste SIGA-DOC selenium-testng-saucelab");
	    this.driver = new RemoteWebDriver(
	           new URL("http://" + USUARIO_SAUCELAB + ":" + ACCESSKEY_SAUCELAB + "@ondemand.saucelabs.com:80/wd/hub"),
	           capabilities);
		// Fim do bloco necess�rio ao acesso ao saucelabs
	}
	
	
	public void efetuaLogin() {
		try {					    			
			driver.get(baseURL + "/siga");
			driver.manage().window().maximize();
			LoginPage loginPage = PageFactory.initElements(driver,	LoginPage.class);
			loginPage.login(System.getProperty("userSiga"), System.getProperty("passSiga"));
			util.getWebElement(driver, By.cssSelector("a.gt-btn-small.gt-btn-right"));
		} catch (Exception e) {
			e.printStackTrace();
			driver.quit();
		}
	}
	
	public void efetuaLogout() {
		try {			
			IntegrationTestUtil util = new IntegrationTestUtil();
			WebElement linkSair = util.getWebElement(driver, By.linkText("sair"));
			new WebDriverWait(driver, 30).until(ExpectedConditions.elementToBeClickable(linkSair));
			linkSair.click();
			util.getWebElement(driver, By.id("j_username"));
		} catch (Exception e) {
			System.out.println("Erro ao efetuar logout!");
		} finally {
			driver.quit();
		}
	}
	
	public void assinarAnexo(String codigoDocumento) {
		// Clicar em "Assinar/Autenticar"
		operacoesDocumentoPage.clicarLinkAssinarCopia();
		
		// Garantir que a String "Link para assinatura externa" apare�a na tela - Assinar anexo
		AssinaturaAnexoPage assinaturaAnexoPage = PageFactory.initElements(driver, AssinaturaAnexoPage.class);
		assinaturaAnexoPage.assinarCopia(baseURL, codigoDocumento);
		Assert.assertNotNull(util.getWebElement(driver, By.xpath("//td[4][contains(., 'Assinado por')]")), "O texto 'Assinado por' n�o foi encontrado!");
	}
	
	public void autuar(Boolean isDigital, String modeloDocumento){
		operacoesDocumentoPage.clicarLinkAutuar();
		ProcessoAssuntosAdministrativosPage processoAssuntosAdministrativosPage = PageFactory.initElements(driver, ProcessoAssuntosAdministrativosPage.class);
		processoAssuntosAdministrativosPage.criaProcesso(propDocumentos, isDigital, modeloDocumento);
		Assert.assertNotNull(util.getWebElement(driver, By.xpath("//b[contains(., 'Processo N�')]")), "Texto 'Processo N�' n�o foi encontrado!");		
	}
	
	public void finalizarProcesso() {
		operacoesDocumentoPage.clicarLinkFinalizar();
		
		Assert.assertNotNull(util.getWebElement(driver, By.xpath(OperacoesDocumentoPage.XPATH_STATUS_DOCUMENTO + 
		 "[contains(text(), '1� Volume - Pendente de Assinatura, Como Subscritor')]|//div[h3 = 'Volumes']/ul/li[contains(., 'Pendente de Assinatura') and contains(., 'Como Subscritor')]")), "Texto '1� Volume - Pendente de Assinatura, Como Subscritor' n�o foi encontrado!");		
	}
	
	public void finalizarDocumento() {
		operacoesDocumentoPage.clicarLinkFinalizar();		
	
		Assert.assertNotNull(util.getWebElement(driver, By.xpath(OperacoesDocumentoPage.XPATH_STATUS_DOCUMENTO + 
				 "[contains(text(), 'Pendente de Assinatura, Como Subscritor')]|//div[h3 = 'Vias']/ul/li[contains(., 'Pendente de Assinatura') and contains(., 'Como Subscritor')]")), "Texto Pendente de Assinatura, Como Subscritor n�o foi encontrado!");		
	}
	
	public void validaDesentranhamento(String codigoProcesso) {		
		// Clicar em Exibir Informa��es completas
		operacoesDocumentoPage.clicarLinkExibirInformacoesCompletas();
		
		// Acessar novamente o processo, pelo link existente na linha do evento de juntada
		WebElement desentranhamentoDocumento = util.getWebElement(driver, By.xpath("//tr[contains(@class, 'desentranhamento ')]"));
		Assert.assertNotNull(desentranhamentoDocumento, "Evento de desentranhamento n�o encontrado!");
		WebElement linkProcessoDesentranhado = util.getWebElement(driver, desentranhamentoDocumento, By.partialLinkText(codigoProcesso));
		linkProcessoDesentranhado.click();
		
		// Clicar em Exibir informa��es completas
		operacoesDocumentoPage.clicarLinkExibirInformacoesCompletas();
		
		// Garantir que o texto "Desentranhamento" apare�a na tela
		WebElement desentranhamentoProcesso = util.getWebElement(driver, By.xpath("//tr[contains(@class, 'desentranhamento ')]"));
		Assert.assertNotNull(desentranhamentoProcesso, "Evento de desentranhamento n�o encontrado!");	
	}
	
	public void cancelarAnexo() {
		// Clicar em "Cancelar" (link no <tr> do evento de anexa��o)
		operacoesDocumentoPage.clicarLinkCancelarAnexo();
		
		// Informar um motivo qualquer e um subscritor qualquer, diferente do usu�rio de teste
		CancelamentoMovimentacaoPage cancelamentoMovimentacaoPage = PageFactory.initElements(driver, CancelamentoMovimentacaoPage.class);
		cancelamentoMovimentacaoPage.cancelarMovimentacao(propDocumentos);
		
		// Garantir que o nome do anexo n�o apare�a mais na tela
		String nomeArquivo = propDocumentos.getProperty("arquivoAnexo");
		Assert.assertTrue(util.isElementInvisible(driver, By.linkText(nomeArquivo.toLowerCase())), "Nome do arquivo continua vis�vel na tela!");
		
		// Clicar "Visualizar Dossi�"
		operacoesDocumentoPage.clicarLinkVisualizarDossie();
		
		// Garantir que o nome do anexo n�o apare�a mais na tela
		Assert.assertTrue(util.isElementInvisible(driver, By.linkText(nomeArquivo.substring(0, nomeArquivo.indexOf(".")).toLowerCase())), 
				"Nome do anexo continua vis�vel na visualiza��o de dossi�!");
	}
	
	public void encerrarVolume() {
		// Clicar em "Encerrar Volume"
		operacoesDocumentoPage.clicarLinkEncerrarVolume();
		
		// Garantir que o texto "Encerramento de Volume" apare�a na tela. 
		Assert.assertNotNull(util.getWebElement(driver, By.xpath("//td[3][contains(text(),'Encerramento de Volume')]")));
		
		// Clicar em "Despachar/Transferir"
		operacoesDocumentoPage.clicarLinkDespacharTransferir();
		
		// Selecionar um despacho qualquer - Clicar "OK" - Garantir que o texto "N�o � permitido" apare�a na tela - Fechar a popup
		TransferenciaPage transferenciaPage = PageFactory.initElements(driver, TransferenciaPage.class);
		Assert.assertFalse(transferenciaPage.despacharVolumeEncerrado(propDocumentos), "O despacho de volume encerrado foi permitido!");			
	}
	
	public void anexarArquivo(String nomeArquivo) {
		// Clicar no link "Anexar Arquivo"
		operacoesDocumentoPage.clicarLinkAnexarArquivo();
		util.getWebElement(driver, By.xpath("//h2[contains(text(), 'Anexa��o de Arquivo')]"));
		// Clicar "OK" - Selecionar um arquivo qualquer - Clicar "OK"
		AnexoPage anexoPage = PageFactory.initElements(driver, AnexoPage.class);
		anexoPage.anexarArquivo(propDocumentos);
		
		// Garantir que o nome do arquivo selecionado apare�a na tela
		Assert.assertNotNull(util.getWebElement(driver, By.linkText(nomeArquivo.toLowerCase())), "Nome do arquivo selecionado n�o encontrado na tela!");
		
		// Clicar em voltar
		anexoPage.clicarBotaovoltar();
	}
	
	public void registrarAssinaturaManual() {
		operacoesDocumentoPage.clicarLinkRegistrarAssinaturaManual();
		RegistraAssinaturaManualPage registraAssinaturaManualPage = PageFactory.initElements(driver, RegistraAssinaturaManualPage.class);
		registraAssinaturaManualPage.registarAssinaturaManual();
		Assert.assertNotNull(util.getWebElement(driver, By.xpath(OperacoesDocumentoPage.XPATH_STATUS_DOCUMENTO + "[contains(text(), 'Aguardando Andamento')]|//div[h3 = 'Volumes' or h3 = 'Vias']/ul/li[contains(., 'Aguardando Andamento')]")), "Texto 'Aguardando Andamento' n�o encontrado!");				
	}
	
	public void assinarDigitalmente(String codigoDocumento, String textoBuscado) {
		// Clicar em Assinar Digitalmente
		operacoesDocumentoPage.clicarLinkAssinarDigitalmente();
		
		// Garantir que a descri��o do documento apare�a na tela (� a se��o OBJETO, da capa do processo)
		Assert.assertNotNull(util.getWebElement(driver, By.xpath("//p[contains(., '" + textoBuscado + "')]")), "Texto '" + textoBuscado + " ' n�o encontrado!");
			
		// usar o link /sigaex/expediente/mov/simular_assinatura?sigla=<c�digo do documento> para gerar uma movimenta��o de assinatura digital
		AssinaturaDigitalPage assinaturaDigitalPage = PageFactory.initElements(driver, AssinaturaDigitalPage.class);
		assinaturaDigitalPage.registrarAssinaturaDigital(baseURL, codigoDocumento);
		
		// Garantir que "Aguardando Andamento" apare�a na tela
		Assert.assertNotNull(util.getWebElement(driver, By.xpath(OperacoesDocumentoPage.XPATH_STATUS_DOCUMENTO + "[contains(text(), 'Aguardando Andamento')]|//div[h3 = 'Volumes' or h3 = 'Vias']/ul/li[contains(., 'Aguardando Andamento')]")), "Texto 'Aguardando Andamento' n�o encontrado!");					
	}		
	
	@AfterClass(alwaysRun = true)
	public void tearDown() {
		driver.quit();
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