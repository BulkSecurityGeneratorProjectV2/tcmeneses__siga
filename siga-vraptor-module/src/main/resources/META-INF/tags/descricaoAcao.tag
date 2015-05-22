<%@ tag body-content="scriptless"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ attribute name="acao" required="false"%>

<c:choose>
	<c:when test="${acao != null}">
		${acao.tituloAcao}
	</c:when>
	<c:otherwise>
		A��o n�o informada
	</c:otherwise>
</c:choose>