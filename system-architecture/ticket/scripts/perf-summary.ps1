param(
    [string]$PrometheusUrl = "http://localhost:9090",
    [string]$Window = "1m"
)

$ErrorActionPreference = "Stop"

function Invoke-PrometheusQuery {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Query
    )

    $encoded = [System.Uri]::EscapeDataString($Query)
    $uri = "$PrometheusUrl/api/v1/query?query=$encoded"
    $response = Invoke-RestMethod -Method Get -Uri $uri

    if ($response.status -ne "success") {
        throw "Prometheus query failed: $Query"
    }

    if ($response.data.result.Count -eq 0) {
        return $null
    }

    return [double]$response.data.result[0].value[1]
}

function Format-Number {
    param(
        [Nullable[Double]]$Value,
        [string]$Unit
    )

    if ($null -eq $Value) {
        return "N/A"
    }

    switch ($Unit) {
        "ms" { return "{0:N2} ms" -f ($Value * 1000) }
        "percent" { return "{0:N2} %" -f ($Value * 100) }
        "mb" { return "{0:N2} MB" -f ($Value / 1024 / 1024) }
        "count" { return "{0:N0}" -f $Value }
        default { return "{0:N2}" -f $Value }
    }
}

$queries = @(
    [pscustomobject]@{
        Name = "API RPS"
        Query = "sum(rate(http_server_requests_seconds_count{job=`"ticket-api`"}[$Window]))"
        Unit = "number"
    },
    [pscustomobject]@{
        Name = "API P95"
        Query = "histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{job=`"ticket-api`"}[$Window])) by (le))"
        Unit = "ms"
    },
    [pscustomobject]@{
        Name = "API P99"
        Query = "histogram_quantile(0.99, sum(rate(http_server_requests_seconds_bucket{job=`"ticket-api`"}[$Window])) by (le))"
        Unit = "ms"
    },
    [pscustomobject]@{
        Name = "API 2xx RPS"
        Query = "sum(rate(http_server_requests_seconds_count{job=`"ticket-api`",status=~`"2..`"}[$Window]))"
        Unit = "number"
    },
    [pscustomobject]@{
        Name = "API 4xx RPS"
        Query = "sum(rate(http_server_requests_seconds_count{job=`"ticket-api`",status=~`"4..`"}[$Window]))"
        Unit = "number"
    },
    [pscustomobject]@{
        Name = "API 5xx Error Rate"
        Query = "sum(rate(http_server_requests_seconds_count{job=`"ticket-api`",status=~`"5..`"}[$Window])) / clamp_min(sum(rate(http_server_requests_seconds_count{job=`"ticket-api`"}[$Window])), 1)"
        Unit = "percent"
    },
    [pscustomobject]@{
        Name = "API CPU"
        Query = "process_cpu_usage{job=`"ticket-api`"}"
        Unit = "percent"
    },
    [pscustomobject]@{
        Name = "Consumer CPU"
        Query = "process_cpu_usage{job=`"ticket-consumer`"}"
        Unit = "percent"
    },
    [pscustomobject]@{
        Name = "API JVM Memory"
        Query = "sum(jvm_memory_used_bytes{job=`"ticket-api`"})"
        Unit = "mb"
    },
    [pscustomobject]@{
        Name = "Consumer JVM Memory"
        Query = "sum(jvm_memory_used_bytes{job=`"ticket-consumer`"})"
        Unit = "mb"
    },
    [pscustomobject]@{
        Name = "API Live Threads"
        Query = "jvm_threads_live_threads{job=`"ticket-api`"}"
        Unit = "count"
    },
    [pscustomobject]@{
        Name = "Consumer Live Threads"
        Query = "jvm_threads_live_threads{job=`"ticket-consumer`"}"
        Unit = "count"
    }
)

Write-Host ""
Write-Host "Ticket Performance Summary"
Write-Host "Prometheus: $PrometheusUrl"
Write-Host "Window: $Window"
Write-Host ""

$queries |
    ForEach-Object {
        $value = Invoke-PrometheusQuery -Query $_.Query
        [pscustomobject]@{
            Metric = $_.Name
            Value = Format-Number -Value $value -Unit $_.Unit
        }
    } |
    Format-Table -AutoSize
