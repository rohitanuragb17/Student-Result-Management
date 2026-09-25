<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="title" value="Dashboard"/><%@ include file="common/header.jspf" %>
<div class="page-head hero-head"><div><span class="eyebrow"><c:out value="${user.role}"/> overview</span><h1>Good day, <c:out value="${firstName}"/>.</h1><p class="subtitle">Here’s what is happening in your academic portal.</p></div><div class="date-card"><span>Academic year</span><strong><c:out value="${academicYear}"/></strong></div></div>
<c:choose><c:when test="${user.role == 'STUDENT'}">
<div class="stats"><div class="card stat-card"><span class="stat-icon">R</span><div><span class="stat-number">${resultCount}</span><span class="stat-label">Saved results</span></div></div>
<div class="card stat-card"><span class="stat-icon">S</span><div><span class="stat-number stat-text"><c:out value="${user.role}"/></span><span class="stat-label">Account access</span></div></div>
<div class="card stat-card"><span class="stat-icon">#</span><div><span class="stat-number stat-text"><c:out value="${user.rollNumber}"/></span><span class="stat-label">Roll number</span></div></div></div>
<div class="quick-grid"><a class="card quick-link" href="${pageContext.request.contextPath}/my-results"><span class="quick-icon">↗</span><h2>My report cards</h2><p>View marks, grades and archived results.</p></a>
<div class="card accent-card"><span class="eyebrow">Privacy protected</span><h2>Your records stay yours.</h2><p class="muted">Your account can access only results linked to your profile.</p></div></div>
</c:when><c:otherwise>
<div class="stats"><div class="card stat-card"><span class="stat-icon">S</span><div><span class="stat-number">${studentCount}</span><span class="stat-label">Total students</span></div></div>
<div class="card stat-card"><span class="stat-icon">R</span><div><span class="stat-number">${resultCount}</span><span class="stat-label">Saved results</span></div></div>
<div class="card stat-card"><span class="stat-icon">A</span><div><span class="stat-number stat-text"><c:out value="${user.role}"/></span><span class="stat-label">Current access</span></div></div></div>
<div class="quick-grid"><a class="card quick-link" href="${pageContext.request.contextPath}/students"><span class="quick-icon">+</span><h2>Manage students</h2><p>Create and maintain student details.</p></a>
<a class="card quick-link" href="${pageContext.request.contextPath}/results"><span class="quick-icon">↗</span><h2>Manage results</h2><p>Add marks, update records and browse archives.</p></a>
<c:if test="${user.role == 'ADMIN'}"><a class="card quick-link" href="${pageContext.request.contextPath}/users"><span class="quick-icon">◎</span><h2>User access</h2><p>Create accounts and assign roles.</p></a></c:if></div>
</c:otherwise></c:choose>
<%@ include file="common/footer.jspf" %>
