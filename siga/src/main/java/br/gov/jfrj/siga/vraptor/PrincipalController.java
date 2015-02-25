package br.gov.jfrj.siga.vraptor;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.view.Results;
import br.gov.jfrj.siga.base.SigaHTTP;
import br.gov.jfrj.siga.cp.bl.Cp;
import br.gov.jfrj.siga.dp.CpOrgaoUsuario;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.dp.dao.CpDao;

@Resource
public class PrincipalController extends SigaController {
	
	// Nem será necessário herdar de Selecao
	public class GenericoSelecao {

		private Long id;
		private String sigla;
		private String matricula;
		private String descricao;

		public String getDescricao() {
			return descricao;
		}

		public void setDescricao(String descricao) {
			this.descricao = descricao;
		}
		
		public Long getId(){
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getSigla() {
			return sigla;
		}

		public void setSigla(String sigla) {
			this.sigla = sigla;
		}

		public String getMatricula() {
			return matricula;
		}

		public void setMatricula(String matricula) {
			this.matricula = matricula;
		}
	}
	
	
	public PrincipalController(HttpServletRequest request, Result result, CpDao dao, SigaObjects so, EntityManager em) {
		super(request, result, dao, so, em);
	}

	@Get("app/principal")
	public void principal() {
	}
	
	@Get("app/pagina_vazia")
	public void paginaVazia() {
	}
	
	@Get("app/usuario_autenticado")
	public void usuarioAutenticado() {
	}
	
	
	@Get("app/generico/selecionar")
	public void selecionar(final String sigla, final String matricula) {
		final GenericoSelecao sel = new GenericoSelecao();
		try {
			DpPessoa pes = getTitular();
			DpLotacao lot = getLotaTitular();
			String testes = "";
			String incluirMatricula = "";
			if (matricula != null) {
				pes = daoPes(matricula);
				lot = pes.getLotacao();
				testes = "/testes";
				incluirMatricula = "&matricula=" + matricula;
			}

			// TODO n�o precisa pegar isso de um properties, isso existe no proprio request getServerName, getPort...
			
			//String urlBase = "http://"+ SigaBaseProperties.getString(SigaBaseProperties.getString("ambiente") + ".servidor.principal")+ getRequest().getServerPort();
			final String urlBase = getRequest().getScheme() + "://" + getRequest().getServerName() + ":" + getRequest().getServerPort();

			String URLSelecionar = "";
			String uRLExibir = "";

			final List<String> orgaos = new ArrayList<String>();
			String copiaSigla = sigla.toUpperCase();
			for (CpOrgaoUsuario o : dao().consultaCpOrgaoUsuario()) {
				orgaos.add(o.getSiglaOrgaoUsu());
				orgaos.add(o.getAcronimoOrgaoUsu());
			}
			for (String s : orgaos)
				if (copiaSigla.startsWith(s)) {
					copiaSigla = copiaSigla.substring(s.length());
					break;
				}
			if (copiaSigla.startsWith("-"))
				copiaSigla = copiaSigla.substring(1);

			//alterada a condi��o que verifica se � uma solicita��o do siga-sr
			//dessa forma a regex verifica se a sigla come�a com SR ou sr e termina com n�meros
			//necess�rio para n�o dar conflito caso exista uma lota��o que inicie com SR
			if (copiaSigla.startsWith("SR")) {
//			if (copiaSigla.matches("^[SR|sr].*[0-9]+$")) {
				if (Cp.getInstance()
						.getConf()
						.podeUtilizarServicoPorConfiguracao(pes, lot, "SIGA;SR"))
					URLSelecionar = urlBase + "/sigasr" + testes+ "/solicitacao/selecionar?sigla=" + sigla + incluirMatricula;
			} else if (copiaSigla.startsWith("MTP")
					|| copiaSigla.startsWith("RTP")
					|| copiaSigla.startsWith("STP")) {
				if (Cp.getInstance()
						.getConf()
						.podeUtilizarServicoPorConfiguracao(pes, lot, "SIGA;TP")) {
					URLSelecionar = urlBase + "/sigatp" + "/selecionar.action?sigla=" + sigla + incluirMatricula;
				}
			} 
			else
				URLSelecionar = urlBase 
						+ "/sigaex" + (testes.length() > 0 ? testes : "/app/expediente") + "/selecionar?sigla=" + sigla+ incluirMatricula;

			final SigaHTTP http = new SigaHTTP();
			String[] response = http.get(URLSelecionar, getRequest(), null).split(";");

			if (response.length == 1 && Integer.valueOf(response[0]) == 0) {
				//verificar se ap�s a retirada dos prefixos referente 
				//ao org�o (sigla_orgao_usu = RJ ou acronimo_orgao_usu = JFRJ) e n�o achar resultado com as op��es anteriores 
				//a string copiaSigla somente possui n�meros
				if (copiaSigla.matches("(^[0-9]+$)")) {
					URLSelecionar = urlBase 
							+ "/siga"+ (testes.length() > 0 ? testes : "/pessoa") + "/selecionar.action?sigla=" + sigla+ incluirMatricula;
				}
				//encontrar lota��es
				else {
					URLSelecionar = urlBase 
						+ "/siga"+ (testes.length() > 0 ? testes : "/lotacao")+ "/selecionar.action?sigla=" + sigla+ incluirMatricula;
				}
				
				response = http.get(URLSelecionar, getRequest(), null).split(";");
				
				if (copiaSigla.matches("(^[0-9]+$)")) 
					uRLExibir = "/siga/pessoa/exibir.action?sigla="+ response[2];
				else
					uRLExibir = "/siga/lotacao/exibir.action?sigla="+ response[2];
			}
			else {
				if (copiaSigla.startsWith("SR"))
//					if (copiaSigla.matches("^[SR|sr].*[0-9]+$"))
						uRLExibir = "/sigasr/solicitacao/exibir/" + response[1];
				else if (copiaSigla.startsWith("MTP")
						|| copiaSigla.startsWith("STP")
						|| copiaSigla.startsWith("RTP"))
					uRLExibir = "/sigatp/exibir.action?sigla=" + response[2];
				else
					uRLExibir = "/sigaex/app/expediente/doc/exibir?sigla="+ response[2];
			}
			
			sel.setId(Long.valueOf(response[1]));
			sel.setSigla(response[2]);
			sel.setDescricao(uRLExibir);

			result.include("sel", sel);
			result.include("request", getRequest());
			result.use(Results.page()).forwardTo("/sigalibs/ajax_retorno.jsp");

		} catch (Exception e) {
			result.use(Results.page()).forwardTo("/sigalibs/ajax_vazio.jsp");
		}
	}
	
}