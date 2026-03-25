$package = "C:\Users\Lenovo\Desktop\n8n\back3\baziback\deploy\docker-server-package-20260324-204751.tar.gz"

if (!(Test-Path $package)) {
  Write-Error "Package not found: $package"
  exit 1
}

scp $package root@35.198.208.41:/root/
