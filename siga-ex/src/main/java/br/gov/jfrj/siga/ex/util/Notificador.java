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
package br.gov.jfrj.siga.ex.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jfree.util.Log;

import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.base.Correio;
import br.gov.jfrj.siga.base.SigaBaseProperties;
import br.gov.jfrj.siga.cp.CpTipoConfiguracao;
import br.gov.jfrj.siga.dp.DpLotacao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.ex.ExConfiguracao;
import br.gov.jfrj.siga.ex.ExMovimentacao;
import br.gov.jfrj.siga.ex.ExTipoMovimentacao;
import br.gov.jfrj.siga.ex.bl.Ex;
import br.gov.jfrj.siga.hibernate.ExDao;
import br.gov.jfrj.siga.ex.ExEmailNotificacao;

public class Notificador {

	public static int TIPO_NOTIFICACAO_GRAVACAO = 1;
	public static int TIPO_NOTIFICACAO_CANCELAMENTO = 2;
	public static int TIPO_NOTIFICACAO_EXCLUSAO = 3;

	// private static String servidor = "localhost:8080"; // teste

	// private static String servidor = "siga"; // produ��o

	// private static String servidor = "http://"
	// + SigaBaseProperties.getString(SigaBaseProperties
	// .getString("ambiente") + ".servidor.principal");

	private static String servidor = SigaBaseProperties.getString("siga.ex."
					+ SigaBaseProperties.getString("ambiente") + ".url");

	/**
	 * M�todo que notifica as pessoas com perfis vinculados ao documento.
	 * 
	 * @param mov
	 * @param tipoNotificacao
	 *            - Se � uma grava��o, cancelamento ou exclus�o de movimenta��o.
	 * @throws AplicacaoException
	 */
	public static void notificarPerfisVinculados(ExMovimentacao mov,
			int tipoNotificacao) throws AplicacaoException {
		
		if(mov.getExMobil().isApensadoAVolumeDoMesmoProcesso())
			return;

		StringBuilder conteudo = new StringBuilder(); // armazena o corpo do
		// e-mail
		StringBuilder conteudoHTML = new StringBuilder(); // armazena o corpo
		// do e-mail no formato HTML

		// lista de destinat�rios com informa��es adicionais de perfil e c�digo
		// da movimenta��o que adicionou o perfil para permitir o cancelamento.
		HashSet<Destinatario> destinatarioPerfil = new HashSet<Destinatario>();

		List<Notificacao> notificacoes = new ArrayList<Notificacao>();

		/*
		 * Para cada movimenta��o do mobil geral (onde fica as movimenta��es
		 * vincula��es de perfis) verifica se: 1) A movimenta��o n�o est�
		 * cancelada; 2) Se a movimenta��o � uma vincula��o de perfil; 3) Se h�
		 * uma configura��o permitindo a notifica��o por e-mail.
		 * 
		 * Caso TODAS as condi��es acima sejam verdadeiras, adiciona o e-mail �
		 * lista de destinat�rios.
		 * Cada notifica��o representa um perfil lido no mobil geral. Uma notifica��o pode  ter um ou 
		 * v�rios emails, dependendo da classe ExEmailNotificacao.		 
		 */
		for (ExMovimentacao m : mov.getExDocumento().getMobilGeral()
				.getExMovimentacaoSet()) {
			if (!m.isCancelada()
					&& m.getExTipoMovimentacao()
							.getIdTpMov()
							.equals(ExTipoMovimentacao.TIPO_MOVIMENTACAO_VINCULACAO_PAPEL)) {

				incluirDestinatarioPerfil(mov, destinatarioPerfil, m);

			}
		}

		for (Destinatario dest : destinatarioPerfil) {
			prepararTextoPapeisVinculados(dest, conteudo, conteudoHTML, mov,
					tipoNotificacao);

			String html = conteudoHTML.toString();
			String txt = conteudo.toString();

			conteudoHTML.delete(0, conteudoHTML.length());
			conteudo.delete(0, conteudo.length());

			Notificacao not = new Notificacao(dest, html, txt, mov.getExMobil().getSigla());
			notificacoes.add(not);
		}

		notificarPorEmail(notificacoes);

	}

	/**
	 * Inclui destinat�rios na lista baseando-se nas configura��es existentes na
	 * tabela EX_CONFIGURACAO
	 * 
	 * @param mov
	 * @param destinatariosEmail
	 * @param m
	 * @throws AplicacaoException
	 */
	private static ExDao dao() {
		return ExDao.getInstance();
	}

	private static void incluirDestinatarioPerfil(ExMovimentacao mov,
			HashSet<Destinatario> destinatariosEmail, ExMovimentacao m)
			throws AplicacaoException {

		try {
			if (m.getSubscritor() != null) {
				/*
				 * Se a movimenta��o � um cancelamento de uma
				 * movimenta��o que pode ser notificada, adiciona o
				 * e-mail.
				 */
				if (mov.getExMovimentacaoRef() != null)
					adicionarDestinatariosEmail(mov.getExMovimentacaoRef(), destinatariosEmail, m, m.getSubscritor().getPessoaAtual(), null); /* verificar ExEmailNotifica��o */
				else
					adicionarDestinatariosEmail(mov, destinatariosEmail, m, m.getSubscritor().getPessoaAtual(), null); /* verificar ExEmailNotifica��o tamb�m */				
			} else {
				if (m.getLotaSubscritor() != null) {
					/*
					 * Se a movimenta��o � um cancelamento de uma
					 * movimenta��o que pode ser notificada, adiciona o
					 * e-mail.
					 */
					if (mov.getExMovimentacaoRef() != null)
						adicionarDestinatariosEmail(mov.getExMovimentacaoRef(), destinatariosEmail, m, null, m.getLotaSubscritor().getLotacaoAtual()); /* verificar ExEmailNotifica��o tmb*/
					else 
						adicionarDestinatariosEmail(mov, destinatariosEmail, m, null, m.getLotaSubscritor().getLotacaoAtual()); /* verificar ExEmailNotifica��o tmb*/
				}
			}
		} catch (Exception e) {
			throw new AplicacaoException(
					"Erro ao enviar email de notifica��o de movimenta��o.", 0,
					e);
		}
	}

	private static void adicionarDestinatariosEmail(ExMovimentacao mov,
			HashSet<Destinatario> destinatariosEmail, ExMovimentacao m, DpPessoa pess, DpLotacao lot)
			throws AplicacaoException, Exception {
		
	//	Destinatario dest = new Destinatario();				
		HashSet<String> emailsTemp = new HashSet<String>();
		String sigla;
		
		
		List<ExEmailNotificacao> listaDeEmails = dao().consultarEmailNotificacao(pess, lot);
		
		if (listaDeEmails.size() > 0) {			
			
			for (ExEmailNotificacao emailNot : listaDeEmails) {
				// Caso exista alguma configura��o com pessoaEmail, lotacaoEmail e email
				// nulos, significa que deve ser enviado para a pessoa ou para
				// todos da lota��o				

				if (emailNot.getPessoaEmail() == null  // configura��o default
						&& emailNot.getPessoaEmail() == null
						&& emailNot.getEmail() == null) {
					if (emailNot.getDpPessoa() != null){
						if (Ex.getInstance()
								.getConf()
								.podePorConfiguracao(
								mov.getExDocumento()
								.getExFormaDocumento()
								.getExTipoFormaDoc(),
								m.getExPapel(),
								emailNot.getDpPessoa(),
								mov.getExTipoMovimentacao(),
								CpTipoConfiguracao.TIPO_CONFIG_NOTIFICAR_POR_EMAIL))	
							emailsTemp.add(emailNot.getDpPessoa().getEmailPessoaAtual());								
					} else {
						for (DpPessoa pes : dao().pessoasPorLotacao(emailNot.getDpLotacao().getId(), false, true)) {
							if (Ex.getInstance()
									.getConf()
									.podePorConfiguracao(
									mov.getExDocumento()
									.getExFormaDocumento()
									.getExTipoFormaDoc(),
									m.getExPapel(),
									pes,
									mov.getExTipoMovimentacao(),
									CpTipoConfiguracao.TIPO_CONFIG_NOTIFICAR_POR_EMAIL))
								
								emailsTemp.add(pes.getEmailPessoaAtual());						
						}						
					}					
				} else {				
						if(emailNot.getPessoaEmail() != null){ // Mandar para pessoa
							if (Ex.getInstance()
									.getConf()
									.podePorConfiguracao(
									mov.getExDocumento()
									.getExFormaDocumento()
									.getExTipoFormaDoc(),
									m.getExPapel(),
									emailNot.getPessoaEmail(),
									mov.getExTipoMovimentacao(),
									CpTipoConfiguracao.TIPO_CONFIG_NOTIFICAR_POR_EMAIL))
								emailsTemp.add(emailNot.getPessoaEmail().getEmailPessoaAtual());								
						} else {
							if (emailNot.getLotacaoEmail() != null) {
								for (DpPessoa pes : dao().pessoasPorLotacao(emailNot.getLotacaoEmail().getId(), false, true)) {
									if (Ex.getInstance()
											.getConf()
											.podePorConfiguracao(
											mov.getExDocumento()
											.getExFormaDocumento()
											.getExTipoFormaDoc(),
											m.getExPapel(),
											pes,
											mov.getExTipoMovimentacao(),
											CpTipoConfiguracao.TIPO_CONFIG_NOTIFICAR_POR_EMAIL))
										emailsTemp.add(pes.getEmailPessoaAtual());									
								}
							} else {
								emailsTemp.add(emailNot.getEmail());								
							 }
						}						
					}
				}	
		} else { /* n�o h� ocorrencias em Ex_email_notificacao */
			if (pess != null){
				if (Ex.getInstance()
						.getConf()
						.podePorConfiguracao(
						mov.getExDocumento()
						.getExFormaDocumento()
						.getExTipoFormaDoc(),
						m.getExPapel(),
						pess,
						mov.getExTipoMovimentacao(),
						CpTipoConfiguracao.TIPO_CONFIG_NOTIFICAR_POR_EMAIL))
					emailsTemp.add(pess.getEmailPessoaAtual());			
			} else {
				for (DpPessoa pes : dao().pessoasPorLotacao(lot.getId(), false, true)) {
					if (Ex.getInstance()
							.getConf()
							.podePorConfiguracao(
							mov.getExDocumento()
							.getExFormaDocumento()
							.getExTipoFormaDoc(),
							m.getExPapel(),
							pes,
							mov.getExTipoMovimentacao(),
							CpTipoConfiguracao.TIPO_CONFIG_NOTIFICAR_POR_EMAIL))
						emailsTemp.add(pes.getEmailPessoaAtual());					
				}				
			}
		}	
		if (pess != null)
			sigla = pess.getPessoaAtual().getSigla();
		else
			sigla = lot.getLotacaoAtual().getSigla();
		
		if (!emailsTemp.isEmpty())	
			destinatariosEmail.add(new Destinatario(sigla, m.getExPapel().getDescPapel(),
					m.getIdMov(), emailsTemp));	
	}
	

	/**
	 * Notifica os destinat�rios assincronamente.
	 * 
	 * @param conteudo
	 *            - texto do e-mail
	 * @param conteudoHTML
	 *            - texto do e-mail no formato HTML
	 * @param destinatariosEmail
	 *            - Conjunto de destinat�rios.
	 */
	private static void notificarPorEmail(List<Notificacao> notificacoes) {
		// Se existirem destinat�rios, envia o e-mail. O e-mail � enviado
		// assincronamente.
		if (notificacoes.size() > 0) {
			CorreioThread t = new CorreioThread(notificacoes);
			t.start();
		}
	}

	/**
	 * Monta o corpo do e-mail que ser� recebido pelas pessoas com perfis
	 * vinculados.
	 * 
	 * @param dest
	 * 
	 * @param conteudo
	 * @param conteudoHTML
	 * @param mov
	 * @param tipoNotificacao
	 *            - Se � uma grava��o, cancelamento ou exclus�o de movimenta��o.
	 */
	private static void prepararTextoPapeisVinculados(Destinatario dest,
			StringBuilder conteudo, StringBuilder conteudoHTML,
			ExMovimentacao mov, int tipoNotificacao) {

		// conte�do texto
		conteudo.append("Documento: ");
		conteudo.append(mov.getExMobil().getSigla());
		conteudo.append("\nDescri��o:");
		conteudo.append(mov.getExDocumento().getDescrDocumento());
		conteudo.append("\nMovimenta��o:");
		if (tipoNotificacao == TIPO_NOTIFICACAO_GRAVACAO
				|| tipoNotificacao == TIPO_NOTIFICACAO_EXCLUSAO) {
			conteudo.append(mov.getExTipoMovimentacao().getDescricao());
		}
		if (tipoNotificacao == TIPO_NOTIFICACAO_CANCELAMENTO) {
			conteudo.append(mov.getExMovimentacaoRef().getDescrTipoMovimentacao()+"(Cancelamento)");
		}
		if (mov.getCadastrante() != null) {
			conteudo.append("\nRealizada por '"
					+ mov.getCadastrante().getNomePessoa() + " (Matr�cula: "
					+ mov.getCadastrante().getMatricula() + ")'.\n\n");

		}
		
		conteudo.append("\n\nEste email foi enviado porque ");
		conteudo.append(dest.sigla);
		conteudo.append(" tem o perfil de '");
		conteudo.append(dest.papel);
		conteudo.append("' no documento ");
		conteudo.append(mov.getExDocumento().getSigla());
		conteudo.append(". Caso n�o deseje mais receber notifica��es desse documento, clique no link abaixo para se descadastrar:\n\n");
		conteudo.append(servidor + "/expediente/mov/cancelar.action?id=");
		conteudo.append(dest.idMovPapel);
		
		
		// conte�do html
		conteudoHTML.append("<html><body>");
		
		conteudoHTML.append("Documento: <b>");
		conteudoHTML.append(mov.getExMobil().getSigla()+ "</b><br>");
		conteudoHTML.append("Descri��o: <b>");
		conteudoHTML.append(mov.getExDocumento().getDescrDocumento()+"</b><br>");
		conteudoHTML.append("Movimenta��o: <b>");		

		if (tipoNotificacao == TIPO_NOTIFICACAO_GRAVACAO
				|| tipoNotificacao == TIPO_NOTIFICACAO_EXCLUSAO) {
			conteudoHTML.append(mov.getExTipoMovimentacao().getDescricao() + "</b><br>");
		}
		if (tipoNotificacao == TIPO_NOTIFICACAO_CANCELAMENTO) {
			conteudoHTML.append(mov.getExMovimentacaoRef().getDescrTipoMovimentacao()
					+ "(Cancelamento)</b><br>.");
		}
		if (mov.getCadastrante() != null) {
			conteudoHTML.append("<br/>Realizada por <b>"
					+ mov.getCadastrante().getNomePessoa() + " (Matr�cula: "
					+ mov.getCadastrante().getMatricula() + ")</b><br>");
		}

		if (mov.getExTipoMovimentacao().getIdTpMov() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_TRANSFERENCIA
				|| mov.getExTipoMovimentacao().getIdTpMov() == ExTipoMovimentacao.TIPO_MOVIMENTACAO_DESPACHO_TRANSFERENCIA) {
			if (mov.getResp() != null) {
				conteudoHTML
						.append("Destinat�rio:  <b>"
								+ mov.getResp().getNomePessoa()
								+ " (Matr�cula: "
								+ mov.getResp().getMatricula() + ") do (a) "
								+ mov.getLotaResp().getNomeLotacao()
								+ " (sigla: " + mov.getLotaResp().getSigla()
								+ ")" + "</b>");
			} else {
				conteudoHTML
						.append("Lota��o Destinat�ria<b>"
								+ mov.getLotaResp().getNomeLotacao()
								+ " (sigla: "
								+ mov.getLotaResp().getSigla()
								+ ")" + "</b>");
			}
		} 

		conteudoHTML.append("<p>Para visualizar o documento, ");
		conteudoHTML.append("clique <a href=\"");
		conteudoHTML.append(servidor
				+ "/expediente/doc/exibir.action?sigla=");
		conteudoHTML.append(mov.getExDocumento().getSigla());
		conteudoHTML.append("\">aqui</a>.</p>");

		conteudoHTML.append("<p>Este email foi enviado porque ");
		conteudoHTML.append(dest.sigla);
		conteudoHTML.append(" tem o perfil de '");
		conteudoHTML.append(dest.papel);
		conteudoHTML.append("' no documento ");
		conteudoHTML.append(mov.getExDocumento().getSigla());
		conteudoHTML
				.append(". Caso n�o deseje mais receber notifica��es desse documento, clique <a href=\"");
		conteudoHTML.append(servidor
				+ "/expediente/mov/cancelar.action?id=");
		conteudoHTML.append(dest.idMovPapel);
		conteudoHTML.append("\">aqui</a> para descadastrar.</p>");

		conteudoHTML.append("</body></html>");

	}

	/**
	 * Classe que representa um thread de envio de e-mail. H� a necessidade do
	 * envio de e-mail ser ass�ncrono, caso contr�rio, o usu�rio sentir� uma
	 * degrada��o de performance.
	 * 
	 * @author kpf
	 * 
	 */
	static class CorreioThread extends Thread {

		private List<Notificacao> notificacoes;

		public CorreioThread(List<Notificacao> notificacoes) {
			super();
			this.notificacoes = notificacoes;
		}

		@Override
		public void run() {			
			String txt;
			String html;			
			try {
				for (Notificacao not : notificacoes){
					txt = not.txt;
					html = not.html;					
					
				/*	for (String email : not.dest.emails) {
						dests[cont] = email;
						cont++;						
					} */
					Correio.enviar(SigaBaseProperties
								.getString("servidor.smtp.usuario.remetente"),
								not.dest.emails.toArray(new String[not.dest.emails.size()]),		
								"Notifica��o Autom�tica -"+not.doc+ "- Movimenta��o de Documento",
								txt, html);					
				}
					
			} catch (Exception e) {
				Log.error(e);
			}
		}
	}

	
	static class Notificacao {
		Destinatario dest;
		String html;
		String txt;
		String doc;

		public Notificacao(Destinatario dest, String html, String txt, String doc) {
			super();
			this.dest = dest;
			this.html = html;
			this.txt = txt;
			this.doc = doc;
		}
	}

	static class Destinatario implements Comparable<Destinatario> {

		public String sigla;
		public String papel;
		public long idMovPapel;
		public HashSet<String> emails = new HashSet<String>();
		
		public Destinatario(String sigla, String papel,
				long idMovPapel, HashSet<String> emails ) {
			super();			
			this.sigla = sigla;
			this.papel = papel;
			this.idMovPapel = idMovPapel;
			this.emails = emails;
		}

		public int compareTo(Destinatario o) {
			// TODO Auto-generated method stub
//			return email.compareTo(o.email);
			return 0; /* RETIRAR */
		}

	}

}
