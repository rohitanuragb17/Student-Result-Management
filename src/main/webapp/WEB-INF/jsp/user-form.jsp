<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="title" value="${isEdit ? 'Edit account' : 'Add account'}"/><%@ include file="common/header.jspf" %>
<div class="page-head"><div><span class="eyebrow"><c:out value="${studentOnly ? 'Academic registry' : 'Access control'}"/></span><h1><c:out value="${isEdit ? 'Edit' : 'Add'}"/> <c:out value="${studentOnly ? 'student' : 'user'}"/></h1><p class="subtitle">Complete the required fields below.</p></div></div>
<section class="card form-card"><c:if test="${not empty error}"><div class="alert error" role="alert"><c:out value="${error}"/></div></c:if>
<form method="post" action="${pageContext.request.contextPath}/${studentOnly ? 'students' : 'users'}/${isEdit ? 'update' : 'create'}">
<input type="hidden" name="csrf" value="<c:out value='${csrf}'/>"><c:if test="${isEdit}"><input type="hidden" name="id" value="${edit.id}"></c:if>
<div class="form-grid">
<div class="field"><label for="fullName">Full name</label><input id="fullName" name="fullName" required maxlength="100" value="<c:out value='${values.fullName}'/>"></div>
<div class="field"><label for="email">Email address</label><input id="email" name="email" type="email" required maxlength="120" value="<c:out value='${values.email}'/>"></div>
<c:choose><c:when test="${studentOnly}"><input type="hidden" name="role" value="STUDENT"></c:when><c:otherwise>
<div class="field"><label for="role">Role</label><select id="role" name="role"><option value="ADMIN" ${values.role == 'ADMIN' ? 'selected' : ''}>ADMIN</option><option value="TEACHER" ${values.role == 'TEACHER' ? 'selected' : ''}>TEACHER</option><option value="STUDENT" ${values.role == 'STUDENT' ? 'selected' : ''}>STUDENT</option></select></div>
</c:otherwise></c:choose>
<div class="field"><label for="rollNumber">Roll number <c:if test="${not studentOnly}">(students only)</c:if></label><input id="rollNumber" name="rollNumber" maxlength="30" value="<c:out value='${values.rollNumber}'/>"></div>
<div class="field full"><label for="password">Password <c:if test="${isEdit}">(leave blank to keep current)</c:if></label><input id="password" name="password" type="password" minlength="8" ${isEdit ? '' : 'required'}></div>
</div><div class="form-actions"><button type="submit">Save</button><a class="button secondary" href="${pageContext.request.contextPath}/${studentOnly ? 'students' : 'users'}">Cancel</a></div></form></section>
<%@ include file="common/footer.jspf" %>
