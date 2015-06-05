package br.gov.jfrj.siga.integration.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;

import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
//Bibliotecas para o saucelabs
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.testng.annotations.Listeners;

import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.testng.SauceOnDemandAuthenticationProvider;
import com.saucelabs.testng.SauceOnDemandTestListener;
// fim saucelabs

import br.gov.jfrj.siga.page.objects.OperacoesDocumentoPage;
import br.gov.jfrj.siga.page.objects.PortariaPage;
import br.gov.jfrj.siga.page.objects.PrincipalPage;
import br.gov.jfrj.siga.page.objects.TransferenciaPage;

//O listener envia o resultado do testng para o saucelab
@Listeners({SauceOnDemandTestListener.class})
public class AcoesDocumentoDigitalIT extends IntegrationTestBase implements SauceOnDemandSessionIdProvider, SauceOnDemandAuthenticationProvider {
	private String codigoDocumento;
	
	public AcoesDocumentoDigitalIT() throws FileNotFoundException, IOException {
		super();
	}
	
	@BeforeClass(dependsOnMethods={"iniciaWebDriver"})
	public void setUp() {
		try {
			efetuaLogin();
			PrincipalPage principalPage = PageFactory.initElements(driver, PrincipalPage.class);
			operacoesDocumentoPage = PageFactory.initElements(driver, OperacoesDocumentoPage.class);

			principalPage.clicarBotaoNovoDocumentoEx();			
			PortariaPage portariaPage = PageFactory.initElements(driver, PortariaPage.class);
			portariaPage.criaPortaria(propDocumentos);			
			codigoDocumento = operacoesDocumentoPage.getTextoVisualizacaoDocumento("/html/body/div[4]/div/h2");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SkipException("Exce��o no m�todo setUp!");
		} 
	}
	
	@BeforeMethod
	public void paginaInicial(Method method) {
		try {
			System.out.println("BeforeMethod: " + method.getName() + " - Titulo p�gina: " + driver.getTitle());
			if(!driver.getCurrentUrl().contains("exibir.action") || driver.getTitle().contains("SIGA - Erro Geral")) {
				System.out.println("Efetuando busca!");
				driver.get(baseURL + "/sigaex/expediente/doc/exibir.action?sigla=" + codigoDocumento);			
			}
			
			codigoDocumento = operacoesDocumentoPage.getTextoVisualizacaoDocumento("/html/body/div[4]/div/h2");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test(enabled = true, priority = 1)
	public void finalizarDocumento() {
		super.finalizarDocumento(); 
	}
	
	@Test(enabled = true, priority = 2)
	public void assinarDigitalmente() {
		super.assinarDigitalmente(codigoDocumento, "N�");
		//Assert.assertNotNull(util.getWebElement(driver, By.xpath("//td[2][contains(., 'Assinatura')]")), "Linha de registro da assinatura n�o encontrada!");
	}
	
	@Test(enabled = true, priority = 3)
	public void anexarArquivo() {
		super.anexarArquivo(propDocumentos.getProperty("arquivoAnexo"));
		
		Assert.assertNotNull(util.getWebElement(driver, By.xpath(OperacoesDocumentoPage.XPATH_STATUS_DOCUMENTO + 
				"[contains(text(), 'Anexo Pendente Assinatura/Confer�ncia')]|//div[h3 = 'Vias']/ul/li[contains(., 'Anexo Pendente Assinatura/Confer�ncia')]")), "Texto 'Anexo Pendente de Assinatura/Confer�ncia' n�o foi encontrado!");		
	}
	
	@Test(enabled = true, priority = 3)
	public void despacharDocumento() {
		operacoesDocumentoPage.clicarLinkDespacharTransferir();
		TransferenciaPage transferenciaPage = PageFactory.initElements(driver, TransferenciaPage.class);
		transferenciaPage.despacharDocumento(propDocumentos);
		Assert.assertNotNull(util.getWebElement(driver, By.xpath("//td[4][contains(., '" + propDocumentos.getProperty("despacho") + "')]")),
				"Texto " + propDocumentos.getProperty("despacho") + " n�o encontrado.");
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