package br.gov.jfrj.siga.gc.vraptor;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;

import br.com.caelum.vraptor.Path;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.interceptor.download.ByteArrayDownload;
import br.com.caelum.vraptor.interceptor.download.Download;
import br.com.caelum.vraptor.interceptor.multipart.UploadedFile;
import br.com.caelum.vraptor.view.Results;
import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.cp.CpIdentidade;
import br.gov.jfrj.siga.dp.CpMarcador;
import br.gov.jfrj.siga.dp.CpOrgaoUsuario;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.gc.model.GcAcesso;
import br.gov.jfrj.siga.gc.model.GcArquivo;
import br.gov.jfrj.siga.gc.model.GcInformacao;
import br.gov.jfrj.siga.gc.model.GcMovimentacao;
import br.gov.jfrj.siga.gc.model.GcTag;
import br.gov.jfrj.siga.gc.model.GcTipoInformacao;
import br.gov.jfrj.siga.gc.model.GcTipoMovimentacao;
import br.gov.jfrj.siga.gc.model.GcTipoTag;
import br.gov.jfrj.siga.gc.util.GcArvore;
import br.gov.jfrj.siga.gc.util.GcBL;
import br.gov.jfrj.siga.gc.util.GcCloud;
import br.gov.jfrj.siga.gc.util.GcGraficoEvolucao;
import br.gov.jfrj.siga.gc.util.GcGraficoEvolucaoItem;
import br.gov.jfrj.siga.gc.util.GcInformacaoFiltro;
import br.gov.jfrj.siga.gc.util.diff_match_patch;
import br.gov.jfrj.siga.gc.util.diff_match_patch.Diff;
import br.gov.jfrj.siga.gc.util.diff_match_patch.Operation;
import br.gov.jfrj.siga.model.DadosRI;
import br.gov.jfrj.siga.vraptor.SigaObjects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Resource
public class AppController extends GcController {

	private GcBL bl;
	private Correio correio;

	public AppController(HttpServletRequest request, Result result, GcBL bl,
			SigaObjects so, EntityManager em, Correio correio) {
		super(request, result, so, em);
		this.bl = bl;
		this.correio = correio;

	}

	private static final String HTTP_LOCALHOST_8080 = "http://localhost:8080";
	private static final int CONTROLE_HASH_TAG = 1;

	public void gadget() {
		Query query = em().createNamedQuery("contarGcMarcas");
		query.setParameter("idPessoaIni", getCadastrante().getIdInicial());
		query.setParameter("idLotacaoIni", getLotaTitular().getIdInicial());
		List contagens = query.getResultList();
		result.include("contagens", contagens);
	}

	@Path("/public/app/knowledge")
	public void publicKnowledge(Long id, String[] tags, String estilo,
			String msgvazio, String urlvazio, String titulo, boolean popup,
			String estiloBusca) throws Exception {
		renderKnowledge(id, tags, estilo, msgvazio, urlvazio, titulo, true,
				popup, estiloBusca);
	}

	public void knowledge(Long id, String[] tags, String msgvazio,
			String urlvazio, String titulo, boolean popup, String estiloBusca)
			throws Exception {
		renderKnowledge(id, tags, null, msgvazio, urlvazio, titulo, false,
				popup, estiloBusca);
	}

	public void knowledge_inplace(Long id, String[] tags, String msgvazio,
			String urlvazio, String titulo, boolean popup, String estiloBusca)
			throws Exception {
		renderKnowledge(id, tags, "inplace", msgvazio, urlvazio, titulo, false,
				popup, estiloBusca);
	}

	public void knowledge_sidebar(Long id, String[] tags, String msgvazio,
			String urlvazio, String titulo, boolean popup, String estiloBusca)
			throws Exception {
		renderKnowledge(id, tags, "sidebar", msgvazio, urlvazio, titulo, false,
				popup, estiloBusca);
	}

	private void renderKnowledge(Long id, String[] tags, String estilo,
			String msgvazio, String urlvazio, String titulo,
			boolean testarAcessoPublico, boolean popup, String estiloBusca)
			throws UnsupportedEncodingException, Exception {
		int index = Integer.MAX_VALUE;
		Long idOutroConhecimento = 0l;
		GcInformacao info = null;
		Set<GcTag> set = null;
		if (tags != null)
			set = bl.buscarTags(tags, true);
		estiloBusca = estiloBusca != null ? estiloBusca.substring(0, 1)
				.toUpperCase() + estiloBusca.substring(1) : "";
		Query query = em().createNamedQuery("buscarConhecimento" + estiloBusca);
		query.setParameter("tags", set);
		List<Object[]> conhecimentosCandidatos = query.getResultList();
		List<Object[]> conhecimentos = new ArrayList<Object[]>();
		for (Object[] o : conhecimentosCandidatos) {
			idOutroConhecimento = Long.parseLong(o[0].toString());
			if (idOutroConhecimento.equals(id))
				continue;

			info = GcInformacao.AR.findById(idOutroConhecimento);

			if (testarAcessoPublico
					&& (info.visualizacao.id != GcAcesso.ACESSO_PUBLICO))
				continue;

			// o[3] = URLEncoder.encode(info.getSigla(), "UTF-8");
			o[3] = info.getSigla();
			if (o[2] != null && o[2] instanceof byte[]) {
				String s = new String((byte[]) o[2], Charset.forName("utf-8"));
				s = bl.ellipsize(s, 100);
				o[2] = s;
			}
			conhecimentos.add(o);
		}

		if (conhecimentos.size() == 1 && "inplace".equals(estilo)) {
			GcInformacao inf = GcInformacao.AR
					.findById(conhecimentos.get(0)[0]);
			conhecimentos.get(0)[1] = inf.arq.titulo;
			conhecimentos.get(0)[2] = inf.getConteudoHTML();
		}

		if (conhecimentos.size() == 0)
			conhecimentos = null;

		String referer = null;
		try {
			referer = getRequest().getHeader("referer");
		} catch (Exception e) {

		}

		String classificacao = "";
		if (tags != null && tags.length > 0) {
			for (String s : tags) {
				if (classificacao.length() > 0)
					classificacao += ", ";
				classificacao += s;
			}
		}
		// Necess�rio para criar um novo conhecimento a partir de um j�
		// existente, a classifica��o
		// � passada como queryString. Sem fazer isso, as hashTags n�o s�o
		// passadas.
		// classificacao = URLEncoder.encode(classificacao, "UTF-8");
		// if (msgvazio != null) {
		// msgvazio = msgvazio.replace("*aqui*", "<a href=\"" + urlvazio +
		// "\">aqui</a>");
		// }

		result.include("conhecimentos", conhecimentos);
		result.include("classificacao", classificacao);
		result.include("msgvazio", msgvazio);
		result.include("urlvazio", urlvazio);
		result.include("titulo", titulo);
		result.include("referer", referer);
		result.include("popup", popup);
	}

	public void updateTag(String before, String after) {

		// Edson: Atualizando tags de classificacao:
		em().createQuery(
				"update GcTag set titulo = '" + after + "' where titulo = '"
						+ before + "'").executeUpdate();

		// Edson: Atualizando tags de ancora. O problema aqui eh que,
		// muitas vezes, o before aparece em tags ancora acompanhado de
		// outra classificacao. Por exemplo, cadeira-consertar, onde
		// before eh consertar. Os patterns abaixo funcionam, mas nao
		// sempre. Por exemplo, se houver uma tag cadeira-tentar-consertar
		// alem da cadeira-consertar, ela vai ser erroneamente atualizada.
		List<GcTag> tags = em().createQuery(
				"from GcTag where titulo like '%" + before
						+ "%' and tipo.id = 3").getResultList();
		for (GcTag t : tags) {
			t.titulo = t.titulo.replaceAll("^" + before + "(-.+|$)", after
					+ "$1");
			t.titulo = t.titulo.replaceAll("(.+-|^)" + before + "$", "$1"
					+ after);
			t.save();
		}

		// Edson: Atualizando os arquivos:
		List<GcArquivo> arqs = em()
				.createQuery(
						"select arq from GcInformacao inf inner join inf.arq arq"
								+ " where inf.hisDtFim is null and arq.classificacao like '%"
								+ before + "%'").getResultList();
		for (GcArquivo arq : arqs) {
			if (arq.classificacao.startsWith("^")) {
				arq.classificacao = arq.classificacao.replace(":" + before, ":"
						+ after);
				arq.classificacao = arq.classificacao.replaceAll("-" + before
						+ "$", "-" + after);
			} else {
				arq.classificacao = arq.classificacao.replaceAll("(@" + before
						+ ")(,|$)", "@" + after + "$2");
			}
			arq.save();
		}

		result.use(Results.http()).body("OK");
	}

	public void index() throws Exception {
		result.redirectTo(this).estatisticaGeral();
	}

	public void estatisticaGeral() throws Exception {
		// List<GcInformacao> lista = GcInformacao.all().fetch();

		Query query1 = em().createNamedQuery("maisRecentes");
		query1.setMaxResults(5);
		List<Object[]> listaMaisRecentes = query1.getResultList();
		if (listaMaisRecentes.size() == 0)
			listaMaisRecentes = null;

		Query query2 = em().createNamedQuery("maisVisitados");
		query2.setMaxResults(5);
		List<Object[]> listaMaisVisitados = query2.getResultList();
		if (listaMaisVisitados.size() == 0)
			listaMaisVisitados = null;

		Query query3 = em().createNamedQuery("principaisAutores");
		query3.setMaxResults(5);
		List<Object[]> listaPrincipaisAutores = query3.getResultList();
		if (listaPrincipaisAutores.size() == 0)
			listaPrincipaisAutores = null;

		Query query4 = em().createNamedQuery("principaisLotacoes");
		query4.setMaxResults(5);
		List<Object[]> listaPrincipaisLotacoes = query4.getResultList();
		if (listaPrincipaisLotacoes.size() == 0)
			listaPrincipaisLotacoes = null;

		GcCloud cloud = new GcCloud(150.0, 60.0);
		Query query5 = em().createNamedQuery("principaisTags");
		query5.setMaxResults(50);
		List<Object[]> listaPrincipaisTags = query5.getResultList();
		if (listaPrincipaisTags.size() == 0)
			listaPrincipaisTags = null;
		else {
			for (Object[] t : listaPrincipaisTags) {
				cloud.criarCloud(t, null);
			}
		}
		GcGraficoEvolucao set = new GcGraficoEvolucao();
		Query query6 = em().createNamedQuery("evolucaoNovos");
		List<Object[]> listaNovos = query6.getResultList();
		for (Object[] novos : listaNovos) {
			set.add(new GcGraficoEvolucaoItem((Integer) novos[0],
					(Integer) novos[1], (Long) novos[2], 0, 0));
		}

		Query query7 = em().createNamedQuery("evolucaoVisitados");
		List<Object[]> listaVisitados = query7.getResultList();
		for (Object[] visitados : listaVisitados) {
			set.add(new GcGraficoEvolucaoItem((Integer) visitados[0],
					(Integer) visitados[1], 0, (Long) visitados[2], 0));
		}
		String evolucao = set.criarGrafico();

		result.include("listaMaisRecentes", listaMaisRecentes);
		result.include("listaMaisVisitados", listaMaisVisitados);
		result.include("listaPrincipaisAutores", listaPrincipaisAutores);
		result.include("listaPrincipaisLotacoes", listaPrincipaisLotacoes);
		result.include("listaPrincipaisTags", listaPrincipaisTags);
		result.include("cloud", cloud);
		result.include("evolucao", evolucao);
	}

	public void estatisticaLotacao() throws Exception {
		// List<GcInformacao> lista = GcInformacao.all().fetch();

		DpLotacao lotacao = getLotaTitular();

		Query query1 = em().createNamedQuery("maisRecentesLotacao");
		// query1.setParameter("idLotacao", lotacao.getId());
		query1.setParameter("idlotacaoInicial", lotacao.getIdLotacaoIni());
		query1.setMaxResults(5);
		List<Object[]> listaMaisRecentes = query1.getResultList();
		if (listaMaisRecentes.size() == 0)
			listaMaisRecentes = null;

		Query query2 = em().createNamedQuery("maisVisitadosLotacao");
		query2.setParameter("idlotacaoInicial", lotacao.getIdLotacaoIni());
		query2.setMaxResults(5);
		List<Object[]> listaMaisVisitados = query2.getResultList();
		if (listaMaisVisitados.size() == 0)
			listaMaisVisitados = null;

		Query query3 = em().createNamedQuery("principaisAutoresLotacao");
		query3.setParameter("idlotacaoInicial", lotacao.getIdLotacaoIni());
		query3.setMaxResults(5);
		List<Object[]> listaPrincipaisAutores = query3.getResultList();
		if (listaPrincipaisAutores.size() == 0)
			listaPrincipaisAutores = null;

		GcCloud cloud = new GcCloud(150.0, 60.0);
		Query query4 = em().createNamedQuery("principaisTagsLotacao");
		query4.setParameter("idlotacaoInicial", lotacao.getIdLotacaoIni());
		query4.setMaxResults(50);
		List<Object[]> listaPrincipaisTags = query4.getResultList();
		if (listaPrincipaisTags.size() == 0)
			listaPrincipaisTags = null;
		else {
			for (Object[] t : listaPrincipaisTags) {
				cloud.criarCloud(t, lotacao.getId());
			}
		}
		GcGraficoEvolucao set = new GcGraficoEvolucao();
		Query query5 = em().createNamedQuery("evolucaoNovosLotacao");
		query5.setParameter("idlotacaoInicial", lotacao.getIdLotacaoIni());
		List<Object[]> listaNovos = query5.getResultList();
		for (Object[] novos : listaNovos) {
			set.add(new GcGraficoEvolucaoItem((Integer) novos[0],
					(Integer) novos[1], (Long) novos[2], 0, 0));
		}

		Query query6 = em().createNamedQuery("evolucaoVisitadosLotacao");
		query6.setParameter("idlotacaoInicial", lotacao.getIdLotacaoIni());
		List<Object[]> listaVisitados = query6.getResultList();
		for (Object[] visitados : listaVisitados) {
			set.add(new GcGraficoEvolucaoItem((Integer) visitados[0],
					(Integer) visitados[1], 0, (Long) visitados[2], 0));
		}
		String evolucao = set.criarGrafico();

		result.include("lotacao", lotacao);
		result.include("listaMaisRecentes", listaMaisRecentes);
		result.include("listaMaisVisitados", listaMaisVisitados);
		result.include("listaPrincipaisAutores", listaPrincipaisAutores);
		result.include("listaPrincipaisTags", listaPrincipaisTags);
		result.include("cloud", cloud);
		result.include("evolucao", evolucao);
	}

	public void listar(GcInformacaoFiltro filtro, int estatistica) {
		List<GcInformacao> lista;
		if (filtro.pesquisa)
			lista = filtro.buscar();
		else
			lista = new ArrayList<GcInformacao>();

		// Montando o filtro...
		// String[] tipos = new String[] { "Pessoa", "Lotaç�o" };
		List<CpMarcador> marcadores = em().createQuery(
				"select distinct cpMarcador from GcMarca").getResultList();
		em().createQuery("select cp from CpOrgaoUsuario cp").getResultList();
		List<CpOrgaoUsuario> orgaosusuarios = CpOrgaoUsuario.AR.all().fetch();

		List<GcTipoInformacao> tiposinformacao = GcTipoInformacao.AR.all()
				.fetch();

		List<Integer> anos = new ArrayList<Integer>();
		int ano = bl.dt().getYear() + 1900;
		for (int i = 0; i < 10; i++) {
			anos.add(ano - i);
		}

		if (filtro == null)
			filtro = new GcInformacaoFiltro();

		result.include("lista", lista);
		result.include("marcadores", marcadores);
		result.include("filtro", filtro);
		result.include("orgaosusuarios", orgaosusuarios);
		result.include("tiposinformacao", tiposinformacao);
		result.include("anos", anos);
		result.include("estatistica", estatistica);
	}

	public void navegar() {
		GcArvore arvore = new GcArvore();

		// List<GcInformacao> infs = GcInformacao.all().fetch();
		// n�o exibe conhecimentos cancelados
		List<GcInformacao> infs = GcInformacao.AR.find("byHisDtFimIsNull")
				.fetch();

		for (GcInformacao inf : infs) {
			for (GcTag tag : inf.tags) {
				arvore.add(tag, inf);
			}
		}

		arvore.build();

		result.include("arvore", arvore);
	}

	public void buscar(String texto, String classificacao) {
		GcArvore arvore = new GcArvore();
		// List<GcInformacao> infs = GcInformacao.all().fetch();
		// n�o exibe conhecimentos cancelados
		List<GcInformacao> infs = GcInformacao.AR.find("byHisDtFimIsNull")
				.fetch();

		if (texto != null && texto.trim().length() > 0) {
			texto = texto.trim().toLowerCase();
			texto = texto.replace("  ", " ");
			String[] palavras = texto.split(" ");

			List<GcInformacao> infsFiltrada = new ArrayList<GcInformacao>();

			for (GcInformacao inf : infs) {
				if (inf.fts(palavras))
					infsFiltrada.add(inf);
			}

			infs = infsFiltrada;
		}

		for (GcInformacao inf : infs) {
			if (!inf.getTags().isEmpty()) {
				for (GcTag tag : inf.tags) {
					arvore.add(tag, inf);
				}
			} else {
				GcTag EmptyTag = new GcTag(null, "",
						"Conhecimentos_Sem_Classificacao");
				arvore.add(EmptyTag, inf);
			}
		}

		if (classificacao == null || classificacao.isEmpty()
				|| classificacao == "") {
			arvore.build();
		} else {
			arvore.build(classificacao);
		}
		// render(arvore);
		// render(arvore, texto);
		result.include("arvore", arvore);
		result.include("texto", texto);
		result.include("classificacao", classificacao);
	}

	/*
	 * public void buscar(String texto) { GcArvore arvore = new GcArvore();
	 * //List<GcInformacao> infs = GcInformacao.all().fetch(); //n�o exibe
	 * conhecimentos cancelados List<GcInformacao> infs =
	 * GcInformacao.find("byHisDtFimIsNull").fetch();
	 * 
	 * if (texto != null && texto.trim().length() > 0) { texto =
	 * texto.trim().toLowerCase(); texto = texto.replace("  ", " "); String[]
	 * palavras = texto.split(" ");
	 * 
	 * List<GcInformacao> infsFiltrada = new ArrayList<GcInformacao>();
	 * 
	 * for (GcInformacao inf : infs) { if (inf.fts(palavras))
	 * infsFiltrada.add(inf); }
	 * 
	 * infs = infsFiltrada; }
	 * 
	 * for (GcInformacao inf : infs) { if(!inf.getTags().isEmpty()){ for (GcTag
	 * tag : inf.tags) { arvore.add(tag, inf); } } else{ GcTag EmptyTag = new
	 * GcTag(null, "", "Conhecimentos_Sem_Classificacao"); arvore.add(EmptyTag,
	 * inf); } }
	 * 
	 * arvore.build();
	 * 
	 * render(arvore, texto); }
	 */

	// public void listar() {
	// List<GcInformacao> informacoes = GcInformacao.all().fetch();
	// render(informacoes);
	// }

	public void exibir(String sigla, String mensagem) throws Exception {
		GcInformacao informacao = GcInformacao.findBySigla(sigla);
		DpPessoa titular = getTitular();
		DpLotacao lotaTitular = getLotaTitular();
		CpIdentidade idc = getIdentidadeCadastrante();
		GcMovimentacao movNotificacao = informacao.podeTomarCiencia(titular,
				lotaTitular);

		if (informacao.acessoPermitido(titular, lotaTitular,
				informacao.visualizacao.id)
				|| informacao.podeRevisar(titular, lotaTitular)
				|| movNotificacao != null) {
			String conteudo = bl.marcarLinkNoConteudo(informacao.arq
					.getConteudoTXT());
			if (conteudo != null)
				informacao.arq.setConteudoTXT(conteudo);
			if (movNotificacao != null)
				bl.notificado(informacao, idc, titular, lotaTitular,
						movNotificacao);
			bl.logarVisita(informacao, idc, titular, lotaTitular);
			result.include("mensagem", mensagem);
			result.include("informacao", informacao);
		} else
			throw new AplicacaoException(
					"Restri��o de Acesso ("
							+ informacao.visualizacao.nome
							+ ") : O usu�rio n�o tem permiss�o para visualizar o conhecimento solicitado.");
	}

	public void editar(String sigla, String classificacao, String inftitulo,
			String origem, String conteudo, GcTipoInformacao tipo)
			throws Exception {
		GcInformacao informacao = null;
		DpPessoa titular = getTitular();
		DpLotacao lotaTitular = getLotaTitular();

		// Edson: esta estranho referenciar o TMPGC-0. Ver solucao melhor.
		if (sigla != null && !sigla.equals("TMPGC-0"))
			informacao = GcInformacao.findBySigla(sigla);
		else
			informacao = new GcInformacao();

		if (informacao.autor == null
				|| informacao.podeRevisar(titular, lotaTitular)
				|| informacao.acessoPermitido(titular, lotaTitular,
						informacao.edicao.id)) {
			List<GcTipoInformacao> tiposInformacao = GcTipoInformacao.AR.all()
					.fetch();
			List<GcAcesso> acessos = GcAcesso.AR.all().fetch();
			if (inftitulo == null)
				inftitulo = (informacao.arq != null) ? informacao.arq.titulo
						: null;

			if (conteudo == null)
				conteudo = (informacao.arq != null) ? informacao.arq
						.getConteudoTXT() : null;

			if (tipo == null || tipo.id == 0)
				tipo = (informacao.tipo != null) ? informacao.tipo
						: tiposInformacao.get(0);

			if (informacao.arq == null)
				conteudo = (tipo.arq != null) ? tipo.arq.getConteudoTXT()
						: null;

			if (conteudo != null && !conteudo.trim().startsWith("<")) {
				conteudo = bl.escapeHashTag(conteudo);
				if (informacao.arq != null) {
					informacao.arq.setConteudoTXT(conteudo);
					conteudo = informacao.getConteudoHTML();
				}

			}

			if (classificacao == null || classificacao.isEmpty())
				classificacao = (informacao.arq != null) ? informacao.arq.classificacao
						: null;
			// inserir hashTag no conteudo quando um conhecimento for
			// relacionado
			else if (classificacao.contains("#") && conteudo == null) {
				conteudo = "";
				String[] listaClassificacao = classificacao.split(",");
				for (String somenteHashTag : listaClassificacao) {
					if (somenteHashTag.trim().startsWith("#"))
						conteudo += somenteHashTag.trim() + " ";
				}
			}
			if (informacao.autor == null) {
				informacao.autor = titular;
			}
			if (informacao.lotacao == null) {
				informacao.lotacao = lotaTitular;
			}

			result.include("informacao", informacao);
			result.include("tiposInformacao", tiposInformacao);
			result.include("acessos", acessos);
			result.include("titulo", inftitulo);
			result.include("conteudo", conteudo);
			result.include("classificacao", classificacao);
			result.include("origem", origem);
			result.include("tipo", tipo);
		} else
			throw new AplicacaoException(
					"Restri��o de Acesso ("
							+ informacao.edicao.nome
							+ ") : O usu�rio n�o tem permiss�o para editar o conhecimento solicitado.");
	}

	public void historico(String sigla) throws Exception {
		GcInformacao informacao = GcInformacao.findBySigla(sigla);

		if (informacao.podeRevisar(getTitular(), getLotaTitular())
				|| informacao.acessoPermitido(getTitular(), getLotaTitular(),
						informacao.visualizacao.id)) {

			diff_match_patch diff = new diff_match_patch();

			String txtAnterior = "";
			String tituloAnterior = "";

			SortedSet<GcMovimentacao> list = new TreeSet<GcMovimentacao>();
			HashMap<GcMovimentacao, String> mapTitulo = new HashMap<GcMovimentacao, String>();
			HashMap<GcMovimentacao, String> mapTxt = new HashMap<GcMovimentacao, String>();
			if (informacao.movs != null) {
				GcMovimentacao[] array = informacao.movs
						.toArray(new GcMovimentacao[informacao.movs.size()]);
				ArrayUtils.reverse(array);
				for (GcMovimentacao mov : array) {
					Long t = mov.tipo.id;

					if (mov.isCancelada())
						continue;

					if (t == GcTipoMovimentacao.TIPO_MOVIMENTACAO_EDICAO
							|| t == GcTipoMovimentacao.TIPO_MOVIMENTACAO_CRIACAO) {
						// Titulo
						String titulo = mov.arq.titulo;
						LinkedList<Diff> tituloDiffs = diff.diff_main(
								tituloAnterior, titulo, true);
						String tituloDiffHtml = diff
								.diff_prettyHtml(tituloDiffs);
						boolean tituloAlterado = tituloDiffs == null
								|| tituloDiffs.size() != 1
								|| tituloDiffs.size() == 1
								&& tituloDiffs.get(0).operation != Operation.EQUAL;

						// Texto
						String txt = mov.arq.getConteudoTXT();
						LinkedList<Diff> txtDiffs = diff.diff_main(txtAnterior,
								txt, true);
						String txtDiffHtml = diff.diff_prettyHtml(txtDiffs);
						boolean txtAlterado = txtDiffs == null
								|| txtDiffs.size() != 1 || txtDiffs.size() == 1
								&& txtDiffs.get(0).operation != Operation.EQUAL;

						if (tituloAlterado || txtAlterado) {
							list.add(mov);
							if (tituloAlterado)
								mapTitulo.put(mov, tituloDiffHtml);
							if (txtAlterado)
								mapTxt.put(mov, txtDiffHtml);
						}
						txtAnterior = txt;
						tituloAnterior = titulo;
					}
				}
			}

			String conteudo = bl.marcarLinkNoConteudo(informacao.arq
					.getConteudoTXT());
			if (conteudo != null)
				informacao.arq.setConteudoTXT(conteudo);

			result.include("informacao", informacao);
			result.include("list", list);
			result.include("mapTitulo", mapTitulo);
			result.include("mapTxt", mapTxt);
		} else
			throw new AplicacaoException(
					"Restriç�o de Acesso ("
							+ informacao.visualizacao.nome
							+ ") : O usu�rio n�o tem permiss�o para visualizar o conhecimento solicitado.");

	}

	public void movimentacoes(String sigla) throws Exception {
		GcInformacao informacao = GcInformacao.findBySigla(sigla);
		if (informacao.podeRevisar(getTitular(), getLotaTitular())
				|| informacao.acessoPermitido(getTitular(), getLotaTitular(),
						informacao.visualizacao.id)) {
			String conteudo = bl.marcarLinkNoConteudo(informacao.arq
					.getConteudoTXT());
			if (conteudo != null)
				informacao.arq.setConteudoTXT(conteudo);
			result.include("informacao", informacao);
		} else
			throw new AplicacaoException(
					"Restriç�o de Acesso ("
							+ informacao.visualizacao.nome
							+ ") : O usu�rio n�o tem permiss�o para visualizar o conhecimento solicitado.");
	}

	public void fechar(String sigla) throws Exception {
		GcInformacao inf = GcInformacao.findBySigla(sigla);
		if (inf.acessoPermitido(getTitular(), getLotaTitular(), inf.edicao.id)) {
			bl.movimentar(inf, GcTipoMovimentacao.TIPO_MOVIMENTACAO_FECHAMENTO,
					null, null, null, null, null, null, null, null, null);
			bl.gravar(inf, getIdentidadeCadastrante(), getTitular(),
					getLotaTitular());
			result.redirectTo(this).exibir(inf.getSigla(), null);
		} else
			throw new AplicacaoException(
					"Restri��o de Acesso ("
							+ inf.edicao.nome
							+ ") : O usu�rio n�o tem permiss�o para finalizar o conhecimento solicitado.");
	}

	public void duplicar(String sigla) throws Exception {
		GcInformacao infDuplicada = GcInformacao.findBySigla(sigla);

		GcMovimentacao movLocalizada = bl.movimentar(infDuplicada,
				GcTipoMovimentacao.TIPO_MOVIMENTACAO_DUPLICAR, null, null,
				null, null, null, null, null, null, null);
		bl.gravar(infDuplicada, getIdentidadeCadastrante(), getTitular(),
				getLotaTitular());

		GcInformacao inf = new GcInformacao();
		GcArquivo arq = new GcArquivo();

		arq = infDuplicada.arq.duplicarConteudoInfo();
		inf.autor = getTitular();
		inf.lotacao = getLotaTitular();
		inf.ou = inf.autor.getOrgaoUsuario();
		inf.tipo = GcTipoInformacao.AR.findById(infDuplicada.tipo.id);
		inf.visualizacao = GcAcesso.AR.findById(infDuplicada.visualizacao.id);
		inf.edicao = GcAcesso.AR.findById(infDuplicada.edicao.id);

		GcMovimentacao movCriada = bl.movimentar(inf,
				GcTipoMovimentacao.TIPO_MOVIMENTACAO_CRIACAO, null, null, null,
				arq.titulo, arq.getConteudoTXT(), arq.classificacao,
				movLocalizada, null, null);
		bl.gravar(inf, getIdentidadeCadastrante(), getTitular(),
				getLotaTitular());

		movLocalizada.movRef = movCriada;
		bl.gravar(infDuplicada, getIdentidadeCadastrante(), getTitular(),
				getLotaTitular());

		if (infDuplicada.isContemArquivos()) {
			for (GcMovimentacao movDuplicado : infDuplicada.movs) {
				if (movDuplicado.isCancelada())
					continue;
				if (movDuplicado.tipo.id == GcTipoMovimentacao.TIPO_MOVIMENTACAO_ANEXAR_ARQUIVO
						&& movDuplicado.movCanceladora == null) {
					GcMovimentacao m = bl
							.movimentar(
									inf,
									movDuplicado.arq,
									GcTipoMovimentacao.TIPO_MOVIMENTACAO_ANEXAR_ARQUIVO);
					m.movRef = movLocalizada;
					bl.gravar(inf, getIdentidadeCadastrante(), getTitular(),
							getLotaTitular());
				}
			}
		}
		result.redirectTo(this).exibir(inf.getSigla(), null);
	}

	public void gravar(GcInformacao informacao, String titulo, String conteudo,
			String classificacao, String origem, GcTipoInformacao tipo)
			throws Exception {
		// DpPessoa pessoa = (DpPessoa) renderArgs.get("cadastrante");
		DpPessoa pessoa = getTitular();
		DpLotacao lotacao = getLotaTitular();

		if (informacao.autor == null) {
			informacao.autor = pessoa;
			informacao.lotacao = lotacao;
		}
		if (informacao.ou == null) {
			if (informacao.autor != null)
				informacao.ou = informacao.autor.getOrgaoUsuario();
			else if (informacao.lotacao != null)
				informacao.ou = informacao.lotacao.getOrgaoUsuario();
			else if (pessoa != null)
				informacao.ou = pessoa.getOrgaoUsuario();
		}

		informacao.tipo = tipo;

		// Atualiza a classifica��o com as hashTags encontradas
		classificacao = bl.findHashTag(conteudo, classificacao,
				CONTROLE_HASH_TAG);

		if (informacao.id != 0)
			bl.movimentar(informacao,
					GcTipoMovimentacao.TIPO_MOVIMENTACAO_EDICAO, null, null,
					null, titulo, conteudo, classificacao, null, null, null);
		else
			bl.movimentar(informacao,
					GcTipoMovimentacao.TIPO_MOVIMENTACAO_CRIACAO, null, null,
					null, titulo, conteudo, classificacao, null, null, null);

		bl.gravar(informacao, getIdentidadeCadastrante(), getTitular(),
				getLotaTitular());
		if (origem != null && origem.trim().length() != 0) {
			if (informacao.podeFinalizar(pessoa, lotacao)) {
				bl.movimentar(informacao,
						GcTipoMovimentacao.TIPO_MOVIMENTACAO_FECHAMENTO, null,
						null, null, null, null, null, null, null, null);
				bl.gravar(informacao, getIdentidadeCadastrante(), pessoa,
						lotacao);
			}
			result.redirectTo(origem);
		} else
			result.redirectTo(this).exibir(informacao.getSigla(), null);
	}

	public void remover(String sigla) throws Exception {
		GcInformacao informacao = GcInformacao.findBySigla(sigla);

		if (informacao.elaboracaoFim != null)
			throw new AplicacaoException(
					"N�o � permitido remover informa��es que j� foram finalizadas.");
		em().createQuery("delete from GcMarca where inf.id = :id")
				.setParameter("id", informacao.id).executeUpdate();
		em().createQuery("delete from GcMovimentacao where inf.id = :id")
				.setParameter("id", informacao.id).executeUpdate();
		informacao.delete();
		index();
	}

	public void notificar(String sigla) throws Exception {
		GcInformacao informacao = GcInformacao.findBySigla(sigla);
		result.include("informacao", informacao);
	}

	public void notificarGravar(GcInformacao informacao, Long pessoa,
			Long lotacao, String email) throws Exception {
		if (pessoa != null || lotacao != null || email != null) {
			DpPessoa pesResponsavel = (DpPessoa) ((pessoa != null) ? DpPessoa.AR
					.findById(pessoa) : null);
			DpLotacao lotResponsavel = (DpLotacao) ((lotacao != null) ? DpLotacao.AR
					.findById(lotacao) : null);
			bl.movimentar(informacao,
					GcTipoMovimentacao.TIPO_MOVIMENTACAO_NOTIFICAR,
					pesResponsavel, lotResponsavel, email, null, null, null,
					null, null, null);
			bl.gravar(informacao, getIdentidadeCadastrante(), getTitular(),
					getLotaTitular());
			correio.notificar(informacao, pesResponsavel, lotResponsavel, email);
			result.redirectTo(this).exibir(informacao.getSigla(),
					"Notificacao realizada com sucesso!");
		} else
			throw new AplicacaoException(
					"Para notificar so e necessario selecionar uma Pessoa ou Lotacao.");
	}

	public void solicitarRevisao(String sigla) throws Exception {
		GcInformacao informacao = GcInformacao.findBySigla(sigla);
		result.include("informacao", informacao);
	}

	public void solicitarRevisaoGravar(GcInformacao informacao, Long pessoa,
			Long lotacao) throws Exception {
		if (pessoa != null || lotacao != null) {
			DpPessoa pesResponsavel = (DpPessoa) ((pessoa != null) ? DpPessoa.AR
					.findById(pessoa) : null);
			DpLotacao lotResponsavel = (DpLotacao) ((lotacao != null) ? DpLotacao.AR
					.findById(lotacao) : null);
			bl.movimentar(informacao,
					GcTipoMovimentacao.TIPO_MOVIMENTACAO_PEDIDO_DE_REVISAO,
					pesResponsavel, lotResponsavel, null, null, null, null,
					null, null, null);
			bl.gravar(informacao, getIdentidadeCadastrante(), getTitular(),
					getLotaTitular());
			result.redirectTo(this).result.redirectTo(this).exibir(
					informacao.getSigla(),
					"Solicita��o de revis�o realizada com sucesso!");
		} else
			throw new AplicacaoException(
					"Para solicitar revis�o � necess�rio selecionar uma Pessoa ou Lota��o.");
	}

	public void anexar(String sigla) throws Exception {
		GcInformacao informacao = GcInformacao.findBySigla(sigla);
		result.include("informacao", informacao);
	}

	public void anexarGravar(GcInformacao informacao, String titulo,
			UploadedFile file) throws Exception {
		DpPessoa titular = getTitular();
		DpLotacao lotaTitular = getLotaTitular();
		CpIdentidade idc = getIdentidadeCadastrante();
		if (file != null)
			if (file.getSize() > 2097152)
				throw new AplicacaoException(
						"O tamanho do arquivo � maior que o "
								+ "m�ximo permitido (2MB)");
		if (file.getSize() > 0) {
			/*
			 * ----N�o pode ser usado porque o "plupload" retorna um mime type
			 * padr�o "octet stream" String mimeType =
			 * file.getContentType().toLowerCase();
			 */
			byte anexo[] = IOUtils.toByteArray(file.getFile());
			if (titulo == null || titulo.trim().length() == 0)
				titulo = file.getFileName();
			bl.movimentar(informacao,
					GcTipoMovimentacao.TIPO_MOVIMENTACAO_ANEXAR_ARQUIVO, null,
					null, null, titulo, null, null, null, null, anexo);
			bl.gravar(informacao, idc, titular, lotaTitular);
			result.use(Results.http()).body("success");
		} else
			throw new AplicacaoException(
					"Nao e permitido anexar se nenhum arquivo estiver selecionado. Favor selecionar arquivo.");
	}

	public void removerAnexo(String sigla, long idArq, long idMov)
			throws Exception {
		GcInformacao informacao = GcInformacao.findBySigla(sigla);
		GcMovimentacao mov = GcMovimentacao.AR.findById(idMov);

		if (mov.arq.id == idArq)
			bl.cancelarMovimentacao(informacao, mov,
					getIdentidadeCadastrante(), getTitular(), getLotaTitular());
		/*
		 * for (GcMovimentacao mov : informacao.movs) { if (mov.isCancelada())
		 * continue; if (mov.tipo.id ==
		 * GcTipoMovimentacao.TIPO_MOVIMENTACAO_ANEXAR_ARQUIVO && mov.arq.id ==
		 * id) { movLocalizada = mov; break; } } GcMovimentacao m =
		 * bl.movimentar(informacao,
		 * GcTipoMovimentacao.TIPO_MOVIMENTACAO_CANCELAMENTO_DE_MOVIMENTACAO,
		 * null, null, null, null, null, movLocalizada, null, null, null);
		 * movLocalizada.movCanceladora = m; bl.gravar(informacao,
		 * getIdentidadeCadastrante(), titular(), lotaTitular());
		 */
		result.include("informacao", informacao);
	}

	public Download baixar(Long id) throws Exception {
		GcArquivo arq = GcArquivo.AR.findById(id);
		if (arq != null)
			return new ByteArrayDownload(arq.conteudo, arq.mimeType,
					arq.titulo, true);
		throw new Exception("Arquivo n�o encontrado.");
	}

	public void revisado(String sigla) throws Exception {
		GcInformacao informacao = GcInformacao.findBySigla(sigla);
		if (informacao.movs != null) {
			DpPessoa titular = getTitular();
			DpLotacao lotaTitular = getLotaTitular();
			for (GcMovimentacao mov : informacao.movs) {
				if (mov.isCancelada())
					continue;
				if (mov.tipo.id == GcTipoMovimentacao.TIPO_MOVIMENTACAO_PEDIDO_DE_REVISAO
						&& (titular.equivale(mov.pessoaAtendente) || lotaTitular
								.equivale(mov.lotacaoAtendente))) {
					GcMovimentacao m = bl
							.movimentar(
									informacao,
									GcTipoMovimentacao.TIPO_MOVIMENTACAO_REVISADO,
									null, null, null, null, null, null, mov,
									null, null);
					mov.movCanceladora = m;
					bl.gravar(informacao, getIdentidadeCadastrante(), titular,
							lotaTitular);
					if (informacao.acessoPermitido(titular, lotaTitular,
							informacao.visualizacao.id))
						result.redirectTo(this).exibir(sigla,
								"Conhecimento revisado com sucesso!");
					else {
						// buscar(null);
						buscar(null, null);
					}

				}
			}
		}
		throw new AplicacaoException("N�o h� pedido de revis�o pendente para "
				+ getIdentidadeCadastrante().getDpPessoa().getSigla());
	}

	public void marcarComoInteressado(String sigla) throws Exception {
		GcInformacao informacao = GcInformacao.findBySigla(sigla);
		bl.interessado(informacao, getIdentidadeCadastrante(), getTitular(),
				getLotaTitular(), true);
		result.redirectTo(this).exibir(sigla, null);
	}

	public void desmarcarComoInteressado(String sigla) throws Exception {
		GcInformacao informacao = GcInformacao.findBySigla(sigla);
		bl.interessado(informacao, getIdentidadeCadastrante(), getTitular(),
				getLotaTitular(), false);
		result.redirectTo(this).exibir(sigla, null);
	}

	public void cancelar(String sigla) throws Exception {
		GcInformacao informacao = GcInformacao.findBySigla(sigla);
		bl.cancelar(informacao, getIdentidadeCadastrante(), getTitular(),
				getLotaTitular());
		result.redirectTo(this).exibir(sigla, null);
	}

	public void selecionarTag(String sigla) throws Exception {
		GcTag sel = (GcTag) new GcTag().selecionar(sigla);
		result.include("sel", sel);
		// render("@siga-play-module.selecionar", sel);
	}

	public void buscarTag(String sigla, GcTag filtro) {
		List<GcTag> itens = null;

		Query query = em().createNamedQuery("listarTagCategorias");
		List<Object[]> listaTagCategorias = query.getResultList();
		if (listaTagCategorias.size() == 0)
			listaTagCategorias = null;

		try {
			if (filtro == null)
				filtro = new GcTag();
			if (sigla != null && !sigla.trim().equals(""))
				filtro.setSigla(sigla);
			itens = (List<GcTag>) filtro.buscar();
		} catch (Exception e) {
			itens = new ArrayList<GcTag>();
		}

		result.include("itens", itens);
		result.include("filtro", filtro);
		result.include("listaTagCategorias", listaTagCategorias);
	}

	// @Path("/public/app/dadosRI")
	public void dadosRI(Long ultimaAtualizacao, Long desempate)
			throws UnsupportedEncodingException {
		Date dtUltimaAtualizacao = new Date(0L);
		Long idDesempate = 0L;
		if (ultimaAtualizacao != null)
			dtUltimaAtualizacao = new Date(ultimaAtualizacao);
		if (desempate != null)
			idDesempate = desempate;

		Query query = em().createNamedQuery("dadosParaRecuperacaoDeInformacao");
		query.setParameter("dt", dtUltimaAtualizacao, TemporalType.TIMESTAMP);
		query.setParameter("desempate", idDesempate);
		query.setMaxResults(10);
		List<Object[]> lista = query.getResultList();
		if (lista.size() == 0) {
			result.use(Results.http()).body("[]");
			return;
		}

		List<DadosRI> resultado = new ArrayList<DadosRI>();
		for (Object[] ao : lista) {
			GcInformacao i = (GcInformacao) ao[0];
			GcArquivo a = (GcArquivo) ao[1];
			Date dt = (Date) ao[2];
			long idMov = (Long) ao[3];
			boolean ativo = !((Long) ao[4]).equals(3L);

			DadosRI dri = new DadosRI();
			dri.uri = "/sigagc/app/exibir?sigla=" + i.getSigla();
			dri.sigla = i.getSigla();
			dri.titulo = a.titulo;
			dri.ultimaAtualizacao = dt;
			dri.idDesempate = idMov;
			dri.ativo = ativo;
			if (ativo) {
				dri.conteudo = new String(a.conteudo, "utf-8");
			}
			resultado.add(dri);
		}
		Gson gson = new GsonBuilder()
				.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z").create();
		result.use(Results.http()).body(gson.toJson(resultado));
	}

	public void desfazer(String sigla, long movId) throws Exception {
		GcInformacao info = GcInformacao.findBySigla(sigla);
		GcMovimentacao mov = GcMovimentacao.AR.findById(movId);

		bl.cancelarMovimentacao(info, mov, getIdentidadeCadastrante(),
				getTitular(), getLotaTitular());
		movimentacoes(sigla);
	}

}