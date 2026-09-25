<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="title" value="${isEdit ? 'Edit result' : 'Add result'}"/><%@ include file="common/header.jspf" %>
<div class="page-head"><div><span class="eyebrow">Academic registry · Evaluation</span><h1><c:out value="${title}"/></h1><p class="subtitle">Enter marks and exam details.</p></div></div>
<section class="card form-card"><c:if test="${not empty error}"><div class="alert error" role="alert"><c:out value="${error}"/></div></c:if>
<form method="post" action="${pageContext.request.contextPath}/results/${isEdit ? 'update' : 'create'}"><input type="hidden" name="csrf" value="<c:out value='${csrf}'/>"><c:if test="${isEdit}"><input type="hidden" name="id" value="${edit.id}"></c:if>
<div class="form-grid"><div class="field full"><label for="studentId">Student</label><select id="studentId" name="studentId" required><option value="">Choose student</option>
<c:forEach var="student" items="${students}"><option value="${student.id}" ${values.studentId == student.id ? 'selected' : ''}><c:out value="${student.rollNumber}"/> — <c:out value="${student.fullName}"/></option></c:forEach></select></div>
<div class="field"><label for="subject">Subject</label><input id="subject" name="subject" required maxlength="80" value="<c:out value='${values.subject}'/>"></div>
<div class="field"><label for="examName">Exam name</label><input id="examName" name="examName" required maxlength="80" value="<c:out value='${values.examName}'/>"></div>
<div class="field"><label for="marks">Marks obtained</label><input id="marks" name="marks" type="number" required min="0" value="<c:out value='${values.marks}'/>"></div>
<div class="field"><label for="maxMarks">Maximum marks</label><input id="maxMarks" name="maxMarks" type="number" required min="1" value="<c:out value='${values.maxMarks}'/>"></div>
<div class="field"><label for="academicYear">Academic year</label><input id="academicYear" name="academicYear" required pattern="20[0-9]{2}-[0-9]{2}" value="<c:out value='${values.academicYear}'/>"></div></div>
<div class="form-actions"><button type="submit">Save result</button><a class="button secondary" href="${pageContext.request.contextPath}/results">Cancel</a></div></form></section>
<%@ include file="common/footer.jspf" %>
