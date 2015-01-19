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
/*
 * Criado em  13/09/2005
 *
 */
package br.gov.jfrj.siga.vraptor;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.SessionStatistics;
import org.hibernate.stat.Statistics;

import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.cp.CpTipoConfiguracao;
import br.gov.jfrj.siga.dp.CpOrgao;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.dp.dao.CpDao;
import br.gov.jfrj.siga.ex.ExClassificacao;
import br.gov.jfrj.siga.ex.ExDocumento;
import br.gov.jfrj.siga.ex.ExFormaDocumento;
import br.gov.jfrj.siga.ex.ExMarca;
import br.gov.jfrj.siga.ex.ExMobil;
import br.gov.jfrj.siga.ex.ExModelo;
import br.gov.jfrj.siga.ex.ExMovimentacao;
import br.gov.jfrj.siga.ex.ExNivelAcesso;
import br.gov.jfrj.siga.ex.ExPreenchimento;
import br.gov.jfrj.siga.ex.ExSituacaoConfiguracao;
import br.gov.jfrj.siga.ex.ExTipoDocumento;
import br.gov.jfrj.siga.ex.ExTipoMobil;
import br.gov.jfrj.siga.ex.ExTipoMovimentacao;
import br.gov.jfrj.siga.ex.bl.Ex;
import br.gov.jfrj.siga.ex.bl.ExBL;
import br.gov.jfrj.siga.ex.bl.BIE.HierarquizadorBoletimInterno;
import br.gov.jfrj.siga.ex.util.FuncoesEL;
import br.gov.jfrj.siga.ex.util.GeradorRTF;
import br.gov.jfrj.siga.ex.util.PublicacaoDJEBL;
import br.gov.jfrj.siga.ex.vo.ExDocumentoVO;
import br.gov.jfrj.siga.hibernate.ExDao;
import br.gov.jfrj.siga.libs.webwork.Selecao;
import br.gov.jfrj.siga.persistencia.ExMobilDaoFiltro;

import com.opensymphony.xwork.Action;

@Resource
public class ExDocumentoController extends ExController {
	
	private static final String URL_EXIBIR = "/app/expediente/doc/exibir?sigla={0}";

	public ExDocumentoController(HttpServletRequest request, Result result, SigaObjects so) {
		super(request, result, CpDao.getInstance(), so);
		
		result.on(AplicacaoException.class).forwardTo(this).appexception();
		result.on(Exception.class).forwardTo(this).exception();		
	}
	
	public String aAlterarPreenchimento(ExDocumentoDTO exDocumentoDTO) throws Exception {
		
		ExPreenchimento exPreenchimento = new ExPreenchimento();
		
		dao().iniciarTransacao();
		exPreenchimento.setIdPreenchimento(exDocumentoDTO.getPreenchimento());
		exPreenchimento = dao().consultar(exDocumentoDTO.getPreenchimento(), ExPreenchimento.class, false);
		
		exPreenchimento.setPreenchimentoBA(getByteArrayFormPreenchimento());
		dao().gravar(exPreenchimento);
		dao().commitTransacao();
		
		exDocumentoDTO.setPreenchimento(exPreenchimento.getIdPreenchimento());
		
		String url = getUrlEncodedParameters();
		if (url.indexOf("preenchimento") >= 0) {
			String parte1 = url.substring(0, url.indexOf("preenchimento"));
			String parte2 = url.substring(url.indexOf("&", url.indexOf("&preenchimento") + 1) + 1);
			parte2 = parte2 + "&preenchimento=" + exDocumentoDTO.getPreenchimento();
			exDocumentoDTO.setPreenchRedirect(parte1 + parte2);
		} else
			exDocumentoDTO.setPreenchRedirect(getUrlEncodedParameters());
		
		return edita(exDocumentoDTO).toString();
	}
	
	public String aAnexo(ExDocumentoDTO exDocumentoDTO) throws Exception {
		buscarDocumento(true, exDocumentoDTO);
		return Action.SUCCESS;
	}
	
	public String aCancelarDocumento(ExDocumentoDTO exDocumentoDTO) throws Exception {
		buscarDocumento(true, exDocumentoDTO);
		
		try {
			Ex.getInstance().getBL().cancelarDocumento(getCadastrante(), getLotaTitular(), exDocumentoDTO.getDoc());
		} catch (final Exception e) {
			throw e;
		}
		return Action.SUCCESS;
		
	}
	
	// Converte encode de url de iso-8859-1 para utf-8
	private String ConverteEncodeDeUriDeIsoParaUtf(String sUri) {
		// return sUri;
		StringBuilder sb = new StringBuilder();
		String aParametros[] = sUri.split("&");
		for (int i = 0; i < aParametros.length; i++) {
			String aTupla[] = aParametros[i].split("=");
			try {
				sb.append(URLEncoder.encode(URLDecoder.decode(aTupla[0], "iso-8859-1"), "utf-8"));
				sb.append("=");
				sb.append(URLEncoder.encode(URLDecoder.decode(aTupla[1], "iso-8859-1"), "utf-8"));
			} catch (UnsupportedEncodingException e) {
			}
			if (i < aParametros.length - 1)
				sb.append("&");
		}
		return sb.toString();
	}
	
	public boolean validar() {
		if (getPar().get("obrigatorios") != null)
			for (String valor : getPar().get("obrigatorios"))
				if (getPar().get(valor) == null || getPar().get(valor)[0].trim().equals("")
						|| getPar().get(valor)[0].trim().equals("N�o") || getPar().get(valor)[0].trim().equals("Nao"))
					return false;
		return true;
	}
	
	public String aCarregarPreenchimento(ExDocumentoDTO exDocumentoDTO) throws Exception {
		ExPreenchimento exPreenchimento = new ExPreenchimento();
		
		// Obt�m arrStrBanco[], com os par�metros vindos do banco
		exPreenchimento = dao().consultar(exDocumentoDTO.getPreenchimento(), ExPreenchimento.class, false);
		String strBanco = new String(exPreenchimento.getPreenchimentoBA());
		String arrStrBanco[] = strBanco.split("&");
		String strBancoLimpa = new String();
		
		// seta os atributos da action com base nos valores do banco, fazendo o
		// decoding da string
		for (String elem : arrStrBanco) {
			String[] paramNameAndValue = ((String) elem).split("=");
			String paramName = paramNameAndValue[0];
			String paramValue = paramNameAndValue[1];
			String paramValueDecoded = URLDecoder.decode(paramValue, "ISO-8859-1");
			String paramValueEncodedUTF8 = URLEncoder.encode(paramValueDecoded, "UTF-8");
			try {
				if (!paramName.contains("Sel.id")) {
					final String mName = "set" + paramName.substring(0, 1).toUpperCase() + paramName.substring(1);
					if (getPar().get(paramName) != null || (paramName.contains("nmOrgaoExterno"))
							|| (paramName.contains("nmDestinatario"))) {
						Class paramType = this.getClass().getDeclaredField(paramName).getType();
						Constructor paramTypeContructor = paramType.getConstructor(new Class[] { String.class });
						final Method method = this.getClass().getMethod(mName, new Class[] { paramType });
						method.invoke(this,
								new Object[] { paramTypeContructor.newInstance(new Object[] { (paramValueDecoded) }) });
					}
				} else {
					final String mName = "get" + paramName.substring(0, 1).toUpperCase()
							+ paramName.substring(1, paramName.indexOf(".id"));
					if (getPar().get(paramName) != null || (paramName.contains("estinatarioSel.id"))) {
						final Method method = this.getClass().getMethod(mName);
						Selecao sel = (Selecao) method.invoke(this);
						sel.setId(Long.parseLong(paramValue));
						sel.buscarPorId();
					}
				}
				
			} catch (NoSuchMethodException nSME) {
			} catch (NoSuchFieldException nSFE) {
			} catch (NumberFormatException nfe) {
				paramValue = "";
			} catch (InvocationTargetException nfe) {
				paramValue = "";
			} finally {
				strBancoLimpa += "&" + paramName + "=" + paramValueEncodedUTF8;
			}
		}
		
		// Obt�m arrStrURL[], com os par�metros atuais da edita.jsp
		String strURL = getUrlEncodedParameters();
		String arrStrURL[] = strURL.split("&");
		String strURLLimpa = "";
		
		// limpa a url vinda do browser, tirando o que j� consta na string do
		// banco, tirando tamb�m os .sigla e .descricao
		if (arrStrURL.length > 0) {
			for (String s : arrStrURL) {
				String arrStrURL2[] = s.split("=");
				if (arrStrURL2.length > 1 && !arrStrURL2[0].contains(".sigla") && !arrStrURL2[0].contains(".descricao")
						&& !strBanco.contains(arrStrURL2[0] + "="))
					strURLLimpa = strURLLimpa + s + "&";
			}
		}
		
		exDocumentoDTO.setPreenchRedirect(strURLLimpa + strBancoLimpa);
		
		return edita(exDocumentoDTO).toString();
	}
	
	@Get("app/expediente/doc/criar_via")
	public void criarVia(String sigla) throws Exception {
		ExDocumentoDTO exDocumentoDTO = new ExDocumentoDTO(sigla);
		buscarDocumento(true, exDocumentoDTO);
		
		if (!Ex.getInstance().getComp().podeCriarVia(getTitular(), getLotaTitular(), exDocumentoDTO.getMob()))
			throw new AplicacaoException("N�o � poss�vel criar vias neste documento");
		try {
			Ex.getInstance().getBL().criarVia(getCadastrante(), getLotaTitular(), exDocumentoDTO.getDoc());
		} catch (final Exception e) {
			throw e;
		}
		ExDocumentoController.redirecionarParaExibir(result, sigla);
	}
	
	@Get("app/expediente/doc/criar_volume")
	public void criarVolume(String sigla) throws Exception {
		ExDocumentoDTO exDocumentoDTO = new ExDocumentoDTO(sigla);
		buscarDocumento(true, exDocumentoDTO);
		
		if (!Ex.getInstance().getComp().podeCriarVolume(getTitular(), getLotaTitular(), exDocumentoDTO.getMob()))
			throw new AplicacaoException("N�o � poss�vel criar volumes neste documento");
		
		try {
			Ex.getInstance().getBL().criarVolume(getCadastrante(), getLotaTitular(), exDocumentoDTO.getDoc());
		} catch (final Exception e) {
			throw e;
		}
		ExDocumentoController.redirecionarParaExibir(result, exDocumentoDTO.getSigla());
	}
	
	@Get("app/expediente/doc/editar")
	public ExDocumentoDTO edita(ExDocumentoDTO exDocumentoDTO) throws Exception {
		
		if (exDocumentoDTO == null) {
			exDocumentoDTO = new ExDocumentoDTO();
		}
		
		buscarDocumentoOuNovo(true, exDocumentoDTO);
		
		if ((getPostback() == null) || (param("docFilho") != null)) {
			exDocumentoDTO.setTipoDestinatario(2);
			exDocumentoDTO.setIdFormaDoc(2);
			exDocumentoDTO.setIdTpDoc(1L);
			
			ExNivelAcesso nivelDefault = getNivelAcessoDefault(exDocumentoDTO);
			if (nivelDefault != null) {
				exDocumentoDTO.setNivelAcesso(nivelDefault.getIdNivelAcesso());
			} else
				exDocumentoDTO.setNivelAcesso(1L);
			
			exDocumentoDTO.setIdMod(((ExModelo) dao().consultarAtivoPorIdInicial(ExModelo.class, 26L)).getIdMod());
		}
		
		if (exDocumentoDTO.isCriandoAnexo() && exDocumentoDTO.getId() == null && getPostback() == null) {
			exDocumentoDTO.setIdFormaDoc(60);
			exDocumentoDTO.setIdMod(((ExModelo) dao().consultarAtivoPorIdInicial(ExModelo.class, 507L)).getIdMod());
		}
		
		if (exDocumentoDTO.getDespachando() && exDocumentoDTO.getId() == null
				&& (getPostback() == null || getPostback() == 0)) {
			
			exDocumentoDTO.setIdFormaDoc(8);
			
		}
		
		if (exDocumentoDTO.getId() == null && exDocumentoDTO.getDoc() != null)
			exDocumentoDTO.setId(exDocumentoDTO.getDoc().getIdDoc());
		
		if (exDocumentoDTO.getId() == null) {
			if (getLotaTitular().isFechada())
				throw new AplicacaoException("A lota��o " + getLotaTitular().getSiglaLotacao() + " foi extinta em "
						+ new SimpleDateFormat("dd/MM/yyyy").format(getLotaTitular().getDataFimLotacao())
						+ ". N�o � poss�vel gerar expedientes em lota��o extinta.");
			exDocumentoDTO.setDoc(new ExDocumento());
			exDocumentoDTO.getDoc().setOrgaoUsuario(getTitular().getOrgaoUsuario());
		} else {
			exDocumentoDTO.setDoc(daoDoc(exDocumentoDTO.getId()));
			
			if (!Ex.getInstance().getComp().podeEditar(getTitular(), getLotaTitular(), exDocumentoDTO.getMob()))
				throw new AplicacaoException("N�o � permitido editar documento fechado");
			
			if (getPostback() == null) {
				escreverForm(exDocumentoDTO);
				lerEntrevista(exDocumentoDTO);
			}
		}
		
		if (exDocumentoDTO.getTipoDocumento() != null && exDocumentoDTO.getTipoDocumento().equals("externo")) {
			exDocumentoDTO.setIdMod(((ExModelo) dao().consultarAtivoPorIdInicial(ExModelo.class, 28L)).getIdMod());
		}
		carregarBeans(exDocumentoDTO);
		
		Long idSit = Ex
				.getInstance()
				.getConf()
				.buscaSituacao(exDocumentoDTO.getModelo(), exDocumentoDTO.getDoc().getExTipoDocumento(), getTitular(),
						getLotaTitular(), CpTipoConfiguracao.TIPO_CONFIG_ELETRONICO).getIdSitConfiguracao();
		
		if (idSit == ExSituacaoConfiguracao.SITUACAO_OBRIGATORIO) {
			exDocumentoDTO.setEletronico(1);
			exDocumentoDTO.setEletronicoFixo(true);
		} else if (idSit == ExSituacaoConfiguracao.SITUACAO_PROIBIDO) {
			exDocumentoDTO.setEletronico(2);
			exDocumentoDTO.setEletronicoFixo(true);
		} else if (idSit == ExSituacaoConfiguracao.SITUACAO_DEFAULT
				&& (exDocumentoDTO.getEletronico() == null || exDocumentoDTO.getEletronico() == 0)) {
			exDocumentoDTO.setEletronico(1);
		} else if (exDocumentoDTO.isAlterouModelo()) {
			if (idSit == ExSituacaoConfiguracao.SITUACAO_DEFAULT) {
				exDocumentoDTO.setEletronico(1);
			} else {
				exDocumentoDTO.setEletronicoFixo(false);
				exDocumentoDTO.setEletronico(0);
			}
		}
		
		lerForm(exDocumentoDTO);
		
		// O (&& classif.getCodAssunto() != null) foi adicionado para permitir
		// que as classifica��es antigas, ainda n�o linkadas por equival�ncia,
		// possam ser usadas

		ExClassificacao classif = exDocumentoDTO.getClassificacaoSel().buscarObjeto();
		if (classif != null && classif.getHisDtFim() != null && classif.getHisDtIni() != null
				&& classif.getCodAssunto() != null) {

			classif = ExDao.getInstance().consultarAtual(classif);
			if (classif != null)
				exDocumentoDTO.getClassificacaoSel().setId(classif.getIdClassificacao());
			else
				exDocumentoDTO.getClassificacaoSel().setId(null);
		}
		
		exDocumentoDTO.getSubscritorSel().buscar();
		exDocumentoDTO.getDestinatarioSel().buscar();
		exDocumentoDTO.getLotacaoDestinatarioSel().buscar();
		exDocumentoDTO.getOrgaoSel().buscar();
		exDocumentoDTO.getOrgaoExternoDestinatarioSel().buscar();
		exDocumentoDTO.getClassificacaoSel().buscar();
		exDocumentoDTO.getMobilPaiSel().buscar();
		
		if (getRequest().getSession().getAttribute("preenchRedirect") != null) {
			exDocumentoDTO.setPreenchRedirect((String) getRequest().getSession().getAttribute("preenchRedirect"));
			getRequest().getSession().removeAttribute("preenchRedirect");
		}
		
		registraErroExtEditor();
		
		// Usado pela extens�o editor...
		getPar().put(
				"serverAndPort",
				new String[] { getRequest().getServerName()
						+ (getRequest().getServerPort() > 0 ? ":" + getRequest().getServerPort() : "") });
		
		// ...inclusive nas opera��es com preenchimento autom�tico
		if (exDocumentoDTO.getPreenchRedirect() != null && exDocumentoDTO.getPreenchRedirect().length() > 2) {
			exDocumentoDTO.setPreenchRedirect(exDocumentoDTO.getPreenchRedirect() + "&serverAndPort="
					+ getPar().get("serverAndPort")[0]);
		}
		
		exDocumentoDTO.setTiposDocumento(getTiposDocumento());
		exDocumentoDTO.setListaNivelAcesso(getListaNivelAcesso(exDocumentoDTO));
		exDocumentoDTO.setFormasDoc(getFormasDocPorTipo(exDocumentoDTO));
		exDocumentoDTO.setModelos(getModelos(exDocumentoDTO));
		getPreenchimentos(exDocumentoDTO);
		
		result.include("possuiMaisQueUmModelo", (getModelos(exDocumentoDTO).size() > 1));
		result.include("par", getPar());
		result.include("cpOrgaoSel", exDocumentoDTO.getCpOrgaoSel());
		result.include("mobilPaiSel", exDocumentoDTO.getMobilPaiSel());
		result.include("subscritorSel", exDocumentoDTO.getSubscritorSel());
		result.include("titularSel", exDocumentoDTO.getTitularSel());
		result.include("destinatarioSel", exDocumentoDTO.getDestinatarioSel());
		result.include("lotacaoDestinatarioSel", exDocumentoDTO.getLotacaoDestinatarioSel());
		result.include("orgaoExternoDestinatarioSel", exDocumentoDTO.getOrgaoExternoDestinatarioSel());
		result.include("classificacaoSel", exDocumentoDTO.getClassificacaoSel());
		return exDocumentoDTO;
	}
	
	public String aExcluir(ExDocumentoDTO exDocumentoDTO) throws Exception {
		ExDocumento documento = null;
		final String sId = getRequest().getParameter("id");
		
		try {
			ExDao.iniciarTransacao();
			documento = daoDoc(Long.valueOf(sId));
			
			verificaNivelAcesso(exDocumentoDTO.getDoc().getMobilGeral());
			
			// Testa se existe algum valor preenchido em documento.
			// Se n�o houver gera ObjectNotFoundException
			final Date d = documento.getDtRegDoc();
			
			if (documento.isFinalizado())
				
				throw new AplicacaoException("Documento j� foi finalizado e n�o pode ser exclu�do");
			if (!Ex.getInstance().getComp().podeExcluir(getTitular(), getLotaTitular(), exDocumentoDTO.getMob()))
				throw new AplicacaoException("N�o � poss�vel excluir");
			
			// Exclui documento da tabela de Boletim Interno
			String funcao = exDocumentoDTO.getDoc().getForm().get("acaoExcluir");
			if (funcao != null) {
				obterMetodoPorString(funcao, exDocumentoDTO.getDoc());
			}
			
			for (ExMovimentacao movRef : exDocumentoDTO.getMob().getExMovimentacaoReferenciaSet())
				Ex.getInstance().getBL().excluirMovimentacao(movRef);
			
			dao().excluir(documento);
			ExDao.commitTransacao();
			
		} catch (final ObjectNotFoundException e) {
			throw new AplicacaoException("Documento j� foi exclu�do anteriormente");
		} catch (final AplicacaoException e) {
			ExDao.rollbackTransacao();
			throw e;
		} catch (final Exception e) {
			ExDao.rollbackTransacao();
			throw new AplicacaoException("Ocorreu um Erro durante a Opera��o", 0, e);
		}
		
		return Action.SUCCESS;
	}
	
	private void registraErroExtEditor() {
		
		try {
			if (param("desconsiderarExtensao") != null && param("desconsiderarExtensao").equals("true")) {
				
				String nomeArquivo = getRequest().getRemoteHost();
				nomeArquivo = nomeArquivo.replaceAll(":", "_");
				
				BufferedWriter out = new BufferedWriter(new FileWriter("./siga-ex-ext-editor-erro/" + nomeArquivo));
				out.close();
			}
		} catch (IOException e) {
			int a = 0;
		}
	}
	
	private void obterMetodoPorString(String metodo, ExDocumento doc) throws Exception {
		if (metodo != null) {
			final Class[] classes = new Class[] { ExDocumento.class };
			
			Method method;
			try {
				method = Ex.getInstance().getBL().getClass().getDeclaredMethod(metodo, classes);
			} catch (NoSuchMethodException e1) {
				e1.printStackTrace();
				return;
			}
			method.invoke(Ex.getInstance().getBL(), new Object[] { doc });
		}
	}
	
	@Get("/app/expediente/doc/excluir")
	public void aExcluirDocMovimentacoes(String sigla) throws Exception {
		ExDocumentoDTO exDocumentoDTO = new ExDocumentoDTO();
		exDocumentoDTO.setSigla(sigla);
		ExDocumento doc = exDocumentoDTO.getDoc();
		buscarDocumento(true, exDocumentoDTO);
		try {
			ExDao.iniciarTransacao();
			
			try {
				final Date d = doc.getDtRegDoc();
			} catch (final ObjectNotFoundException e) {
				throw new AplicacaoException("Documento j� foi exclu�do anteriormente", 1, e);
			}
			
			if (doc.isFinalizado())
				
				throw new AplicacaoException("Documento j� foi finalizado e n�o pode ser exclu�do", 2);
			for (ExMobil m : doc.getExMobilSet()) {
				Set set = m.getExMovimentacaoSet();
				
				if (!Ex.getInstance().getComp().podeExcluir(getTitular(), getLotaTitular(), m))
					throw new AplicacaoException("N�o � poss�vel excluir");
				
				if (set.size() > 0) {
					final Object[] aMovimentacao = set.toArray();
					for (int i = 0; i < set.size(); i++) {
						final ExMovimentacao movimentacao = (ExMovimentacao) aMovimentacao[i];
						dao().excluir(movimentacao);
					}
				}
				
				for (ExMarca marc : m.getExMarcaSet())
					dao().excluir(marc);
				
				set = m.getExMovimentacaoReferenciaSet();
				if (set.size() > 0) {
					final Object[] aMovimentacao = set.toArray();
					for (int i = 0; i < set.size(); i++) {
						final ExMovimentacao movimentacao = (ExMovimentacao) aMovimentacao[i];
						Ex.getInstance()
								.getBL()
								.excluirMovimentacao(getCadastrante(), getLotaTitular(), movimentacao.getExMobil(),
										movimentacao.getIdMov());
					}
				}
				
				dao().excluir(m);
			}
			
			// Exclui documento da tabela de Boletim Interno
			String funcao = doc.getForm().get("acaoExcluir");
			if (funcao != null) {
				obterMetodoPorString(funcao, doc);
			}
			
			dao().excluir(doc);
			ExDao.commitTransacao();
			result.include("sigla", sigla);
			result.forwardTo("/app/expediente/doc/listar");
		} catch (final AplicacaoException e) {
			ExDao.rollbackTransacao();
			throw e;
		} catch (final Exception e) {
			ExDao.rollbackTransacao();
			throw new AplicacaoException("Ocorreu um Erro durante a Opera��o", 0, e);
		}
	}
	
	public String aExcluirPreenchimento(ExDocumentoDTO exDocumentoDTO) throws Exception {
		dao().iniciarTransacao();
		ExPreenchimento exemplo = dao().consultar(exDocumentoDTO.getPreenchimento(), ExPreenchimento.class, false);
		dao().excluir(exemplo);
		// preenchDao.excluirPorId(preenchimento);
		dao().commitTransacao();
		exDocumentoDTO.setPreenchimento(0L);
		return edita(exDocumentoDTO).toString();
	}
	
	public String aTestarConexao() {
		return Action.SUCCESS;
	}
	
	public String aAcessar(ExDocumentoDTO exDocumentoDTO) throws Exception {
		buscarDocumento(false, exDocumentoDTO);
		
		assertAcesso(exDocumentoDTO);
		
		return Action.SUCCESS;
	}
	
	private void assertAcesso(ExDocumentoDTO exDocumentoDTO) throws Exception {
		String msgDestinoDoc = "";
		DpPessoa dest;
		if (!Ex.getInstance().getComp().podeAcessarDocumento(getTitular(), getLotaTitular(), exDocumentoDTO.getMob())) {
			
			String s = "";
			try {
				s += exDocumentoDTO.getMob().doc().getListaDeAcessosString();
				s = "(" + s + ")";
				s = " " + exDocumentoDTO.getMob().doc().getExNivelAcesso().getNmNivelAcesso() + " " + s;
			} catch (Exception e) {
			}

			throw new AplicacaoException("Documento " + exDocumentoDTO.getMob().getSigla() + " inacess�vel ao usu�rio "
					+ getTitular().getSigla() + "/" + getLotaTitular().getSiglaCompleta() + "." + s + " "
					+ msgDestinoDoc);
		}
	}
	
	public String aExibirAntigo(ExDocumentoDTO exDocumentoDTO) throws Exception {
		buscarDocumento(false, exDocumentoDTO);
		
		assertAcesso(exDocumentoDTO);
		
		if (Ex.getInstance().getComp().podeReceberEletronico(getTitular(), getLotaTitular(), exDocumentoDTO.getMob()))
			Ex.getInstance().getBL().receber(getCadastrante(), getLotaTitular(), exDocumentoDTO.getMob(), new Date());
		
		ExDocumentoVO docVO = new ExDocumentoVO(exDocumentoDTO.getDoc(), exDocumentoDTO.getMob(), getTitular(),
				getLotaTitular(), true, true);

		super.getRequest().setAttribute("docVO", docVO);
		
		// logStatistics();
		
		if (exDocumentoDTO.getMob().isEliminado())
			throw new AplicacaoException("Documento "
					+ exDocumentoDTO.getMob().getSigla()
					+ " eliminado, conforme o termo "
					+ exDocumentoDTO.getMob().getUltimaMovimentacaoNaoCancelada(ExTipoMovimentacao.TIPO_MOVIMENTACAO_ELIMINACAO)
							.getExMobilRef());
		
		return Action.SUCCESS;
	}
	
	@Get("/app/expediente/doc/exibir")
	public void exibe(boolean conviteEletronico, String sigla) throws Exception {
		ExDocumentoDTO exDocumentoDto = new ExDocumentoDTO();
		exDocumentoDto.setSigla(sigla);
		buscarDocumento(false, exDocumentoDto);
		
		assertAcesso(exDocumentoDto);
		
		if (Ex.getInstance().getComp().podeReceberEletronico(getTitular(), getLotaTitular(), exDocumentoDto.getMob()))
			Ex.getInstance().getBL().receber(getCadastrante(), getLotaTitular(), exDocumentoDto.getMob(), new Date());
		
		if (exDocumentoDto.getMob() == null || exDocumentoDto.getMob().isGeral()) {
			if (exDocumentoDto.getMob().getDoc().isFinalizado()) {
				if (exDocumentoDto.getDoc().isProcesso())
					exDocumentoDto.setMob(exDocumentoDto.getDoc().getUltimoVolume());
				else
					exDocumentoDto.setMob(exDocumentoDto.getDoc().getPrimeiraVia());
			}
		}
		
		ExDocumentoVO docVO = new ExDocumentoVO(exDocumentoDto.getDoc(), exDocumentoDto.getMob(), getTitular(), getLotaTitular(),
				true, false);
		
		docVO.exibe();
		
		result.include("docVO", docVO);
		result.include("sigla", exDocumentoDto.getSigla().replace("/", ""));
		result.include("id", exDocumentoDto.getId());
		result.include("mob", exDocumentoDto.getMob());
		result.include("lota", this.getLotaTitular());
		result.include("param", exDocumentoDto.getParamsEntrevista());
	}
	
	@Get("/app/expediente/doc/exibeProcesso")
	public void exibeProcesso(String sigla, boolean podeExibir) throws Exception {
		exibe(false, sigla);
	}
	
	@Get("/app/expediente/doc/exibirResumoProcesso")
	public void exibeResumoProcesso(String sigla, boolean podeExibir) throws Exception {
		exibe(false, sigla);
	}
	
	public String aCorrigirPDF(ExDocumentoDTO exDocumentoDTO) throws Exception {
		if (exDocumentoDTO.getSigla() != null) {
			final ExMobilDaoFiltro filter = new ExMobilDaoFiltro();
			filter.setSigla(exDocumentoDTO.getSigla());
			exDocumentoDTO.setMob((ExMobil) dao().consultarPorSigla(filter));
			
			Ex.getInstance().getBL().processar(exDocumentoDTO.getMob().getExDocumento(), true, false, null);
		}
		return Action.SUCCESS;
	}
	
	private void logStatistics() {
		Statistics stats = ExDao.getInstance().getSessao().getSessionFactory().getStatistics();
		SessionStatistics statsSession = ExDao.getInstance().getSessao().getStatistics();
		
		double queryCacheHitCount = stats.getQueryCacheHitCount();
		double queryCacheMissCount = stats.getQueryCacheMissCount();
		double queryCacheHitRatio = queryCacheHitCount / (queryCacheHitCount + queryCacheMissCount);
		
		System.out.println("Query Hit ratio:" + queryCacheHitRatio);
		
		System.out
				.println(stats.getQueryExecutionMaxTimeQueryString() + " [time (ms): " + stats.getQueryExecutionMaxTime() + "]");
		
		System.out.println("\n\n\n\n\n\n*****************************************\n\n\n\n\n\n\n");
		
		for (String query : stats.getQueries()) {
			QueryStatistics qs = stats.getQueryStatistics(query);
			System.out.println(query + " [time (ms): " + qs.getExecutionAvgTime() + ", count: " + qs.getExecutionCount() + "]");
		}
		
		for (String ent : stats.getEntityNames()) {
			EntityStatistics es = stats.getEntityStatistics(ent);
			System.out.println(ent + " [count: " + es.getFetchCount() + "]");
		}
		
		System.out.println("\n\n\n\n\n\n*****************************************\n\n\n\n\n\n\n");
		
	}
	
	private void verificaDocumento(ExDocumento doc) throws AplicacaoException, Exception {
		if ((doc.getSubscritor() == null && doc.getNmSubscritor() == null && doc.getNmSubscritorExt() == null)
				&& ((doc.getExFormaDocumento().getExTipoFormaDoc().getIdTipoFormaDoc() == 2 && doc.isEletronico()) || doc
						.getExFormaDocumento().getExTipoFormaDoc().getIdTipoFormaDoc() != 2))
			throw new AplicacaoException("� necess�rio definir um subscritor para o documento.");
		
		if (doc.getDestinatario() == null && doc.getLotaDestinatario() == null
				&& (doc.getNmDestinatario() == null || doc.getNmDestinatario().trim().equals(""))
				&& doc.getOrgaoExternoDestinatario() == null
				&& (doc.getNmOrgaoExterno() == null || doc.getNmOrgaoExterno().trim().equals(""))) {
			Long idSit = Ex
					.getInstance()
					.getConf()
					.buscaSituacao(doc.getExModelo(), getTitular(), getLotaTitular(), CpTipoConfiguracao.TIPO_CONFIG_DESTINATARIO)
					.getIdSitConfiguracao();
			if (idSit == ExSituacaoConfiguracao.SITUACAO_OBRIGATORIO)
				throw new AplicacaoException("Para documentos do modelo " + doc.getExModelo().getNmMod()
						+ ", � necess�rio definir um destinat�rio");
		}
		
		if (doc.getExClassificacao() == null)
			throw new AplicacaoException("� necess�rio informar a classifica��o documental.");
		
	}
	
	private void buscarDocumentoOuNovo(boolean fVerificarAcesso, ExDocumentoDTO exDocumentoDTO) throws Exception {
		buscarDocumento(fVerificarAcesso, true, exDocumentoDTO);
		ExDocumento doc = exDocumentoDTO.getDoc();
		ExMobil mob = exDocumentoDTO.getMob();
		if (doc == null) {
			doc = new ExDocumento();
			doc.setExTipoDocumento(dao().consultar(ExTipoDocumento.TIPO_DOCUMENTO_INTERNO, ExTipoDocumento.class, false));
			mob = new ExMobil();
			mob.setExTipoMobil(dao().consultar(ExTipoMobil.TIPO_MOBIL_GERAL, ExTipoMobil.class, false));
			mob.setNumSequencia(1);
			mob.setExDocumento(doc);
			
			doc.setExMobilSet(new TreeSet<ExMobil>());
			doc.getExMobilSet().add(mob);
		}
	}
	
	private void buscarDocumento(boolean fVerificarAcesso, ExDocumentoDTO exDocumentoDTO) throws Exception {
		buscarDocumento(fVerificarAcesso, false, exDocumentoDTO);
	}
	
	private void buscarDocumento(boolean fVerificarAcesso, boolean fPodeNaoExistir, ExDocumentoDTO exDocumentoDto)
			throws Exception {
		if (exDocumentoDto.getMob() == null && exDocumentoDto.getSigla() != null && exDocumentoDto.getSigla().length() != 0) {
			final ExMobilDaoFiltro filter = new ExMobilDaoFiltro();
			filter.setSigla(exDocumentoDto.getSigla());
			exDocumentoDto.setMob((ExMobil) dao().consultarPorSigla(filter));
			if (exDocumentoDto.getMob() != null) {
				exDocumentoDto.setDoc(exDocumentoDto.getMob().getExDocumento());
			}
		} else if (exDocumentoDto.getMob() == null && exDocumentoDto.getDocumentoViaSel().getId() != null) {
			exDocumentoDto.setIdMob(exDocumentoDto.getDocumentoViaSel().getId());
			exDocumentoDto.setMob(dao().consultar(exDocumentoDto.getIdMob(), ExMobil.class, false));
		} else if (exDocumentoDto.getMob() == null && exDocumentoDto.getIdMob() != null && exDocumentoDto.getIdMob() != 0) {
			exDocumentoDto.setMob(dao().consultar(exDocumentoDto.getIdMob(), ExMobil.class, false));

		}
		if (exDocumentoDto.getMob() != null)
			exDocumentoDto.setDoc(exDocumentoDto.getMob().doc());
		if (exDocumentoDto.getDoc() == null) {
			String id = param("id");
			if (id != null && id.length() != 0) {
				exDocumentoDto.setDoc(daoDoc(Long.parseLong(id)));
			}
		}
		if (exDocumentoDto.getDoc() != null && exDocumentoDto.getMob() == null)
			exDocumentoDto.setMob(exDocumentoDto.getDoc().getMobilGeral());
		
		if (!fPodeNaoExistir && exDocumentoDto.getDoc() == null)
			throw new AplicacaoException("Documento n�o informado");
		if (fVerificarAcesso && exDocumentoDto.getMob() != null && exDocumentoDto.getMob().getIdMobil() != null)
			verificaNivelAcesso(exDocumentoDto.getMob());
	}
	
	@Get("/app/expediente/doc/finalizar")
	public void aFinalizar(String sigla) throws Exception {
		
		ExDocumentoDTO exDocumentoDto = new ExDocumentoDTO();
		exDocumentoDto.setSigla(sigla);
		
		buscarDocumento(true, exDocumentoDto);

		
		ExMobil mob = exDocumentoDto.getMob();
		
		verificaDocumento(exDocumentoDto.getDoc());
		
		if (!Ex.getInstance().getComp().podeFinalizar(getTitular(), getLotaTitular(), exDocumentoDto.getMob()))
			throw new AplicacaoException("N�o � poss�vel Finalizar");
		
		try {
			exDocumentoDto.setMsg(Ex.getInstance().getBL()
					.finalizar(getCadastrante(), getLotaTitular(), exDocumentoDto.getDoc(), null));
			

			if (exDocumentoDto.getDoc().getForm() != null) {
				if (exDocumentoDto.getDoc().getForm().get("acaoFinalizar") != null
						&& exDocumentoDto.getDoc().getForm().get("acaoFinalizar").trim().length() > 0) {
					obterMetodoPorString(exDocumentoDto.getDoc().getForm().get("acaoFinalizar"), exDocumentoDto.getDoc());

				}
			}
			
		} catch (final Throwable t) {
			throw new AplicacaoException("Erro ao finalizar documento", 0, t);
		}
		
		result.redirectTo("exibir?sigla=" + exDocumentoDto.getDoc().getCodigo());
		
	}
	
	public void aFinalizarAssinar(String sigla) throws Exception {
		
		aFinalizar(sigla);
		
		// buscarDocumento(true, exDocumentoDto);
		
		// return Action.SUCCESS;
	}
	
	@Post("app/expediente/doc/gravar")
	public String gravar(ExDocumentoDTO exDocumentoDTO) throws Exception {
		ExDocumento doc = exDocumentoDTO.getDoc();
		
		try {
			buscarDocumentoOuNovo(true, exDocumentoDTO);
			if (doc == null){
				doc = new ExDocumento();
				exDocumentoDTO.setDoc(doc);	
			}
			
			long tempoIni = System.currentTimeMillis();
			
			if (!validar()) {
				edita(exDocumentoDTO);
				getPar().put("alerta", new String[] { "Sim" });
				exDocumentoDTO.setAlerta("Sim");
				return "form_incompleto";
			}
			
			ByteArrayOutputStream baos = null;
			
			lerForm(exDocumentoDTO);
			
			if (!Ex.getInstance()
					.getConf()
					.podePorConfiguracao(getTitular(), getLotaTitular(), doc.getExTipoDocumento(), doc.getExFormaDocumento() 
							,doc.getExModelo(), doc.getExClassificacaoAtual(), doc.getExNivelAcesso()
							,CpTipoConfiguracao.TIPO_CONFIG_CRIAR)) {
				
				if (!Ex.getInstance().getConf().podePorConfiguracao(getTitular(), getLotaTitular(), null, null, null,
								doc.getExClassificacao(), null, CpTipoConfiguracao.TIPO_CONFIG_CRIAR))
					throw new AplicacaoException("Usu�rio n�o possui permiss�o de criar documento da classifica��o "
							+ doc.getExClassificacao().getCodificacao());
				
				throw new AplicacaoException("Opera��o n�o permitida");
			}
			

			System.out.println("monitorando gravacao IDDoc " + doc.getIdDoc() + ", PESSOA "+ doc.getCadastrante().getIdPessoa() 
							  +". Terminou verificacao de config PodeCriarModelo: "+ (System.currentTimeMillis() - tempoIni));
			
			tempoIni = System.currentTimeMillis();
			
			doc.setOrgaoUsuario(getLotaTitular().getOrgaoUsuario());
			

			if (exDocumentoDTO.isClassificacaoIntermediaria()
					&& (exDocumentoDTO.getDescrClassifNovo() == null || exDocumentoDTO.getDescrClassifNovo().trim()
							.length() == 0))
				throw new AplicacaoException("Quando a classifica��o selecionada n�o traz informa��o para cria��o de vias, o "
							                +"sistema exige que, antes de gravar o documento, seja informada uma sugest�o de "
						                    +"classifica��o para ser inclu�da na pr�xima revis�o da tabela de classifica��es.");
			
			if (doc.getDescrDocumento().length() > exDocumentoDTO.getTamanhoMaximoDescricao())
				throw new AplicacaoException("O campo descri��o possui mais do que "+ exDocumentoDTO.getTamanhoMaximoDescricao() 
						                    +" caracteres.");
			
			if (doc.isFinalizado()) {
				
				Date dt = dao().dt();
				Calendar c = Calendar.getInstance();
				c.setTime(dt);
				
				Calendar dtDocCalendar = Calendar.getInstance();
				
				if (doc.getDtDoc() == null)
					throw new Exception("A data do documento deve ser informada.");
				
				dtDocCalendar.setTime(doc.getDtDoc());
				
				if (c.before(dtDocCalendar))
					throw new Exception("N�o � permitido criar documento com data futura");
				
				verificaDocumento(doc);
			}
			
			ExMobil mobilAutuado = null;
			if (exDocumentoDTO.getIdMobilAutuado() != null) {
				
				mobilAutuado = dao().consultar(exDocumentoDTO.getIdMobilAutuado(), ExMobil.class, false);
				
				doc.setExMobilAutuado(mobilAutuado);
			}
			
			Ex.getInstance().getBL().gravar(getCadastrante(), getLotaTitular(), doc, null);
			
			lerEntrevista(exDocumentoDTO);
			
			if (exDocumentoDTO.getDesativarDocPai().equals("sim"))
				exDocumentoDTO.setDesativ("&desativarDocPai=sim");
			
			try {				
				Ex.getInstance().getBL().incluirCosignatariosAutomaticamente(getCadastrante(), getLotaTitular(), doc);				
			} catch (Exception e) {				
				throw new AplicacaoException("Erro ao tentar incluir os cosignat�rios deste documento", 0, e);				
			}
			
			result.redirectTo(this).exibe(false,exDocumentoDTO.getSigla());
			
		} catch (final Exception e) {
			throw new AplicacaoException("Erro na grava��o", 0, e);
		} finally {
		}
		
		if (param("ajax") != null && param("ajax").equals("true"))
			return "ajax";
		else
			return Action.SUCCESS;
	}
	
	public String aGravarPreenchimento(ExDocumentoDTO exDocumentoDTO) throws Exception {
		dao().iniciarTransacao();
		ExPreenchimento exPreenchimento = new ExPreenchimento();
		
		DpLotacao provLota = new DpLotacao();
		provLota = getLotaTitular();
		ExModelo provMod = new ExModelo();
		provMod.setIdMod(exDocumentoDTO.getIdMod());
		
		exPreenchimento.setDpLotacao(provLota);
		exPreenchimento.setExModelo(provMod);
		exPreenchimento.setNomePreenchimento(exDocumentoDTO.getNomePreenchimento());
		
		exPreenchimento.setPreenchimentoBA(getByteArrayFormPreenchimento());
		dao().gravar(exPreenchimento);
		dao().commitTransacao();
		
		exDocumentoDTO.setPreenchimento(exPreenchimento.getIdPreenchimento());
		
		return edita(exDocumentoDTO).toString();
		
	}
	
	public String aPrever(ExDocumentoDTO exDocumentoDTO) throws Exception {
		buscarDocumentoOuNovo(true, exDocumentoDTO);
		if (exDocumentoDTO.getDoc() != null) {
			if (getPostback() == null) {
				escreverForm(exDocumentoDTO);
			} else {
				
				lerForm(exDocumentoDTO);
			}
		} else {
			exDocumentoDTO.setDoc(new ExDocumento());
			lerForm(exDocumentoDTO);
		}
		
		carregarBeans(exDocumentoDTO);
		
		if (param("idMod") != null) {
			exDocumentoDTO.setModelo(dao().consultar(paramLong("idMod"), ExModelo.class, false));
		}
		
		if (param("processar_modelo") != null)
			return "processa_modelo";
		return Action.SUCCESS;
	}
	
	public String aPreverPdf(ExDocumentoDTO exDocumentoDTO) throws Exception {
		buscarDocumentoOuNovo(true, exDocumentoDTO);
		if (exDocumentoDTO.getDoc() != null) {
			if (getPostback() == null) {
				escreverForm(exDocumentoDTO);
			} else {
				lerForm(exDocumentoDTO);
			}
		} else {
			exDocumentoDTO.setDoc(new ExDocumento());
			lerForm(exDocumentoDTO);
		}
		
		carregarBeans(exDocumentoDTO);
		
		if (param("idMod") != null) {
			exDocumentoDTO.setModelo(dao().consultar(paramLong("idMod"), ExModelo.class, false));
		}
		
		Ex.getInstance().getBL().processar(exDocumentoDTO.getDoc(), false, false, null);
		
		exDocumentoDTO.setPdfStreamResult(new ByteArrayInputStream(exDocumentoDTO.getDoc().getConteudoBlobPdf()));
		
		return Action.SUCCESS;
	}
	
	@Get("app/expediente/doc/refazer")
	public void refazer(String sigla) throws Exception {
		ExDocumentoDTO exDocumentoDTO = new ExDocumentoDTO(sigla);
		this.buscarDocumento(true, exDocumentoDTO);
		
		if (!Ex.getInstance().getComp().podeRefazer(getTitular(), getLotaTitular(), exDocumentoDTO.getMob()))
			throw new AplicacaoException("N�o � poss�vel refazer o documento");
		
		try {
			exDocumentoDTO.setDoc(Ex.getInstance().getBL().refazer(getCadastrante(), getLotaTitular(), exDocumentoDTO.getDoc()));
		} catch (final Exception e) {
			throw e;
		}
		ExDocumentoController.redirecionarParaExibir(result, sigla);
	}
	
	public String aAtualizarMarcasDoc(ExDocumentoDTO exDocumentoDTO) throws Exception {
		
		buscarDocumento(false, exDocumentoDTO);
		Ex.getInstance().getBL().atualizarMarcas(exDocumentoDTO.getDoc());
		
		return Action.SUCCESS;
	}
	
	public String aTestarPdf() throws Exception {
		return Action.SUCCESS;
	}
	
	public String aTesteEnvioDJE() throws Exception {
		
		try {
			ExMovimentacao fakeMov = ExDao.getInstance().consultar(39468L, ExMovimentacao.class, false);
			ExDocumento doque = fakeMov.getExDocumento();
			GeradorRTF gerador = new GeradorRTF();
			String nomeArq = doque.getIdDoc().toString();
			fakeMov.setConteudoBlobRTF(nomeArq, gerador.geraRTF(doque));
			fakeMov.setConteudoBlobXML(nomeArq, PublicacaoDJEBL.gerarXMLPublicacao(fakeMov, "A", "SESIA", "Teste descri��o"));
			fakeMov.setNmArqMov(nomeArq + ".zip");
			
			PublicacaoDJEBL.primeiroEnvio(fakeMov);
			return Action.SUCCESS;
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public String aCorrigirArquivamentosVolume() throws Exception {
		int idPrimeiroDoc, idUltimoDoc;
		boolean efetivar;
		try {
			idPrimeiroDoc = Integer.valueOf(param("de"));
		} catch (Exception e) {
			idPrimeiroDoc = 1;
		}
		try {
			idUltimoDoc = Integer.valueOf(param("ate"));
		} catch (Exception e) {
			idUltimoDoc = 999999999;
		}
		try {
			efetivar = Boolean.parseBoolean(param("efetivar"));
		} catch (Exception e) {
			efetivar = false;
		}
		Ex.getInstance().getBL().corrigirArquivamentosEmVolume(idPrimeiroDoc, idUltimoDoc, efetivar);
		return Action.SUCCESS;
	}
	
	public String aTesteExclusaoDJE() throws Exception {
		ExMovimentacao fakeMov = ExDao.getInstance().consultar(37644L, ExMovimentacao.class, false);
		PublicacaoDJEBL.cancelarRemessaPublicacao(fakeMov);
		return null;
	}
	
	public String aMassaTesteDJE2() throws Exception {
		DpPessoa edson = new DpPessoa();
		edson.setSigla("RJ13285");
		edson = CpDao.getInstance().consultar(edson, null).get(0);
		
		ExDao exDao = ExDao.getInstance();
		StringBuffer appender = new StringBuffer(
				"from ExDocumento doc where (doc.exModelo.hisIdIni in (73, 76) and doc.exModelo.hisAtivo = 1) and doc.dtFechamento between :start and :end");
		Query query = exDao.getSessao().createQuery(appender.toString());
		Calendar cal = new GregorianCalendar();
		cal.set(2008, 07, 14);
		query.setDate("start", cal.getTime());
		cal.set(2008, 07, 18);
		query.setDate("end", cal.getTime());
		
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_MONTH, 2);
		
		List<ExDocumento> doques = query.list();
		for (ExDocumento ex : doques) { /*
										 * ExDocumentoBL.remeterParaPublicacao(edson , edson.getLotacao(), ex, null, new Date(),
										 * edson, edson, edson.getLotacao(), cal .getTime());
										 */
			/*
			 * ExMovimentacao fakeMov = new ExMovimentacao(); GeradorRTF gerador = new GeradorRTF(); String nomeArq = "I-" +
			 * doc.getIdDoc(); fakeMov.setConteudoBlobRTF(nomeArq, gerador.geraRTF(doc)); fakeMov.setConteudoBlobXML(nomeArq,
			 * PublicacaoDJEBL .gerarXMLPublicacao(fakeMov)); fakeMov.setNmArqMov(nomeArq + ".zip"); Long numTRF = PublicacaoDJEBL
			 * .verificaPrimeiroRetornoPublicacao(fakeMov); /* final Compactador zip = new Compactador(); final byte[] arqZip =
			 * getConteudoBlobMov2(); byte[] conteudoZip = null; conteudoZip = zip.adicionarStream(nome, conteudo, arqZip);
			 * setConteudoBlobMov2(conteudoZip);
			 */
			
		}
		return Action.SUCCESS;
	}
	
	@Get("app/expediente/doc/duplicar")
	public void aDuplicar(boolean conviteEletronico, String sigla) throws Exception {
		
		ExDocumentoDTO exDocumentoDto = new ExDocumentoDTO();
		exDocumentoDto.setSigla(sigla);
		buscarDocumento(false, exDocumentoDto);
		if (!Ex.getInstance().getComp().podeDuplicar(getTitular(), getLotaTitular(), exDocumentoDto.getMob()))
			throw new AplicacaoException("N�o � poss�vel duplicar o documento");
		try {
			exDocumentoDto.setDoc(Ex.getInstance().getBL().duplicar(getCadastrante(), getLotaTitular(), exDocumentoDto.getDoc()));

		} catch (final Exception e) {
			throw e;
		}
		result.redirectTo("exibir?sigla=" + exDocumentoDto.getDoc().getCodigo());
	}
	
	private void atualizaLotacoesPreenchimentos(DpLotacao lotaTitular) throws AplicacaoException {
		if (lotaTitular.getIdLotacao().longValue() != lotaTitular.getIdLotacaoIni().longValue()) {
			ExPreenchimento exp = new ExPreenchimento();
			exp.setDpLotacao(daoLot(lotaTitular.getIdLotacaoIni()));
			for (ExPreenchimento exp2 : dao().consultar(exp)) {
				dao().iniciarTransacao();
				exp2.setDpLotacao(lotaTitular);
				dao().gravar(exp2);
				dao().commitTransacao();
			}
		}
	}
	
	public String aDesfazerCancelamentoDocumento(ExDocumentoDTO exDocumentoDTO) throws Exception {
		buscarDocumento(true, exDocumentoDTO);
		if (!Ex.getInstance().getComp()
				.podeDesfazerCancelamentoDocumento(getTitular(), getLotaTitular(), exDocumentoDTO.getMob()))
			throw new AplicacaoException("N�o � poss�vel desfazer o cancelamento deste documento");
		try {

			Ex.getInstance().getBL().DesfazerCancelamentoDocumento(getCadastrante(), getLotaTitular(), exDocumentoDTO.getDoc());

		} catch (final Exception e) {
			throw e;
		}
		return Action.SUCCESS;
	}
	
	public String aTornarDocumentoSemEfeito(ExDocumentoDTO exDocumentoDTO) throws Exception {
		buscarDocumento(true, exDocumentoDTO);
		return Action.SUCCESS;
	}
	
	public String aTornarDocumentoSemEfeitoGravar(ExDocumentoDTO exDocumentoDTO) throws Exception {
		if (exDocumentoDTO.getDescrMov() == null || exDocumentoDTO.getDescrMov().trim().length() == 0) {
			throw new AplicacaoException("O preenchimento do campo MOTIVO � obrigat�rio!");
		}

		buscarDocumento(true, exDocumentoDTO);
		if (!Ex.getInstance().getComp()
				.podeTornarDocumentoSemEfeito(getTitular(), getLotaTitular(), exDocumentoDTO.getMob()))

			throw new AplicacaoException("N�o � poss�vel tornar documento sem efeito.");
		try {
			Ex.getInstance()
					.getBL()
					.TornarDocumentoSemEfeito(getCadastrante(), getLotaTitular(), exDocumentoDTO.getDoc(),
							exDocumentoDTO.getDescrMov());
		} catch (final Exception e) {
			throw e;
		}
		return Action.SUCCESS;
	}
	
	public String acriarDocTest() throws Exception {
		try {
			setMensagem(Ex.getInstance().getBL().criarDocTeste());
		} catch (final Exception e) {
			throw e;
		}
		
		return Action.SUCCESS;
	}
	
	private void carregarBeans(ExDocumentoDTO exDocumentoDTO) throws Exception {
		ExMobil mobPai = null;
		

		exDocumentoDTO.getDoc().setExTipoDocumento(dao().consultar(exDocumentoDTO.getIdTpDoc(), ExTipoDocumento.class, false));
		
		// Quest�es referentes a doc pai-----------------------------
		
		if (exDocumentoDTO.getDoc().getIdDoc() == null) {
			String req = "nao";
			if (getPar().get("reqdocumentoRefSel") != null)
				req = getPar().get("reqmobilPaiSel")[0].toString();
			
			if (param("mobilPaiSel.sigla") != null)
				exDocumentoDTO.getMobilPaiSel().setSigla(param("mobilPaiSel.sigla"));
			exDocumentoDTO.getMobilPaiSel().buscar();
			if ((exDocumentoDTO.getMobilPaiSel().getId() != null) || (req.equals("sim"))) {
				
				if (exDocumentoDTO.getMobilPaiSel().getId() != null) {
					// Documento Pai
					mobPai = daoMob(exDocumentoDTO.getMobilPaiSel().getId());
					
					Integer idForma = paramInteger("idForma");
					if (idForma != null)
						exDocumentoDTO.setIdFormaDoc(idForma);
					
					if (exDocumentoDTO.getClassificacaoSel() != null
							&& exDocumentoDTO.getClassificacaoSel().getId() == null)
						exDocumentoDTO.getClassificacaoSel().setId(mobPai.doc().getExClassificacaoAtual().getId());
					
					exDocumentoDTO.setDescrDocumento(mobPai.doc().getDescrDocumento());
					
					exDocumentoDTO.setDesativarDocPai("sim");
				}
				
			}
			
			if (exDocumentoDTO.getAutuando() && exDocumentoDTO.getIdMobilAutuado() != null) {
				ExMobil mobilAutuado = daoMob(exDocumentoDTO.getIdMobilAutuado());
				
				exDocumentoDTO.getDoc().setExMobilAutuado(mobilAutuado);
				
				exDocumentoDTO.getClassificacaoSel().setId(mobilAutuado.getDoc().getExClassificacao().getId());
				exDocumentoDTO.setDescrDocumento(mobilAutuado.getDoc().getDescrDocumento());
			}
		}
		
		// Fim das quest�es referentes a doc pai--------------------
		
		Integer idFormaDoc = exDocumentoDTO.getIdFormaDoc();
		if (idFormaDoc != null) {
			if (idFormaDoc == 0) {
				exDocumentoDTO.setIdMod(0L);
			} else {
				
				// Mudou origem? Escolhe um tipo automaticamente--------
				// V� se usu�rio alterou campo Origem. Caso sim, seleciona um
				// tipo
				// automaticamente, dentro daquela origem
				
				final List<ExFormaDocumento> formasDoc = getFormasDocPorTipo(exDocumentoDTO);
				
				ExFormaDocumento forma = dao().consultar(exDocumentoDTO.getIdFormaDoc(), ExFormaDocumento.class, false);
				
				if (!formasDoc.contains(forma)) {
					exDocumentoDTO.setIdFormaDoc(exDocumentoDTO.getFormaDocPorTipo().getIdFormaDoc());
					forma = dao().consultar(exDocumentoDTO.getIdFormaDoc(), ExFormaDocumento.class, false);
				}
				
				// Fim -- Mudou origem? Escolhe um tipo automaticamente--------
				
				if (forma.getExModeloSet().size() == 0) {
					exDocumentoDTO.setIdMod(0L);
				}
			}
		}
		
		ExModelo mod = null;
		if (exDocumentoDTO.getIdMod() != null && exDocumentoDTO.getIdMod() != 0) {
			mod = dao().consultar(exDocumentoDTO.getIdMod(), ExModelo.class, false);
		}
		if (mod != null) {
			mod = mod.getModeloAtual();
		}
		
		List<ExModelo> modelos = getModelos(exDocumentoDTO);
		if (mod == null || !modelos.contains(mod)) {
			mod = (ExModelo) (modelos.toArray())[0];
			
			for (ExModelo modeloAtual : modelos) {
				if (modeloAtual.getIdMod() != null && modeloAtual.getIdMod() != 0
						&& modeloAtual.getNmMod().equals(modeloAtual.getExFormaDocumento().getDescricao())) {
					mod = modeloAtual;
					break;
				}
			}
			
			exDocumentoDTO.setIdMod(mod.getIdMod());
			if ((exDocumentoDTO.getIdMod() != 0) && (exDocumentoDTO.getMobilPaiSel().getId() == null)
					&& (exDocumentoDTO.getIdMobilAutuado() == null))
				exDocumentoDTO.getClassificacaoSel().apagar();
		}
		
		if (getPreenchimentos(exDocumentoDTO).size() <= 1) {
			exDocumentoDTO.setPreenchimento(0L);
		}
		
		if (exDocumentoDTO.isAlterouModelo() && exDocumentoDTO.getMobilPaiSel().getId() == null
				&& exDocumentoDTO.getIdMobilAutuado() == null)
			exDocumentoDTO.getClassificacaoSel().apagar();
		
		boolean naLista = false;
		final Set<ExPreenchimento> preenchimentos = getPreenchimentos(exDocumentoDTO);
		if (preenchimentos != null && preenchimentos.size() > 0) {
			for (ExPreenchimento exp : preenchimentos) {
				if (exp.getIdPreenchimento().equals(exDocumentoDTO.getPreenchimento())) {
					naLista = true;
					break;
				}
			}
			if (!naLista)
				exDocumentoDTO.setPreenchimento(((ExPreenchimento) (preenchimentos.toArray())[0]).getIdPreenchimento());
		}
		
		exDocumentoDTO.setModelo(mod);
		if (mod.getExClassificacao() != null
				&& mod.getExClassificacao().getId() != exDocumentoDTO.getClassificacaoSel().getId()) {
			exDocumentoDTO.getClassificacaoSel().buscarPorObjeto(mod.getExClassificacao());

		}
		
	}
	
	private void escreverForm(ExDocumentoDTO exDocumentoDTO) throws IllegalAccessException, NoSuchMethodException,
			AplicacaoException, InvocationTargetException {
		ExDocumento doc = exDocumentoDTO.getDoc();
		
		// Destino , Origem
		DpLotacao backupLotaTitular = getLotaTitular();
		DpPessoa backupTitular = getTitular();
		DpPessoa backupCadastrante = getCadastrante();
		
		BeanUtils.copyProperties(this, doc);
		
		setTitular(backupTitular);
		setLotaTitular(backupLotaTitular);
		// Orlando: Inclus�o da linha, abaixo, para preservar o cadastrante do
		// ambiente.
		setCadastrante(backupCadastrante);
		
		if (doc.getConteudoBlob("doc.htm") != null)
			exDocumentoDTO.setConteudo(new String(doc.getConteudoBlob("doc.htm")));
		
		exDocumentoDTO.setIdTpDoc(doc.getExTipoDocumento().getIdTpDoc());
		exDocumentoDTO.setNivelAcesso(doc.getIdExNivelAcesso());
		
		if (doc.getExFormaDocumento() != null) {
			exDocumentoDTO.setIdFormaDoc(doc.getExFormaDocumento().getIdFormaDoc());
		}
		
		ExClassificacao classif = doc.getExClassificacaoAtual();
		if (classif != null)
			exDocumentoDTO.getClassificacaoSel().buscarPorObjeto(classif.getAtual());
		exDocumentoDTO.getSubscritorSel().buscarPorObjeto(doc.getSubscritor());
		// form.getDestinatarioSel().buscarPorObjeto(doc.getDestinatario());
		if (doc.getExModelo() != null) {
			ExModelo modeloAtual = doc.getExModelo().getModeloAtual();
			if (modeloAtual != null) {
				exDocumentoDTO.setIdMod(modeloAtual.getIdMod());
			}
		}
		
		if (doc.getDestinatario() != null) {
			exDocumentoDTO.getDestinatarioSel().buscarPorObjeto(doc.getDestinatario());
			exDocumentoDTO.setTipoDestinatario(1);
		}
		if (doc.getLotaDestinatario() != null) {
			exDocumentoDTO.getLotacaoDestinatarioSel().buscarPorObjeto(doc.getLotaDestinatario());
			if (doc.getDestinatario() == null)
				exDocumentoDTO.setTipoDestinatario(2);
		}
		
		if (doc.getExMobilPai() != null) {
			exDocumentoDTO.getMobilPaiSel().buscarPorObjeto(doc.getExMobilPai());
		}
		
		if (doc.getTitular() != null && doc.getSubscritor() != null
				&& !doc.getTitular().getIdPessoa().equals(doc.getSubscritor().getIdPessoa())) {
			exDocumentoDTO.getTitularSel().buscarPorObjeto(doc.getTitular());
			exDocumentoDTO.setSubstituicao(true);
		}
		
		// TODO Verificar se ha realmente a necessidade de setar novamente o
		// n�vel de acesso do documento
		// tendo em vista que o n�vel de acesso j� foi setado anteriormente
		// neste mesmo m�todo sem que o documento fosse alterado
		exDocumentoDTO.setNivelAcesso(doc.getIdExNivelAcesso());
		
		if (doc.getOrgaoExternoDestinatario() != null) {
			exDocumentoDTO.getOrgaoExternoDestinatarioSel().buscarPorObjeto(doc.getOrgaoExternoDestinatario());
			exDocumentoDTO.setTipoDestinatario(3);
		}
		if (doc.getNmOrgaoExterno() != null && !doc.getNmOrgaoExterno().equals("")) {
			exDocumentoDTO.setTipoDestinatario(3);
		}
		if (doc.getNmDestinatario() != null) {
			exDocumentoDTO.setNmDestinatario(doc.getNmDestinatario());
			exDocumentoDTO.setTipoDestinatario(4);
		}
		
		if (doc.getOrgaoExterno() != null) {
			exDocumentoDTO.getCpOrgaoSel().buscarPorObjeto(doc.getOrgaoExterno());
			exDocumentoDTO.setIdTpDoc(3L);
		}
		
		if (doc.getObsOrgao() != null) {
			exDocumentoDTO.setObsOrgao(doc.getObsOrgao());
		}
		
		final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		try {
			exDocumentoDTO.setDtDocString(df.format(doc.getDtDoc()));
		} catch (final Exception e) {
		}
		
		try {
			exDocumentoDTO.setDtDocOriginalString(df.format(doc.getDtDocOriginal()));
		} catch (final Exception e) {
		}
		
		if (doc.getAnoEmissao() != null)
			exDocumentoDTO.setAnoEmissaoString(doc.getAnoEmissao().toString());
		
		exDocumentoDTO.setEletronico(doc.isEletronico() ? 1 : 2);
		
	}
	
	private byte[] getByteArrayFormPreenchimento() throws Exception {
		ByteArrayOutputStream baos = null;
		String[] aVars = getPar().get("vars");
		String[] aCampos = getPar().get("campos");
		ArrayList<String> aFinal = new ArrayList<String>();
		if (aVars != null && aVars.length > 0)
			for (String str : aVars) {
				aFinal.add(str);
			}
		if (aCampos != null && aCampos.length > 0)
			for (String str : aCampos) {
				aFinal.add(str);
			}
		if (aFinal != null && aFinal.size() > 0) {
			baos = new ByteArrayOutputStream();
			for (final String s : aFinal) {
				if (param(s) != null && !param(s).trim().equals("") && !s.trim().equals("preenchimento")
						&& !param(s).matches("[0-9]{2}/[0-9]{2}/[0-9]{4}")/*
																		   * s.trim ( ). equals ( "dtDocString" )
																		   */) {
					if (baos.size() > 0)
						baos.write('&');
					baos.write(s.getBytes());
					baos.write('=');
					
					// Deveria estar gravando como UTF-8
					baos.write(URLEncoder.encode(param(s), "iso-8859-1").getBytes());
				}
			}
		}
		return baos.toByteArray();
	}
	
	private void lerEntrevista(ExDocumentoDTO exDocumentoDTO) {
		final ExDocumento doc = exDocumentoDTO.getDoc();
		if (doc.getExModelo() != null) {
			final byte[] form = doc.getConteudoBlob("doc.form");
			if (form != null) {
				final String as[] = new String(form).split("&");
				for (final String s : as) {
					final String param[] = s.split("=");
					try {
						if (param.length == 2)
							exDocumentoDTO.getParamsEntrevista().put(param[0], URLDecoder.decode(param[1], "iso-8859-1"));

						// setParam(param[0], URLDecoder.decode(param[1],
						// "iso-8859-1"));
					} catch (final UnsupportedEncodingException e) {
					}
				}
			}
		}
	}
	

	private void lerForm(ExDocumentoDTO exDocumentoDTO) throws IllegalAccessException, NoSuchMethodException, AplicacaoException {
		
		ExDocumento doc = exDocumentoDTO.getDoc();
		if (exDocumentoDTO.getAnexar()) {
			doc.setConteudoTpDoc(exDocumentoDTO.getConteudoTpDoc());
			doc.setNmArqDoc(exDocumentoDTO.getNmArqDoc());
		}
		
		// BeanUtils.copyProperties(doc, form);
		// fabrica = DaoFactory.getDAOFactory();
		doc.setDescrDocumento(exDocumentoDTO.getDescrDocumento());
		doc.setNmSubscritorExt(exDocumentoDTO.getNmSubscritorExt());
		doc.setNmFuncaoSubscritor(exDocumentoDTO.getNmFuncaoSubscritor());
		doc.setNumExtDoc(exDocumentoDTO.getNumExtDoc());
		doc.setNumAntigoDoc(exDocumentoDTO.getNumAntigoDoc());
		doc.setObsOrgao(exDocumentoDTO.getObsOrgao());
		doc.setEletronico(exDocumentoDTO.getEletronico() == 1 ? true : false);
		doc.setNmOrgaoExterno(exDocumentoDTO.getNmOrgaoExterno());
		doc.setDescrClassifNovo(exDocumentoDTO.getDescrClassifNovo());
		
		doc.setExNivelAcesso(dao().consultar(exDocumentoDTO.getNivelAcesso(), ExNivelAcesso.class, false));
		
		doc.setExTipoDocumento(dao().consultar(exDocumentoDTO.getIdTpDoc(), ExTipoDocumento.class, false));
		
		if (!doc.isFinalizado())
			doc.setExFormaDocumento(dao().consultar(exDocumentoDTO.getIdFormaDoc(), ExFormaDocumento.class, false));
		doc.setNmDestinatario(exDocumentoDTO.getNmDestinatario());
		
		doc.setExModelo(null);
		if (exDocumentoDTO.getIdMod() != 0) {
			ExModelo modelo = dao().consultar(exDocumentoDTO.getIdMod(), ExModelo.class, false);
			if (modelo != null)
				doc.setExModelo(modelo.getModeloAtual());
		}
		
		if (exDocumentoDTO.getClassificacaoSel().getId() != null && exDocumentoDTO.getClassificacaoSel().getId() != 0) {
			
			ExClassificacao classificacao = dao().consultar(exDocumentoDTO.getClassificacaoSel().getId(), ExClassificacao.class, 
					false);

			
			if (classificacao != null && !classificacao.isFechada())
				doc.setExClassificacao(classificacao);
			else {
				doc.setExClassificacao(null);
				exDocumentoDTO.getClassificacaoSel().apagar();
			}
			
		} else
			doc.setExClassificacao(null);
		if (exDocumentoDTO.getCpOrgaoSel().getId() != null) {
			doc.setOrgaoExterno(dao().consultar(exDocumentoDTO.getCpOrgaoSel().getId(), CpOrgao.class, false));
		} else
			doc.setOrgaoExterno(null);
		
		// Orlando: Alterei o IF abaixo incluindo a instru��o
		// "doc.setLotaCadastrante(getLotaTitular());".
		// Esta linha estava "solta",ap�s o IF, e era executada sempre.
		// Fiz esta modifica��o porque esta linha alterava a lota��o do
		// cadastrante, n�o permitindo que este,
		// ao preencher o campo subscritor com a matr�cula de outro usu�rio,
		// tivesse acesso ao documento.
		
		if (doc.getCadastrante() == null) {
			doc.setCadastrante(getCadastrante());
			doc.setLotaCadastrante(getLotaTitular());
		}
		
		if (doc.getLotaCadastrante() == null)
			doc.setLotaCadastrante(doc.getCadastrante().getLotacao());
		if (exDocumentoDTO.getSubscritorSel().getId() != null) {
			doc.setSubscritor(daoPes(exDocumentoDTO.getSubscritorSel().getId()));
			doc.setLotaSubscritor(doc.getSubscritor().getLotacao());
		} else {
			doc.setSubscritor(null);
		}
		
		if (exDocumentoDTO.isSubstituicao()) {
			if (exDocumentoDTO.getTitularSel().getId() != null) {
				doc.setTitular(daoPes(exDocumentoDTO.getTitularSel().getId()));
				doc.setLotaTitular(doc.getTitular().getLotacao());
			} else {
				doc.setTitular(doc.getSubscritor());
				doc.setLotaTitular(doc.getLotaSubscritor());
			}
		} else {
			doc.setTitular(doc.getSubscritor());
			doc.setLotaTitular(doc.getLotaSubscritor());
		}
		
		if (exDocumentoDTO.getDestinatarioSel().getId() != null) {
			doc.setDestinatario(daoPes(exDocumentoDTO.getDestinatarioSel().getId()));
			doc.setLotaDestinatario(daoPes(exDocumentoDTO.getDestinatarioSel().getId()).getLotacao());
			doc.setOrgaoExternoDestinatario(null);
		} else {
			doc.setDestinatario(null);
			if (exDocumentoDTO.getLotacaoDestinatarioSel().getId() != null) {
				doc.setLotaDestinatario(daoLot(exDocumentoDTO.getLotacaoDestinatarioSel().getId()));
				doc.setOrgaoExternoDestinatario(null);
			} else {
				doc.setLotaDestinatario(null);

				if (exDocumentoDTO.getOrgaoExternoDestinatarioSel().getId() != null) {
					doc.setOrgaoExternoDestinatario(dao().consultar(exDocumentoDTO.getOrgaoExternoDestinatarioSel().getId(), 
							CpOrgao.class, false));

				} else {
					doc.setOrgaoExternoDestinatario(null);
				}
			}
		}
		
		final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		try {
			doc.setDtDoc(df.parse(exDocumentoDTO.getDtDocString()));
		} catch (final ParseException e) {
			doc.setDtDoc(null);
		} catch (final NullPointerException e) {
			doc.setDtDoc(null);
		}
		if (doc.getDtRegDoc() == null)
			doc.setDtRegDoc(dao().dt());
		
		try {
			doc.setDtDocOriginal(df.parse(exDocumentoDTO.getDtDocOriginalString()));
		} catch (final ParseException e) {
			doc.setDtDocOriginal(null);
		} catch (final NullPointerException e) {
			doc.setDtDocOriginal(null);
		}
		
		if (exDocumentoDTO.getNumExpediente() != null) {
			doc.setNumExpediente(new Long(exDocumentoDTO.getNumExpediente()));
			doc.setAnoEmissao(new Long(exDocumentoDTO.getAnoEmissaoString()));
		}
		
		if (exDocumentoDTO.getMobilPaiSel().getId() != null) {
			doc.setExMobilPai(dao().consultar(exDocumentoDTO.getMobilPaiSel().getId(), ExMobil.class, false));
		} else {
			doc.setExMobilPai(null);
		}
		
		try {
			ByteArrayOutputStream baos;
			
			final String marcacoes[] = { "<!-- INICIO NUMERO -->", "<!-- FIM NUMERO -->", "<!-- INICIO NUMERO", "FIM NUMERO -->",
					"<!-- INICIO TITULO", "FIM TITULO -->", "<!-- INICIO MIOLO -->", "<!-- FIM MIOLO -->",
					"<!-- INICIO CORPO -->", "<!-- FIM CORPO -->", "<!-- INICIO CORPO", "FIM CORPO -->",
					"<!-- INICIO ASSINATURA -->", "<!-- FIM ASSINATURA -->", "<!-- INICIO ABERTURA -->", "<!-- FIM ABERTURA -->",
					"<!-- INICIO ABERTURA", "FIM ABERTURA -->", "<!-- INICIO FECHO -->", "<!-- FIM FECHO -->" };
			
			final String as[] = getPar().get("vars");
			if (as != null) {
				baos = new ByteArrayOutputStream();
				for (final String s : as) {
					if (baos.size() > 0)
						baos.write('&');
					baos.write(s.getBytes());
					baos.write('=');
					if (param(s) != null) {
						String parametro = param(s);
						for (final String m : marcacoes) {
							if (parametro.contains(m))
								parametro = parametro.replaceAll(m, "");
						}
						if (!FuncoesEL.contemTagHTML(parametro)) {
							if (parametro.contains("\"")) {
								parametro = parametro.replace("\"", "&quot;");
								setParam(s, parametro);
							}
						}
						
						baos.write(URLEncoder.encode(parametro, "iso-8859-1").getBytes());
					}
				}
				doc.setConteudoTpDoc("application/zip");
				doc.setConteudoBlobForm(baos.toByteArray());
				
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Retorna o conte�do do arquivo em um array de Byte
	public byte[] toByteArray(final File file) throws IOException {
		
		final InputStream is = new FileInputStream(file);
		
		// Get the size of the file
		final long tamanho = file.length();
		
		// N�o podemos criar um array usando o tipo long.
		// � necess�rio usar o tipo int.
		if (tamanho > Integer.MAX_VALUE)
			throw new IOException("Arquivo muito grande");
		
		// Create the byte array to hold the data
		final byte[] meuByteArray = new byte[(int) tamanho];
		
		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < meuByteArray.length && (numRead = is.read(meuByteArray, offset, meuByteArray.length - offset)) >= 0) {
			offset += numRead;
		}
		
		// Ensure all the bytes have been read in
		if (offset < meuByteArray.length)
			throw new IOException("N�o foi poss�vel ler o arquivo completamente " + file.getName());
		
		// Close the input stream and return bytes
		is.close();
		return meuByteArray;
	}
	
	public List<ExModelo> getModelos(ExDocumentoDTO exDocumentoDTO) throws Exception {
		if (exDocumentoDTO.getModelos() != null)
			return exDocumentoDTO.getModelos();
		
		ExFormaDocumento forma = null;
		if (exDocumentoDTO.getIdFormaDoc() != null && exDocumentoDTO.getIdFormaDoc() != 0)
			forma = dao().consultar(exDocumentoDTO.getIdFormaDoc(), ExFormaDocumento.class, false);
		
		String headerValue = null;
		if (exDocumentoDTO.getTipoDocumento() != null && exDocumentoDTO.getTipoDocumento().equals("antigo"))
			headerValue = "N�o Informado";
		
		exDocumentoDTO.setModelos(Ex
				.getInstance()
				.getBL()
				.obterListaModelos(forma, exDocumentoDTO.getDespachando(), headerValue, true, getTitular(), getLotaTitular(), 
						exDocumentoDTO.getAutuando()));
		return exDocumentoDTO.getModelos();

		
	}
	
	public List<ExFormaDocumento> getFormasDocPorTipo(ExDocumentoDTO exDocumentoDTO) throws Exception {
		if (exDocumentoDTO.getFormasDoc() == null) {
			exDocumentoDTO.setFormasDoc(new ArrayList<ExFormaDocumento>());
			ExBL bl = Ex.getInstance().getBL();

			exDocumentoDTO.getFormasDoc().addAll(
					bl.obterFormasDocumento(bl.obterListaModelos(null, exDocumentoDTO.getDespachando(), null, true, getTitular(), 
							getLotaTitular(), exDocumentoDTO.getAutuando()), exDocumentoDTO.getDoc().getExTipoDocumento(), null));

		}
		
		return exDocumentoDTO.getFormasDoc();
	}
	
	public Set<ExPreenchimento> getPreenchimentos(ExDocumentoDTO exDocumentoDTO) throws AplicacaoException {
		if (exDocumentoDTO.getPreenchSet() != null)
			return exDocumentoDTO.getPreenchSet();
		
		exDocumentoDTO.setPreenchSet(new LinkedHashSet<ExPreenchimento>());
		if (exDocumentoDTO.getIdFormaDoc() == null || exDocumentoDTO.getIdFormaDoc() == 0)
			return exDocumentoDTO.getPreenchSet();
		
		ExPreenchimento preench = new ExPreenchimento();
		if (exDocumentoDTO.getIdMod() != null && exDocumentoDTO.getIdMod() != 0L)
			preench.setExModelo(dao().consultar(exDocumentoDTO.getIdMod(), ExModelo.class, false));
		
		DpLotacao lota = new DpLotacao();
		lota.setIdLotacaoIni(getLotaTitular().getIdLotacaoIni());
		List<DpLotacao> lotacaoSet = dao().consultar(lota, null);
		
		exDocumentoDTO.getPreenchSet().add(new ExPreenchimento(0, null, " [Em branco] ", null));
		
		if (exDocumentoDTO.getIdMod() != null && exDocumentoDTO.getIdMod() != 0) {
			for (DpLotacao lotacao : lotacaoSet) {
				preench.setDpLotacao(lotacao);
				exDocumentoDTO.getPreenchSet().addAll(dao().consultar(preench));
			}
		}
		
		return exDocumentoDTO.getPreenchSet();
	}
	
	public List<ExNivelAcesso> getListaNivelAcesso(ExDocumentoDTO exDocumentoDTO) throws Exception {
		ExFormaDocumento exForma = new ExFormaDocumento();
		ExClassificacao exClassif = new ExClassificacao();
		ExTipoDocumento exTipo = new ExTipoDocumento();
		ExModelo exMod = new ExModelo();
		
		if (exDocumentoDTO.getIdTpDoc() != null) {
			exTipo = dao().consultar(exDocumentoDTO.getIdTpDoc(), ExTipoDocumento.class, false);
		}
		
		if (exDocumentoDTO.getIdFormaDoc() != null) {
			exForma = dao().consultar(exDocumentoDTO.getIdFormaDoc(), ExFormaDocumento.class, false);
		}
		
		if (exDocumentoDTO.getIdMod() != null && exDocumentoDTO.getIdMod() != 0) {
			exMod = dao().consultar(exDocumentoDTO.getIdMod(), ExModelo.class, false);
		}
		
		if (exDocumentoDTO.getClassificacaoSel().getId() != null) {
			exClassif = dao().consultar(exDocumentoDTO.getClassificacaoSel().getId(), ExClassificacao.class, false);
		}
		
		return getListaNivelAcesso(exTipo, exForma, exMod, exClassif);
	}
	
	public ExNivelAcesso getNivelAcessoDefault(ExDocumentoDTO exDocumentoDTO) throws Exception {
		ExFormaDocumento exForma = new ExFormaDocumento();
		ExClassificacao exClassif = new ExClassificacao();
		ExTipoDocumento exTipo = new ExTipoDocumento();
		ExModelo exMod = new ExModelo();
		
		if (exDocumentoDTO.getIdTpDoc() != null) {
			exTipo = dao().consultar(exDocumentoDTO.getIdTpDoc(), ExTipoDocumento.class, false);
		}
		
		if (exDocumentoDTO.getIdFormaDoc() != null) {
			exForma = dao().consultar(exDocumentoDTO.getIdFormaDoc(), ExFormaDocumento.class, false);
		}
		
		if (exDocumentoDTO.getIdMod() != null) {
			exMod = dao().consultar(exDocumentoDTO.getIdMod(), ExModelo.class, false);
		}
		
		if (exDocumentoDTO.getClassificacaoSel().getId() != null) {
			exClassif = dao().consultar(exDocumentoDTO.getClassificacaoSel().getId(), ExClassificacao.class, false);
		}
		
		return getNivelAcessoDefault(exTipo, exForma, exMod, exClassif);
	}
	
	public Map<Integer, String> getListaVias() {
		final Map<Integer, String> map = new TreeMap<Integer, String>();
		for (byte k = 1; k <= 20; k++) {
			final byte[] k2 = { (byte) (k + 64) };
			map.put(new Integer(k), new String(k2));
		}
		
		return map;
	}
	
	public List<ExFormaDocumento> getFormasDocumento() throws Exception {
		List<ExFormaDocumento> formasSet = dao().listarExFormasDocumento();
		ArrayList<ExFormaDocumento> formasFinal = new ArrayList<ExFormaDocumento>();
		for (ExFormaDocumento forma : formasSet) {
			if (Ex.getInstance().getConf()
					.podePorConfiguracao(getTitular(), getLotaTitular(), forma, CpTipoConfiguracao.TIPO_CONFIG_CRIAR))
				formasFinal.add(forma);
		}
		return formasFinal;
		
	}
	
	public List<ExDocumento> getListaDocsAPublicarBoletim() {
		return FuncoesEL.listaDocsAPublicarBoletim(getLotaTitular().getOrgaoUsuario());
	}
	
	public HierarquizadorBoletimInterno getHierarquizadorBIE() {
		return new HierarquizadorBoletimInterno(getLotaTitular().getOrgaoUsuario());
	}
	
	public static void redirecionarParaExibir(Result result, String sigla) {
		result.redirectTo(MessageFormat.format(URL_EXIBIR, sigla));
	}
}
