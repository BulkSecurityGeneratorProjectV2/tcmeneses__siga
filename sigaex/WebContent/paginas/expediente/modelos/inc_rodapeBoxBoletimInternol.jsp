<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	buffer="64kb"%>
<%@ taglib tagdir="/WEB-INF/tags/mod" prefix="mod"%>
<%@ taglib uri="http://localhost/functiontag" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:set var="idOrgaoUsu" value=""></c:set>
<c:set var="descricaoOrgaoUsu" value=""></c:set>
<c:set var="acronimoOrgaoUsu" value=""></c:set>
<c:choose>
	<c:when test="${empty mov}">
		<c:set var="descricaoOrgaoUsu" value="${doc.lotaTitular.orgaoUsuario.descricao}"/>
		<c:set var="idOrgaoUsu" value="${doc.lotaTitular.orgaoUsuario.idOrgaoUsu}"/>
		<c:set var="acronimoOrgaoUsu" value="${doc.lotaTitular.orgaoUsuario.acronimoOrgaoUsu}"/>
	</c:when>
	<c:otherwise>
		<c:set var="descricaoOrgaoUsu" value="${mov.lotaTitular.orgaoUsuario.descricao}"/>
		<c:set var="idOrgaoUsu" value="${mov.lotaTitular.orgaoUsuario.idOrgaoUsu}"/>
		<c:set var="acronimoOrgaoUsu" value="${mov.lotaTitular.orgaoUsuario.acronimoOrgaoUsu}"/>
	</c:otherwise>
</c:choose>

<span align="center">
<table cellspacing="0" width="96%" bgcolor="#FFFFFF" style="border-width: 1px; border-style: solid; ">
	<tr>
		<td width="100%">
			<c:import url="/paginas/expediente/modelos/inc_cabecalhoEsquerdaPrimeiraPagina2.jsp" />
		</td>
	</tr> 
	<tr>
		<td width="100%" style="padding-left: 0px; margin-left: 0px;">
		<table cellspacing="0" >
			<col width="35%"></col>
			<col width="65%"></col>
			<tr>
				<td width="35%" align="left" valign="center" style="margin-left:4px; font-size: 10pt; border-width: 1px 1px 0px 0px; border-style: solid">
					&nbsp;<br/>
					${requestScope['nmDiretorForo']}<br/>
					Juiz Federal - Diretor do Foro<br/>
					<br/>&nbsp;<br/>
					${requestScope['nmDiretorRH']}<br/>
					Diretora da Secretaria Geral
				</td>
				<td width="65%" align="right" style="margin-right:4px; font-size: 10pt; border-width: 1px 0px 0px 0px; border-style: solid">
					${doc.codigo} - Gera��o e impress�o: <br/>
					${requestScope['geraImpress']}<br/>
					Setores respons�veis pelas informa��es:<br/>
					${requestScope['setoresResponsaveis']}<br/>
					Publica��o di�ria na intranet ${acronimoOrgaoUsu}<br/>
					<br/>&nbsp;<br/>
					 Justi�a Federal - ${descricaoOrgaoUsu}<br/>
					<c:choose>
						<c:when test="${idOrgaoUsu == 1}">Av. Almirante Barroso, 78 - Centro / RJ</c:when>
						<c:otherwise>Rua S�o Francisco, 52, Centro - Vit�ria-ES</c:otherwise>
					</c:choose><br/>
				</td>
			</tr>
		</table>
		</td>
	</tr>
</table>
</span>