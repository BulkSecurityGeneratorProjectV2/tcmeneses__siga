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
package br.gov.jfrj.siga.model.prop;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Superclasse abstrata respons�vel pela infraestrutura de tratamento de
 * propriedades Toda classe que tratar propriedades est�ticas deve subclasse�-la
 * e criar um arquivo de propriedades chamado 'app.properties' no mesmo pacote
 * que ela.
 * 
 * @author aym
 * 
 */
public abstract class ModeloPropriedade {
	private Properties propriedades = null;
	protected String[] prefixo;

	/**
	 * Carrega as propriedades
	 * 
	 * @throws Exception
	 */
	private void carregarPropriedades() throws Exception {
		if (propriedades == null) {
			propriedades = CarregadorDeModeloPropriedade.obterTodas(this);
		}
	}

	/**
	 * atribui o prefixo para as propriedades. Note que se um prefixo for, por
	 * exemplo, "a.b.c", ent�o as combina��es de busca nos arquivos de
	 * propriedades ser�o a.b.c.[propriedade], b.c.[propriedade],
	 * c.[propriedade] e [propriedade]
	 * 
	 * @param prefstr
	 *            a string de par�metros contendo os prefixos separados por
	 *            pontos
	 */
	public void setPrefixo(String prefstr) {
		prefixo = prefstr.split("\\.");
	}

	public String getPrefixo() {
		String pars = new String();
		for (int i = 0; i < prefixo.length; i++) {
			if (i == prefixo.length - 1) {
				pars += prefixo[i];
			} else {
				pars += prefixo[i] + ".";
			}
		}
		return pars;
	}

	/**
	 * Obt�m a propriedade referente ao nome (par�metro) ou null se n�o existir.
	 * obs-1: Note que o par�metro tem heran�a e � procurado da subclasse para
	 * as superclasses, assim com um objeto. obs-2: Note tamb�m que se
	 * setPrefixo(prefixo) for executado, a procura do par�metro ser� feita pela
	 * combina��o dos prefixos gerados por 'prefixo'. Ex: se for executado
	 * setPrefixo("a.b.c"), ent�o quando executado o m�todo
	 * obterPropriedade("um.dois.tres") ser� procurada propriedade na seguinte
	 * ordem ....: 1) a.b.c.um.dois.tres 2) b.c.um.dois.tres 3) c.um.dois.tres
	 * 4) um.dois.tres
	 * 
	 * @param nome
	 *            nome da propriedade a carregar.
	 * @return propriedade referente ao nome (par�metro) ou null.
	 * @throws Exception
	 */
	public String obterPropriedade(String nome) throws Exception {
		carregarPropriedades();
		if (prefixo != null) {
			for (int i = 0; i < prefixo.length; i++) {
				String prop = new String();
				for (int j = i; j < prefixo.length; j++) {
					prop += prefixo[j] + ".";
				}
				String value = propriedades.getProperty(prop + nome);
				if (value != null)
					return value;
			}
		}
		return propriedades.getProperty(nome);
	}

	/**
	 * obt�m uma lista de propriedades que come�am com um nome (par�metro) que �
	 * separado por um ponto e seguido de um numero sequencial. Ex: par�metro
	 * nome -> uf no arquivo : uf.0=AC uf.1=AL uf.2=AM ... uf.26=TO
	 * 
	 * Obs: Note que os par�metros em lista tamb�m t�m heran�a, da mesma forma
	 * que os par�metros simples
	 * 
	 * @param nome
	 *            nome do par�metro a obter. Se atribu�do o prefixo, o mesmo n�o
	 *            deve estar contido.
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> obterPropriedadeLista(String nome)
			throws Exception {
		ArrayList<String> lista = new ArrayList<String>();
		for (int i = 0; true; i++) {
			String prp = obterPropriedade(nome + "." + String.valueOf(i));
			if (prp == null)
				break;
			lista.add(prp);
		}
		return lista;
	}
}
