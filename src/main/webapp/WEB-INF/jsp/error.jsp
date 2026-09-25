<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="title" value="Error"/><%@ include file="common/header.jspf" %>
<div class="error-page"><h1>${status}</h1><p class="subtitle"><c:out value="${error}"/></p><p><a class="button" href="${pageContext.request.contextPath}/dashboard">Back to dashboard</a></p></div>
<%@ include file="common/footer.jspf" %>
