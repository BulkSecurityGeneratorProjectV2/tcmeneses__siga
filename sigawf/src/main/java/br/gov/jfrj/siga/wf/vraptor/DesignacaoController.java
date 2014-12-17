/*******************************************************************************
 * Copyright (c) 2006 - 2011 SJRJ.
 * 
 *     This file is part of SIGA.
 * 
 *     SIGA is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     SIGA is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with SIGA.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package br.gov.jfrj.siga.wf.vraptor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.SessionFactory;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.taskmgmt.def.Swimlane;
import org.jbpm.taskmgmt.def.Task;

import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.cp.CpConfiguracao;
import br.gov.jfrj.siga.cp.CpTipoConfiguracao;
import br.gov.jfrj.siga.dp.CpOrgaoUsuario;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.dp.dao.CpDao;
import br.gov.jfrj.siga.vraptor.SigaObjects;
import br.gov.jfrj.siga.wf.Designacao;
import br.gov.jfrj.siga.wf.WfConfiguracao;
import br.gov.jfrj.siga.wf.bl.Wf;
import br.gov.jfrj.siga.wf.dao.WfDao;
import br.gov.jfrj.siga.wf.util.WfContextBuilder;
import br.gov.jfrj.siga.wf.util.WfTipoResponsavel;

@Resource
public class DesignacaoController extends WfController {

	private static int TIPO_RESP_INDEFINIDO = 0;
	private static int TIPO_RESP_MATRICULA = 1;
	private static int TIPO_RESP_LOTACAO = 2;
	private static int TIPO_RESP_LOTA_SUP = 3;
	private static int TIPO_RESP_SUP_HIER = 4;
	private static int TIPO_RESP_EXPRESSAO = 5;

	private List<WfTipoResponsavel> listaTipoResponsavel = new ArrayList<WfTipoResponsavel>();
	private Map<Integer, WfTipoResponsavel> mapaTipoResponsavel = new HashMap<Integer, WfTipoResponsavel>();

	// private Set<String> raiaProcessada = new HashSet<String>();

	/**
	 * Inicializa os tipos de respons�veis e suas respectivas express�es, quando
	 * houver.
	 */
	public DesignacaoController(HttpServletRequest request, Result result,
			WfDao dao, SigaObjects so, WfUtil util) {
		super(request, result, dao, so, util);

		WfTipoResponsavel tpIndefinido = new WfTipoResponsavel(
				TIPO_RESP_INDEFINIDO, "[Indefinido]", "");
		WfTipoResponsavel tpMatricula = new WfTipoResponsavel(
				TIPO_RESP_MATRICULA, "Matr�cula", "matricula");
		WfTipoResponsavel tpLotacao = new WfTipoResponsavel(TIPO_RESP_LOTACAO,
				"Lota��o", "lotacao");
		WfTipoResponsavel tpLotaSuperior = new WfTipoResponsavel(
				TIPO_RESP_LOTA_SUP, "Lota��o Superior",
				"previous --> group() --> superior_group");
		WfTipoResponsavel tpSupHier = new WfTipoResponsavel(TIPO_RESP_SUP_HIER,
				"Superior Hier�rquico", "previous --> chief");
		WfTipoResponsavel tpExpressao = new WfTipoResponsavel(
				TIPO_RESP_EXPRESSAO, "Express�o", "");

		listaTipoResponsavel.add(tpIndefinido);
		listaTipoResponsavel.add(tpMatricula);
		listaTipoResponsavel.add(tpLotacao);
		listaTipoResponsavel.add(tpLotaSuperior);
		listaTipoResponsavel.add(tpSupHier);
		listaTipoResponsavel.add(tpExpressao);

		for (WfTipoResponsavel tr : listaTipoResponsavel) {
			mapaTipoResponsavel.put(tr.getId(), tr);
		}
	}

	/**
	 * Grava a configura��o da designa��o. Inicialmente processa-se as raias e
	 * depois as tarefas, desativa-se as configura��es antigas e retorna �
	 * p�gina pesquisarDesigna��o.jsp com os dados gravados.
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
		Set<Long> raiasGravadas = new HashSet<Long>();

		List<Designacao> listaDesignacaoRaia = getDesignacaoSwimlanes(
				orgaoUsuario, procedimento, pd);
		for (Designacao d : listaDesignacaoRaia) {
			d.setAtor(null);
			d.setLotaAtor(null);
			d.setExpressao(null);
			d.setTipoResponsavel(null);
		}
		List<Designacao> listaDesignacaoTarefa = getDesignacaoTarefas(
				orgaoUsuario, procedimento, pd);
		for (Designacao d : listaDesignacaoTarefa) {
			d.setAtor(null);
			d.setLotaAtor(null);
			d.setExpressao(null);
			d.setTipoResponsavel(null);
		}

		if (pd != null) {
			for (Object o : pd.getTaskMgmtDefinition().getTasks().values()) {
				Task t = (Task) o;

				WfConfiguracao cfg = prepararConfiguracao(orgaoUsuario,
						procedimento);

				if (t.getSwimlane() != null) { // se task est� em uma raia
					listaDesignacaoRaia = processarRaia(orgaoUsuario,
							procedimento, t.getSwimlane(), cfg, horaDoDB,
							raiasGravadas, listaDesignacaoRaia);
				} else {
					listaDesignacaoTarefa = processarTarefa(orgaoUsuario,
							procedimento, t, cfg, horaDoDB,
							listaDesignacaoTarefa);
				}

			}

			limparCache();
		}

		result.redirectTo(this).pesquisar(orgao, procedimento);
	}

	/**
	 * L� as defini��es gravadas no banco e exibe na p�gina.
	 * 
	 * @return
	 * @throws Exception
	 */
	public void pesquisar(String orgao, String procedimento) throws Exception {
		assertAcesso("DESIGNAR:Designar tarefas");

		limparCache();
		ProcessDefinition pd = WfContextBuilder.getJbpmContext()
				.getJbpmContext().getGraphSession()
				.findLatestProcessDefinition(procedimento);
		CpOrgaoUsuario orgaoUsuario = daoOU(orgao);

		if (pd != null) {
			List<Designacao> listaDesignacaoRaia = new ArrayList<Designacao>();
			listaDesignacaoRaia.addAll(getDesignacaoSwimlanes(orgaoUsuario,
					procedimento, pd));
			List<Designacao> listaDesignacaoTarefa = new ArrayList<Designacao>();
			listaDesignacaoTarefa.addAll(getDesignacaoTarefas(orgaoUsuario,
					procedimento, pd));
			result.include("orgao", orgao);
			result.include("procedimento", procedimento);
			result.include("listaDesignacaoRaia", listaDesignacaoRaia);
			result.include("listaDesignacaoTarefa", listaDesignacaoTarefa);
			result.include("listaOrgao", dao().listarOrgaosUsuarios());
			result.include("listaTipoResponsavel", listaTipoResponsavel);
			result.include("listaProcedimento", getListaProcedimento());
			result.include("mapaDesignacaoRaia",
					getMapaDesignacaoRaia(listaDesignacaoRaia));
		}
	}

	private Map<String, List<Designacao>> getMapaDesignacaoRaia(
			List<Designacao> listaDesignacaoRaia) {
		Map<String, List<Designacao>> m = new TreeMap<String, List<Designacao>>();
		for (Designacao d : listaDesignacaoRaia) {
			if (!m.containsKey(d.getRaia())) {
				m.put(d.getRaia(), new ArrayList<Designacao>());
			}
			m.get(d.getRaia()).add(d);
		}
		return m;
	}

	/**
	 * Como na p�gina pesquisarDesigna��o.jsp os componentes de sele��o das
	 * express�es s�o din�micas, � necess�ria a extra��o dos dados diretamente
	 * dos par�metros do request. O prefixo "tipoResponsavel_" � definido na
	 * p�gina pesquisarDesignacao.jsp
	 * 
	 * @param id
	 *            da tarefa
	 * @return Um objeto String com a express�o que equivale ao item
	 *         selecionado.
	 */
	private String extrairExpressao(long id) {

		String keyExpressao = "tipoResponsavel_" + id;
		String expressaoId = "expressao_" + id;
		Map<?, ?> parametros = this.getRequest().getParameterMap();
		String expressao = null;

		if (!this.getRequest().getParameterMap().containsKey(keyExpressao))
			return null;

		String responsavel = ((String[]) parametros.get(keyExpressao))[0];
		if (responsavel == null || responsavel.equals(""))
			return null;

		int idResp = new Integer(responsavel).intValue();

		if (idResp != TIPO_RESP_LOTA_SUP && idResp != TIPO_RESP_SUP_HIER
				&& idResp != TIPO_RESP_EXPRESSAO)
			return null;

		for (WfTipoResponsavel tr : listaTipoResponsavel) {
			if (tr.getId() == idResp) {
				expressao = tr.getValor();
				if (expressao == null || expressao.length() == 0) {
					expressao = ((String[]) parametros.get(expressaoId))[0];
				}
			}
		}
		return expressao;
	}

	/**
	 * Busca um configura��o do tipo TIPO_CONFIG_DESIGNAR_TAREFA no banco de
	 * dados, caso exista.
	 * 
	 * @param nome
	 *            - Nome da raia ou tarefa.
	 * @param isRaia
	 *            - Determina se o par�metro anterior � uma raia ou tarefa.
	 * @return
	 * @throws Exception
	 */
	private WfConfiguracao getConfiguracaoExistente(
			CpOrgaoUsuario orgaoUsuario, String procedimento, String nome,
			boolean isRaia) throws Exception {
		WfConfiguracao fltConfigExistente = new WfConfiguracao();

		CpTipoConfiguracao tipoConfig = CpDao.getInstance().consultar(
				CpTipoConfiguracao.TIPO_CONFIG_DESIGNAR_TAREFA,
				CpTipoConfiguracao.class, false);

		fltConfigExistente.setCpTipoConfiguracao(tipoConfig);
		fltConfigExistente.setOrgaoUsuario(orgaoUsuario);
		fltConfigExistente.setProcedimento(procedimento);

		if (isRaia) {
			fltConfigExistente.setRaia(nome);
		} else {
			fltConfigExistente.setTarefa(nome);
		}

		CpConfiguracao cfgExistente = Wf.getInstance().getConf()
				.buscaConfiguracao(fltConfigExistente, new int[] { 0 }, null);

		return (WfConfiguracao) cfgExistente;
	}

	/**
	 * Monta a lista de designa��es que se referem �s raias.
	 * 
	 * @param pd
	 *            - Objeto ProcessDefinition
	 * @return - Lista de designa��es
	 * @throws Exception
	 */
	private List<Designacao> getDesignacaoSwimlanes(
			CpOrgaoUsuario orgaoUsuario, String procedimento,
			ProcessDefinition pd) throws Exception {

		WfConfiguracao cfgFiltro = new WfConfiguracao();

		CpTipoConfiguracao tipoConfig = CpDao.getInstance().consultar(
				CpTipoConfiguracao.TIPO_CONFIG_DESIGNAR_TAREFA,
				CpTipoConfiguracao.class, false);

		cfgFiltro.setCpTipoConfiguracao(tipoConfig);
		cfgFiltro.setOrgaoUsuario(orgaoUsuario);
		cfgFiltro.setProcedimento(procedimento);

		List<Designacao> resultado = new ArrayList<Designacao>();

		for (Object s : pd.getTaskMgmtDefinition().getSwimlanes().values()) {
			Swimlane raia = (Swimlane) s;

			for (Object t : raia.getTasks()) {
				Task tarefa = (Task) t;
				Designacao d = new Designacao();
				d.setProcedimento(procedimento);
				d.setId(tarefa.getId());
				d.setNomeDoNo(tarefa.getTaskNode() == null ? "" : tarefa
						.getTaskNode().getName());
				d.setTarefa(tarefa.getName());
				d.setRaia(raia.getName());
				cfgFiltro.setRaia(raia.getName());

				WfConfiguracao cfg = (WfConfiguracao) Wf.getInstance()
						.getConf()
						.buscaConfiguracao(cfgFiltro, new int[] { 0 }, null);

				if (cfg != null) {
					d.setAtor(cfg.getAtor());
					d.setLotaAtor(cfg.getLotaAtor());

					if (cfg.getExpressao() != null) {
						d.setExpressao(cfg.getExpressao());
						for (WfTipoResponsavel tr : listaTipoResponsavel) {
							if (tr.getValor().equals(d.getExpressao())) {
								d.setTipoResponsavel(tr.getId());
							}
						}
						if (d.getTipoResponsavel() == null) {
							d.setTipoResponsavel(TIPO_RESP_EXPRESSAO);
							d.setExpressao(cfg.getExpressao());
						}
					}
				}

				if (d.getAtor() != null) {
					d.setTipoResponsavel(TIPO_RESP_MATRICULA);
				}
				if (d.getLotaAtor() != null) {
					d.setTipoResponsavel(TIPO_RESP_LOTACAO);
				}

				resultado.add(d);
			}

		}

		return resultado;
	}

	/**
	 * Monta a lista de designa��es que se referem �s tarefas.
	 * 
	 * @param pd
	 *            - Objeto ProcessDefinition
	 * @return - Lista de designa��es
	 * @throws Exception
	 */
	private List<Designacao> getDesignacaoTarefas(CpOrgaoUsuario orgaoUsuario,
			String procedimento, ProcessDefinition pd) throws Exception {

		WfConfiguracao cfgFiltro = new WfConfiguracao();

		CpTipoConfiguracao tipoConfig = CpDao.getInstance().consultar(
				CpTipoConfiguracao.TIPO_CONFIG_DESIGNAR_TAREFA,
				CpTipoConfiguracao.class, false);

		cfgFiltro.setCpTipoConfiguracao(tipoConfig);
		cfgFiltro.setOrgaoUsuario(orgaoUsuario);
		cfgFiltro.setProcedimento(procedimento);

		List<Designacao> resultado = new ArrayList<Designacao>();
		for (Object o : pd.getTaskMgmtDefinition().getTasks().values()) {
			Task t = (Task) o;
			if (t.getSwimlane() == null) {
				Designacao d = new Designacao();
				d.setProcedimento(procedimento);
				d.setId(t.getId());
				d.setNomeDoNo(t.getTaskNode() == null ? "" : t.getTaskNode()
						.getName());
				d.setTarefa(t.getName());
				cfgFiltro.setTarefa(d.getTarefa());

				WfConfiguracao cfg = (WfConfiguracao) Wf.getInstance()
						.getConf()
						.buscaConfiguracao(cfgFiltro, new int[] { 0 }, null);

				if (cfg != null) {
					d.setAtor(cfg.getAtor());
					d.setLotaAtor(cfg.getLotaAtor());

					if (cfg.getExpressao() != null) {
						d.setExpressao(cfg.getExpressao());
						for (WfTipoResponsavel tr : listaTipoResponsavel) {
							if (tr.getValor().equals(d.getExpressao())) {
								d.setTipoResponsavel(tr.getId());
							}
						}
						if (d.getTipoResponsavel() == null) {
							d.setTipoResponsavel(TIPO_RESP_EXPRESSAO);
							d.setExpressao(cfg.getExpressao());
						}
					}
				}
				if (d.getAtor() != null) {
					d.setTipoResponsavel(TIPO_RESP_MATRICULA);
				}
				if (d.getLotaAtor() != null) {
					d.setTipoResponsavel(TIPO_RESP_LOTACAO);
				}

				resultado.add(d);
			}
		}

		return resultado;

	}

	/**
	 * Retorna a lista de tipos de respons�veis que podem ter designa��es
	 * definidas. Este m�todo � usado pela p�gina pesquisarDesigna��o.jsp
	 * 
	 * @return Lista de designa��es
	 */
	private List<WfTipoResponsavel> getListaTipoResponsavel() {
		return listaTipoResponsavel;
	}

	/**
	 * Grava a nova configura��o no banco de dados.
	 * 
	 * @param cfg
	 *            - Configura��o a ser gravada
	 * @throws Exception
	 */
	private void gravarNovaConfig(WfConfiguracao cfg) throws Exception {
		WfDao.getInstance().gravarComHistorico(cfg, getIdentidadeCadastrante());
	}

	/**
	 * Desativa uma configura��o existente.
	 * 
	 * @param cfgExistente
	 *            - objecto da configura��o existente
	 * @param dataFim
	 *            - data em que a configura��o ser� desativada
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
	 * Limpa o cache do hibernate. Como as configura��es s�o armazenadas em
	 * cache por serem pouco modificadas, faz-se necess�rio limpar o cache
	 * quando as configura��es s�o alteradas ou inclu�das, sen�o as
	 * configura��es somente ser�o v�lidas quando o session factory for
	 * reinicializado.
	 * 
	 * @throws Exception
	 */
	private void limparCache() throws Exception {

		SessionFactory sfWfDao = WfDao.getInstance().getSessao()
				.getSessionFactory();
		SessionFactory sfCpDao = CpDao.getInstance().getSessao()
				.getSessionFactory();

		CpTipoConfiguracao tipoConfig = CpDao.getInstance().consultar(
				CpTipoConfiguracao.TIPO_CONFIG_DESIGNAR_TAREFA,
				CpTipoConfiguracao.class, false);

		Wf.getInstance().getConf().evictListaPorTipo(tipoConfig);

		sfWfDao.evict(CpConfiguracao.class);
		sfWfDao.evict(WfConfiguracao.class);
		sfCpDao.evict(DpLotacao.class);

		sfWfDao.evictQueries(CpDao.CACHE_QUERY_CONFIGURACAO);

		return;

	}

	/**
	 * Prepara um objeto para receber uma nova configura��o.
	 * 
	 * @return Objeto apto a receber uma configura��o.
	 */
	private WfConfiguracao prepararConfiguracao(CpOrgaoUsuario orgaoUsuario,
			String procedimento) {
		WfConfiguracao cfg = new WfConfiguracao();
		CpTipoConfiguracao tipoConfig = CpDao.getInstance().consultar(
				CpTipoConfiguracao.TIPO_CONFIG_DESIGNAR_TAREFA,
				CpTipoConfiguracao.class, false);
		cfg.setCpTipoConfiguracao(tipoConfig);
		cfg.setOrgaoUsuario(orgaoUsuario);
		cfg.setProcedimento(procedimento);
		return cfg;
	}

	/**
	 * Processa as designa��es definidas para raia extraindo as informa��es do
	 * request.
	 * 
	 * @param raia
	 *            - Raia a ser processada
	 * @param cfg
	 *            - Configura��o pr�-processada que receber� as informa��es de
	 *            designa��o.
	 * @param horaDoBD
	 *            - Hora que ser� utilizada em todas as opera��es do banco de
	 *            dados fazendo com que todas as altera��es tenham o mesmo
	 *            hor�rio.
	 * @param raiasGravadas
	 * @param listaDesignacaoRaia
	 * @return
	 * @throws Exception
	 */
	private List<Designacao> processarRaia(CpOrgaoUsuario orgaoUsuario,
			String procedimento, Swimlane raia, WfConfiguracao cfg,
			Date horaDoBD, Set<Long> raiasGravadas,
			List<Designacao> listaDesignacaoRaia) throws Exception {

		if (!raiasGravadas.contains(raia.getId())) { // se a swimlane n�o foi
			// gravada ainda

			for (Object o : raia.getTasks()) {
				Task tarefaRaia = (Task) o;

				DpLotacao lotaAtor = null;
				DpPessoa ator = null;
				String expressao = null;

				lotaAtor = extrairLotaAtor(tarefaRaia.getId());
				ator = extrairAtor(tarefaRaia.getId());
				expressao = extrairExpressao(tarefaRaia.getId());

				if (ator != null || lotaAtor != null || expressao != null) {// se
					// configura��o
					// definida
					cfg.setAtor(ator);
					cfg.setLotaAtor(lotaAtor);
					cfg.setExpressao(expressao);

					cfg.setRaia(raia.getName());
					cfg.setHisDtIni(horaDoBD);

					WfConfiguracao cfgExistente = getConfiguracaoExistente(
							orgaoUsuario, procedimento, raia.getName(), true);
					invalidarConfiguracao(cfgExistente, horaDoBD);

					gravarNovaConfig(cfg);

					// atualiza os dados da vis�o
					for (Designacao d : listaDesignacaoRaia) {
						if (d.getRaia().equals(
								tarefaRaia.getSwimlane().getName())) {
							d.setAtor(null);
							d.setLotaAtor(null);
							d.setExpressao(null);
							if (ator != null) {
								d.setAtor(ator);
								d.setTipoResponsavel(TIPO_RESP_MATRICULA);
							}

							if (lotaAtor != null) {
								d.setLotaAtor(lotaAtor);
								d.setTipoResponsavel(TIPO_RESP_LOTACAO);
							}

							if (expressao != null) {
								d.setExpressao(expressao);
								for (WfTipoResponsavel tr : listaTipoResponsavel) {
									if (tr.getValor().equals(expressao)) {
										d.setTipoResponsavel(tr.getId());
									}
								}
							}
						}
					}
					break;

				} else { // configuracao indefinida
					WfConfiguracao cfgExistente = getConfiguracaoExistente(
							orgaoUsuario, procedimento, raia.getName(), true);
					invalidarConfiguracao(cfgExistente, horaDoBD);
				}

			}

			raiasGravadas.add(raia.getId());

		}

		return listaDesignacaoRaia;
	}

	/**
	 * Processa as designa��es definidas para a tarefa extraindo as informa��es
	 * do request.
	 * 
	 * @param t
	 *            - Tarefa a ser processada
	 * @param cfg
	 *            - Configura��o pr�-processada que receber� as informa��es de
	 *            designa��o.
	 * @param horaDoBD
	 *            - Hora que ser� utilizada em todas as opera��es do banco de
	 *            dados fazendo com que todas as altera��es tenham o mesmo
	 *            hor�rio.
	 * @param listaDesignacaoTarefa
	 * @return
	 * @throws Exception
	 */
	private List<Designacao> processarTarefa(CpOrgaoUsuario orgaoUsuario,
			String procedimento, Task t, WfConfiguracao cfg, Date horaDoBD,
			List<Designacao> listaDesignacaoTarefa) throws Exception {
		DpLotacao lotaAtor = null;
		DpPessoa ator = null;
		String expressao = null;

		lotaAtor = extrairLotaAtor(t.getId());
		ator = extrairAtor(t.getId());
		expressao = extrairExpressao(t.getId());

		if (ator != null || lotaAtor != null || expressao != null) {// se
			// configura��o
			// definida
			cfg.setAtor(ator);
			cfg.setLotaAtor(lotaAtor);
			cfg.setExpressao(expressao);

			cfg.setTarefa(t.getName());
			cfg.setHisDtIni(horaDoBD);

			WfConfiguracao cfgExistente = getConfiguracaoExistente(
					orgaoUsuario, procedimento, t.getName(), false);
			invalidarConfiguracao(cfgExistente, horaDoBD);

			gravarNovaConfig(cfg);
			// atualiza os dados da vis�o
			for (Designacao d : listaDesignacaoTarefa) {
				if (d.getTarefa().equals(t.getName())) {
					d.setAtor(null);
					d.setLotaAtor(null);
					d.setExpressao(null);
					if (ator != null) {
						d.setAtor(ator);
						d.setTipoResponsavel(TIPO_RESP_MATRICULA);
					}

					if (lotaAtor != null) {
						d.setLotaAtor(lotaAtor);
						d.setTipoResponsavel(TIPO_RESP_LOTACAO);
					}

					if (expressao != null) {
						d.setExpressao(expressao);
						for (WfTipoResponsavel tr : listaTipoResponsavel) {
							if (tr.getValor().equals(expressao)) {
								d.setTipoResponsavel(tr.getId());
							}
						}
					}

				}
			}

		} else { // configuracao indefinida
			WfConfiguracao cfgExistente = getConfiguracaoExistente(
					orgaoUsuario, procedimento, t.getName(), false);
			invalidarConfiguracao(cfgExistente, horaDoBD);
		}
		return listaDesignacaoTarefa;

	}

}
