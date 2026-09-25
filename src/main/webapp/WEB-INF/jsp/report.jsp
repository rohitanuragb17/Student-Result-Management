<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="title" value="Report card"/><%@ include file="common/header.jspf" %>
<div class="page-head print-hidden"><div><span class="eyebrow">Academic record</span><h1>Report card</h1><p class="subtitle">A clear view of academic performance.</p></div><button class="button" type="button" onclick="window.print()">Print report</button></div>
<section class="card report"><div class="report-heading"><div><span class="brand-mark">SR</span><h2>Academic report card</h2><p class="muted">Student Result Management</p></div><div class="report-student"><span class="avatar large"><c:out value="${studentInitials}"/></span><div><strong><c:out value="${selected.studentName}"/></strong><br><span class="muted"><c:out value="${selected.rollNumber}"/></span></div></div></div>
<div class="report-meta"><div><span class="muted">Examination</span><br><strong><c:out value="${selected.examName}"/></strong></div><div><span class="muted">Academic year</span><br><strong><c:out value="${selected.academicYear}"/></strong></div><div><span class="muted">Last updated</span><br><strong><c:out value="${updated}"/></strong></div></div>
<div class="table-wrap"><table><thead><tr><th>Subject</th><th>Maximum</th><th>Obtained</th><th>Percentage</th><th>Grade</th><th>Status</th></tr></thead><tbody>
<c:forEach var="item" items="${grouped}"><tr><td><c:out value="${item.subject}"/></td><td>${item.maxMarks}</td><td>${item.marks}</td><td>${item.percentageLabel}</td><td>${item.grade}</td><td><span class="badge ${item.status == 'Pass' ? 'pass' : 'fail'}">${item.status}</span></td></tr></c:forEach>
</tbody></table></div>
<div class="summary"><div><span>Total marks</span><strong>${obtained}/${maximum}</strong></div><div><span>Percentage</span><strong><c:out value="${percentage}"/></strong></div><div><span>Result status</span><strong class="${overallStatus == 'Passed' ? 'success-text' : 'warning-text'}"><c:out value="${overallStatus}"/></strong></div></div></section>
<%@ include file="common/footer.jspf" %>
