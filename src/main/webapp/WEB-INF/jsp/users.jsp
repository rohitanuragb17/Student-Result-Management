<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="title" value="Users"/><%@ include file="common/header.jspf" %>
<div class="page-head"><div><span class="eyebrow">Access control &amp; authorization</span><h1>User accounts</h1><p class="subtitle">Admin-only account and role management.</p></div><a class="button" href="${pageContext.request.contextPath}/users/new"><span aria-hidden="true">+</span> Add user</a></div>
<c:if test="${not empty message}"><div class="alert success" role="alert"><c:out value="${message}"/></div></c:if>
<c:choose><c:when test="${empty users}"><div class="card empty">No users found.</div></c:when><c:otherwise>
<div class="table-wrap users-table"><table><thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Roll no.</th><th>Actions</th></tr></thead><tbody>
<c:forEach var="account" items="${users}"><tr><td><c:out value="${account.fullName}"/></td><td><c:out value="${account.email}"/></td><td><span class="badge"><c:out value="${account.role}"/></span></td><td><c:out value="${empty account.rollNumber ? '—' : account.rollNumber}"/></td><td class="actions">
<a class="button secondary small" href="${pageContext.request.contextPath}/users/edit?id=${account.id}">Edit</a>
<c:if test="${account.id != user.id}"><form method="post" action="${pageContext.request.contextPath}/users/delete" onsubmit="return confirm('Delete user? Student results will also be deleted.')"><input type="hidden" name="csrf" value="<c:out value='${csrf}'/>"><input type="hidden" name="id" value="${account.id}"><button class="danger" type="submit">Delete</button></form></c:if>
</td></tr></c:forEach></tbody></table></div>
</c:otherwise></c:choose>
<%@ include file="common/footer.jspf" %>
