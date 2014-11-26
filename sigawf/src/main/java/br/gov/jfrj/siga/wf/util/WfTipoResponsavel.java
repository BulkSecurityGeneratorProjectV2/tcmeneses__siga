package br.gov.jfrj.siga.wf.util;

/**
 * Classe que representa o tipo do respons�vel designado.
 * 
 * @author kpf
 * 
 */
public class WfTipoResponsavel {
	private int id;
	/**
	 * Texto amig�vel que representa o tipo de respons�vel.
	 */
	private String texto;

	/**
	 * Texto da express�o ou identificador do tipo de respons�vel.
	 */
	private String valor;

	/**
	 * Construtor da classe TipoRespons�vel.
	 * 
	 * @param id
	 * @param texto
	 * @param valor
	 */
	public WfTipoResponsavel(int id, String texto, String valor) {
		this.setId(id);
		this.setTexto(texto);
		this.setValor(valor);
	}

	/**
	 * Retorna o id do tipo de respons�vel.
	 * 
	 * @return
	 */
	public int getId() {
		return id;
	}

	/**
	 * Retorna o texto amig�vel do Tipo de respons�vel
	 * 
	 * @return
	 */
	public String getTexto() {
		return texto;
	}

	/**
	 * Retorna o valor do tipo de respons�vel, por exemplo, a express�o
	 * associada ao tipo de respons�vel.
	 * 
	 * @return
	 */
	public String getValor() {
		return valor;
	}

	/**
	 * Define o id do tipo de respons�vel.
	 * 
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Define o texto amig�vel do tipo de respons�vel.
	 * 
	 * @param texto
	 */
	public void setTexto(String texto) {
		this.texto = texto;
	}

	/**
	 * Define o valor (por exemplo, a express�o) do tipo de respons�vel.
	 * 
	 * @param valor
	 */
	public void setValor(String valor) {
		this.valor = valor;
	}

	/**
	 * Retorna o texto amig�vel do tipo de respons�vel.
	 */
	public String toString() {
		return this.getId() + ")" + this.getTexto();
	}
}
