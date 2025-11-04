# Chemin du dossier de données MongoDB, relatif au script
$dbPath = Join-Path $PSScriptRoot "mongo"

# Vérifier que le répertoire existe
if (-not (Test-Path $dbPath -PathType Container)) {
    Write-Error "Le répertoire MongoDB n'existe pas : $dbPath"
    exit 1
}

# Lancer MongoDB
mongod.exe --dbpath $dbPath
