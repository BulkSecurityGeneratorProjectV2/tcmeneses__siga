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
 * Criado em  23/11/2005
 *
 */
package br.gov.jfrj.siga.vraptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.view.Results;
import br.gov.jfrj.siga.acesso.ConfiguracaoAcesso;
import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.cp.CpPerfil;
import br.gov.jfrj.siga.cp.CpServico;
import br.gov.jfrj.siga.cp.CpSituacaoConfiguracao;
import br.gov.jfrj.siga.cp.CpTipoConfiguracao;
import br.gov.jfrj.siga.cp.bl.Cp;
import br.gov.jfrj.siga.dp.CpOrgaoUsuario;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.dp.dao.CpDao;
import br.gov.jfrj.siga.libs.webwork.DpLotacaoSelecao;
import br.gov.jfrj.siga.libs.webwork.DpPessoaSelecao;
import br.gov.jfrj.webwork.action.CpPerfilSelecao;

@Resource
public class AcessoController extends GiControllerSupport {

	private Long idOrgaoUsuSel;
	private String nomeOrgaoUsuSel;
	private DpPessoaSelecao pessoaSel;
	private DpLotacaoSelecao lotacaoSel;
	private CpPerfilSelecao perfilSel;

	private Long idServico;
	private Long idSituacao;
	private int idAbrangencia;
	private List<ConfiguracaoAcesso> itens;
	private String itensHTML;
	private String itemHTML;
	
	public AcessoController(HttpServletRequest request, Result result,SigaObjects so, EntityManager em) {
		
		super(request, result, CpDao.getInstance(), so, em);
		pessoaSel = new DpPessoaSelecao();
		lotacaoSel = new DpLotacaoSelecao();
		perfilSel = new CpPerfilSelecao();
	}

	@Get("/app/gi/acesso/listar")
	public void acessoconf(int idAbrangencia
						  ,DpPessoaSelecao pessoaSel
						  ,DpLotacaoSelecao lotacaoSel
						  ,CpPerfilSelecao perfilSel
						  ,Long idOrgaoUsuSel)throws Exception {
		
		this.pessoaSel = pessoaSel != null ? pessoaSel: this.pessoaSel;
		this.lotacaoSel = lotacaoSel != null ? lotacaoSel: this.lotacaoSel;
		this.perfilSel = perfilSel != null ? perfilSel: this.perfilSel;		
		
		listar(idAbrangencia, pessoaSel, lotacaoSel, perfilSel, idOrgaoUsuSel);
		
		result.include("idAbrangencia", this.idAbrangencia);
		
		result.include("perfilSel", this.perfilSel);
		result.include("pessoaSel", this.pessoaSel);
		result.include("lotacaoSel", this.lotacaoSel);

		result.include("idOrgaoUsuSel", this.idOrgaoUsuSel);
		result.include("nomeOrgaoUsuSel", this.nomeOrgaoUsuSel);
		result.include("orgaosUsu", getOrgaosUsu());			
		
		result.include("itemHTML", this.itemHTML);
		result.include("itensHTML", this.itensHTML);
		
		
		result.include("idServico", this.idServico);
		result.include("idSituacao", this.idSituacao);		
	}
	
	public void listar(int idAbrangencia
					  ,DpPessoaSelecao pessoaSel
					  ,DpLotacaoSelecao lotacaoSel
					  ,CpPerfilSelecao perfilSel
					  ,Long idOrgaoUsuSel) throws Exception {
		assertAcesso("PERMISSAO:Gerenciar permiss�es");
		if (idAbrangencia == 0) {
			this.idAbrangencia = 1;
			this.idOrgaoUsuSel = getLotaTitular().getOrgaoUsuario().getId();
		}else{
			this.idAbrangencia = idAbrangencia;
			this.idOrgaoUsuSel = idOrgaoUsuSel; 
		}

		CpPerfil perfil = this.idAbrangencia == 4 ? perfilSel.buscarObjeto(): null;
		
		DpPessoa pessoa = this.idAbrangencia == 3 ? pessoaSel.buscarObjeto(): null;
		
		DpLotacao lotacao = this.idAbrangencia == 2 ? lotacaoSel.buscarObjeto(): null;
		
		CpOrgaoUsuario orgao = this.idAbrangencia == 1 ? (this.idOrgaoUsuSel != null ? dao().consultar(this.idOrgaoUsuSel, CpOrgaoUsuario.class, false): null): null;
				
		if (orgao != null)
			this.nomeOrgaoUsuSel = orgao.getDescricao();				
				
		if (perfil != null || pessoa != null || lotacao != null || orgao != null) {
			List<CpServico> l = dao().listarServicos();

			HashMap<CpServico, ConfiguracaoAcesso> achm = new HashMap<CpServico, ConfiguracaoAcesso>();
			for (CpServico srv : l) {
				ConfiguracaoAcesso ac = ConfiguracaoAcesso.gerar(perfil, pessoa, lotacao, orgao, srv, null);

				System.out.print(ac.getSituacao().getDscSitConfiguracao());
				achm.put(ac.getServico(), ac);
			}

			SortedSet<ConfiguracaoAcesso> acs = new TreeSet<ConfiguracaoAcesso>();
			for (ConfiguracaoAcesso ac : achm.values()) {
				if (ac.getServico().getCpServicoPai() == null) {
					acs.add(ac);
				} else {
					achm.get(ac.getServico().getCpServicoPai()).getSubitens()
							.add(ac);
				}
			}

			this.itens = (new ArrayList<ConfiguracaoAcesso>(acs));
			this.itensHTML = arvoreHTML();
		}
	}
	
	@Get("/app/gi/acesso/gravar")
	public void gravar(Long idServico
			          ,Long idSituacao
			          ,DpPessoaSelecao pessoaSel
					  ,DpLotacaoSelecao lotacaoSel
					  ,CpPerfilSelecao perfilSel
					  ,Long idOrgaoUsuSel) throws Exception {
		
		assertAcesso("PERMISSAO:Gerenciar permiss�es");
		CpPerfil perfil = null;
		DpPessoa pessoa = null;
		DpLotacao lotacao = null;
		CpOrgaoUsuario orgao = null;

		if (pessoaSel != null) {
			pessoa = daoPes(pessoaSel.getId());
		} else if (lotacaoSel != null) {
			lotacao = daoLot(lotacaoSel.getId());
		} else if (perfilSel != null) {
			perfil = dao().consultar(perfilSel.getId(), CpPerfil.class,false);
		} else if (idOrgaoUsuSel != null) {
			orgao = dao().consultar(idOrgaoUsuSel,CpOrgaoUsuario.class, false);
		} else {
			throw new AplicacaoException(
					"N�o foi informada pessoa, lota��o ou �rg�o usu�rio.");
		}

		CpServico servico = dao().consultar(idServico, CpServico.class, false);
		
		CpSituacaoConfiguracao situacao = dao().consultar(idSituacao,CpSituacaoConfiguracao.class, false);
		
		CpTipoConfiguracao tpConf = dao().consultar(CpTipoConfiguracao.TIPO_CONFIG_UTILIZAR_SERVICO, CpTipoConfiguracao.class, false);
		
		Cp.getInstance().getBL().configurarAcesso(perfil, orgao, lotacao,pessoa, servico, situacao,tpConf, getIdentidadeCadastrante());
		
		ConfiguracaoAcesso ac = ConfiguracaoAcesso.gerar(perfil, pessoa,lotacao, orgao, servico,null);
		
		StringBuilder sb = new StringBuilder();
		acessoHTML(sb, ac);
		this.itemHTML = sb.toString();	
		result.use(Results.http()).body(this.itemHTML);
	}	

	private String arvoreHTML() {
		StringBuilder sb = new StringBuilder();
		acrescentarHTML(this.itens, sb);
		return sb.toString();
	}

	private void acrescentarHTML(Collection<ConfiguracaoAcesso> l,
			StringBuilder sb) {
		sb.append("<ul style=\"list-style-type:disc;\">");
		for (ConfiguracaoAcesso ac : l) {
			sb.append("<span id=\"SPAN-" + ac.getServico().getId() + "\">");
			acessoHTML(sb, ac);
			sb.append("</span>");
			if (ac.getSubitens() != null && ac.getSubitens().size() > 0)
				acrescentarHTML(ac.getSubitens(), sb);
		}
		sb.append("</ul>");
	}

	private void acessoHTML(StringBuilder sb, ConfiguracaoAcesso ac) {
		boolean fPode = ac.getSituacao().getDscSitConfiguracao().equals("Pode");
		boolean fNaoPode = ac.getSituacao().getDscSitConfiguracao().equals("N�o Pode");
		
		sb.append("<li style=\"color:"+ (fPode ? "green" : (fNaoPode ? "red" : "black")) + ";\">");
		
		if (ac.getServico().getCpServicoPai() != null
				&& ac.getServico().getSigla().startsWith(
						ac.getServico().getCpServicoPai().getSigla() + "-")) {
			sb.append(ac.getServico().getSigla().substring(
					ac.getServico().getCpServicoPai().getSigla().length() + 1));
		} else {
			sb.append(ac.getServico().getSigla());
		}
		
		sb.append(": <span style=\"\">");
		sb.append(ac.getServico().getDescricao());
		
		if (!(fPode || fNaoPode)) {
			sb.append(" (");
			sb.append(ac.getSituacao().getDscSitConfiguracao());
			sb.append(")");
		}
		
		sb.append("</span>");
		
		if (fNaoPode)
			sb.append(" - <a style=\"color:gray;\" href=\"javascript:enviar("+ ac.getServico().getId() + ",1)\">Permitir</a>");
		
		if (fPode)
			sb.append(" - <a  style=\"color:gray;\" href=\"javascript:enviar("+ ac.getServico().getId() + ",2)\">Proibir</a>");
		
		if (!ac.isDefault())
			sb.append(" - <a  style=\"color:gray;\" href=\"javascript:enviar("+ ac.getServico().getId() + ",9)\">Default</a>");
		
		if (ac.getOrgao() != null)
			sb.append(" - origem: <a  style=\"color:gray;\" href=\"?idAbrangencia=1&idOrgaoUsu="
							+ ac.getOrgao().getId()
							+ "\">"
							+ ac.getOrgao().getSigla() + "</a>");
		
		if (ac.getPerfil() != null)
			sb.append(" - origem: <a  style=\"color:gray;\" href=\"?idAbrangencia=4&perfilSel.id="
							+ ac.getPerfil().getId()
							+ "&perfilSel.descricao="
							+ ac.getPerfil().getDescricao()
							+ "&perfilSel.sigla="
							+ ac.getPerfil().getSigla()
							+ "\">" + ac.getPerfil().getSigla() + "</a>");
		
		if (ac.getLotacao() != null)
			sb.append(" - origem: <a  style=\"color:gray;\" href=\"?idAbrangencia=2&lotacaoSel.id="
							+ ac.getLotacao().getId()
							+ "&lotacaoSel.descricao="
							+ ac.getLotacao().getDescricao()
							+ "&lotacaoSel.sigla="
							+ ac.getLotacao().getSigla()
							+ "\">" + ac.getLotacao().getSigla() + "</a>");
		
		if (ac.getPessoa() != null)
			sb.append(" - origem: <a  style=\"color:gray;\" href=\"?idAbrangencia=3&pessoaSel.id="
							+ ac.getPessoa().getId()
							+ "&pessoaSel.descricao="
							+ ac.getPessoa().getDescricao()
							+ "&pessoaSel.sigla="
							+ ac.getPessoa().getSigla()
							+ "\">" + ac.getPessoa().getSigla() + "</a>");
		
		sb.append("</li>");
	}

	private void acrescentarHTMLOld(Collection<ConfiguracaoAcesso> l,StringBuilder sb) {
		sb.append("<ul style=\"list-style-type:disc;\">");
		for (ConfiguracaoAcesso ac : l) {
			sb.append("<span id=\"SPAN-" + ac.getServico().getId() + "\">");
			acessoHTML(sb, ac);
			sb.append("</span>");
			if (ac.getSubitens() != null && ac.getSubitens().size() > 0)
				acrescentarHTML(ac.getSubitens(), sb);
		}
		sb.append("</ul>");
	}

	private void acessoHTMLOld(StringBuilder sb, ConfiguracaoAcesso ac) {
		boolean fPode = ac.getSituacao().getDscSitConfiguracao().equals("Pode");
		boolean fNaoPode = ac.getSituacao().getDscSitConfiguracao().equals(
				"N�o Pode");
		sb.append("<li style=\"color:"
				+ (fPode ? "green" : (fNaoPode ? "red" : "black")) + ";\">");
		if (ac.getServico().getCpServicoPai() != null
				&& ac.getServico().getSigla().startsWith(
						ac.getServico().getCpServicoPai().getSigla() + "-")) {
			sb.append(ac.getServico().getSigla().substring(
					ac.getServico().getCpServicoPai().getSigla().length() + 1));
		} else {
			sb.append(ac.getServico().getSigla());
		}
		
		sb.append(": <span style=\"\">");
		sb.append(ac.getServico().getDescricao());
		
		if (!(fPode || fNaoPode)) {
			sb.append(" (");
			sb.append(ac.getSituacao().getDscSitConfiguracao());
			sb.append(")");
		}
		
		sb.append("</span>");
		
		if (fNaoPode)
			sb.append(" - <a style=\"color:gray;\" href=\"javascript:enviar("+ ac.getServico().getId() + ",1)\">Permitir</a>");
		
		if (fPode)
			sb.append(" - <a  style=\"color:gray;\" href=\"javascript:enviar("+ ac.getServico().getId() + ",2)\">Proibir</a>");
		
		if (!ac.isDefault())
			sb.append(" - <a  style=\"color:gray;\" href=\"javascript:enviar("+ ac.getServico().getId() + ",9)\">Default</a>");
		
		sb.append("</li>");
	}

	public List<CpOrgaoUsuario> getOrgaosUsu() throws AplicacaoException {
		return this.dao.listarOrgaosUsuarios();
	}	
	
}
