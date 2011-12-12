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
 */
package br.gov.jfrj.siga.ex;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.xerces.impl.dv.util.Base64;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Entity;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;

import br.gov.jfrj.itextpdf.Documento;
import br.gov.jfrj.lucene.HtmlBridge;
import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.base.Texto;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.util.Compactador;
import br.gov.jfrj.siga.ex.util.ProcessadorHtml;

/**
 * A class that represents a row in the 'EX_DOCUMENTO' table. This class may be
 * customized as it is never re-generated after being created.
 */
@Entity
@Indexed
public class ExDocumento extends AbstractExDocumento implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1462217739890785344L;

	private byte[] cacheConteudoBlobDoc;

	private String descrDocumentoAI;

	/**
	 * Simple constructor of ExDocumento instances.
	 */
	public ExDocumento() {
	}

	@Override
	public Long getIdDoc() {
		// TODO Auto-generated method stub
		return super.getIdDoc();
	}

	/**
	 * Retorna o documento pai, a partir do m�bil pai.
	 * @return
	 */
	public ExDocumento getPai() {
		if (getExMobilPai() == null)
			return null;
		return getExMobilPai().getExDocumento();
	}

	/**
	 * Retorna qual � o n�vel de acesso atual do documento.
	 * 
	 * @return N�vel de acesso atual do documento.
	 * 
	 */

	// @Override
	public ExNivelAcesso getExNivelAcesso() {
		ExNivelAcesso nivel = null;
		if (getMobilGeral() != null
				&& getMobilGeral().getUltimaMovimentacaoNaoCancelada() != null)
			nivel = getMobilGeral().getUltimaMovimentacaoNaoCancelada()
					.getExNivelAcesso();
		if (nivel != null)
			return nivel;
		return super.getExNivelAcesso();
	}

	/**
	 * Retorna qual � o n�vel de acesso do documento definido na cria��o do
	 * documento.
	 * 
	 * @return N�vel de acesso do documento definido na cria��o do documento.
	 * 
	 */
	public ExNivelAcesso getExNivelAcessoDoDocumento() {
		return super.getExNivelAcesso();
	}

	/**
	 * Retorna o c�digo do documento.
	 * 
	 * @return C�digo do documento.
	 * 
	 */
	public String getSigla() {
		return getCodigo();
	}

	/**
	 * Retorna lista com todos os documentos que s�o filho do documento atual.
	 * 
	 * @return Lista com todos os documentos que s�o filho do documento atual.
	 * 
	 */
	public Set<ExDocumento> getTodosDocumentosFilhosSet() {
		Set<ExDocumento> docsFilhos = new HashSet<ExDocumento>();
		for (ExMobil m : getExMobilSet()) {
			docsFilhos.addAll(m.getExDocumentoFilhoSet());
		}
		return docsFilhos;
	}

	/**
	 * Retorna o c�digo do documento.
	 * 
	 * @return C�digo do documento.
	 * 
	 */
	public String getCodigo() {
		if (getExMobilPai() != null && getNumSequencia() != null) {
			String s = getNumSequencia().toString();
			while (s.length() < 2)
				s = "0" + s;

			return getExMobilPai().getSigla() + "." + s;
		}
		if (getAnoEmissao() != null && getNumExpediente() != null) {
			String s = getNumExpediente().toString();
			while (s.length() < 5)
				s = "0" + s;

			if (getOrgaoUsuario() != null)
				return getOrgaoUsuario().getSiglaOrgaoUsu() + "-"
						+ getExFormaDocumento().getSiglaFormaDoc() + "-"
						+ getAnoEmissao() + "/" + s;
		}
		if (getIdDoc() == null)
			return "NOVO";
		return "TMP-" + getIdDoc();
	}

	/**
	 * Retorna o c�digo do documento sem "-" ou "/".
	 * 
	 * @return C�digo do documento sem "-" ou "/".
	 * 
	 */
	public String getCodigoCompacto() {
		String s = getCodigo();
		if (s == null)
			return null;
		return s.replace("-", "").replace("/", "");
	}

	/**
	 * Retorna o c�digo do documento e se este documento for do tipo externo
	 * adiciona ao c�digo do documento o c�digo do documento externo, e caso
	 * seja do tipo interno importado adiciona ao c�digo do documento o c�digo
	 * antigo do documento importado.
	 * 
	 * @return C�digo do documento mais o c�digo do documento externo ou interno
	 *         importado caso este documento seja do tipo externo ou interno
	 *         importado.
	 * 
	 */
	@Field(name = "codigo", store = Store.COMPRESS, index = Index.NO)
	public String getCodigoString() {
		String s = getCodigo();

		if (getNumExtDoc() != null)
			s += " (" + getNumExtDoc() + ")";
		if (getNumAntigoDoc() != null)
			s += " [" + getNumAntigoDoc() + "]";
		return s;
	}

	/**
	 * Retorna o conte�do do documento.
	 * 
	 * @return Conte�do do documento.
	 * 
	 */
	public String getConteudo() {
		if (getConteudoBlobDoc() != null)
			return new String(getConteudoBlobDoc2());
		return "";
	}

	public byte[] getConteudoBlob(final String nome) {
		final byte[] conteudoZip = getConteudoBlobDoc2();
		byte[] conteudo = null;
		final Compactador zip = new Compactador();
		if (conteudoZip != null) {
			conteudo = zip.descompactarStream(conteudoZip, nome);
		}
		return conteudo;
	}

	public byte[] getConteudoBlobDoc2() {

		if (cacheConteudoBlobDoc == null)
			cacheConteudoBlobDoc = br.gov.jfrj.siga.cp.util.Blob
					.toByteArray(getConteudoBlobDoc());
		return cacheConteudoBlobDoc;

	}

	public byte[] getConteudoBlobForm() {
		return getConteudoBlob("doc.form");
	}

	public byte[] getConteudoBlobResumo() {
		return getConteudoBlob("doc.resumo");
	}

	@Field(name = "conteudoBlobDocHtml", index = Index.TOKENIZED)
	@FieldBridge(impl = HtmlBridge.class)
	@Analyzer(impl = BrazilianAnalyzer.class)
	public byte[] getConteudoBlobHtml() {
		return getConteudoBlob("doc.htm");
	}

	public String getConteudoBlobHtmlB64() {
		return Base64.encode(getConteudoBlobHtml());
	}

	public String getConteudoBlobHtmlString() {
		byte[] ab = getConteudoBlobHtml();
		if (ab == null)
			return null;
		try {
			return new String(ab, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			return new String(ab);
		}
	}

	public byte[] getConteudoBlobPdf() {
		return getConteudoBlob("doc.pdf");
	}

	public String getConteudoBlobPdfB64() {
		return Base64.encode(getConteudoBlobPdf());
	}

	/**
	 * Retorna um descri��o do documento com no m�ximo 40 caracteres.
	 * 
	 * @return Descri��o do documento com no m�ximo 40 caracteres.
	 * 
	 */
	public java.lang.String getDescrCurta() {
		if (getDescrDocumento() == null)
			return "[sem descri��o]";
		if (getDescrDocumento().length() > 40)
			return getDescrDocumento().substring(0, 39) + "...";
		else
			return getDescrDocumento();
	}

	/**
	 * Retorna a descri��o completa do documento.
	 * 
	 * @return Descri��o completa do documento.
	 * 
	 */
	@Field(index = Index.TOKENIZED, name = "descrDocumento", store = Store.COMPRESS)
	@Analyzer(impl = BrazilianAnalyzer.class)
	@Override
	public String getDescrDocumento() {
		return super.getDescrDocumento();
	}

	public String getDescrDocumentoAI() {
		return descrDocumentoAI;
	}

	/**
	 * Retorna a descri��o do n�vel de acesso do documento.
	 * 
	 * @return Descri��o do n�vel de acesso do documento.
	 * 
	 */
	@Field(index = Index.TOKENIZED, name = "nivelAcesso", store = Store.COMPRESS)
	public String getNivelAcesso() {
		return getExNivelAcesso().getGrauNivelAcesso().toString();
	}

	// @Field(index = Index.TOKENIZED, name = "idOrgaoUsuario")
	/*
	 * public String getIdOrgaoUsuario() { return
	 * getOrgaoUsuario().getIdOrgaoUsu().toString(); }
	 */

	/**
	 * Retorna o nome do destinat�rio de um documento.
	 * 
	 * @return Nome do destinat�rio de um documento.
	 * 
	 */
	@Field(name = "destinatarioString", index = Index.NO, store = Store.COMPRESS)
	public String getDestinatarioString() {
		if (getDestinatario() != null)
			return getDestinatario().getDescricaoIniciaisMaiusculas();
		else if (getNmDestinatario() != null)
			return getNmDestinatario();
		else if (getLotaDestinatario() != null)
			return getLotaDestinatario().getDescricao();
		else if (getOrgaoExternoDestinatario() != null)
			if (getNmOrgaoExterno() != null && !getNmOrgaoExterno().equals(""))
				return getOrgaoExternoDestinatario().getDescricao() + ";"
						+ getNmOrgaoExterno();
			else
				return getOrgaoExternoDestinatario().getDescricao();
		else if (getNmOrgaoExterno() != null && !getNmOrgaoExterno().equals(""))
			return getNmOrgaoExterno();
		return null;
	}

	public String getDtD() {
		SimpleDateFormat df1 = new SimpleDateFormat();
		try {
			df1.applyPattern("d");
			return df1.format(getDtDoc());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Retorna a data do documento no formato dd/mm/aa, por exemplo, 01/02/10.
	 * 
	 * @return Data do documento no formato dd/mm/aa, por exemplo, 01/02/10.
	 * 
	 */
	@Field(name = "dtDocDDMMYY", index = Index.NO, store = Store.COMPRESS)
	public String getDtDocDDMMYY() {
		if (getDtDoc() != null) {
			final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
			return df.format(getDtDoc());
		}
		return "";
	}

	/**
	 * Retorna a data do documento no formato dd/mm/aaaa, por exemplo,
	 * 01/02/2010.
	 * 
	 * @return Data do documento no formato dd/mm/aaaa, por exemplo, 01/02/2010.
	 * 
	 */
	public String getDtDocDDMMYYYY() {
		if (getDtDoc() != null) {
			final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			return df.format(getDtDoc());
		}
		return "";
	}

	/**
	 * Retorna a data de fechamento do documento no formato dd/mm/aa, por
	 * exemplo, 01/02/10.
	 * 
	 * @return Data de fechamento do documento no formato dd/mm/aa.
	 */
	public String getDtFechamentoDDMMYY() {
		if (getDtFechamento() != null) {
			final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
			return df.format(getDtFechamento());
		}
		return "";
	}

	/**
	 * Retorna o nome da localidade da lota��o do documento. Se a lota��o do
	 * titular estiver preenchida, retorna o nome da lota��o do titular. Se a
	 * lota��o do subscritor estiver preenchida, retorna o nome da lota��o do
	 * subscritor. Se a lota��o do titular e do subscritor n�o estiverem
	 * preenchida, retorna o nome da lota��o do cadastrante.
	 * 
	 * @return Nome da localidade da lota��o do documento.
	 */
	public String getLocalidadeString() {
		String s = getNmLocalidade();

		DpLotacao lotaBase = null;
		if (getLotaTitular() != null)
			lotaBase = getLotaTitular();
		else if (getLotaSubscritor() != null)
			lotaBase = getLotaSubscritor();
		else if (getLotaCadastrante() != null)
			lotaBase = getLotaCadastrante();

		if (s == null && lotaBase != null) {
			s = lotaBase.getLocalidadeString();
			/*
			 * s = getLotaTitular().getOrgaoUsuario().getMunicipioOrgaoUsu() +
			 * ", ";
			 */
		}

		return s;
	}

	/**
	 * Retorna o nome da localidade da lota��o do documento em Mai�sculas. Se a
	 * lota��o do titular estiver preenchida, retorna o nome da lota��o do
	 * titular. Se a lota��o do subscritor estiver preenchida, retorna o nome da
	 * lota��o do subscritor. Se a lota��o do titular e do subscritor n�o
	 * estiverem preenchida, retorna o nome da lota��o do cadastrante.
	 * 
	 * @return Nome da localidade da lota��o do documento em Mai�sculas.
	 */
	public String getLocalidadeStringMaiusculas() {
		return getLocalidadeString().toUpperCase();
	}

	/**
	 * Retorna a data do documento por extenso. no formato "Rio de Janeiro, 01
	 * de fevereiro de 2010", por exemplo.
	 * 
	 * @return Data por extenso no formato "Rio de Janeiro, 01 de fevereiro de
	 *         2010", por exemplo.
	 */
	public String getDtExtenso() {
		// For�ando a ficar em pt_BR, antes a data aparecia na linguagem
		// definida no servidor de aplica��o (tomcat, jbos, etc.)
		SimpleDateFormat df1 = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy.",
				new Locale("pt", "BR"));
		try {
			// As linhas abaixo foram comentadas porque o formato j� est�
			// definido na declara��o da vari�vel df1.
			//
			// df1.applyPattern("dd/MM/yyyy");
			// df1.applyPattern("dd 'de' MMMM 'de' yyyy.");
			String s = getLocalidadeString();

			return s + ", " + df1.format(getDtDoc()).toLowerCase();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Retorna a data do documento por extenso no formato "01 de fevereiro de
	 * 2010", por exemplo.
	 * 
	 * @return Data por extenso no formato "01 de fevereiro de 2010", por
	 *         exemplo.
	 */
	public String getDtExtensoSemLocalidade() {
		// For�ando a ficar em pt_BR, antes a data aparecia na linguagem
		// definida no servidor de aplica��o (tomcat, jbos, etc.)

		SimpleDateFormat df1 = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy.",
				new Locale("pt", "BR"));
		try {
			// As linhas abaixo foram comentadas porque o formato j� est�
			// definido na declara��o da vari�vel df1.
			//
			// df1.applyPattern("dd/MM/yyyy");
			// df1.applyPattern("dd 'de' MMMM 'de' yyyy.");

			return df1.format(getDtDoc()).toLowerCase();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Retorna a data do documento por extenso em mai�sculas no formato "01 DE
	 * FEVEREIRO DE 2010", por exemplo.
	 * 
	 * @return Data por extenso no formato "01 DE FEVEREIRO DE 2010", por
	 *         exemplo.
	 */
	public String getDtExtensoMaiusculasSemLocalidade() {
		String data = getDtExtensoSemLocalidade();
		if (data != null)
			return data.toUpperCase();
		return "";
	}

	public String getDtMMMM() {
		SimpleDateFormat df1 = new SimpleDateFormat();
		try {
			df1.applyPattern("MMMM");
			return df1.format(getDtDoc()).toLowerCase();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Retorna a data de registro do documento no formato dd/mm/aa, por exemplo,
	 * 01/02/10.
	 * 
	 * @return Data de registro do documento no formato dd/mm/aa, por exemplo,
	 *         01/02/10.
	 */
	public String getDtRegDocDDMMYY() {
		if (getDtRegDoc() != null) {
			final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
			return df.format(getDtRegDoc());
		}
		return "";
	}

	/**
	 * Retorna a data de disponibiliza��o da movimenta��o de agendamento de
	 * publica��o no DJE.
	 * 
	 * @return Data de disponibiliza��o da movimenta��o de agendamento de
	 *         publica��o no DJE.
	 */
	public String getDtDispUltimoAgendamento() {
		Date dt = new Date();
		if (getMobilGeral().getUltimaMovimentacaoNaoCancelada() != null)
			dt = getMobilGeral().getUltimaMovimentacaoNaoCancelada()
					.getDtDispPublicacao();
		if (dt != null) {
			final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
			return df.format(dt);
		}
		return "";
	}

	/**
	 * Retorna a data da �ltima movimenta��o do Mobil Geral.
	 * 
	 * @return Data da �ltima movimenta��o do Mobil Geral.
	 */
	public String getDtUltimaRemessaParaPublicacao() {
		if (getMobilGeral().getUltimaMovimentacaoNaoCancelada() != null)
			return getMobilGeral().getUltimaMovimentacaoNaoCancelada()
					.getDtMovDDMMYY();
		return "";
	}

	/**
	 * Retorna a data de registro do documento no formato dd/mm/aa HH:mm:ss, por
	 * exemplo, 01/02/2010 16:10:00.
	 * 
	 * @return Data de registro do documento no formato dd/mm/aa HH:mm:ss, por
	 *         exemplo, 01/02/2010 16:10:00.
	 */
	public String getDtRegDocDDMMYYHHMMSS() {
		if (getDtRegDoc() != null) {
			final SimpleDateFormat df = new SimpleDateFormat(
					"dd/MM/yy HH:mm:ss");
			return df.format(getDtRegDoc());
		}
		return "";
	}

	/**
	 * Retorna o ano da data do documento no formato aaaa, por exemplo, 2010.
	 * 
	 * @return Ano da data do documento no formato aaaa, por exemplo, 2010.
	 */
	public String getDtYYYY() {
		SimpleDateFormat df1 = new SimpleDateFormat();
		try {
			df1.applyPattern("yyyy");
			return df1.format(getDtDoc());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Retorna o nome da fun��o do subscritor do documento.
	 * 
	 * @return Nome da fun��o do subscritor do documento.
	 */
	public java.lang.String getNmFuncao() {
		if (getNmFuncaoSubscritor() == null)
			return null;
		String a[] = getNmFuncaoSubscritor().split(";");
		if (a.length < 1)
			return null;
		if (a[0].length() == 0)
			return null;
		return a[0];
	}

	/**
	 * Retorna o nome da lota��o do subscritor do documento.
	 * 
	 * @return Nome da lota��o do subscritor do documento.
	 */
	public java.lang.String getNmLotacao() {
		if (getNmFuncaoSubscritor() == null)
			return null;
		String a[] = getNmFuncaoSubscritor().split(";");
		if (a.length < 2)
			return null;
		if (a[1].length() == 0)
			return null;
		return a[1];
	}

	/**
	 * Retorna o nome da localidade da lota��o do subscritor do documento.
	 * 
	 * @return Nome da localidade da lota��o do subscritor do documento.
	 */
	public java.lang.String getNmLocalidade() {
		if (getNmFuncaoSubscritor() == null)
			return null;
		String a[] = getNmFuncaoSubscritor().split(";");
		if (a.length < 3)
			return null;
		if (a[2].length() == 0)
			return null;
		return a[2];
	}

	/**
	 * Verifica se um documento pode ser indexado.
	 * 
	 * @return Nome da localidade da lota��o do subscritor do documento.
	 */
	public Boolean isIndexavel() {
		return isAssinado() && !isCancelado();
	}

	public java.lang.String getNmSubscritor() {
		if (getNmFuncaoSubscritor() == null)
			return null;
		String a[] = getNmFuncaoSubscritor().split(";");
		if (a.length < 4)
			return null;
		if (a[3].length() == 0)
			return null;
		return a[3];
	}

	/**
	 * Retorna o n�mero da �ltima via do documento.
	 * 
	 * @return N�mero da �ltima via do documento.
	 */
	public int getNumUltimaVia() {
		int maxNumVia = 0;
		for (final ExMobil mob : getExMobilSet()) {
			if (mob.isVia() && mob.getNumSequencia() > maxNumVia) {
				maxNumVia = mob.getNumSequencia();
			}
		}
		return maxNumVia;
	}

	/**
	 * Retorna o n�mero da �ltima via que ainda n�o foi cancelada.
	 * 
	 * @return O n�mero da �ltima via que ainda n�o foi cancelada.
	 */
	public int getNumUltimaViaNaoCancelada() {
		ExMobil mobUltimaVia = null;
		for (ExMobil mob : getExMobilSet()) {
			if (mob.isVia() && !mob.isCancelada()) {
				mobUltimaVia = mob;
			}
		}

		if (mobUltimaVia == null)
			return 0;

		return mobUltimaVia.getNumSequencia();
	}

	/**
	 * Retorna o set de Vias do documento de acordo com o modelo e a
	 * classifica��o do assunto. Se o o modelo possuir uma classifica��o
	 * espec�fica para a cria��o de vias esta ser� utilizada, caso contr�rio,
	 * ser� utilizada a classifica��o do assunto.
	 * 
	 * @return Set<ExVia>
	 */
	public Set<ExVia> getSetVias() {
		Set<ExVia> vias = new HashSet<ExVia>();
		HashSet<ExVia> viasFinal = new HashSet<ExVia>();
		if (getExModelo() != null
				&& getExModelo().getExClassCriacaoVia() != null) {
			vias = getExModelo().getExClassCriacaoVia().getExViaSet();
		} else {
			if (getExClassificacao() != null)
				vias = getExClassificacao().getExViaSet();
		}

		// Expediente externo ou eletr�nico e com Documento Pai tem apenas 1 via
		if (getExTipoDocumento().getIdTpDoc() == 3 || isEletronico()
				|| getExMobilPai() != null) {
			if (vias != null)
				for (ExVia via : vias) {
					if (via.getExTipoDestinacao() != null
							&& via.getExTipoDestinacao()
									.getDescrTipoDestinacao().contains(
											"ompetente"))
						viasFinal.add(via);
				}
			if (viasFinal.size() == 0)
				for (ExVia via : vias) {
					if (via.getCodVia() == null
							|| Integer.parseInt(via.getCodVia()) == 1) {
						viasFinal.add(via);
						break;
					}
				}
		} else
			return vias;

		return viasFinal;
	}

	/**
	 * Retorna o nome do subscritor.
	 * 
	 * @return Nome do subscritor.
	 */
	@Field(name = "subscritorString", index = Index.NO, store = Store.COMPRESS)
	public String getSubscritorString() {
		if (getSubscritor() != null)
			return getSubscritor().getDescricaoIniciaisMaiusculas();
		else if (getOrgaoExterno() != null || getObsOrgao() != null) {
			String str = "";
			if (getOrgaoExterno() != null)
				str = getOrgaoExterno().getDescricao();
			if (getObsOrgao() != null) {
				if (str.length() != 0)
					str = str + "; ";
				str = str + getObsOrgao();
			}
			if (getNmSubscritorExt() != null) {
				if (str.length() != 0)
					str = str + "; ";
				str = str + getNmSubscritorExt();
			}
			return str;
		} else
			return null;
	}

	/**
	 * Retorna o subscritor ou o cadastrante do documento.
	 * 
	 * @return Subscritor caso o documento possua subscritor, caso contr�rio
	 *         retorna o cadastrante do documento.
	 */
	public DpPessoa getEditor() {
		return getSubscritor() != null ? getSubscritor() : getCadastrante();
	}

	public Integer getTeste(final Integer i) {
		return i;
	}

	/**
	 * Retorna o nome do modelo do documento
	 * 
	 * @return Nome do modelo do documento.
	 */
	@Field(name = "nmMod", index = Index.TOKENIZED, store = Store.COMPRESS)
	@Analyzer(impl = BrazilianAnalyzer.class)
	public String getNmMod() {
		if (getExModelo() != null)
			return getExModelo().getNmMod();
		return null;
	}

	public boolean getViasAdicionais() {
		for (final ExVia via : getExClassificacao().getExViaSet()) {
			// String ch = via.getFgMaior();
			// if (ch.equals("S")) {
			// return true;
			// }
			final char ch = via.getFgMaior();
			if (ch == 'S')
				return true;
		}
		return false;
	}

	/**
	 * Verifica se um documento j� est� assinado.
	 * 
	 * @return Verdadeiro caso o documento j� esteja assinado e Falso caso o
	 *         documento ainda n�o esteja assinado.
	 */
	public boolean isAssinado() {
		// Interno antigo e externo s�o considerados como assinados
		if (getExTipoDocumento().getIdTpDoc() != 1L) {
			return getExMobilSet() != null && getExMobilSet().size() > 1;
		}

		ExMovimentacao mov = getMovAssinatura();
		if (mov == null)
			return false;
		return true;
	}

	/**
	 * Verifica se um documento est� cancelado.
	 * 
	 * @return Verdadeiro caso o documento esteja cancelado e Falso caso o
	 *         documento ainda esteja cancelado.
	 */
	@Override
	public boolean isCancelado() {
		// Documento s� poss�vel a via geral
		if (getExMobilSet().size() == 1)
			return false;

		for (ExMobil mob : getExMobilSet()) {
			if (mob.getExTipoMobil().getIdTipoMobil() == ExTipoMobil.TIPO_MOBIL_VIA
					|| mob.getExTipoMobil().getIdTipoMobil() == ExTipoMobil.TIPO_MOBIL_VOLUME)
				if (!mob.isCancelada())
					return false;
		}
		return true;
	}

	/**
	 * Retorna a data de assinatura do documento.
	 * 
	 * @return Data de assinatura do documento.
	 */
	public Date getDtAssinatura() {
		ExMovimentacao mov = getMovAssinatura();
		if (mov == null)
			return null;
		return mov.getDtIniMov();
	}

	/**
	 * Retorna a primeira movimenta��o de assinatura encontrada.
	 * 
	 * @return Primeira movimenta��o de assinatura encontrada caso o documento
	 *         j� esteja assinado ou null caso o documento ainda n�o esteja
	 *         assinado.
	 */
	private ExMovimentacao getMovAssinatura() {
		final Set<ExMovimentacao> movs = getMobilGeral().getExMovimentacaoSet();
		if (movs == null || movs.size() == 0)
			return null;

		for (final Object element : movs) {
			final ExMovimentacao movIterate = (ExMovimentacao) element;

			if ((movIterate.getExTipoMovimentacao().getIdTpMov() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_ASSINATURA_DIGITAL_DOCUMENTO || movIterate
					.getExTipoMovimentacao().getIdTpMov() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_REGISTRO_ASSINATURA_DOCUMENTO)
					&& movIterate.getExMovimentacaoCanceladora() == null) {
				return movIterate;
			}
		}
		return null;
	}

	/**
	 * Verifica se um documento � do tipo eletr�nico.
	 * 
	 * @return Verdadeiro caso um documento seja do tipo eletr�nico e Falso caso
	 *         contr�rio.
	 */
	public boolean isEletronico() {
		if (getFgEletronico() != null
				&& getFgEletronico().toUpperCase().equals("S"))
			return true;
		else
			return false;
	}

	/**
	 * Verifica se um documento possui agendamento de publica��o no DJE.
	 * 
	 * @return Verdadeiro caso um documento possua agendamente de publica��o no
	 *         DJE e Falso caso contr�rio.
	 */
	public boolean isPublicacaoAgendada() {
		final Set<ExMovimentacao> movs = getMobilGeral().getExMovimentacaoSet();

		for (final ExMovimentacao mov : movs) {
			if ((mov.getExTipoMovimentacao().getIdTpMov() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_AGENDAMENTO_DE_PUBLICACAO)
					&& mov.getExMovimentacaoCanceladora() == null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Verifica se um documento possui solicita��o de publica��o no DJE.
	 * 
	 * @return Verdadeiro caso um documento possua solicita��o de publica��o no
	 *         DJE e Falso caso contr�rio.
	 */
	public boolean isPublicacaoSolicitada() {
		final Set<ExMovimentacao> movs = getMobilGeral().getExMovimentacaoSet();

		for (final ExMovimentacao mov : movs) {
			if ((mov.getExTipoMovimentacao().getIdTpMov() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_PEDIDO_PUBLICACAO)
					&& mov.getExMovimentacaoCanceladora() == null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Verifica se um documento possui solicita��o de publica��o no Boletim
	 * Interno.
	 * 
	 * @return Verdadeiro caso um documento possua solicita��o de publica��o no
	 *         Boletim Interno e Falso caso contr�rio.
	 */
	public boolean isPublicacaoBoletimSolicitada() {
		final Set<ExMovimentacao> movs = getMobilGeral().getExMovimentacaoSet();

		for (final ExMovimentacao mov : movs) {
			if ((mov.getExTipoMovimentacao().getIdTpMov() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_AGENDAMENTO_DE_PUBLICACAO_BOLETIM)
					&& mov.getExMovimentacaoCanceladora() == null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Verifica se um documento do tipo Boletim Interno j� foi publicado.
	 * 
	 * @return Verdadeiro caso um documento do tipo Boletim Interno j� foi
	 *         publicado e Falso caso contr�rio.
	 */
	public boolean isBoletimPublicado() {
		final Set<ExMovimentacao> movs = getMobilGeral().getExMovimentacaoSet();

		for (final ExMovimentacao mov : movs) {
			if ((mov.getExTipoMovimentacao().getIdTpMov() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_PUBLICACAO_BOLETIM)
					&& mov.getExMovimentacaoCanceladora() == null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Verifica se uma via est� cancelada.
	 * 
	 * @param ivia
	 * 
	 * @return Verdadeiro Caso a via esteja cancelada e Falso caso contr�rio.
	 */
	public boolean isViaCancelada(Integer iVia) {
		for (ExMobil mob : getExMobilSet()) {
			if (mob.isVia() && mob.getNumSequencia().equals(iVia))
				return mob.isCancelada();
		}
		return false;
	}

	// public boolean isMovCancelada(Integer iVia) {
	// final Set<ExMovimentacao> movs = getExMovimentacaoSet();
	// ExMovimentacao mov = null;
	// for (final Object element : movs) {
	// final ExMovimentacao movIterate = (ExMovimentacao) element;
	// if ((movIterate.getExTipoMovimentacao().getIdTpMov() ==
	// ExTipoMovimentacao.TIPO_MOVIMENTACAO_CANCELAMENTO_DE_MOVIMENTACAO)
	// && (movIterate.getNumVia() != null && movIterate
	// .getNumVia().equals(iVia))) {
	// return true;
	// }
	// }
	// return false;
	// }

	/**
	 * Valida a inexistencia de uma via se:<br/>
	 * 1. Se 'iVia' for nula ou for 0, retorna falso.<br/>
	 * 2. Para toda movimenta��o do documento, verifica se existe um que esteja
	 * associada com a via 'iVia', entao retorna falso.<br/>
	 * 3. Caso aqui, retorna verdadeiro. Resumindo : A fun��o retorna a n�o
	 * existencia de uma movimenta��o com a via 'iVia' associada e a via n�o for
	 * 0.<br/>
	 * 
	 * @param iVia
	 *            O n�mero da via que deseja checar a existencia.
	 * @return Retorna a inexistencia da via 'iVia'.
	 */
	public boolean isViaInexistente(Integer iVia) {
		if (iVia == null || iVia.equals(0))
			return false;
		for (ExMobil mob : getExMobilSet()) {
			if (mob.isVia() && mob.getNumSequencia().equals(iVia))
				return false;
		}
		return true;
	}

	public void setConteudoBlob(final String nome, final byte[] conteudo) {
		final Compactador zip = new Compactador();
		final byte[] arqZip = getConteudoBlobDoc2();
		byte[] conteudoZip = null;
		if (arqZip == null || (zip.listarStream(arqZip) == null)) {
			if (conteudo != null) {
				conteudoZip = zip.compactarStream(nome, conteudo);
			} else {
				conteudoZip = null;
			}
		} else {
			if (conteudo != null) {
				conteudoZip = zip.adicionarStream(nome, conteudo, arqZip);
			} else {
				conteudoZip = zip.removerStream(nome, arqZip);
			}
		}
		setConteudoBlobDoc2(conteudoZip);
	}

	public void setConteudoBlobDoc2(byte[] blob) {
		if (blob != null)
			setConteudoBlobDoc(Hibernate.createBlob(blob));
		cacheConteudoBlobDoc = blob;
	}

	public void setConteudoBlobForm(final byte[] conteudo) {
		setConteudoBlob("doc.form", conteudo);
	}

	public void setConteudoBlobResumo(final byte[] conteudo) {
		setConteudoBlob("doc.resumo", conteudo);
	}

	public void setConteudoBlobHtml(final byte[] conteudo) {
		setConteudoBlob("doc.htm", conteudo);
	}

	public void setConteudoBlobHtmlString(final String s) throws Exception {
		final String sHtml = (new ProcessadorHtml()).canonicalizarHtml(s,
				false, true, false, false, false);
		setConteudoBlob("doc.htm", sHtml.getBytes("ISO-8859-1"));
	}

	public void setConteudoBlobPdf(final byte[] conteudo) throws Exception {
		if (isAssinado() || isAssinadoDigitalmente())
			throw new AplicacaoException(
					"O conte�do n�o pode ser alterado pois o documento j� est� assinado");
		setConteudoBlob("doc.pdf", conteudo);
	}

	public void setDescrDocumentoAI(String descrDocumentoAI) {
		this.descrDocumentoAI = descrDocumentoAI;
	}

	public void setEletronico(boolean eletronico) {
		if (eletronico)
			setFgEletronico("S");
		else
			setFgEletronico("N");
	}

	/**
	 * Retorna a via do documento.
	 * 
	 * @param numVia
	 * 
	 * @return Objeto do tipo via de acordo com o n�mero da via ou null caso n�o
	 *         exista via com o n�mero informado.
	 */
	public ExVia via(final Short numVia) {
		Short i;

		if (getSetVias() == null)
			return null;
		try {
			for (final ExVia via : getSetVias()) {
				i = Short.parseShort(via.getCodVia());
				if (numVia.equals(i))
					return via;
			}
		} catch (Exception e) {
			return null;
		}

		return null;
	}

	/**
	 * Retorna a descri��o da forma do documento. Caso o documento seja
	 * eltr�nico junto com a descri��o adiciona o texto "digital".
	 * 
	 * @return A descri��o da forma do documento.
	 */
	public String getDescrFormaDoc() {
		if (getExFormaDocumento() == null)
			return null;
		return getExFormaDocumento().getDescrFormaDoc()
				+ (isEletronico() ? " (digital)" : "");
	}

	public Map<String, String> getForm() {
		Hashtable<String, String> m = new Hashtable<String, String>();
		final byte[] form = getConteudoBlob("doc.form");
		if (form != null) {
			final String as[] = new String(form).split("&");
			for (final String s : as) {
				final String param[] = s.split("=");
				try {
					if (param.length == 2)
						m.put(param[0], URLDecoder.decode(param[1],
								"iso-8859-1"));
				} catch (final UnsupportedEncodingException e) {
				}
			}
		}
		return m;
	}

	public Map<String, String> getResumo() {
		LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();
		final byte[] resumo = getConteudoBlob("doc.resumo");
		if (resumo != null) {
			final String as[] = new String(resumo).split("&");
			for (final String s : as) {
				final String param[] = s.split("=");
				try {
					if (param.length == 2)
						m.put(URLDecoder.decode(param[0], "iso-8859-1"),
								URLDecoder.decode(param[1], "iso-8859-1"));
				} catch (final UnsupportedEncodingException e) {
				}
			}
		}
		return m;
	}

	/**
	 * Retorna o conte�do do corpo do documento que se encontra entre as tags
	 * <!-- INICIO CORPO --> e <!-- FIM CORPO -->
	 * 
	 * @return Conte�do do corpo do documento.
	 */
	public String getCorpoHtmlString() {
		try {
			String s = getConteudoBlobHtmlString();
			if (s.contains("<!-- INICIO CORPO -->")) {
				return Texto.extraiTudo(s, "<!-- INICIO CORPO -->",
						"<!-- FIM CORPO -->");
			} else {
				String inicioCorpo = "<!-- INICIO CORPO";

				String fimCorpo = "FIM CORPO -->";

				return Texto.extrai(s, inicioCorpo, fimCorpo);
			}

		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * Retorna texto da assinatura do documento que se encontra entre as tags
	 * <!-- INICIO ASSINATURA --> e <!-- FIM ASSINATURA -->.
	 * 
	 * @return Texto da assinatura do documento.
	 */
	public String getAssinaturaHtmlString() {
		try {
			String s = getConteudoBlobHtmlString();
			return Texto.extrai(s, "<!-- INICIO ASSINATURA -->",
					"<!-- FIM ASSINATURA -->");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * Retorna n�mero do documento que se encontra entre as tags <!-- INICIO
	 * NUMERO --> e <!-- FIM NUMERO -->.
	 * 
	 * @return N�mero do documento.
	 */
	public String getNumeroHtmlString() {

		try {

			String s = getConteudoBlobHtmlString();

			String inicioNumero = "<!-- INICIO NUMERO -->";

			String fimNumero = "<!-- FIM NUMERO -->";

			if (!s.contains(inicioNumero)) {

				inicioNumero = "<!-- INICIO NUMERO";

				fimNumero = "FIM NUMERO -->";

			}

			return Texto.extrai(s, inicioNumero, fimNumero);

		} catch (UnsupportedEncodingException e) {

			return null;

		}

	}

	/**
	 * Retorna texto da abertura do documento que se encontra entre as tags <!--
	 * INICIO ABERTURA --> e <!-- FIM ABERTURA -->.
	 * 
	 * @return Texto da abertura do documento.
	 */
	public String getAberturaHtmlString() {
		try {
			String s = getConteudoBlobHtmlString();

			String inicioAbertura = "<!-- INICIO ABERTURA -->";
			String fimAbertura = "<!-- FIM ABERTURA -->";

			if (!s.contains(inicioAbertura)) {
				inicioAbertura = "<!-- INICIO ABERTURA";

				fimAbertura = "FIM ABERTURA -->";
			}

			return Texto.extrai(s, inicioAbertura, fimAbertura);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * Retorna texto do fecho do documento que se encontra entre as tags <!--
	 * INICIO FECHO --> e <!-- FIM FECHO -->.
	 * 
	 * @return Texto do fecho do documento.
	 */
	public String getFechoHtmlString() {
		try {
			String s = getConteudoBlobHtmlString();
			return Texto.extrai(s, "<!-- INICIO FECHO -->",
					"<!-- FIM FECHO -->");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	// public String getNumViaDocPaiToChar() {
	// return ""
	// + Character.toChars(getExMobilPai().getNumSequencia()
	// .intValue() + 64)[0];
	// }

	@IndexedEmbedded
	public Set<ExMovimentacao> getExMovimentacaoIndexacaoSet() {
		Set<ExMovimentacao> mSet = new HashSet<ExMovimentacao>();
		for (ExMobil mob : getExMobilSet()) {
			for (ExMovimentacao m : mob.getExMovimentacaoSet()) {
				if (m.getExMovimentacaoCanceladora() == null
						&& (m.getExTipoMovimentacao().getIdTpMov() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_ANOTACAO
								|| m.getExTipoMovimentacao().getIdTpMov() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_ANEXACAO
								|| m.getExTipoMovimentacao().getIdTpMov() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_DESPACHO_TRANSFERENCIA
								|| m.getExTipoMovimentacao().getIdTpMov() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_DESPACHO || m
								.getExTipoMovimentacao().getIdTpMov() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_DESPACHO_TRANSFERENCIA_EXTERNA)) {
					mSet.add(m);
				}
			}
		}
		return mSet;
	}

	/**
	 * Retorna conjunto com todas as movimenta��es do documento.
	 * 
	 * @return Conjunto com todas as movimenta��es do documento.
	 */
	@IndexedEmbedded
	public Set<ExMovimentacao> getExMovimentacaoSet() {
		Set<ExMovimentacao> mSet = new HashSet<ExMovimentacao>();
		for (ExMobil mob : getExMobilSet()) {
			for (ExMovimentacao m : mob.getExMovimentacaoSet()) {
				mSet.add(m);
			}
		}
		return mSet;
	}

	@Override
	public String getHtml() {
		return getConteudoBlobHtmlString();
	}

	@Override
	public byte[] getPdf() {
		return getConteudoBlobPdf();
	}

	public List<ExArquivoNumerado> getArquivosNumerados(ExMobil mob) {
		List<ExArquivoNumerado> list = new ArrayList<ExArquivoNumerado>();
		return getArquivosNumerados(mob, list, 0);
	}

	public List<ExArquivoNumerado> getArquivosNumerados(ExMobil mob,
			List<ExArquivoNumerado> list, int nivel) {

		// Incluir o documento principal
		ExArquivoNumerado anDoc = new ExArquivoNumerado();
		anDoc.setArquivo(this);
		anDoc.setMobil(mob);
		anDoc.setNivel(nivel);
		list.add(anDoc);

		getAnexosNumerados(mob, list, nivel + 1);

		// Numerar as paginas
		if (isNumeracaoUnicaAutomatica()) {
			int i = 0;

			if (mob.isVolume() && mob.getNumSequencia() > 1) {
				List<ExArquivoNumerado> listVolumeAnterior = getArquivosNumerados(mob
						.doc().getVolume(mob.getNumSequencia() - 1));
				i = listVolumeAnterior.get(listVolumeAnterior.size() - 1)
						.getPaginaFinal();
			}

			for (ExArquivoNumerado an : list) {
				i++;
				an.setPaginaInicial(i);
				i += an.getNumeroDePaginasParaInsercaoEmDossie() - 1;
				an.setPaginaFinal(i);
			}
		}

		return list;
	}

	public boolean isNumeracaoUnicaAutomatica() {
		// return isEletronico() && getExFormaDocumento().isNumeracaoUnica();
		return (getExFormaDocumento().isNumeracaoUnica())
				&& (getExTipoDocumento().getId() == ExTipoDocumento.TIPO_DOCUMENTO_INTERNO)
				&& isEletronico();
		// return true;
	}

	/**
	 * A cole��o que ordena as movimenta��es deve respeitar a cronologia, exceto
	 * no caso das movimenta��es de cancelamento de juntada, anexa��o e
	 * despacho, que, quando prossuirem certid�es de exclus�o, estas dever�o ser
	 * inseridas no lugar do documento removido.
	 * 
	 * @param mob
	 * @param list
	 * @param nivel
	 */
	private void getAnexosNumerados(ExMobil mob, List<ExArquivoNumerado> list,
			int nivel) {

		SortedSet<ExMovimentacao> set = new TreeSet<ExMovimentacao>(
				new Comparator<ExMovimentacao>() {
					public int compare(ExMovimentacao o1, ExMovimentacao o2) {
						try {
							int i = o1
									.getDtIniMovParaInsercaoEmDossie()
									.compareTo(
											o2
													.getDtIniMovParaInsercaoEmDossie());
							if (i != 0)
								return i;
							i = o1.getIdMov().compareTo(o2.getIdMov());
							return i;
						} catch (final Exception ex) {
							return 0;
						}
					}
				});

		incluirArquivos(getMobilGeral(), set);
		incluirArquivos(mob, set);

		// Incluir recursivamente
		for (ExMovimentacao m : set) {
			ExArquivoNumerado an = new ExArquivoNumerado();
			an.setNivel(nivel);
			if (m.getExTipoMovimentacao().getId() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_JUNTADA) {
				an.setArquivo(m.getExDocumento());
				an.setMobil(m.getExMobil());
				an.setData(m.getData());
				list.add(an);
				m.getExDocumento().getAnexosNumerados(m.getExMobil(), list,
						nivel + 1);
			} else {
				an.setArquivo(m);
				an.setMobil(m.getExMobil());
				list.add(an);
			}
		}
	}

	private void incluirArquivos(ExMobil mob, SortedSet<ExMovimentacao> set) {
		// Incluir os documentos anexos
		for (ExMovimentacao m : mob.getExMovimentacaoSet()) {
			if (!m.isCancelada()
					&& m.getPdf() != null
					&& m.getExTipoMovimentacao().getId() != ExTipoMovimentacao.TIPO_MOVIMENTACAO_CANCELAMENTO_JUNTADA) {
				set.add(m);
			}
		}

		// Incluir os documentos juntados
		for (ExMovimentacao m : mob.getExMovimentacaoReferenciaSet()) {
			if (!m.isCancelada()) {
				if (m.getExTipoMovimentacao().getId() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_JUNTADA) {
					set.add(m);
				} else if (m.getExTipoMovimentacao().getId() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_CANCELAMENTO_JUNTADA) {
					set.remove(m.getExMovimentacaoRef());
					if (m.getPdf() != null)
						set.add(m);
				}
			}
		}
	}

	@Override
	public Date getData() {
		return getDtRegDoc();
	}

	public String getSiglaAssinatura() {
		return getIdDoc() + "-" + Math.abs(getDescrCurta().hashCode() % 10000);
	}

	/**
	 * Retorna uma lista de movimenta��es do tipo assinatura digital de
	 * documento com todas as assinaturas digitais que foram feitas em um
	 * documento.
	 * 
	 * @return Lista de movimenta��es.
	 */
	@Override
	public Set<ExMovimentacao> getAssinaturasDigitais() {
		Set<ExMovimentacao> set = new TreeSet<ExMovimentacao>();

		if (getMobilGeral() == null)
			return null;

		if (getMobilGeral().getExMovimentacaoSet() == null)
			return null;

		for (ExMovimentacao m : getMobilGeral().getExMovimentacaoSet()) {
			if (m.getExTipoMovimentacao().getIdTpMov() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_ASSINATURA_DIGITAL_DOCUMENTO
					&& m.getExMovimentacaoCanceladora() == null) {
				set.add(m);
			}
		}
		return set;
	}

	/**
	 * verifica se um documento ainda est� em rascunho.
	 * 
	 * @return Verdadeiro se um documento ainda � rascunho e falso caso
	 *         contr�rio.
	 */
	@Override
	public boolean isRascunho() {
		return getDtFechamento() == null || (isEletronico() && !isAssinado());
	}

	@Override
	public DpLotacao getLotacao() {
		return getLotaSubscritor();
	}

	/**
	 * Retorna a lota��o do titular ou do subscritor do documento.
	 * 
	 * @return Retorna a lota��o do titular caso a lota��o do titular esteja
	 *         preenchida ou em caso contr�rio a lota��o do subscritor.
	 */
	public DpLotacao getLotaSubscritorEfetiva() {
		if (getLotaTitular() != null)
			return getLotaTitular();
		return getLotaSubscritor();
	}

	/**
	 * Retorna o Mobil Geral de um documento.
	 * 
	 * @return Mobil Geral de um documento.
	 */
	public ExMobil getMobilGeral() {
		if (getExMobilSet() == null)
			return null;
		for (ExMobil mob : getExMobilSet()) {
			if (mob.getExTipoMobil().getIdTipoMobil() == ExTipoMobil.TIPO_MOBIL_GERAL)
				return mob;
		}
		return null;
	}

	/**
	 * Retorna uma lista de documentos que s�o filhos do documento atual.
	 * 
	 * @return Lista de documentos filhos do documento atual.
	 */
	public java.util.Set<ExDocumento> getExDocumentoFilhoSet() {
		Set<ExDocumento> set = new TreeSet<ExDocumento>(
				new Comparator<ExDocumento>() {
					public int compare(ExDocumento o1, ExDocumento o2) {
						if (o1.getNumSequencia() != null
								&& o2.getNumSequencia() != null)
							return o1.getNumSequencia().compareTo(
									o2.getNumSequencia());
						if (o1.getDtFechamento() != null
								&& o2.getDtFechamento() != null)
							return o1.getDtFechamento().compareTo(
									o2.getDtFechamento());
						if (o1.getDtRegDoc() != null
								&& o2.getDtRegDoc() != null)
							return o1.getDtRegDoc().compareTo(o2.getDtRegDoc());
						if (o1.getIdDoc() != null && o2.getIdDoc() != null)
							return o1.getIdDoc().compareTo(o2.getIdDoc());
						throw new Error("N�o � possivel comparar documentos.");
					}
				});
		for (ExMobil m : getExMobilSet())
			if (m.getExDocumentoFilhoSet() != null)
				set.addAll(m.getExDocumentoFilhoSet());
		return set;
	}

	@Override
	public String toString() {
		return getSigla();
	}

	/**
	 * Retorna o n�mero do �ltimo volume de um processo administrativo.
	 * 
	 * @return N�mero do �ltimo volume de um processo administrativo.
	 */
	public int getNumUltimoVolume() {
		int maxNumVolume = 0;
		for (final ExMobil mob : getExMobilSet()) {
			if (mob.isVolume() && mob.getNumSequencia() > maxNumVolume) {
				maxNumVolume = mob.getNumSequencia();
			}
		}
		return maxNumVolume;
	}

	/**
	 * Retorna o volume de um processo administrativo de acordo com o seu
	 * n�mero.
	 * 
	 * @param i
	 * 
	 * @return Volume de um processo administrativo.
	 */
	public ExMobil getVolume(int i) {
		for (final ExMobil mob : getExMobilSet()) {
			if (mob.isVolume() && mob.getNumSequencia() == i) {
				return mob;
			}
		}
		return null;
	}

	/**
	 * Verifica se um documento � do tipo Expediente.
	 * 
	 * @return Verdadeiro se um documento for do tipo Expediente e Falso se for
	 *         do tipo Processo.
	 */
	public boolean isExpediente() {
		try {
			if (getExModelo() == null)
				return false;
			if (getExModelo().getExFormaDocumento() == null)
				return true;
			return getExModelo().getExFormaDocumento().getExTipoFormaDoc()
					.isExpediente();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Verifica se um documento � do tipo Processo.
	 * 
	 * @return Verdadeiro se um documento for do tipo Processo e Falso se for do
	 *         tipo Expediente.
	 */
	public boolean isProcesso() {
		try {
			if (getExModelo() == null)
				return false;
			if (getExModelo().getExFormaDocumento() == null)
				return false;
			return getExModelo().getExFormaDocumento().getExTipoFormaDoc()
					.isProcesso();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Retorna o �ltimo volume de um processo administrativo.
	 * 
	 * @return Volume de um processo administrativo.
	 */
	public ExMobil getUltimoVolume() {
		return getVolume(getNumUltimoVolume());
	}

	/**
	 * Retorna o nome completo de um documento. � o nome composto pela descri��o
	 * e pelo c�digo do documento.
	 * 
	 * @return Nome completo de um documento.
	 */
	public String getNomeCompleto() {
		return "Documento " + getExTipoDocumento().getDescricao() + ":"
				+ getCodigoString();
	}

	/**
	 * Verifica se um documento n�o cancelado possui PDF.
	 * 
	 * @return Verdadeiro se o documento possui PDF e falso caso contr�rio.
	 */
	public boolean hasPDF() {
		for (ExMovimentacao m : getExMovimentacaoSet()) {
			if (!m.isCancelada() && m.getNumPaginas() != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retorna uma lista de Mobils do documento, ordenados de forma decrescente
	 * pelo n�mero de sequ�ncia dos Mobils.
	 * 
	 * @return Lista de Mobils
	 */
	public java.util.SortedSet<ExMobil> getExMobilSetInvertido() {
		final TreeSet<ExMobil> mobilInvertido = new TreeSet<ExMobil>(
				new TipoMobilComparatorInverso());
		mobilInvertido.addAll(getExMobilSet());

		return mobilInvertido;
	}

	private class TipoMobilComparatorInverso implements Comparator<ExMobil> {

		public int compare(ExMobil o1, ExMobil o2) {
			if (o1.getExTipoMobil().getIdTipoMobil() > o2.getExTipoMobil()
					.getIdTipoMobil())
				return -1;
			else if (o1.getExTipoMobil().getIdTipoMobil() < o2.getExTipoMobil()
					.getIdTipoMobil())
				return 1;
			else if (o1.getNumSequencia() > o2.getNumSequencia())
				return -1;
			else if (o1.getNumSequencia() < o2.getNumSequencia())
				return 1;
			else
				return 0;
		}
	}

	/**
	 * Verifica se um documento � do tipo Externo.
	 * 
	 * @return Verdadeiro caso o documento seja do tipo Externo e falso caso
	 *         contr�rio.
	 */
	public boolean isExterno() {
		if (getExTipoDocumento() == null)
			return false;
		return (getExTipoDocumento().getIdTpDoc() == 3);
	}
	
	/**
	 * Retorna o subscritor e todos os cosignat�rios
	 * 
	 * @return Lista de Pessoas.
	 */
	public List<DpPessoa> getSubscritorECosignatarios() {
		List<DpPessoa> subscritores = new ArrayList<DpPessoa>();
		
		subscritores.add(getSubscritor());
		
		for (ExMovimentacao m : getMobilGeral()
				.getExMovimentacaoSet()) {
			if (m.getExTipoMovimentacao().getIdTpMov() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_INCLUSAO_DE_COSIGNATARIO
					&& m.getExMovimentacaoCanceladora() == null) {
				subscritores.add(m.getSubscritor());
			}
		}
		
		return subscritores;
	}	
	
	/**
	 * Retorna as assinaturas Digitas e os Registros de Assinatura.
	 * 
	 * @return Lista de movimenta��es.
	 */
	public Set<ExMovimentacao> getTodasAsAssinaturas() {
		Set<ExMovimentacao> set = new TreeSet<ExMovimentacao>();

		if (getMobilGeral() == null)
			return null;

		if (getMobilGeral().getExMovimentacaoSet() == null)
			return null;

		for (ExMovimentacao m : getMobilGeral().getExMovimentacaoSet()) {
			if ((m.getExTipoMovimentacao().getIdTpMov() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_ASSINATURA_DIGITAL_DOCUMENTO
					|| m.getExTipoMovimentacao().getIdTpMov() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_REGISTRO_ASSINATURA_DOCUMENTO)
					&& m.getExMovimentacaoCanceladora() == null) {
				set.add(m);
			}
		}
		return set;
	}	
	
	/**
	 * Verifica se um documento foi assinado pelo subscritor e por todos os cosignat�rios
	 * 
	 * @return Verdadeiro se o documento foi assinado pelo subscritor e todos os cosignat�rios e falso caso contr�rio.
	*/
	public boolean isAssinadoPorTodosOsSignatarios() {
		// Interno antigo e externo s�o considerados como assinados
		if (getExTipoDocumento().getIdTpDoc() != 1L) {
			return getExMobilSet() != null && getExMobilSet().size() > 1;
		}

		ExMovimentacao mov = getMovAssinatura();
		if (mov == null)
			return false;

		String sMatricula = null;
		List<Long> matriculasAssinatura = new ArrayList<Long>();
		
		for (ExMovimentacao assinatura : getTodasAsAssinaturas()) {
			if(assinatura.getExTipoMovimentacao().getIdTpMov() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_ASSINATURA_DIGITAL_DOCUMENTO) {
				sMatricula = assinatura.getDescrMov().split(":")[1];
				matriculasAssinatura.add(Long.valueOf(sMatricula));
			} else {
				matriculasAssinatura.add(assinatura.getSubscritor().getMatricula());
			}
		}
		
		for (DpPessoa signatario : getSubscritorECosignatarios()) {
			if(!matriculasAssinatura.contains(signatario.getMatricula()))
				return false;
		}
		
		return true;
	}	
}
