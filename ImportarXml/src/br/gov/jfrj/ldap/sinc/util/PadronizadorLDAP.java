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
package br.gov.jfrj.ldap.sinc.util;

import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.hibernate.cfg.AnnotationConfiguration;

import br.gov.jfrj.importar.AdObjeto;
import br.gov.jfrj.importar.AdUsuario;
import br.gov.jfrj.ldap.sinc.LdapDaoSinc;
import br.gov.jfrj.ldap.sinc.SincProperties;
import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.dp.dao.CpDao;
import br.gov.jfrj.siga.model.dao.HibernateUtil;

/**
 * Classe respons�vel por padronizar a �rvore do AD com o Oracle.
 * 
 * @author kpf
 * 
 */
public class PadronizadorLDAP {

	private static LdapDaoSinc ldap;
	private static SincProperties conf;

	private static Logger log = Logger.getLogger(PadronizadorLDAP.class
			.getName());
	private static Level nivelLog = Level.FINE;

	/**
	 * 
	 * @param args
	 *            - primeiro par�metro (ambiente) : -homolo -desenv -prod
	 *            segundo par�metro (localidade): -sjrj -trf2 -sjes
	 * @throws AplicacaoException
	 */
	public static void main(String args[]) throws AplicacaoException {
		if (args.length < 2) {
			System.err.println("N�mero de Parametros inv�lidos");
			System.err
					.println("PadronizadorLDAP.main ([-sjrj -trf2 -sjes] [-desenv -homolo -prod]");
			return;
		}
		if (!args[0].equals("-sjrj") && !args[0].equals("-trf2")
				&& !args[0].equals("-sjes")) {
			System.err.println("Localidade + " + args[0]
					+ " +inv�lida! Defina -sjrj ou -trf2 ou -sjes");
			return;
		}
		if (!args[1].equals("-desenv") && !args[1].equals("-homolo")
				&& !args[1].equals("-prod")) {
			System.err.println("Ambiente " + args[1]
					+ " inv�lido! Defina -desenv ou -homolo ou -prod");
			return;
		}

		conf = SincProperties.getInstancia(args[0].replace("-", "") + "."
				+ args[1].replace("-", ""));
		definirNivelLog();
		ldap = LdapDaoSinc.getInstance(conf);
		if (ldap == null) {
			throw new AplicacaoException(
					"N�o foi poss�vel carregar a inst�ncia do LDAP!");
		}

		System.out.println("\n\nIniciando padroniza��o...\n\n");
		iniciarPadronizacao();
		System.out.println("\n\nPadroniza��o conclu�da!\n\n");
	}

	private static void definirNivelLog() {
		for (int i = 0; i < log.getParent().getHandlers().length; i++) {
			if (log.getParent().getHandlers()[i] instanceof ConsoleHandler) {
				log.getParent().getHandlers()[i].setLevel(nivelLog);
			}
		}
		log.setLevel(nivelLog);
	}

	/**
	 * 
	 * 
	 * @param lLDAP
	 * @throws AplicacaoException
	 */
	public static void iniciarPadronizacao() throws AplicacaoException {
		AnnotationConfiguration cfg;
		try {
			cfg = CpDao.criarHibernateCfg(conf.getBdStringConexao(), conf
					.getBdUsuario(), conf.getBdSenha());
		} catch (Exception e) {
			e.printStackTrace();
			throw new AplicacaoException(
					"N�o foi poss�vel configurar o hibernate!");
		}
		HibernateUtil.configurarHibernate(cfg, "");

		List<AdObjeto> lista;
		try {
			lista = ldap.pesquisarObjetoSemIgnorarNada(conf
					.getDnGestaoIdentidade());
		} catch (NamingException e) {
			e.printStackTrace();
			throw new AplicacaoException(
					"N�o foi poss�vel ler a lista de usu�rios do LDAP no DN "
							+ conf.getDnGestaoIdentidade());
		}
		// nomes de usu�rios com iniciais mai�sculas e sem acentos
		for (AdObjeto o : lista) {
			if (o instanceof AdUsuario) {

				if (!conf.getPadronizadorIgnoraUsuariosConvertidos()) {
					padronizarUsuario(o);
				} else {
					if (!o.getNome().startsWith(conf.getPrefixoMatricula())) {
						padronizarUsuario(o);
					}
				}
			}
		}
	}

	/**
	 * Executa a padroniza��o dos usu�rios propriamente dita. <br/>
	 * 1) Consulta os usu�rios do CORPORATIVO com a sigla do objeto a ser
	 * padronizado. <br/>
	 * 2) Se a pessoa n�o for encontrada no banco, ignora e coloca no log. <br/>
	 * 3) Se a pessoa for encontrada <br/>
	 * 3.1) o displayName do objeto AD � definido na coluna NOME_EXIBICAO do
	 * CORPORATIVO <br/>
	 * 3.2) o cn do objeto AD � convertido para RJXXXXX (ou ES, T2) <br/>
	 * 4) Se houver mais de uma pessoa com a mesma sigla, coloca no log.
	 * 
	 * <br/>
	 * Obs: Somente ser�o padronizados os usu�rios que estiverem abaixo
	 * (incluindo ou filhas) do container raiz da Gest�o de Identidade.
	 * 
	 * @param o
	 *            - objeto a ser padronizado.
	 * @throws AplicacaoException
	 */
	private static void padronizarUsuario(AdObjeto o) throws AplicacaoException {
		AdUsuario u = (AdUsuario) o;

		List<DpPessoa> listaPessoa = consultarPessoaAtiva(u);

		if (listaPessoa.size() == 0) {
			verificarPessoasInativas(u, listaPessoa);
		}

		if (listaPessoa.size() == 1) {
			padronizar(u, listaPessoa);
		}

		if (listaPessoa.size() > 1) {
			log.warning("A sigla " + u.getSigla()
					+ " possui mais de um usu�rio!");
		}

	}

	/**
	 * Consulta a pessoa ativa que corresponde ao objeto no AD
	 * 
	 * @param u
	 * @return
	 */
	private static List<DpPessoa> consultarPessoaAtiva(AdUsuario u) {
		return HibernateUtil.getSessao().createQuery(
				"from DpPessoa p where data_fim_pessoa is null and ID_ORGAO_USU = "
						+ conf.getIdLocalidade() + " AND sigla_pessoa like '"
						+ u.getSigla().toUpperCase() + "'").list();
	}

	private static void padronizar(AdUsuario u, List<DpPessoa> listaPessoa)
			throws AplicacaoException {
		CpDao dao = CpDao.getInstance();
		DpPessoa pessoa = listaPessoa.get(0);
		if (conf.isModoEscrita()) {
			dao.iniciarTransacao();
			pessoa.setNomeExibicao(u.getNomeExibicao());
			try {
				dao.commitTransacao();
			} catch (AplicacaoException e1) {
				e1.printStackTrace();
				throw new AplicacaoException(
						"N�o foi poss�vel definir a coluna NOME_EXIBICAO com o valor do displayName do LDAP para o objeto "
								+ u.getNomeCompleto());
			}

		}
		String nomeCompletoAntigo = u.getNomeCompleto();
		u
				.setNome(conf.getPrefixoMatricula()
						+ pessoa.getMatricula().toString());
		try {
			ldap.moverObjeto(nomeCompletoAntigo, u.getNomeCompleto());
		} catch (NamingException e) {
			e.printStackTrace();
			throw new AplicacaoException("N�o foi poss�vel converter "
					+ nomeCompletoAntigo + " para " + u.getNomeCompleto());
		}
		log.finer(nomeCompletoAntigo.split(",")[0] + "\talterado para:\t"
				+ u.getNomeCompleto().split(",")[0]);
	}

	/**
	 * Verifica se o objeto AD � uma pessoa inativa. Se for, inclui a pessoa
	 * para padroniza��o.
	 * 
	 * @param u
	 * @param listaPessoa
	 */
	private static void verificarPessoasInativas(AdUsuario u,
			List<DpPessoa> listaPessoa) {
		List<DpPessoa> pessoasInativas = consultarPessoaInativa(u);
		if (pessoasInativas.size() > 0) {
			listaPessoa.add(pessoasInativas.get(pessoasInativas.size() - 1));
			log
					.info("SIGLA_PESSOA "
							+ u.getSigla().toUpperCase()
							+ " INATIVA no CORPORATIVO (o usu�rio ser� padronizado)!\tAD: "
							+ u.getNomeCompleto());
		} else {
			log.warning("SIGLA_PESSOA " + u.getSigla().toUpperCase()
					+ " n�o encontrada no CORPORATIVO!\tAD: "
					+ u.getNomeCompleto());
		}
	}

	/**
	 * Consulta a pessoa inativa que corresponde ao objeto no AD.
	 * 
	 * @param u
	 * @return
	 */
	private static List<DpPessoa> consultarPessoaInativa(AdUsuario u) {
		return HibernateUtil.getSessao().createQuery(
				"from DpPessoa p where ID_ORGAO_USU = "
						+ conf.getIdLocalidade() + " AND sigla_pessoa like '"
						+ u.getSigla().toUpperCase() + "' ORDER BY ID_PESSOA")
				.list();
	}

}
