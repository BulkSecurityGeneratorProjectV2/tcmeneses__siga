package br.gov.jfrj.siga.page.objects;

import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import br.gov.jfrj.siga.integration.test.util.IntegrationTestUtil;

public class InclusaoCossignatarioPage {	
	private WebDriver driver;
	
	@FindBy(id="incluir_cosignatario_gravar_cosignatarioSel_sigla")
	private WebElement cossignatario;
	
	@FindBy(id="incluir_cosignatario_gravar_funcaoCosignatario")
	private WebElement funcaoLocalidade;
	
	@FindBy(css="input.gt-btn-small.gt-btn-left")
	private WebElement botaoOk;
	
	private IntegrationTestUtil util;
	
	public InclusaoCossignatarioPage(WebDriver driver) {
		this.driver = driver;
		util = new IntegrationTestUtil();
		
		if(!util.isDescricaoPaginaVisivel(driver, "Inclus�o de Cossignat�rio")) {
			throw new RuntimeException("Esta n�o � a p�gina de Inclus�o de Cossignat�rio!");
		}
	}
	
	public OperacoesDocumentoPage incluiCossignatario(Properties propDocumento) {
		cossignatario.clear();
		util.preencheElemento(driver, cossignatario, propDocumento.getProperty("siglaCossignatario"));
		funcaoLocalidade.click();
		new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.id("cosignatarioSelSpan")));
		funcaoLocalidade.clear();
		util.preencheElemento(driver, funcaoLocalidade, propDocumento.getProperty("funcaoLocalidade"));
		botaoOk.click();
		
		return PageFactory.initElements(driver, OperacoesDocumentoPage.class);
	}	
}
