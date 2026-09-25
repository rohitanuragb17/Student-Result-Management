<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="title" value="Results"/><%@ include file="common/header.jspf" %>
<div class="page-head"><div><span class="eyebrow">Academic registry · Chronicle</span><h1>Results &amp; archives</h1><p class="subtitle">Manage marks and retrieve previously stored result records.</p></div><a class="button" href="${pageContext.request.contextPath}/results/new"><span aria-hidden="true">+</span> Add result</a></div>
<c:if test="${not empty message}"><div class="alert success" role="alert"><c:out value="${message}"/></div></c:if>
<div class="toolbar"><form class="search" method="get" action="${pageContext.request.contextPath}/results"><label class="sr-only" for="resultSearch">Search results</label><input id="resultSearch" name="q" value="<c:out value='${search}'/>" placeholder="Search student, roll no., subject or exam"><button type="submit">Search</button></form></div>
<c:choose><c:when test="${empty results}"><div class="card empty">No result records found.</div></c:when><c:otherwise>
<div class="table-wrap results-table"><table><thead><tr><th>Roll no.</th><th>Student</th><th>Subject</th><th>Marks</th><th>Grade</th><th>Exam</th><th>Actions</th></tr></thead><tbody>
<c:forEach var="result" items="${results}"><tr><td><c:out value="${result.rollNumber}"/></td><td><c:out value="${result.studentName}"/></td><td><c:out value="${result.subject}"/></td><td>${result.marks}/${result.maxMarks}</td><td><span class="badge ${result.status == 'Pass' ? 'pass' : 'fail'}">${result.grade} · ${result.status}</span></td><td><c:out value="${result.examName}"/><br><span class="muted"><c:out value="${result.academicYear}"/></span></td><td class="actions">
<a class="button secondary small" href="${pageContext.request.contextPath}/report?id=${result.id}">View</a><a class="button secondary small" href="${pageContext.request.contextPath}/results/edit?id=${result.id}">Edit</a>
<form method="post" action="${pageContext.request.contextPath}/results/delete" onsubmit="return confirm('Delete this result?')"><input type="hidden" name="csrf" value="<c:out value='${csrf}'/>"><input type="hidden" name="id" value="${result.id}"><button class="danger" type="submit">Delete</button></form>
</td></tr></c:forEach></tbody></table></div>
</c:otherwise></c:choose>
<%@ include file="common/footer.jspf" %>
