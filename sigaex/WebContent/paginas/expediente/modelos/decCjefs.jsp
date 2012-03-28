<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	buffer="64kb"%>
<%@ taglib uri="http://localhost/modelostag" prefix="mod"%>
<%@ taglib uri="http://localhost/functiontag" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<mod:modelo>
	<mod:entrevista>
	  	<br>
		<mod:grupo>
				<mod:selecao var="organ"
				titulo="<b>�rg�o</b>"
				opcoes="Coordenadoria dos Juizados Especiais Federais;Gabinete Pres. Coordenadoria dos Juizados Especiais Federais;N�cleo de Apoio - Coordenadoria dos Juizados Especiais Federais"	
				reler="sim" />
		</mod:grupo>
		<mod:grupo>
			    <mod:texto titulo="<b>Refer�ncia</b>" var="proc" largura="25"/>
		</mod:grupo>
		<c:if test="${empty esconderTexto}">
			<mod:grupo
				titulo="Texto a ser inserido no corpo da Decis�o">
				<mod:grupo>
					<mod:editor titulo="" var="texto_dec" />
				</mod:grupo>  
			</mod:grupo>
		</c:if>
		<br>
		<mod:grupo>
			    <mod:texto titulo="<b>Fecho</b>" var="theend" largura="40"/>
		</mod:grupo>
		<br><mod:selecao titulo="Tamanho da letra" var="tamanhoLetra" opcoes="Normal;Pequeno;Grande"/></br>
	</mod:entrevista>
	
	<mod:documento>
		<c:if test="${tamanhoLetra=='Normal'}">
			<c:set var="tl" value="11pt" />
		</c:if>
		<c:if test="${tamanhoLetra=='Pequeno'}">
			<c:set var="tl" value="9pt" />
		</c:if>
		<c:if test="${tamanhoLetra=='Grande'}">
			<c:set var="tl" value="13pt" />
		</c:if>
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

		<c:if test="${empty tl}">
			<c:set var="tl" value="11pt"></c:set>
		</c:if>

		<!-- INICIO PRIMEIRO CABECALHO
		<table width="100%" border="0" bgcolor="#FFFFFF"><tr><td>
		<c:import url="/paginas/expediente/modelos/inc_cabecalhoCentralizadoPrimeiraPagina.jsp" />
		</td></tr>
		
		<tr><td><br/>&nbsp;<br/></td></tr>
			<tr bgcolor="#FFFFFF">
				<td width="100%">
					<table width="100%">
						<tr>
							<td align="center"><p style="font-family:Arial;font-weight:bold;font-size:11pt;">DECIS�O N&ordm; ${doc.codigo} DE ${doc.dtExtensoMaiusculasSemLocalidade}</p></td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
		FIM PRIMEIRO CABECALHO -->

		<!-- INICIO CABECALHO
		<c:import url="/paginas/expediente/modelos/inc_cabecalhoEsquerda.jsp" />
		FIM CABECALHO -->
                <!-- INICIO NUMERO <span style="font-weight: bold;">DECIS�O N&ordm; ${doc.codigo} DE ${doc.dtExtensoMaiusculasSemLocalidade}</span> FIM NUMERO -->
		<mod:letra tamanho="${tl}">
		    <!-- INICIO MIOLO -->
                        <p><span style="font-size:${tl};">
			<!-- INICIO CORPO -->
			<br>
			<p align="left"><b>Refer�ncia: ${proc}</b></p>
			<p align="left"><b>${organ}</b></p>
			<p align="center" style="font-family:Arial;font-weight:bold;font-size:14pt;"><b>Decis�o</b></p>
			${texto_dec}
			<!-- FIM CORPO -->
                        </span>
                        </p>
                        <p align="center">
			<!-- INICIO FECHO -->
			<c:if test="${not empty theend}"><p align="center"><b>${theend}</b></p></c:if>
			
			<!-- FIM FECHO -->
            </p>
			<!-- FIM MIOLO -->
                        <!-- INICIO ASSINATURA -->
			<c:import url="/paginas/expediente/modelos/inc_assinatura.jsp?formatarOrgao=sim" />
                        <!-- FIM ASSINATURA -->
		</mod:letra>
		/${doc.cadastrante.siglaPessoa}</br>
		<!-- INICIO PRIMEIRO RODAPE
		<c:import url="/paginas/expediente/modelos/inc_rodapeClassificacaoDocumental.jsp" />
		FIM PRIMEIRO RODAPE -->
		<!-- INICIO RODAPE
		<c:import url="/paginas/expediente/modelos/inc_rodapeNumeracaoADireita.jsp" />
		FIM RODAPE -->
		</body>
		</html>
	</mod:documento>
</mod:modelo>

