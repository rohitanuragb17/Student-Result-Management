<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="title" value="My results"/><%@ include file="common/header.jspf" %>
<div class="page-head"><div><span class="eyebrow">Your academic record</span><h1>My result archive</h1><p class="subtitle">Your saved report cards and marks.</p></div></div>
<c:choose><c:when test="${empty results}"><div class="card empty">No results have been saved yet.</div></c:when><c:otherwise>
<div class="table-wrap"><table><thead><tr><th>Subject</th><th>Marks</th><th>Percentage</th><th>Grade</th><th>Exam</th><th>Year</th><th></th></tr></thead><tbody>
<c:forEach var="result" items="${results}"><tr><td><c:out value="${result.subject}"/></td><td>${result.marks}/${result.maxMarks}</td><td>${result.percentageLabel}</td><td><span class="badge ${result.status == 'Pass' ? 'pass' : 'fail'}">${result.grade} · ${result.status}</span></td><td><c:out value="${result.examName}"/></td><td><c:out value="${result.academicYear}"/></td><td><a class="button secondary small" href="${pageContext.request.contextPath}/report?id=${result.id}">Report</a></td></tr></c:forEach>
</tbody></table></div>
</c:otherwise></c:choose>
<%@ include file="common/footer.jspf" %>
