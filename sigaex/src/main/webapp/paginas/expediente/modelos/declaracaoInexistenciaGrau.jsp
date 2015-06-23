<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	buffer="64kb"%>
<%@ taglib tagdir="/WEB-INF/tags/mod" prefix="mod"%>
<%@ taglib uri="http://localhost/functiontag" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!-- Alterei este formul�rio para que sirva tanto para declarar que tem grau parentesco, como para declarar que n�o tem grau de parentesco.Rj13939 -->

<mod:modelo>
	<mod:entrevista>
		<mod:grupo>
			<mod:selecao titulo="V�nculo" var="vinculo"
				opcoes="Servidor;Requisitado com cargo;Requisitado sem cargo; Sem v�nculo com a administra��o"
				reler="ajax" idAjax="vinculoajax" />
		</mod:grupo>
		<mod:grupo depende="vinculoajax">
			<c:if
				test="${vinculo=='Servidor'or vinculo=='Requisitado com cargo'}">
				<mod:grupo>
					<mod:texto titulo="Cargo efetivo/Especialidade" var="cargo_espec"
						largura="50" />
				</mod:grupo>

				<mod:grupo>
					<mod:texto titulo="Fun��o comissionada/Cargo em Comiss�o"
						var="funcao_cargo" largura="50" />
				</mod:grupo>
			</c:if>
		</mod:grupo>
		<mod:grupo>
			<mod:texto titulo="Lota��o" var="lotacao" />
		</mod:grupo>
		<mod:grupo>
			<mod:selecao titulo="Possui Parentesco" var="parentesco"
				opcoes="N�o;Sim;Sim, mas n�o infringi nenhum dos dispositivos da Resolu��o n� 07/2005 - CNJ" reler="ajax" idAjax="parentescoajax" />
		</mod:grupo>
		<mod:grupo depende="parentescoajax">
			<c:if test="${parentesco != 'N�o'}">
				<mod:grupo>
					<mod:selecao titulo="Quantidade de parentes" var="nr_parentes"
						opcoes="1;2;3;" reler="ajax" idAjax="nr_parentesajax" />
				</mod:grupo>
				<mod:grupo depende="nr_parentesajax">
					<c:forEach var="i" begin="1" end="${nr_parentes}">

						<mod:grupo>
							<mod:texto titulo="${i}- Nome do parente" var="parente${i}"
								largura="50" />
						</mod:grupo>
						<mod:grupo>
							<mod:texto titulo="&nbsp;&nbsp;&nbsp; Grau de parentesco"
								var="grau${i}" largura="20" />
						</mod:grupo>
						<mod:grupo>
							<mod:data
								titulo="&nbsp;&nbsp;&nbsp Data de ingresso no cargo em comiss�o/fun��o de confian�a"
								var="dataingresso${i}" />
						</mod:grupo>
						<br>
					</c:forEach>
				</mod:grupo>
			</c:if>
		</mod:grupo>
	</mod:entrevista>

	<mod:documento>
		<c:set var="tl" value="11pt" />
		<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
		<head>
		<style type="text/css">
@page {
	margin-left: 3cm;
	margin-right: 3cm;
	margin-top: 1cm;
	margin-bottom: 2cm;
}
</style>
		</head>
		<body>
		<!-- INICIO PRIMEIRO CABECALHO
		<table width="100%" border="0"  bgcolor="#FFFFFF">
			<tr><td>
			<c:import url="/paginas/expediente/modelos/inc_cabecalhoCentralizadoPrimeiraPagina.jsp" />
			</td></tr>
			<tr bgcolor="#FFFFFF">
				<td width="100%">
					<br/><br/>
					<table width="100%" border="0" >
						<tr>
							<td align="center"><mod:letra tamanho="${tl}"><p style="font-family:Arial;font-weight:bold" >DECLARA&Ccedil;&Atilde;O</p></mod:letra></td>
						</tr>
						<tr>
							<td><br/><br/></td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
		FIM PRIMEIRO CABECALHO -->

		<!-- INICIO CABECALHO
		<c:import url="/paginas/expediente/modelos/inc_cabecalhoCentralizado.jsp" />
		FIM CABECALHO -->

		<mod:letra tamanho="${tl}">
			<!-- Esta vari�vel "corpoTexto" tem a finalidade de evitar duplicidade no c�digo -->
			<c:set var="corpoTextoParte1"
				value="rela��o familiar ou de parentesco que "></c:set>
			<c:set var="corpoTextoParte2"
				value="importe pr�tica vedada pela
						S�mula Vinculante N� 13 de 29 de agosto de 2008 do Supremo Tribunal
						Federal combinado com a Resolu��o N� 07/2005 do Conselho Nacional de
						Justi�a (CNJ)."></c:set>
			
			<c:set var="corpoTexto"
				value="${corpoTextoParte1}${corpoTextoParte2}"></c:set>

			<!-- Estes ifs s�o para omitir  v�rgulas e espa�os no texto, dependendo dos campos que forem preenchidos -->

			<c:if test="${ not empty funcao_cargo and not empty cargo_espec}">
				<p style="TEXT-INDENT: 2cm" align="justify">
				${doc.subscritor.descricao}, ${cargo_espec}, ${funcao_cargo},
				matr�cula n� ${doc.subscritor.matricula}, lotado(a) no(a) ${lotacao}, <b>DECLARA</b>
				que
			</c:if>
			<c:if test="${ empty funcao_cargo and not empty cargo_espec}">
				<p style="TEXT-INDENT: 2cm" align="justify">
				${doc.subscritor.descricao}, ${cargo_espec}, matr�cula n�
				${doc.subscritor.matricula}, lotado(a)  no(a) ${lotacao}, <b>DECLARA</b>
				que
			</c:if>
			<c:if test="${empty funcao_cargo and empty cargo_espec}">
				<p style="TEXT-INDENT: 2cm" align="justify">
				${doc.subscritor.descricao}, matr�cula n�
				${doc.subscritor.matricula}, lotado(a)  no(a) ${lotacao}, <b>DECLARA</b>
				que
			</c:if>
			<c:if test="${ empty cargo_espec and not empty funcao_cargo}">
				<p style="TEXT-INDENT: 2cm" align="justify">
				${doc.subscritor.descricao}, ${funcao_cargo}, matr�cula n�
				${doc.subscritor.matricula}, lotado(a)  no(a) ${lotacao}, <b>DECLARA</b>
				que
			</c:if>

			<c:choose>
				<c:when test="${parentesco eq 'Sim'}">
					<b>tem</b>
					${corpoTexto}		
				</c:when>
				<c:when test="${parentesco eq 'N�o'}">
					<b>n�o tem</b>
					${corpoTexto}
				</c:when>
				<c:otherwise>
					<b>tem</b>
					${corpoTextoParte1}
					<b>N�O</b>
					${corpoTextoParte2}
				</c:otherwise>
			</c:choose>
			<c:if test="${parentesco ne 'N�o'}">
				<c:forEach var="i" begin="1" end="${nr_parentes}">
					<p>Nome do Parente: <b>${requestScope[f:concat('parente',i)]}</b>.</p>
					<p>Grau de parentesco: <b>${requestScope[f:concat('grau',i)]}</b>.</p>
					<p>Data de ingresso no cargo em comiss�o/fun��o de confian�a:
					<b>${requestScope[f:concat('dataingresso',i)]}</b>.</p>
					<br>
				</c:forEach>
			</c:if>

			<p style="TEXT-INDENT: 2cm" align="justify">Declara, por fim, que
			dever� comunicar � Subsecretaria de Gest�o de Pessoas, de imediato, a
			ocorr�ncia de fatos que possam alterar a situa��o objeto desta
			declara��o.</p>

			<p style="TEXT-INDENT: 2cm" align="justify">Responsabiliza-se
			pela exatid�o e veracidade das informa��es declaradas, ciente de que,
			se falsa a declara��o, ficar� sujeito(a) �s penas da lei (art. 299, do
			CP).</p>

			<p align="center">${doc.dtExtenso}</p>
			<br />
			<c:import url="/paginas/expediente/modelos/inc_assinatura.jsp" />
			<p align="center">Matr�cula: ${doc.subscritor.matricula}</p>
		</mod:letra>

		<!-- INICIO PRIMEIRO RODAPE
		<c:import url="/paginas/expediente/modelos/inc_rodapeClassificacaoDocumental.jsp" />
		FIM PRIMEIRO RODAPE -->

		<!-- INICIO RODAPE
		<c:import url="/paginas/expediente/modelos/inc_rodapeNumeracaoCentralizada.jsp" />
		FIM RODAPE -->

		</body>
		</html>
	</mod:documento>
</mod:modelo>