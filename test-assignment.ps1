[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$ErrorActionPreference = "Stop"

# 1. Login as teacher
Write-Host "=== 1. Teacher Login ===" -ForegroundColor Cyan
$login = Invoke-RestMethod -Uri http://localhost:9000/api/auth/login -Method POST -ContentType "application/json; charset=utf-8" -Body '{"username":"teacher1","password":"123456"}'
$token = $login.data.token
$h = @{Authorization="Bearer $token"}
Write-Host "OK userId=$($login.data.userId) role=$($login.data.role)"

# 2. Get assignment by ID
Write-Host "`n=== 2. Get Assignment by ID ===" -ForegroundColor Cyan
$a = Invoke-RestMethod -Uri "http://localhost:9000/api/system/assignment/46" -Headers $h
Write-Host "OK id=$($a.data.id) title=$($a.data.title) attachmentUrl=$($a.data.attachmentUrl)"

# 3. List all submissions for assignment 46
Write-Host "`n=== 3. List All Submissions ===" -ForegroundColor Cyan
$subs = Invoke-RestMethod -Uri "http://localhost:9000/api/system/assignment/46/all-submissions" -Headers $h
Write-Host "OK count=$($subs.data.Count)"
foreach ($s in $subs.data) {
    Write-Host "  sub id=$($s.id) student=$($s.studentName) status=$($s.gradingStatus) score=$($s.score)"
}

# 4. Create assignment with file (no actual file, just params)
Write-Host "`n=== 4. Create Assignment (no file) ===" -ForegroundColor Cyan
$boundary = [System.Guid]::NewGuid().ToString()
$body = @"
--$boundary
Content-Disposition: form-data; name="title"

API Test Assignment
--$boundary
Content-Disposition: form-data; name="courseId"

1
--$boundary
Content-Disposition: form-data; name="totalScore"

100
--$boundary
Content-Disposition: form-data; name="assignmentType"

homework
--$boundary--
"@
$create = Invoke-RestMethod -Uri "http://localhost:9000/api/system/assignment/create-with-file" -Method POST -Headers $h -ContentType "multipart/form-data; boundary=$boundary" -Body $body
Write-Host "OK newId=$($create.data.id) title=$($create.data.title)"

Write-Host "`n=== ALL TESTS PASSED ===" -ForegroundColor Green
