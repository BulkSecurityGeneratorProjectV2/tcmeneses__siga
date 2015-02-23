package br.gov.jfrj.siga.vraptor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.view.Results;
import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.cp.CpConfiguracao;
import br.gov.jfrj.siga.cp.CpSituacaoConfiguracao;
import br.gov.jfrj.siga.cp.CpTipoConfiguracao;
import br.gov.jfrj.siga.dp.CpOrgaoUsuario;
import br.gov.jfrj.siga.ex.ExConfiguracao;
import br.gov.jfrj.siga.ex.ExFormaDocumento;
import br.gov.jfrj.siga.ex.ExModelo;
import br.gov.jfrj.siga.ex.ExNivelAcesso;
import br.gov.jfrj.siga.ex.ExSituacaoConfiguracao;
import br.gov.jfrj.siga.ex.ExTipoDocumento;
import br.gov.jfrj.siga.ex.ExTipoFormaDoc;
import br.gov.jfrj.siga.ex.ExTipoMovimentacao;
import br.gov.jfrj.siga.ex.bl.Ex;
import br.gov.jfrj.siga.ex.bl.ExBL;
import br.gov.jfrj.siga.ex.bl.ExConfiguracaoComparator;
import br.gov.jfrj.siga.hibernate.ExDao;
import br.gov.jfrj.siga.libs.webwork.DpCargoSelecao;
import br.gov.jfrj.siga.libs.webwork.DpFuncaoConfiancaSelecao;
import br.gov.jfrj.siga.libs.webwork.DpLotacaoSelecao;
import br.gov.jfrj.siga.libs.webwork.DpPessoaSelecao;
import br.gov.jfrj.siga.vraptor.builder.ExConfiguracaoBuilder;

@Resource
public class ExConfiguracaoController extends ExController {

	private static final int ORGAO_INTEGRADO = 2;
	private static final String VERIFICADOR_ACESSO = "FE:Ferramentas;CFG:Configura��es";
	
	public ExConfiguracaoController(HttpServletRequest request, HttpServletResponse response, ServletContext context, Result result, SigaObjects so,
			EntityManager em) {
		super(request, response, context, result, ExDao.getInstance(), so, em);

		result.on(AplicacaoException.class).forwardTo(this).appexception();
		result.on(Exception.class).forwardTo(this).exception();
	}

	@Get("app/expediente/configuracao/listar")
	public void lista() throws Exception {
		assertAcesso(VERIFICADOR_ACESSO);
		result.include("listaTiposConfiguracao", getListaTiposConfiguracao());
		result.include("orgaosUsu", getOrgaosUsu());

	}

	@Get("app/expediente/configuracao/editar")
	public void edita(Long id, boolean campoFixo, 
			Long idOrgaoUsu, Long idTpMov, Long idTpDoc,
			Long idMod, Long idFormaDoc,
			Long idNivelAcesso, Long idSituacao, Long idTpConfiguracao,
			DpPessoaSelecao pessoaSel, DpLotacaoSelecao lotacaoSel, DpCargoSelecao cargoSel, 
			DpFuncaoConfiancaSelecao funcaoSel, ExClassificacaoSelecao classificacaoSel,
			Long idOrgaoObjeto) throws Exception {

		ExConfiguracao config = new ExConfiguracao();

		if (id != null) {
			config = daoCon(id);
		} else if(campoFixo) {

			final ExConfiguracaoBuilder configuracaoBuilder = ExConfiguracaoBuilder.novaInstancia()
					.setIdNivelAcesso(idNivelAcesso).setIdTpMov(idTpMov).setIdTpDoc(idTpDoc)
					.setIdMod(idMod).setIdFormaDoc(idFormaDoc).setIdNivelAcesso(idNivelAcesso)
					.setIdSituacao(idSituacao).setIdTpConfiguracao(idTpConfiguracao)
					.setPessoaSel(pessoaSel).setLotacaoSel(lotacaoSel).setCargoSel(cargoSel)
					.setFuncaoSel(funcaoSel).setClassificacaoSel(classificacaoSel).setIdOrgaoObjeto(idOrgaoObjeto);

			config = configuracaoBuilder.construir(dao());
		}
		escreverForm(config);

		result.include("id", id);
		result.include("listaTiposConfiguracao", getListaTiposConfiguracao());
		result.include("listaSituacao", getListaSituacao());
		result.include("listaNivelAcesso", getListaNivelAcesso());
		result.include("orgaosUsu", getOrgaosUsu());
		result.include("listaTiposMovimentacao", getListaTiposMovimentacao());
		result.include("tiposFormaDoc", getTiposFormaDoc());
		result.include("listaTiposDocumento", getListaTiposDocumento());

	}

	@SuppressWarnings("all")
	@Get("app/expediente/configuracao/excluir")
	public void excluir(Long id, String nmTipoRetorno, Long idMod, Long idFormaDoc) throws Exception {

		assertAcesso(VERIFICADOR_ACESSO);

		if (id != null) {
			try {
				dao().iniciarTransacao();
				ExConfiguracao config = daoCon(id);
				config.setHisDtFim(dao().consultarDataEHoraDoServidor());
				dao().gravarComHistorico(config, getIdentidadeCadastrante());
				dao().commitTransacao();
			} catch (final Exception e) {
				dao().rollbackTransacao();
				throw new AplicacaoException("Erro na grava��o", 0, e);
			}
		} else
			throw new AplicacaoException("ID n�o informada");
		
		escreveFormRetornoExclusao(nmTipoRetorno, idMod, idFormaDoc);
		
	}
	
	@SuppressWarnings("all")
	@Get("app/expediente/configuracao/editar_gravar")
	public void editarGravar(Long id,
			Long idOrgaoUsu, Long idTpMov, Long idTpDoc,
			Long idMod, Long idFormaDoc,
			Long idNivelAcesso, Long idSituacao, Long idTpConfiguracao,
			DpPessoaSelecao pessoaSel, DpLotacaoSelecao lotacaoSel, DpCargoSelecao cargoSel, 
			DpFuncaoConfiancaSelecao funcaoSel, ExClassificacaoSelecao classificacaoSel,
			Long idOrgaoObjeto, String nmTipoRetorno) throws Exception {

		assertAcesso(VERIFICADOR_ACESSO);

		if(idTpConfiguracao == null || idTpConfiguracao == 0)
			throw new AplicacaoException("Tipo de configuracao n�o informado");

		if(idSituacao == null || idSituacao == 0)
			throw new AplicacaoException("Situa��o de Configuracao n�o informada");

		final ExConfiguracaoBuilder configuracaoBuilder = ExConfiguracaoBuilder.novaInstancia()
				.setId(id)
				.setIdNivelAcesso(idNivelAcesso).setIdTpMov(idTpMov).setIdTpDoc(idTpDoc)
				.setIdMod(idMod).setIdFormaDoc(idFormaDoc).setIdNivelAcesso(idNivelAcesso)
				.setIdSituacao(idSituacao).setIdTpConfiguracao(idTpConfiguracao)
				.setPessoaSel(pessoaSel).setLotacaoSel(lotacaoSel).setCargoSel(cargoSel)
				.setFuncaoSel(funcaoSel).setClassificacaoSel(classificacaoSel).setIdOrgaoObjeto(idOrgaoObjeto);
		
		ExConfiguracao config = configuracaoBuilder.construir(dao());

		try {
			dao().iniciarTransacao();
			config.setHisDtIni(dao().consultarDataEHoraDoServidor());
			dao().gravarComHistorico(config, getIdentidadeCadastrante());
			dao().commitTransacao();
		} catch (final Exception e) {
			dao().rollbackTransacao();
			throw new AplicacaoException("Erro na grava��o", 0, e);
		}
		escreveFormRetorno(nmTipoRetorno, configuracaoBuilder);
	}

	private void escreveFormRetorno(String nmTipoRetorno, ExConfiguracaoBuilder builder) throws Exception {
		if("ajax".equals(nmTipoRetorno)) {
			Integer idFormaDoc = builder.getIdFormaDoc() != null ? builder.getIdFormaDoc().intValue() : null;
			result.redirectTo(this).listaCadastradas(builder.getIdTpConfiguracao(), null, builder.getIdTpMov(), idFormaDoc, builder.getIdMod());
		} else if("modelo".equals(nmTipoRetorno)) {
			result.redirectTo(ExModeloController.class).edita(builder.getIdMod(), 1);
			
		} else if("forma".equals(nmTipoRetorno)) {
			Integer idFormaDoc = builder.getIdFormaDoc() != null ? builder.getIdFormaDoc().intValue() : null;
			result.redirectTo(ExFormaDocumentoController.class).editarForma(idFormaDoc);
		} else {
			result.redirectTo(this).lista();
		}
	}

	@Get("app/expediente/configuracao/listar_cadastradas")
	public void listaCadastradas(Long idTpConfiguracao, Long idOrgaoUsu,
			Long idTpMov, Integer idFormaDoc, Long idMod) throws Exception {

		assertAcesso(VERIFICADOR_ACESSO);

		ExConfiguracao config = new ExConfiguracao();

		if (idTpConfiguracao != null && idTpConfiguracao != 0) {
			config.setCpTipoConfiguracao(dao().consultar(idTpConfiguracao,
					CpTipoConfiguracao.class, false));
		} else {
			result.include("err", "Tipo de configura��o n�o informado");
			result.use(Results.page()).forwardTo("/paginas/erro.jsp");
			return;
		}

		if (idOrgaoUsu != null && idOrgaoUsu != 0) {
			config.setOrgaoUsuario(dao().consultar(idOrgaoUsu,
					CpOrgaoUsuario.class, false));
		} else 
			config.setOrgaoUsuario(null);

		if (idTpMov != null && idTpMov != 0) {
			config.setExTipoMovimentacao(dao().consultar(idTpMov,
					ExTipoMovimentacao.class, false));
		} else
			config.setExTipoMovimentacao(null);

		if (idFormaDoc != null && idFormaDoc != 0) {
			config.setExFormaDocumento(dao().consultar(idFormaDoc,
					ExFormaDocumento.class, false));
		} else
			config.setExFormaDocumento(null);

		if (idMod != null && idMod != 0) {
			config.setExModelo(dao().consultar(idMod, ExModelo.class, false));
		} else 
			config.setExModelo(null);

		List<ExConfiguracao> listConfig = Ex.getInstance().getConf()
				.buscarConfiguracoesVigentes(config);

		Collections.sort(listConfig, new ExConfiguracaoComparator());

		this.getRequest().setAttribute("listConfig", listConfig);
		this.getRequest().setAttribute("tpConfiguracao", config.getCpTipoConfiguracao());
	}

	@SuppressWarnings("unchecked")
	@Get("app/expediente/configuracao/gerenciar_publicacao_boletim")
	public void gerenciarPublicacaoBoletim() throws Exception {
		List<Object[]> itens = new ArrayList<>();
		this.validarPodeGerenciarBoletim();

		for (ExConfiguracao c : gerarPublicadores()) {
			String nomeMod = gerarNomeModelo(c);
			Object[] entrada = buscarEntradaPorNomeMod(itens, nomeMod);

			if (entrada == null) {
				entrada = new Object[2];
				entrada[0] = nomeMod;
				entrada[1] = new ArrayList<ExConfiguracao>();
				itens.add(entrada);
			}
			((ArrayList<ExConfiguracao>) entrada[1]).add(c);
		}

		/*
		 * 
		 * 
		 * setIdFormaDoc(3);
		 * 
		 * setConfigsPorModelo(new HashMap<String, List<ExConfiguracao>>());
		 * 
		 * for (ExConfiguracao c : getPublicadores()){ ExModelo mod =
		 * c.getExModelo(); if (!getConfigsPorModelo().containsKey(mod))
		 * getConfigsPorModelo().put(mod.getNmMod(), new
		 * ArrayList<ExConfiguracao>()); getConfigsPorModelo().get(mod).add(c);
		 * }
		 */
		result.include("listaFormas", getListaFormas());
		result.include("listaModelosPorForma", getListaModelosPorForma(null));
		result.include("listaTipoPublicador", getListaTipoPublicador());
		result.include("listaSituacaoPodeNaoPode", getListaSituacaoPodeNaoPode());
		result.include("tipoPublicador", ORGAO_INTEGRADO);
		result.include("pessoaSel", new DpPessoaSelecao());
		result.include("lotacaoSel", new DpLotacaoSelecao());
		result.include("itens", itens);
		result.include("request", getRequest());
	}
	
	private Object[] buscarEntradaPorNomeMod(List<Object[]> itens, String nomeMod) {
		for (Object[] obj : itens) {
			if (obj[0].equals(nomeMod))
				return obj;
		}
		return null;
	}

	private String gerarNomeModelo(ExConfiguracao c) {
		if (c.getExModelo() != null) {
			String nomeMod = c.getExModelo().getNmMod();
			if (!c.getExModelo().getExFormaDocumento().getDescrFormaDoc().equals(nomeMod))
				nomeMod = MessageFormat.format("{0} -> {1}", 
					c.getExModelo()
					.getExFormaDocumento()
					.getDescrFormaDoc(), nomeMod);
			
			return nomeMod;
		} else if(c.getExFormaDocumento() != null)
			return c.getExFormaDocumento().getDescrFormaDoc();
		return "[Todos os modelos]";
	}

	private Set<ExConfiguracao> gerarPublicadores() {
		Set<ExConfiguracao> publicadores = new HashSet<ExConfiguracao>();
		TreeSet<CpConfiguracao> listaConfigs = getListaConfiguracao();
		
		for (CpConfiguracao cfg : listaConfigs) {
			if (cfg instanceof ExConfiguracao) {
				ExConfiguracao config = (ExConfiguracao) cfg;
				
				if (config.isAgendamentoPublicacaoBoletim() && config.podeAdicionarComoPublicador(getTitular(), getLotaTitular())) {
					publicadores.add(config);
				}
			}
		}
		return publicadores;
	}

	private TreeSet<CpConfiguracao> getListaConfiguracao() {
		TreeSet<CpConfiguracao> listaConfigs = Ex
				.getInstance()
				.getConf()
				.getListaPorTipo(CpTipoConfiguracao.TIPO_CONFIG_MOVIMENTAR);
		
		if (listaConfigs == null)
			return new TreeSet<CpConfiguracao>();
		return listaConfigs;
	}

	private void validarPodeGerenciarBoletim() {
		if (!Ex.getInstance().getConf().podePorConfiguracao(getTitular(),
				getLotaTitular(),
				CpTipoConfiguracao.TIPO_CONFIG_GERENCIAR_PUBLICACAO_BOLETIM))
			throw new AplicacaoException("Opera��o restrita");
	}
	
	private Set<ExFormaDocumento> getListaFormas() throws Exception {
		ExBL bl = Ex.getInstance().getBL();
		return bl.obterFormasDocumento(bl.obterListaModelos(null, false, null, false, null, null, false), null, null);
	}
	
	private Set<ExModelo> getListaModelosPorForma(Long idFormaDoc) throws Exception {
		if (idFormaDoc != null && idFormaDoc != 0) {
			ExFormaDocumento forma = ExDao.getInstance().consultar(idFormaDoc, ExFormaDocumento.class, false);
			return forma.getExModeloSet();
		}
		return getListaModelos();
	}
	
	private Set<ExModelo> getListaModelos() throws Exception {
		TreeSet<ExModelo> s = new TreeSet<ExModelo>(getExModeloComparator());
		s.addAll(dao().listarExModelos());
		return s;
	}

	private Comparator<ExModelo> getExModeloComparator() {
		return new Comparator<ExModelo>() {
			public int compare(ExModelo o1, ExModelo o2) {
				return o1.getNmMod().compareTo(o2.getNmMod());
			}
		};
	}
	
	private Map<Integer, String> getListaTipoPublicador() {
		final Map<Integer, String> map = new TreeMap<Integer, String>();
		map.put(1, "Matr�cula");
		map.put(2, "�rg�o Integrado");
		return map;
	}
	
	private Set<ExSituacaoConfiguracao> getListaSituacaoPodeNaoPode() throws Exception {
		HashSet<ExSituacaoConfiguracao> s = new HashSet<ExSituacaoConfiguracao>();
		s.add(ExDao.getInstance().consultar(1L, ExSituacaoConfiguracao.class, false));
		s.add(ExDao.getInstance().consultar(2L, ExSituacaoConfiguracao.class, false));
		return s;
	}
	
	private ExConfiguracao daoCon(long id) {
		return dao().consultar(id, ExConfiguracao.class, false);
	}

	private void escreveFormRetornoExclusao(String nmTipoRetorno, Long idMod,
			Long idFormaDoc) throws Exception {

		if("modelo".equals(nmTipoRetorno)) {
			result.redirectTo(ExModeloController.class).edita(idMod, 1);
		} else if("forma".equals(nmTipoRetorno)) {
			Integer formaDoc = idFormaDoc != null ? idFormaDoc.intValue() : null;
			result.redirectTo(ExFormaDocumentoController.class).editarForma(formaDoc);
		} else {
			result.redirectTo(this).lista();
		}
	}

	private void escreverForm(ExConfiguracao c) throws Exception {
		DpPessoaSelecao pessoaSelecao = new DpPessoaSelecao();
		DpLotacaoSelecao lotacaoSelecao = new DpLotacaoSelecao();
		DpFuncaoConfiancaSelecao funcaoConfiancaSelecao = new DpFuncaoConfiancaSelecao();
		DpCargoSelecao cargoSelecao = new DpCargoSelecao();
		ExClassificacaoSelecao classificacaoSelecao = new ExClassificacaoSelecao();

		if (c.getOrgaoUsuario() != null)
			result.include("idOrgaoUsu", c.getOrgaoUsuario().getIdOrgaoUsu());

		if (c.getExTipoMovimentacao() != null)
			result.include("idTpMov", c.getExTipoMovimentacao().getIdTpMov());

		if (c.getExTipoDocumento() != null)
			result.include("idTpDoc", c.getExTipoDocumento().getIdTpDoc());

		if (c.getExTipoFormaDoc() != null)
			result.include("idTpFormaDoc", c.getExTipoFormaDoc().getIdTipoFormaDoc());

		if (c.getExFormaDocumento() != null)
			result.include("idFormaDoc", c.getExFormaDocumento().getIdFormaDoc());

		if (c.getExModelo() != null)
			result.include("idMod", c.getExModelo().getIdMod());

		if (c.getExNivelAcesso() != null)
			result.include("idNivelAcesso", c.getExNivelAcesso().getIdNivelAcesso());

		if (c.getCpSituacaoConfiguracao() != null)
			result.include("idSituacao", c.getCpSituacaoConfiguracao().getIdSitConfiguracao());

		if (c.getCpTipoConfiguracao() != null)
			result.include("idTpConfiguracao", c.getCpTipoConfiguracao().getIdTpConfiguracao());

		if (c.getDpPessoa() != null)
			pessoaSelecao.buscarPorObjeto(c.getDpPessoa());

		if (c.getLotacao() != null)
			lotacaoSelecao.buscarPorObjeto(c.getLotacao());
		
		if (c.getCargo() != null)
			cargoSelecao.buscarPorObjeto(c.getCargo());

		if (c.getFuncaoConfianca() != null) 
			funcaoConfiancaSelecao.buscarPorObjeto(c.getFuncaoConfianca());

		if (c.getExClassificacao() != null)
			classificacaoSelecao.buscarPorObjeto(c.getExClassificacao());

		if (c.getOrgaoObjeto() != null)
			result.include("idOrgaoObjeto", c.getOrgaoObjeto().getIdOrgaoUsu());

		result.include("pessoaSel", pessoaSelecao);
		result.include("lotacaoSel", lotacaoSelecao);
		result.include("cargoSel", cargoSelecao);
		result.include("funcaoSel", funcaoConfiancaSelecao);
		result.include("classificacaoSel", classificacaoSelecao);

	}

	@SuppressWarnings("all")
	private Set<CpTipoConfiguracao> getListaTiposConfiguracao() throws Exception {
		TreeSet<CpTipoConfiguracao> s = new TreeSet<CpTipoConfiguracao>(
				new Comparator() {

					public int compare(Object o1, Object o2) {
						return ((CpTipoConfiguracao) o1).getDscTpConfiguracao()
								.compareTo(
										((CpTipoConfiguracao) o2)
												.getDscTpConfiguracao());
					}
				});

		s.addAll(dao().listarTiposConfiguracao());

		return s;
	}

	@SuppressWarnings("all")
	private Set<CpSituacaoConfiguracao> getListaSituacao() throws Exception {
		TreeSet<CpSituacaoConfiguracao> s = new TreeSet<CpSituacaoConfiguracao>(
				new Comparator() {

					public int compare(Object o1, Object o2) {
						return ((CpSituacaoConfiguracao) o1)
								.getDscSitConfiguracao().compareTo(
										((CpSituacaoConfiguracao) o2)
												.getDscSitConfiguracao());
					}

				});

		s.addAll(dao().listarSituacoesConfiguracao());

		return s;
	}
	
	@SuppressWarnings("all")
	private Set<ExTipoMovimentacao> getListaTiposMovimentacao() throws Exception {
		TreeSet<ExTipoMovimentacao> s = new TreeSet<ExTipoMovimentacao>(
				new Comparator() {

					public int compare(Object o1, Object o2) {
						return ((ExTipoMovimentacao) o1)
								.getDescrTipoMovimentacao().compareTo(
										((ExTipoMovimentacao) o2)
												.getDescrTipoMovimentacao());
					}

				});

		s.addAll(dao().listarExTiposMovimentacao());

		return s;
	}
	
	private List<ExTipoFormaDoc> getTiposFormaDoc() throws Exception {
		return dao().listarExTiposFormaDoc();
	}
	
	private List<ExNivelAcesso> getListaNivelAcesso() throws Exception {
		return dao().listarOrdemNivel();
	}
	
	private List<ExTipoDocumento> getListaTiposDocumento() throws Exception {
		return dao().listarExTiposDocumento();
	}
}
