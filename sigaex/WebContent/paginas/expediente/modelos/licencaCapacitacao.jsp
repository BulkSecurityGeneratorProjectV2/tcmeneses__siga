<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	buffer="64kb"%>
<%@ taglib tagdir="/WEB-INF/tags/mod" prefix="mod"%>
<%@ taglib uri="http://localhost/functiontag" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<mod:modelo>
	<mod:entrevista> 
	<mod:grupo titulo="Per�odo de Licen�a para Capacita��o">
			<mod:data titulo="De" var="dataInicio" />
			<mod:data titulo="a" var="dataFim" />
		</mod:grupo>
		    
			<mod:selecao titulo="Participa��o em:" var="cursos" opcoes="Curso de Capacita��o;Pesquisa e Levantamento de Dados" reler="ajax" idAjax="cursosAjax" />
				<mod:grupo depende="cursosAjax">
					<c:if test="${cursos eq 'Pesquisa e Levantamento de Dados'}">
						<mod:texto titulo="Tema do curso:" var="temaCurso" largura="60" />
					</c:if>
				</mod:grupo>
		
	</mod:entrevista>
	
	<mod:documento>
	<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
		<head>
		<style type="text/css">
			@page {
				margin-left: 3cm;
				margin-right: 2cm;
				margin-top: 1cm;
				margin-bottom: 2cm;
			}
		</style>
		</head>
		<body>
		<c:import url="/paginas/expediente/modelos/inc_tit_SraDiretoraSubsecretariaRH.jsp" />
		<p style="TEXT-INDENT: 2cm" align="justify">
		${doc.subscritor.descricao}, ${doc.subscritor.cargo.nomeCargo}, ${doc.subscritor.padraoReferenciaInvertido}, lotado(a) no(a) ${doc.subscritor.lotacao.descricao}, vem requerer a Vossa Senhoria, nos termos do art.87, da Lei n&ordm; 8.112/90, com a
		reda��o da Lei n&ordm; 9.527/97, c/c Resolu��o n&ordm; 5/2008, do Conselho da Justi�a Federal, e Resolu��o n&ordm; 22/2008, do TRF da 2� Regi�o, <b>LICEN�A PARA CAPACITA��O</b> a que faz jus, para frui��o 
		<c:choose>
				<c:when test="${(dataInicio == dataFim) or (empty dataFim)}">
					no dia <b>${dataInicio}</b>,
				</c:when>
					<c:otherwise>
					no per�odo de <b>${dataInicio}</b> a <b>${dataFim}</b>,
					</c:otherwise>
			</c:choose>
		
		destinada � 
			<c:choose>
				<c:when test="${cursos eq 'Curso de Capacita��o'}">
					participa��o em Curso de Capacita��o.
				</c:when>
				<c:otherwise>
					pesquisa e levantamento de dados necess�rios � elabora��o de trabalho para conclus�o de curso de p�s-gradua��o, cujo tema �: ${temaCurso}.				
				</c:otherwise>
			</c:choose>
		</p>
		<p style="TEXT-INDENT: 2cm" align="justify">
			Declara, ainda, estar ciente de que, de acordo com o art. 23, &sect;1&ordm; e &sect; 2&ordm;, da Resolu��o n&ordm; 5/2008, do CJF, e art. 5&ordm;, da Resolu��o n&ordm; 22/2008, do TRF da 2� Regi�o, dever�, ao final da atividade, apresentar, no prazo m�ximo de trinta dias, comprovante de freq��ncia no curso ou 
			certificado de conclus�o, c�pia da monografia/disserta��o e, a crit�rio da Administra��o, relat�rio circunstanciado, sendo que o descumprimento poder� acarretar a instaura��o de sindic�ncia nos termos da legisla��o vigente.
		</p>
		<c:import url="/paginas/expediente/modelos/inc_deferimento.jsp" />
		<br/>
		<br/>
		<p align="center">${doc.dtExtenso}</p>
		<c:import
			url="/paginas/expediente/modelos/inc_assinatura.jsp?apenasCargo=sim" />
		<c:import
			url="/paginas/expediente/modelos/inc_classificacaoDocumental.jsp" />
			
		<c:import url="/paginas/expediente/modelos/inc_quebra_pagina.jsp" />
		<c:import url="/paginas/expediente/modelos/inc_tit_termoCompromisso.jsp" />
		<p style="TEXT-INDENT: 2cm" align="justify">
		${doc.subscritor.descricao}, ${doc.subscritor.cargo.nomeCargo}, ${doc.subscritor.padraoReferenciaInvertido}, lotado(a) no(a) ${doc.subscritor.lotacao.descricao}, firma o compromisso de apresentar relat�rio semanal das atividades desenvolvidas, 
		devidamente endossado pelo orientador ou coordenador do respectivo curso, nos termos do art. 2�, da Resolu��o n� 22/2008, do TRF da 2� Regi�o, tendo em vista tratar-se de licen�a para capacita��o com a finalidade de conclus�o de curso de especializa��o, mestrado ou doutorado. 
		</p>
		<br/>
		<br/>
		<p align="center">${doc.dtExtenso}</p>
		<c:import
			url="/paginas/expediente/modelos/inc_assinatura.jsp?apenasCargo=sim" />
		</body>
		</html>
	</mod:documento>
</mod:modelo>
