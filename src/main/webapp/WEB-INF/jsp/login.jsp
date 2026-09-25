<%@ page contentType="text/html; charset=UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Sign in | Student Result Management</title><link rel="stylesheet" href="${pageContext.request.contextPath}/style.css?v=3"></head>
<body class="login-page"><main class="login-shell">
<section class="login-intro"><div class="login-brand"><span class="brand-mark">SR</span><span>Student Result Management<small>Institutional archives</small></span></div>
<div><span class="eyebrow light">Academic portal</span><h2>Knowledge,<br><em>beautifully</em> recorded.</h2><p>A thoughtful, secure home for results, records and every academic milestone.</p></div>
<div class="login-note">Scholarship · Progress · Integrity</div></section>
<section class="login-card"><div class="login-logo">SR</div><span class="eyebrow">Welcome back</span><h1>Sign in to continue</h1>
<p class="subtitle">Use your Admin, Teacher or Student account.</p>
<c:if test="${not empty error}"><div class="alert error" role="alert"><c:out value="${error}"/></div></c:if>
<form method="post" action="${pageContext.request.contextPath}/login">
<div class="field"><label for="email">Email address</label><input id="email" name="email" type="email" required autocomplete="email" placeholder="you@school.com"></div>
<div class="field"><label for="password">Password</label><input id="password" name="password" type="password" required autocomplete="current-password"></div>
<button type="submit">Sign in</button></form>
</section></main></body></html>
