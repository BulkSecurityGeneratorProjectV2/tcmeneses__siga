<%@ tag body-content="scriptless"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ attribute name="itemConfiguracao" required="false"%>

<c:choose>
	<c:when test="${itemConfiguracao != null}">
		${itemConfiguracao.tituloItemConfiguracao}	
	</c:when>
	
	<c:otherwise>
		Item n�o informado
	</c:otherwise>
</c:choose>