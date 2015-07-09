package br.gov.jfrj.siga.page.objects;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import br.gov.jfrj.siga.integration.test.util.IntegrationTestUtil;

public class TransferenciaPage {
	private WebDriver driver;
	
	@FindBy(id="transferir_gravar_dtMovString")
	private WebElement data;
	
	@FindBy(id="transferir_gravar_subscritorSel_sigla")
	private WebElement subscritor;
	
	@FindBy(id="transferir_gravar_nmFuncaoSubscritor")
	private WebElement funcaoLotacao;
	
	@FindBy(id="transferir_gravar_idTpDespacho")
	private WebElement despacho;	
		
	@FindBy(id="transferir_gravar_tipoResponsavel")
	private WebElement tipoAtendente;	
	
	@FindBy(id="transferir_gravar_lotaResponsavelSel_sigla")
	private WebElement atendente;	
	
	@FindBy(id="transferir_gravar_dtDevolucaoMovString")
	private WebElement dataDevolucao;	
	
	@FindBy(xpath="//input[@value='Ok']")
    private WebElement botaoOk;
    
	@FindBy(xpath="//input[@value='Cancela']")
    private WebElement botaoCancela;
	
	@FindBy(xpath="//input[@value='Visualizar o despacho']")
    private WebElement botaoVisualizarDespacho;
	
	private IntegrationTestUtil util;
/*	
	private String winHandleBefore;
	private String popupHandle;	
	*/
	public TransferenciaPage(WebDriver driver) {
		this.driver = driver;
		util = new IntegrationTestUtil();
		
		util.openPopup(driver);	
		if(!util.isDescricaoPaginaVisivel(driver, "Despacho / Transferencia")) {
			util.closePopup(driver);
			throw new RuntimeException("Esta n�o � a p�gina de Despacho / Transferencia!");
		}
	}
	
	public OperacoesDocumentoPage despacharDocumento(Properties propDocumentos) {
	
		try {
			despachar(propDocumentos);
			new WebDriverWait(driver, 30).until(util.popupFechada());
		} finally {
			util.closePopup(driver);
		}
		return PageFactory.initElements(driver, OperacoesDocumentoPage.class);
	}
	
	public Boolean despacharVolumeEncerrado(Properties propDocumentos) {		
		//util.openPopup(driver);		
		try {
			despachar(propDocumentos);
			if(util.getWebElement(driver, By.xpath("//h3[contains(text(), 'N�o � permitido')]")) != null) {
				return Boolean.FALSE;
			}			
		} finally {
			util.closePopup(driver);
		}
		return Boolean.TRUE;
	}
	
	private void despachar(Properties propDocumentos) {
		String URL = driver.getCurrentUrl();
		util.preencheElemento(driver, data, new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime()));
		util.preencheElemento(driver, subscritor, propDocumentos.getProperty("siglaSubscritor"));
		util.preencheElemento(driver, funcaoLotacao, propDocumentos.getProperty("funcaoLocalidade"));
		util.getSelect(driver, despacho).selectByVisibleText(propDocumentos.getProperty("despacho"));
		new WebDriverWait(driver, 30).until(util.trocaURL(URL));
		new WebDriverWait(driver, 30).until(ExpectedConditions.elementToBeClickable(botaoOk));
		botaoOk.click();
	}
	
	public OperacoesDocumentoPage transferirDocumento(Properties propDocumentos) {		
		try {
			util.getSelect(driver, tipoAtendente).selectByVisibleText(propDocumentos.getProperty("tipoAtendente"));
			util.preencheElemento(driver, atendente, propDocumentos.getProperty("atendente"));			
			funcaoLotacao.click();
			new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.id("lotaResponsavelSelSpan")));			
			util.preencheElemento(driver, dataDevolucao, new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime()));
			botaoOk.click();
			new WebDriverWait(driver, 30).until(util.popupFechada());
		} finally {
			util.closePopup(driver);
		}
		
		return PageFactory.initElements(driver, OperacoesDocumentoPage.class);
	}
	
	/**
	 * 
	 * @param propDocumentos
	 * @param codigoDocumento
	 * @return
	 */
	public String despachoDocumentoFilho(Properties propDocumentos, String codigoDocumento) {
		String codigoDocumentoJuntado;
		
		try {
			util.getSelect(driver, despacho).selectByVisibleText(propDocumentos.getProperty("despachoTextoLongo"));
			DespachoPage despachoPage = PageFactory.initElements(driver, DespachoPage.class);
			OperacoesDocumentoPage operacoesDocumentoPage = despachoPage.criarDespacho(propDocumentos, Boolean.FALSE);
			
			new WebDriverWait(driver, 30).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h3[1][contains(text(), 'Geral - Em Elabora��o, Revisar')]|//div[h3 = 'Vias']/ul/li[contains(., 'Geral - Em Elabora��o')]")));
						
			operacoesDocumentoPage = operacoesDocumentoPage.clicarLinkFinalizar();
			
			RegistraAssinaturaManualPage registraAssinaturaManualPage = operacoesDocumentoPage.clicarLinkRegistrarAssinaturaManual();
			operacoesDocumentoPage = registraAssinaturaManualPage.registarAssinaturaManual();
			
			new WebDriverWait(driver, 30).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h3[1][contains(text(), 'Juntado')]|//div[h3 = 'Vias']/ul/li[contains(., 'Juntado')]")));			
			operacoesDocumentoPage = operacoesDocumentoPage.clicarLinkDesentranhar();
			new WebDriverWait(driver, 30).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h3[1][contains(text(), 'Aguardando Andamento')]|//div[h3 = 'Vias']/ul/li[contains(., 'Aguardando Andamento')]")));	
			
			JuntadaDocumentoPage juntadaDocumentoPage = operacoesDocumentoPage.clicarlinkJuntar();
			operacoesDocumentoPage = juntadaDocumentoPage.juntarDocumento(propDocumentos, codigoDocumento);
			codigoDocumentoJuntado = operacoesDocumentoPage.getTextoVisualizacaoDocumento();
			
		} finally {
			util.closePopup(driver);
		}
		driver.navigate().refresh();
		
		return codigoDocumentoJuntado;
	}
}

