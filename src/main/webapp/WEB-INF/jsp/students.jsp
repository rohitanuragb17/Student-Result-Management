<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="title" value="Students"/><%@ include file="common/header.jspf" %>
<div class="page-head"><div><span class="eyebrow">Academic registry · Directory</span><h1>Students</h1><p class="subtitle">Create, view, update and delete student details.</p></div><a class="button" href="${pageContext.request.contextPath}/students/new"><span aria-hidden="true">+</span> Add student</a></div>
<c:if test="${not empty message}"><div class="alert success" role="alert"><c:out value="${message}"/></div></c:if>
<c:choose><c:when test="${empty students}"><div class="card empty">No students found.</div></c:when><c:otherwise>
<div class="table-wrap students-table"><table><thead><tr><th>Roll no.</th><th>Student name</th><th>Email</th><th>Actions</th></tr></thead><tbody>
<c:forEach var="student" items="${students}"><tr><td><c:out value="${student.rollNumber}"/></td><td><c:out value="${student.fullName}"/></td><td><c:out value="${student.email}"/></td><td class="actions">
<a class="button secondary small" href="${pageContext.request.contextPath}/students/edit?id=${student.id}">Edit</a>
<form method="post" action="${pageContext.request.contextPath}/students/delete" onsubmit="return confirm('Delete student and all linked results?')"><input type="hidden" name="csrf" value="<c:out value='${csrf}'/>"><input type="hidden" name="id" value="${student.id}"><button class="danger" type="submit">Delete</button></form>
</td></tr></c:forEach></tbody></table></div>
</c:otherwise></c:choose>
<%@ include file="common/footer.jspf" %>
