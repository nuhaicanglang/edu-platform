$ErrorActionPreference = "Continue"
function Test-API($name, $method, $url, $body, $headers) {
    try {
        $params = @{ Uri = $url; Method = $method; UseBasicParsing = $true }
        if ($body) {
            $params.Body = [System.Text.Encoding]::UTF8.GetBytes($body)
            $params.ContentType = "application/json; charset=utf-8"
        }
        if ($headers) { $params.Headers = $headers }
        $r = Invoke-WebRequest @params
        $content = [System.Text.Encoding]::UTF8.GetString($r.RawContentStream.ToArray())
        $label = if ($content -match '"code":200') { "PASS" } else { "WARN" }
        Write-Host "$label [$name] $(if ($content.Length -gt 120) { $content.Substring(0,120)+'...' } else { $content })"
        return $content
    } catch {
        $msg = $_.Exception.Message
        try { $msg = $_.ErrorDetails.Message } catch {}
        Write-Host "FAIL [$name] $msg"
        return $null
    }
}

Write-Output "===== AUTH TESTS ====="
Test-API "Register teacher2" "POST" "http://127.0.0.1:8081/auth/register" '{"username":"teacher2","password":"123456","realName":"Li Teacher","role":"teacher","userCode":"T002"}' $null
Test-API "Register student2" "POST" "http://127.0.0.1:8081/auth/register" '{"username":"student2","password":"123456","realName":"Wang Student","role":"student","userCode":"S002"}' $null

$loginResp = Test-API "Login teacher1" "POST" "http://127.0.0.1:8081/auth/login" '{"username":"teacher1","password":"123456"}' $null
$json = $loginResp | ConvertFrom-Json
$token = $json.data.token
Write-Output "Token: $($token.Substring(0,30))..."

Test-API "Get user info" "GET" "http://127.0.0.1:8081/auth/info" $null @{"X-User-Id"="8"}

Write-Output ""
Write-Output "===== COURSE TESTS ====="
Test-API "Create course" "POST" "http://127.0.0.1:8082/system/course" '{"courseName":"Java Programming","courseCode":"CS101","description":"Intro to Java","category":"theory","credit":3.0,"classHours":48}' @{"X-User-Id"="8"}
Test-API "List courses" "GET" "http://127.0.0.1:8082/system/course/list" $null $null
Test-API "Page courses" "GET" "http://127.0.0.1:8082/system/course/page?pageNum=1&pageSize=10" $null $null
Test-API "My courses" "GET" "http://127.0.0.1:8082/system/course/my" $null @{"X-User-Id"="8"}

Write-Output ""
Write-Output "===== CLASS GROUP TESTS ====="
Test-API "Create class" "POST" "http://127.0.0.1:8082/system/class" '{"className":"Java Class A","courseId":1,"semester":"2025-2026-1","studentCount":0}' @{"X-User-Id"="8"}
Test-API "Page classes" "GET" "http://127.0.0.1:8082/system/class/page?pageNum=1&pageSize=10" $null $null
Test-API "Add student to class" "POST" "http://127.0.0.1:8082/system/class/1/student/9" $null $null

Write-Output ""
Write-Output "===== ASSIGNMENT TESTS ====="
Test-API "Create assignment" "POST" "http://127.0.0.1:8082/system/assignment" '{"title":"Java Basics HW","description":"Complete exercises","courseId":1,"classId":1,"assignmentType":"text","totalScore":100,"status":"published","deadline":"2026-12-31 23:59:59"}' @{"X-User-Id"="8"}
Test-API "Page assignments" "GET" "http://127.0.0.1:8082/system/assignment/page?pageNum=1&pageSize=10" $null $null

Write-Output ""
Write-Output "===== KNOWLEDGE TESTS ====="
Test-API "Page documents" "GET" "http://127.0.0.1:8084/knowledge/documents?pageNum=1&pageSize=10" $null $null
Test-API "Search knowledge" "GET" "http://127.0.0.1:8084/knowledge/search?keyword=java" $null $null

Write-Output ""
Write-Output "===== AGENT TESTS ====="
Test-API "QA ask-simple" "POST" "http://127.0.0.1:8083/agent/qa/ask-simple" '{"question":"What is Java?"}' $null

Write-Output ""
Write-Output "===== GATEWAY ROUTING TESTS ====="
$gwLogin = Test-API "GW->Auth login" "POST" "http://127.0.0.1:9000/api/auth/login" '{"username":"teacher1","password":"123456"}' $null
$gwJson = $gwLogin | ConvertFrom-Json
$gwToken = if ($gwJson) { $gwJson.data.token } else { "" }
$authH = @{"Authorization"="Bearer $gwToken"}
Test-API "GW->System courses" "GET" "http://127.0.0.1:9000/api/system/course/list" $null $authH
Test-API "GW->Knowledge docs" "GET" "http://127.0.0.1:9000/api/knowledge/documents?pageNum=1&pageSize=10" $null $authH
Test-API "GW->Agent qa" "POST" "http://127.0.0.1:9000/api/agent/qa/ask-simple" '{"question":"Hello"}' $authH

Write-Output ""
Write-Output "===== ALL TESTS DONE ====="
