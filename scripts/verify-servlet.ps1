param([string]$BaseUrl = 'http://localhost:8080')

$ErrorActionPreference = 'Stop'

function New-Client {
    $handler = [System.Net.Http.HttpClientHandler]::new()
    $handler.UseCookies = $true
    $handler.CookieContainer = [System.Net.CookieContainer]::new()
    $handler.AllowAutoRedirect = $false
    return [System.Net.Http.HttpClient]::new($handler)
}

function Get-Page($client, $path) {
    $response = $client.GetAsync("$BaseUrl$path").GetAwaiter().GetResult()
    return @{ Status = [int]$response.StatusCode; Body = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult() }
}

function Post-Form($client, $path, $fields) {
    $pairs = [System.Collections.Generic.List[System.Collections.Generic.KeyValuePair[string,string]]]::new()
    foreach ($key in $fields.Keys) { $pairs.Add([System.Collections.Generic.KeyValuePair[string,string]]::new($key, [string]$fields[$key])) }
    $content = [System.Net.Http.FormUrlEncodedContent]::new($pairs)
    $response = $client.PostAsync("$BaseUrl$path", $content).GetAwaiter().GetResult()
    return @{ Status = [int]$response.StatusCode; Body = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult() }
}

function Assert-Status($actual, $expected, $label) {
    if ($actual.Status -ne $expected) { throw "$label expected HTTP $expected, got $($actual.Status): $($actual.Body.Substring(0, [Math]::Min(300, $actual.Body.Length)))" }
    Write-Host "PASS $label ($expected)"
}

function Sign-In($client, $email, $password) {
    Assert-Status (Post-Form $client '/login' @{ email=$email; password=$password }) 303 "Sign in $email"
    $dashboard = Get-Page $client '/dashboard'
    Assert-Status $dashboard 200 "Dashboard $email"
    $match = [regex]::Match($dashboard.Body, 'name="csrf" value="([^"]+)"')
    if (!$match.Success) { throw 'CSRF token not rendered' }
    return $match.Groups[1].Value
}

$guest = New-Client
Assert-Status (Get-Page $guest '/login') 200 'Login page'
Assert-Status (Get-Page $guest '/dashboard') 303 'Guest protected route'
Assert-Status (Get-Page $guest '/style.css') 200 'Editorial theme CSS'
Assert-Status (Post-Form $guest '/login' @{ email='teacher@school.com'; password='wrong' }) 401 'Bad password'

$teacher = New-Client
$teacherCsrf = Sign-In $teacher 'teacher@school.com' 'teacher123'
foreach ($path in @('/students','/students/new','/results','/results/new','/my-results','/missing')) {
    $expected = if ($path -eq '/missing') { 404 } elseif ($path -eq '/my-results') { 303 } else { 200 }
    Assert-Status (Get-Page $teacher $path) $expected "Teacher GET $path"
}
Assert-Status (Get-Page $teacher '/users') 403 'Teacher blocked from admin users'
Assert-Status (Post-Form $teacher '/students/create' @{ fullName='No Token' }) 403 'CSRF protection'

$studentForm = @{ csrf=$teacherCsrf; email='demo-verify@school.com'; password='password123'; fullName='Demo Verify'; role='STUDENT'; rollNumber='VERIFY-001' }
Assert-Status (Post-Form $teacher '/students/create' $studentForm) 303 'Create student'
Assert-Status (Post-Form $teacher '/students/create' $studentForm) 400 'Duplicate student'
$studentPage = Get-Page $teacher '/students'
if ($studentPage.Body -notmatch 'Demo Verify') { throw 'Created student not listed' }
$row = [regex]::Match($studentPage.Body, '<tr>\s*<td>VERIFY-001</td>[\s\S]*?</tr>').Value
$studentId = [regex]::Match($row, 'edit\?id=(\d+)').Groups[1].Value
if (!$studentId) { throw 'Could not find created student ID' }
Assert-Status (Get-Page $teacher "/students/edit?id=$studentId") 200 'Edit student page'
Assert-Status (Post-Form $teacher '/students/update' @{ csrf=$teacherCsrf; id=$studentId; email='demo-verify@school.com'; password=''; fullName='Demo Updated'; role='STUDENT'; rollNumber='VERIFY-001' }) 303 'Update student'

$resultForm = @{ csrf=$teacherCsrf; studentId=$studentId; subject='History'; marks='75'; maxMarks='100'; examName='Annual'; academicYear='2025-26' }
Assert-Status (Post-Form $teacher '/results/create' $resultForm) 303 'Create result'
Assert-Status (Post-Form $teacher '/results/create' $resultForm) 400 'Duplicate result'
$invalid = $resultForm.Clone(); $invalid['marks'] = '101'
Assert-Status (Post-Form $teacher '/results/create' $invalid) 400 'Invalid marks'
$resultPage = Get-Page $teacher '/results?q=History'
if ($resultPage.Body -notmatch 'Demo Updated') { throw 'Search did not find saved result' }
$row = [regex]::Match($resultPage.Body, '<tr>\s*<td>VERIFY-001</td>[\s\S]*?</tr>').Value
$resultId = [regex]::Match($row, '/report\?id=(\d+)').Groups[1].Value
if (!$resultId) { throw 'Could not find created result ID' }
Assert-Status (Get-Page $teacher "/report?id=$resultId") 200 'Report card'
Assert-Status (Get-Page $teacher "/results/edit?id=$resultId") 200 'Edit result page'
$updated = $resultForm.Clone(); $updated['id'] = $resultId; $updated['marks'] = '85'
Assert-Status (Post-Form $teacher '/results/update' $updated) 303 'Update result'
Assert-Status (Post-Form $teacher '/results/delete' @{csrf=$teacherCsrf;id=$resultId}) 303 'Delete result'
Assert-Status (Post-Form $teacher '/students/delete' @{csrf=$teacherCsrf;id=$studentId}) 303 'Delete student'

$student = New-Client
$studentCsrf = Sign-In $student 'student@school.com' 'student123'
Assert-Status (Get-Page $student '/my-results') 200 'Student results'
Assert-Status (Get-Page $student '/students') 403 'Student blocked from staff page'
Assert-Status (Get-Page $student '/results') 403 'Student blocked from results management'
$myResults = Get-Page $student '/my-results'
$ownResultId = [regex]::Match($myResults.Body, '/report\?id=(\d+)').Groups[1].Value
if (!$ownResultId) { throw 'Seeded student result missing' }
Assert-Status (Get-Page $student "/report?id=$ownResultId") 200 'Student own report'
Assert-Status (Post-Form $student '/logout' @{csrf=$studentCsrf}) 303 'Student logout'
Assert-Status (Get-Page $student '/my-results') 303 'Logged-out protected route'

$admin = New-Client
$adminCsrf = Sign-In $admin 'admin@school.com' 'admin123'
Assert-Status (Get-Page $admin '/users') 200 'Admin accounts'
Assert-Status (Get-Page $admin '/users/new') 200 'Admin new-account form'
Assert-Status (Post-Form $admin '/users/create' @{csrf=$adminCsrf; email='verify-teacher@school.com'; password='password123'; fullName='Verify Teacher'; role='TEACHER';rollNumber=''}) 303 'Create teacher'
$newTeacher = New-Client
$null = Sign-In $newTeacher 'verify-teacher@school.com' 'password123'
$usersPage = Get-Page $admin '/users'
$row = [regex]::Match($usersPage.Body, '<tr>\s*<td>Verify Teacher</td>[\s\S]*?</tr>').Value
$userId = [regex]::Match($row, '/users/edit\?id=(\d+)').Groups[1].Value
if (!$userId) { throw 'Created teacher not found' }
Assert-Status (Get-Page $admin "/users/edit?id=$userId") 200 'Edit teacher page'
Assert-Status (Post-Form $admin '/users/update' @{csrf=$adminCsrf;id=$userId;email='verify-teacher@school.com';password='newpassword123';fullName='Verify Updated';role='TEACHER';rollNumber=''}) 303 'Update teacher password'
Assert-Status (Get-Page $newTeacher '/dashboard') 303 'Old session revoked after password change'
Assert-Status (Post-Form $admin '/users/create' @{csrf=$adminCsrf; email='verify-student@school.com'; password='password123'; fullName='<script>alert(1)</script>'; role='STUDENT';rollNumber='VERIFY-002'}) 303 'Create XSS test student'
$studentsPage = Get-Page $admin '/students'
if ($studentsPage.Body -match '<script>alert\(1\)</script>' -or $studentsPage.Body -notmatch '&lt;script&gt;alert\(1\)&lt;/script&gt;') { throw 'Student name was not escaped in JSP' }
$row = [regex]::Match($studentsPage.Body, '<tr>\s*<td>VERIFY-002</td>[\s\S]*?</tr>').Value
$otherStudentId = [regex]::Match($row, 'edit\?id=(\d+)').Groups[1].Value
if (!$otherStudentId) { throw 'Other student missing' }
Assert-Status (Post-Form $admin '/results/create' @{csrf=$adminCsrf;studentId=$otherStudentId;subject='Physics';marks='80';maxMarks='100';examName='Annual';academicYear='2025-26'}) 303 'Create other student result'
$otherResults = Get-Page $admin '/results?q=Physics'
$row = [regex]::Match($otherResults.Body, '<tr>\s*<td>VERIFY-002</td>[\s\S]*?</tr>').Value
$otherResultId = [regex]::Match($row, '/report\?id=(\d+)').Groups[1].Value
if (!$otherResultId) { throw 'Other result missing' }
$student = New-Client
$null = Sign-In $student 'student@school.com' 'student123'
Assert-Status (Get-Page $student "/report?id=$otherResultId") 403 'Student blocked from another report'
Assert-Status (Post-Form $admin '/students/delete' @{csrf=$adminCsrf;id=$otherStudentId}) 303 'Cascade-delete XSS test student'
Assert-Status (Post-Form $admin '/users/delete' @{csrf=$adminCsrf;id=$userId}) 303 'Delete teacher'

Write-Host 'All Servlet/JSP flow checks passed.'
