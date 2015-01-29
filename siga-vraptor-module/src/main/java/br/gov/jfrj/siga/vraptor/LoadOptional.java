package br.gov.jfrj.siga.vraptor;

/**
 * Melhoria feita sobre a annotation @Load, para permitir contornar o 
 * Result.nothing() gerado quando uma entidade n�o � carregada pelo 
 * EntityManager.
 * 
 * @author Carlos Alberto Junior Spohr Poletto (carlosjrcabello@gmail.com)
 */
@java.lang.annotation.Target(value={java.lang.annotation.ElementType.PARAMETER})
@java.lang.annotation.Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface LoadOptional
{
	/**
	 * Se marcado como true, o objeto � obrigat�rio, n�o encontrando ele ir�
	 * redirecionar a p�gina para uma determinada l�gica. Caso ela n�o seja
	 * informada, ser� retornado Result.nothing().
	 * 
	 * @return
	 */
	boolean required() default false;
	
	/**
	 * O caminho da l�gica a redirecionar a aplica��o. Exemplo:
	 * &#47;cadastros&#47;usuarios&#47;listagem&#47;
	 * @return
	 */
	String redirectToWhenObjectNotFound() default "";
}