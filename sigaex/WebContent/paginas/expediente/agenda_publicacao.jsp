<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	buffer="64kb"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="ww" uri="/webwork"%>
<%@ taglib uri="http://fckeditor.net/tags-fckeditor" prefix="FCK"%>
<%--<%@ taglib prefix="tags" tagdir="/WEB-INF/tags"%>--%>
<%@ taglib uri="http://localhost/customtag" prefix="tags"%>
<%@ taglib uri="http://localhost/sigatags" prefix="siga"%>

<siga:pagina titulo="Movimenta��o">

<ww:url id="url" action="prever_data" namespace="/expediente/mov">
</ww:url>
<script type="text/javascript">
	function prever_data() {
		var dtPublDiv = document.getElementById('dt_publ');
		ReplaceInnerHTMLFromAjaxResponse('<ww:property value="%{url}"/>'+'?data='+document.getElementById('dt_dispon').value, null, dtPublDiv);
	}
	
	function sbmt() {
		ExMovimentacaoForm.page.value='';
		ExMovimentacaoForm.acao.value='aAgendarPublicacao';
		ExMovimentacaoForm.submit();
}
</script>

<!-- A linha abaixo � tempor�ria, pois est� presente num dos cabe�alhos  -->
<div id="carregando" style="position:absolute;top:0px;right:0px;background-color:red;font-weight:bold;padding:4px;color:white;display:none">Carregando...</div>

<table width="100%">
	<tr>
		<td><form action="agendar_publicacao_gravar.action"
			namespace="/expediente/mov" cssClass="form" method="GET">
			<ww:token/>
			<input type="hidden" name="postback" value="1" />
			<ww:hidden name="sigla" value="%{sigla}"/>

			<h1>Agendamento de Publica��o - ${doc.codigo} <!--<c:if
				test="${numVia != null && numVia != 0}">
			- ${numVia}&ordf; Via
			</c:if>--></h1>
			<table class="form">
				<tr class="header">
					<td colspan="2">Dados do Agendamento</td>
				</tr>
				<c:choose>
					<c:when test="${cadernoDJEObrigatorio}">
						<c:set var="disabledTpMat">true</c:set> 
						<input type="hidden" name="tipoMateria" value="${tipoMateria}" />
						<tr>
							<td>Tipo de Mat�ria:</td>
							<td>
								<c:choose>
									<c:when test="${tipoMateria eq 'A'}">
										Administrativa 
									</c:when>
									<c:otherwise>
										Judicial
									</c:otherwise>
								</c:choose>
							</td>
						</tr>
					</c:when>
					<c:otherwise>
						<ww:radio list="#{'J':'Judicial', 'A':'Administrativa'}" name="tipoMateria" id="tm" label="Tipo de Mat�ria"  value="${tipoMateria}" disabled="${disabledTpMat}" />
					</c:otherwise>
				</c:choose>
				<ww:textfield name="dtDispon" id="dt_dispon"
					onblur="javascript:verifica_data(this,true);prever_data();"
					label="Data para disponibiliza��o" />
				<tr>
					<td>Data de publica��o:</td>
					<td><div id="dt_publ" /></td>
				</tr>
				<tr class="button">
					<td></td>
					<td><input type="submit" value="Ok" /> <input type="button"
						value="Cancela" onclick="javascript:history.back();" />
				</tr>
			</table>
			<p>Aten��o:
			<ul>
				<li><span style="font-weight:bold">Data para
				Disponibiliza��o</span> - data em que a mat�ria efetivamente aparece no
				site</li>
				<li><span style="font-weight:bold">Data de Publica��o</span> -
				a Data de Disponibiliza��o + 1, conforme prev� art. 4�, par�grafo 3�
				da Lei 11419 / 2006</li>
			</ul>
			</p>
</form>
</td>
</tr>
</table>
</siga:pagina>