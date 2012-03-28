<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	buffer="64kb"%>
<%@ taglib tagdir="/WEB-INF/tags/mod" prefix="mod"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="siga"%>
<%@ taglib uri="/WEB-INF/tld/func.tld" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>


<c:set var="esconderTexto" value="sim" scope="request" />

<mod:modelo>
	<mod:entrevista>
		<mod:grupo>
			<p align="left"><span style="color: red"> <br>
			(os campos marcados com * s�o de preenchimento obrigat�rio)</span></p>

		</mod:grupo>
		<!-- 
		<mod:grupo>
			<mod:radio titulo="INICIAL" var="periodo" valor="1" reler="sim"
				marcado="Sim" />
			<mod:radio titulo="PRORROGA��O" var="periodo" valor="2" reler="sim" />
		</mod:grupo>
         -->
		<hr style="color: #FFFFFF;" />
		
        <mod:grupo titulo="Proposto pertencente aos quadros do Tribunal Regional Federal da 2� Regi�o (*)">

			<mod:radio titulo="SIM" var="ppq" valor="1" reler="sim"/>
			<mod:radio titulo="N�O" var="ppq" valor="2" reler="sim"/>
		</mod:grupo>
		<c:set var="valorppq" value="${ppq}" />
	        <c:if test="${empty valorppq}">
	           <c:set var="valorppq" value="${param['ppq']}" />
	        </c:if>
		<c:if test="${valorppq == '1'}">
		<mod:grupo titulo="Proposto (*)">
		    <mod:pessoa titulo="Nome" var="prop" />
			<br>
			<br>

			<mod:texto titulo="C/C n�" var="contac" />
			<mod:texto titulo="Banco" var="banc" />
			<mod:texto titulo="Ag�ncia" var="agen" />
			<br>
			<mod:grupo>
			    <p align="left"><span style="color: red"> <br>Aten��o : use ponto ao inv�s de v�rgula nos valores abaixo, para que n�o ocorra erro</span></p>
            </mod:grupo>
            <!--
			<mod:texto titulo="Valor Mensal do Aux�lio-Alimenta��o" var="valref" />
			<mod:texto titulo="Valor Mensal do Aux�lio-Transporte" var="valtrans" />
		    -->
		</mod:grupo>
		</c:if>
		<c:if test="${valorppq == '2'}">
		<mod:grupo titulo="Proposto (*)">
		<mod:texto titulo="Nome" var="prop" largura="50"/>	
		<mod:texto titulo="CPF (somente algarismos)" var="cpf" />
		<br>
		<mod:texto titulo="Cargo" var="carg" largura="45"/>
			<br>

			<mod:texto titulo="C/C n�" var="contac" />
			<mod:texto titulo="Banco" var="banc" />
			<mod:texto titulo="Ag�ncia" var="agen" />
			<br>
			<mod:grupo>
			    <p align="left"><span style="color: red"> <br>Aten��o : use ponto ao inv�s de v�rgula nos valores abaixo, para que n�o ocorra erro</span></p>
            </mod:grupo>
			<mod:texto titulo="Valor Mensal do Aux�lio-Alimenta��o" var="valref" />
			<mod:texto titulo="Valor Mensal do Aux�lio-Transporte" var="valtrans" />
		</mod:grupo>
		 </c:if>
		<hr style="color: #FFFFFF;" />
		<mod:grupo>
			<mod:memo titulo="<b>Local e Servi�o a ser executado (*)</b>" var="texto_serv" colunas="70"
				linhas="3" />
		</mod:grupo>
		<br>
		<br>
		<mod:grupo titulo="Solicita��o de passagem a�rea (*)">

			<mod:radio titulo="SIM" var="opc" valor="1" reler="sim" />
			<mod:radio titulo="N�O" var="opc" valor="2" reler="sim" marcado="Sim"/>
		</mod:grupo>
		<hr style="color: #FFFFFF;" />
		<mod:grupo titulo="Uso de Carro Oficial (*)">

			<mod:radio titulo="SIM" var="opcr" valor="1" reler="sim"
				/>
			<mod:radio titulo="N�O" var="opcr" valor="2" reler="sim" marcado="Sim" />
		</mod:grupo>

		<hr style="color: #FFFFFF;" />
		<mod:grupo titulo="Itiner�rios (*)">
			<mod:texto titulo="Origem/Destino" var="dest" largura="40" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<br></br>
		</mod:grupo>
		<mod:grupo titulo="Per�odo de afastamento (*)">
			<mod:selecao titulo="Dia �nico" var="diaunic" opcoes="SIM;N�O"
				reler="sim" />
			<br>
			<c:choose>
				<c:when test="${diaunic=='SIM'}">
					<mod:data titulo="Dia de afastamento" var="perafast" />
				</c:when>
				<c:otherwise>
					<mod:data titulo="Per�odo de afastamento" var="perafast" />
					&nbsp;&nbsp;<mod:data titulo="a" var="perafast2" />
				</c:otherwise>
			</c:choose>
		</mod:grupo>
		
		<hr style="color: #FFFFFF;" />
		<span style="color: red"> <b>OBS.: O afastamento que se
		iniciar a partir da sexta-feira ou que inclua s�bado, domingo ou
		feriado dever� ser acompanhado de justificativa, condicionando-se o
		pagamento � aceita��o da justificativa pelo ordenador de despesas(art 111,II,�2� Res. 4/2008/CJF). O mesmo se dar� quando houver
		necessidade de pernoite na noite anterior e/ou posterior ao evento.</b> </span>


		<mod:grupo>
			<mod:memo titulo="<b>Justificativa</b>" var="just" colunas="60"
				linhas="3" />
		</mod:grupo>



	</mod:entrevista>

	<mod:documento>
		<head>
		<style type="text/css">
@page {
	margin-left: 0.5cm;
	margin-right: 0.5cm;
	margin-top: 1cm;
	margin-bottom: 1cm;
}
</style>
		</head>
		<c:set var="pessoa_propon"
			value="${f:pessoa(requestScope['propo_pessoaSel.id'])}" />
		<c:set var="pessoa_prop"
			value="${f:pessoa(requestScope['prop_pessoaSel.id'])}" />

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
							<td align="center"><p style="font-family:Arial;font-weight:bold;font-size:11pt;"><br/>PROPOSTA E CONCESS�O DE DI�RIAS N&ordm; ${doc.codigo} DE ${doc.dtExtensoMaiusculasSemLocalidade}</p></td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
		FIM PRIMEIRO CABECALHO -->
		<br>
		<c:set var="cpf" value="${f:formatarCPF(cpf)}"></c:set>
		<c:set var="alm" value="${f:floatParaMonetario(valref)}"></c:set>
        <c:set var="trp" value="${f:floatParaMonetario(valtrans)}"></c:set>
		<c:if test="${ppq == '1'}">
           <c:set var="prop" value="${f:maiusculasEMinusculas(pessoa_prop.nomePessoa)}"></c:set>
           <c:set var="cpf" value="${f:formatarCPF(pessoa_prop.cpfPessoa)}"></c:set>
           <c:set var="alm" value=""></c:set>
           <c:set var="trp" value=""></c:set>
           <c:set var="carg" value="${f:maiusculasEMinusculas(pessoa_prop.cargo.descricao)}"></c:set>
           <c:set var="func" value="${pessoa_prop.funcaoConfianca.nomeFuncao}"></c:set>
           <c:set var="matr" value="${pessoa_prop.sesbPessoa}${pessoa_prop.matricula}"></c:set>
        </c:if>
       <!--
        <b>
        <table border="0"  bgcolor="#FFFFFF" cellpadding="1" width="51%" align="right" >
	  		   <tr>
  				<td width="40%" style="font-family:Arial;font-size:8.5pt;font-weight:normal" bgcolor="#FFFFFF" border="0" align="left"><b>(&nbsp;&nbsp;)&nbsp;INICIAL</b></td>
				<td width="60%" style="font-family:Arial;font-size:8.5pt;font-weight:normal" bgcolor="#FFFFFF" border="0" align="left"><b>(&nbsp;&nbsp;) Julgamento de Causas</b></td>
  			   </tr>
  		</table>
        <table width="100%" border="0" cellpadding="1" cellspacing="0" align="right">
			<tr>
               	<td bgcolor="#FFFFFF" align="left">  </td>
			</tr>
		</table>
        <table border="0"  bgcolor="#FFFFFF" cellpadding="1" width="51%" align="right" >
	  		   <tr>
  				<td width="40%" style="font-family:Arial;font-size:8.5pt;font-weight:normal" bgcolor="#FFFFFF" border="0" align="left"><b>(&nbsp;&nbsp;)&nbsp;PRORROGA��O</b></td>
				<td width="60%" style="font-family:Arial;font-size:8.5pt;font-weight:normal" bgcolor="#FFFFFF" border="0" align="left"><b>(&nbsp;&nbsp;) Capacita��o de Recursos Humanos</b></td>
  			   </tr>
  		</table>
  		</b>
  		<br>
  		</br>
          -->
		<!-- INICIO DA TABELA PROPONENTE -->
		<table width="100%" border="1" cellpadding="2" cellspacing="1">
			<tr>
				<td>
				<table width="100%" border="1" cellpadding="2" cellspacing="1">
					<tr>

						<td>
						<table width="100%" border="1" cellpadding="2" cellspacing="1">
							<tr>
								<td width="100%" bgcolor="#FFFFFF" align="left"><b>1-PROPONENTE</b>
								</td>
							</tr>
						</table>


						<table width="100%" border="0" cellpadding="3" cellspacing="0">
							<tr>
								<td width="100%" bgcolor="#FFFFFF" align="left"><b>Nome&nbsp;
								&nbsp;&nbsp;:</b>&nbsp;${f:maiusculasEMinusculas(doc.subscritor.descricao)}</td>
							</tr>
						</table>


						<table width="100%" border="0" cellpadding="3" cellspacing="0">
							<tr>
								<td width="100%" bgcolor="#FFFFFF" align="left"><b>Fun��o
								:</b>&nbsp;${f:maiusculasEMinusculas(doc.subscritor.funcaoConfianca.nomeFuncao)}</td>
							</tr>
						</table>
				</table>
				<table width="100%" border="1" cellpadding="2" cellspacing="1">
					<tr>
						<td>
						<table width="100%" border="1" cellpadding="2" cellspacing="1">
							<tr>
								<td width="100%" bgcolor="#FFFFFF" align="left"><b>2-PROPOSTO</b>
								</td>
							</tr>
						</table>
						<table width="100%" border="0" cellpadding="5" cellspacing="0">
							<tr>
								<td width="75%" bgcolor="#FFFFFF" align="left"><b>Nome :
								</b>&nbsp;${prop}</td>
								
								<td width="25%" bgcolor="#FFFFFF" align="left"><b>CPF :</b> &nbsp;${cpf}</td>
							</tr>
						</table>
						<table width="100%" border="0" cellpadding="5" cellspacing="0">
							<tr>
								<td width="75%" bgcolor="#FFFFFF" align="left"><b>Cargo
								</b>&nbsp;:&nbsp;${carg}&nbsp;&nbsp;&nbsp;&nbsp;<b>Fun��o
								</b>&nbsp;:&nbsp;${func}</td>
								<td width="25%" bgcolor="#FFFFFF" align="left"><b>Matr�cula :
								</b>&nbsp;&nbsp;${matr}</td>
							</tr>
						</table>
						<table width="100%" border="0" cellpadding="5" cellspacing="0">
							<tr>
								<td width="40%" bgcolor="#FFFFFF" align="left"><b>C/C
								N� : </b>&nbsp;${contac}</td>
								<td width="35%" bgcolor="#FFFFFF" align="left"><b>Banco :
								</b>&nbsp;${banc}</td>
								<td width="25%" bgcolor="#FFFFFF" align="left"><b>Ag�ncia :
								</b>&nbsp;${agen}</td>
							</tr>
						</table>
						<table width="100%" border="0" cellpadding="5" cellspacing="0">
							<tr>
								<td width="100%" bgcolor="#FFFFFF" align="left"><b>Valor
								Mensal do Aux�lio-Alimenta��o : </b>&nbsp;R$&nbsp;${alm}</td>
								<td width="100%" bgcolor="#FFFFFF" align="left"><b>Valor
								Mensal do Aux�lio-Transporte : </b>&nbsp;R$&nbsp;${trp}
								</td>

							</tr>
						</table>
				</table>

				<table width="100%" border="1" cellpadding="2" cellspacing="1">
					<tr>
						<td>

						<table width="100%" border="1" cellpadding="2" cellspacing="1">
							<tr>
								<td width="100%" bgcolor="#FFFFFF" align="left"><b>Local
								e Servi�o a ser executado:</b></td>
							</tr>
						</table>
						<table width="100%" border="0" cellpadding="5" cellspacing="0">
							<tr>
								<td width="100%" bgcolor="#FFFFFF">${texto_serv}</td>


							</tr>
						</table>
				</table>
				<table width="100%" border="1" cellpadding="2" cellspacing="1">
					<tr>

						<td>
						<table width="100%" border="1" cellpadding="2" cellspacing="1">
							<tr>
								<td width="100%" bgcolor="#FFFFFF" align="left"><b>Solicita��o
								de Passagem A�rea </b></td>

							</tr>
						</table>
						<table width="100%" border="0" cellpadding="2" cellspacing="0">
							<tr>
								<td width="50%" bgcolor="#FFFFFF"><c:choose>
									<c:when test="${opc==1}">&nbsp;(X)&nbsp;SIM&nbsp;(&nbsp;&nbsp;)&nbsp;N�O</c:when>
									<c:otherwise>&nbsp;(&nbsp;&nbsp;)&nbsp;SIM&nbsp;(X)&nbsp;N�O</c:otherwise>
								</c:choose></td>



							</tr>
						</table>
				</table>
				<table width="100%" border="1" cellpadding="2" cellspacing="1">
					<tr>

						<td>
						<table width="100%" border="1" cellpadding="2" cellspacing="1">
							<tr>
								<td width="100%" bgcolor="#FFFFFF" align="left"><b>Uso
								de Carro Oficial</b></td>

							</tr>
						</table>
						<table width="100%" border="0" cellpadding="2" cellspacing="0">
							<tr>
								<td width="50%" bgcolor="#FFFFFF"><c:choose>
									<c:when test="${opcr==1}">&nbsp;(X)&nbsp;SIM&nbsp;(&nbsp;&nbsp;)&nbsp;N�O</c:when>
									<c:otherwise>&nbsp;(&nbsp;&nbsp;)&nbsp;SIM&nbsp;(X)&nbsp;N�O</c:otherwise>
								</c:choose></td>
							</tr>
						</table>
			     </table>
			     
			     <!--  -->
			     <table width="100%" border="1" cellpadding="2" cellspacing="1">
					  <tr>
                      <td>
						<table width="100%" border="1" cellpadding="2" cellspacing="1">
						   <tr>
							<td width="100%" bgcolor="#FFFFFF" align="left"><b>Itiner�rios</b></td>
                           </tr>
				        </table>
				        <table width="100%" border="0" cellpadding="2" cellspacing="0">
					       <tr>
						     <td width="65%" bgcolor="#FFFFFF"><b>Origem/Destino</b></td>
                             <td width="35%" bgcolor="#FFFFFF"><b><c:choose>
					            <c:when test="${diaunic=='SIM'}">Dia de Afastamento</c:when>
					            <c:otherwise>Per�odo de Afastamento</c:otherwise>
					            </c:choose></b></td>
                           </tr>
				        </table>
				        <table width="100%" border="0" cellpadding="2" cellspacing="0">
					         <tr>
					         <td width="65%" bgcolor="#FFFFFF">${dest}</td>
					         <td width="35%" bgcolor="#FFFFFF"><c:choose>
					            <c:when test="${diaunic=='SIM'}">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${perafast}</c:when>
					            <c:otherwise>${perafast}&nbsp;a&nbsp;${perafast2}</c:otherwise>
					            </c:choose>
					         </td>
					         </tr>
        		        </table>
			     </table>
			     
			     <!--  -->
			     <table width="100%" border="1" cellpadding="2" cellspacing="1">
					<tr>
						<td>

						<table width="100%" border="1" cellpadding="2" cellspacing="1">
							<tr>
								<td width="100%" bgcolor="#FFFFFF" align="left"><b>Justificativa</b></td>
							</tr>
						</table>
						<table width="100%" border="0" cellpadding="5" cellspacing="0">
							<tr>
								<td width="100%" bgcolor="#FFFFFF">${just}</td>


							</tr>
						</table>
				</table>
			     
			  </table>   
		    
		

		<!-- FIM DA TABELA PROPONENTE-->


       <!-- INICIO CABECALHO
		<c:import url="/paginas/expediente/modelos/inc_cabecalhoEsquerda.jsp" />
		FIM CABECALHO -->
				
		<!-- INICIO FECHO -->
		<p align="center">${doc.dtExtenso}</p>
	    <!-- FIM FECHO -->
	    
	    <br>
	    
		<!-- INICIO ASSINATURA -->
		<c:import url="/paginas/expediente/modelos/inc_assinatura.jsp?formatarOrgao=sim" />
		<!-- FIM ASSINATURA -->
		
		<br>/${doc.cadastrante.siglaPessoa}</br> 
		<!-- 
		<c:import url="/paginas/expediente/modelos/inc_quebra_pagina.jsp" />
		<br>
		<table width="100%" border="1" cellpadding="2" cellspacing="1">
			<tr>
				<td>
				
				<table width="100%" border="1" cellpadding="2" cellspacing="1">
					<tr>

						<td>
						<table width="100%" border="1" cellpadding="2" cellspacing="1">
							<tr>
								<td width="100%" bgcolor="#FFFFFF" align="left"><b>C�lculo
								das Di�rias</b></td>
							</tr>
						</table>
						<table width="100%" border="0" cellpadding="2" cellspacing="0">
							<tr>
								<td width="27.5%" bgcolor="#FFFFFF"><b>N�mero Total de
								Di�rias</b></td>
                                <td width="27.5%" bgcolor="#FFFFFF" align="left"><b>:</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;___________________</td>
								<td width="16%" bgcolor="#FFFFFF"><b>Vale-Refei��o</b></td>
                                <td width="29%" bgcolor="#FFFFFF" align="left"><b>:</b>&nbsp;R$&nbsp;_____________________</td>
							</tr>
						</table>
						<table width="100%" border="0" cellpadding="2" cellspacing="0">
							<tr>
								<td width="27.5%" bgcolor="#FFFFFF" align="left"><b>Valor Unit�rio da Di�ria</td>
								<td width="27.5%" bgcolor="#FFFFFF" align="left"><b>:</b>&nbsp;R$&nbsp;___________________</td>
								<td width="16%" bgcolor="#FFFFFF"><b>Vale-Transporte</b></td>
								<td width="29%" bgcolor="#FFFFFF" align="left"><b>:</b>&nbsp;R$&nbsp;_____________________</td>
							</tr>
						</table>
						<table width="100%" border="0" cellpadding="2" cellspacing="0">
							<tr>
								<td width="27.5%" bgcolor="#FFFFFF" align="left"><b>Aux�lio</b>&nbsp;(art. 107 �2� Res.CJF)</b></td>
								<td width="27.5%" bgcolor="#FFFFFF" align="left"><b>:</b>&nbsp;R$&nbsp;___________________</td>
								<td width="16%" bgcolor="#FFFFFF" align="left"><b>Total Desconto</b></td>
                                <td width="29%" bgcolor="#FFFFFF" align="left"><b>:</b>&nbsp;R$&nbsp;_____________________</td>
							</tr>
						</table>
						<table width="100%" border="0" cellpadding="2" cellspacing="0">
							<tr>
								<td width="27.5%" bgcolor="#FFFFFF" align="left"><b>Valor Bruto</b></td>
								<td width="27.5%" bgcolor="#FFFFFF" align="left"><b>:</b>&nbsp;R$&nbsp;___________________</td>
								<td width="16%" bgcolor="#FFFFFF" align="left"><b>Valor L�quido</b></td>
                                <td width="29%" bgcolor="#FFFFFF" align="left"><b>:</b>&nbsp;R$&nbsp;_____________________</td>
							</tr>
						</table>
						<table width="100%" border="1" cellpadding="2" cellspacing="1">
							<tr>

								<td>
								<table width="100%" border="1" cellpadding="2" cellspacing="1">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"><b>3-Concess�o</b>
										</td>

									</tr>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="50%" bgcolor="#FFFFFF">(&nbsp;&nbsp;)&nbsp;Concedo
										as di�rias.Pague-se.</td>
										<td width="50%" bgcolor="#FFFFFF">(&nbsp;&nbsp;)&nbsp;Requisite(m)-se
										a(s) passagem(ens)</td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left">Em
										&nbsp;____ /____ /_________.</td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="center">__________________________________________________________</td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="center">Assinatura
										da Autoridade Competente</td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="1" cellpadding="2" cellspacing="1">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"><b>4-Pagamento/Recebimento</b>
										</td>

									</tr>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left">Paga a
										import�ncia
										de&nbsp;&nbsp;_________________&nbsp;(&nbsp;___________________________________________________</td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left">________________________________________________________________&nbsp;),</td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="60%" bgcolor="#FFFFFF" align="left">atrav�s da
										Ordem Banc�ria n�.&nbsp;&nbsp;___________________________,</td>
										<td width="40%" bgcolor="#FFFFFF" align="left">&nbsp;de&nbsp;____
										/____ /_________.</td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="center">__________________________________________________________</td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="center">Assinatura
										do Resp. Setor Financeiro</td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="1" cellpadding="2" cellspacing="1">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"><b>5-Publica��o</b>
										</td>

									</tr>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left">O
										presidente documento est� de acordo com as normas
										regulamentares pertinentes e ser� publicado no Boletim:</td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="50%" bgcolor="#FFFFFF" align="left">Concess�o:</td>

										<td width="50%" bgcolor="#FFFFFF" align="left">Pagamento:</td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="50%" bgcolor="#FFFFFF" align="left">N�.&nbsp;______________
										&nbsp;,&nbsp;&nbsp;de&nbsp;____ /____ /_______.</td>

										<td width="50%" bgcolor="#FFFFFF" align="left">N�.&nbsp;_______________
										&nbsp;,&nbsp;&nbsp;de&nbsp;____ /____ /_______.</td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="center">__________________________________________________________</td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="center">Assinatura
										do Respons�vel</td>
								</table>
								<table width="100%" border="0" cellpadding="2" cellspacing="0">
									<tr>
										<td width="100%" bgcolor="#FFFFFF" align="left"></td>
								</table>
						</table>
				</table>
		</table>
         -->

		
		<!-- INICIO PRIMEIRO RODAPE
		<c:import url="/paginas/expediente/modelos/inc_rodapeClassificacaoDocumental.jsp" />
		FIM PRIMEIRO RODAPE -->
		<!-- INICIO RODAPE
		<c:import url="/paginas/expediente/modelos/inc_rodapeNumeracaoADireita.jsp" />
		FIM RODAPE -->
		</body>
		
	</mod:documento>
</mod:modelo>