[CmdletBinding()]
param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$TokenUrl = "http://localhost:9090/realms/minibanking/protocol/openid-connect/token",
    [string]$Username = "customer",
    [string]$Password = "customer123"
)

$ErrorActionPreference = "Stop"

function Wait-HttpEndpoint {
    param(
        [Parameter(Mandatory = $true)][string]$Uri,
        [int]$Attempts = 60,
        [int]$DelaySeconds = 5
    )

    for ($attempt = 1; $attempt -le $Attempts; $attempt++) {
        try {
            Invoke-WebRequest -UseBasicParsing -Uri $Uri -TimeoutSec 5 | Out-Null
            return
        }
        catch {
            if ($attempt -eq $Attempts) {
                throw "Endpoint $Uri nije postao dostupan posle $Attempts pokusaja."
            }
            Start-Sleep -Seconds $DelaySeconds
        }
    }
}

function Invoke-MiniBankingApi {
    param(
        [Parameter(Mandatory = $true)][ValidateSet("GET", "POST", "PUT", "DELETE")][string]$Method,
        [Parameter(Mandatory = $true)][string]$Path,
        [object]$Body
    )

    $parameters = @{
        Method  = $Method
        Uri     = "$BaseUrl$Path"
        Headers = $script:AuthorizationHeaders
    }
    if ($null -ne $Body) {
        $parameters.ContentType = "application/json"
        $parameters.Body = $Body | ConvertTo-Json -Depth 10
    }
    Invoke-RestMethod @parameters
}

function Write-Step([string]$Message) {
    Write-Host "`n==> $Message" -ForegroundColor Cyan
}

Write-Step "Cekam Gateway i Keycloak"
Wait-HttpEndpoint -Uri "$BaseUrl/actuator/health"
Wait-HttpEndpoint -Uri "http://localhost:9090/realms/minibanking/.well-known/openid-configuration"

Write-Step "Pribavljam CUSTOMER JWT"
$tokenResponse = Invoke-RestMethod -Method Post -Uri $TokenUrl `
    -ContentType "application/x-www-form-urlencoded" `
    -Body @{
        client_id  = "minibanking-cli"
        grant_type = "password"
        username   = $Username
        password   = $Password
    }
$script:AuthorizationHeaders = @{ Authorization = "Bearer $($tokenResponse.access_token)" }

$stamp = (Get-Date).ToUniversalTime().ToString("yyyyMMddHHmmss")
$accountSuffixA = [guid]::NewGuid().ToString("N").Substring(0, 20).ToUpperInvariant()
$accountSuffixB = [guid]::NewGuid().ToString("N").Substring(0, 20).ToUpperInvariant()

Write-Step "Kreiram dva klijenta"
$customerA = Invoke-MiniBankingApi -Method POST -Path "/api/customers" -Body @{
    firstName = "Ana"
    lastName  = "Petrovic"
    email     = "ana.$stamp@minibanking.local"
    phone     = "+381 60 111 222"
    address   = "Bulevar oslobodjenja 1, Novi Sad"
    status    = "ACTIVE"
}
$customerB = Invoke-MiniBankingApi -Method POST -Path "/api/customers" -Body @{
    firstName = "Marko"
    lastName  = "Jovanovic"
    email     = "marko.$stamp@minibanking.local"
    phone     = "+381 60 333 444"
    address   = "Knez Mihailova 10, Beograd"
    status    = "ACTIVE"
}
Write-Host "Ana ID:   $($customerA.id)"
Write-Host "Marko ID: $($customerB.id)"

Write-Step "Otvaram RSD racune"
$sourceAccount = Invoke-MiniBankingApi -Method POST -Path "/api/accounts" -Body @{
    customerId    = $customerA.id
    accountNumber = "MB$accountSuffixA"
    type          = "CHECKING"
    currencyCode  = "RSD"
    initialBalance = 1000.00
}
$destinationAccount = Invoke-MiniBankingApi -Method POST -Path "/api/accounts" -Body @{
    customerId    = $customerB.id
    accountNumber = "MB$accountSuffixB"
    type          = "CHECKING"
    currencyCode  = "RSD"
    initialBalance = 100.00
}
Write-Host "Izvorni racun: $($sourceAccount.id), stanje $($sourceAccount.balance) RSD"
Write-Host "Ciljni racun:  $($destinationAccount.id), stanje $($destinationAccount.balance) RSD"

Write-Step "Izdajem tokenizovanu debitnu karticu"
$card = Invoke-MiniBankingApi -Method POST -Path "/api/cards" -Body @{
    customerId    = $customerA.id
    accountId     = $sourceAccount.id
    type          = "DEBIT"
    cardholderName = "ANA PETROVIC"
    dailyLimit    = 50000.00
}
Write-Host "Kartica: $($card.maskedPan), token $($card.panToken)"

Write-Step "Izvrsavam transfer od 125.50 RSD"
$idempotencyKey = [guid]::NewGuid().ToString()
$transferBody = @{
    idempotencyKey      = $idempotencyKey
    sourceAccountId     = $sourceAccount.id
    destinationAccountId = $destinationAccount.id
    amount              = 125.50
    description         = "Demo transfer za odbranu"
}
$transaction = Invoke-MiniBankingApi -Method POST -Path "/api/transactions/transfers" -Body $transferBody
if ($transaction.status -ne "COMPLETED") {
    throw "Transfer nije zavrsen: status=$($transaction.status), razlog=$($transaction.failureReason)"
}
Write-Host "Transakcija: $($transaction.id), status $($transaction.status)"
Write-Host "Nova stanja: $($transaction.sourceBalanceAfter) / $($transaction.destinationBalanceAfter) RSD"

Write-Step "Ponavljam identican zahtev - stanje se ne menja drugi put"
$replay = Invoke-MiniBankingApi -Method POST -Path "/api/transactions/transfers" -Body $transferBody
Write-Host "Isti transaction ID: $($replay.id)"
Write-Host "idempotentReplay: $($replay.idempotentReplay)"

Write-Step "Citam customer overview iz cetiri servisa"
$overview = Invoke-MiniBankingApi -Method GET -Path "/api/overview/customers/$($customerA.id)"
$overview | ConvertTo-Json -Depth 10

Write-Step "Cekam asinhrona RabbitMQ obavestenja"
Start-Sleep -Seconds 2
$notifications = Invoke-MiniBankingApi -Method GET -Path "/api/notifications?customerId=$($customerA.id)"
$notifications | ConvertTo-Json -Depth 10

Write-Step "Proveravam instancu iza Gateway load balancer-a"
$instances = 1..4 | ForEach-Object {
    Invoke-RestMethod -Uri "$BaseUrl/api/overview/instance"
}
$instances | Format-Table

Write-Host "`nMiniBanking demonstracioni scenario je uspesno zavrsen." -ForegroundColor Green
