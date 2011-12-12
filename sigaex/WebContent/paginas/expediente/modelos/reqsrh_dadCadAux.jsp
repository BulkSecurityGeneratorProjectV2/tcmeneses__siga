<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	buffer="64kb"%>
<%@ taglib tagdir="/WEB-INF/tags/mod" prefix="mod"%>
<%@ taglib uri="http://localhost/sigatags" prefix="siga"%>
<%@ taglib uri="/WEB-INF/tld/func.tld" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<mod:modelo>
	<mod:entrevista>
	<br>
	    <br>
		<span style="color:red"> <b>ESTE DOCUMENTO DEVER� SER ENVIADO � DIMED</b></span>
		<br/><br/>
		<mod:grupo titulo="DADOS DO MAGISTRADO/SERVIDOR BENEFICI�RIO TITULAR">
			<mod:grupo>
				
				<mod:texto titulo="Ramal" var="ramal" largura="15"/>
		
				<mod:texto titulo="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Plano de Sa�de a que est� vinculado" var ="plano"/>
				
			</mod:grupo>
		</mod:grupo>
		<br>
		<hr style="color: #FFFFFF;" />
		<mod:selecao var="contdep"
				titulo="Quantidade de Dependentes"
				opcoes="1;2;3;4;5"
				reler="sim"  /><br/>
		<mod:grupo depende="contDependAjax">
				<c:forEach var="i" begin="1" end="${contdep}">
		           <mod:grupo titulo="Dependente n� ${i}">
			           <mod:grupo>
				           <mod:texto titulo="Nome" var="nom${i}" largura="60"/>
		               </mod:grupo>
			           <mod:texto titulo="Parentesco" var="paren${i}" largura="20"/>
				
			           <mod:grupo>
				           <mod:data titulo="Data de Nascimento" var="dtNasc${i}"/>
			               <mod:selecao titulo="Estado Civil" var="estado${i}" opcoes="Solteiro;Solteira;Casado;Casada;Vi�vo;Vi�va;Outros" reler="n�o" />
			           </mod:grupo>
			           <mod:grupo>
				           <mod:texto titulo="N� Identidade" var="iden${i}" largura="20" obrigatorio="Nao"/>
				           <mod:texto titulo="Org�o Expedidor" var ="OrgExp${i}" obrigatorio="Nao"/>
				           <mod:data titulo="Data de Expedi��o" var="dtExp${i}" obrigatorio="Nao"/>
			           </mod:grupo>
			       </mod:grupo> 
			       <hr style="color: #FFFFFF;" />
				</c:forEach>    
		</mod:grupo>								
			
	        <br>	
	        
	        <hr style="color: #FFFFFF;" />
		<mod:selecao var="contdoc"
				titulo="Quantidade de Documentos Comprobat�rios"
				opcoes="1;2;3;4;5"
				reler="sim"  /><br/>
		<mod:grupo depende="contDependAjax">
				<c:forEach var="i" begin="1" end="${contdoc}">
		              <mod:grupo>
				          <mod:texto titulo="Documento n� ${i}" var="dcmt${i}" largura="60"/>
		               </mod:grupo>
	            </c:forEach>    
	    </mod:grupo>        
	        
			<mod:grupo>			
			<mod:caixaverif titulo="Declaro estar ciente dos termos do capitulo IV da resolu�ao n� 2 de 2008 do Conselho da Justi�a
					Federal, que regulamenta a assist�ncia � saude prevista no art. 2320 da lei n� &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;8.112, de 1990, 
					com a reda��o dada pela lei 11.032, de 2006, e de que  o respectivo auxilio ser� pago mediante
					reembolso.<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Declaro, ainda que os benefici�rios acima relacionados n�o recebem  aux�lio semelhante, nem participam de outro 
					programa de assist�ncia sa�de, custeado pelos cofres p�blicos, &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ainda que em partes.<br><br>
					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Declaro, por fim, estar ciente de que a documenta��o sobre a operadora/contrato do contrato do plano de saude
					ser� apresentada ap�s a regulamenta��o da mat�ria pelo TRF - &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2� Regi�o." var="decl" marcado="Sim" obrigatorio="Sim" reler="Nao"/>
			</mod:grupo>
			<br>
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<mod:obrigatorios />
			<br><br>
			
			<mod:grupo titulo="Resolu��o 02/2008/CJF:<br/><br>
					Art. 42. S� far� jus ao ressarcimento o
					benefici�rio que n�o receber aux�lio semelhante e nem participar de outro 
					programa de assist�ncia � sa�de de servidor, custeado pelos cofres p�blicos, 
					ainda que em parte.<br/><br>
					Art. 43. S�o benefici�rios do aux�lio:<br/>
					I - na qualidade de titulares: <br/>
 					a)     magistrados e servidores ativos e inativos, inclu�dos os cedidos e ocupantes apenas de cargo comissionado no Conselho e �rg�os da Justi�a Federal de primeiro e segundo graus;
					b)     pensionistas estatut�rios.<br/>
					II - na qualidade de dependente do titular:<br/>
					a)     o c�njuge, o companheiro ou companheira de uni�o est�vel;<br/>
					b)     a pessoa desquitada, separada judicialmente ou divorciada, que perceba pens�o aliment�cia;<br/>
					c)     os filhos e enteados, solteiros, at� 21 (vinte e um) anos de idade ou, se inv�lidos, enquanto durar a invalidez;<br/>
					d)     os filhos e enteados, entre 21 (vinte e um) e 24 (vinte e quatro) anos de idade, dependentes econ�micos do magistrado ou servidor e estudantes de curso regular reconhecido pelo Minist�rio da  Educa��o;<br/>
					e)     o menor sob guarda ou tutela concedida por decis�o judicial.<br/><br>
					Art. 45. S�o documentos indispens�veis para inscri��o:<br>
					I - c�pia autenticada do contrato celebrado entre o benefici�rio titular e a operadora de planos de sa�de ou o original seguido de c�pia, a ser conferida pelo servidor respons�vel;<br/>
					II - comprovante de que a operadora de planos de sa�de contratada pelo servidor est� regular e autorizada pela Ag�ncia Nacional de Sa�de (ANS);<br>
					III - declara��o para fins de cumprimento do art. 42 desta Resolu��o;<br>
					IV - documentos oficiais que comprovem a situa��o de  depend�ncia, (...)"/>
			
		 	
		<br>	
	</mod:entrevista>
	<mod:documento>
	<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
		<head>
			<style type="text/css">
			@page {
				margin-left: 1cm;
				margin-right: 0.5cm;
				margin-top: 1cm;
				margin-bottom: 2cm;
			}
			</style>
		</head>
		<body>
		<c:set var="pessoa" value ="${f:pessoa(requestScope['pessoa_pessoaSel.id'])}" />
		<c:set var="vinc" value="vinculado"/>
		<c:if test="${doc.subscritor.sexo == 'F'}">
			<c:set var="vinc" value="vinculada"></c:set>
		</c:if>
				<!-- INICIO PRIMEIRO CABECALHO
		<table width="100%" border="0" bgcolor="#FFFFFF"><tr><td>
		<c:import url="/paginas/expediente/modelos/inc_cabecalhoCentralizadoPrimeiraPagina.jsp" />
		</td></tr>
		
		</td></tr>
			<tr bgcolor="#FFFFFF">
				<td width="100%">
					<table width="100%">
						<tr> 
							<td align="center"><p style="font-family:Arial;font-size:11pt;font-weight:bold;"><br/>
						DADOS CADASTRAIS PARA O AUX&Iacute;LIO-SA&Uacute;DE<br/></td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
		FIM PRIMEIRO CABECALHO -->
		<!-- INICIO CABECALHO
		<c:import url="/paginas/expediente/modelos/inc_cabecalhoEsquerda.jsp" />
		FIM CABECALHO -->
<br/>
		<table border="1" cellpadding="5" width="96%"style="font-size:19" >
			<tr>
    			<th colspan="2" align="left" style="font-size:10"><b>1-DADOS DO MAGISTRADOS/SERVIDOR BENEFICI�RIO(TITULAR)</b></th>
  			</tr>
  			<tr >
  				<td>
  				<p style="font-family:Arial;font-size:9pt">
  				Nome: ${doc.subscritor.descricao}&nbsp;_________&nbsp; Matricula: ${doc.subscritor.matricula}<br/>
				Cargo: ${doc.subscritor.cargo.descricao} &nbsp;_________&nbsp;Quadro de Pessoal:&nbsp;TRF<br/>
				Lota��o: ${doc.subscritor.lotacao.descricao}  &nbsp;_________&nbsp;Ramal: ${ramal}<br/>
				Plano de Sa&uacute;de a que est&aacute; ${vinc}: ${plano}
				</p>
				</td>
  			</tr>
  		</table>
  		<br/>
		<table border="1" cellpadding="5" width="96%"style="font-size:10" >
	  		<tr>
  				<td colspan="2" align="left" style="font-size:10"><b>2-DADOS DOS DEPENDENTES</b></td>
  			</tr>
  			<c:forEach var="i" begin="1" end="${contdep}">
  			   <c:set var="nomm" value="${requestScope[(f:concat('nom',i))]}" />
	 		   <c:set var="parenn" value="${requestScope[(f:concat('paren',i))]}" />
	 		   <c:set var="estd" value="${requestScope[(f:concat('estado',i))]}" />
	 	       <c:set var="dtNascc" value="${requestScope[(f:concat('dtNasc',i))]}" />
	 	       <c:set var="idenn" value="${requestScope[(f:concat('iden',i))]}" />
	 		   <c:set var="OrgExpp" value="${requestScope[(f:concat('OrgExp',i))]}" />
	 		   <c:set var="dtExpp" value="${requestScope[(f:concat('dtExp',i))]}" />
	 		  
  			<tr>
  				<td>
  				    <p style="font-family:Arial;font-size:9pt">
  					Nome: ${nomm}  <br> Parentesco: ${parenn} &nbsp;_____&nbsp;
  					Estado Civil: ${estd} &nbsp;_____&nbsp;
  					Data de Nascimento: ${dtNascc} &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <br>
  					N� Identidade: ${idenn} &nbsp;_____&nbsp; 
  					&Oacute;rg&atilde;o Expedidor: ${OrgExpp} &nbsp;_____&nbsp;
  					Data de Expedi&ccedil;&atilde;o: ${dtExpp}<br/>
  					</p>
  				</td>
  			</tr>
  			</c:forEach>
		</table>
		
		<br/>
		
		<table border="1" cellpadding="5" width="96%"style="font-size:10">
			<tr>
				<th colspan="2" align="left" style="font-size:9"><b>3-DECLARA&Ccedil;&Atilde;O </b></th>
			</tr>
			<tr >
				<td >
					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Declaro estar ciente dos termos do capitulo IV da resolu��o n� 2 de 2008 do Conselho da Justi�a
					Federal, que regulamenta a assist�ncia � saude prevista no art. 2320 da lei n� 8.112, de 1990, 
					com a reda��o dada pela lei 11.032, de 2006, e de que  o respectivo aux�lio ser� pago mediante
					reembolso.<br/>
					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Declaro, ainda que os beneficiados acima relacionados n�o recebem  aux�lio semelhante, nem participam de outro 
					programa de assist�ncia sa�de, custeado pelos cofres p�blicos, ainda que em partes.<br/>
					<b>
					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Declaro, por fim, estar ciente de que a documenta��o sobre a operadora/contrato do contrato do plano de sa�de
					ser� apresentada ap�s a regulamenta��o da mat�ria pelo TRF - 2� Regi�o.
					</b> 
				</td>
			</tr>
		</table>
		<br>
		<table border="1" cellpadding="5" width="96%"style="font-size:9">
			<tr>
				<th colspan="2" align="left" style="font-size:10"><b>4- ANEXOS </b></th>
			</tr>
			<tr >
				<td >
					Em anexo seguem os documentos que comprovam a situa��o de depend�ncia: ${rel}<br/>
					<c:if test="${contdoc > '0'}">
		        	   <c:forEach var="i" begin="1" end="${contdoc}">
				          <c:set var="docs" value="${requestScope[(f:concat('dcmt',i))]}" />
				          ${i})- <u>${docs}</u><br>		    
			           </c:forEach>
		            </c:if>
				</td>
			</tr>
			<tr align="center">
				<td>${doc.dtExtenso}<br><br><br><c:import url="/paginas/expediente/modelos/inc_assinatura.jsp" /></td>
			</tr>
		</table>
		
		<br><br><br>
		<table border="1" cellpadding="5" width="96%"style="font-size:9">
			<tr>
				<th colspan="2" align="left" style="font-size:9"><b>5- OBSERVA��ES </b></th>
			</tr>
			<tr >
				<td >
					Res. 02/2008/CJF:<br/> 
					"Art. 42. S� far� jus ao ressarcimento o
					benefici�rio que n�o receber aux�lio semelhante e nem participar de outro 
					programa de assist�ncia � sa�de de servidor, custeado pelos cofres p�blicos, 
					ainda que em parte.<br/>
					Art. 43. S�o benefici�rios do aux�lio:<br/>
					I - na qualidade de titulares: <br/>
 					a)     magistrados e servidores ativos e inativos, inclu�dos os cedidos e ocupantes apenas de cargo comissionado no Conselho e �rg�os da Justi�a Federal de primeiro e segundo graus;
					b)     pensionistas estatut�rios.<br/>
					II - na qualidade de dependente do titular:<br/>
					a)     o c�njuge, o companheiro ou companheira de uni�o est�vel;<br/>
					b)     a pessoa desquitada, separada judicialmente ou divorciada, que perceba pens�o aliment�cia;<br/>
					c)     os filhos e enteados, solteiros, at� 21 (vinte e um) anos de idade ou, se inv�lidos, enquanto durar a invalidez;<br/>
					d)     os filhos e enteados, entre 21 (vinte e um) e 24 (vinte e quatro) anos de idade, dependentes econ�micos do magistrado ou servidor e estudantes de curso regular reconhecido pelo Minist�rio da  Educa��o;<br/>
					e)     o menor sob guarda ou tutela concedida por decis�o judicial.<br/>
					Art. 45. S�o documentos indispens�veis para inscri��o:
					I - c�pia autenticada do contrato celebrado entre o benefici�rio titular e a operadora de planos de sa�de ou o original seguido de c�pia, a ser conferida pelo servidor respons�vel;<br/>
					II - comprovante de que a operadora de planos de sa�de contratada pelo servidor est� regular e autorizada pela Ag�ncia Nacional de Sa�de (ANS);
					III - declara��o para fins de cumprimento do art. 42 desta Resolu��o;
					IV - documentos oficiais que comprovem a situa��o de  depend�ncia, (...)"
				</td>
			</tr>
		</table>

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