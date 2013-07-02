<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	buffer="64kb"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="ww" uri="/webwork"%>

<%--<%@ taglib prefix="tags" tagdir="/WEB-INF/tags"%>--%>
<%@ taglib uri="http://localhost/customtag" prefix="tags"%>
<%@ taglib uri="http://localhost/sigatags" prefix="siga"%>

<siga:pagina titulo="Movimenta��o">

<c:if test="${not mob.doc.eletronico}">
	<script type="text/javascript">$("html").addClass("fisico");</script>
</c:if>

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
	function contaLetras() {
		var i = 256 - tamanho();
		document.getElementById("Qtd").innerText = 'Restam ' + i + ' caracteres';
	}

	function verificaTamanho() {		
		var i = tamanho();	
		if (i>256) {
			alert('Descri��o com mais de 256 caracteres');
			document.getElementById('descrPublicacao').focus();
		}		
	}

	function tamanho() {
		nota= new String();
		nota = this.frm.descrPublicacao.value;
		return nota.length;		
	}

	function validar() {
		var data = document.getElementsByName('dtDispon')[0].value;
		if (data==null || data=="") {			
			alert("Preencha a data para disponibiliza��o.");
			document.getElementById('dt_dispon').focus();		
		}else
			frm.submit();	
	}
	
	
</script>

<!-- A linha abaixo � tempor�ria, pois est� presente num dos cabe�alhos  -->
<div id="carregando" style="position:absolute;top:0px;right:0px;background-color:red;font-weight:bold;padding:4px;color:white;display:none">Carregando...</div>

	<div class="gt-bd clearfix">
		<div class="gt-content clearfix">
		
			<h2>Agendamento de Publica��o - ${mob.siglaEDescricaoCompleta}</h2>

			<div class="gt-content-box gt-for-table">

		<form name="frm" action="agendar_publicacao_gravar.action"
			namespace="/expediente/mov" cssClass="form" method="GET">
			<ww:token/>
			<input type="hidden" name="postback" value="1" />
			<ww:hidden name="sigla" value="%{sigla}"/>
			<table class="gt-form-table">
				<colgroup>
				<col  style="width:30%"/>
				<col  style="width:70%"/>
				</colgroup>
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
				<ww:select name="lotPublicacao" list="listaLotPubl" label="Lota��o de publica��o"/>	
				<ww:textarea name="descrPublicacao" cols="80" id="descrPublicacao"
							rows="2" cssClass="gt-form-textarea" label="Descri��o do documento"
							onkeyup="contaLetras();" onblur="verificaTamanho();"/>	
				<tr><td></td><td><div id="Qtd">Restam&nbsp;${tamMaxDescr}&nbsp;caracteres</div></td></tr>						
				<tr>
					<td colspan="2"><input type="button" value="Ok" onclick="javascript: validar();" class="gt-btn-medium gt-btn-left" /> <input type="button"
						value="Cancela" onclick="javascript:history.back();" class="gt-btn-medium gt-btn-left" />
				</tr>
			</table>
			</form>
			</div>
			
			<br/>
			<span style="margin-left: 0.5cm;color: red;"><b>Aten��o:</b></span>
			<ul>
				<li><span style="font-weight:bold">Data para
				Disponibiliza��o</span> - data em que a mat�ria efetivamente aparece no
				site</li>
				<li><span style="font-weight:bold">Data de Publica��o</span> -
				a Data de Disponibiliza��o + 1, conforme prev� art. 4�, par�grafo 3�
				da Lei 11419 / 2006</li>
			</ul>
</div></div>
</siga:pagina>
