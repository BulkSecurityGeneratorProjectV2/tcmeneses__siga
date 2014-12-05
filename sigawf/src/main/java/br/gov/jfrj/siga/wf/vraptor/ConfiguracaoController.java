package br.gov.jfrj.siga.wf.vraptor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.SessionFactory;
import org.jbpm.graph.def.ProcessDefinition;

import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.cp.CpConfiguracao;
import br.gov.jfrj.siga.cp.CpSituacaoConfiguracao;
import br.gov.jfrj.siga.cp.CpTipoConfiguracao;
import br.gov.jfrj.siga.dp.CpOrgaoUsuario;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.dp.dao.CpDao;
import br.gov.jfrj.siga.vraptor.SigaObjects;
import br.gov.jfrj.siga.wf.Permissao;
import br.gov.jfrj.siga.wf.WfConfiguracao;
import br.gov.jfrj.siga.wf.bl.Wf;
import br.gov.jfrj.siga.wf.dao.WfDao;
import br.gov.jfrj.siga.wf.util.WfContextBuilder;
import br.gov.jfrj.siga.wf.util.WfTipoResponsavel;

@Resource
public class ConfiguracaoController extends WfController {

	private static int TIPO_RESP_INDEFINIDO = 0;
	private static int TIPO_RESP_MATRICULA = 1;
	private static int TIPO_RESP_LOTACAO = 2;

	private List<WfTipoResponsavel> listaTipoResponsavel = new ArrayList<WfTipoResponsavel>();

	/**
	 * Inicializa os tipos de respons�veis e suas respectivas express�es, quando
	 * houver.
	 */
	public ConfiguracaoController(HttpServletRequest request, Result result,
			WfDao dao, SigaObjects so, WfUtil util) {
		super(request, result, dao, so, util);

		WfTipoResponsavel tpIndefinido = new WfTipoResponsavel(
				TIPO_RESP_INDEFINIDO, "[Indefinido]", "");
		WfTipoResponsavel tpMatricula = new WfTipoResponsavel(
				TIPO_RESP_MATRICULA, "Matr�cula", "matricula");
		WfTipoResponsavel tpLotacao = new WfTipoResponsavel(TIPO_RESP_LOTACAO,
				"Lota��o", "lotacao");

		listaTipoResponsavel.add(tpIndefinido);
		listaTipoResponsavel.add(tpMatricula);
		listaTipoResponsavel.add(tpLotacao);
	}

	/**
	 * Grava a configura��o da permiss�o. Primeiro, processa-se as altera�es nas
	 * permiss�es existentes e depois processa-se as novas permiss�es. As
	 * permiss�es indefinidas s�o exclu�das da lista de permiss�es.
	 * 
	 * @return
	 * @throws Exception
	 */
	public void gravar(String orgao, String procedimento) throws Exception {
		ProcessDefinition pd = WfContextBuilder.getJbpmContext()
				.getJbpmContext().getGraphSession()
				.findLatestProcessDefinition(procedimento);

		CpOrgaoUsuario orgaoUsuario = daoOU(orgao);

		Date horaDoDB = dao().consultarDataEHoraDoServidor();
		if (pd != null) {

			// processa permiss�es existentes
			List<Permissao> listaPermissao = getPermissoes(orgaoUsuario,
					procedimento);
			for (Permissao perm : listaPermissao) {
				WfConfiguracao cfg = prepararConfiguracao(orgaoUsuario,
						procedimento);

				processarPermissao(orgaoUsuario, procedimento, perm, cfg,
						horaDoDB);

			}

			// processa nova permiss�o
			WfConfiguracao cfg = prepararConfiguracao(orgaoUsuario,
					procedimento);
			Permissao novaPermissao = new Permissao();
			novaPermissao.setId(this.getIdNovaPermissao());
			novaPermissao.setProcedimento(procedimento);

			processarPermissao(orgaoUsuario, procedimento, novaPermissao, cfg,
					horaDoDB);

			if (novaPermissao.getPessoa() != null
					|| novaPermissao.getLotacao() != null) {
				listaPermissao.add(novaPermissao);
			}

			// remove permiss�es inv�lidas
			ArrayList<Permissao> listaAux = new ArrayList<Permissao>();
			for (Permissao perm : listaPermissao) {
				if (perm.getPessoa() != null || perm.getLotacao() != null) {
					listaAux.add(perm);
				}
			}

			listaPermissao = listaAux;

			limparCache();
		}

		result.redirectTo(this).pesquisar(orgao, procedimento);
	}

	/**
	 * Seleciona um procedimento que ter� suas permiss�es configuradas.
	 * 
	 * @return
	 * @throws Exception
	 */
	public void pesquisar(String orgao, String procedimento) throws Exception {
		assertAcesso("CONFIGURAR:Configurar iniciadores");

		limparCache();
		ProcessDefinition pd = WfContextBuilder.getJbpmContext()
				.getJbpmContext().getGraphSession()
				.findLatestProcessDefinition(procedimento);
		CpOrgaoUsuario orgaoUsuario = daoOU(orgao);

		if (pd != null) {
			List<Permissao> listaPermissao = new ArrayList<Permissao>();
			listaPermissao.addAll(getPermissoes(orgaoUsuario, procedimento));
			result.include("orgao", orgao);
			result.include("procedimento", procedimento);
			result.include("listaPermissao", listaPermissao);
			result.include("listaOrgao", dao().listarOrgaosUsuarios());
			result.include("listaTipoResponsavel", listaTipoResponsavel);
			result.include("listaProcedimento", getListaProcedimento());
		}
	}

	/**
	 * Retorna a lista de configura��es definidas para uma permiss�o.
	 * 
	 * @param perm
	 *            -
	 * @return Lista de permiss�es j� gravadas no banco de dados.
	 * @throws Exception
	 */
	private List<WfConfiguracao> getConfiguracaoExistente(
			CpOrgaoUsuario orgaoUsuario, String procedimento, Permissao perm)
			throws Exception {
		WfConfiguracao fltConfigExistente = new WfConfiguracao();

		CpTipoConfiguracao tipoConfig = CpDao.getInstance().consultar(
				CpTipoConfiguracao.TIPO_CONFIG_INSTANCIAR_PROCEDIMENTO,
				CpTipoConfiguracao.class, false);

		fltConfigExistente.setCpTipoConfiguracao(tipoConfig);
		fltConfigExistente.setOrgaoUsuario(orgaoUsuario);
		fltConfigExistente.setProcedimento(procedimento);
		fltConfigExistente.setDpPessoa(perm.getPessoa());
		fltConfigExistente.setLotacao(perm.getLotacao());

		List<WfConfiguracao> cfgExistente = WfDao.getInstance().consultar(
				fltConfigExistente);
		List<WfConfiguracao> resultado = new ArrayList<WfConfiguracao>();
		// Melhorar isso: o filtro por pessoa ou lota��o n�o est� funcionando
		for (WfConfiguracao c : cfgExistente) {
			if ((c.getDpPessoa() != null || c.getLotacao() != null)
					&& c.getHisDtFim() == null
					&& c.getIdConfiguracao().equals(perm.getId())) {
				resultado.add(c);
			}
		}

		return resultado;
	}

	/**
	 * Retorna a lista de permiss�es definidas para uma defini��o de
	 * procedimento.
	 * 
	 * @param pd
	 *            - Defini��o de processo
	 * @return - Lista de permiss�es
	 * @throws Exception
	 */
	private List<Permissao> getPermissoes(CpOrgaoUsuario orgaoUsuario,
			String procedimento) throws Exception {

		List<Permissao> resultado = new ArrayList<Permissao>();

		TreeSet<CpConfiguracao> cfg = Wf
				.getInstance()
				.getConf()
				.getListaPorTipo(
						CpTipoConfiguracao.TIPO_CONFIG_INSTANCIAR_PROCEDIMENTO);
		for (CpConfiguracao c : cfg) {

			if (c instanceof WfConfiguracao) {
				WfConfiguracao wfC = (WfConfiguracao) c;
				if (wfC.getProcedimento() != null
						&& wfC.getProcedimento().equals(procedimento)
						&& wfC.getHisDtFim() == null) {

					Permissao perm = new Permissao();
					perm.setProcedimento(procedimento);
					perm.setId(c.getIdConfiguracao());

					perm.setPessoa(c.getDpPessoa());
					perm.setLotacao(c.getLotacao());

					if (perm.getPessoa() != null) {
						perm.setTipoResponsavel(TIPO_RESP_MATRICULA);
					}
					if (perm.getLotacao() != null) {
						perm.setTipoResponsavel(TIPO_RESP_LOTACAO);
					}

					resultado.add(perm);
				}
			}
		}

		return resultado;

	}

	/**
	 * M�todo utilizado para gravar uma nova configura��o. ATEN��O: ESTE M�TODO
	 * PROVAVELMENTE PODE SER ELIMINADO EM UM REFACTORING.
	 * 
	 * @param cfg
	 * @throws Exception
	 */
	private void gravarNovaConfig(WfConfiguracao cfg) throws Exception {
		WfDao.getInstance().gravarComHistorico(cfg, getIdentidadeCadastrante());
	}

	/**
	 * Torna uma configura��o existente inv�lida. A invalida��o da configura��o
	 * normalmente ocorre ao se criar uma nova configura��o. A configura��o
	 * antiga torna-se inv�lida mas continua sendo mantida no banco de dados
	 * para fins de hist�rico.
	 * 
	 * @param cfgExistente
	 *            - Configura��o a ser invalidada
	 * @param dataFim
	 *            - Data que define o fim da validade da configura��o, ou seja,
	 *            data de invalida��o.
	 * @throws AplicacaoException
	 */
	private void invalidarConfiguracao(WfConfiguracao cfgExistente, Date dataFim)
			throws AplicacaoException {
		if (cfgExistente != null && cfgExistente.getHisDtFim() == null) {
			cfgExistente.setHisDtFim(dataFim);
			WfDao.getInstance().gravarComHistorico(cfgExistente,
					getIdentidadeCadastrante());
		}
	}

	/**
	 * Limpa o cache do hibernate. Como as configura��es s�o mantidas em cache
	 * por motivo de performance, as altera��es precisam ser atualizadas para
	 * que possam valer imediatamente.
	 * 
	 * @throws Exception
	 */
	public void limparCache() throws Exception {

		SessionFactory sfWfDao = WfDao.getInstance().getSessao()
				.getSessionFactory();
		SessionFactory sfCpDao = CpDao.getInstance().getSessao()
				.getSessionFactory();

		CpTipoConfiguracao tipoConfig = CpDao.getInstance().consultar(
				CpTipoConfiguracao.TIPO_CONFIG_INSTANCIAR_PROCEDIMENTO,
				CpTipoConfiguracao.class, false);

		Wf.getInstance().getConf().limparCacheSeNecessario();

		sfWfDao.evict(CpConfiguracao.class);
		sfWfDao.evict(WfConfiguracao.class);
		sfCpDao.evict(DpLotacao.class);

		sfWfDao.evictQueries(CpDao.CACHE_QUERY_CONFIGURACAO);

		return;

	}

	/**
	 * Inicia um objeto WfConfiguracao de modo que possa receber as
	 * configura��es definidas pelo usu�rio.
	 * 
	 * @return - Configura��o pronta para receber as configura��es.
	 */
	private WfConfiguracao prepararConfiguracao(CpOrgaoUsuario orgaoUsuario,
			String procedimento) {
		WfConfiguracao cfg = new WfConfiguracao();
		CpTipoConfiguracao tipoConfig = CpDao.getInstance().consultar(
				CpTipoConfiguracao.TIPO_CONFIG_INSTANCIAR_PROCEDIMENTO,
				CpTipoConfiguracao.class, false);
		cfg.setCpTipoConfiguracao(tipoConfig);
		cfg.setOrgaoUsuario(orgaoUsuario);
		cfg.setProcedimento(procedimento);
		return cfg;
	}

	/**
	 * Processa as permiss�es definidas pelo usu�rio. S�o extra�dos os dados do
	 * request, definida a data de in�cio da configura��o, definidas as
	 * permiss�es, definida a invalida��o da configura��o antiga e atualizada a
	 * vis�o do usu�rio.
	 * 
	 * @param permissao
	 *            - Permiss�o a ser processada
	 * @param cfg
	 *            - Configura��o preparada para receber as novas configura��es
	 * @param horaDoBD
	 *            - Hora do banco de dados. A data/hora de in�cio de vig�ncia
	 *            deve ser a mesma da invalida��o da configura��o anterior.
	 * @throws Exception
	 */
	private void processarPermissao(CpOrgaoUsuario orgaoUsuario,
			String procedimento, Permissao permissao, WfConfiguracao cfg,
			Date horaDoBD) throws Exception {
		DpLotacao lotacao = null;
		DpPessoa pessoa = null;

		lotacao = extrairLotaAtor(permissao.getId());
		pessoa = extrairAtor(permissao.getId());

		if (pessoa != null || lotacao != null) {// se
			// configura��o
			// definida
			cfg.setDpPessoa(pessoa);
			cfg.setLotacao(lotacao);

			cfg.setHisDtIni(horaDoBD);

			for (WfConfiguracao cfgExistente : getConfiguracaoExistente(
					orgaoUsuario, procedimento, permissao)) {
				invalidarConfiguracao(cfgExistente, horaDoBD);
			}

			permissao.setPessoa(pessoa);
			permissao.setLotacao(lotacao);

			CpSituacaoConfiguracao sit = new CpSituacaoConfiguracao();
			sit.setIdSitConfiguracao(CpSituacaoConfiguracao.SITUACAO_PODE);
			cfg.setCpSituacaoConfiguracao(sit);
			gravarNovaConfig(cfg);
			permissao.setId(cfg.getIdConfiguracao()); // deixa de usar a id
			// tempor�ria

			// atualiza os dados da vis�o
			if (pessoa != null) {
				permissao.setPessoa(pessoa);
				permissao.setTipoResponsavel(TIPO_RESP_MATRICULA);
			}

			if (lotacao != null) {
				permissao.setLotacao(lotacao);
				permissao.setTipoResponsavel(TIPO_RESP_LOTACAO);
			}

		} else {
			for (WfConfiguracao cfgExistente : getConfiguracaoExistente(
					orgaoUsuario, procedimento, permissao)) {
				invalidarConfiguracao(cfgExistente, horaDoBD);
			}

			permissao.setPessoa(pessoa);
			permissao.setLotacao(lotacao);
		}

	}

	private Long getIdNovaPermissao() {
		return Long.MAX_VALUE;
	}

}
